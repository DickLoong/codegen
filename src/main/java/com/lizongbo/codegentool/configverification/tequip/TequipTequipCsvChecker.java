package com.lizongbo.codegentool.configverification.tequip;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lizongbo.codegentool.configverification.CsvChecker;
import com.lizongbo.codegentool.csv2db.CSVUtil;

public class TequipTequipCsvChecker implements CsvChecker {

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
		if(dataCache.size() <= 0){
			dataCache.putAll(CSVUtil.getDataCacheMap(colList));
		}
		return errorList;
	}

	@Override
	public Map<String, Object> getCache(int id) {
		return dataCache.get(id);
	}
	
	private static Map<Integer,Map<String, Object>> dataCache = new HashMap<>();
	
	public static void main(String[] args) {
		new TequipTequipCsvChecker().checkCsv("D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\csvfiles\\Public\\Common\\"
				+ "TEquip(装备信息)_Tequip(装备信息).csv");
	}

}
