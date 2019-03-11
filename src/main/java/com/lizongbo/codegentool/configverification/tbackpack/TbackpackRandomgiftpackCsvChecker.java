package com.lizongbo.codegentool.configverification.tbackpack;

import java.io.*;
import java.util.*;

import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.configverification.tequip.TequipTequipCsvChecker;
import com.lizongbo.codegentool.csv2db.*;

public class TbackpackRandomgiftpackCsvChecker implements CsvChecker {

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
		// TODO 先遍历第一行
		// TODO 获取全部字段名
		if (dataCache.size() <= 0) {
			dataCache.putAll(CSVUtil.getDataCacheMap(colList));
		}
		return errorList;
	}

	@Override
	public Map<String, Object> getCache(int id) {
		return dataCache.get(id);
	}

	private static Map<Integer, Map<String, Object>> dataCache = new HashMap<>();


}
