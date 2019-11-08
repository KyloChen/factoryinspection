package com.loohos.factoryinspection.dao;

import com.loohos.factoryinspection.utils.GenericUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;


@Transactional
public class DaoSupport<T> implements DAO<T> {
	@PersistenceContext
    protected EntityManager em;
	protected Class<T> entityClass = GenericUtils.getSuperClassGenericType(this.getClass());
	
	/**
	 * 分离方法，把托管态变成游离态
	 */
	public void clear(){
		em.clear();
	}
	
	public void delete(Serializable ... entityids) {
		for(Object id : entityids){
			em.remove(em.getReference(this.entityClass, id));
		}
	}
	
	@Transactional(readOnly=true,propagation= Propagation.NOT_SUPPORTED)
	public T find(Serializable entityId) {
		if(entityId==null) throw new RuntimeException(this.entityClass.getName()+ ":传入的实体id不能为空");
		return em.find(entityClass, entityId);
	}
	
	public void save(T entity) {
		em.persist(entity);
	}
	
	@Transactional(readOnly=true,propagation= Propagation.NOT_SUPPORTED)
	public long getCount() {
		return (Long)em.createQuery("select count("+ getCountField(this.entityClass) +") from "+ getEntityName(this.entityClass)+ " o").getSingleResult();
	}
	
	public void update(T entity) {
		em.merge(entity);
	}
	
	/**
	 * 分页
	 */
	@Transactional(readOnly=true,propagation= Propagation.NOT_SUPPORTED)
	public QueryResult<T> getScrollData(int firstIndex, int maxResult,
			String whereJpql, Object[]queryParams,LinkedHashMap<String, String> orderBy) {
		QueryResult<T> queryResult=new QueryResult<T>();
		String entityName=getEntityName(entityClass);
		//要分页，先查询,对所有的实体bean都通用
		Query query=em.createQuery("select o from " +entityName +" o "+(whereJpql==null || "".equals(whereJpql.trim())?"":" where "+whereJpql) + buildOrderBy(orderBy));
		setQueryParams(query,queryParams);
		if(firstIndex>=0 && maxResult>0)
		{
			query.setFirstResult(firstIndex);
			query.setMaxResults(maxResult);
		}
		queryResult.setResultList(query.getResultList());//返回一个list
		//得到总记录数
		//query=em.createQuery("select count(o) from " +entityName+" o"+(whereJpql==null || "".equals(whereJpql.trim())?"": " where " +whereJpql));
		query = em.createQuery("select count("+ getCountField(this.entityClass)+ ") from "+ entityName+ " o "+(whereJpql==null || "".equals(whereJpql.trim())? "": "where "+ whereJpql));
		setQueryParams(query,queryParams);
		queryResult.setTotalRecords((Long)query.getSingleResult()); //总数是单行
		return queryResult;
	}

	protected void setQueryParams(Query query, Object[] queryParams){
		if(queryParams!=null &&queryParams.length>0){
			for(int i=0;i<queryParams.length;i++){
				query.setParameter(i+1, queryParams[i]);
			}
		}
	}
	/**
	 * 组装order by 语句
	 * @param orderBy
	 * @return
	 */
	protected String buildOrderBy(LinkedHashMap<String, String> orderBy){
		StringBuilder orderBySequence=new StringBuilder();
		if(orderBy!=null&& !orderBy.isEmpty()){
			orderBySequence.append(" order by ");
			for(String key:orderBy.keySet()){
				orderBySequence.append("o.").append(key).append(" ").append(orderBy.get(key)).append(",");
			}
			orderBySequence.deleteCharAt(orderBySequence.length()-1); //最后多一个逗号，删除
		}
		return orderBySequence.toString();
	}
	
	/**
	 * 获取实体的名称
	 * @param <T>
	 * @param entityClass 实体类
	 * @return
	 */
	protected static <T> String getEntityName(Class<T> entityClass){
		String entityName=entityClass.getSimpleName();
		//获取entity注解后的名字声明
		Entity entity=entityClass.getAnnotation(Entity.class);
		if(entity.name()!=null &&!"".equals(entity.name())){
			entityName=entity.name();
		}
		
		return entityName;
	}

	@Transactional(readOnly=true,propagation= Propagation.NOT_SUPPORTED)
	public QueryResult<T> getScrollData(int firstIndex, int maxResult,LinkedHashMap<String, String> orderBy) {
		return getScrollData(firstIndex, maxResult, null, null, orderBy);
	}

	@Transactional(readOnly=true,propagation= Propagation.NOT_SUPPORTED)
	public QueryResult<T> getScrollData(int firstIndex, int maxResult, String whereJpql,Object[] queryParams) {
		
		return getScrollData(firstIndex, maxResult, whereJpql, queryParams,null);
	}

	@Transactional(readOnly=true,propagation= Propagation.NOT_SUPPORTED)
	public QueryResult<T> getScrollData(int firstIndex, int maxResult) {
		return getScrollData(firstIndex, maxResult, null, null,null);
	}

	@Transactional(readOnly=true,propagation= Propagation.NOT_SUPPORTED)
	public QueryResult<T> getScrollData() {
		return getScrollData(-1, -1, null, null,null);
	}
	
	/**
	 * 获取统计属性，该方法是为了解决hibernate解析联合主键select count(0) from xxx o语句BUG而增加，hibernate对此jqpl解析后的sql为
	 * select count(field1,field2,....)，显示使用count()统计多个字段是错误的
	 * @param <E>
	 * @param clazz
	 * @return
	 */
	protected static <E> String getCountField(Class<E> clazz){
		String out = "o";
		try {
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			for(PropertyDescriptor propertydesc : propertyDescriptors){
				Method method = propertydesc.getReadMethod();
				if(method!=null && method.isAnnotationPresent(EmbeddedId.class)){
					PropertyDescriptor[] ps = Introspector.getBeanInfo(propertydesc.getPropertyType()).getPropertyDescriptors();
					out = "o."+ propertydesc.getName()+ "." + (!ps[1].getName().equals("class")? ps[1].getName(): ps[0].getName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return out;
	}
}
