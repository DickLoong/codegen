package com.lizongbo.codegentool.configverification.toperateactivity;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.csv2db.*;
import com.lizongbo.codegentool.tools.StringUtil;

public class ToperateactivityActivityopenCsvChecker implements CsvChecker {

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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (String[] item : colList) {
			try {
				// 检测是否一个配置项
				Integer.parseInt(item[0]);
			} catch (NumberFormatException ex) {
				continue;
			}
			String specificStartTime = CSVUtil.getColValue("num_int", item, colList);
			String specificEndTime = CSVUtil.getColValue("num_int", item, colList);
			try{
				sdf.format(specificStartTime);
				try{
					sdf.format(specificEndTime);
				}catch(IllegalArgumentException illegalConfig){
					errorList.add("operate activity has illegal date config|" + item[0] + "|" + specificStartTime + "|" + specificEndTime);
				}
			}catch(IllegalArgumentException notASpecificDate){
				continue;
			}
		}

		return errorList;
	}

}
