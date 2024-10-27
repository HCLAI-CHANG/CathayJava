package com.chang.service;

import java.util.List;
import java.util.Map;

import com.chang.bean.ExRateBean;

public interface ExRateService {

	String getCcyInfoByCoindeskApi();

	Map<String, Object> getCcyInfoByCustomApi();

	List<ExRateBean> findAll();

	ExRateBean findByCcyCode(String ccyCode);

	ExRateBean findByCcyName(String ccyName);
	
	ExRateBean addCcy(ExRateBean exRateBean);

	ExRateBean updateCcy(ExRateBean exRateBean);

	boolean deleteByCcyCode(String ccyCode);

}
