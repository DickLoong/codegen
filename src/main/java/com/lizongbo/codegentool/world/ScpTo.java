package com.lizongbo.codegentool.world;

/**
 * 从本地SCP文件到远程
 *
 */
public class ScpTo {

	/**
	 * host
	 * login
	 * pwd
	 * localFile
	 * remoteFile
	 * @param args
	 */
	public static void main(String[] args) {
		String host = args[0];
		String login = args[1];
		String pwd = args[2];
		String localFile = args[3];
		String remoteFile = args[4];
		
		ScpCommandUtil.ScpTo(host, login, pwd, localFile, remoteFile);
	}
	
}
