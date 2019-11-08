package com.loohos.factoryinspection.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;

public interface DAO<T> {
	/**获取总记录数**/
	public long getCount();
	/**
	 * 把托管状态变成游离态,清除一级缓存
	 */
	public void clear();
	
	/**
	 * 保存实体
	 * @param entity 实体ID
	 */
	public void save(T entity);
	
	/**
	 * 更新实体

	 */
	public void update(T entity);
	
	/**
	 * 删除实体

	 */
	public void delete(Serializable... entityIds); //JPA规范规定实体的ID属性必须实现序列化接口
	
	/**
	 * 查询实体
	 * @param entityId 实体ID
	 * @return
	 */
	//public <T> T find(Class<T> entityClass, Object entityId);
	public T find(Serializable entityId);
	/**
	 * 获取分页数据
	 *
	 * @param firstIndex 开始索引
	 * @param maxResult 需要获取的记录数
	 *@param orderBy 实体属性,ASC/DESC orderby Key1 desc, key2 asc 
	 *用linkedhashmap的作用是为了让排序有顺序，因为hashmap会自动重排序，而linkedhashmap不会 
	 * 返回查询结果对象
	 */
	
	public QueryResult<T> getScrollData(int firstIndex, int maxResult, String whereJpql, Object[] queryParams, LinkedHashMap<String, String> orderBy);
	
	public QueryResult<T> getScrollData(int firstIndex, int maxResult, String whereJpql, Object[] queryParams);
	
	public QueryResult<T> getScrollData(int firstIndex, int maxResult, LinkedHashMap<String, String> orderBy);
	
	public QueryResult<T> getScrollData(int firstIndex, int maxResult);
	
	public QueryResult<T> getScrollData();
}
