package com.loohos.factoryinspection.dao;

/**
 * 功能：计算页码
 * 因为每页只显示10个页码，需要计算jsp页面上的显示从哪一页开始，到哪一页结束。计算后的返回结果有两个值：起始页码firstIndex和结束页码endIndex，
 * PageIndex类中的两个属性可以接收这个结果，所以返回一个PageIndex类型的对象 
 * @author Scott Van
 * 
 */
public class WebTool {
	/**
	 * 
	 * @param viewPageCount 每页显示的导航页数，形如 第一页 第二页 第三页   
	 * @param currentPage 当前页
	 * @param totalPages 总页数
	 * @return
	 */

	public static PageIndex getPageIndex(long viewPageCount, int currentPage,
			long totalPages) {
		long startPage = currentPage
				- (viewPageCount % 2 == 0 ? viewPageCount / 2 - 1
						: viewPageCount / 2);
		long endPage = currentPage + viewPageCount / 2;
		if (startPage < 1) {
			startPage = 1;
			if (totalPages >= viewPageCount)
				endPage = viewPageCount;
			else
				endPage = totalPages;
		}
		if (endPage > totalPages) {
			endPage = totalPages;
			if ((endPage - viewPageCount) > 0)
				startPage = endPage - viewPageCount + 1;
			else
				startPage = 1;
		}
		return new PageIndex(startPage, endPage);
	}
}
