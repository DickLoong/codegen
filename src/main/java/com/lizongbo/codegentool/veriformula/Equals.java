package com.lizongbo.codegentool.veriformula;

import java.util.LinkedList;
import java.util.List;

public class Equals extends DataVerifyFormula {

	@Override
	public List<String> calculate(String csvPath) {
		// TODO Auto-generated method stub
		Boolean result =  this.argsList.get(0).equals(this.argsList.get(1));
		List<String> resultList = new LinkedList<>();
		resultList.add(result.toString());
		return resultList;
	}

}
