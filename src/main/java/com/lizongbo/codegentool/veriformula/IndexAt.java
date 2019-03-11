package com.lizongbo.codegentool.veriformula;

import java.util.LinkedList;
import java.util.List;

import com.lizongbo.codegentool.tools.StringUtil;

public class IndexAt extends DataVerifyFormula {

	@Override
	public List<String> calculate(String csvPath) {
		// TODO Auto-generated method stub
		String separator = this.argsList.get(1);
		int index = Integer.parseInt(this.argsList.get(0));
		List<String> changableArgsList = this.argsList.subList(2, this.argsList.size() - 1);
		List<String> resultList = new LinkedList<>();
		for (String preList : changableArgsList) {
			List<String> e = StringUtil.splitList(preList,separator);
			if (e.size() > index + 1) {
				resultList.add(e.get(index));
			}

		}
		return resultList;
	}

}
