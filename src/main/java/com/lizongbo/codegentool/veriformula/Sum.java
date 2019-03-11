package com.lizongbo.codegentool.veriformula;

import java.util.LinkedList;
import java.util.List;

public class Sum extends DataVerifyFormula{

	@Override
	public List<String> calculate(String csvPath) {
		// TODO Auto-generated method stub
		List<String> resultList = new LinkedList<>();
		Integer sum = this.argsList.stream().mapToInt(e -> Integer.parseInt(e)).sum();
		resultList.add(sum.toString());
		return resultList;
	}

}
