package com.lizongbo.codegentool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.lizongbo.codegentool.csv2db.CSVUtil;

public class GarbageCodeCleaner {

	private static Set<String> excludeSet;

	static {
		excludeSet = new HashSet<>();
		excludeSet.add("ArenareportResultArenaProtoBufResponse");
		excludeSet.add("ArmyGroupDoContributionProtoBufResponse");
		excludeSet.add("ArmyRoastSubmitAnswerProtoBufResponse");
		excludeSet.add("ConstellationGetSpecifyConstellationInfoProtoBufResponse");
		excludeSet.add("DestroyRebelFightReportProtoBufResponse");
		excludeSet.add("EquipSlotStarLevelUpBatchProtoBufResponse");
		excludeSet.add("ExpeditionFightReportProtoBufResponse");
		excludeSet.add("ImBigBossStartMatchProtoBufResponse");
		excludeSet.add("StarCraftPickUpResProtoBufResponse");

	}

	private static ArrayList<String> filelist = new ArrayList<String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// clearUselessErrInfoInJavaCode();
		clearUselessErrInfoInCsv();
	}

	private static void clearUselessErrInfoInJavaCode() {
		String logicFileDir = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_server\\WEB-INF\\protojavarpcimpls\\net\\bilinkeji\\gecaoshoulie\\"
				+ "mgameprotorpc\\logics";
		getFiles(logicFileDir);
	}

	/*
	 * 通过递归得到某一路径下所有的目录及其文件
	 */
	static void getFiles(String filePath) {
		File root = new File(filePath);
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				/*
				 * 递归调用
				 */
				getFiles(file.getAbsolutePath());
				filelist.add(file.getAbsolutePath());
				System.out.println("显示" + filePath + "下所有子目录及其文件" + file.getAbsolutePath());
			} else {
				System.out.println("显示" + filePath + "下所有子目录" + file.getAbsolutePath());
				BufferedReader reader = null;
				BufferedWriter writer = null;
				try {
					System.out.println("以行为单位读取文件内容，一次读一整行：");
					reader = new BufferedReader(new FileReader(file));
					StringBuilder filterFileContent = new StringBuilder();
					String tempString = null;
					int line = 1;
					// 一次读入一行，直到读入null为文件结束
					while ((tempString = reader.readLine()) != null) {
						// 显示行号
						System.out.println("line " + line + ": " + tempString);
						if (!tempString.contains(
								"net.bilinkeji.gecaoshoulie.mgameproto.common.CommErrorInfo errInfo = req.getErrInfo();")) {
							filterFileContent.append(tempString).append("\r\n");
						} else {
							filterFileContent.append("\r\n");
						}
						line++;
					}
					reader.close();
					writer = new BufferedWriter(new FileWriter(file.getAbsolutePath(), false));
					writer.write(filterFileContent.toString());
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							reader.close();
							writer.close();
						} catch (IOException e1) {
						}
					}
				}

			}
		}
	}

	private static void clearUselessErrInfoInCsv() {
		String logicFileDir = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\csvfiles\\ProtobufFiles\\TprotoCmds";
		getCsvFiles(logicFileDir);
	}

	/*
	 * 通过递归得到某一路径下所有的目录及其文件
	 */
	static void getCsvFiles(String filePath) {
		File root = new File(filePath);
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				/*
				 * 递归调用
				 */
				getCsvFiles(file.getAbsolutePath());
				filelist.add(file.getAbsolutePath());
				System.out.println("显示" + filePath + "下所有子目录及其文件" + file.getAbsolutePath());
			} else {
				if (excludeSet.stream().allMatch(e -> !file.getName().contains(e))) {
					List<String[]> rawData = CSVUtil.getDataFromCSV2(file.getAbsolutePath());
					List<String[]> filteredData = new ArrayList<>();
					for (String[] data : rawData) {
						boolean isFiltered = false;
						for (String singleElement : data) {
							if ("net.bilinkeji.gecaoshoulie.mgameproto.common.CommErrorInfo".equals(singleElement)) {
								isFiltered = true;
								break;
							}
						}
						if (!isFiltered) {
							filteredData.add(data);
						}
					}
					// 这里显式地配置一下CSV文件的Header，然后设置跳过Header（要不然读的时候会把头也当成一条记录）
					CSVFormat format = org.apache.commons.csv.CSVFormat.EXCEL;

					// 这是写入CSV的代码
					try (java.io.FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
							java.io.OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
							CSVPrinter printer = new CSVPrinter(out, format);) {
						for (String[] data : filteredData) {
							List<String> records = new ArrayList<>();
							for (String singleElement : data) {
								records.add(singleElement);
							}
							printer.printRecord(records);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
