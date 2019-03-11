package com.lizongbo.codegentool.configverification.tdatacheck;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.configverification.tequip.TequipTequipCsvChecker;
import com.lizongbo.codegentool.csv2db.*;

public class TdatacheckSqlcheckerCsvChecker implements CsvChecker {

	@Override
	public List<String> checkCsv(String csvPath) {
		List<String> errorList = new ArrayList<String>();
		File csvFile = new File(csvPath);
		if (!csvFile.exists() || !csvFile.isFile()) {
			errorList.add("File Error:" + csvFile);
			return errorList;
		}
		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		Connection conn = GameCSV2DB.getDbCoon();
		// TODO 把全部检验sql导出来
		Map<Integer, Map<String, Object>> checkerMaps = CSVUtil.getDataCacheMap(colList);
		// TODO for循环，执行检验sql，
		/*try {
			for (Map<String, Object> checker : checkerMaps.values()) {
				// TODO 如果检验结果不为空，把sql，查出来的数据还有出错提示打印出来，
				boolean passed = true;
				Statement stmt = conn.createStatement();
				ResultSet resultSet = stmt.executeQuery((String)checker.get("checking_sql"));
				// TODO 如果存在出错的sql，return errorlist
				String errorString = "bugs found |" + checker.get("error_hint") + "|" + (String)checker.get("checking_sql");
				while(resultSet.next()){
					errorString = errorString +  "|" + resultSet.getObject(1);
					passed = passed ? !passed : false;
				}
				if(passed){
					System.out.println("data check|" + checker.get("checker_id") + "| check passed");
				}else{
					errorList.add(errorString);
					System.out.println("data check|" + checker.get("checker_id") + "| check failed |" + errorString);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return errorList;
	}
	
	//TDataCheck(数据校验)_Sqlchecker(校验sql语句)
	
	public static void main(String[] args) {
		new TdatacheckSqlcheckerCsvChecker().checkCsv("D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\csvfiles\\Public\\Common\\"
				+ "TDataCheck(数据校验)_Sqlchecker(校验sql语句).csv");
	}

}
