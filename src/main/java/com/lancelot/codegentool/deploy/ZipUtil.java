package com.lancelot.codegentool.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
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

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("usage: java ScpTo file1 user@remotehost:file2");
			System.exit(-1);
		}
		String sourceFilePath = args[0];
		String zipFilePath = args[1];
		String fileName = args[2];
		ZipUtil.Zip(sourceFilePath, zipFilePath, fileName);
	}
}
