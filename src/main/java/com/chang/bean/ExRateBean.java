package com.chang.bean;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exrate")
public class ExRateBean {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;	
	
	@Column(name = "ccy_code", nullable = false, unique = true, length = 3)
	private String ccyCode;
	
	@Column(name = "ccy_name", nullable = false, unique = true, length = 30)
	private String ccyName;
	
	@Column(name = "exchange_rate", nullable = false)
	private Double exRate;
	
	@Column(name = "update_time", nullable = false)
	private LocalDateTime updateTime;
}
