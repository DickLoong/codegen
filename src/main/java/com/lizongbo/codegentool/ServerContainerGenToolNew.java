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
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.tools.GameZoneDbImport;

public class ServerContainerGenToolNew {

	private static String resinSetupZipedFilePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/serversofts/resin-4.0.46.zip";

	private static String resinconftempdir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/resinconftemp/resin-4.0.46";
	private static String javaserverconftempdir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/javaserverconftemp";
	private static String localAppDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_server";
	private static String linuxUserName = "root";
	private static String linuxUserPwd = "quick10343QQ";
	private static String jmxBindIp = "";

	public static void main(String[] args) {
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
	private static void compressDir(String resourcesPath, String targetPath, String zipRootDirName) throws Exception {
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
			System.err.println("try runShellCmd|" + command);
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
				try {
					error = new LineNumberReader(isrError);
					while ((line = error.readLine()) != null) {
						System.out.println("errorStream runShellCmd:" + line);
					}
					child.waitFor();
				} finally {
					if (null != error) {
						error.close();
					}
				}
			} finally {
				if (null != writer) {
					writer.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
