package com.lizongbo.codegentool;

import java.util.Map;
import java.util.Map.Entry;

public class DeployUtil {

	/**
	 * 显示当前运行环境的所有环境变量
	 */
	public static void ShowEnv(){
		Map<String, String> envMap = System.getenv();
		
		for (Entry<String, String> item : envMap.entrySet()){
			LogUtil.printLog(item.getKey() + "==>" + item.getValue());
		}
	}
	
}
