package com.lizongbo.codegentool.tools;

import java.util.*;

import com.lizongbo.codegentool.GuidCompare;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.db2java.GenAll;

import java.io.*;

/***
 * 替换unity的资源引用为新资源的工具，确保不会出错
 * 
 * @author quickli
 *
 */
public class ReplaceUnitymetaRef {

	public static void main(String[] args) {
		// 需要替换资源的Assets文件夹路径
		String assetsDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/";
		Map<String, String> fileReplaceMap = new TreeMap<String, String>();
		// 在这里发
		/*
		fileReplaceMap.put("/Art/UIImage/BasicUIElement/Common.SpriteMapping/ui_common_xing_bai.png",
				"/Art/UIImage/UIModule/Mecha/SpriteMapping/ui_mecha_xing_bai.png");
		fileReplaceMap.put("/Art/UIImage/BasicUIElement/Common.SpriteMapping/ui_common_xing_lv.png",
				"/Art/UIImage/UIModule/Mecha/SpriteMapping/ui_mecha_xing_lv.png");
		fileReplaceMap.put("/Art/UIImage/BasicUIElement/Common.SpriteMapping/ui_common_xing_lan.png",
				"/Art/UIImage/UIModule/Mecha/SpriteMapping/ui_mecha_xing_lan.png");
		fileReplaceMap.put("/Art/UIImage/BasicUIElement/Common.SpriteMapping/ui_common_xing_zi.png",
				"/Art/UIImage/UIModule/Mecha/SpriteMapping/ui_mecha_xing_zi.png");
		fileReplaceMap.put("/Art/UIImage/BasicUIElement/Common.SpriteMapping/ui_common_xing_cheng.png",
				"/Art/UIImage/UIModule/Mecha/SpriteMapping/ui_mecha_xing_cheng.png");
		*/
		fileReplaceMap.put("/Art/UIImage/BasicUIElement/Common.SpriteMapping/ui_common_xing_hong.png",
				"/Art/UIImage/UIModule/Mecha/SpriteMapping/ui_mecha_xing_hong.png");

		Map<String, String> guidReplaceMap = new TreeMap<String, String>();
		for (Map.Entry<String, String> e : fileReplaceMap.entrySet()) {
			String orgGuid = GuidCompare.getGuid(new File(assetsDir + "/" + e.getKey() + ".meta"));
			String replaceGuid = GuidCompare.getGuid(new File(assetsDir + "/" + e.getValue() + ".meta"));
			if (orgGuid.length() < 4 || replaceGuid.length() < 4) {
				System.err.println("找不到guid，不能替换：" + e.getKey() + "|guid=" + orgGuid);
				System.err.println("找不到guid，不能替换：" + e.getValue() + "|guid=" + replaceGuid);
				return;
			}
			System.out.println(e.getKey() + " === " + orgGuid);
			System.out.println(e.getValue() + " === " + replaceGuid);
			guidReplaceMap.put(orgGuid, replaceGuid);
		}
		//System.out.println("先不替换");
		//System.exit(1);
		System.out.println("开始替换：" + fileReplaceMap);
		System.out.println("开始替换guid：" + guidReplaceMap);
		replacePrefabFileByMap(new File(assetsDir), guidReplaceMap);
	}

	public static void replacePrefabFileByMap(File fbxDir, Map<String, String> map) {
		if (fbxDir.isDirectory()) {
			File fs[] = fbxDir.listFiles();
			for (File f : fs) {
				replacePrefabFileByMap(f, map);
			}

		}
		if (fbxDir.isFile() && fbxDir.getName().endsWith(".prefab")) {
			String fileText = GenAll.readFile(fbxDir.getAbsolutePath(), "UTF-8");
			boolean hashGuid = false;
			for (Map.Entry<String, String> e : map.entrySet()) {
				if (fileText.contains(e.getKey())) {
					hashGuid = true;
				}
			}
			if (hashGuid) {
				System.out.println(fbxDir + "|需要替换metaid");
				for (Map.Entry<String, String> e : map.entrySet()) {
					fileText = fileText.replaceAll(e.getKey(), e.getValue());
				}
			}
			GameCSV2DB.writeFile(fbxDir.getAbsolutePath(), fileText);
		} else {
			// System.err.println("文件夹不存在" + fbxDir);
		}
	}
}
