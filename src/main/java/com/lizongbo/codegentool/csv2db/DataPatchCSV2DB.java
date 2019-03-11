package com.lizongbo.codegentool.csv2db;

public class DataPatchCSV2DB {
	public static void main(String[] args) {
		
		if(null == args || args.length == 0){
			System.out.println("请指定一个数据补丁包的位置");
			System.exit(0);
		}
		GameCSV2DB.DataPatchCSV2DB(args);
	}
}
