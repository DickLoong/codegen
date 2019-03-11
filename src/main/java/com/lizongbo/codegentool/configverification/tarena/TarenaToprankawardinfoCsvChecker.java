package com.lizongbo.codegentool.configverification.tarena;

import java.io.*;
import java.util.*;

import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.configverification.tbackpack.TbackpackGiftpackCsvChecker;
import com.lizongbo.codegentool.csv2db.*;

public class TarenaToprankawardinfoCsvChecker implements CsvChecker {

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
			int giftPackId = Integer.parseInt(CSVUtil.getColValue("rewardId", item, colList));
			if(new TbackpackGiftpackCsvChecker().getCache(giftPackId) == null && giftPackId != 0){
				errorList.add("giftpack not exists|" + giftPackId);
			}
		}

		return errorList;
	}
	
	public static void main(String[] args) {
		new TarenaToprankawardinfoCsvChecker().checkCsv("D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\csvfiles\\Public\\Common\\"
				+ "TArena(竞技场)_Toprankawardinfo(最高排名奖).csv");
	}

}
