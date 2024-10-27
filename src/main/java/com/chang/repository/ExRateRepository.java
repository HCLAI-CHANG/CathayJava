package com.chang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chang.bean.ExRateBean;

@Repository
public interface ExRateRepository extends JpaRepository<ExRateBean, Long>{

	ExRateBean findByCcyCode(String ccy);
	
	ExRateBean findByCcyName(String ccyName);
	
	void deleteByCcyCode(String ccyCode);
}
