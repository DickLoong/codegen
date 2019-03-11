package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.util.List;

import com.lizongbo.codegentool.CodeGenConsts;

/**
 * 生成protobuf模板
 * 
 * @author quickli
 *
 */
public class GenProtoBufcsvTask {

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		CodeGenConsts.switchPlat();
		GameCSV2DB.checkIsGapp();
		File csvFile = new File(CodeGenConsts.PROJCSVFILE_DIRROOT + "/Public/World/TProto(前后台数据协议)_Cmd(命令字).csv");
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		GameCSV2DB.genProtoFile(csvFile, tableName, colList);
		GameCSV2DB.protoCsvFile2Proto(new File(CodeGenConsts.PROJCSVFILE_DIRROOT));
		//再加一个生成bat的
		
		long endTime = System.currentTimeMillis();
		System.out.println("GenProtoBufcsvTask use time:" + (endTime - startTime) + "ms");
	}

}
