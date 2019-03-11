package com.lizongbo.codegentool.configverification.trewardback;

import java.io.*;
import java.util.*;

import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.configverification.tbackpack.TbackpackGiftpackCsvChecker;
import com.lizongbo.codegentool.csv2db.*;

public class TrewardbackPurgatorycopyCsvChecker implements CsvChecker {

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
		for (String[] item : colList) {
			try {
				// 检测是否一个配置项
				Integer.parseInt(item[0]);
			} catch (NumberFormatException ex) {
				continue;
			}
			int giftPackId = Integer.parseInt(CSVUtil.getColValue("back_diamonds_reward", item, colList));
			if(new TbackpackGiftpackCsvChecker().getCache(giftPackId) == null && giftPackId != 0){
				errorList.add("giftpack not exists|" + giftPackId);
			}
			giftPackId = Integer.parseInt(CSVUtil.getColValue("back_gold_reward", item, colList));
			if(new TbackpackGiftpackCsvChecker().getCache(giftPackId) == null && giftPackId != 0){
				errorList.add("giftpack not exists|" + giftPackId);
			}
		}

		return errorList;
	}

}
