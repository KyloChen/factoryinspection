package com.loohos.factoryinspection.dao;

import java.util.List;

/**
 * 查询数据库需返回的信息
 * @author Administrator
 * @param <T> 查询的实体bean
 */
public class QueryResult<T> {
	//与实体bean查询相关的结果集
	private List<T> resultList;
	//与实体bean查询相关的总记录数
	private Long totalRecords;
	
	public List<T> getResultList() {
		return resultList;
	}
	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}
	public Long getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(Long totalRecords) {
		this.totalRecords = totalRecords;
	}
	
	
	
}
