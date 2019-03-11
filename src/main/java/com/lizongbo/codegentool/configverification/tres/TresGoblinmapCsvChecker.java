package com.lizongbo.codegentool.configverification.tres;

import java.io.*;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.lizongbo.codegentool.configverification.*;
import com.lizongbo.codegentool.csv2db.*;
import com.lizongbo.codegentool.tools.StringUtil;

public class TresGoblinmapCsvChecker implements CsvChecker {

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
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			{
				String jsonText = CSVUtil.getColValue("goblin_json_data", s, colList);
				if (jsonText != null && jsonText.trim().length() > 0) {
					try {
						jsonText = StringUtil.tryZipedHex2Json(jsonText);
						JSONObject jo = new JSONObject(jsonText);
					} catch (Exception e) {
						errorList.add("json error:" + e + s[0] + "|" + s[1]);
					}
				}

			}
		}

		return errorList;
	}

	public static void main(String[] args) {
		String csvPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TRes(资源信息)_Goblinmap(地图种怪配置表).csv";
		TresGoblinmapCsvChecker cc = new TresGoblinmapCsvChecker();
		System.out.println(cc.checkCsv(csvPath));
	}

}
