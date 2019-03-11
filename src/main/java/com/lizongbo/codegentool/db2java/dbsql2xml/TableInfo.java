package com.lizongbo.codegentool.db2java.dbsql2xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableInfo
{
	private ColumnInfo[] columns;
	private String[] pkColumns;
	private String[] noPkColumns;
	private String tableComment;
	private String tableName;
	private String pojoClassName;
	private String abstractPojoClassName;
	/** 拆分注释comments，竖线前面的当作标题 */
	private String title = "";
	/** 拆分注释comments，竖线后面的当作备注说明 */
	private String subcmt = "";
	/** 分表依据的字段名 */
	private String partFiled = "";
	/** 分表的个数 */
	private int partTableCount = 0;
	/** 用于自定义排序的字段名 */
	private String sortFiled = "";
	/**
	 * unique key 相关列名
	 */
	private Map<String, List<String>> uniqueKeyColumns = new HashMap<String, List<String>>();

	public String getTitle()
	{
		if ((title == null) || (title.length() < 1))
		{
			if ((tableComment == null) || (tableComment.length() < 1))
			{
				title = tableName;
			}
		}

		return title = (title == null) ? "" : title.trim();
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getSubcmt()
	{
		return subcmt = (subcmt == null) ? "" : subcmt.trim();
	}

	public void setSubcmt(String subcmt)
	{
		this.subcmt = subcmt;
	}

	public String getPartFiled()
	{
		return partFiled = (partFiled == null) ? "" : partFiled.trim();
	}

	public void setPartFiled(String partFiled)
	{
		this.partFiled = partFiled;
	}

	public int getPartTableCount()
	{
		return partTableCount;
	}

	public void setPartTableCount(int partTableCount)
	{
		this.partTableCount = partTableCount;
	}

	public String getSortFiled()
	{
		return sortFiled = (sortFiled == null) ? "" : sortFiled.trim();
	}

	public void setSortFiled(String sortFiled)
	{
		this.sortFiled = sortFiled;
	}

	public ColumnInfo[] getColumns()
	{
		return columns;
	}

	public String[] getPkColumns()
	{
		return pkColumns;
	}

	public String[] getNoPkColumns()
	{
		return noPkColumns;
	}

	public String getTableComment()
	{
		return tableComment;
	}

	public String getTableName()
	{
		return tableName;
	}

	public String getPojoClassName()
	{
		return pojoClassName;
	}

	public String getAbstractPojoClassName()
	{
		return abstractPojoClassName;
	}

	public void setColumns(ColumnInfo[] columns)
	{

		this.columns = columns;
	}

	public void setPkColumns(String[] pkColumns)
	{
		this.pkColumns = pkColumns;
	}

	public void setNoPkColumns(String[] noPkColumns)
	{
		this.noPkColumns = noPkColumns;
	}

	public void setTableComment(String tableComment)
	{
		this.tableComment = tableComment;
		if ((tableComment != null) && tableComment.contains("|"))
		{
			title = tableComment.substring(0, tableComment.indexOf("|"));
			subcmt = tableComment.substring(tableComment.indexOf("|") + 1);
			if (subcmt.length() < 1)
			{
				subcmt = title;
			}
		}
		else
		{
			title = this.tableComment;
			subcmt = this.tableComment;
		}
		// 在这里看这个表是不是需要建立分表,匹配信息表|[10,softid]
		// System.out.println(tableComment);
		// System.out.println(this.title + " and " + this.subcmt);
		if ((tableComment != null)
				&& (tableComment.indexOf("]") > tableComment.indexOf("[")))
		{
			// 存在中括号
			String partionStr = tableComment.substring(
					tableComment.indexOf("[") + 1, tableComment.indexOf("]"));
			if (partionStr.indexOf(",") > 0)
			{
				partFiled = partionStr
						.substring(partionStr.indexOf(",") + 1);
				partTableCount = Integer.parseInt(partionStr.substring(0,
						partionStr.indexOf(",")));
			}
		}

	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public void setPojoClassName(String pojoClassName)
	{
		this.pojoClassName = pojoClassName;
	}

	public void setAbstractPojoClassName(String abstractPojoClassName)
	{
		this.abstractPojoClassName = abstractPojoClassName;
	}

	public Map<String, List<String>> getUniqueKeyColumns()
	{
		return uniqueKeyColumns;
	}

	public void setUniqueKeyColumns(Map<String, List<String>> uniqueKeyColumns)
	{
		this.uniqueKeyColumns = uniqueKeyColumns;
	}

	private ColumnInfo getColumnbyName(String column_name)
	{
		// System.out.println("getColumnbyName column_name =" + column_name);
		for (ColumnInfo c : this.getColumns())
		{
			if (c.getName().equalsIgnoreCase(column_name))
			{
				return c;
			}
		}
		return null;
	}

	public String toTableXMLString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<table>\n");
		sb.append("<tableName><![CDATA[").append(tableName)
				.append("]]></tableName>\n");
		sb.append("<tableComment><![CDATA[").append(tableComment)
				.append("]]></tableComment>\n");
		sb.append("<pojoClassName><![CDATA[").append(pojoClassName)
				.append("]]></pojoClassName>\n");
		sb.append("<title><![CDATA[").append(this.getTitle()).append("]]></title>\n");
		sb.append("<subcmt><![CDATA[").append(this.getSubcmt()).append("]]></subcmt>\n");
		sb.append("<partFiled><![CDATA[").append(partFiled)
				.append("]]></partFiled>\n");
		sb.append("<partTableCount><![CDATA[").append(partTableCount)
				.append("]]></partTableCount>\n");
		sb.append("<pkColumns>\n");
		for (String pkColumn : pkColumns)
		{
			sb.append("<pkColumn>");
			for (ColumnInfo column : columns)
			{
				if (pkColumn.equals(column.getName()))
				{
					column.setPkColumn(true);
					sb.append(column.toColumnXMLString());
				}
			}
			sb.append("</pkColumn>\n");
		}
		sb.append("</pkColumns>\n");
		sb.append("<noPkColumns>\n");
		for (String noPkColumn : noPkColumns)
		{
			sb.append("<noPkColumn>");
			for (ColumnInfo column : columns)
			{
				if (noPkColumn.equals(column.getName()))
				{
					sb.append(column.toColumnXMLString());
				}
			}
			sb.append("</noPkColumn>\n");
		}
		sb.append("</noPkColumns>\n");

		sb.append("<uniqueColumns>\n");
		for (Map.Entry<String, List<String>> ukcl : this.getUniqueKeyColumns().entrySet())
		{
			if (!"PRIMARY".equalsIgnoreCase(ukcl.getKey()))
			{// 排除主键
				sb.append("<uniqueColumn name=\"" + ukcl.getKey() + "\">");
				for (ColumnInfo column : columns)
				{
					if (ukcl.getValue().contains((column.getName())))
					{
						sb.append(column.toColumnXMLString());
					}
				}
				sb.append("</uniqueColumn>\n");
			}
		}
		sb.append("</uniqueColumns>\n");

		sb.append("<columns>\n");
		for (ColumnInfo column : columns)
		{
			sb.append(column.toColumnXMLString());
		}
		sb.append("</columns>\n");
		sb.append("<partColumn>\n");
		for (ColumnInfo column : columns)
		{
			if (column.getName().equals(partFiled))
			{
				sb.append(column.toColumnXMLString());
			}
		}
		sb.append("</partColumn>\n");
		for (String pkColumn : pkColumns)
		{
			sortFiled = pkColumn;
		}
		for (ColumnInfo column : columns)
		{
			if (column.getName().equalsIgnoreCase("sort"))
			{
				sortFiled = column.getName();
			}
		}
		sb.append("<sortColumn>\n");
		for (ColumnInfo column : columns)
		{
			if (column.getName().equals(sortFiled))
			{
				sb.append(column.toColumnXMLString());
			}
		}
		sb.append("</sortColumn>\n");
		sb.append("</table>\n");
		return sb.toString();

	}
}
