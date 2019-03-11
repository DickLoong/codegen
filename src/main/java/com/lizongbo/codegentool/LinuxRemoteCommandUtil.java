package com.lizongbo.codegentool;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.lizongbo.codegentool.tools.StringUtil;

public class LinuxRemoteCommandUtil {
	
	public static void main(String[] args) {
		// runCmd("10.0.0.16", 22, "bilin", "bilinkeji.net", "env;ls -alh
		// /usr/local/");
		// runCmd("10.0.0.16", 22, "bilin", "bilinkeji.net", "ls -alh
		// /usr/local/apps/");

		String linuxUserName = "root";
		String linuxUserPwd = "quick10343QQ";
		String serverInerIp = "112.74.108.38";
		LinuxRemoteCommandUtil.runCmd(serverInerIp, 22, linuxUserName, linuxUserPwd,
				"cd /usr/local/apps/;unzip ./resin_8090.zip;");
	}
	
	/**
	 * 2017-09-10 linyaoheng 添加sshPort端口的处理,通过System.setProperty来传递,默认是22
	 */
	public static void SetSSHPort(String port){
		if (null != port){
			System.setProperty("DEPLOY_SSH_PORT", port);
		}
	}
	
	/**
	 * 2017-09-10 linyaoheng 添加sshPort端口的处理,通过System.setProperty来传递,默认是22
	 */
	public static int GetSSHPort(){
		return StringUtil.toInt(System.getProperty("DEPLOY_SSH_PORT", "22"));
	}

	/**
	 * 远程执行shell命令
	 * 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param shellCmd
	 */
	public static void runCmd(String host, int port, String userName, String password, String shellCmd) {
		LogUtil.printLog("LinuxRemoteCommandUtil.runCmd|" + userName + "@" + host + ":" + GetSSHPort() + "/ " + shellCmd);
		try {
			JSch jsch = new JSch();
			Session session;
			try
			{
				session = jsch.getSession(userName, host, GetSSHPort());
				session.setTimeout(5000);     //5秒超时

				// username and password will be given via UserInfo interface.
				UserInfo ui = new LinuxUerInfo(password);
				session.setUserInfo(ui);
				java.util.Properties config = new java.util.Properties();
		        config.put("StrictHostKeyChecking", "no");
		        session.setConfig(config);
				session.connect();
			}
			catch(Exception e)
			{
				LogUtil.printLogErr("LinuxRemoteCommandUtil.runCmd connectTimeout #1 " + e);
				
				session = null;
				
				for (int i = 0; i < 5; i++) {
					//重试
					try {						
						LogUtil.printLog("trying connect " + (i+1) + " " + host);
						session = null;
						session = jsch.getSession(userName, host, GetSSHPort());
						session.setTimeout(5000); //5秒超时
						// username and password will be given via UserInfo interface.
						UserInfo ui = new LinuxUerInfo(password);
						session.setUserInfo(ui);
						java.util.Properties config = new java.util.Properties();
						config.put("StrictHostKeyChecking", "no");
						session.setConfig(config);
						session.connect();
						LogUtil.printLog("LinuxRemoteCommandUtil.runCmd reconnect successfully");
						
						break;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}

			boolean ptimestamp = true;

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(shellCmd);

			// get I/O streams for remote scp
			// OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();
			channel.setInputStream(null);
			// channel.setOutputStream(System.out);
			channel.connect();
			byte[] tmp = new byte[1024];
			StringBuilder sb = new StringBuilder(1024 * 8);
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					String line = new String(tmp, 0, i);
					sb.append(line).append("\n");
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					int exitStatus = channel.getExitStatus();
					
					//运行指令出错,打error,发邮件,退出
					if (exitStatus > 0) 
					{
						//String line = "error:" + exitStatus + " ,see http://tldp.org/LDP/abs/html/exitcodes.html";
						//System.err.println(line);
						String errorMsg = "[run remote cmd error],errorCode:" + exitStatus + ",(" + host +"): " + shellCmd;
						
						LogUtil.printLogErr(errorMsg);
						LogUtil.printLogErr("[remoteServer terminal info]: " + sb);
						
						MailTest.sendErrorMail("[run remote cmd error]", errorMsg);
						System.exit(1);
					}
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}
	
	/**
	 * 调用此方法请记得处理完逻辑后必须调用session.disconnect(),不然进程在多线程方式中无法正常结束
	 */
	public static Session getSSHSession(String host, int port, String userName, String password)
	{
		LogUtil.printLog("LinuxRemoteCommandUtil.getSSHSession|" + userName + "@" + host + ":" + GetSSHPort());
		try {
			JSch jsch = new JSch();
			Session session;
			try
			{
				session = jsch.getSession(userName, host, GetSSHPort());
				session.setTimeout(5000);     //5秒超时

				// username and password will be given via UserInfo interface.
				UserInfo ui = new LinuxUerInfo(password);
				session.setUserInfo(ui);
				java.util.Properties config = new java.util.Properties();
		        config.put("StrictHostKeyChecking", "no");
		        session.setConfig(config);
				session.connect();
			}
			catch(Exception e)
			{
				LogUtil.printLogErr("LinuxRemoteCommandUtil.runCmd connectTimeout #1 " + e);
				
				session = null;
				
				for (int i = 0; i < 5; i++) {
					//重试
					try {						
						LogUtil.printLog("trying connect " + (i+1) + " " + host);
						session = null;
						session = jsch.getSession(userName, host, GetSSHPort());
						session.setTimeout(5000); //5秒超时
						// username and password will be given via UserInfo interface.
						UserInfo ui = new LinuxUerInfo(password);
						session.setUserInfo(ui);
						java.util.Properties config = new java.util.Properties();
						config.put("StrictHostKeyChecking", "no");
						session.setConfig(config);
						session.connect();
						LogUtil.printLog("LinuxRemoteCommandUtil.runCmd reconnect successfully");
						
						break;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			
			return session;
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
		
		return null;
	}
	
	/**
	 * 调用此方法请使用getSSHSession获得一个有效的SSH会话
	 * 调用此方法请记得处理完逻辑后必须调用session.disconnect(),不然进程在多线程方式中无法正常结束
	 */
	public static void runCmd(Session session, String shellCmd) {
		LogUtil.printLog("LinuxRemoteCommandUtil.runCmd|" + session.getUserName() + "@" + session.getHost() + ":" + session.getPort() + "/ " + shellCmd);
		try {
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(shellCmd);

			// get I/O streams for remote scp
			// OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();
			channel.setInputStream(null);
			// channel.setOutputStream(System.out);
			channel.connect();
			byte[] tmp = new byte[1024];
			StringBuilder sb = new StringBuilder(1024 * 8);
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					String line = new String(tmp, 0, i);
					sb.append(line).append("\n");
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					int exitStatus = channel.getExitStatus();
					
					//运行指令出错,打error,发邮件,退出
					if (exitStatus > 0) 
					{
						//String line = "error:" + exitStatus + " ,see http://tldp.org/LDP/abs/html/exitcodes.html";
						//System.err.println(line);
						String errorMsg = "[run remote cmd error],errorCode:" + exitStatus + ",(" + session.getHost() +"): " + shellCmd;
						
						LogUtil.printLogErr(errorMsg);
						LogUtil.printLogErr("[remoteServer terminal info]: " + sb);
						
						MailTest.sendErrorMail("[run remote cmd error]", errorMsg);
						System.exit(1);
					}
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}

}
