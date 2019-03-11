package com.lizongbo.codegentool.world;

/**
 * 将真机包SCP到共享服务器
 * @author linyaoheng
 *
 */
public class ScpClientToShareServer {

	/**
	 * packagePath
	 * destname
	 * @param args
	 */
	public static void main(String[] args) {
		String packagePath = args[0];
		String destName = args[1];
		
		ScpCommandUtil.ScpTo("10.0.0.16", "bilin", "bilinkeji.net", packagePath, "/mgamedev/tools/devtool_setupfiles/体验包/" + destName);
	}
	
}
