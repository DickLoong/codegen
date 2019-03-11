package com.lizongbo.codegentool.veriformula;

import java.util.LinkedList;
import java.util.List;

import com.lizongbo.codegentool.csv2db.CSVUtil;

public class GetValueByColName extends DataVerifyFormula {

	@Override
	public List<String> calculate(String csvPath) {
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvPath);
		String colName = this.argsList.get(0);
		List<String> valueList = new LinkedList<>();
		for(String[] singleItem : colList.subList(4, colList.size() - 1)){
			String value = CSVUtil.getColValue(colName, singleItem, colList);
			valueList.add(value);
		}
		return valueList;
	}

}
