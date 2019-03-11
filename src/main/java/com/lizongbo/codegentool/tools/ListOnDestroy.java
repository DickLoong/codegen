package com.lizongbo.codegentool.tools;

import java.io.File;
import java.util.TreeSet;

import com.lizongbo.codegentool.db2java.GenAll;

public class ListOnDestroy {
	// static int i = 0;
	// static TreeSet<String> set = new TreeSet<String>();

	public static void main(String[] args) {
		listWindow(new File(
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Scripts"));

	}

	public static void listWindow(File f) {
		if (f.isFile() && f.getName().endsWith("Window.cs")) {
			String csTxt = GenAll.readFile(f.getAbsolutePath(), "UTF-8");
			if (csTxt.contains(": Window") && csTxt.contains("OnDestroy")) {// 确实是窗口但是有OnDestroy
				System.err.println(f);
			}

		}
		if (f.isDirectory()) {
			File[] subfs = f.listFiles();
			for (int i = 0; i < subfs.length; i++) {
				listWindow(subfs[i]);
			}
		}

	}
}
