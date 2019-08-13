package com.lancelot.codegentool.deploy;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	private ZipUtil() {
	}

	/**
	 * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
	 * 
	 * @param sourceFilePath
	 *            :待压缩的文件路径
	 * @param zipFilePath
	 *            :压缩后存放路径
	 * @param fileName
	 *            :压缩后文件的名称
	 * @param prefix 
	 * @return
	 */
	public static void Zip(String sourceFilePath, String zipFilePath, String fileName) {
		zipMultiFile(sourceFilePath, zipFilePath + File.separator + fileName + ".zip", true);
	}

	/**
	 * 压缩整个文件夹中的所有文件，生成指定名称的zip压缩包
	 * 
	 * @param filepath
	 *            文件所在目录
	 * @param zippath
	 *            压缩后zip文件名称
	 * @param dirFlag
	 *            zip文件中第一层是否包含一级目录，true包含；false没有 2015年6月9日
	 */
	public static void zipMultiFile(String filepath, String zippath,boolean dirFlag) {
		try {
			File file = new File(filepath);// 要被压缩的文件夹
			File zipFile = new File(zippath);
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File fileSec : files) {
					if (dirFlag) {
						recursionZip(zipOut, fileSec, file.getName() + File.separator);
					} else {
						recursionZip(zipOut, fileSec, "");
					}
				}
			}
			zipOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void recursionZip(ZipOutputStream zipOut, File file, String baseDir) throws Exception {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File fileSec : files) {
				recursionZip(zipOut, fileSec, baseDir + file.getName() + File.separator);
			}
		} else {
			byte[] buf = new byte[1024];
			InputStream input = new FileInputStream(file);
			String entryPath =  baseDir + file.getName();
			entryPath = entryPath.replace("\\", "/");
			zipOut.putNextEntry(new ZipEntry(entryPath));
			int len;
			while ((len = input.read(buf)) != -1) {
				zipOut.write(buf, 0, len);
			}
			input.close();
		}
	}

	/**
	 *  此方法将默认设置解压缩后文件的保存路径为zip文件所在路径
	 *      即解压缩到当前文件夹下
	 * @param zip zip文件位置
	 * @param charsetName 字符编码
	 */
	public static void unpack(String zip, String charsetName) {
		unpack(new File(zip), charsetName);
	}

	/**
	 *
	 * @param zip zip文件位置
	 * @param outputDir 解压缩后文件保存路径
	 * @param charsetName 字符编码
	 */
	public static void unpack(String zip, String outputDir, String charsetName) {
		unpack(new File(zip), new File(outputDir), charsetName);
	}

	/**
	 *  此方法将默认设置解压缩后文件的保存路径为zip文件所在路径
	 *      即解压缩到当前文件夹
	 * @param zip zip文件位置
	 * @param charsetName 字符编码
	 */
	public static void unpack(File zip, String charsetName) {
		unpack(zip, null, charsetName);
	}

	/**
	 *
	 * @param zip zip文件位置
	 * @param outputDir 解压缩后文件保存路径
	 */
	public static void unpack(File zip, File outputDir) {
		unpack(zip, outputDir, "");
	}

	/**
	 *
	 * @param zip zip文件位置
	 * @param outputDir 解压缩后文件保存路径
	 * @param charsetName 字符编码
	 */
	public static void unpack(File zip, File outputDir, String charsetName) {

		FileOutputStream out = null;
		InputStream in = null;
		//读出文件数据
		ZipFile zipFileData = null;
		ZipFile zipFile = null;
		try {
			//若目标保存文件位置不存在
			if (outputDir != null) {
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}
			}
			if (charsetName != null && charsetName != "") {
				zipFile = new ZipFile(zip.getPath(), Charset.forName(charsetName));
			} else {
				zipFile = new ZipFile(zip.getPath(), Charset.forName("utf8"));
			}
			//zipFile = new ZipFile(zip.getPath(), Charset.forName(charsetName));
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			//处理创建文件夹
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String filePath = "";

				if (outputDir == null) {
					filePath = zip.getParentFile().getPath() + File.separator + entry.getName();
				} else {
					filePath = outputDir.getPath() + File.separator + entry.getName();
				}
				File file = new File(filePath);
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				if (parentFile.isDirectory()) {
					continue;
				}
			}
			if (charsetName != null && charsetName != "") {
				zipFileData = new ZipFile(zip.getPath(), Charset.forName(charsetName));
			} else {
				zipFileData = new ZipFile(zip.getPath(), Charset.forName("utf8"));
			}
			Enumeration<? extends ZipEntry> entriesData = zipFileData.entries();
			while (entriesData.hasMoreElements()) {
				ZipEntry entry = entriesData.nextElement();
				in = zipFile.getInputStream(entry);
				String filePath = "";
				if (outputDir == null) {
					filePath = zip.getParentFile().getPath() + File.separator + entry.getName();
				} else {
					filePath = outputDir.getPath() + File.separator + entry.getName();
				}
				File file = new File(filePath);
				if (file.isDirectory()) {
					continue;
				}
				out = new FileOutputStream(filePath);
				int len = -1;
				byte[] bytes = new byte[1024];
				while ((len = in.read(bytes)) != -1) {
					out.write(bytes, 0, len);
				}
				out.flush();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
				zipFile.close();
				zipFileData.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
