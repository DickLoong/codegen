package com.lizongbo.codegentool.configverification.tbasetask;

import java.io.*;
import java.util.*;

import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.configverification.tbackpack.TbackpackGiftpackCsvChecker;
import com.lizongbo.codegentool.configverification.tdatacheck.TdatacheckSqlcheckerCsvChecker;
import com.lizongbo.codegentool.csv2db.*;
import com.lizongbo.codegentool.tools.StringUtil;

public class TbasetaskBasetaskrewardCsvChecker implements CsvChecker {

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
			int[] giftPackIds = StringUtil.splitInt(CSVUtil.getColValue("task_reward", item, colList),0);
			for(int giftPackId : giftPackIds){
				if(new TbackpackGiftpackCsvChecker().getCache(giftPackId) == null && giftPackId != 0){
					errorList.add("giftpack not exists|" + giftPackId);
				}
			}
		}
		return errorList;
	}
	
	public static void main(String[] args) {
		new TbasetaskBasetaskrewardCsvChecker().checkCsv("D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\csvfiles\\Public\\Common\\"
				+ "TBaseTask(基地任务配置表)_Basetaskreward(基地任务奖励).csv");
	}

}
