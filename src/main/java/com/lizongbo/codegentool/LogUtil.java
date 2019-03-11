package com.lizongbo.codegentool;

import java.util.Date;
import com.lizongbo.codegentool.tools.StringUtil;

public class LogUtil 
{
	private static void printLogInner(String logType, String logStr)
	{
		//############### deploy_log ################### 2017-06-17 22:38 日志 ############### deploy_log ###################
		String hintStr = "########## deploy_log ##########";
		String dateStr = StringUtil.currentSqlTimestampStr() + ":";
		String outputStr = hintStr + " " + dateStr + " [" + logType + "] " + logStr;
		System.out.println(outputStr);
	}
	
	public static void printLog(String logStr)
	{
		printLogInner("info", logStr);
	}
	
	public static void printLogErr(String logStr)
	{
		printLogInner("error", logStr);
		
		new Throwable(logStr).printStackTrace();
	}
}
