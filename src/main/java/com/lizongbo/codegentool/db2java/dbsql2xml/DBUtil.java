package com.lizongbo.codegentool.db2java.dbsql2xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtil {
	static Connection conn = null;

	/**
	 * 根据数据库配置信息遍历获取表名
	 * 
	 * @param dbConfig
	 * @return
	 */
	public static String[] listTableNames(DbConfig dbConfig) {
		java.util.List<String> tbNames = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = getConn(dbConfig);
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet tbrs = dbmd.getTables(null, dbConfig.getSchema(), null, new String[] { "TABLE" });
			while (tbrs.next()) {
				String table_name = tbrs.getString("TABLE_NAME");
				///// System.out.println("table_name==" + table_name);
				if (isTableName(table_name)) {
					tbNames.add(table_name);
				}
			}
			tbrs.close();
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return tbNames.toArray(new String[0]);
	}

	/**
	 * 判断数据库表名是否合法，只能英文数字加下划线
	 * 
	 * @param tableName
	 * @return
	 */
	private static boolean isTableName(String tableName) {
		if (tableName == null || tableName.trim().length() < 1) {
			return false;
		}
		char[] cs = tableName.toCharArray();
		for (char c : cs) {
			if (!isTableNameChar(c)) {
				System.err.println("isTableName|for|" + tableName + "|FALSE");
				return false;
			}
		}
		return true;
	}

	private static boolean isTableNameChar(char c) {
		if (c >= '0' && c <= '9') {
			return true;
		}
		if (c >= 'A' && c <= 'Z') {
			return true;
		}
		if (c >= 'a' && c <= 'z') {
			return true;
		}
		if (c == '_') {
			return true;
		}
		System.err.println("ccccccccccccc " + c);
		return false;
	}

	/**
	 * 获取数据库连接
	 * 
	 * @param dbConfig
	 * @return
	 */
	public static Connection getConn(DbConfig dbConfig) {

		try {
			Class.forName(dbConfig.getDbDriver());
			if (conn == null) {
				conn = java.sql.DriverManager.getConnection(dbConfig.getDburl(), dbConfig.getDbuser(),
						dbConfig.getDbpass());
				conn = new CachedConnection(conn);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return conn;
	}

	public static Map<String, List<String>> getUniquekeys(String table_name, DbConfig dbConfig) {
		Map<String, List<String>> uniquekeyMap = new HashMap<String, List<String>>();
		Connection conn = null;
		try {
			conn = getConn(dbConfig);
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet pkrs = dbmd.getIndexInfo(null, dbConfig.getSchema(), table_name, true, true);
			while (pkrs.next()) {
				String ukName = pkrs.getString("INDEX_NAME");
				String ukc_name = pkrs.getString("COLUMN_NAME");
				List<String> clist = uniquekeyMap.get(ukName);
				if (clist == null) {
					clist = new ArrayList<String>();
					uniquekeyMap.put(ukName, clist);
				}
				clist.add(ukc_name);
			}
			pkrs.close();
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return uniquekeyMap;

	}

	public static List<String> getPkColumns(String table_name, DbConfig dbConfig) {
		java.util.List<String> pks = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = getConn(dbConfig);
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet pkrs = dbmd.getPrimaryKeys(null, dbConfig.getSchema(), table_name);
			while (pkrs.next()) {
				String pk_name = pkrs.getString("COLUMN_NAME");
				pks.add(pk_name);
			}
			pkrs.close();
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pks;

	}

	public static List<String> listColumns(String table_name, DbConfig dbConfig) {
		java.util.List<String> cls = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = getConn(dbConfig);
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet clrs = dbmd.getColumns(null, dbConfig.getSchema(), table_name, null);
			while (clrs.next()) {
				String cl_name = clrs.getString("COLUMN_NAME");
				// System.out.println(table_name + "." + cl_name);
				cls.add(cl_name);
			}
			clrs.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return cls;
	}

	public static List<String> getNotPkColumns(String table_name, DbConfig dbConfig) {
		java.util.List<String> cls = listColumns(table_name, dbConfig);
		java.util.List<String> pks = getPkColumns(table_name, dbConfig);
		for (String pk : pks) {
			if (cls.contains(pk)) {
				cls.remove(pk);
			}
		}
		return cls;

	}

	public static String getMysqlTableComments(String table_name, DbConfig dbConfig) {
		String dv = "";
		String sql = "SELECT \n" + "  TABLE_COMMENT \n" + " FROM \n" + "    information_schema.TABLES \n" + " WHERE \n "
				+ "  TABLE_COMMENT is not null and  \n " + "    TABLE_NAME     = '" + table_name + "' \n";
		if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
			sql = "SELECT c.relname                                     AS \"TABLE_NAME\", "
					+ "       pg_catalog.obj_description(c.oid, \'pg_class\') AS \"TABLE_COMMENT\" "
					+ " FROM   pg_class c " + "   LEFT JOIN pg_namespace n   ON n.oid = c.relnamespace "
					+ "   LEFT JOIN pg_tablespace t  ON t.oid = c.reltablespace "
					+ " WHERE c.relkind = \'r\'::\"char\" " + " AND   n.nspname LIKE \'public\' "
					+ " AND   c.relname LIKE \'" + table_name + "\' " + " ORDER BY n.nspname, c.relname";
		}
		Connection conn = null;
		conn = getConn(dbConfig);
		Statement stmt = null;
		ResultSet dvrs = null;
		try {
			stmt = conn.createStatement();
			//// System.out.println("sql == " + sql);
			dvrs = stmt.executeQuery(sql);
			while (dvrs.next()) {
				String cd = dvrs.getString("TABLE_COMMENT");
				// System.out.println("cd==" + cd);
				if (cd == null) {
					cd = ""; //
				}
				dv = dv + cd;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				dvrs.close();
				stmt.close();
				conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return dv;

	}

	/**
	 * 驼峰命名转换 a_bb_cc转换成aBbCc
	 * 
	 * @param name
	 * @return
	 */
	public static String camelName(String name) {
		if (name == null) {
			return name;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			// System.out.println("cc=" + c + "|i==" + i);
			if ((c == '_') && i < name.length() - 1) {// 兼容protobuf的名字转换规则
				if (name.charAt(i + 1) <= '9' && name.charAt(i + 1) >= '0') {
					i++;
					sb.append(Character.toUpperCase(name.charAt(i)));
					if (i < name.length() - 1) {
						i++;
						sb.append(Character.toUpperCase(name.charAt(i)));
					}
				} else {
					i++;
					sb.append(Character.toUpperCase(name.charAt(i)));
				}
			} else if ((c <= '9' && c >= '0') && (i < name.length() - 1) && name.charAt(i + 1) != '_') {
				i++;
				sb.append(c);
				// System.err.println("aaaaaacc=" + c);
				sb.append(Character.toUpperCase(name.charAt(i)));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static ColumnInfo getMysqlColumnInfo(String dbName, String table_name, String column_name,
			DbConfig dbConfig) {
		ColumnInfo c = null;
		Connection conn = null;
		try {
			conn = getConn(dbConfig);
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet crs = dbmd.getColumns(null, null, table_name, column_name);
			while (crs.next()) {
				ResultSetMetaData cnrsmd = crs.getMetaData();
				int cncolumnCount = cnrsmd.getColumnCount();
				// for (int ii = 1; ii <= cncolumnCount; ii++)
				// {
				// System.out.println("ttttttttttt|" + cnrsmd.getColumnName(ii)
				// + " == " +
				// crs.getString(ii) + " " +
				// cnrsmd.getColumnClassName(ii));
				// }

				c = new ColumnInfo();
				int sqlType = crs.getInt("DATA_TYPE");
				c.setJavaType(dbConfig.toJavaType(sqlType));
				c.setProtoType(dbConfig.toProtoType(sqlType));

				// String defaultValue=crs.getInt("COLUMN_SIZE");
				c.setLength(crs.getInt("COLUMN_SIZE"));
				c.setName(crs.getString("COLUMN_NAME"));
				c.setNullable("YES".equalsIgnoreCase(crs.getString("IS_NULLABLE")));
				c.setPropertyName(camelName(c.getName().toLowerCase()));
				c.setSqlType(sqlType);
				c.setSqlTypeName(crs.getString("TYPE_NAME"));

				if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
					String dv = crs.getString("COLUMN_DEF");
					System.out.println(c.getName() + " ====POSTGRESQL COLUMN_DEF==" + dv);
					if (dv == null) {
						dv = "";
					} else if (dv.startsWith("\'")) {
						dv = dv.substring(dv.indexOf("\'") + 1, dv.lastIndexOf("\'"));
					}
					System.out.println(c.getName() + " ====POSTGRESQL COLUMN_DEF== ============" + dv);
					c.setDefaultValue(dv);
				} else {
					c.setDefaultValue(getMysqlColumnDefaultValue(dbName, table_name, column_name, dbConfig));
				}
				if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
					c.setComments(crs.getString("REMARKS"));
					// System.out.println("c.getComments() ==
					// "+c.getComments());
				} else {
					c.setComments(getMysqlColumnComments(dbName, table_name, column_name, dbConfig));
					if (c.getComments().endsWith("[]|")) {
						String tmp = c.getComments();
						tmp = tmp.substring(0, tmp.length() - 1);
						String protoTypeTmp = tmp.substring(tmp.lastIndexOf("|") + 1).replace('[', ' ')
								.replace(']', ' ').trim();
						//System.err.println("特殊proto类型" + protoTypeTmp + " 来自  " + c.getComments());
						c.setProtoType(protoTypeTmp);
					}
				}
				boolean autoIncrement = false;
				if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
					autoIncrement = "YES".equalsIgnoreCase(crs.getString("IS_AUTOINCREMENT"));
				} else {
					autoIncrement = isMysqlAutoIncrement(dbName, table_name, column_name, dbConfig);
				}
				c.setAutoIncrement(autoIncrement);
				// System.out.println(c);
				/**
				 * 
				 * 
				 * 
				 * 
				 * TABLE_CAT == null TABLE_SCHEM == null TABLE_NAME == words
				 * COLUMN_NAME == id DATA_TYPE == 5 TYPE_NAME == smallint
				 * unsigned COLUMN_SIZE == 5 BUFFER_LENGTH == 65535
				 * DECIMAL_DIGITS == 0 NUM_PREC_RADIX == 10 NULLABLE == 0
				 * REMARKS == COLUMN_DEF == null SQL_DATA_TYPE == 0
				 * SQL_DATETIME_SUB == 0 CHAR_OCTET_LENGTH == null
				 * ORDINAL_POSITION == 1 IS_NULLABLE == NO
				 * 
				 */
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return c;
	}

	public static boolean isMysqlAutoIncrement(String dbName, String table_name, String column_name,
			DbConfig dbConfig) {
		String sql = "SELECT \n" + " EXTRA \n" + " FROM \n" + "    information_schema.COLUMNS \n" + " WHERE \n"
				+ "    TABLE_SCHEMA  = '" + dbName + "'  \n" + "    and \n" + "    TABLE_NAME     = '" + table_name
				+ "' \n" + "    and COLUMN_NAME= '" + column_name + "' \n";
		Connection conn = getConn(dbConfig);
		Statement stmt = null;
		ResultSet dvrs = null;
		try {

			stmt = conn.createStatement();
			dvrs = stmt.executeQuery(sql);
			while (dvrs.next()) {
				String cd = dvrs.getString("EXTRA");
				if ("auto_increment".equalsIgnoreCase(cd)) {
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				dvrs.close();
				stmt.close();
				conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	public static String getMysqlColumnComments(String dbName, String table_name, String column_name,
			DbConfig dbConfig) {
		String dv = "";
		String sql = "SELECT \n" + " COLUMN_COMMENT \n" + " FROM \n" + "    information_schema.COLUMNS \n" + " WHERE \n"
				+ "    TABLE_SCHEMA  = '" + dbName + "'  \n" + "    and \n" + "    TABLE_NAME     = '" + table_name
				+ "' \n" + "    and COLUMN_NAME= '" + column_name + "' \n";
		Connection conn = getConn(dbConfig);
		Statement stmt = null;
		ResultSet dvrs = null;
		try {

			stmt = conn.createStatement();
			dvrs = stmt.executeQuery(sql);
			while (dvrs.next()) {
				String cd = dvrs.getString("COLUMN_COMMENT");
				if (cd == null) {
					cd = ""; //
					// System.out.println(table_name + "." + column_name);
				}
				dv = dv + cd;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				dvrs.close();
				stmt.close();
				conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// System.out.println(table_name + "." + column_name + "==" + dv);
		return dv;
	}

	public static String getMysqlColumnDefaultValue(String dbName, String table_name, String column_name,
			DbConfig dbConfig) {
		String dv = "";
		String sql = "SELECT \n" + "   COLUMN_DEFAULT \n" + " FROM \n" + "    information_schema.COLUMNS \n"
				+ " WHERE \n" + "    TABLE_SCHEMA  = '" + dbName + "'  \n" + "    and \n" + "    TABLE_NAME     = '"
				+ table_name + "' \n" + "    and COLUMN_NAME= '" + column_name + "' \n";
		Connection conn = getConn(dbConfig);
		Statement stmt = null;
		ResultSet dvrs = null;
		try {
			stmt = conn.createStatement();
			dvrs = stmt.executeQuery(sql);
			while (dvrs.next()) {
				String cd = dvrs.getString("COLUMN_DEFAULT");
				if (cd == null) {
					cd = ""; //
				}
				dv = dv + cd;
				if ("addDate".equalsIgnoreCase(column_name)) {
					System.err.println("sql==" + sql);
					System.out.println("addDate ==========" + dv);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				dvrs.close();
				stmt.close();
				conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return dv;
	}

	public static boolean writeFile(String filePath, String content) {
		System.out.println("writeFile to:" + filePath);
		new File(filePath).getParentFile().mkdirs();
		try {
			java.io.OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(filePath, false), "UTF-8");
			dos.write(content);
			dos.flush();
			dos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean appendFile(String filePath, String content) {
		// System.out.println("appendFile to:" + filePath);
		new File(filePath).getParentFile().mkdirs();
		try {
			java.io.OutputStreamWriter dos = new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8");
			dos.write(content);
			dos.flush();
			dos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static String getPojoClassName(String table_name) {
		if (table_name == null) {
			return "";
		}
		String className = table_name.toLowerCase();
		className = camelName(className);
		if (className.length() > 1) {
			className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
		}
		return className;
	}

	public static String getAbstractPojoClassName(String table_name) {
		return "Abstract" + getPojoClassName(table_name);
	};

	public static void main(String[] args) {
		String s = "db2_keys";
		System.out.println(camelName(s));
		s = "db21_keys";
		System.out.println(camelName(s));
		s = "nicknames_4check";
		System.out.println(camelName(s));
		s = "activationcondition3_1";
		System.out.println(camelName(s));

	}
}
