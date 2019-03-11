package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.util.Arrays;

public class LevelResCheckTool {

	public static void main(String[] args) {
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets";
		checkDDSFile(new File(dir));
	}

	public static void checkDDSFile(File f) {
		if (!f.exists()) {
			GameCSV2DB.addErrMailMsgList("checkDDSFile|checkError|文件或文件夹不存在|" + f.getAbsolutePath());
			return;
		}
		if (f.isDirectory()) {
			File fs[] = f.listFiles();
			for (File subF : fs) {
				checkDDSFile(subF);
			}
		} else {
			if (f.isFile() && f.getName().toLowerCase().endsWith(".dds")) {
				System.err.println("手游不支持DDS格式" + f.getAbsolutePath());
				GameCSV2DB.addErrMailMsgList("checkDDSFile|checkError|手游不支持DDS格式|" + f.getAbsolutePath());
			}
		}
	}
}
