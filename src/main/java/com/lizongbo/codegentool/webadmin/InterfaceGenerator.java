package com.lizongbo.codegentool.webadmin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;

import com.lizongbo.codegentool.csv2db.CSVUtil;

public class InterfaceGenerator {
	public static final String metaCsvFilePath = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_adminconsole\\CodeGenConf"
			+ "\\InterfaceDetail\\DInterfaceMeta_Interfacemeta.csv";
	public static final String interfaceCsvFileOutputPath = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_adminconsole\\"
			+ "CodeGenConf\\InterfaceDetail";
	public static final String interfaceCodeOutPutpath = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_adminconsole\\"
			+ "src\\net\\bilinkeji\\gecaoshoulie\\";

	public static final String mainControllerPath = "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_adminconsole\\"
			+ "src\\net\\bilinkeji\\gecaoshoulie\\controller\\MainBusController.java";

	public static void interfaceMeta2Csv() throws Throwable {
		// 代码meta从第三行非#号开头数据开始
		List<String[]> metaData = CSVUtil.getDataFromCSV2WithoutCheck(metaCsvFilePath);
		File rootCsvFile = new File(interfaceCsvFileOutputPath);
		StringBuilder codeBuilder = new StringBuilder();
		Set<String> interfaceSet = new HashSet<>();
		codeBuilder.append("package net.bilinkeji.gecaoshoulie.controller;\n");
		codeBuilder.append("import org.springframework.web.servlet.ModelAndView;\n\n");
		codeBuilder.append("import org.springframework.stereotype.Controller;\n");
		codeBuilder.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
		codeBuilder.append("@Controller\n").append("public class MainBusController {\n");
		if (rootCsvFile.isDirectory()) {
			for (int startRow = 2; startRow < metaData.size(); startRow++) {
				// 注释行跳过
				if (metaData.get(startRow)[0].startsWith("#")) {
					continue;
				}
				String interfaceName = metaData.get(startRow)[0];
				String nameSpace = metaData.get(startRow)[1].toLowerCase();
				String creator = metaData.get(startRow)[2];
				String createTime = metaData.get(startRow)[3];
				String comment = metaData.get(startRow)[4];
				String returnType = metaData.get(startRow)[5];
				if (null == returnType || "".equals(returnType)) {
					returnType = "ModelAndView";
				}
				// TODO 先生成csv
				// TODO 先检查命名空间的目录是否已存在
				File nameSpaceDir = new File(rootCsvFile, nameSpace);
				if (!nameSpaceDir.exists()) {
					nameSpaceDir.mkdirs();
				}
				// TODO 往里面插入中间类的代码
				File preProcessObjectDir = new File(interfaceCodeOutPutpath, "requestobject");
				preProcessObjectDir = new File(preProcessObjectDir, nameSpace);
				if (!preProcessObjectDir.exists()) {
					preProcessObjectDir.mkdir();
				}
				File logicCodeDir = new File(interfaceCodeOutPutpath, "logic");
				if (!logicCodeDir.exists()) {
					logicCodeDir.mkdir();
				}
				logicCodeDir = new File(logicCodeDir, nameSpace);
				if (!logicCodeDir.exists()) {
					logicCodeDir.mkdir();
				}
				File interfaceCodeCSV = new File(nameSpaceDir, interfaceName + ".csv");
				if (!interfaceCodeCSV.exists()) {
					// TODO 如果不存在文件，添加上去
					FileOutputStream fos = new FileOutputStream(interfaceCodeCSV.getAbsolutePath());
					OutputStreamWriter isw = new OutputStreamWriter(fos, "UTF-8");
					try (org.apache.commons.csv.CSVPrinter csvp = new CSVPrinter(isw,
							org.apache.commons.csv.CSVFormat.EXCEL)) {
						// TODO 先插入表头
						List<String> records = new ArrayList<>();
						records.add("propName");
						records.add("propType");
						records.add("comment");
						csvp.printRecord(records);
						csvp.flush();
					}
				} else {
					// TODO 已经创建出csv了，尝试导出代码
					interfaceCsv2Code(interfaceCodeCSV, nameSpace, interfaceName, creator, createTime, comment,
							returnType, preProcessObjectDir, logicCodeDir);
				}
				String interfaceRequestObjectClassName = nameSpace + interfaceName + "RequestObject ";
				if(interfaceSet.contains(interfaceRequestObjectClassName)){
					System.out.println(interfaceCodeCSV.getAbsolutePath() + "has duplicated interface" + interfaceRequestObjectClassName);
					return ;
				}else{
					interfaceSet.add(interfaceRequestObjectClassName);
				}
				// TODO 在主controller下面加入一一段映射代码
				codeBuilder.append("	@RequestMapping(value=\"/").append(nameSpace).append("/").append(interfaceName)
						.append("\")\n");
				codeBuilder.append("	public ").append(returnType).append(" ").append(nameSpace).append(interfaceName)
						.append("(\n			"
								+ "net.bilinkeji.gecaoshoulie.requestobject.").append(nameSpace.toLowerCase()).append(".")
						.append(interfaceRequestObjectClassName).append(" requestObject,"
								+ "\n			ModelAndView modelAndView")
						.append("){\n").append("			return ").append("net.bilinkeji.gecaoshoulie.logic.")
						.append(nameSpace.toLowerCase()).append(".").append(nameSpace).append(interfaceName)
						.append("Logic.process(requestObject,modelAndView);\n").append("			}\n");
			}
		}
		codeBuilder.append("\n}");
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(mainControllerPath), "UTF-8");) {
			writer.write(codeBuilder.toString());
		} catch (Throwable th) {
			th.getStackTrace();
		}
	}

	public static void interfaceCsv2Code(File interfaceCodeCSV, String nameSpace, String interfaceName, String creator,
			String createTime, String comment, String returnType, File preProcessObjectDir, File logicCodeDir) {
		// TODO 先生成中间类的代码
		interfaceCsv2RequestObject(interfaceCodeCSV, nameSpace, interfaceName, creator, createTime, comment, returnType,
				preProcessObjectDir);
		// TODO 再生成logic的代码
		interfaceCsv2Logic(interfaceCodeCSV, nameSpace, interfaceName, creator, createTime, comment, returnType,
				logicCodeDir);
		//TODO 后面要加生成页面的代码
	}

	public static void interfaceCsv2RequestObject(File interfaceCodeCSV, String nameSpace, String interfaceName,
			String creator, String createTime, String comment, String returnType, File preProcessObjectDir) {
		// TODO 读取类的属性
		List<String[]> datas = CSVUtil.getDataFromCSV2WithoutCheck(interfaceCodeCSV.getAbsolutePath());
		StringBuilder codeBuilder = new StringBuilder();
		// 声明包
		codeBuilder.append("package net.bilinkeji.gecaoshoulie.requestobject.").append(nameSpace.toLowerCase())
				.append(";\n");
		codeBuilder.append("/*\n");
		codeBuilder.append("created by:").append(creator).append("\n");
		codeBuilder.append("create time:").append(createTime).append("\n");
		codeBuilder.append("comment:").append(comment).append("\n");
		codeBuilder.append("*/\n");
		codeBuilder.append("public class ").append(nameSpace).append(interfaceName).append("RequestObject\n");
		// TODO 声明方法
		codeBuilder.append("{\n");
		Set<String> propNameSet = new HashSet<>();
		for (int startRow = 1; startRow < datas.size(); startRow++) {
			if (datas.get(startRow)[0].startsWith("#")) {
				continue;
			}
			String javaPropName = datas.get(startRow)[0];
			if(propNameSet.contains(javaPropName)){
				System.out.println(interfaceCodeCSV.getAbsolutePath() + "has duplicated props" + javaPropName);
				return ;
			}else{
				propNameSet.add(javaPropName);
			}
			String propType = datas.get(startRow)[1];
			String propName = Character.toUpperCase(javaPropName.charAt(0)) + javaPropName.substring(1);
			String propComment = datas.get(startRow)[2];
			if (null != propComment && !"".equals(propComment)) {
				codeBuilder.append("	/**\n		").append(propComment).append("\n	**/\n");
			}
			codeBuilder.append("	private ").append(propType).append(" ").append(javaPropName).append(";\n\n");
			// TODO 生成setter
			codeBuilder.append("	public void set").append(propName).append("(").append(propType).append(" ")
					.append(javaPropName).append(")").append("{\n");
			codeBuilder.append("		this.").append(javaPropName).append(" = ").append(javaPropName).append(";\n");
			codeBuilder.append("	}\n\n");
			// TODO 生成getter
			codeBuilder.append("	public ").append(propType).append(" get").append(propName).append("()")
					.append("{\n");
			codeBuilder.append("		return this.").append(javaPropName).append(";\n");
			codeBuilder.append("	}\n\n");
		}
		codeBuilder.append("}");
		File requestObjectFile = new File(preProcessObjectDir, nameSpace + interfaceName + "RequestObject.java");
		try (Writer writer = new FileWriter(requestObjectFile)) {
			writer.write(codeBuilder.toString());
		} catch (Throwable th) {
			th.getStackTrace();
		}
	}

	public static void interfaceCsv2Logic(File interfaceCodeCSV, String nameSpace, String interfaceName, String creator,
			String createTime, String comment, String returnType, File logicCodeDir) {
		// TODO 读取类的属性
		StringBuilder codeBuilder = new StringBuilder();
		// 声明包
		codeBuilder.append("package net.bilinkeji.gecaoshoulie.logic.").append(nameSpace.toLowerCase()).append(";\n");
		codeBuilder.append("import org.springframework.web.servlet.ModelAndView;\n\n");
		String interfaceRequestObjectClassName = nameSpace + interfaceName + "RequestObject ";
		codeBuilder.append("import net.bilinkeji.gecaoshoulie.requestobject.").append(nameSpace).append(".")
				.append(interfaceRequestObjectClassName).append(";\n\n");
		codeBuilder.append("public class ").append(nameSpace).append(interfaceName).append("Logic\n");
		// TODO 声明方法
		codeBuilder.append("{\n");
		codeBuilder.append("	public static ").append(returnType).append(" process(")
				.append(interfaceRequestObjectClassName).append("requestObject,ModelAndView modelAndView){\n");
		codeBuilder.append("		try{\n\n		}catch(Throwable th){\n					\n		}");
		switch (returnType) {
		case "int":
			codeBuilder.append("return 0;\n");
			break;
		case "long":
			codeBuilder.append("return 0;\n");
			break;
		case "double":
			codeBuilder.append("return 0;\n");
			break;
		case "float":
			codeBuilder.append("return 0;\n");
			break;
		default:
			codeBuilder.append("return null;\n");
			break;
		}
		codeBuilder.append("\n	}");
		codeBuilder.append("\n}");
		File requestObjectFile = new File(logicCodeDir, nameSpace + interfaceName + "Logic.java");
		if(!requestObjectFile.exists()){
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(requestObjectFile.getAbsolutePath()),
					"UTF-8");) {
				writer.write(codeBuilder.toString());
			} catch (Throwable th) {
				th.getStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Throwable {
		interfaceMeta2Csv();
	}

}
