package com.lizongbo.codegentool.tools;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;

public class CheckAnimName {

	public static Set<String> animNameSet = new HashSet<String>();

	public static void main(String[] args) {
		animNameSet.add("walk");
		animNameSet.add("run");
		animNameSet.add("attack01");
		animNameSet.add("attack02");
		animNameSet.add("attack03");
		animNameSet.add("attack04");
		animNameSet.add("attack05");
		animNameSet.add("attack06");
		animNameSet.add("dengchangzhanshi");
		animNameSet.add("idle");
		animNameSet.add("skill01");
		animNameSet.add("skill02");
		animNameSet.add("skill03");
		animNameSet.add("skill04");
		animNameSet.add("skill05");
		animNameSet.add("fangun");
		animNameSet.add("qingshouji");
		animNameSet.add("zhongshouji");
		animNameSet.add("beijifei");
		animNameSet.add("beijitui");
		animNameSet.add("xuanyun");
		animNameSet.add("xuruo");
		animNameSet.add("dead");
		animNameSet.add("qingzhushengli");
		animNameSet.add("beijidao");
		animNameSet.add("fukongyingzhi");
		animNameSet.add("fukong");
		animNameSet.add("qishen");
		animNameSet.add("skill01_01");
		animNameSet.add("skill01_02");
		animNameSet.add("skill02_01");
		animNameSet.add("skill02_02");
		animNameSet.add("skill02_03");
		animNameSet.add("dead_02");
		animNameSet.add("dead_03");
		animNameSet.add("dead_04");
		animNameSet.add("dead_05");
		animNameSet.add("beijifei_02");
		animNameSet.add("beijifei_03");
		animNameSet.add("beijifei_04");
		animNameSet.add("beijifei_05");
		animNameSet.add("jipao");
		animNameSet.add("qieru");
		animNameSet.add("qiechu");
		animNameSet.add("shengliqingzhu");
		animNameSet.add("zhanshi");
		animNameSet.add("silie");
		animNameSet.add("shenglizhanshi");
		animNameSet.add("xuanyun");
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Art/Players/ModelFBXs";
		checkAnimFileName(new File(dir));
	}

	public static void checkAnimFileName(File f) {
		if (!f.exists()) {
			return;
		}
		if (f.isDirectory()) {
			File fs[] = f.listFiles();
			for (File subF : fs) {
				checkAnimFileName(subF);
			}
		} else {
			if (f.isFile() && f.getName().contains("@") && f.getName().toLowerCase().endsWith(".fbx")) {
				String animName = f.getName().substring(f.getName().indexOf("@") + 1);
				animName = StringUtil.replaceAll(animName, ".FBX", "");
				if (!animNameSet.contains(animName) && !animName.contains("_")) {
					System.err.println(f.getName() + "==" + animName);
				}
			}
		}
	}

}
