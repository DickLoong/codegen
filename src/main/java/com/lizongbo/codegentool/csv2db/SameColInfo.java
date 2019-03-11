package com.lizongbo.codegentool.csv2db;

public class SameColInfo
{
	private String colName = "";// 主键id
	private String csvValue = "";// csv文件里的值

	/**
	 * @return the colName
	 */
	public String getColName()
	{
		return colName = (colName == null) ? "" : colName.trim();
	}

	/**
	 * @param colName
	 *        the colName to set
	 */
	public void setColName(String colName)
	{
		this.colName = colName;
	}

	/**
	 * @return the csvValue
	 */
	public String getCsvValue()
	{
		return csvValue = (csvValue == null) ? "" : csvValue.trim();
	}

	/**
	 * @param csvValue
	 *        the csvValue to set
	 */
	public void setCsvValue(String csvValue)
	{
		this.csvValue = csvValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SameColInfo [colName=");
		builder.append(colName);
		builder.append(", csvValue=");
		builder.append(csvValue);
		builder.append("]");
		return builder.toString();
	}


}
