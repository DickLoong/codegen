package com.lizongbo.codegentool.world;

import java.io.File;

import com.lizongbo.codegentool.LogUtil;

/**
 * 传入的参数必须是文件
 * @author linyaoheng
 *
 */
public class BlMkdir {
	
	public static void main(String[] args){
		if (args.length == 0){
			LogUtil.printLogErr("No arguments");
			
			System.exit(1);
		}
		
		String path = args[0];
		File f = new File(path);
		if (!f.exists()){
			f.getParentFile().mkdirs();
		}
	}

}
