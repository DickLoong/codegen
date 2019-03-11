package com.lizongbo.codegentool.csv2db;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <b>功能：存放单条数据记录中有差异的地方</b><br>
 * <br>
 * <b>完整路径：</b> com.lizongbo.mgamedevtools.DiffInfo <br>
 * <b>创建日期：</b> 2015-3-31 下午1:50:30 <br>
 * 
 * @author <a href="mailto:quickli@tencent.com">quickli</a><br>
 *         <a href="http://www.tencent.com">Shenzhen Tencent Co.,Ltd.</a>
 * @version 1.0, 2015-3-31
 * @since MqqGame V1.0
 */
public class DiffInfo
{
	private String fid = "";// 有差异的记录id
	private List<DiffColInfo> diffCols = new ArrayList<DiffColInfo>();
	private List<SameColInfo> sameCols = new ArrayList<SameColInfo>();

	/**
	 * @return the fid
	 */
	public String getFid()
	{
		return fid = (fid == null) ? "" : fid.trim();
	}

	/**
	 * @param fid
	 *        the fid to set
	 */
	public void setFid(String fid)
	{
		this.fid = fid;
	}

	/**
	 * @return the diffCols
	 */
	public List<DiffColInfo> getDiffCols()
	{
		return diffCols;
	}

	/**
	 * @param diffCols
	 *        the diffCols to set
	 */
	public void setDiffCols(List<DiffColInfo> diffCols)
	{
		this.diffCols = diffCols;
	}

	
	/**
	 * @return the sameCols
	 */
	public List<SameColInfo> getSameCols()
	{
		return sameCols ;
	}

	/**
	 * @param sameCols the sameCols to set
	 */
	public void setSameCols(List<SameColInfo> sameCols)
	{
		this.sameCols = sameCols;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DiffInfo [fid=");
		builder.append(fid);
		builder.append(", diffCols=");
		builder.append(diffCols);
		builder.append(", sameCols=");
		builder.append(sameCols);
		builder.append("]");
		return builder.toString();
	}

}
