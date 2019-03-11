package com.lizongbo.codegentool;

import java.io.*;

import com.jcraft.jsch.*;
import com.lizongbo.codegentool.world.DeployCommandUtil;

public class SCPUtil {
	public static void main(String[] arg) {
		if (arg == null || arg.length < 2) {
			arg = new String[] { "/Users/quickli/Downloads/jdk-8u66-linux-x64.gz",
					"bilin@10.0.0.16:/usr/local/apps/jdk-8u66-linux-x64.gz"

			};
		}
		if (arg.length != 2) {
			System.err.println("usage: java ScpTo file1 user@remotehost:file2");
			System.exit(-1);
		}

		String lfile = arg[0];
		String user = arg[1].substring(0, arg[1].indexOf('@'));
		arg[1] = arg[1].substring(arg[1].indexOf('@') + 1);
		String host = arg[1].substring(0, arg[1].indexOf(':'));
		String rfile = arg[1].substring(arg[1].indexOf(':') + 1);
		// doSCP(lfile, user, "bilinkeji.net", host, rfile);

		// SCPUtil.doSCPTo("/Users/quickli/Downloads/devtool_setupfiles/Common/JDK/jdk-8u66-linux-x64.gz",
		// "root",
		// "quick10343QQ", "112.74.108.38",
		// "/usr/local/apps/jdk-8u66-linux-x64.gz");
		SCPUtil.doSCPFrom("bilin", "bilinkeji.net", "10.0.0.16", "/usr/local/apps/mysql-5.7.9.tar.gz",
				"/Users/quickli/Downloads/");
	}

	/**
	 * 
	 * 往远程服务器scp文件上去
	 * 
	 * @param lfile
	 *            本地文件路径
	 *            ，比如/Users/quickli/Downloads/devtool_setupfiles/Common/JDK/jdk-
	 *            8u66-linux-x64.gz
	 * @param user
	 *            远程服务器用户名，比如root
	 * @param pwd
	 *            远程服务器用户密码，比如quickli
	 * @param host
	 *            远程服务器ip或主机名,比如vps.lizongbo.com
	 * @param rfile
	 *            远程存放文件的路径，比如/usr/local/apps/jdk-8u66-linux-x64.gz
	 */
	public static void doSCPTo(String lfile, String user, String pwd, String host, String rfile) {
		System.out.println("try doSCPTo|scp " + lfile + " " + user + "@" + host + ":" + rfile);
		FileInputStream fis = null;
		try {
			//总是先创建远程目录
			File remoteParent = new File(rfile).getParentFile();
			LogUtil.printLog("mkdir -p " + remoteParent.toString().replace("\\", "/") + "/");
			LinuxRemoteCommandUtil.runCmd(host, LinuxRemoteCommandUtil.GetSSHPort(), user, pwd,
					"mkdir -p " + remoteParent.toString().replace("\\", "/") + "/");
			
			JSch jsch = new JSch();
			Session session = null;
			
			try
			{
				session = jsch.getSession(user, host, LinuxRemoteCommandUtil.GetSSHPort());

				// username and password will be given via UserInfo interface.
				UserInfo ui = new LinuxUerInfo(pwd);
				session.setUserInfo(ui);
				session.connect();
			}
			catch(Exception ex2)
			{
				LogUtil.printLogErr("doSCPTo connectTimeout #1 " + ex2);
				
				session = null;
				
				for (int i = 0; i < 5; i++) {
					//重试
					try {
						LogUtil.printLog("trying connect " + (i+1) + " " + host);
						session = null;
						session = jsch.getSession(user, host, LinuxRemoteCommandUtil.GetSSHPort());
						// username and password will be given via UserInfo interface.
						UserInfo ui = new LinuxUerInfo(pwd);
						session.setUserInfo(ui);
						session.connect();
						LogUtil.printLog("doSCPTo reconnect successfully");
						
						break;
					} catch (Exception er) {
						er.printStackTrace();
					}
				}
			}

			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				System.err.println("Error:111111111111111");
				System.exit(1);
			}

			File _lfile = new File(lfile);

			if (ptimestamp) {
				command = "T" + (_lfile.lastModified() / 1000) + " 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
				System.out.println("scp111 command ==" + command);
				out.write(command.getBytes());
				out.flush();
				if (checkAck(in) != 0) {
					System.err.println("Error:222222222222");
					System.exit(1);
				}
			}

			// send "C0644 filesize filename", where filename should not include
			// '/'
			long filesize = _lfile.length();
			command = "C0644 " + filesize + " ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			} else {
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes());
			System.out.println("scp222 command ==" + command);
			out.flush();
			if (checkAck(in) != 0) {
				System.err.println("Error:3333333333333333");
				System.exit(0);
			}

			// send a content of lfile
			fis = new FileInputStream(lfile);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				System.err.println("Error:44444444444444444");
				System.exit(0);
			}
			out.close();

			channel.disconnect();
			session.disconnect();

			// System.exit(0);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
			
			System.exit(1);
		}
	}

	/**
	 * 从远程服务器scp下载文件回来
	 * 
	 * @param user
	 *            远程服务器用户名，比如root
	 * @param pwd
	 *            远程服务器用户密码，比如quickli
	 * @param host
	 *            远程服务器ip或主机名,比如vps.lizongbo.com
	 * @param rfile
	 *            远程存放文件的路径，比如/usr/local/apps/jdk-8u66-linux-x64.gz
	 * @param lfile
	 *            本地文件路径
	 *            ，比如/Users/quickli/Downloads/devtool_setupfiles/Common/JDK/jdk-
	 *            8u66-linux-x64.gz
	 */
	public static void doSCPFrom(String user, String pwd, String host, String rfile, String lfile) {

		String prefix = null;
		if (new File(lfile).isDirectory()) {
			prefix = lfile + File.separator;
		}

		FileOutputStream fos = null;
		try {

			JSch jsch = new JSch();
			Session session = null;
			try
			{
				session = jsch.getSession(user, host, LinuxRemoteCommandUtil.GetSSHPort());

				// username and password will be given via UserInfo interface.
				UserInfo ui = new LinuxUerInfo(pwd);
				session.setUserInfo(ui);
				session.connect();
			}
			catch(Exception ex2)
			{
				LogUtil.printLogErr("doSCPFrom connectTimeout #1 " + ex2);
				
				session = null;
				
				for (int i = 0; i < 5; i++) {
					//重试
					try {
						LogUtil.printLog("trying connect " + (i + 1) + " " + host);
						session = null;
						session = jsch.getSession(user, host, LinuxRemoteCommandUtil.GetSSHPort());
						// username and password will be given via UserInfo interface.
						UserInfo ui = new LinuxUerInfo(pwd);
						session.setUserInfo(ui);
						session.connect();
						LogUtil.printLog("doSCPFrom reconnect successfully");
						
						break;
					} catch (Exception er) {
						er.printStackTrace();
					}
				}
			}

			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			// String command = "scp " + (ptimestamp ? "-p" : "") + " -t " +
			// rfile;

			// exec 'scp -f rfile' remotely
			String command = "scp -f " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] buf = new byte[1024];

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			while (true) {
				int c = checkAck(in);
				if (c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while (true) {
					if (in.read(buf, 0, 1) < 0) {
						// error
						break;
					}
					if (buf[0] == ' ')
						break;
					filesize = filesize * 10L + (long) (buf[0] - '0');
				}

				String file = null;
				for (int i = 0;; i++) {
					in.read(buf, i, 1);
					if (buf[i] == (byte) 0x0a) {
						file = new String(buf, 0, i);
						break;
					}
				}

				// System.out.println("filesize="+filesize+", file="+file);

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();

				// read a content of lfile
				fos = new FileOutputStream(prefix == null ? lfile : prefix + file);
				int foo;
				while (true) {
					if (buf.length < filesize)
						foo = buf.length;
					else
						foo = (int) filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if (filesize == 0L)
						break;
				}
				fos.close();
				fos = null;

				if (checkAck(in) != 0) {
					System.err.println("Error:doSCPFrom|" + host + "|" + rfile + "|" + lfile);
					System.exit(0);
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
			}

			session.disconnect();

			// System.exit(0);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			try {
				if (fos != null)
					fos.close();
			} catch (Exception ee) {
			}
		}
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print("erroe == " + sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print("ERROR: " + sb.toString());
			}
		}
		return b;
	}

}
