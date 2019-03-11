package com.lizongbo.codegentool.tools;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.lizongbo.codegentool.MailTest;
import com.lizongbo.codegentool.csv2db.CSVUtil;

public class UIResCheck {

	public static Set<String> getEffectPrafabs() {
		Set<String> set = new HashSet<String>();
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Art/UI";
		getEffectPrafabs(dir, set);
		return set;
	}

	public static Set<String> getEffectPrafabs(String dir, Set<String> set) {
		File f = new File(dir);
		if (f.isDirectory()) {
			File[] subfs = f.listFiles();
			for (File subf : subfs) {
				if (subf.isFile() && !subf.getName().endsWith(".prefab") && subf.getName().startsWith("ui_")) {
					String name = subf.getName();
					name = name.substring(0, subf.getName().indexOf("."));
					set.add(name);
				}
				if (subf.isDirectory()) {
					set.addAll(getEffectPrafabs(subf.getAbsolutePath(), set));
				}
			}
		}
		return set;
	}

	public static Set<String> getLostUis(Set<String> esSet) {
		Set<String> set = new HashSet<String>();
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles";
		getLostUis(dir, set, esSet);
		return set;
	}

	public static Set<String> getLostUis(String dir, Set<String> set, Set<String> esSet) {
		// System.out.println("getLostUis|" + dir);
		File f = new File(dir);
		if (f.isDirectory()) {
			File[] subfs = f.listFiles();
			for (File subf : subfs) {
				if (subf.isFile() && subf.getName().endsWith(".csv")) {
					// System.out.println("check csv " + subf);
					// 加载csv
					List<String[]> colList = CSVUtil.getDataFromCSV2(subf.getAbsolutePath());
					for (int i = 4; i < colList.size(); i++) {
						String[] s = colList.get(i);
						for (String as : s) {
							if (as.startsWith("ui_") && !esSet.contains(as)) {
								set.add(as);
								System.out.println("资源不存在：" + as + "|来自表：" + subf.getName() + "|" + Arrays.toString(s));
							}
						}

					}
				}
				if (subf.isDirectory()) {
					set.addAll(getLostUis(subf.getAbsolutePath(), set, esSet));
				}
			}
		}
		return set;
	}

	public static void main(String[] args) {
		Set<String> es = getEffectPrafabs();
		System.out.println("UIResCheck|" + es.size() + "|" + es);
		System.out.println(new TreeSet<String>(getLostUis(es)));
		;
	}

}
