package com.loohos.factoryinspection.dao;

import java.util.List;

public class PageView<T> {
	// 分页数据
	private List<T> records;
	// 页码开始索引
	private PageIndex pageIndex;
	// 总页数
	private long totalPages = 1;
	// 每页显示的记录数量
	private int maxResult = 12;
	// 当前页
	private int currentPage = 1;
	// 总记录数
	private long totalRecords;
	// 导航页数，即每页显示多少页的超链接
	private int viewPageCount = 10;

	public PageView(int maxResult, int currentPage) {
		this.maxResult = maxResult;
		this.currentPage = currentPage;
	}

	public PageView(int maxResult, int currentPage, int viewPageCount) {
		this.maxResult = maxResult;
		this.currentPage = currentPage;
		this.viewPageCount = viewPageCount;
	}

	/**
	 * 计算开始索引
	 */
	public int getFistIndex() {
		return (currentPage - 1) * maxResult;
	}

	public List<T> getRecords() {
		return records;
	}

	public void setRecords(List<T> records) {
		this.records = records;
	}

	public PageIndex getPageIndex() {
		return pageIndex;
	}

	public long getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
		this.pageIndex = PageIndex.getPageIndex(viewPageCount, currentPage, totalPages);
	}

	public int getMaxResult() {
		return maxResult;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public long getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(long totalRecords) {
		this.totalRecords = totalRecords;
		setTotalPages(this.totalRecords % this.maxResult == 0 ? (this.totalRecords / this.maxResult)
				: (this.totalRecords / this.maxResult + 1));
	}
	
	public void setQueryResult(QueryResult queryResult){
		setTotalRecords(queryResult.getTotalRecords());
		setRecords(queryResult.getResultList());
	}

	public int getViewPageCount() {
		return viewPageCount;
	}

	public void setViewPageCount(int viewPageCount) {
		this.viewPageCount = viewPageCount;
	}

}
