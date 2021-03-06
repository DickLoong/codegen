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


public class InterfaceGeneratorV3 {
	public static final String metaCsvFilePath = "D:\\workspace\\XRGameOperateConsole\\CodeGenConf"
			+ "\\InterfaceDetail\\DInterfaceMeta_Interfacemeta.csv";
	public static final String interfaceCsvFileOutputPath = "D:\\workspace\\XRGameOperateConsole\\"
			+ "CodeGenConf\\InterfaceDetail";
	public static final String interfaceCsvFileGameServerHessianServiceOutputPath = "D:\\workspace\\XRGameServer\\basesrc";
	public static final String interfaceCsvFileGameServerRequestObjectOutputPath = "D:\\workspace\\XRGameServer\\basesrc";
	public static final String interfaceCodeOutPutpath = "D:\\workspace\\XRGameOperateConsole\\"
			+ "src";

	public static final String mainControllerPath = "D:\\workspace\\XRGameOperateConsole\\"
			+ "src\\controller\\MainBusController.java";

	public static void interfaceMeta2Csv() throws Throwable {
		// 代码meta从第三行非#号开头数据开始
		List<String[]> metaData = CSVUtil.getDataFromCSV2WithoutCheck(metaCsvFilePath);
		File rootCsvFile = new File(interfaceCsvFileOutputPath);
		StringBuilder codeBuilder = new StringBuilder();
		Set<String> interfaceSet = new HashSet<>();
		codeBuilder.append("package controller;\n");
		codeBuilder.append("import org.springframework.web.servlet.ModelAndView;\n\n");
		codeBuilder.append("import org.springframework.stereotype.Controller;\n");
		codeBuilder.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
		codeBuilder.append("import javax.annotation.Resource;\n");
		codeBuilder.append("import utils.WebUtil;\n");
		codeBuilder.append("import org.springframework.web.bind.annotation.ResponseBody;\n");
		codeBuilder.append("import org.springframework.web.bind.annotation.RequestBody;\n");
		codeBuilder.append("@Controller\n").append("public class MainBusController {\n");
		if (rootCsvFile.isDirectory()) {
			for (int startRow = 2; startRow < metaData.size(); startRow++) {
				// 注释行跳过
				if (metaData.get(startRow)[0].startsWith("#")) {
					continue;
				}
				String interfaceName = metaData.get(startRow)[0];
				String nameSpace = metaData.get(startRow)[1];
				String creator = metaData.get(startRow)[2];
				String createTime = metaData.get(startRow)[3];
				String comment = metaData.get(startRow)[4];
				String returnType = metaData.get(startRow)[5];
				String isCreateHessianService = metaData.get(startRow)[6];
				String hessianServiceReturnType = metaData.get(startRow)[7];
				String isAJAXInterface = metaData.get(startRow)[8];
				String isJsonInputSupport = metaData.get(startRow)[9];
				if (null == returnType || "".equals(returnType)) {
					returnType = "ModelAndView";
				}
				// 先生成csv
				// 先检查命名空间的目录是否已存在
				File nameSpaceDir = new File(rootCsvFile, nameSpace);
				if (!nameSpaceDir.exists()) {
					nameSpaceDir.mkdirs();
				}
				// 往里面插入中间类的代码
				File preProcessObjectDir = new File(interfaceCodeOutPutpath, "requestobject");
				if(!preProcessObjectDir.exists()){
					preProcessObjectDir.mkdir();
				}
				preProcessObjectDir = new File(preProcessObjectDir, nameSpace);
				if (!preProcessObjectDir.exists()) {
					preProcessObjectDir.mkdir();
				}

				File gamegameserverRequestObjectDir = new File(interfaceCsvFileGameServerRequestObjectOutputPath,
						"requestobject");
				if(!gamegameserverRequestObjectDir.exists()){
					gamegameserverRequestObjectDir.mkdir();
				}
				gamegameserverRequestObjectDir = new File(gamegameserverRequestObjectDir, nameSpace);
				if (!gamegameserverRequestObjectDir.exists()) {
					gamegameserverRequestObjectDir.mkdir();
				}

				File logicCodeDir = new File(interfaceCodeOutPutpath, "logic");
				if (!logicCodeDir.exists()) {
					logicCodeDir.mkdir();
				}
				logicCodeDir = new File(logicCodeDir, nameSpace);
				if (!logicCodeDir.exists()) {
					logicCodeDir.mkdir();
				}
				// package
				// hessian.impl;
				File gameServerProcessorCodeDir = new File(interfaceCodeOutPutpath,  "hessian");
				if (!gameServerProcessorCodeDir.exists()) {
					gameServerProcessorCodeDir.mkdir();
				}
				gameServerProcessorCodeDir = new File(gameServerProcessorCodeDir, "service");
				if (!gameServerProcessorCodeDir.exists()) {
					gameServerProcessorCodeDir.mkdir();
				} else {
					gameServerProcessorCodeDir.delete();
					gameServerProcessorCodeDir.mkdir();
				}
				// 生成到gameserver hessian service目录下的java文件
				File gameserverHessianServerCodeDir = new File(interfaceCsvFileGameServerHessianServiceOutputPath,
						 "hessian");
				if (!gameserverHessianServerCodeDir.exists()) {
					gameserverHessianServerCodeDir.mkdir();
				}
				gameserverHessianServerCodeDir = new File(gameserverHessianServerCodeDir, "service");
				if (!gameserverHessianServerCodeDir.exists()) {
					gameserverHessianServerCodeDir.mkdir();
				}

				File interfaceCodeCSV = new File(nameSpaceDir, interfaceName + ".csv");
				if (!interfaceCodeCSV.exists()) {
					// 如果不存在文件，添加上去
					FileOutputStream fos = new FileOutputStream(interfaceCodeCSV.getAbsolutePath());
					OutputStreamWriter isw = new OutputStreamWriter(fos, "UTF-8");
					try (org.apache.commons.csv.CSVPrinter csvp = new CSVPrinter(isw,
							org.apache.commons.csv.CSVFormat.EXCEL)) {
						// 先插入表头
						List<String> records = new ArrayList<>();
						records.add("propName");
						records.add("propType");
						records.add("comment");
						csvp.printRecord(records);
						csvp.flush();
					}
				} else {
					// 已经创建出csv了，尝试导出代码
					interfaceCsv2Code(interfaceCodeCSV, nameSpace, interfaceName, creator, createTime, comment,
							returnType, preProcessObjectDir, logicCodeDir, gameServerProcessorCodeDir,
							isCreateHessianService, hessianServiceReturnType, gamegameserverRequestObjectDir,
							gameserverHessianServerCodeDir);
				}
				String interfaceRequestObjectClassName = nameSpace + interfaceName + "RequestObject ";
				if (interfaceSet.contains(interfaceRequestObjectClassName)) {
					System.out.println(interfaceCodeCSV.getAbsolutePath() + "has duplicated interface"
							+ interfaceRequestObjectClassName);
					return;
				} else {
					interfaceSet.add(interfaceRequestObjectClassName);
				}
				// 对主controller注入各个logic
				codeBuilder.append("\n@Resource\n").append("private logic.")
						.append(nameSpace).append(".").append(nameSpace).append(interfaceName).append("Logic")
						.append(" ").append(nameSpace).append(interfaceName).append("Logic").append(";\n");
				// 在主controller下面加入一一段映射代码
				codeBuilder.append("	@RequestMapping(value=\"/").append(nameSpace).append("/").append(interfaceName)
						.append("\")\n");
				//判断是不是json
				if("1".equals(isAJAXInterface)){
					codeBuilder.append("	@ResponseBody\n");
				}
				String returnStr = returnType.contains("void")?" ":"return";
				String requestBodyAnnotation = "1".equals(isJsonInputSupport) ? "@RequestBody" : "";
				codeBuilder.append("	public ").append(returnType).append(" ").append(nameSpace).append(interfaceName)
						.append("(\n			" + requestBodyAnnotation + " requestobject.")
						.append(nameSpace).append(".").append(interfaceRequestObjectClassName)
						.append(" requestObject," + "\n			ModelAndView modelAndView").append("){\n")
						.append("String webColour = WebUtil.getRequestColour();requestObject.setWebColour(webColour);")
						.append("			").append(returnStr).append(" ").append("logic.")
						.append(nameSpace).append(".").append(nameSpace).append(interfaceName)
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
			String createTime, String comment, String returnType, File preProcessObjectDir, File logicCodeDir,
			File gameServerProcessorCodeDir, String isCreateHessianService, String hessianServiceReturnType,
			File gameServerRequestObjectDir, File gameServerHessianServiceDir) {
		// 先生成中间类的代码
		interfaceCsv2RequestObject(interfaceCodeCSV, nameSpace, interfaceName, creator, createTime, comment, returnType,
				preProcessObjectDir, gameServerRequestObjectDir);
		// 再生成logic的代码
		interfaceCsv2Logic(interfaceCodeCSV, nameSpace, interfaceName, creator, createTime, comment, returnType,
				logicCodeDir);
		// 生成每个request在远程服务器上运行的调度代码
		if (Integer.parseInt(isCreateHessianService) > 0) {
			interfaceCsv2GameServerProcessor(nameSpace, interfaceName, creator, createTime, comment,
					hessianServiceReturnType, gameServerProcessorCodeDir, gameServerHessianServiceDir);
		}
		// TODO 生成总的调度manager代码
		// TODO 后面要加生成页面的代码
	}

	private static void interfaceCsv2GameServerProcessor(String nameSpace, String interfaceName, String creator,
			String createTime, String comment, String hessianServiceReturnType, File gameServerProcessorCodeDir,
			File gameServerHessianServiceDir) {
		StringBuilder codeBuilder = new StringBuilder();
		// 声明包
		codeBuilder.append("package hessian.service").append(";").append("\n");
		codeBuilder.append("import hessian.components.BaseHessianService;\n");
		codeBuilder.append("/*\n");
		codeBuilder.append("created by:").append(creator).append("\n");
		codeBuilder.append("create time:").append(createTime).append("\n");
		codeBuilder.append("comment:").append(comment).append("\n");
		codeBuilder.append("warning:").append("tool-gencode,please do not edit it.").append("\n");
		codeBuilder.append("*/\n");
		codeBuilder.append("public interface I").append(nameSpace).append(interfaceName)
				.append("GameServerHessianService extends  BaseHessianService \n");
		// 声明方法
		codeBuilder.append("{\n");
		String returnType = "boolean";
		if (!"".equals(hessianServiceReturnType)) {
			returnType = hessianServiceReturnType;
		}
		codeBuilder.append("              public ").append(returnType).append(" dealRemoteCommandRequest(")
				.append("requestobject.").append(nameSpace).append(".")
				.append(nameSpace).append(interfaceName).append("RequestObject requestObject);\n");
		codeBuilder.append("}\n");
		File requestObjectFile = new File(gameServerProcessorCodeDir,
				"I" + nameSpace + interfaceName + "GameServerHessianService.java");
		try (Writer writer = new FileWriter(requestObjectFile)) {
			writer.write(codeBuilder.toString());
		} catch (Throwable th) {
			th.getStackTrace();
		}
		
		File gameServerRequestObjectFile = new File(gameServerHessianServiceDir,
				"I" + nameSpace + interfaceName + "GameServerHessianService.java");
		try (Writer writer = new FileWriter(gameServerRequestObjectFile)) {
			writer.write(codeBuilder.toString());
		} catch (Throwable th) {
			th.getStackTrace();
		}
	}

	public static void interfaceCsv2RequestObject(File interfaceCodeCSV, String nameSpace, String interfaceName,
			String creator, String createTime, String comment, String returnType, File preProcessObjectDir,
			File gameServerRequestObjectDir) {
		// 读取类的属性
		List<String[]> datas = CSVUtil.getDataFromCSV2WithoutCheck(interfaceCodeCSV.getAbsolutePath());
		StringBuilder codeBuilder = new StringBuilder();
		// 声明包
		codeBuilder.append("package requestobject.").append(nameSpace)
				.append(";\n");
		codeBuilder.append("import requestobject.BaseRequestObject;\n");
		codeBuilder.append("/*\n");
		codeBuilder.append("created by:").append(creator).append("\n");
		codeBuilder.append("create time:").append(createTime).append("\n");
		codeBuilder.append("comment:").append(comment).append("\n");
		codeBuilder.append("*/\n");
		codeBuilder.append("public class ").append(nameSpace).append(interfaceName)
				.append("RequestObject extends BaseRequestObject\n");
		// 声明方法
		codeBuilder.append("{\n");
		Set<String> propNameSet = new HashSet<>();
		for (int startRow = 1; startRow < datas.size(); startRow++) {
			if (datas.get(startRow)[0].startsWith("#")) {
				continue;
			}
			String javaPropName = datas.get(startRow)[0];
			if (propNameSet.contains(javaPropName)) {
				System.out.println(interfaceCodeCSV.getAbsolutePath() + "has duplicated props" + javaPropName);
				return;
			} else {
				propNameSet.add(javaPropName);
			}
			String propType = datas.get(startRow)[1];
			String propName = Character.toUpperCase(javaPropName.charAt(0)) + javaPropName.substring(1);
			String propComment = datas.get(startRow)[2];
			if (null != propComment && !"".equals(propComment)) {
				codeBuilder.append("	/**\n		").append(propComment).append("\n	**/\n");
			}
			codeBuilder.append("	private ").append(propType).append(" ").append(javaPropName).append(";\n\n");
			// 生成setter
			codeBuilder.append("	public void set").append(propName).append("(").append(propType).append(" ")
					.append(javaPropName).append(")").append("{\n");
			codeBuilder.append("		this.").append(javaPropName).append(" = ").append(javaPropName).append(";\n");
			codeBuilder.append("	}\n\n");
			// 生成getter
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
		
		File gameServerRequestObjectFile = new File(gameServerRequestObjectDir, nameSpace + interfaceName + "RequestObject.java");
		try (Writer writer = new FileWriter(gameServerRequestObjectFile)) {
			writer.write(codeBuilder.toString());
		} catch (Throwable th) {
			th.getStackTrace();
		}
	}

	public static void interfaceCsv2Logic(File interfaceCodeCSV, String nameSpace, String interfaceName, String creator,
			String createTime, String comment, String returnType, File logicCodeDir) {
		// 读取类的属性
		StringBuilder codeBuilder = new StringBuilder();
		// 声明包
		codeBuilder.append("package logic.").append(nameSpace).append(";\n");
		codeBuilder.append("import org.springframework.stereotype.Controller;\n\n");
		codeBuilder.append("import org.springframework.web.servlet.ModelAndView;\n\n");
		codeBuilder.append("import com.alibaba.fastjson.JSON;\n")
				.append("import utils.WebUtil;\n")
				.append("import net.bilinkeji.common.log.LoggerWraper;\n\n");
		String interfaceRequestObjectClassName = nameSpace + interfaceName + "RequestObject ";
		codeBuilder.append("import requestobject.").append(nameSpace).append(".")
				.append(interfaceRequestObjectClassName).append(";\n\n");
		codeBuilder.append("@Controller").append("\n\n");
		codeBuilder.append("public class ").append(nameSpace).append(interfaceName).append("Logic\n");
		// 声明方法
		codeBuilder.append("{\n");
		codeBuilder.append("private static LoggerWraper log = LoggerWraper.getLogger(\"").append(nameSpace)
				.append(interfaceName).append("\"); \n");
		codeBuilder.append("//本接口默认本地访问路径是http://localhost:8080/").append(nameSpace).append("/").append(interfaceName)
				.append(".do\n");
		codeBuilder
				.append("//本接口页面本地访问路径是D:\\workspace\\XRGameOperateConsole\\WebContent\\WEB-INF\\page\\")
				.append(nameSpace).append("\\").append(interfaceName).append(".jsp\n");
		codeBuilder.append("	public static ").append(returnType).append(" process(")
				.append(interfaceRequestObjectClassName).append("requestObject,ModelAndView modelAndView){\n");
		codeBuilder.append("		try{\n\n		}catch(Throwable th){\n			")
				.append("log.error(\"error|duringProcessOf|\" + requestObject.getClass().getName() + \"|\" + WebUtil.getRequestColour() + \"|\" + JSON.toJSONString(requestObject),th);")
				.append("		\n		}");
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
		case "void":
			codeBuilder.append("return ;\n");
			break;
		case "":
			codeBuilder.append("return  new ModelAndView(\"/admin/login\");\n");
			break;
		default:
			codeBuilder.append("return  null;\n");
			break;
		}
		codeBuilder.append("\n	}");
		codeBuilder.append("\n}");
		File requestObjectFile = new File(logicCodeDir, nameSpace + interfaceName + "Logic.java");
		if (!requestObjectFile.exists()) {
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
