package com.lizongbo.codegentool.db2java.dbsql2xml;

import java.lang.reflect.Field;

public class ColumnInfo {
	/** 字段名 */
	private String name;
	/** java属性名 */
	private String propertyName;
	/** 字段长度 */
	private int length;
	/** sql类型 */
	private int sqlType = java.sql.Types.VARCHAR;
	/** 字符串型表示的sql类型 */
	private String sqlTypeStr = "";
	/** 数据字段类型的字符串名称 */
	private String sqlTypeName = "";
	/** java类型 */
	private String javaType;
	/** proto类型 */
	private String protoType;
	/** 是否是自增字段，只对单一自增主键有效 */
	private boolean autoIncrement = false;
	private boolean nullable = true;
	/** 默认值 */
	private String defaultValue = "";
	/** 默认值 */
	private String protoDefaultValue = "";
	private String comments = "";
	/** 拆分注释comments，竖线前面的当作标题 */
	private String title = "";
	/** 拆分注释comments，竖线后面的当作备注说明 */
	private String subcmt = "";

	private boolean pkColumn = false;

	public ColumnInfo() {
	}

	public String getSqlTypeStr() {
		return sqlTypeStr;
	}

	public void setSqlTypeStr(String sqlTypeStr) {
		this.sqlTypeStr = sqlTypeStr;
	}

	public String getSqlTypeName() {
		return sqlTypeName = (sqlTypeName == null) ? "" : sqlTypeName.trim();
	}

	public void setSqlTypeName(String sqlTypeName) {
		this.sqlTypeName = sqlTypeName;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public String getTitle() {
		if ((title == null) || (title.length() < 1)) {
			if ((comments == null) || (comments.length() < 1)) {
				title = name;
			}
		}
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubcmt() {
		return subcmt;
	}

	public void setSubcmt(String subcmt) {
		this.subcmt = subcmt;
	}

	public void setPropertyName(String propertyName) {
		if (propertyName.equalsIgnoreCase("class")) {
			this.propertyName = "calssType";
			return;
		}
		if (propertyName.equalsIgnoreCase("new")) {
			this.propertyName = "newType";
			return;
		}

		this.propertyName = propertyName;
	}

	public void setName(String name) {

		this.name = name;
	}

	public void setLength(int length) {

		this.length = length;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
		sqlTypeStr = this.getSQLTYPEstr(sqlType);
	}

	public String getProtoType() {
		return protoType;
	}

	public void setProtoType(String protoType) {
		this.protoType = protoType;
	}

	public void setDefaultValue(String defaultValue) {
		if ("Boolean".equalsIgnoreCase(this.getJavaType())) {
			this.defaultValue = "0".equals(defaultValue) ? "false" : "true";
			return;
		}
		this.defaultValue = defaultValue;
		if ("String".equals(javaType) && (defaultValue.length() > 0) && (defaultValue.indexOf('"') < 0)) {
			this.defaultValue = "\"" + this.defaultValue + "\"";
		}
		if ("float".equals(javaType) && (defaultValue.length() > 0)) {
			this.defaultValue = this.defaultValue + "F";
		}
		if (javaType.endsWith("BigDecimal") && (defaultValue != null) && (defaultValue.length() > 0)) {
			this.defaultValue = "java.math.BigDecimal.valueOf(" + this.defaultValue + ")";

		}
		if (javaType.endsWith("java.sql.Date") && (defaultValue != null) && (defaultValue.length() > 0)) {
			this.defaultValue = "java.sql.Date.valueOf(\"" + this.defaultValue + "\")";
		}
		if (("\"CURRENT_TIMESTAMP\"".equalsIgnoreCase(this.defaultValue))) {// 如果是这个，则搞成默认值的
			this.defaultValue = "new java.sql.Timestamp(System.currentTimeMillis()).toString().substring(0,19)";

		}
	}

	public String getProtoDefaultValue() {
		return protoDefaultValue;
	}

	public void setProtoDefaultValue(String protoDefaultValue) {
		if ("Boolean".equalsIgnoreCase(this.getJavaType())) {
			this.protoDefaultValue = "0".equals(protoDefaultValue) ? "false" : "true";
			return;
		}
		this.protoDefaultValue = protoDefaultValue;
		if ("String".equals(javaType) && (protoDefaultValue.length() > 0) && (protoDefaultValue.indexOf('"') < 0)) {
			this.protoDefaultValue = "\"" + this.protoDefaultValue + "\"";
		}
		if ("float".equals(javaType) && (protoDefaultValue.length() > 0)) {
			this.protoDefaultValue = this.protoDefaultValue + "F";
		}
		if (javaType.endsWith("BigDecimal") && (protoDefaultValue != null) && (protoDefaultValue.length() > 0)) {
			this.protoDefaultValue = "java.math.BigDecimal.valueOf(" + this.protoDefaultValue + ")";

		}
		if (javaType.endsWith("java.sql.Date") && (protoDefaultValue != null) && (protoDefaultValue.length() > 0)) {
			this.protoDefaultValue = "java.sql.Date.valueOf(\"" + this.protoDefaultValue + "\")";
		}
		if (("\"CURRENT_TIMESTAMP\"".equalsIgnoreCase(this.protoDefaultValue))) {// 如果是这个，则搞成默认值的
			this.protoDefaultValue = "";

		}
	}

	public void setComments(String comments) {
		if (comments == null) {
			comments = "";
		}
		this.comments = comments.trim();
		if (comments.contains("[]")) {
			//System.err.println("setComments带了特殊prop===" + comments);
			//Exception ex = new Exception("测试");
			//ex.printStackTrace();
		}
		if ((comments != null) && comments.contains("|")) {
			title = comments.substring(0, comments.indexOf("|"));
			subcmt = comments.substring(comments.indexOf("|") + 1);
			if (subcmt.length() < 1) {
				subcmt = title;
			}
		} else {
			title = this.comments;
			subcmt = this.comments;
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}

	public boolean isNullable() {
		return nullable;
	}

	public String getJavaType() {
		return javaType;
	}

	public int getSqlType() {
		return sqlType;
	}

	public String getDefaultValue() {

		return defaultValue;
	}

	public String getComments() {
		return comments;
	}

	public boolean isPkColumn() {
		return pkColumn;
	}

	public void setPkColumn(boolean pkColumn) {
		this.pkColumn = pkColumn;
	}

	private String getSQLTYPEstr(int sqlType) {
		switch (sqlType) {
		case java.sql.Types.BIT:
			return "BIT";
		case java.sql.Types.TINYINT:
			return "TINYINT";
		case java.sql.Types.SMALLINT:
			return "SMALLINT";
		case java.sql.Types.INTEGER:
			return "INTEGER";
		case java.sql.Types.BIGINT:
			return "BIGINT";
		case java.sql.Types.FLOAT:
			return "FLOAT";
		case java.sql.Types.REAL:
			return "REAL";
		case java.sql.Types.DOUBLE:
			return "DOUBLE";
		case java.sql.Types.NUMERIC:
			return "NUMERIC";
		case java.sql.Types.DECIMAL:
			return "DECIMAL";
		case java.sql.Types.CHAR:
			return "CHAR";
		case java.sql.Types.VARCHAR:
			return "VARCHAR";
		case java.sql.Types.LONGVARCHAR:
			return "LONGVARCHAR";
		case java.sql.Types.DATE:
			return "DATE";
		case java.sql.Types.TIME:
			return "TIME";
		case java.sql.Types.TIMESTAMP:
			return "TIMESTAMP";
		case java.sql.Types.BINARY:
			return "BINARY";
		case java.sql.Types.VARBINARY:
			return "VARBINARY";
		case java.sql.Types.LONGVARBINARY:
			return "LONGVARBINARY";
		case java.sql.Types.NULL:
			return "NULL";
		case java.sql.Types.OTHER:
			return "OTHER";
		case java.sql.Types.JAVA_OBJECT:
			return "JAVA_OBJECT";
		case java.sql.Types.DISTINCT:
			return "DISTINCT";
		case java.sql.Types.STRUCT:
			return "STRUCT";
		case java.sql.Types.ARRAY:
			return "ARRAY";
		case java.sql.Types.BLOB:
			return "BLOB";
		case java.sql.Types.CLOB:
			return "CLOB";
		case java.sql.Types.REF:
			return "REF";
		case java.sql.Types.DATALINK:
			return "DATALINK";
		case java.sql.Types.BOOLEAN:
			return "BOOLEAN";
		case java.sql.Types.ROWID:
			return "ROWID";
		case java.sql.Types.NCHAR:
			return "NCHAR";
		case java.sql.Types.NVARCHAR:
			return "NVARCHAR";
		case java.sql.Types.LONGNVARCHAR:
			return "LONGNVARCHAR";
		case java.sql.Types.NCLOB:
			return "NCLOB";
		case java.sql.Types.SQLXML:
			return "SQLXML";
		default:
			return "NONE";
		}
	}

	public String toColumnXMLString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<column>\n");
		sb.append("<name><![CDATA[").append(name).append("]]></name>\n");
		sb.append("<propertyName><![CDATA[").append(propertyName).append("]]></propertyName>\n");
		sb.append("<length><![CDATA[").append(length).append("]]></length>\n");
		sb.append("<sqlTypeStr><![CDATA[").append(sqlTypeStr).append("]]></sqlTypeStr>\n");
		sb.append("<sqlTypeName><![CDATA[").append(sqlTypeName).append("]]></sqlTypeName>\n");
		sb.append("<javaType><![CDATA[").append(javaType).append("]]></javaType>\n");
		sb.append("<protoType><![CDATA[").append(protoType).append("]]></protoType>\n");
		sb.append("<defaultValue><![CDATA[").append(defaultValue).append("]]></defaultValue>\n");
		sb.append("<protoDefaultValue><![CDATA[").append(protoDefaultValue).append("]]></protoDefaultValue>\n");
		sb.append("<nullable><![CDATA[").append(nullable).append("]]></nullable>\n");
		sb.append("<autoIncrement><![CDATA[").append(autoIncrement).append("]]></autoIncrement>\n");
		sb.append("<comments><![CDATA[").append(comments).append("]]></comments>\n");
		if (comments.contains("[]")) {
			//System.err.println("带了特殊prop===" + comments);
		}
		sb.append("<title><![CDATA[").append(this.getTitle()).append("]]></title>\n");
		sb.append("<subcmt><![CDATA[").append(this.getSubcmt()).append("]]></subcmt>\n");
		sb.append("<pkColumn><![CDATA[").append(pkColumn).append("]]></pkColumn>\n");
		sb.append("</column>\n");
		return sb.toString();
	}

	public static void main(String[] args) {
		Class sqlclass = java.sql.Types.class;
		Field[] fd = sqlclass.getDeclaredFields();
		for (Field f : fd) {
			System.out.println("case java.sql.Types." + f.getName() + ": return \"" + f.getName() + "\"; ");
		}
		ColumnInfo c = new ColumnInfo();
		c.setPropertyName("age");
		c.setName("AGE");
		c.setLength(20);
		System.out.println(c);
	}
}
