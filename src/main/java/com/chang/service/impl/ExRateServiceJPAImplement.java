package com.chang.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.chang.bean.ExRateBean;
import com.chang.exception.DuplicatedNameException;
import com.chang.exception.EntityNotFoundException;
import com.chang.exception.IllegalInputException;
import com.chang.repository.ExRateRepository;
import com.chang.service.ExRateService;

@Service
public class ExRateServiceJPAImplement implements ExRateService {

	private static final String COINDESK_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";

	private RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private ExRateRepository repository;

	@Override
	public String getCcyInfoByCoindeskApi() {

		// 呼叫coindesk API，取得json物件並回傳
		String currentCcyInfo = restTemplate.getForObject(COINDESK_URL, String.class);

		// 同步將資料更新至資料庫
		this.updateAllCcy(currentCcyInfo);

		return currentCcyInfo;
	}

	@Override
	public Map<String, Object> getCcyInfoByCustomApi() {

		// 呼叫coindesk API以取得json物件
		String response = restTemplate.getForObject(COINDESK_URL, String.class);
		JSONObject coindeskObj = new JSONObject(response);

		// 轉換時間格式
		String updateTimeStr = coindeskObj.getJSONObject("time").getString("updatedISO");

		// 將字串解析為ISO時間
		OffsetDateTime utcTime = OffsetDateTime.parse(updateTimeStr);

		// 轉換時區為台灣 (UTC+8)
		LocalDateTime taiwanTime = utcTime.atZoneSameInstant(ZoneId.of("Asia/Taipei")).toLocalDateTime();

		// 格式化時間格式為: 1990/01/01 00:00:00
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String formattedTime = taiwanTime.format(formatter);

		// 遍歷bpi物件中的所有幣別
		JSONObject bpiObj = coindeskObj.getJSONObject("bpi");

		// new一個HashMap來裝返回結果
		Map<String, Object> customResult = new HashMap<>();
		customResult.put("更新時間", formattedTime);

		// 遍歷bpi物件中的所有幣別，並設置幣別、幣別中文名稱、匯率
		for (String c : bpiObj.keySet()) {
			JSONObject ccyObj = bpiObj.getJSONObject(c);

			Map<String, Object> ccy = new HashMap<>();

			ccy.put("幣別", ccyObj.getString("code"));

			switch (ccyObj.getString("code")) {
			case "USD":
				ccy.put("幣別中文名稱", "美金");
				break;
			case "GBP":
				ccy.put("幣別中文名稱", "英鎊");
				break;
			case "EUR":
				ccy.put("幣別中文名稱", "歐元");
				break;
			default:
				break;
			}

			ccy.put("匯率", ccyObj.getDouble("rate_float"));

			customResult.put(c, ccy);
		}

		// 同步更新至資料庫
		this.updateAllCcy(response);

		return customResult;
	}

	@Override
	public List<ExRateBean> findAll() {

		List<ExRateBean> allExRate = repository.findAll();

		if (allExRate.size() != 0) {
			return repository.findAll();
		} else {
			throw new EntityNotFoundException("查無幣別資訊");
		}
	}

	@Override
	public ExRateBean findByCcyCode(String ccyCode) {

		return repository.findByCcyCode(ccyCode);
	}

	@Override
	public ExRateBean findByCcyName(String ccyName) {

		return repository.findByCcyName(ccyName);
	}

	@Override
	public ExRateBean addCcy(ExRateBean exRateBean) {
		
		//	檢核傳入的格式是否正確
		if(exRateBean.getCcyCode().length() != 3) {
			throw new IllegalInputException("請輸入正確幣別代碼");
		}
		
		if(exRateBean.getCcyName().length() > 30 || exRateBean.getCcyName().trim().equals("") || exRateBean.getCcyName() == null) {
			throw new IllegalInputException("請輸入正確幣別中文名稱");
		}
		
		if(exRateBean.getExRate() == null || exRateBean.getExRate() <= 0) {
			throw new IllegalInputException("請輸入有效的匯率");
		}		
		
		for(ExRateBean e : repository.findAll()) {
			if (e.getCcyCode().equals(exRateBean.getCcyCode())) {
				throw new DuplicatedNameException("幣別不可重複");
			}
			
			if(e.getCcyName().equals(exRateBean.getCcyName())) {
				throw new DuplicatedNameException("幣別中文名稱不可重複");
			}
		}
		
		//	實例化物件並存入資料庫
		ExRateBean newExRateBean = new ExRateBean();
		newExRateBean.setCcyCode(exRateBean.getCcyCode());
		newExRateBean.setCcyName(exRateBean.getCcyName());
		newExRateBean.setExRate(exRateBean.getExRate());
		newExRateBean.setUpdateTime(LocalDateTime.now());
		
		return repository.save(newExRateBean);
	}

	@Override
	public ExRateBean updateCcy(ExRateBean exRateBean) {
		
		//	檢核傳入的格式是否正確
		if (exRateBean.getCcyName().length() > 30 || exRateBean.getCcyName().trim().equals("") || exRateBean.getCcyName() == null) {
			throw new IllegalInputException("請輸入正確幣別中文名稱");
		}

		if(exRateBean.getExRate() == null || exRateBean.getExRate() <= 0) {
			throw new IllegalInputException("請輸入有效的匯率");
		}	
		
		//	先將原本的幣別資訊剔除，再檢核幣別中文名稱是否有跟剩下的幣別中文名稱重複
		List<ExRateBean> allExRate = repository.findAll();
		allExRate.remove(repository.findByCcyCode(exRateBean.getCcyCode()));
		
		for (ExRateBean e : allExRate) {
			if (e.getCcyName().equals(exRateBean.getCcyName())) {
				throw new DuplicatedNameException("幣別中文名稱不可重複");
			}
		}

		//	實例化物件並存入資料庫
		ExRateBean updatedExRateBean = repository.findByCcyCode(exRateBean.getCcyCode());
		updatedExRateBean.setCcyName(exRateBean.getCcyName());
		updatedExRateBean.setExRate(exRateBean.getExRate());

		return repository.save(updatedExRateBean);
	}

	@Override
	@Transactional
	public boolean deleteByCcyCode(String ccyCode) {

		ExRateBean exRateEntity = repository.findByCcyCode(ccyCode);

		if (exRateEntity != null) {
			repository.deleteByCcyCode(ccyCode);
			return true;
		} else {
			throw new EntityNotFoundException("刪除失敗，無此幣別");
		}
	}

	// 呼叫 coindesk API 或 資料轉換 API 時同步更新資料庫
	private void updateAllCcy(String coindeskApiResponse) {

		// 將呼叫 coindesk API 後取得的json字串拆解成可以存進資料庫的格式
		JSONObject coindeskObj = new JSONObject(coindeskApiResponse);

		// 將更新時間轉換為ISO格式的台灣時間
		String updateTimeStr = coindeskObj.getJSONObject("time").getString("updatedISO");
		LocalDateTime updateTime = OffsetDateTime.parse(updateTimeStr).atZoneSameInstant(ZoneId.of("Asia/Taipei"))
				.toLocalDateTime();

		// 取得bpi物件，並將幣別代碼、幣別中文名稱、更新時間等資訊放入ExRateBean，再存入資料庫
		JSONObject bpiObj = coindeskObj.getJSONObject("bpi");

		for (String c : bpiObj.keySet()) {
			JSONObject ccyObj = bpiObj.getJSONObject(c);

			// 先以bpi物件的"code"進資料庫做查詢，若查詢不到視為新增
			ExRateBean exRateEntity = repository.findByCcyCode(ccyObj.getString("code"));

			if (exRateEntity == null) {
				ExRateBean newExRateBean = new ExRateBean();
				newExRateBean.setCcyCode(ccyObj.getString("code"));

				switch (ccyObj.getString("code")) {
				case "USD":
					newExRateBean.setCcyName("美金");
					break;
				case "GBP":
					newExRateBean.setCcyName("英鎊");
					break;
				case "EUR":
					newExRateBean.setCcyName("歐元");
					break;
				default:
					break;
				}

				newExRateBean.setExRate(ccyObj.getDouble("rate_float"));
				newExRateBean.setUpdateTime(updateTime);

				repository.save(newExRateBean);
			} else {

				// 若查詢得到則更新匯率及更新時間
				exRateEntity.setExRate(ccyObj.getDouble("rate_float"));
				exRateEntity.setUpdateTime(updateTime);

				repository.save(exRateEntity);
			}

		}

	}
}
