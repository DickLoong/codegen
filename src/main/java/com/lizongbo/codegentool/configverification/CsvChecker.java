package com.lizongbo.codegentool.configverification;

import java.util.List;
import java.util.Map;

/**
 * 校验csv，校验通不过的，代码不给生成出来
 * 
 * @author quickli
 *
 */
public interface CsvChecker {
	/**
	 * 校验指定路径的csv，并返回出错信息，如果校验ok，返回空list
	 * 
	 * @param csvPath
	 * @return
	 */
	public List<String> checkCsv(String csvPath);
	
	default public Map<String,Object> getCache(int id){
		return null;
	}
	
}
