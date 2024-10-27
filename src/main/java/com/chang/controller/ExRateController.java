package com.chang.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;

import com.chang.bean.ExRateBean;
import com.chang.exception.EntityNotFoundException;
import com.chang.exception.IllegalInputException;
import com.chang.service.impl.ExRateServiceJPAImplement;

@RestController
@RequestMapping("api/exrate")
public class ExRateController {
	
	@Autowired
	private ExRateServiceJPAImplement service;
	
	@GetMapping("/coindeskapi")
	public String getCcyInfoByCoindeskApi() {
		return service.getCcyInfoByCoindeskApi();
	}
	
	@GetMapping("/customapi")
	public Map<String, Object> getCcyInfoByCustomApi() {
		return service.getCcyInfoByCustomApi();
	}
	
	@GetMapping("/all")
	public List<ExRateBean> getAllCcy() {
		return service.findAll();
	}
	
	@RequestMapping(value = "{condition}", method = RequestMethod.GET)
	public ExRateBean getExRateByCcy(@PathVariable String condition) {
			
		if(condition == null || condition.equals("")) {
			throw new IllegalInputException("請輸入正確查詢條件");
		} else if (condition.matches("[A-Z]{3}")) {
			
			//	若輸入值為三碼英文，判斷為輸入幣別代碼，以幣別代碼去做查詢
			ExRateBean ccyCodeResult = service.findByCcyCode(condition);
			
			if(ccyCodeResult == null) {
				throw new EntityNotFoundException("查無此幣別");
			} else {
				return ccyCodeResult;
			}
		} else {
			
			//	其餘輸入值一律視為以幣別中文名稱去做查詢
			ExRateBean ccyNameResult = service.findByCcyName(condition);
			
			if(ccyNameResult == null) {
				throw new EntityNotFoundException("查無此幣別");
			} else {
				return ccyNameResult;
			}
		}
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public ExRateBean addOrEditExRate(@RequestBody ExRateBean exRateBean) {
		
		//	若查得到幣別，視為修改該幣別，否則視為新增幣別
		if (service.findByCcyCode(exRateBean.getCcyCode()) == null) {
			return service.addCcy(exRateBean);
		} else {
			return service.updateCcy(exRateBean);
		}
	}
	
	@RequestMapping(value = "{ccyCode}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public String deleteCourse(@PathVariable String ccyCode) {
		boolean isSuccess = service.deleteByCcyCode(ccyCode);
		if (isSuccess) {
			return "刪除成功";
		} else {
			return "刪除失敗";
		}
	}
}
