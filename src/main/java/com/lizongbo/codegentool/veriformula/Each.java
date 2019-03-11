package com.lizongbo.codegentool.veriformula;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.lizongbo.codegentool.tools.StringUtil;

public class Each extends DataVerifyFormula {

	@Override
	public List<String> calculate(String csvPath) {
		// TODO Auto-generated method stub
		String seperator = this.argsList.get(0);
		List<String> preResultList = new LinkedList<>();
		List<List<String>> resultList = new LinkedList<>();
		List<String> forheadList =  this.argsList.subList(1, argsList.size()-1);
		for(String forhead: forheadList){
			String tempString = new String(forhead);
			List<String> aa = 	StringUtil.splitList(tempString, seperator);
			resultList.add(aa);
		}
		for(List<String> resultLista : resultList ){
			preResultList.addAll(resultLista);
		}
		return preResultList;
	}

}
