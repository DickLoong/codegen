package com.lizongbo.codegentool.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class LoggerWrapperFormatter {

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, SQLException {
		try (Scanner inputScanner = new Scanner(System.in);) {
			String bilinSqlCommand = inputScanner.nextLine();
			while (!"exit".equals(bilinSqlCommand)) {
				if (StringUtils.isEmpty(bilinSqlCommand)) {
					continue;
				}
				try {
					Map<String, Object> context = parseContext(bilinSqlCommand);
					String rootWorkSpaceFileName = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_server\\WEB-INF\\protojavarpcimpls\\net\\bilinkeji\\gecaoshoulie\\mgameprotorpc\\logics";// (String)
					                                                                                                                                                                                  // context.get("javaCodeFileName");
					File root = new File(rootWorkSpaceFileName);
					for (File moduleDir : root.listFiles()) {
						if (moduleDir.isFile() || moduleDir.getName().contains("common") || moduleDir.getName().contains("crud")
						        || moduleDir.getName().contains("main") || moduleDir.getName().contains("userinfo")
						        || moduleDir.getName().contains("iap")) {
							continue;
						}
						String moduleName = moduleDir.getName();
						String targetSqlWorkSpaceFileName = moduleDir.getAbsolutePath();
						scanFile(targetSqlWorkSpaceFileName, "UTF-8", moduleName);
					}
				} catch (Throwable th) {
					th.printStackTrace();
				}
				System.out.println("------------------");
				bilinSqlCommand = inputScanner.nextLine();
			}
		}
	}

	public static void scanFile(String path, String encoding, String moduleName) throws IOException {
		File file = new File(path);
		if (file.isFile()) {
			String formattedJavaCode = read(path, encoding, moduleName);
			write(path, "UTF-8", formattedJavaCode);
			// System.out.println(formattedJavaCode);
		} else {
			for (File childFile : file.listFiles()) {
				scanFile(childFile.getAbsolutePath(), encoding, moduleName);
			}
		}
	}

	public static String read(String path, String encoding, String moduleName) throws IOException {
		String content = "";
		File file = new File(path);
		String fileName = file.getName().replace(".java", "");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String line = null;
		while ((line = reader.readLine()) != null) {
			// TODO 判断是不是声明端，如果是，换模块名
			if (line.contains("LoggerWraper.getLogger(")) {
				if (!line.contains(moduleName)) {
					String loggerDeclare = "LoggerWraper.getLogger(\"{moduleName}\");////".replace("{moduleName}", moduleName);
					line = line.replace("LoggerWraper.getLogger(", loggerDeclare);
				}
			}
			// TODO 判断是不是代码段，如果是，添加类名
			else if (fileName.contains("Logic")) {
				;
			} else {
				if (line.contains(".info(")) {
					if (!line.contains(fileName)) {
						line = line.replace(".info(", ".info( \"{fileName}|\" + ".replace("{fileName}", fileName));
					}
				} else if (line.contains(".error(")) {
					if (!line.contains(fileName)) {
						line = line.replace(".error(", ".error( \"{fileName}|\" + ".replace("{fileName}", fileName));
					}
				} else if (line.contains(".debug(")) {
					if (!line.contains(fileName)) {
						line = line.replace(".debug(", ".debug( \"{fileName}|\" + ".replace("{fileName}", fileName));
					}
				} else if (line.contains(".warn(")) {
					if (!line.contains(fileName)) {
						line = line.replace(".warn(", ".warn( \"{fileName}|\" + ".replace("{fileName}", fileName));
					}
				}
			}
			content += line + "\r\n";
		}
		reader.close();
		return content;
	}

	public static void write(String path, String encoding, String content) throws IOException {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false), encoding));
		writer.write(content);
		writer.flush();
		writer.close();
	}

	public static Map<String, Object> parseContext(String bilinSqlCommand) {
		Map<String, Object> hashMap = new HashMap<>();
		String[] keyValuePairs = StringUtils.split(bilinSqlCommand, "|");
		for (String keyValuePairString : keyValuePairs) {
			String[] keValuePair = StringUtils.split(keyValuePairString, "=");
			String key = keValuePair[0];
			String value = keValuePair[1];
			hashMap.put(key, value);
		}
		return hashMap;
	}

}
