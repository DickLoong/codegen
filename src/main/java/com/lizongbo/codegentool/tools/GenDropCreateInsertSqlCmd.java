package com.lizongbo.codegentool.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.lizongbo.codegentool.configverification.CsvCheckerManager;
import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.Pair;

public class GenDropCreateInsertSqlCmd {
	private static Map<String, StringBuilder> sbDropTableMap = new HashMap<String, StringBuilder>();
	private static Map<String, StringBuilder> sbCreateTableMap = new HashMap<String, StringBuilder>();
	private static Map<String, StringBuilder> sbInsertTableMap = new HashMap<String, StringBuilder>();

	private static final int CSV_DATA_COL_START_INDEX = 4;

	public static void main(String[] args) {
		String remoteIp = System.getenv("remoteIp");
		String csvFilepath = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\csvfiles";
		String outputPath = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\sqlfiles\\dropCreateInsertsqls/dropCreateInsertdb4dbconfig.sql";
		//TODO 读取一个配置文件
		 Properties prop = new Properties();   
	     InputStream in = Object.class.getResourceAsStream("/DeployConfig." + remoteIp + ".properties");  
	     System.out.println("try load properties.");
	     try {   
	            prop.load(in);   
	            if(prop.containsKey("csvFilepath")){
	            	csvFilepath = prop.getProperty("csvFilepath").trim();
	            }
	            if(prop.containsKey("outputPath")){
	            	outputPath = prop.getProperty("outputPath").trim();
	            }
	            System.out.println("use customed path.|" + csvFilepath);
	      } catch (Throwable e) {   
	    	  System.out.println("use default path.|" + csvFilepath);
	      }   
	     System.out.println("try load properties finished.");
		File file = new File(csvFilepath);
		System.out.println(csvFilepath);
		genSql(file);

		try {
			java.io.File f = new File("E:/cehua/dropCreateInsertdb4dbconfig.sql");
			if (remoteIp != null && remoteIp.length() > 0) {
				f = new File(outputPath);
			}
			System.out.println("GenDropCreateInsertSqlCmd:" + f);
			f.getParentFile().mkdirs();
			java.io.PrintWriter pw = new java.io.PrintWriter(f, "UTF-8");
			System.out.println("Start create DB file.");
			for (StringBuilder sql : sbDropTableMap.values()) {
				pw.print(sql + "\n");
			}
			for (StringBuilder sql : sbCreateTableMap.values()) {
				pw.print(sql + "\n");
			}
			for (StringBuilder sql : sbInsertTableMap.values()) {
				pw.print(sql + "\n");
			}
			System.out.println("Finish create DB file.");
			pw.flush();
			pw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("转换结束");
	}

	public static void genSql(File csvFile) {
		System.out.println(csvFile.getAbsolutePath());
		if (csvFile.isDirectory()) {
			File fs[] = csvFile.listFiles();
			for (File subf : fs) {
				genSql(subf);
			}
		}
		if (csvFile.isFile() && csvFile.getName().startsWith("T") && csvFile.getName().endsWith(".csv")) {
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			if (tableName == null || tableName.length() < 1 || tableName.contains("user")) {
				return;
			}
			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

			CSVUtil.genCsvCheckerClass(csvFile.getAbsolutePath());
			CsvCheckerManager.CheckCsv(csvFile.getAbsolutePath());
			if (tableName.length() > 0 && colList != null && colList.size() > 3) {
				// 检查表得字段名是不是有重复，如果有，程序直接退出并警告是excel配置错了。
				String[] colNames = colList.get(0);
				if (GameCSV2DB.hasRepeatCol(colNames, csvFile)) {
					System.err.println("ERROR:hasRepeatCol" + Arrays.toString(colNames) + "|for|" + csvFile);
					System.exit(-2);
				}
				if (!sbDropTableMap.containsKey(tablePrefix)) {
					sbDropTableMap.put(tablePrefix, new StringBuilder());
					sbCreateTableMap.put(tablePrefix, new StringBuilder());
					sbInsertTableMap.put(tablePrefix, new StringBuilder());
				}
				String dropTableSql = GameCSV2DB.genDropTableSql(tableName);
				sbDropTableMap.get(tablePrefix).append(dropTableSql).append("\n");
				String createTableSql = GameCSV2DB.genCreateTableSql(tableName, tableCmt, colList);
				sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
				// System.out.println("createTableSql == " + createTableSql);
				List<Pair<String, String>> rsqlList = genReplaceInsertSql(tablePrefix, tableName, colList);
				List<Pair<String, String>> usqlList = GameCSV2DB.genUpdateSql(tableName, colList);
			} else {
				System.out.println(csvFile + "|tableName=" + tableName + " is not table csv ,try for protobuf csv");
			}
		}
	}

	public static List<Pair<String, String>> genReplaceInsertSql(String tablePrefix, String tableName,
			List<String[]> colList) {
		List<Pair<String, String>> sqlList = new ArrayList<Pair<String, String>>();
		if (colList != null && colList.size() > CSV_DATA_COL_START_INDEX) {
			for (int k = CSV_DATA_COL_START_INDEX; k < colList.size(); k++) {
				StringBuilder sb = new StringBuilder();
				String[] colNames = colList.get(0);
				String[] colJavaTypes = colList.get(1);
				String[] colValues = colList.get(k);

				String sqlPrefix = " REPLACE INTO " + tableName + " (";
				StringBuilder colNamesb = new StringBuilder();
				StringBuilder colValuesb = new StringBuilder();
				int cc = 0;
				for (int i = 0; i < colNames.length; i++) {
					String colName = colNames[i].toLowerCase().trim();
					if (colName.length() > 0) {
						String colJavaType = colJavaTypes[i].toLowerCase().trim();
						String colValue = "";
						if (colValues.length > i) {
							colValue = colValues[i].trim();
						}
						// 数组类型的不不能补0占位
						if (colValue.length() < 1 && !colJavaType.contains("[")
								&& (colJavaType.toLowerCase().startsWith("int")
										|| colJavaType.toLowerCase().startsWith("long")
										|| colJavaType.toLowerCase().startsWith("float"))) {
							colValue = "0";
						}
						if (colNames.length != colValues.length || colJavaTypes.length != colValues.length) {
							System.out.println(colNames.length + "|" + colJavaTypes.length + "|" + colValues.length
									+ "|colName=" + colName + "|colValue=" + colValue);
						}
						String colSqlType = GameCSV2DB.getSqlTypebyJavaType(colJavaType, colName, tableName);
						String colSqlDefault = GameCSV2DB.getSqlDefaultJavaType(colJavaType);
						// 有字段名,有字段值，有备注，才认为是合法的字段
						if (cc > 0) {
							colNamesb.append(",");
						}
						colNamesb.append(colName);
						if (cc > 0) {
							colValuesb.append(" , ");
						}
						cc++;
						if (colJavaType.contains("[]") || "String".equalsIgnoreCase(colJavaType)
								|| "Text".equalsIgnoreCase(colJavaType) || "str".equalsIgnoreCase(colJavaType)) {
							colValuesb.append("\"");
						}
						colValuesb.append(GameCSV2DB.encodeSQL(colValue));
						if (colJavaType.contains("[]") || "String".equalsIgnoreCase(colJavaType)
								|| "Text".equalsIgnoreCase(colJavaType) || "str".equalsIgnoreCase(colJavaType)) {
							colValuesb.append("\"");
						}
					} else {
						// //////// System.err.println("error: col info not
						// //////// full:colName=" + colName);
						// System.err.println("error: col info not
						// full:colName="
						// + colName
						// + "|colJavaType="
						// + colJavaType + "|colValue=" + colValue);
					}
				}

				sb.append(sqlPrefix).append(colNamesb).append(",added_time_long,lastupdated_time_long,del_flag")
						.append(") VALUES(");
				sb.append(colValuesb).append("," + (GameCSV2DB.currentSqlTimestamp4Long() / 1000000 * 1000000) + ","
						+ (GameCSV2DB.currentSqlTimestamp4Long() / 1000000 * 1000000)).append(",0);\n");
				sbInsertTableMap.get(tablePrefix).append(sb).append("\n");
				sqlList.add(Pair.makePair(colValues[0], sb.toString()));

			}
		}
		return sqlList;
	}
}
