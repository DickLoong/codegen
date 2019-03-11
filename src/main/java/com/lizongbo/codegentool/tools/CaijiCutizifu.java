package com.lizongbo.codegentool.tools;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.TreeSet;

import com.lizongbo.codegentool.db2java.GenAll;

public class CaijiCutizifu {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] files = new String[] {
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/World/TLocale(文本本地化)_Zh(中文信息).csv",
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Client/Tui(UI配置)_Windowinfo(ui窗口配置信息).csv",
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TComm(基础配置)_Errcode(错误码信息).csv",
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TBackpack(背包系统)_Item(物品信息).csv",

		};
		String[] dirs = new String[] {
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common",
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Client" };

		TreeSet<Character> ts = new TreeSet<Character>();

		for (int i = 0; i < dirs.length; i++) {
			String string = dirs[i];
			File[] fs = new File(string).listFiles();
			for (int j = 0; j < fs.length; j++) {
				File file = fs[j];
				if (file.getName().endsWith(".csv")) {
					String fileText = GenAll.readFile(file.getAbsolutePath(), "UTF-8");
					System.out.println("读" + file);
					char[] cs = fileText.toCharArray();
					for (int k = 0; k < cs.length; k++) {
						char c = cs[k];
						ts.add(c);
					}
				}
			}
		}
		for (int i = 32; i < files.length; i++) {
			String string = files[i];
			String fileText = GenAll.readFile(string, "UTF-8");
			char[] cs = fileText.toCharArray();
			for (int j = 0; j < cs.length; j++) {
				char c = cs[j];
				ts.add(c);
			}
		}

		Charset gb2312 = java.nio.charset.Charset.forName("GB2312");
		CharsetEncoder ce = gb2312.newEncoder();
		for (char c = 256; c < 65533; c++) {
			if (ce.canEncode(c)) {
				ts.add(c);
			}
		}
		System.out.println(ts.size());
		System.out.println(ts);
		StringBuilder sb = new StringBuilder(10086);
		for (char c : ts) {
			sb.append(c);
		}
		System.out.println(sb);
		System.out.println();

	}

}
