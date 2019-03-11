package com.lizongbo.codegentool.db2java.dbsql2xml;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class DbConfig {
	String dburl = "jdbc:mysql://localhost:3306/mobileqq?useUnicode=true&characterEncoding=UTF-8"; // "jdbc:mysql://10.108.20.100:3306/shareforum";
	String dbuser = "root"; // "";
	String dbpass = ""; // "";
	String dbType = "mysql";

	private String dbDriver = "com.mysql.jdbc.Driver";
	private String schema;
	private final String[] noGeneratedValueTable = new String[] {};
	/**
	 * @see http://docs.oracle.com/javase/6/docs/technotes/guides/jdbc/getstart/
	 *      mapping.html#996857
	 */
	private static Map<Integer, String> javaTypeMap = new HashMap<Integer, String>();
	private static Map<Integer, String> protoTypeMap = new HashMap<Integer, String>();
	static {
		initJavaTypeMap();
		initProtoTypeMap();

	}

	private static void initJavaTypeMap() {
		// typeMap.put(Types.BIT, "Boolean");
		javaTypeMap.put(Types.BIT, "short");// 针对mysql，先弄成整数的
		javaTypeMap.put(Types.TINYINT, "short");
		javaTypeMap.put(Types.SMALLINT, "short");
		javaTypeMap.put(Types.INTEGER, "int");
		javaTypeMap.put(Types.BIGINT, "long");
		javaTypeMap.put(Types.FLOAT, "double");
		javaTypeMap.put(Types.REAL, "float");
		javaTypeMap.put(Types.DOUBLE, "double");
		javaTypeMap.put(Types.NUMERIC, "java.math.BigDecimal");
		javaTypeMap.put(Types.DECIMAL, "java.math.BigDecimal");
		javaTypeMap.put(Types.CHAR, "String");
		javaTypeMap.put(Types.VARCHAR, "String");
		javaTypeMap.put(Types.LONGVARCHAR, "String");
		// typeMap.put(Types.DATE, "java.sql.Date");
		// typeMap.put(Types.TIME, "java.sql.Time");
		javaTypeMap.put(Types.DATE, "String");
		javaTypeMap.put(Types.TIME, "String");
		javaTypeMap.put(Types.TIMESTAMP, "String");
		javaTypeMap.put(Types.BINARY, "byte[]");
		javaTypeMap.put(Types.VARBINARY, "byte[]");
		javaTypeMap.put(Types.LONGVARBINARY, "byte[]");
		javaTypeMap.put(Types.NULL, "Object");
		javaTypeMap.put(Types.OTHER, "Object");
		javaTypeMap.put(Types.JAVA_OBJECT, "Object");
		javaTypeMap.put(Types.DISTINCT, "String");
		javaTypeMap.put(Types.STRUCT, "Object");
		javaTypeMap.put(Types.ARRAY, "byte[]");
		javaTypeMap.put(Types.BLOB, "byte[]");
		javaTypeMap.put(Types.CLOB, "String");
		javaTypeMap.put(Types.REF, "Object");
		javaTypeMap.put(Types.DATALINK, "");
		javaTypeMap.put(Types.BOOLEAN, "Boolean");
		javaTypeMap.put(Types.ROWID, "Object");
		javaTypeMap.put(Types.NCHAR, "String");
		javaTypeMap.put(Types.NVARCHAR, "String");
		javaTypeMap.put(Types.LONGNVARCHAR, "String");
		javaTypeMap.put(Types.NCLOB, "String");
		javaTypeMap.put(Types.SQLXML, "String");

	}

	private static void initProtoTypeMap() {
		// typeMap.put(Types.BIT, "Boolean");
		protoTypeMap.put(Types.BIT, "int32");// 针对mysql，先弄成整数的
		protoTypeMap.put(Types.TINYINT, "int32");
		protoTypeMap.put(Types.SMALLINT, "int32");
		protoTypeMap.put(Types.INTEGER, "int32");
		protoTypeMap.put(Types.BIGINT, "int64");
		protoTypeMap.put(Types.FLOAT, "double");
		protoTypeMap.put(Types.REAL, "float");
		protoTypeMap.put(Types.DOUBLE, "double");
		protoTypeMap.put(Types.NUMERIC, "string");
		protoTypeMap.put(Types.DECIMAL, "string");
		protoTypeMap.put(Types.CHAR, "string");
		protoTypeMap.put(Types.VARCHAR, "string");
		protoTypeMap.put(Types.LONGVARCHAR, "string");
		// typeMap.put(Types.DATE, "java.sql.Date");
		// typeMap.put(Types.TIME, "java.sql.Time");
		protoTypeMap.put(Types.DATE, "string");
		protoTypeMap.put(Types.TIME, "string");
		protoTypeMap.put(Types.TIMESTAMP, "string");
		protoTypeMap.put(Types.BINARY, "bytes");
		protoTypeMap.put(Types.VARBINARY, "bytes");
		protoTypeMap.put(Types.LONGVARBINARY, "bytes");
		protoTypeMap.put(Types.NULL, "string");
		protoTypeMap.put(Types.OTHER, "string");
		protoTypeMap.put(Types.JAVA_OBJECT, "bytes");
		protoTypeMap.put(Types.DISTINCT, "string");
		protoTypeMap.put(Types.STRUCT, "bytes");
		protoTypeMap.put(Types.ARRAY, "bytes");
		protoTypeMap.put(Types.BLOB, "bytes");
		protoTypeMap.put(Types.CLOB, "string");
		protoTypeMap.put(Types.REF, "bytes");
		protoTypeMap.put(Types.DATALINK, "");
		protoTypeMap.put(Types.BOOLEAN, "bool");
		protoTypeMap.put(Types.ROWID, "bytes");
		protoTypeMap.put(Types.NCHAR, "string");
		protoTypeMap.put(Types.NVARCHAR, "string");
		protoTypeMap.put(Types.LONGNVARCHAR, "string");
		protoTypeMap.put(Types.NCLOB, "string");
		protoTypeMap.put(Types.SQLXML, "string");

	}

	public String getDbpass() {
		return dbpass;
	}

	public String getDbType() {
		return dbType;
	}

	public String getDburl() {
		return dburl;
	}

	public String getDbuser() {
		return dbuser;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public String getSchema() {
		return schema;
	}

	public String[] getNoGeneratedValueTable() {
		return noGeneratedValueTable;
	}

	public void setDbuser(String dbuser) {
		this.dbuser = dbuser;
	}

	public void setDburl(String dburl) {
		this.dburl = dburl;
		if(dburl.contains("postgres")){
			this.dbType="postgresql";
			
		}
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setDbpass(String dbpass) {
		this.dbpass = dbpass;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String toJavaType(int sqlType) {
		return javaTypeMap.get(sqlType);
	}

	public String toProtoType(int sqlType) {
		return protoTypeMap.get(sqlType);
	}
}
