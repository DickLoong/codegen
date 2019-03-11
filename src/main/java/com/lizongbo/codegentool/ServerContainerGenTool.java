package com.lizongbo.codegentool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

public class ServerContainerGenTool {

	public static String resinconftempdir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/resinconftemp/resin-4.0.46";
	public static String javaserverconftempdir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/javaserverconftemp";

	public static void main(String[] args) {
		// genResinServer("10.0.0.16", 8090, "10.0.0.16");
		// genResinServer("112.74.108.38", 8090, "10.116.33.153");
	}

	public static void unZipFile(String zipFile, String outputFolder) {

		byte[] buffer = new byte[1024 * 8];

		try {

			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();
				if (!ze.isDirectory()) {
					FileOutputStream fos = new FileOutputStream(newFile);

					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void zipDir(String outputFolder, String zipFile, String zipRootDirName) {
		System.out.println("zipDir|" + outputFolder + "|to|" + zipFile);
		try {
			compressDir(outputFolder, zipFile, zipRootDirName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void zipFile(String orgFilePath, String zipFile) {
		System.out.println("zipFile|" + orgFilePath + "|to|" + zipFile);
		try {
			File f = new File(orgFilePath);
			if (f.isDirectory()) {
				compressDir(orgFilePath, zipFile, f.getName());
			} else if (f.isFile()) {
				File targetFile = new File(zipFile); // 目的
				targetFile.getParentFile().mkdirs();

				FileOutputStream outputStream = new FileOutputStream(targetFile);
				ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));
				FileInputStream fis = new FileInputStream(f);

				out.putNextEntry(new ZipEntry(f.getName()));
				// 进行写操作
				int j = 0;
				byte[] buffer = new byte[1024 * 8];
				while ((j = fis.read(buffer)) > 0) {
					out.write(buffer, 0, j);
				}
				// 关闭输入流
				fis.close();
				out.close();

			} else {
				System.err.println(orgFilePath + " |notfound|error");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void compressFile(String resourcesPath, String targetPath) throws Exception {
		compressDir(resourcesPath, targetPath, "");
	}

	/**
	 * @desc 将源文件/文件夹生成指定格式的压缩文件,格式zip
	 * @param resourePath
	 *            源文件/文件夹
	 * @param targetPath
	 *            目的压缩文件保存路径
	 * @return void
	 * @throws Exception
	 */
	public static void compressDir(String resourcesPath, String targetPath, String zipRootDirName) throws Exception {
		File resourcesFile = new File(resourcesPath); // 源文件
		File targetFile = new File(targetPath); // 目的
		targetFile.getParentFile().mkdirs();

		FileOutputStream outputStream = new FileOutputStream(targetFile);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));
		if (zipRootDirName == null) {
			zipRootDirName = "";
		}
		createCompressedFile(out, resourcesFile, zipRootDirName);

		out.close();
	}

	/**
	 * @desc 生成压缩文件。 如果是文件夹，则使用递归，进行文件遍历、压缩 如果是文件，直接压缩
	 * @param out
	 *            输出流
	 * @param file
	 *            目标文件
	 * @return void
	 * @throws Exception
	 */
	private static void createCompressedFile(ZipOutputStream out, File file, String dir) throws Exception {
		// 如果当前的是文件夹，则进行进一步处理
		if (file.isDirectory()) {
			// 得到文件列表信息
			File[] files = file.listFiles();
			// 将文件夹添加到下一级打包目录
			out.putNextEntry(new ZipEntry(dir + "/"));

			dir = dir.length() == 0 ? "" : dir + "/";

			// 循环将文件夹中的文件打包
			for (int i = 0; i < files.length; i++) {
				createCompressedFile(out, files[i], dir + files[i].getName()); // 递归处理
			}
		} else { // 当前的是文件，打包处理
			// 文件输入流
			FileInputStream fis = new FileInputStream(file);

			out.putNextEntry(new ZipEntry(dir));
			// 进行写操作
			int j = 0;
			byte[] buffer = new byte[1024 * 8];
			while ((j = fis.read(buffer)) > 0) {
				out.write(buffer, 0, j);
			}
			// 关闭输入流
			fis.close();
		}
	}

	public static String replaceAll(String s, String src, String dest) {
		if (s == null || src == null || dest == null || src.length() == 0)
			return s;
		int pos = s.indexOf(src); // 查找第一个替换的位置
		if (pos < 0)
			return s;
		int capacity = dest.length() > src.length() ? s.length() * 2 : s.length();
		StringBuilder sb = new StringBuilder(capacity);
		int writen = 0;
		for (; pos >= 0;) {
			sb.append(s, writen, pos); // append 原字符串不需替换部分
			sb.append(dest); // append 新字符串
			writen = pos + src.length(); // 忽略原字符串需要替换部分
			pos = s.indexOf(src, writen); // 查找下一个替换位置
		}
		sb.append(s, writen, s.length()); // 替换剩下的原字符串
		return sb.toString();
	}

	public static boolean delAllFile4Need(String dirPath) {
		boolean bea = false;
		File file = new File(dirPath);
		if (!file.exists()) {
			return bea;
		}
		if (file.getName().endsWith(".4del")) {
			if (file.isFile()) {
				file.delete();
				File orgfile = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 5));
				System.out.println("delAllFile4Need|file=====" + orgfile);
				delAllFile(orgfile.getAbsolutePath());
				orgfile.delete();
			}
			if (file.isDirectory()) {
				delAllFile(file.getAbsolutePath());
				file.delete();
				File orgfile = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 5));
				System.out.println("delAllFile4Need|dir====" + orgfile);
				delAllFile(orgfile.getAbsolutePath());
				orgfile.delete();
			}
		}
		if (file.getName().equals(".svn") || file.getName().startsWith(".")) {
			delAllFile(file.getAbsolutePath());
		}
		if (file.isFile()) {
			return false;
		}

		File[] tempList = file.listFiles();
		for (int i = 0; tempList != null && i < tempList.length; i++) {
			delAllFile4Need(tempList[i].getAbsolutePath());
		}
		return bea;
	}

	public static boolean delAllFile(String path) {
		boolean bea = false;
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (file.isFile()) {
			// System.out.println("|delAllFile|for|" + file);
			file.delete();
			return true;
		}
		// System.out.println("delAllFile|TRY|" + path);
		File[] tempList = file.listFiles();
		for (int i = 0; tempList != null && i < tempList.length; i++) {
			delAllFile(tempList[i].getAbsolutePath());
			tempList[i].delete();
		}
		file.delete();
		return bea;
	}

	public static boolean copyFile(File src, File target) throws IOException {
		if ((src == null) || (target == null) || !src.exists()) {
			return false;
		}

		// System.out.println("try|copyFile|" + src + "|to|" + target);
		// System.out.println("copyFile|form|" + src.getAbsolutePath() + "|to|"
		// + target.getAbsolutePath());
		InputStream ins = new BufferedInputStream(new FileInputStream(src));
		OutputStream ops = new BufferedOutputStream(new FileOutputStream(target));
		int b;
		while (-1 != (b = ins.read())) {
			ops.write(b);
		}

		GenAll.safeClose(ins);
		GenAll.safeFlush(ops);
		GenAll.safeClose(ops);
		return target.setLastModified(src.lastModified());
	}

	/**
	 * 自动决定是 copy 文件还是目录
	 * 
	 * @param src
	 *            源
	 * @param target
	 *            目标
	 * @return 是否 copy 成功
	 */
	public static boolean copy(File src, File target) {
		try {
			if (src.isDirectory()) {
				return copyDir(src, target);
			}
			return copyFile(src, target);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 拷贝一个目录
	 * 
	 * @param src
	 *            原始目录
	 * @param target
	 *            新目录
	 * @return 是否拷贝成功
	 * @throws IOException
	 */
	public static boolean copyDir(File src, File target) throws IOException {
		if ((src == null) || (target == null) || !src.exists() || src.getName().endsWith(".svn")) {
			return false;
		}
		if (!src.isDirectory()) {
			throw new IOException(src.getAbsolutePath() + " should be a directory!");
		}
		// System.out.println("try|copyDir|" + src + "|to|" + target);
		if (!target.exists()) {
			if (!target.mkdirs()) {
				return false;
			}
		}
		boolean re = true;
		File[] files = src.listFiles();
		if (null != files) {
			for (File f : files) {
				if (f.isFile()) {
					re &= copyFile(f, new File(target.getAbsolutePath() + "/" + f.getName()));
				} else {
					re &= copyDir(f, new File(target.getAbsolutePath() + "/" + f.getName()));
				}
			}
		}
		return re;
	}

	public static void runShellCmd(String sendCommand) {
		try {
			Runtime rt = Runtime.getRuntime();
			String command = "/bin/bash " + sendCommand;
			if (new File("D:\\mgamedev").exists()) {
				command = sendCommand;
				if (sendCommand.startsWith("/")) {
					command = "D:" + sendCommand;
				}
				command = command.replaceAll("ant ", "ant.bat ");
			}
			LogUtil.printLog("try runShellCmd|" + command);
			Process child = rt.exec(command);
			OutputStream out = child.getOutputStream();

			OutputStreamWriter writer = null;
			try {
				writer = new OutputStreamWriter(child.getOutputStream(), "UTF-8");
				writer.write(command);
				writer.flush();
				out.close();
				InputStreamReader isr = new InputStreamReader(child.getInputStream(), "UTF-8");
				LineNumberReader input = new LineNumberReader(isr);
				String line = "";
				while ((line = input.readLine()) != null) {
					System.out.println("inputStream runShellCmd:" + line);
				}
				InputStreamReader isrError = new InputStreamReader(child.getErrorStream(), "UTF-8");
				LineNumberReader error = null;
				boolean hasError = false;
				try {
					error = new LineNumberReader(isrError);
					
					while ((line = error.readLine()) != null) {
						System.out.println("errorStream runShellCmd:" + line);
						hasError = true;
					}
					child.waitFor();
				} finally {
					if (null != error) {
						error.close();
					}
					
					if (hasError){
						LogUtil.printLog("runShellCmd ERROR");
						System.exit(1);
					}
				}
			} finally {
				if (null != writer) {
					writer.close();
				}
			}

		} catch (Exception e) {
			LogUtil.printLogErr(e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * 替换配置模板中的变量值
	 * @param frameSyncServerPort 不用则填0
	 * @param frameSyncServerHessianPort 不用则填0
	 * @param frameSyncJmxPort 不用则填0
	 */
	public static String replaceConfigVar(String configText, ServerDeployConfig deployConfig, String mainClassName,
			I18NUtil.SERVER_TYP serverType){
		
		int jmxPort = 0;
		int mx = 0;
		int selfPort = 0;
		int selfHessianPort = 0;
		
		int frameServer2HessianPort = deployConfig.frame_sync_server2_hessian_port;
		int frameServer3HessianPort = deployConfig.frame_sync_server3_hessian_port;
		int frameServer4HessianPort = deployConfig.frame_sync_server4_hessian_port;
		
		switch (serverType){
		case GAME_SERVER:
		case WORLD_SERVER:
			{
				jmxPort = deployConfig.game_server_jmx_port;
				mx = deployConfig.game_server_memory;
				selfPort = deployConfig.game_server_port;
				selfHessianPort = deployConfig.game_server_hessian_port;
				
				int frameServerCount = BilinGameWorldConfig.getFrameServerCountByZoneId(System.getenv("worldName"), deployConfig.zone_id);
				if (frameServerCount < 2)
				{
					frameServer2HessianPort = 0;
				}
				
				if (frameServerCount < 3)
				{
					frameServer3HessianPort = 0;
				}
				
				if (frameServerCount < 4)
				{
					frameServer4HessianPort = 0;
				}
			}
			break;
		case MAP_SERVER:
			{
				jmxPort = deployConfig.secne_map_server_jmx_port;
				mx = deployConfig.scene_map_server_memory;
				selfPort = deployConfig.scene_map_server_port;
				selfHessianPort = deployConfig.secne_map_server_hessian_port;
			}
			break;
		case WORLD_FRAME_SYNC_SERVER1:
		case FRAME_SYNC_SERVER1:
			{
				jmxPort = deployConfig.frame_sync_server1_jmx_port;
				selfPort = deployConfig.frame_sync_server1_port;
				selfHessianPort = deployConfig.frame_sync_server1_hessian_port;
				
				mx = deployConfig.frame_sync_server_memory;
			}
			break;
		case WORLD_FRAME_SYNC_SERVER2:
		case FRAME_SYNC_SERVER2:
			{
				jmxPort = deployConfig.frame_sync_server2_jmx_port;
				selfPort = deployConfig.frame_sync_server2_port;
				selfHessianPort = deployConfig.frame_sync_server2_hessian_port;
				
				mx = deployConfig.frame_sync_server_memory;
			}
			break;
		case WORLD_FRAME_SYNC_SERVER3:
		case FRAME_SYNC_SERVER3:
			{
				jmxPort = deployConfig.frame_sync_server3_jmx_port;
				selfPort = deployConfig.frame_sync_server3_port;
				selfHessianPort = deployConfig.frame_sync_server3_hessian_port;
				
				mx = deployConfig.frame_sync_server_memory;
			}
			break;
		case WORLD_FRAME_SYNC_SERVER4:
		case FRAME_SYNC_SERVER4:
			{
				jmxPort = deployConfig.frame_sync_server4_jmx_port;
				selfPort = deployConfig.frame_sync_server4_port;
				selfHessianPort = deployConfig.frame_sync_server4_hessian_port;
				
				mx = deployConfig.frame_sync_server_memory;
			}
			break;
		default:
			break;
		}
		
		//zoneid配置
		configText = ServerContainerGenTool.replaceAll(configText, "zoneid=4444", "zoneid=" + deployConfig.zone_id);
				
		//内存配置
		configText = ServerContainerGenTool.replaceAll(configText, "java_xmx=3000m", "java_xmx=" + mx + "m");
		
		//端口/IP配置
		//帧同步服务器端口
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server1_port=9999", "frame_sync_server1_port=" + deployConfig.frame_sync_server1_port);
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server_hessian1_port=9999", "frame_sync_server_hessian1_port=" + deployConfig.frame_sync_server1_hessian_port);
		
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server2_port=9999", "frame_sync_server2_port=" + deployConfig.frame_sync_server2_port);
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server_hessian2_port=9999", "frame_sync_server_hessian2_port=" + frameServer2HessianPort);
		
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server3_port=9999", "frame_sync_server3_port=" + deployConfig.frame_sync_server3_port);
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server_hessian3_port=9999", "frame_sync_server_hessian3_port=" + frameServer3HessianPort);
		
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server4_port=9999", "frame_sync_server4_port=" + deployConfig.frame_sync_server4_port);
		configText = ServerContainerGenTool.replaceAll(configText, "frame_sync_server_hessian4_port=9999", "frame_sync_server_hessian4_port=" + frameServer4HessianPort);
		
		
		configText = ServerContainerGenTool.replaceAll(configText, "scene_map_server=6666", "scene_map_server=" + deployConfig.server_public_ip);
		configText = ServerContainerGenTool.replaceAll(configText, "scene_map_port=6666", "scene_map_port=" + deployConfig.scene_map_server_port);
		configText = ServerContainerGenTool.replaceAll(configText, "scene_map_hessian_port=6666", "scene_map_hessian_port=" + deployConfig.secne_map_server_hessian_port);
		
		configText = ServerContainerGenTool.replaceAll(configText, "javaserver_mainip=127.0.0.1",
				"javaserver_mainip=" + deployConfig.server_inner_ip);
		configText = ServerContainerGenTool.replaceAll(configText, "javaserver_mainport=9000",
				"javaserver_mainport=" + selfPort);
		configText = ServerContainerGenTool.replaceAll(configText, "javaserver_hessian_port=8888", "javaserver_hessian_port=" + selfHessianPort);
		
		configText = ServerContainerGenTool.replaceAll(configText, "jmx_port=44445", "jmx_port=" + jmxPort);
		
		configText = ServerContainerGenTool.replaceAll(configText, "net.binlin.MainClass", mainClassName);
		
		configText = ServerContainerGenTool.replaceAll(configText, "internal_ip=127.0.0.1", "internal_ip=" + deployConfig.server_inner_ip);
		configText = ServerContainerGenTool.replaceAll(configText, "public_ip=10.0.0.10", "public_ip=" + deployConfig.server_public_ip);
		
		return configText;
	}
	
	/**
	 * 替换配置模板中的变量值,这里使用的参数和GameServer的是不一致的
	 * @param frameSyncServerPort 不用则填0
	 * @param frameSyncServerHessianPort 不用则填0
	 * @param frameSyncJmxPort 不用则填0
	 */
	public static String replaceSingleServerConfigVar(String worldName, ServerDeployConfig zoneConfig, String configText, String mainClassName,
			I18NUtil.SERVER_TYP serverType){
		
		int jmxPort = 0;
		int mx = 0;
		int selfPort = 0;
		
		String innerIP = null;
		String publicIP = null;
		
		switch (serverType){
		case COMMON_SERVER:
			{
				selfPort = zoneConfig.game_server_port;
				jmxPort = zoneConfig.game_server_jmx_port;
				mx = zoneConfig.game_server_memory;
				
				innerIP = zoneConfig.server_inner_ip;
				publicIP = zoneConfig.server_public_ip;
			}
			break;
		default:
			break;
		}
				
		//内存配置
		configText = ServerContainerGenTool.replaceAll(configText, "java_xmx=3000m", "java_xmx=" + mx + "m");
		
		//端口/IP配置
		configText = ServerContainerGenTool.replaceAll(configText, "javaserver_mainip=127.0.0.1",
				"javaserver_mainip=" + innerIP);
		configText = ServerContainerGenTool.replaceAll(configText, "javaserver_mainport=9000",
				"javaserver_mainport=" + selfPort);
		
		configText = ServerContainerGenTool.replaceAll(configText, "jmx_port=44445", "jmx_port=" + jmxPort);
		
		configText = ServerContainerGenTool.replaceAll(configText, "net.binlin.MainClass", mainClassName);
		
		configText = ServerContainerGenTool.replaceAll(configText, "internal_ip=127.0.0.1", "internal_ip=" + innerIP);
		configText = ServerContainerGenTool.replaceAll(configText, "public_ip=10.0.0.10", "public_ip=" + publicIP);
		
		return configText;
	}
	
}
