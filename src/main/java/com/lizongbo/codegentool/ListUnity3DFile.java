package com.lizongbo.codegentool;

import java.io.*;

public class ListUnity3DFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int cc = 0;
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/StreamingAssets/AssetBundle/Android";
		for (File f : new File(dir).listFiles()) {
			if (f.isDirectory()) {
				for (File f2 : f.listFiles()) {
					if (f2.getName().endsWith(".unity3d") && f2.getName().length() > ".unity3d".length()) {
						System.err.println("\"" + f.getName() + "/" + f2.getName() + "\",");
						cc++;
					}
				}

			}
		}
		System.out.println(cc);

	}

}
