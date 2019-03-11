package com.lizongbo.codegentool.world;

/**
 * 从本地SCP文件到远程
 *
 */
public class ScpFrom {

	/**
	 * host
	 * login
	 * pwd
	 * remoteFile
	 * localFile
	 * @param args
	 */
	public static void main(String[] args) {
		String host = args[0];
		String login = args[1];
		String pwd = args[2];
		String remoteFile = args[3];
		String localFile = args[4];
		
		ScpCommandUtil.ScpFrom(host, login, pwd, remoteFile, localFile);
	}
	
}
