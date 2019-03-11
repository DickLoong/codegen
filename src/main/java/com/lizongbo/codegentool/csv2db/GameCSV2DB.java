package com.lizongbo.codegentool.csv2db;

import com.lizongbo.codegentool.*;
import com.lizongbo.codegentool.configverification.CsvCheckerManager;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.db2java.dbsql2xml.DBUtil;
import com.lizongbo.codegentool.db2java.dbsql2xml.XmlCodeGen;
import com.lizongbo.codegentool.tools.StringUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class GameCSV2DB {

	/**
	 * 前四列都用来存放潜规则信息，第一列是表的字段名，第二列是字段类型,第三列是字段的中文说明, 第四列用来存放特殊注解
	 */
	private static final int CSV_DATA_COL_START_INDEX = 4;
	private static int isGapp = 0;

	private static String protoFileRootDir = CodeGenConsts.PROJPROTOFILE_DIRROOT;
	private static Map<String, List<String[]>> tableColMap = new HashMap<String, List<String[]>>();

	private static String protorpcCmdTableName = "TProto_CMD";
	private static String protoCommConfTableName = "TComm_Commconf";
	private static String protoCommChannelTableName = "tcomm_channel";
	public final static String protoCommErrorCodeTableName = "tcomm_errcode";
	private static String gameZoneTableName = "tserver_gamezone";
	private static String gameModuleInfoTableName = "TComm_Moduleinfo";

	private static String gameLogicSceneInfoTableName = "TComm_Logicsceneinfo";
	private static List<String[]> colList4TModelActiondescinfo = null;// 模型动作描述信息表

	private static List<String[]> colList4TModellActionreqinfo = null;// 模型动作需求信息定义表

	private static List<String[]> colList4TModellEffectreqinfo = null;// 模型动作对应特效需求信息定义表
	private static Set<String> dbbeansList = new TreeSet<String>();

	private static List<String[]> colList4UIComponentDefineinfo = null;
	private static List<String[]> colList4TskillSkillinfo4Proto = null;
	private static List<String[]> colList4TskillMainskillunitinfo4Proto = null;

	public static StringBuilder sbDropTable = new StringBuilder();
	public static StringBuilder sbCreateTable = new StringBuilder();
	public static StringBuilder sbInsertTable = new StringBuilder();

	public static Map<String, StringBuilder> sbDropTableMap = new HashMap<String, StringBuilder>();
	public static Map<String, StringBuilder> sbCreateTableMap = new HashMap<String, StringBuilder>();
	public static Map<String, StringBuilder> sbInsertTableMap = new HashMap<String, StringBuilder>();

	private static StringBuilder sbDbBeanConfigProto4Import = new StringBuilder();
	private static StringBuilder sbDbBeanConfigProto = new StringBuilder();

	private static List<String> errMailMsgList = new ArrayList<String>();

	/**
	 * 输出并累计出错信息，然后发邮件
	 * 
	 * @param errMsg
	 */
	public static void addErrMailMsgList(String errMsg) {
		if (errMsg == null) {
			return;
		}
		if (errMailMsgList.contains(errMsg)) {
			return;
		}
		System.err.println(errMsg);
		errMailMsgList.add(errMsg);
	}
	// private static int sbDbBeanConfigProtoIndex = 1;

	/**
	 * <b>功能：将游戏策划在Excel里配置的数据，通过csv格式导入数据库，实现自动创建表格并导入</b><br>
	 * <br>
	 * <b>实现步骤：</b><br>
	 * <b>1.</b> <br>
	 * <b>2.</b> <br>
	 * 
	 * @修改者 ~ , quickli 2015-3-30
	 * @param args
	 *            void
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CsvCheckerManager.initCheckerMap();
		CodeGenConsts.switchPlat();
		checkIsGapp();

		XmlCodeGen.genTablesRegion();

		// 2017-07-18 linyaoheng 总是生成全量的collect文件
		// linyaoheng 之前的运行结果怪怪的.直接放到这里来执行了
		File collectTemplate = new File(
				"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/templates/TLocale(文本本地化)_Collect(表字段汇总).csv");
		File collectTarget = new File(CodeGenConsts.PROJCSVFILE_DIRROOT + "I18Ncsv/TLocale(文本本地化)_Collect(表字段汇总).csv");
		LogUtil.printLog("try copy|" + collectTemplate + "|to|" + collectTarget);
		copyFile(collectTemplate, collectTarget);

		dbbeansList.clear();
		protoFileRootDir = CodeGenConsts.PROJPROTOFILE_DIRROOT;
		String testcsvs = CodeGenConsts.PROJCSVFILE_DIRROOT;
		testcsvs = CodeGenConsts.PROJCSVFILE_DIRROOT;
		System.out.println("try load properties finished.");
		tableColMap.clear();
		LevelResCheckTool.checkDDSFile(new File(
				"D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets"));
		LevelResCheckTool.checkDDSFile(new File(
				"D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets"));
		CSVUtil.genEffectList4UnitestTest();
		// 偶尔需要drop所有的表，默认不删除所有的表，采取增量更新模式
		// 这里需要做成判断csv的方式，只drop没有对应csv表格的表，因为有可能是删除了Excel文件
		XmlCodeGen.dropNoCSVTables(CodeGenConsts.PROJDBNAME);
		protoCsvDir2Proto(testcsvs);
		csvDir2DB(testcsvs);
		if (CsvCheckerManager.errorMap.size() > 0) {
			StringBuilder sbmsgText = new StringBuilder();
			System.err.println("CsvCheck Error Begin:---------------------------------------");
			for (Map.Entry<String, List<String>> e : CsvCheckerManager.errorMap.entrySet()) {
				System.err.println(e.getKey() + "|Error:" + e.getValue());
				sbmsgText.append(e.getKey() + "|Error:" + e.getValue());
			}
			System.err.println("CsvCheck Error End:---------------------------------------");
			MailTest.sendErrorMail("CSV数据校验出错了", sbmsgText.toString());
		}
		CSVUtil.genCsvCheckerManager();
		CSVUtil.genClientUIJumpEnableFile();
		// crud页需要做成通用rpc命令
		// genProtoBufProtoCRUDJavaServiceFile(tableColMap);
		appendptor2netbat4dbbeans();
		// genModelReqFiles();
		// genEffectReqFilesAndUnityFolder();
		// gen3dsMax2FBXbatFile();
		// checkModelReqFiles();

		// genUIReqFiles();
		// CSVUtil.genEffectReqFiles();
		genProtoBufDBBeansProtoBufConfigUtilFile();
		genProtoBufDBBeansProtoBufConfigUtilFile2();
		genProtoBufDBBeansDbbeansConfig4ProtoUtilJavaFile();
		CSVUtil.protoReqRespFileCombine(CodeGenConsts.PROJPROTOFILE_DIRROOT);

		CSVUtil.genTskillSkillinfoConvertUtilCSFile(colList4TskillSkillinfo4Proto,
				colList4TskillMainskillunitinfo4Proto);
		GenAll.main(args);
		for (int i = 0; i < 20; i++) {
			System.err.println();
		}
		GameCSV2DB.writeFile(new File(CodeGenConsts.PROJSQLFILE_DIRROOT, "dropdb.sql").getAbsolutePath(),
				sbDropTable.toString());
		GameCSV2DB.writeFile(new File(CodeGenConsts.PROJSQLFILE_DIRROOT, "createdb.sql").getAbsolutePath(),
				sbCreateTable.toString());

		GameCSV2DB.writeFile(new File(CodeGenConsts.PROJSQLFILE_DIRROOT, "dropandcreatedb.sql").getAbsolutePath(),
				sbDropTable.toString() + sbCreateTable.toString());
		GameCSV2DB.writeFile(new File(CodeGenConsts.PROJSQLFILE_DIRROOT, "insertdb.sql").getAbsolutePath(),
				" set sql_mode= \'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n"
						+ sbInsertTable.toString());
		StringBuilder sbDropCreateInsert = new StringBuilder(8192 * 10);
		StringBuilder sbDropCreateInsert4user = new StringBuilder(8192 * 10);

		for (Map.Entry<String, StringBuilder> e : sbDropTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(CodeGenConsts.PROJSQLFILE_DIRROOT + "/dropsqls", "dropdb4" + e.getKey() + ".sql")
							.getAbsolutePath(),
					e.getValue().toString());
			GameCSV2DB.writeFile(
					new File(CodeGenConsts.PROJSQLFILE_DIRROOT + "/dropCreateInsertsqls",
							"dropCreateInsertdb4" + e.getKey() + ".sql").getAbsolutePath(),
					e.getValue().toString() + sbCreateTableMap.get(e.getKey()) + sbInsertTableMap.get(e.getKey())

			);
			if (!e.getKey().contains("user")) {
				sbDropCreateInsert.append(e.getValue().toString()).append(sbCreateTableMap.get(e.getKey()))
						.append(sbInsertTableMap.get(e.getKey())).append("\n\n\n");
			} else {
				sbDropCreateInsert4user.append(e.getValue().toString()).append(sbCreateTableMap.get(e.getKey()))
						.append(sbInsertTableMap.get(e.getKey())).append("\n\n\n");
			}

		}
		GameCSV2DB.writeFile(
				new File(CodeGenConsts.PROJSQLFILE_DIRROOT + "/dropCreateInsertsqls", "dropCreateInsertdb4dbconfig.sql")
						.getAbsolutePath(),
				"SET sql_mode='NO_AUTO_VALUE_ON_ZERO';\n" + sbDropCreateInsert.toString()

		);
		GameCSV2DB.writeFile(
				new File(CodeGenConsts.PROJSQLFILE_DIRROOT + "/dropCreateInsertsqls", "dropCreateInsertdb4dbuser.sql")
						.getAbsolutePath(),
				"SET sql_mode='NO_AUTO_VALUE_ON_ZERO';\n" + sbDropCreateInsert4user.toString()

		);

		for (Map.Entry<String, StringBuilder> e : sbCreateTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(CodeGenConsts.PROJSQLFILE_DIRROOT + "/createsqls", "createdb4" + e.getKey() + ".sql")
							.getAbsolutePath(),
					e.getValue().toString());
		}
		for (Map.Entry<String, StringBuilder> e : sbInsertTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(CodeGenConsts.PROJSQLFILE_DIRROOT + "/insertsqls", "insertdb4" + e.getKey() + ".sql")
							.getAbsolutePath(),
					e.getValue().toString());
		}
		String mysqlHosttxt = GenAll.readFile(
				new File(CodeGenConsts.PROJ_MYSQLDB_CONFIG_ROOT, "mysqlhosts.txt").getAbsolutePath(), "UTF-8");
		String redisHosttxt = GenAll
				.readFile(new File(CodeGenConsts.PROJ_REDIS_CONFIG_ROOT, "redishosts.txt").getAbsolutePath(), "UTF-8");

		writeFile(new File(CodeGenConsts.PROJ_REDIS_CONFIG_ROOT, "redisandmysqlhosts4local.txt").getAbsolutePath(),
				mysqlHosttxt + redisHosttxt);
		mysqlHosttxt = ServerContainerGenTool.replaceAll(mysqlHosttxt, "10.0.0.16", "10.116.33.153");
		redisHosttxt = ServerContainerGenTool.replaceAll(redisHosttxt, "10.0.0.16", "10.116.33.153");

		writeFile(new File(CodeGenConsts.PROJ_REDIS_CONFIG_ROOT, "redisandmysqlhosts4remote.txt").getAbsolutePath(),
				mysqlHosttxt + redisHosttxt);

		genDbBeansConfigProtoFile();
		System.out.println(
				"CSVUtil.interrColMap=" + StringUtil.replaceAll(CSVUtil.interrColMap.toString(), "],", "],\n"));
		sendMailAndExit();
	}

	public static void sendMailAndExit() {
		if (errMailMsgList.size() > 0) {
			MailTest.sendErrorMail("GameCSV2DB执行出错了", StringUtil.join("\n", errMailMsgList));
			System.exit(-100);
		}
		System.exit(0);
	}

	/**
	 * 
	 * @param args
	 *            第一个参数为csv文件,第二个为保存目录
	 */
	public static void DataPatchCSV2DB(String[] args) {
		// TODO Auto-generated method stub
		// TODO 要更改的属性
		// TODO CodeGenConsts.PROJCSVFILE_DIRROOT;
		// TODO CodeGenConsts.PROJSQLFILE_DIRROOT
		CsvCheckerManager.initCheckerMap();
		dbbeansList.clear();
		String testcsvs = args[0];
		System.out.println("try load properties finished.");
		tableColMap.clear();
		// 偶尔需要drop所有的表，默认不删除所有的表，采取增量更新模式
		// 这里需要做成判断csv的方式，只drop没有对应csv表格的表，因为有可能是删除了Excel文件
		XmlCodeGen.dropNoCSVTables(CodeGenConsts.PROJDBNAME);
		protoCsvDir2Proto(testcsvs);
		csvDir2DB(testcsvs);
		if (CsvCheckerManager.errorMap.size() > 0) {
			StringBuilder sbmsgText = new StringBuilder();
			System.err.println("CsvCheck Error Begin:---------------------------------------");
			for (Map.Entry<String, List<String>> e : CsvCheckerManager.errorMap.entrySet()) {
				System.err.println(e.getKey() + "|Error:" + e.getValue());
				sbmsgText.append(e.getKey() + "|Error:" + e.getValue());
			}
			System.err.println("CsvCheck Error End:---------------------------------------");
			MailTest.sendErrorMail("CSV数据校验出错了", sbmsgText.toString());
		}

		for (int i = 0; i < 20; i++) {
			System.err.println();
		}
		String PROJSQLFILE_DIRROOT = args[1];
		GameCSV2DB.writeFile(new File(PROJSQLFILE_DIRROOT, "dropdb.sql").getAbsolutePath(), sbDropTable.toString());
		GameCSV2DB.writeFile(new File(PROJSQLFILE_DIRROOT, "createdb.sql").getAbsolutePath(), sbCreateTable.toString());

		GameCSV2DB.writeFile(new File(PROJSQLFILE_DIRROOT, "dropandcreatedb.sql").getAbsolutePath(),
				sbDropTable.toString() + sbCreateTable.toString());
		GameCSV2DB.writeFile(new File(PROJSQLFILE_DIRROOT, "insertdb.sql").getAbsolutePath(),
				" set sql_mode= \'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n"
						+ sbInsertTable.toString());
		StringBuilder sbDropCreateInsert = new StringBuilder(8192 * 10);
		StringBuilder sbDropCreateInsert4user = new StringBuilder(8192 * 10);
		StringBuilder sbInsert = new StringBuilder(8192 * 10);

		for (Map.Entry<String, StringBuilder> e : sbDropTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(PROJSQLFILE_DIRROOT + "/dropsqls", "dropdb4" + e.getKey() + ".sql").getAbsolutePath(),
					e.getValue().toString());
			GameCSV2DB.writeFile(
					new File(PROJSQLFILE_DIRROOT + "/dropCreateInsertsqls", "dropCreateInsertdb4" + e.getKey() + ".sql")
							.getAbsolutePath(),
					e.getValue().toString() + sbCreateTableMap.get(e.getKey()) + sbInsertTableMap.get(e.getKey())

			);
			if (!e.getKey().contains("user")) {
				sbDropCreateInsert.append(e.getValue().toString()).append(sbCreateTableMap.get(e.getKey()))
						.append(sbInsertTableMap.get(e.getKey())).append("\n\n\n");
				sbInsert.append(sbInsertTableMap.get(e.getKey())).append("\n\n\n");
			} else {
				sbDropCreateInsert4user.append(e.getValue().toString()).append(sbCreateTableMap.get(e.getKey()))
						.append(sbInsertTableMap.get(e.getKey())).append("\n\n\n");
				sbInsert.append(sbInsertTableMap.get(e.getKey())).append("\n\n\n");
			}

		}
		GameCSV2DB.writeFile(new File(PROJSQLFILE_DIRROOT + "/dropCreateInsertsqls", "dropCreateInsertdb4dbconfig.sql")
				.getAbsolutePath(), "SET sql_mode='NO_AUTO_VALUE_ON_ZERO';\n" + sbDropCreateInsert.toString()

		);
		GameCSV2DB.writeFile(
				new File(PROJSQLFILE_DIRROOT + "/dropCreateInsertsqls", "dropCreateInsertdb4dbuser.sql")
						.getAbsolutePath(),
				"SET sql_mode='NO_AUTO_VALUE_ON_ZERO';\n" + sbDropCreateInsert4user.toString()

		);
		GameCSV2DB.writeFile(new File(PROJSQLFILE_DIRROOT + "/insertsqls", "Insertdb4dbuser.sql").getAbsolutePath(),
				"SET sql_mode='NO_AUTO_VALUE_ON_ZERO';\n" + sbInsert.toString()

		);

		for (Map.Entry<String, StringBuilder> e : sbCreateTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(PROJSQLFILE_DIRROOT + "/createsqls", "createdb4" + e.getKey() + ".sql").getAbsolutePath(),
					e.getValue().toString());
		}
		for (Map.Entry<String, StringBuilder> e : sbInsertTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(PROJSQLFILE_DIRROOT + "/insertsqls", "insertdb4" + e.getKey() + ".sql").getAbsolutePath(),
					e.getValue().toString());
		}
		System.out.println(
				"CSVUtil.interrColMap=" + StringUtil.replaceAll(CSVUtil.interrColMap.toString(), "],", "],\n"));
		if (errMailMsgList.size() > 0) {
			MailTest.sendErrorMail("DataPatchCSV2DB执行出错了", StringUtil.join("\n", errMailMsgList));
		}
	}

	public static void checkIsGapp() {
		// TODO Auto-generated method stub
		String remoteIp = // "120.76.54.48";;//
				System.getenv("remoteIp");
		// TODO 读取一个配置文件
		Properties prop = new Properties();
		InputStream in = Object.class.getResourceAsStream("/DeployConfig." + remoteIp + ".properties");
		System.out.println("try load properties.");
		try {
			String url = "String";
			prop.load(in);
			protoFileRootDir = CodeGenConsts.PROJPROTOFILE_DIRROOT;
			if (prop.containsKey("ifGapp")) {
				Object isGappObject = prop.getProperty("ifGapp");
				int isGapp = Integer.parseInt(isGappObject.toString());
				if (isGapp > 0) {
					Class<CodeGenConsts> constsClazz = CodeGenConsts.class;
					Class<CodeGenGAPPConsts> gappConstsClazz = CodeGenGAPPConsts.class;
					CodeGenGAPPConsts gappConstsInstance = gappConstsClazz.newInstance();
					Field[] constsClazzFields = constsClazz.getDeclaredFields();
					Field[] gappConstsClazzFields = gappConstsClazz.getDeclaredFields();
					for (Field field : constsClazzFields) {
						field.setAccessible(true);
						for (Field gappField : gappConstsClazzFields) {
							if (gappField.getName().equals(field.getName())) {
								field.set(constsClazz, gappField.get(gappConstsInstance));
								System.out.println(field.get(constsClazz));
							}
						}
						// TODO 使用properties中的属性来赋值,暂时不用
						if (prop.containsKey(field.getName())) {
							field.set(constsClazz, prop.get(field.getName()));
						}
					}

				}
			}
		} catch (Throwable e) {
			;
		}

		protoFileRootDir = CodeGenConsts.PROJPROTOFILE_DIRROOT;
	}

	private static void genDbBeansConfigProtoFile() {
		String className = "DbbeansConfig4Proto";
		String protoFileDir = protoFileRootDir + "/dbbeans4proto";
		String protoFileName = className + ".proto";
		StringBuilder sb = new StringBuilder();

		sb.append("package " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".dbbeans4proto" + ";").append("\n\n");
		// optional bcl.Decimal UnitPrice = 3;

		sb.append("option optimize_for = SPEED;\n");
		sb.append("option java_generate_equals_and_hash = true;\n");
		sb.append("option java_outer_classname = \"" + className + "OuterClass\";\n");
		sb.append(sbDbBeanConfigProto4Import);

		sb.append("message IntStringPair{\n");
		sb.append("	required int32 key = 1 [default = 0]; //键值对的整数Key\n");
		sb.append("	required string value = 2 ; //键值对的String Value\n");
		sb.append("}\n");

		sb.append("message ").append(className).append("{\n");
		sb.append("\toptional int32 zoneId = 998;//服务区id\n");
		sb.append("\trepeated IntStringPair dbVerList = 999;//各表md5值，用于增量更新配置,key是pb的index，value是md5值的前四位\n");
		sb.append(sbDbBeanConfigProto);
		sb.append("\n");
		sb.append("}").append("\n");
		File protoFile = new File(protoFileDir, protoFileName);
		System.out.println("genDbBeansConfigProtoFile|" + protoFile.getAbsolutePath());
		writeFile(protoFile.getAbsolutePath(), sb.toString());

	}

	public static Set<String> getTableNamesbyCSV(String csvdir) {
		File f = new File(csvdir);
		if (!f.isDirectory()) {
			return null;
		}
		Set<String> s = new HashSet<String>();
		File[] subfs = f.listFiles();
		for (int i = 0; i < subfs.length; i++) {
			File file = subfs[i];
			if (file.isDirectory()) {
				Set<String> ss = getTableNamesbyCSV(file.getAbsolutePath());
				if (ss != null) {
					s.addAll(ss);
				}
			}
			if (file.isFile() && file.getName().endsWith(".csv")) {
				String tablename = CSVUtil.getTableNameFromCSVFile(file.getAbsolutePath());
				if (tablename != null && tablename.length() > 1) {
					s.add(tablename);
				}
			}
		}
		return s;

	}

	private static String getActionTitle(String actionName) {

		for (int k = 0; colList4TModelActiondescinfo != null && k < colList4TModelActiondescinfo.size(); k++) {
			String[] s = colList4TModelActiondescinfo.get(k);
			if (s.length >= 2 && actionName != null && actionName.equalsIgnoreCase(s[0])) {
				return s[1];
			}
		}
		return "无名动作";
	}

	/**
	 * 检查已经收货的模型文件，对比需求列出尚未交货的文件
	 */
	private static void checkModelReqFiles() {
		List<String> list = checkLostMdelFiles(new File(CodeGenConsts.PROJMODELREQFILE_DIRROOT),
				new File(CodeGenConsts.PROJMODELSFILE_DIRROOT));
		for (String s : list) {
			System.err.println("nofile for modles：" + s);
		}
	}

	/**
	 * 遍历已经提交的max文件，生成批量转化fbx文件的脚本.
	 * 
	 */
	private static void gen3dsMax2FBXbatFile() {
		File modleReqsDir = new File(CodeGenConsts.PROJMODELSFILE_DIRROOT);
		if (!modleReqsDir.isDirectory()) {
			return;
		}
		StringBuilder sbBat = new StringBuilder();
		for (File dir : modleReqsDir.listFiles()) {
			if (dir.isDirectory()) {
				sbBat.append(("mkdir D:/mgamedev/workspace/" + CodeGenConsts.PROJNAME + "_proj/"
						+ CodeGenConsts.PROJNAME + "_docs/ModelFBXs/" + dir.getName()).replace('/', '\\') + " \r\n");
				sbBat.append("\"" + CodeGenConsts.EXE_3DSMAXPATH + "\" /Language=CHS -silent -U MAXScript "
						+ "\"D:/mgamedev/workspace/" + CodeGenConsts.PROJNAME + "_proj/" + CodeGenConsts.PROJNAME
						+ "_docs/modleReqs/batfiles/fbxbatchexport_" + dir.getName() + ".ms\"\r\n");
				StringBuilder sbMs = new StringBuilder();
				sbMs.append("FBXExporterSetParam  \"LoadExportPresetFile\"  \"D:/mgamedev/workspace/"
						+ CodeGenConsts.PROJNAME + "_proj/" + CodeGenConsts.PROJNAME
						+ "_docs/3dsmaxcfgs/fbxexportcfgs//daomoxing.fbxexportpreset\"\r\n");
				sbMs.append("\r\n");
				sbMs.append("FBXExporterGetParam \"FBXProperties\" \r\n");
				sbMs.append("\r\n");

				// 先导模型，
				File[] maxFiles = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname != null && pathname.getName().endsWith(".max")
								&& !pathname.getName().contains("@");
					}
				});
				for (File mf : maxFiles) {
					sbMs.append("loadmaxfile \"D:/mgamedev/workspace/" + CodeGenConsts.PROJNAME + "_proj/"
							+ CodeGenConsts.PROJNAME + "_docs/modleReqs/Models/" + dir.getName() + "/" + mf.getName()
							+ "\" --打开max模型文件\r\n");
					sbMs.append("exportFile \"D:/mgamedev/workspace/" + CodeGenConsts.PROJNAME + "_proj/"
							+ CodeGenConsts.PROJNAME + "_docs/ModelFBXs/" + dir.getName() + "/"
							+ mf.getName().substring(0, mf.getName().lastIndexOf(".")) + ".FBX" + "\"  #noPrompt\r\n");
					sbMs.append("\r\n");
				}

				sbMs.append("FBXExporterSetParam  \"LoadExportPresetFile\"  \"D:/mgamedev/workspace/"
						+ CodeGenConsts.PROJNAME + "_proj/" + CodeGenConsts.PROJNAME
						+ "_docs/3dsmaxcfgs/fbxexportcfgs//daodongzuo.fbxexportpreset\"\r\n");
				sbMs.append("\r\n");
				sbMs.append("FBXExporterGetParam \"FBXProperties\" \r\n");
				sbMs.append("\r\n");
				// 再导动作
				maxFiles = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname != null && pathname.getName().endsWith(".max")
								&& pathname.getName().contains("@");
					}
				});

				for (File mf : maxFiles) {

					sbMs.append("loadmaxfile \"D:/mgamedev/workspace/" + CodeGenConsts.PROJNAME + "_proj/"
							+ CodeGenConsts.PROJNAME + "_docs/modleReqs/Models/" + dir.getName() + "/" + mf.getName()
							+ "\" --打开max动作文件\r\n");
					sbMs.append("exportFile \"D:/mgamedev/workspace/" + CodeGenConsts.PROJNAME + "_proj/"
							+ CodeGenConsts.PROJNAME + "_docs/ModelFBXs/" + dir.getName() + "/"
							+ mf.getName().substring(0, mf.getName().lastIndexOf(".")) + ".FBX" + "\"  #noPrompt\r\n");
					sbMs.append("\r\n");
				}

				sbMs.append("\r\n");
				sbMs.append("quitMax #noPrompt\r\n");

				writeFile(new File(CodeGenConsts.PROJMODELSBATFILE_DIRROOT, "fbxbatchexport_" + dir.getName() + ".ms")
						.getAbsolutePath(), sbMs.toString(), "UTF-8");
			}
		}

		writeFile(new File(CodeGenConsts.PROJMODELSBATFILE_DIRROOT, "exportfbx.bat").getAbsolutePath(),
				sbBat.toString(), "UTF-8");

	}

	private static List<String> checkLostMdelFiles(File org, File dest) {
		if (org == null || dest == null || !org.isDirectory() || !dest.isDirectory()) {
			return new ArrayList<String>();
		}
		List<String> list = new ArrayList<String>();
		for (File f : org.listFiles()) {
			if (f.isDirectory()) {
				list.addAll(checkLostMdelFiles(f, new File(dest, f.getName())));
			}
			if (f.isFile() && (f.getName().endsWith(".max") || f.getName().endsWith(".png"))) {
				File destF = new File(dest, f.getName());
				if (!destF.exists() || destF.length() < 10) {// 文件不存在或文件为空
					list.add(destF.getAbsolutePath());
				}
			}
		}
		return list;

	}

	private static void genModelReqFiles() {
		if (colList4TModelActiondescinfo != null && colList4TModellActionreqinfo != null) {
			String[] colNames = colList4TModellActionreqinfo.get(0);
			for (int k = 4; colList4TModellActionreqinfo != null && k < colList4TModellActionreqinfo.size(); k++) {
				StringBuilder sb = new StringBuilder();
				String[] colValues = colList4TModellActionreqinfo.get(k);
				String modelName = colList4TModellActionreqinfo.get(k)[0].toLowerCase();
				File modelDir = new File(CodeGenConsts.PROJMODELREQFILE_DIRROOT, modelName);
				modelDir.mkdirs();
				// 先删除文件夹下已经生成的文件，因为有的动作可能最初提了需求然后🈶又干掉了
				File[] maxFileExistList = modelDir.listFiles();
				for (int i = 0; maxFileExistList != null && i < maxFileExistList.length; i++) {
					maxFileExistList[i].delete();
				}
				sb.append("\r\n文件名： " + modelName + ".max \r\n模型需求说明：\r\n 这个是人物模型文件，人物模型必须为T-Pose\r\n");
				try {
					new File(modelDir, modelName + ".max").createNewFile();
					new File(modelDir, modelName + "_skin01.png").createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int i = 6; colNames != null && i < colNames.length; i++) {
					if (colValues.length > i && colValues[i] != null && colValues[i].trim().length() > 1) {
						String maxFileName = modelName + "@" + colNames[i].toLowerCase() + ".max";
						sb.append("\r\n\r\n-----------------------------------\r\n");
						sb.append("文件名： " + maxFileName + "\r\n");
						sb.append("动作名称： ").append(getActionTitle(colNames[i])).append("\r\n");
						sb.append("动作需求说明： ").append(colValues[i]).append("\r\n");
						try {
							new File(modelDir, maxFileName).createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				writeFile(new File(modelDir, "readme.txt").getAbsolutePath(), sb.toString());
			}

		} else {
			System.err.println("colList4TModelActiondescinfo is null =" + colList4TModelActiondescinfo
					+ " or colList4TModellActionreqinfo is null=" + colList4TModellActionreqinfo);
		}

	}

	private static void genEffectReqFilesAndUnityFolder() {
		if (colList4TModelActiondescinfo != null && colList4TModellEffectreqinfo != null) {
			String[] colNames = colList4TModellEffectreqinfo.get(0);
			for (int k = 4; colList4TModellEffectreqinfo != null && k < colList4TModellEffectreqinfo.size(); k++) {
				String[] colValues = colList4TModellEffectreqinfo.get(k);
				String modelName = colList4TModellEffectreqinfo.get(k)[0].toLowerCase();
				File modelDir = new File(new File(CodeGenConsts.PROJ_EFFECTRES_UNITYROOT, "formodels"), modelName);
				modelDir.mkdirs();
				for (int i = 6; colNames != null && i < colNames.length; i++) {
					if (colValues.length > i && colValues[i] != null && colValues[i].trim().length() > 1) {
						File effectResDir = new File(modelDir,
								"effectres_" + modelName + "_" + colNames[i].toLowerCase());

						StringBuilder sb = new StringBuilder();
						sb.append("特效需求资源请存放在此目录：\r\n");
						sb.append("动作名称： ").append(getActionTitle(colNames[i])).append("\r\n");
						sb.append("特效需求说明： ").append(colValues[i]).append("\r\n");
						sb.append("预制对象在 Assets/Resources/Prefabs4ModelEffects/" + modelName + "/" + "effect_"
								+ modelName + "_" + colNames[i].toLowerCase() + ".prefab\r\n");
						effectResDir.mkdirs();
						// System.err.println("genEffectReqFilesAndUnityFolder|"
						// + effectResDir);
						writeFile(new File(effectResDir, "readme.txt").getAbsolutePath(), sb.toString());
						// 把readme生成进去
						// new File(modelDir, maxFileName).createNewFile();

					}
				}

			}

		} else {
			System.err.println("colList4TModelActiondescinfo is null =" + colList4TModelActiondescinfo
					+ " or colList4TModellActionreqinfo is null=" + colList4TModellActionreqinfo);
		}

	}

	private static String getUISuffixByType(String type) {
		switch (type) {
		case "btn":
		case "lpbtn":
			return "=Button";
		case "label":
		case "inputtext":
		case "textarea":
		case "password":
			return "=Text";
		case "image":
		case "num10image":
		case "num100image":
		case "joystick":
			return "=PNG";
		default:
			return "";
		}
	}

	private static void genUIReqFiles() {
		if (colList4UIComponentDefineinfo != null) {
			String[] colNames = colList4UIComponentDefineinfo.get(0);

			// 先删除文件夹下已经生成的文件，因为可能最初提了需求然后又干掉了
			File uiDirs = new File(CodeGenConsts.PROJUIREQFILE_DIRROOT);
			File[] uiFileExistList = uiDirs.listFiles();
			for (int i = 0; uiFileExistList != null && i < uiFileExistList.length; i++) {
				uiFileExistList[i].delete();
			}
			uiDirs.mkdirs();

			// 容器类型
			// modulename -> psdname -> type
			ConcurrentHashMap<String, ConcurrentHashMap<String, String>> windowContainerType = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();

			for (int k = 4; colList4UIComponentDefineinfo != null && k < colList4UIComponentDefineinfo.size(); k++) {
				String[] colValues = colList4UIComponentDefineinfo.get(k);

				String psdName = colValues[4].toLowerCase();
				String moduleName = colValues[3].toLowerCase();
				String compName = colValues[2].toLowerCase();
				String compType = colValues[1].toLowerCase();

				if (!windowContainerType.containsKey(moduleName)) {
					windowContainerType.put(moduleName, new ConcurrentHashMap<String, String>());
				}

				if (compName.equals(psdName)) {
					if (!windowContainerType.get(moduleName).containsKey(psdName)) {
						windowContainerType.get(moduleName).put(psdName, compType);
					}
				}

				File moduleDir = new File(uiDirs, moduleName);
				File[] moduleDefFileList = moduleDir.listFiles();
				for (int i = 0; moduleDir != null && moduleDefFileList != null && i < moduleDefFileList.length; ++i) {
					moduleDefFileList[i].delete();
				}

				moduleDir.mkdirs();
			}

			// 生成文件
			String preModulePSDName = "";
			for (int k = 4; colList4UIComponentDefineinfo != null && k < colList4UIComponentDefineinfo.size(); k++) {
				String[] colValues = colList4UIComponentDefineinfo.get(k);

				String moduleName = colValues[3].toLowerCase();
				String psdName = colValues[4].toLowerCase();
				String compName = colValues[2].toLowerCase();
				String compType = colValues[1].toLowerCase();

				File moduleDir = new File(uiDirs, moduleName);

				StringBuilder sb = new StringBuilder();

				String uiControlName = "ui_" + compType + "_" + moduleName + "_" + compName;
				// ui_btn_login_guestlogin
				String uiTypeSuffix = getUISuffixByType(compType);

				int theWidth = Integer.parseInt(colValues[6]);
				int theHeight = Integer.parseInt(colValues[7]);

				String curModulePSDName = moduleName + "_" + psdName;

				String psdFileName = "ui_"
						+ (windowContainerType.get(moduleName).containsKey(psdName)
								? windowContainerType.get(moduleName).get(psdName) : "XXXXXX")
						+ "_" + moduleName + "_" + psdName;

				if (!preModulePSDName.equalsIgnoreCase(curModulePSDName)) {
					sb.append("PSD文件名： " + psdFileName + ".psd\r\n");
					sb.append("模块： " + moduleName + "\r\n");
					sb.append("需求负责人： " + colValues[5].toLowerCase() + "\r\n");
					sb.append("控件尺寸(宽X高)： "
							+ ((theWidth > 0 || theHeight > 0) ? colValues[6] + " X " + colValues[7] : "") + "\r\n");
					sb.append("需求描述： " + colValues[8].toLowerCase() + "\r\n");
					sb.append("提需求时间： " + colValues[9].toLowerCase() + "\r\n");
					sb.append("注意： 文本输入框请放到Textinputs组\r\n");
					sb.append("      按钮请放到Buttonlist组\r\n");
				}

				preModulePSDName = curModulePSDName;

				if (!psdName.equals(compName)) {
					sb.append("\r\n\r\n-----------------------------------\r\n");
					sb.append("控件名： " + uiControlName + uiTypeSuffix + "\r\n");
					sb.append("类型： " + compType + "\r\n");
					sb.append("模块： " + moduleName + "\r\n");
					sb.append("需求负责人： " + colValues[5].toLowerCase() + "\r\n");
					sb.append("控件尺寸(宽X高)： "
							+ ((theWidth > 0 || theHeight > 0) ? colValues[6] + " X " + colValues[7] : "") + "\r\n");

					sb.append("需求描述： " + colValues[8].toLowerCase());
					if ("btn".equals(compType)) {
						sb.append(" (三个组: Normal/Highlighted/Pressed/Disabled)");
					}
					sb.append("\r\n");

					sb.append("提需求时间： " + colValues[9].toLowerCase() + "\r\n");
				}

				if ("scrollpanelupdown".equals(compType) || "scrollpanelleftright".equals(compType)) {
					int number = Integer.parseInt(colValues[12]) * 2;
					for (int tmpN = 1; tmpN <= number; ++tmpN) {
						sb.append("\r\n\r\n-----------------------------------\r\n");
						sb.append("控件名： ui_item" + tmpN + "=PNG\r\n");
						sb.append("类型： " + colValues[11] + "\r\n");
						sb.append("模块： " + moduleName + "\r\n");
						sb.append("需求负责人： " + colValues[5].toLowerCase() + "\r\n");
						sb.append("控件尺寸(宽X高)： " + "\r\n");
						sb.append("需求描述： " + colValues[8].toLowerCase() + "\r\n");
						sb.append("提需求时间： " + colValues[9].toLowerCase() + "\r\n");
					}
				}

				if (sb.length() > 0) {
					appendFile(new File(moduleDir, psdFileName + ".psd.txt").getAbsolutePath(), sb.toString());
				}
			}

			genUIHandlerFiles();
		} else {
			System.err.println("colList4UIComponentDefineinfo is null=" + colList4UIComponentDefineinfo);
		}

	}

	/**
	 * 生成UIHandler所用的C#文件 目录结构 Scripts/Moduels/UIHandlerBase/{ModuleName}
	 * Scripts/Moduels/UIHandlerImpl/{ModuleName}
	 */
	private static void genUIHandlerFiles() {
		if (colList4UIComponentDefineinfo != null) {
			String[] colNames = colList4UIComponentDefineinfo.get(0);

			// /start
			ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<String>>> ctrlNameEnumMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<String>>>();
			ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<String>>> btnCallbacksMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<String>>>();
			ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<String>>> addOnClickEventStatementMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<String>>>();

			// 1. 读取文件
			for (int k = 4; colList4UIComponentDefineinfo != null && k < colList4UIComponentDefineinfo.size(); k++) {
				String[] colValues = colList4UIComponentDefineinfo.get(k);

				String moduleName = colValues[3].toLowerCase();
				String psdName = colValues[4].toLowerCase();

				if (!ctrlNameEnumMap.containsKey(moduleName)) {
					ctrlNameEnumMap.put(moduleName, new ConcurrentHashMap<String, ArrayList<String>>());
				}
				if (!ctrlNameEnumMap.get(moduleName).containsKey(psdName)) {
					ctrlNameEnumMap.get(moduleName).put(psdName, new ArrayList<String>());
				}

				if (!btnCallbacksMap.containsKey(moduleName)) {
					btnCallbacksMap.put(moduleName, new ConcurrentHashMap<String, ArrayList<String>>());
				}
				if (!btnCallbacksMap.get(moduleName).containsKey(psdName)) {
					btnCallbacksMap.get(moduleName).put(psdName, new ArrayList<String>());
				}

				if (!addOnClickEventStatementMap.containsKey(moduleName)) {
					addOnClickEventStatementMap.put(moduleName, new ConcurrentHashMap<String, ArrayList<String>>());
				}
				if (!addOnClickEventStatementMap.get(moduleName).containsKey(psdName)) {
					addOnClickEventStatementMap.get(moduleName).put(psdName, new ArrayList<String>());
				}

				String uiControlName = "ui_" + colValues[1].toLowerCase() + "_" + colValues[3].toLowerCase() + "_"
						+ colValues[2].toLowerCase();

				ctrlNameEnumMap.get(moduleName).get(psdName).add(uiControlName);

				String controlType = colValues[1].toLowerCase();

				if ("btn".equals(controlType)) {
					String controlNameShort = DBUtil
							.camelName(colValues[1].toLowerCase() + "_" + colValues[2].toLowerCase());

					StringBuilder btnOnClickDefinitionStatement = new StringBuilder();
					btnOnClickDefinitionStatement
							.append("public virtual void " + controlNameShort + "${ClassName}OnClick(Button btn)");
					btnOnClickDefinitionStatement.append("\n	{");
					btnOnClickDefinitionStatement.append("\n	}");

					btnCallbacksMap.get(moduleName).get(psdName).add(btnOnClickDefinitionStatement.toString());

					addOnClickEventStatementMap.get(moduleName).get(psdName)
							.add("gbch.AddOnClickEvent(${ClassName}ButtonClickHandler.CTRL_NAME." + uiControlName
									+ ".ToString(), this." + controlNameShort + "${ClassName}OnClick);");
				}
			}

			// 2. 检查目录/生成Base代码/生成Impl代码
			Iterator<Entry<String, ConcurrentHashMap<String, ArrayList<String>>>> iterModule = ctrlNameEnumMap
					.entrySet().iterator();
			while (iterModule.hasNext()) {
				Entry<String, ConcurrentHashMap<String, ArrayList<String>>> entry = iterModule.next();

				String moduleName = entry.getKey();
				ConcurrentHashMap<String, ArrayList<String>> psdMap = entry.getValue();

				File moduleDir = new File(CodeGenConsts.PROJUI_HANDLER_SRCROOT, moduleName);
				File baseDir = new File(CodeGenConsts.PROJUI_HANDLER_SRCROOT, "UIHandlerBase/" + moduleName);
				File implDir = new File(CodeGenConsts.PROJUI_HANDLER_SRCROOT, "UIHandlerImpl/" + moduleName);

				// a. 检查目录
				File[] baseFileExistList = baseDir.listFiles();
				for (int i = 0; baseFileExistList != null && i < baseFileExistList.length; i++) {
					baseFileExistList[i].delete();
				}

				// moduleDir.mkdirs();
				baseDir.mkdirs();
				implDir.mkdirs();

				Iterator<Entry<String, ArrayList<String>>> iterPSD = psdMap.entrySet().iterator();
				while (iterPSD.hasNext()) {
					Entry<String, ArrayList<String>> entryPSD = iterPSD.next();

					String psdName = entryPSD.getKey();
					StringBuilder theClassNameBuilder = new StringBuilder(DBUtil.camelName(moduleName + "_" + psdName));
					String theClassName = theClassNameBuilder
							.replace(0, 1, theClassNameBuilder.substring(0, 1).toUpperCase()).toString();

					// b. 生成Base代码
					File uiBaseFile = new File(baseDir, theClassName + "ButtonClickHandler.cs");

					String baseTplFile = "/com/lizongbo/codegentool/templeles/UIHandler.cs.txt";
					URL baseTplUrl = GameCSV2DB.class.getResource(baseTplFile);
					System.out.println("baseTplFile|url=" + baseTplUrl);
					String csTxt = GenAll.readFile(baseTplUrl.getFile(), "UTF-8");
					csTxt = ServerContainerGenTool.replaceAll(csTxt, "${CtrlNameEnum}",
							String.join(", \n\t\t", ctrlNameEnumMap.get(moduleName).get(psdName)));
					csTxt = ServerContainerGenTool.replaceAll(csTxt, "${BtnCallbacks}",
							String.join("\n\n\t", btnCallbacksMap.get(moduleName).get(psdName)));
					csTxt = ServerContainerGenTool.replaceAll(csTxt, "${addOnClickEvents}",
							String.join("\n\t\t", addOnClickEventStatementMap.get(moduleName).get(psdName)));
					csTxt = ServerContainerGenTool.replaceAll(csTxt, "${ClassName}", theClassName);

					GameCSV2DB.writeFile(uiBaseFile.getAbsolutePath(), csTxt);

					// c. 生成Impl代码，仅生成一次
					File uiImplFile = new File(implDir, theClassName + "ButtonClickHandlerImpl.cs");
					if (!uiImplFile.exists()) {
						String implTplFile = "/com/lizongbo/codegentool/templeles/UIHandlerImpl.cs.txt";
						URL implTplUrl = GameCSV2DB.class.getResource(implTplFile);

						String impleTxt = GenAll.readFile(implTplUrl.getFile(), "UTF-8");
						impleTxt = ServerContainerGenTool.replaceAll(impleTxt, "${BtnCallbacks}",
								String.join("\n\n\t", btnCallbacksMap.get(moduleName).get(psdName)));
						impleTxt = ServerContainerGenTool.replaceAll(impleTxt, "${addOnClickEvents}",
								String.join("\n\t\t", addOnClickEventStatementMap.get(moduleName).get(psdName)));
						impleTxt = ServerContainerGenTool.replaceAll(impleTxt, "${ClassName}", theClassName);

						GameCSV2DB.writeFile(uiImplFile.getAbsolutePath(), impleTxt);
					}
				}
			}
		} else {
			System.err.println("colList4UIComponentDefineinfo is null=" + colList4UIComponentDefineinfo);
		}
	}

	/**
	 * 把dbbean和配合接口协议的自定义*4Proto.proto文件也需要全部生成cs代码。
	 */
	private static void appendptor2netbat4dbbeans() {
		StringBuilder sbproto2net = new StringBuilder();
		sbproto2net.append("cd ../protofilestmp/\r\n");
		String proto2netbatDir = protoFileRootDir;
		File rooDir = new File(CodeGenConsts.PROJPROTOFILE_DIRROOT);
		if (rooDir.isDirectory()) {
			for (File dbbeans4protoDir : rooDir.listFiles()) {
				// File dbbeans4protoDir = new
				// File(CodeGenConsts.PROJPROTOFILE_DIRROOT, "dbbeans4proto");
				if (dbbeans4protoDir.isDirectory()) {

					String packageName = CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "."
							+ dbbeans4protoDir.getName().toLowerCase();
					sbproto2net.append("mkdir \"../protonets/").append(packageName.replace('.', '/')).append("\"\r\n");
					File[] pFiles = dbbeans4protoDir.listFiles();
					for (int i = 0; pFiles != null && i < pFiles.length; i++) {
						File f = pFiles[i];
						if (f.getName().endsWith("4Proto.proto")) {
							// System.out.println("appendptor2netbat4dbbeans| "
							// + f);
							// 检查md5是否有发生变化，如果没发生，则不运行生产c#代码的操作
							String zengliangFlag = System.getenv("zenglianggoujian");
							boolean needrem = false;
							if (zengliangFlag != null && "true".equalsIgnoreCase(zengliangFlag.trim())) {
								String fileMd5 = HashCalc.md5(f);
								String oldMd5 = GenAll.readFile(f.getAbsolutePath() + ".md5.txt", "UTF-8").trim();
								if (fileMd5.equalsIgnoreCase(oldMd5)) {// 如果md5相同则不执行生成
									System.err.println("md5notchange for:" + f);
									sbproto2net.append("echo \"notrun ").append("/")
											.append(f.getName().subSequence(0, f.getName().indexOf(".")))
											.append(".cs\"\r\n");
									needrem = true;
									// sbproto2net.append("rem ");
								} else {
									System.err.println(
											"update md5 for:" + f + "|fileMd5=" + fileMd5 + "|oldMd5=" + oldMd5);
									writeFile(f.getAbsolutePath() + ".md5.txt", fileMd5);
								}
							}
							if (needrem) {
								sbproto2net.append("rem ");
							}
							sbproto2net.append("\"../protobufs/protogen\" -i:\"./").append(f.getName()).append("\"")
									.append(" -o:\"../protonets/").append(packageName.replace('.', '/')).append("/")
									.append(f.getName().subSequence(0, f.getName().indexOf("."))).append(".cs\"\r\n");
							if (packageName.contains("dbbeans4proto")) {
								if (needrem) {
									sbproto2net.append("rem ");
								}

							}
							// if (needrem) {//不生成pbbin文件了
							sbproto2net.append("rem ");
							// }
							sbproto2net.append("\"../protobufs/protoc\" --descriptor_set_out=\"./")
									.append(f.getName().subSequence(0, f.getName().indexOf("."))).append(".pbbin\"")
									.append(" \"./").append(f.getName()).append("\"\r\n");
						}
					}
				}
			}

			sbproto2net.append("cd ../protobufs\r\n");
			GameCSV2DB.appendFile(new File(proto2netbatDir, "proto2netbat.bat").getAbsolutePath(),
					sbproto2net.toString());
		}
	}

	public static void protoCsvDir2Proto(String testProtocsvsDir) {
		File csvDir = new File(testProtocsvsDir);
		File scvFiles[] = csvDir.listFiles();
		for (int i = 0; scvFiles != null && i < scvFiles.length; i++) {
			File csvFile = scvFiles[i];
			if (csvFile.isFile() && csvFile.getName().toLowerCase().endsWith(".csv")
					&& !csvFile.getName().contains("__")) {
				// 和维光沟通了，约定下划线开头的表格表示是策划本地计算测试用的表格，数据不入库
				protoCsvFile2Proto(csvFile);
			}
			if (csvFile.isDirectory()) {
				protoCsvDir2Proto(csvFile.getAbsolutePath());
			}
		}
	}

	public static void protoCsvFile2Proto(File csvFile) {
		if (csvFile == null) {
			return;
		}
		if (csvFile.isDirectory()) {
			protoCsvDir2Proto(csvFile.getAbsolutePath());
		}
		String csvName = csvFile.getName();
		if (csvName.endsWith("ProtoBufRequest.csv") || csvName.endsWith("ProtoBufResponse.csv")) {
			// System.err.println("protoCsvFile2Proto for:" + csvFile);
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			String className = csvName.substring(0, csvName.indexOf("."));
			String protoFileName = className + ".proto";
			StringBuilder sb = new StringBuilder();
			sb.append("package " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "."
					+ csvFile.getParentFile().getName().toLowerCase() + ";").append("\n\n");

			sb.append("option optimize_for = SPEED;\n");
			sb.append("option java_generate_equals_and_hash = true;\n");
			sb.append("option java_outer_classname = \"" + className + "OuterClass\";\n");
			if (colList.size() > 3) {
				// 第三行第一列是用来放proto的import信息.
				sb.append("\n\n").append(colList.get(2)[0]).append("\n\n");
			}
			if (csvName.endsWith("ProtoBufResponse.csv")) {// 应答包把请求包潜规则带回去
				String reqClassName = StringUtil.replaceAll(csvName, ".csv", "");
				reqClassName = StringUtil.replaceAll(reqClassName, "ProtoBufResponse", "ProtoBufRequest");
				sb.append("import \"" + reqClassName + ".proto\";\n\n");
			}
			// 插入注释说明

			String moduleinfoCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
					+ "/Public/Common/TComm(基础配置)_Moduleinfo(模块信息).csv";
			List<String[]> moduleInfoColList = CSVUtil.getDataFromCSV2(moduleinfoCsvPath);
			Map<String, String> moduleInfoMap = new HashMap<String, String>();
			for (int k = 5; k < moduleInfoColList.size(); k++) {
				String[] s2 = moduleInfoColList.get(k);
				moduleInfoMap.put(CSVUtil.getColValue("module_name", s2, moduleInfoColList),
						CSVUtil.getColValue("module_title", s2, moduleInfoColList));
			}

			String protoCmdCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/Public/World/TProto(前后台数据协议)_Cmd(命令字).csv";
			List<String[]> cmdInfoColList = CSVUtil.getDataFromCSV2(protoCmdCsvPath);
			Map<String, String> cmdInfoMap = new HashMap<String, String>();
			for (int k = 5; k < cmdInfoColList.size(); k++) {
				String[] s2 = cmdInfoColList.get(k);
				cmdInfoMap.put(
						CSVUtil.getColValue("cmdMod", s2, cmdInfoColList)
								+ CSVUtil.getColValue("cmdEnumName", s2, cmdInfoColList),
						CSVUtil.getColValue("cmdDesc", s2, cmdInfoColList));
			}

			sb.append("// ").append(
					cmdInfoMap.get(className.replaceAll("ProtoBufRequest", "").replaceAll("ProtoBufResponse", "")));
			if (className.endsWith("ProtoBufRequest")) {
				sb.append(" 的 请求包 ");
			} else {
				sb.append(" 的 应答包 ");

			}
			sb.append(" \n");
			sb.append("message ").append(className).append("{\n");
			List<String> colNameList = new ArrayList<String>();
			for (int i = 5; i < colList.size(); i++) {
				String[] s = colList.get(i);
				colNameList.add(s[3]);
			}
			if (colNameList.size() > 1 && hasRepeatCol(colNameList.toArray(new String[0]), csvFile)) {
				addErrMailMsgList("ERROR:hasRepeatCol" + colNameList + "|for|" + csvFile);
				GameCSV2DB.sendMailAndExit();
			}

			for (int i = 5; i < colList.size(); i++) {
				String[] s = colList.get(i);
				String defVal = s[4];
				if (defVal != null && defVal.trim().length() > 0) {
					if (s[2].trim().contains("str")) {
						defVal = defVal.replace('\"', ' ').replace('\'', ' ').trim();
						defVal = "\"" + defVal + "\"";
					} else {
						defVal = defVal.trim();
						if (defVal.toLowerCase().endsWith("f")) {
							defVal = defVal.substring(0, defVal.length() - 2);
						}
					}
				}
				String protoType = s[2];
				if ("int".equalsIgnoreCase(protoType)) {
					protoType = "int32";
				} else if ("long".equalsIgnoreCase(protoType)) {
					protoType = "int64";
				}
				String defaultValueStr = "";
				if (defVal != null && defVal.length() > 0) {
					defaultValueStr = " [default = " + defVal + "]";
				}
				sb.append("\t").append(s[1]).append(" ").append(protoType).append(" ").append(s[3]).append(" = ")
						.append(s[0]).append(defaultValueStr).append("; //").append(s[5]).append("\n");
			}

			if (csvName.endsWith("ProtoBufResponse.csv")) {// 应答包把请求包潜规则带回去
				String reqClassName = StringUtil.replaceAll(csvName, ".csv", "");
				reqClassName = StringUtil.replaceAll(reqClassName, "ProtoBufResponse", "ProtoBufRequest");
				sb.append("\toptional " + reqClassName + " orgReqObj = 199; //对应当前应答的原始请求参数信息\n");
			}
			sb.append("}").append("\n");
			File protoFileTmp = new File(new File(protoFileRootDir, csvFile.getParentFile().getName()), protoFileName);
			// if (protoFileTmp.exists()) {
			writeFile(protoFileTmp.getAbsolutePath(), sb.toString());
			// }
			CSVUtil.protoBufCSV2ProtobufReqValidatorFile(csvFile);
			// 为每个requst和response的proto生成单独可以运行的bat进行编译校验。
			genProtoCheckBat(csvFile);
		} else {
			// /// System.out.println(csvFile + " is not protobuf csv");
		}
	}

	private static void genProtoCheckBat(File csvFile) {
		StringBuilder sb = new StringBuilder();
		sb.append("cd D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_configs\\protobufs\r\n");
		sb.append("D:\\mgamedev\\tools\\jdk1.8.0_20\\bin\\java.exe -Dfile.encoding=UTF-8 -classpath "
				+ "\"D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\classes;"
				+ "D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-beanutils-1.9.2.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-codec-1.9.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-collections-3.2.1.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-csv-1.1.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-fileupload-1.3.1.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-httpclient-3.1.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-io-2.3.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-lang-2.4.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-lang3-3.3.2.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-logging-1.2.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\commons-pool2-2.0.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\mysql-connector-java-5.1.35-bin.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\svnkit-1.8.8.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\svnkit-cli-1.8.8.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\svnkit-javahl16-1.8.8.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\fmpp-0.9.14.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\freemarker-2.3.19.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\jdom-2.0.4-sources.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\jdom-2.0.4.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\jaxen-1.1.6.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\bsh-2.0b5.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\org_json-chargebee-1.0-SNAPSHOT.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\oro-2.0.8.jar;D:\\mgamedev\\workspace\\gecaoshoulie_proj\\gecaoshoulie_codegentool\\WEB-INF\\lib\\pinyin4j-2.5.0.jar\" com.lizongbo.codegentool.csv2db.GenProtoBufcsvTask\r\n");
		sb.append("copy /Y .\\" + csvFile.getParentFile().getName() + "\\*.proto ..\\protofilestmp\\\r\n");
		sb.append("protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/"
				+ csvFile.getParentFile().getName() + "*.proto\r\n");
		sb.append("pause\r\n");
		writeFile(new File(csvFile.getParentFile(), csvFile.getParentFile().getName() + "protobufcheck.bat")
				.getAbsolutePath(), sb.toString());

	}

	public static void csvDir2DB(String testcsvsDir) {
		File csvDir = new File(testcsvsDir);
		File scvFiles[] = csvDir.listFiles();
		// TODO 预先读取表缓存全部数据
		for (int i = 0; scvFiles != null && i < scvFiles.length; i++) {
			File csvFile = scvFiles[i];
			if (csvFile.isFile() && csvFile.getName().toLowerCase().endsWith(".csv")
					&& !csvFile.getName().contains("__")) {
				// 和维光沟通了，约定下划线开头的表格表示是策划本地计算测试用的表格，数据不入库
				csvFile2DB(csvFile);
			}
			if (csvFile.isDirectory()) {
				csvDir2DB(csvFile.getAbsolutePath());
			}
		}
	}

	public static boolean hasRepeatCol(String[] colNames, File csvFile) {
		if (colNames == null || colNames.length < 1) {
			System.err.println("Error:no colNames ,must have one more" + Arrays.toString(colNames) + "|" + csvFile);
			MailTest.sendErrorMail("ERROR:hasRepeatCol|for|" + csvFile,
					csvFile + "|no colNames|" + Arrays.toString(colNames));
			return true;
		}
		Set<String> s = new HashSet<String>();
		for (String cn : colNames) {
			String cc = cn.trim().toLowerCase();
			if (cc.length() > 0 && s.contains(cc)) {

				System.err.println("Error:colNames " + cn + " is repeated big error!!" + Arrays.toString(colNames) + "|"
						+ csvFile);
				List<String> list = Arrays.asList(colNames);
				Collections.sort(list);
				MailTest.sendErrorMail("ERROR:hasRepeatCol|for|" + csvFile, csvFile + "|ERROR:hasRepeatCol|" + list);
				return true;
			} else {
				s.add(cc);
			}
		}
		return false;
	}

	private static Set<String> csvFileSet = new TreeSet<String>();

	public static void csvFile2DB(File csvFile) {
		if (csvFile == null) {
			return;
		}
		if (csvFile.isDirectory()) {
			csvDir2DB(csvFile.getAbsolutePath());
		}
		if (!csvFile.getName().toLowerCase().startsWith("t")) {
			return;
		}
		if (csvFileSet.contains(csvFile.getAbsolutePath())) {
			System.err.println("csvFile2DB|csvFileSet.contains|" + csvFile);
			return;
		}
		csvFileSet.add(csvFile.getAbsolutePath());
		CSVUtil.locale2CsFile(csvFile);
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		if (tableName == null || tableName.length() < 1) {
			return;
		}
		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		CSVUtil.genCsvCheckerClass(csvFile.getAbsolutePath());
		CsvCheckerManager.CheckCsv(csvFile.getAbsolutePath());
		if (tableName.length() > 0 && colList != null && colList.size() > 3) {
			// 检查表得字段名是不是有重复，如果有，程序直接退出并警告是excel配置错了。
			String[] colNames = colList.get(0);
			if (hasRepeatCol(colNames, csvFile)) {
				addErrMailMsgList("ERROR:hasRepeatCol" + Arrays.toString(colNames) + "|for|" + csvFile);
				GameCSV2DB.sendMailAndExit();
			}
			if (!sbDropTableMap.containsKey(tablePrefix)) {
				sbDropTableMap.put(tablePrefix, new StringBuilder());
				sbCreateTableMap.put(tablePrefix, new StringBuilder());
				sbInsertTableMap.put(tablePrefix, new StringBuilder());
			}
			String dropTableSql = genDropTableSql(tableName);
			sbDropTable.append(dropTableSql).append("\n");
			sbDropTableMap.get(tablePrefix).append(dropTableSql).append("\n");
			String createTableSql = genCreateTableSql(tableName, tableCmt, colList);
			sbCreateTable.append(createTableSql).append("\n");
			sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
			// System.out.println("createTableSql == " + createTableSql);
			List<Pair<String, String>> rsqlList = genReplaceInsertSql(tablePrefix, tableName, colList);
			List<Pair<String, String>> usqlList = genUpdateSql(tableName, colList);

			Connection conn = getDbCoon();
			boolean needDelTable = isDiffTableDescbyCsvAndTable(csvFile.getAbsolutePath(), conn);
			Set<String> dfDataIdSet = new HashSet<String>();
			List<DiffInfo> dfList = genDiffbyCsvAndTable(csvFile.getAbsolutePath(), conn);
			for (DiffInfo df : dfList) {
				// System.out.println("DiffInfo==" + df + "|" + tableName);
				dfDataIdSet.add(df.getFid());
			}
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				stmt.execute("set sql_mode= 'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO';");
				int rs = -99;
				if (needDelTable) {
					rs = stmt.executeUpdate(dropTableSql);
					System.out.println("needDelTable|dropTableSql|rs=" + rs + "|forsql|" + dropTableSql);
					rs = stmt.executeUpdate(createTableSql);
					System.out.println("needDelTable|createTableSql|rs=" + rs + "|forsql|" + createTableSql);
				}
				for (int i = 0; i < usqlList.size(); i++) {
					if (dfDataIdSet.contains(usqlList.get(i).getFirst())) {
						String sql4u = usqlList.get(i).getSecond();
						try {
							rs = stmt.executeUpdate(sql4u);
							// System.out.println("needUpdate|rsql|rs=" + rs +
							// "|forsql|" + sql4u);
							if (rs < 1) {// no record for update ,so need insert
								String sql4ri = rsqlList.get(i).getSecond();
								rs = stmt.executeUpdate(sql4ri);
								// System.out.println("nodata|NeedInsert|rsql|rs="
								// + rs + "|forsql|" + sql4ri);
							}
						} catch (Exception ex11) {
							System.err.println(sql4u);
							ex11.printStackTrace();
						}
					} else {
						// // System.out.println("notneedUpdate|for|" +
						// // usqlList.get(i).getFirst());
					}
					// 补丁一下针对第一次插入的数据，批量更新一下addedtime
					// String sql4AddedTime = " update " + tableName
					// + " set added_time_long=lastupdated_time_long where
					// added_time_long=19700101112233 or added_time_long=0";
					// rs = stmt.executeUpdate(sql4AddedTime);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeSqlConnStmtRs(null, stmt, null);
			}
			GameCSV2DB.closeSqlConnStmtRs(conn, null, null);
			genProtoFile(csvFile, tableName, colList);
			// genUnityMotionStateMachineBehaviourFile(csvFile, tableName,
			// colList);
			genCommonChannelEnumProtoFile(csvFile, tableName, colList);
			genCommonErrorcodeEnumProtoFile(csvFile, tableName, colList);
			/// genCommonErrorcodeUtilCsFile(csvFile, tableName, colList);
			genJavaCommonConfKeyConstsFile(csvFile, tableName, colList);
			genRedisPoolConfigFile(csvFile, tableName, colList);
			genMysqlDBPoolConfigFile(csvFile, tableName, colList);
			genUnityModulecs(csvFile, tableName, colList);
			CSVUtil.genSkillStatusEnumFile(csvFile, tableName, colList);
			CSVUtil.genSkillModelActionEnumFile(csvFile, tableName, colList);
			CSVUtil.genSkillStatusUtilFile(csvFile, tableName, colList);
			genUnityLogicScenecs(csvFile, tableName, colList);
			genUnityLogicScenecShowParams(csvFile, tableName, colList);
			// genUnityLogicScenecArray(csvFile, tableName, colList);
			tableColMap.put(tableName, colList);
			if ("TModel_Actiondescinfo".equalsIgnoreCase(tableName)) {// 遍历模型需求表生成提需求用的文件夹目录
				colList4TModelActiondescinfo = colList;
			}
			if ("TModel_Actionreqinfo".equalsIgnoreCase(tableName)) {// 遍历模型需求表生成提需求用的文件夹目录
				colList4TModellActionreqinfo = colList;
			}

			if ("tmodel_effectreqinfo".equalsIgnoreCase(tableName)) {// 遍历模型特效需求表生成提需求用的文件夹目录
				colList4TModellEffectreqinfo = colList;
			}
			if ("Tui_ComponentDefineInfo".equalsIgnoreCase(tableName)) {
				if (colList4UIComponentDefineinfo == null) {
					colList4UIComponentDefineinfo = colList;
				} else {
					colList4UIComponentDefineinfo.addAll(colList.subList(4, colList.size()));
				}
			}
			if ("Tui_ComponentCommon".equalsIgnoreCase(tableName)) {
				if (colList4UIComponentDefineinfo == null) {
					colList4UIComponentDefineinfo = colList;
				} else {
					colList4UIComponentDefineinfo.addAll(colList.subList(4, colList.size()));
				}
			}
			if ("TSkill_Skillinfo".equalsIgnoreCase(tableName)) {
				colList4TskillSkillinfo4Proto = colList;
			}
			if ("TSkill_Mainskillunitinfo".equalsIgnoreCase(tableName)) {
				colList4TskillMainskillunitinfo4Proto = colList;
			}

		} else {
			System.out.println(csvFile + "|tableName=" + tableName + " is not table csv ,try for protobuf csv");

		}
		// System.out.println(tableName);
		// System.out.println(dropTableSql);
		// System.out.println(createTableSql);
		// System.out.println(rsql);
	}

	/**
	 * 针对命令字触发的事件，基于配置来批量生成对应的ServerEvent模板
	 * 
	 * @param tableName
	 * @param colList
	 */
	private static void genServerEventFromProtobufCmd(String tableName, List<String[]> colList) {

		Map<String, Set<String>> cmdMap = new HashMap<String, Set<String>>();
		for (int i = 4; i < colList.size(); i++) {

			StringBuilder sb = new StringBuilder();
			String[] s = colList.get(i);
			String modName = CSVUtil.getColValue("cmdMod", s, colList).toLowerCase();
			String eventClassName = CSVUtil.capFirst(CSVUtil.getColValue("cmdEnumName", s, colList)) + "Event";
			String packageName = "net.bilinkeji.gecaoshoulie.events." + modName;
			String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
			String javaFileDir = protoRpcJavaServiceCodeRootDir + "/../protojavarpcimpls" + "/"
					+ packageName.replace('.', '/') + "/";
			String cmdDesc = CSVUtil.getColValue("cmdDesc", s, colList).toLowerCase();
			String javaFileName = eventClassName + ".java";

			String genServerEvent = CSVUtil.getColValue("genServerEvent", s, colList).toLowerCase();
			sb.append("package net.bilinkeji.gecaoshoulie.events." + modName + ";\n");
			sb.append("\n");
			sb.append("import com.google.common.base.MoreObjects;\n");
			sb.append("import net.bilinkeji.gecaoshoulie.events.ServerEvent;\n");
			sb.append("\n");
			sb.append("/**\n");
			sb.append(" * " + cmdDesc + "  成功触发的事件\n");
			sb.append(" * \n");
			sb.append(" * @author quickli\n");
			sb.append(" *\n");
			sb.append(" */\n");
			String pClass = "ServerEvent";
			if (modName.equalsIgnoreCase("herogrow")) {
				pClass = "MechaEvent";
			}
			String cmdReqCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/"
					+ CSVUtil.getColValue("cmdMod", s, colList) + "/" + CSVUtil.getColValue("cmdMod", s, colList)
					+ CSVUtil.getColValue("cmdEnumName", s, colList) + "ProtoBufRequest.csv";

			List<String[]> reqColList = CSVUtil.getDataFromCSV2(cmdReqCsvPath);

			sb.append("public class " + eventClassName + " extends " + pClass + " {\n");
			for (int k = 5; k < reqColList.size(); k++) {
				String[] s2 = reqColList.get(k);
				if (!("mechaId".equalsIgnoreCase(s2[3]) && "MechaEvent".equalsIgnoreCase(pClass))) {
					sb.append("	/**\n");
					sb.append("	 * " + s2[5] + "\n");
					sb.append("	 */\n");
					sb.append("protected " + getJavaTypebyProtoType(s2[2], s2[1]) + " " + DBUtil.camelName(s2[3])
							+ ";\n");
				}
			}
			for (int k = 5; k < reqColList.size(); k++) {
				String[] s2 = reqColList.get(k);
				if (!("mechaId".equalsIgnoreCase(s2[3]) && "MechaEvent".equalsIgnoreCase(pClass))) {
					sb.append("	public " + getJavaTypebyProtoType(s2[2], s2[1]) + " get"
							+ CSVUtil.capFirst(DBUtil.camelName(s2[3])) + "() {\n");
					sb.append("		return " + DBUtil.camelName(s2[3]) + ";\n");
					sb.append("	}\n");
					sb.append("\n");
					sb.append("	public void set" + CSVUtil.capFirst(DBUtil.camelName(s2[3])) + "("
							+ getJavaTypebyProtoType(s2[2], s2[1]) + " " + DBUtil.camelName(s2[3]) + ") {\n");
					sb.append("		this." + DBUtil.camelName(s2[3]) + " = " + DBUtil.camelName(s2[3]) + ";\n");
					sb.append("	}\n");
				}
			}

			sb.append("public static " + eventClassName + " new" + eventClassName + "(int zoneId, int playerId");
			// 在这里补参数

			for (int k = 5; k < reqColList.size(); k++) {
				String[] s2 = reqColList.get(k);
				sb.append(", " + getJavaTypebyProtoType(s2[2], s2[1]) + " " + DBUtil.camelName(s2[3]));
			}

			sb.append(") {\n");
			sb.append("	" + eventClassName + " event = new " + eventClassName + "();\n");
			sb.append("	event.setZoneId(zoneId);\n");
			sb.append("	event.setPlayerId(playerId);\n");
			for (int k = 5; k < reqColList.size(); k++) {
				String[] s2 = reqColList.get(k);
				sb.append("	event.set" + CSVUtil.capFirst(DBUtil.camelName(s2[3])) + "(" + DBUtil.camelName(s2[3])
						+ ");\n");
			}
			// 在这里补设置
			sb.append("	return event;\n");
			sb.append("}\n");
			sb.append("\n");

			sb.append("@Override\n");
			sb.append("public String toString() {\n");
			sb.append("	 return MoreObjects.toStringHelper(this)\n");
			sb.append("		        .add(\"zoneId\", zoneId)\n");
			sb.append("		        .add(\"playerId\", playerId)\n");
			sb.append("		        .add(\"eventTime\", eventTime)\n");
			for (int k = 5; k < reqColList.size(); k++) {
				String[] s2 = reqColList.get(k);
				sb.append("		        .add(\"" + DBUtil.camelName(s2[3]) + "\", " + DBUtil.camelName(s2[3]) + ")\n");
			}
			sb.append("		        .toString();\n");
			sb.append("}\n");
			sb.append("}\n");
			sb.append("\n");

			File protoFile = new File(javaFileDir, javaFileName);
			if (genServerEvent.trim().length() > 0) {
				String cmdMod = CSVUtil.getColValue("cmdMod", s, colList);
				Set<String> cmdSet = cmdMap.get(cmdMod);
				if (cmdSet == null) {
					cmdSet = new TreeSet<String>();
				}
				cmdSet.add(eventClassName);
				cmdMap.put(cmdMod, cmdSet);
			}
			if (genServerEvent.trim().length() > 0 && !protoFile.exists()) {
				GameCSV2DB.writeFile(protoFile.getAbsolutePath(), sb.toString());
			} else {
				System.out.println(protoFile + " exists so ignore it" + "|genServerEvent=" + genServerEvent);
			}

		}

		for (Map.Entry<String, Set<String>> e : cmdMap.entrySet()) {
			String packageName = "net.bilinkeji.gecaoshoulie.events." + e.getKey().toLowerCase();
			String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
			String javaFileDir = protoRpcJavaServiceCodeRootDir + "/../protojavarpcimpls" + "/"
					+ packageName.replace('.', '/') + "/";
			String javaFileName = "Defalut" + e.getKey() + "ServerEventListener.java";
			StringBuilder sb = new StringBuilder();
			sb.append("package net.bilinkeji.gecaoshoulie.events." + e.getKey().toLowerCase() + ";\n");
			sb.append("\n");
			sb.append("import com.google.common.eventbus.Subscribe;\n");
			sb.append("\n");
			sb.append("import net.bilinkeji.common.log.LoggerWraper;\n");
			sb.append("\n");
			sb.append("/**\n");
			sb.append(" * " + e.getKey() + " 模块的默认事件处理监听器，代码是模板，仅供参考，便于copy\n");
			sb.append(" * \n");
			sb.append(" * @author quickli\n");
			sb.append(" *\n");
			sb.append(" */\n");
			sb.append("public class Defalut" + e.getKey() + "ServerEventListener {\n");
			sb.append("	private static LoggerWraper log = LoggerWraper.getLogger(\"Defalut" + e.getKey()
					+ "ServerEventListener\");\n");
			sb.append("\n");
			for (String s : e.getValue()) {
				sb.append("	@Subscribe\n");
				sb.append("	public void deal" + s + "(" + s + " event) {\n");
				sb.append("		//log.debug(\"deal" + s + "|\" + event);\n");
				sb.append("	}\n");
			}
			sb.append("\n");
			sb.append("}\n");
			File protoFile = new File(javaFileDir, javaFileName);
			if (true || !protoFile.exists()) {
				GameCSV2DB.writeFile(protoFile.getAbsolutePath(), sb.toString());
			} else {
				System.out.println(protoFile + " exists so ignore it");
			}
		}

	}

	private static void genProtobufCmdXMLFile(String tableName, List<String[]> colList) {
		String xmlFileName = protorpcCmdTableName.toLowerCase() + "_" + CodeGenConsts.PROJNAME + ".xml";
		String xmlFileDir = CodeGenConsts.fmppDir4ProtoCmd + "/templates/data";
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<protoCmds>\n");
		Map<String, List<String[]>> modMap = new HashMap<String, List<String[]>>();
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String modName = s[1];
			List<String[]> modCols = modMap.get(modName);
			if (modCols == null) {
				modCols = new ArrayList<String[]>();
				modMap.put(modName, modCols);
			}
			modCols.add(s);
		}
		for (Map.Entry<String, List<String[]>> me : modMap.entrySet()) {
			List<String[]> modCols = me.getValue();
			sb.append("\t<cmdMod>\n");
			sb.append("\t<modName>").append(me.getKey()).append("</modName>\n");
			sb.append("\t<modNamePackage>").append(me.getKey().toLowerCase()).append("</modNamePackage>\n");
			sb.append("\t<modNamePrefix>").append(me.getKey().toUpperCase()).append("</modNamePrefix>\n");

			for (int i = 0; i < modCols.size(); i++) {
				String[] s = modCols.get(i);

				String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();

				// 2017-07-12 linyaoheng 捕捉幽灵关键字
				if (cmdEnum.contains("SHOP_BUYGIFTPACK")) {
					new Throwable().printStackTrace();
					System.exit(1);
				}

				sb.append("\t<protoCmd>\n");
				sb.append("\t<cmdEnum><![CDATA[").append(cmdEnum).append("]]></cmdEnum>\n");
				sb.append("\t<cmdId><![CDATA[").append(s[0]).append("]]></cmdId>\n");

				sb.append("\t<cmdMod><![CDATA[").append(s[1]).append("]]></cmdMod>\n");
				sb.append("\t<cmdEnumName><![CDATA[").append(s[2]).append("]]></cmdEnumName>\n");

				sb.append("\t<cmdDesc><![CDATA[").append(s[3]).append("]]></cmdDesc>\n");

				sb.append("\t<needLogin><![CDATA[").append(s[4]).append("]]></needLogin>\n");

				sb.append("\t<amdAuthor><![CDATA[").append(s[5]).append("]]></amdAuthor>\n");

				sb.append("\t<creatertime><![CDATA[").append(s[6]).append("]]></creatertime>\n");

				sb.append("\t<cmdCmt><![CDATA[").append(s[7]).append("]]></cmdCmt>\n");

				// cmdId cmdMod cmdEnumName
				// cmdDesc needLogin amdAuthor creatertime cmdCmt

				sb.append("\t</protoCmd>\n");

			}
			sb.append("\t</cmdMod>\n");

		}

		sb.append("</protoCmds>\n");
		writeFile(new File(xmlFileDir, xmlFileName).getAbsolutePath(), sb.toString());

		xmlFileDir = CodeGenConsts.fmppDir4ProtoCmd2Unity + "/templates/data";

		writeFile(new File(xmlFileDir, xmlFileName).getAbsolutePath(), sb.toString());

	}

	private static void genCommonErrorcodeEnumProtoFile(File csvFile, String tableName, List<String[]> colList) {
		if (protoCommErrorCodeTableName.equalsIgnoreCase(tableName)) {// 特殊表潜规则生成proto文件提交到svn.把渠道做成常量定义
			{
				String protoFileName = "CommErrorCodeEnum.proto";
				StringBuilder sb = new StringBuilder();
				// String packageName =
				// CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT
				// + ".common";
				sb.append("package " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".common;").append("\n\n");
				sb.append("enum CommErrorCode {\n");
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					// if ("0".equals(s[4]))
					{
						// repeated MapEntry clientInfo = 1; //字符串keyvalue方式的信息
						// optional bool gzipData = 4 [default = false];
						/*
						 * sb.append("\toptional string ").append(s[2]).append(
						 * " = " ) .append(s[0]).append("; //" + s[1] + "\n");
						 */
						sb.append("ERR_" + CSVUtil.getColValue("errcode_enum", s, colList) + " = "
								+ CSVUtil.getColValue("errcode", s, colList) + ";//"
								+ CSVUtil.getColValue("errorcode_desc", s, colList)
								+ CSVUtil.getColValue("rtx", s, colList) + "\n\n");
					}
				}
				sb.append("}").append("\n");

				String javaFileDir = CodeGenConsts.PROJPROTOFILE_DIRROOT + "/";
				writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());

			}
		}
	}

	private static void genCommonErrorcodeUtilCsFile(File csvFile, String tableName, List<String[]> colList) {
		if (protoCommErrorCodeTableName.equalsIgnoreCase(tableName + "xxxxxxx")) {// 特殊表潜规则生成proto文件提交到svn.把渠道做成常量定义
			{
				String protoFileName = "CommErrorCodeUtil.cs";
				StringBuilder sb = new StringBuilder();
				sb.append("using UnityEngine;\n");
				sb.append("using System.Collections;\n");
				sb.append("using net.bilinkeji.gecaoshoulie.mgameprotorpc.common;\n");
				sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.common;\n");
				sb.append("\n");
				sb.append("namespace net.bilinkeji.common.util\n");
				sb.append("{\n");
				sb.append("public class CommErrorCodeUtil\n");
				sb.append("{\n");
				sb.append("		public static bool errMsgWithErrCode = true;\n");
				sb.append("\n");
				sb.append("\n");
				sb.append("		/// <summary>\n");
				sb.append("		/// 根据出错信息获取需要显示的错误信息字符串，如果没有自定义出错信息，则从本地根据错误码获取通用出错信息字符串\n");
				sb.append("		/// </summary>\n");
				sb.append("		/// <returns>The comm error message.</returns>\n");
				sb.append("		/// <param name=\"commErrorInfo\">Comm error info.</param>\n");
				sb.append("		public static string getCommErrorMsg (CommErrorInfo commErrorInfo)\n");
				sb.append("		{\n");
				sb.append("			string str = \"err\";\n");
				sb.append("			if (commErrorInfo != null) {\n");
				sb.append(
						"				if (string.IsNullOrEmpty (commErrorInfo.errMsg)  || commErrorInfo.errMsg.Length < 2 ) {\n");
				sb.append("					str = getCommErrorMsg (commErrorInfo.errCode);\n");
				sb.append("				} else {\n");
				sb.append("					str = commErrorInfo.errMsg;\n");
				sb.append("				}\n");
				sb.append("			}\n");
				sb.append("			return str;\n");
				sb.append("		}\n");

				sb.append("	public static string getCommErrorMsg (int commErrorCodeValue)\n");
				sb.append("	{\n");
				sb.append("		string str = \"\";\n");
				sb.append("		switch (commErrorCodeValue) {\n");

				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					{
						sb.append("		case (int)CommErrorCode.ERR_" + CSVUtil.getColValue("errcode_enum", s, colList)
								+ ":\n");
						sb.append("			{\n");
						sb.append("				str = \"" + CSVUtil.getColValue("errcode_msg", s, colList) + "\";\n");
						sb.append("				break;\n");
						sb.append("			}\n");
					}
				}

				sb.append("		default:\n");
				sb.append("			{\n");
				sb.append("				str = \"错误码：\" + commErrorCodeValue;\n");
				sb.append("				break;\n");
				sb.append("			}\n");
				sb.append("			\n");
				sb.append("		}\n");
				sb.append("		if (errMsgWithErrCode) {\n");
				sb.append("			str = str + \" (ret:\" + commErrorCodeValue + \")\";\n");
				sb.append("		}\n");
				sb.append("		return str;\n");
				sb.append("	}\n");
				sb.append("}\n");
				sb.append("}\n");

				String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT + "/../Bilinkeji/Util/";
				writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());

			}
		}
	}

	private static void genUnityLogicScenecShowParams(File csvFile, String tableName, List<String[]> colList) {
		if (gameLogicSceneInfoTableName.equalsIgnoreCase(tableName)) {// 生成unity客户端的逻辑场景代码
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String sceneName = CSVUtil.getColValue("scene_name", s, colList);
					String moduleName = CSVUtil.getColValue("module_name", s, colList);
					String unity_scene_name = CSVUtil.getColValue("unity_scene_name", s, colList);
					String amFileName = "Abstract" + moduleName + sceneName + "LogicSceneShowParam.cs";
					String mFileName = moduleName + sceneName + "LogicSceneShowParam.cs";

					StringBuilder sb2 = new StringBuilder();
					sb2.append("using UnityEngine;\n");
					sb2.append("using System.Collections;\n");
					sb2.append("using net.bilinkeji.common;\n");
					sb2.append(
							"using net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase() + ".abstracts;\n");
					sb2.append("\n");
					sb2.append("namespace net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase() + "\n");
					sb2.append("{\n");
					sb2.append("	public class " + moduleName + sceneName + "LogicSceneShowParam : Abstract"
							+ moduleName + sceneName + "LogicSceneShowParam\n");
					sb2.append("	{\n");
					sb2.append("\n");
					sb2.append("\n");
					sb2.append("	}\n");
					sb2.append("}\n");

					StringBuilder sb = new StringBuilder();
					sb.append("using UnityEngine;\n");
					sb.append("using System.Collections;\n");
					sb.append("using net.bilinkeji.common;\n");
					sb.append("\n");
					sb.append("namespace net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase()
							+ ".abstracts\n");
					sb.append("{\n");
					sb.append("	public abstract class Abstract" + moduleName + sceneName
							+ "LogicSceneShowParam : LogicSceneShowParam\n");
					sb.append("	{\n");
					sb.append("	}\n");
					sb.append("}\n");

					String javaFileDir = CodeGenConsts.PROJUNITY_MODULE_SRCROOT + "/" + moduleName.toLowerCase();
					String javaFileDir2 = CodeGenConsts.PROJUNITY_MODULE_SRCROOT + "/" + moduleName.toLowerCase()
							+ "/abstracts";
					writeFile(new File(javaFileDir2, amFileName).getAbsolutePath(), sb.toString());
					if (!new File(javaFileDir, mFileName).exists()) {
						writeFile(new File(javaFileDir, mFileName).getAbsolutePath(), sb2.toString());
					}
				}
			}

		}
	}

	private static void genUnityLogicScenecs(File csvFile, String tableName, List<String[]> colList) {
		if (gameLogicSceneInfoTableName.equalsIgnoreCase(tableName)) {// 生成unity客户端的逻辑场景代码
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String sceneName = CSVUtil.getColValue("scene_name", s, colList);
					String moduleName = CSVUtil.getColValue("module_name", s, colList);
					String unity_scene_name = CSVUtil.getColValue("unity_scene_name", s, colList);
					String amFileName = "Abstract" + moduleName + sceneName + "LogicScene.cs";
					String mFileName = moduleName + sceneName + "LogicScene.cs";

					StringBuilder sb2 = new StringBuilder();
					sb2.append("using UnityEngine;\n");
					sb2.append("using System.Collections;\n");
					sb2.append("using net.bilinkeji.common;\n");
					sb2.append("using net.bilinkeji.gecaoshoulie.mgameprotorpc.common;\n");
					sb2.append(
							"using net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase() + ".abstracts;\n");
					sb2.append("\n");
					sb2.append("namespace net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase() + "\n");
					sb2.append("{\n");
					sb2.append("	public class " + moduleName + sceneName + "LogicScene : Abstract" + moduleName
							+ sceneName + "LogicScene\n");
					sb2.append("	{\n");
					sb2.append("\n");
					sb2.append("\n");
					sb2.append("		public void loadUnityRes ()\n");
					sb2.append("		{\n");
					sb2.append("			BLDebug.Log(this+\".loadUnityRes\");\n");
					sb2.append("		}\n");
					sb2.append("\n");
					sb2.append("		public override LogicSceneShowResult showSceneInternal(" + moduleName
							+ sceneName + "LogicSceneShowParam showParam)\n");
					sb2.append("		{\n");
					sb2.append("			BLDebug.Log(this+\".showSceneInternal|\"+showParam);\n");
					sb2.append("		    LogicSceneShowResult lssr = new LogicSceneShowResult();\n");
					sb2.append("		    lssr.showResult = true;\n");
					sb2.append("		    return lssr ;\n");
					sb2.append("		}\n");
					sb2.append("	}\n");
					sb2.append("}\n");

					StringBuilder sb = new StringBuilder();
					sb.append("using UnityEngine;\n");
					sb.append("using System.Collections;\n");
					sb.append("using net.bilinkeji.common;\n");
					sb.append("using net.bilinkeji.gecaoshoulie.mgameprotorpc.common;\n");
					sb.append("\n");
					sb.append("namespace net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase()
							+ ".abstracts\n");
					sb.append("{\n");
					sb.append("	public abstract class Abstract" + moduleName + sceneName + "LogicScene : LogicScene\n");
					sb.append("	{\n");
					sb.append("		protected GameObject uiRootWindow = null;\n");
					sb.append("		\n");
					sb.append("		protected bool isSceneLoadDone = false;\n");
					sb.append("		\n");
					sb.append("		protected bool isSceneReady4UI = false;\n");
					sb.append("		\n");

					sb.append("		public bool IsSceneLoadDone ()\n");
					sb.append("		{\n");
					sb.append("			return isSceneLoadDone;\n");
					sb.append("		}\n");

					sb.append("		public bool IsSceneReady4UI ()\n");
					sb.append("		{\n");
					sb.append("			return isSceneReady4UI;\n");
					sb.append("		}\n");

					sb.append("		public string getUnitySceneName ()\n");
					sb.append("		{\n");
					sb.append("			return \"" + unity_scene_name + "\";\n");
					sb.append("		}\n");

					sb.append("\n");
					sb.append("		public string getUnityUIPrefabDir ()\n");
					sb.append("		{\n");
					sb.append("			return \"forClient/ui/" + moduleName.toLowerCase() + "/\";\n");
					sb.append("		}\n");
					sb.append("\n");
					sb.append("		public void loadUnityRes ()\n");
					sb.append("		{\n");
					sb.append("			BLDebug.Log(this+\".loadUnityRes\");\n");
					sb.append("		}\n");
					sb.append("\n");
					sb.append("		public LogicSceneShowResult showScene (LogicSceneShowParam showParam)\n");
					sb.append("		{\n");
					sb.append("			BLDebug.Log(this+\".showScene|\"+showParam);\n");

					sb.append("					if (showParam!=null && showParam is " + moduleName + sceneName
							+ "LogicSceneShowParam) {\n");
					sb.append("						" + moduleName + sceneName + "LogicSceneShowParam p = ("
							+ moduleName + sceneName + "LogicSceneShowParam)showParam;\n");
					sb.append("						return showSceneInternal (p);\n");
					sb.append("					}\n");
					sb.append("		return null;\n");
					sb.append("		}\n");

					sb.append("					public virtual LogicSceneShowResult showSceneInternal (" + moduleName
							+ sceneName + "LogicSceneShowParam showParam)\n");
					sb.append("					{\n");
					sb.append("					    return null;\n");
					sb.append("					}\n");

					sb.append("					public virtual void onHideNotify ()\n");
					sb.append("					{\n");
					sb.append("						BLDebug.Log (this + \".onHideNotify\");\n");
					sb.append("						if (uiRootWindow != null) {\n");
					sb.append("							GameObject.Destroy (uiRootWindow);\n");
					sb.append("							uiRootWindow = null;\n");
					sb.append("						}\n");
					sb.append("					}\n");
					sb.append("	}\n");
					sb.append("}\n");

					String javaFileDir = CodeGenConsts.PROJUNITY_MODULE_SRCROOT + "/" + moduleName.toLowerCase();
					String javaFileDir2 = CodeGenConsts.PROJUNITY_MODULE_SRCROOT + "/" + moduleName.toLowerCase()
							+ "/abstracts";
					writeFile(new File(javaFileDir2, amFileName).getAbsolutePath(), sb.toString());
					new File(
							"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/UIRes/"
									+ moduleName + "/" + sceneName).mkdirs();
					new File(
							"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Resources/forClient/ui/"
									+ moduleName.toLowerCase()).mkdirs();
					if (!new File(javaFileDir, mFileName).exists()) {
						writeFile(new File(javaFileDir, mFileName).getAbsolutePath(), sb2.toString());
					}
				}
			}

		}
	}

	private static void genUnityModulecs(File csvFile, String tableName, List<String[]> colList) {
		if (gameModuleInfoTableName.equalsIgnoreCase(tableName)) {// 生成unity客户端的模块定义代码和场景定义代码
			// 还需要创建一张逻辑场景信息表，表名就叫 LogicScene
			genUnityModuleEnumFile(csvFile, tableName, colList);
			genUnityGameModuleFile(csvFile, tableName, colList);
			genUnityGameModuleUtilFile(csvFile, tableName, colList);
		}
	}

	private static void genUnityGameModuleUtilFile(File csvFile, String tableName, List<String[]> colList) {
		if (gameModuleInfoTableName.equalsIgnoreCase(tableName)) {// 生成模块定义的枚举

			String protoFileName = "GameModuleUtil.cs";
			StringBuilder sb = new StringBuilder();
			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("using net.bilinkeji.common;\n");
			sb.append("using net.bilinkeji.gecaoshoulie.mgameprotorpc.common;\n");
			sb.append("using System.Collections.Generic;\n");
			sb.append("using net.bilinkeji.gecaoshoulie.modules.common;\n");

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					sb.append("using net.bilinkeji.gecaoshoulie.modules."
							+ CSVUtil.getColValue("module_name", s, colList).toLowerCase() + ";\n");
				}
			}

			sb.append("namespace net.bilinkeji.gecaoshoulie.modules\n");
			sb.append("{\n");
			sb.append("\n");
			sb.append("public class GameModuleUtil\n");
			sb.append("{\n");
			sb.append("	private static Dictionary<string,GameModule> gmDic = new Dictionary<string,GameModule> ();\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					sb.append("public const string moduleName_" + CSVUtil.getColValue("module_name", s, colList)
							+ " = \"" + CSVUtil.getColValue("module_name", s, colList) + "\";\n");
				}
			}

			sb.append("\n");
			sb.append("	public static GameModule getGameModule (GameModuleType gmt)\n");
			sb.append("	{\n");
			sb.append("		GameModule gm = null;\n");
			sb.append("		string key = \"\" + gmt;\n");
			sb.append("		gmDic.TryGetValue (key, out gm);\n");
			sb.append("		if (gm != null) {\n");
			sb.append("			return gm;\n");
			sb.append("		}\n");
			sb.append("		switch (gmt) {\n");

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					sb.append("		case GameModuleType.GM_" + CSVUtil.getColValue("module_name", s, colList) + ":\n");
					sb.append("			gm = new " + CSVUtil.getColValue("module_name", s, colList)
							+ "GameModule ();\n");
					sb.append("			break;\n");
					sb.append("\n");
				}
			}
			sb.append("\n");
			sb.append("		default:\n");
			sb.append("			break;\n");
			sb.append("		}\n");
			sb.append("		gmDic [key] = gm;\n");
			sb.append("		return gm;\n");
			sb.append("	}\n");

			sb.append("		public static Dictionary<string,string> getModuleTitlemap ()\n");
			sb.append("		{\n");
			sb.append("			Dictionary<string,string> moduleTitleMap = new Dictionary<string,string> ();\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					sb.append("			moduleTitleMap [moduleName_Common] = \""
							+ CSVUtil.getColValue("module_title", s, colList) + "\";\n");
				}
			}
			sb.append("			return moduleTitleMap;\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("		public static GameModuleType getModuleTypeByName (string moduleName)\n");
			sb.append("		{\n");
			sb.append("			GameModuleType gmt = GameModuleType.GM_Common;\n");
			sb.append("			switch (moduleName) {\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					sb.append("			case moduleName_" + CSVUtil.getColValue("module_name", s, colList) + ":\n");
					sb.append("				gmt = GameModuleType.GM_" + CSVUtil.getColValue("module_name", s, colList)
							+ ";\n");
					sb.append("				break;\n");

				}
			}

			sb.append("			default:\n");
			sb.append("				break;\n");
			sb.append("			}\n");
			sb.append("\n");
			sb.append("			return gmt;\n");
			sb.append("		}\n");

			// 这里需要遍历场景表
			String logicScenecsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
					+ "/Public/Common/TComm(基础配置)_Logicsceneinfo(逻辑场景信息).csv";
			List<String[]> logicSceneColListOrg = CSVUtil.getDataFromCSV2(logicScenecsvPath);

			List<String[]> logicSceneColList = new ArrayList<String[]>();
			for (int i = 0; i < 4 && i < logicSceneColListOrg.size(); i++) {
				String[] s = logicSceneColListOrg.get(i);
				{
					logicSceneColList.add(s);
				}
			}

			for (int i = 4; i < logicSceneColListOrg.size(); i++) {
				String[] s = logicSceneColListOrg.get(i);
				{
					if ("1".equals(CSVUtil.getColValue("debug_menu", s, logicSceneColListOrg).trim())) {
						logicSceneColList.add(s);
					} else {
					}
				}
			}

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					boolean hasLogicScene = false;
					StringBuilder sbTmp = new StringBuilder();
					if (logicSceneColList != null) {
						for (int j = 4; j < logicSceneColList.size(); j++) {
							String[] s2 = logicSceneColList.get(j);
							{
								String moduleNameTmp = CSVUtil.getColValue("module_name", s2, logicSceneColList);

								String moduleNameTmp2 = CSVUtil.getColValue("module_name", s, colList);

								if (moduleNameTmp != null && moduleNameTmp.equals(moduleNameTmp2)) {// 是模块内的场景
									String scene_name = CSVUtil.getColValue("scene_name", s2, logicSceneColList);
									String scene_title = CSVUtil.getColValue("scene_title", s2, logicSceneColList);
									sbTmp.append("		{ \"" + scene_name + "\", \"" + scene_title + "\" },\n");
									hasLogicScene = true;
								} else {
								}
							}
						}
					}
					if (hasLogicScene) {
						sb.append("public static 	string[,] logicScenes4"
								+ CSVUtil.getColValue("module_name", s, colList) + " = new string[,] { \n");
						sb.append(sbTmp);
						sb.append("	};\n");

					} else {
						System.err.println("GameModuleUtil=====666666" + Arrays.toString(s));
					}
				}
			}

			sb.append("public static Dictionary<string,string[,]> getAllLogicScene4Menus ()\n");
			sb.append("{\n");
			sb.append("	Dictionary<string,string[,]> moduleMenuMap = new Dictionary<string,string[,]> ();\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					boolean hasLogicScene = false;
					if (logicSceneColList != null) {
						for (int j = 4; j < logicSceneColList.size(); j++) {
							String[] s2 = logicSceneColList.get(j);
							{
								String moduleNameTmp = CSVUtil.getColValue("module_name", s2, logicSceneColList);
								if (moduleNameTmp != null
										&& moduleNameTmp.equals(CSVUtil.getColValue("module_name", s, colList))) {// 是模块内的场景
									hasLogicScene = true;
								}
							}
						}
					}
					if (hasLogicScene) {

						sb.append("	moduleMenuMap [moduleName_" + CSVUtil.getColValue("module_name", s, colList)
								+ "] = logicScenes4" + CSVUtil.getColValue("module_name", s, colList) + ";\n");
					}
				}
			}
			sb.append("	return moduleMenuMap;\n");
			sb.append("\n");
			sb.append("}\n");

			sb.append("}\n");

			sb.append("}\n");
			sb.append("\n");

			String javaFileDir = CodeGenConsts.PROJUNITY_MODULE_SRCROOT;
			writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}
	}

	/**
	 * 生成模块定义的通用代码
	 * 
	 * @param csvFile
	 * @param tableName
	 * @param colList
	 */
	private static void genUnityGameModuleFile(File csvFile, String tableName, List<String[]> colList) {
		if (gameModuleInfoTableName.equalsIgnoreCase(tableName)) {// 生成模块定义的枚举
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String moduleName = CSVUtil.getColValue("module_name", s, colList);
					String amFileName = "Abstract" + moduleName + "GameModule.cs";
					String mFileName = moduleName + "GameModule.cs";

					StringBuilder sb2 = new StringBuilder();
					sb2.append("using UnityEngine;\n");
					sb2.append("using System.Collections;\n");
					sb2.append("using net.bilinkeji.common;\n");
					sb2.append("using net.bilinkeji.gecaoshoulie.mgameprotorpc.common;\n");
					sb2.append(
							"using net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase() + ".abstracts;\n");
					sb2.append("\n");
					sb2.append("namespace net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase() + "\n");
					sb2.append("{\n");
					sb2.append(
							"	public class " + moduleName + "GameModule : Abstract" + moduleName + "GameModule\n");
					sb2.append("	{\n");
					sb2.append("\n");
					sb2.append("	}\n");
					sb2.append("}\n");
					StringBuilder sb = new StringBuilder();
					sb.append("using UnityEngine;\n");
					sb.append("using System.Collections;\n");
					sb.append("using net.bilinkeji.common;\n");
					sb.append("using net.bilinkeji.gecaoshoulie.mgameprotorpc.common;\n");
					sb.append("using System.Collections.Generic;\n");
					sb.append("\n");
					sb.append("namespace net.bilinkeji.gecaoshoulie.modules." + moduleName.toLowerCase()
							+ ".abstracts\n");
					sb.append("{\n");
					sb.append("	public class Abstract" + moduleName + "GameModule : GameModule\n");
					sb.append("	{\n");

					sb.append(
							"	  protected Dictionary<string,LogicScene> showedSceneMap = new Dictionary<string, LogicScene> ();\n");
					// 这里需要遍历场景表
					String logicScenecsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
							+ "/Public/Common/TComm(基础配置)_Logicsceneinfo(逻辑场景信息).csv";
					List<String[]> logicSceneColList = CSVUtil.getDataFromCSV2(logicScenecsvPath);
					StringBuilder sb3 = new StringBuilder();
					StringBuilder sb4 = new StringBuilder();
					StringBuilder sb5 = new StringBuilder();
					StringBuilder sb6 = new StringBuilder();
					StringBuilder sb7 = new StringBuilder();
					if (logicSceneColList != null) {
						for (int j = 4; j < logicSceneColList.size(); j++) {
							String[] s2 = logicSceneColList.get(j);
							{
								String moduleNameTmp = CSVUtil.getColValue("module_name", s2, logicSceneColList);
								if (moduleNameTmp != null && moduleNameTmp.equals(moduleName)) {// 是模块内的场景
									String scene_name = CSVUtil.getColValue("scene_name", s2, logicSceneColList);
									String unity_scene_name = CSVUtil.getColValue("unity_scene_name", s2,
											logicSceneColList);
									sb.append("		public const string LS_" + scene_name.toUpperCase() + " = \""
											+ scene_name + "\";\n");
									sb3.append("			hs.Add (LS_" + scene_name.toUpperCase() + ");\n");
									sb4.append("	case LS_" + scene_name.toUpperCase() + ":\n");
									sb4.append("		return new " + moduleName + scene_name + "LogicScene ();\n");

									sb5.append("			if (LS_" + scene_name.toUpperCase()
											+ ".Equals (logicSceneName)) {\n");
									sb5.append("				return \"" + unity_scene_name + "\";\n");
									sb5.append("			}\n");

									sb6.append("			if (LS_" + scene_name.toUpperCase()
											+ ".Equals (logicSceneName)) {\n");
									sb6.append("				return new " + moduleName + scene_name
											+ "LogicSceneShowParam ();\n");
									sb6.append("			}\n");

									sb7.append("					if (LS_" + scene_name.toUpperCase()
											+ ".Equals (logicSceneName)) {\n");
									sb7.append("						LogicScene ls = new " + moduleName + scene_name
											+ "LogicScene ();\n");
									sb7.append("						showResult = ls.showScene (showParam);\n");
									sb7.append("						if (showResult.showResult) {\n");
									sb7.append("							showedSceneMap[logicSceneName] = ls;\n");
									sb7.append(
											"							GecaoshoulieGameManager.Instance.currentLogicScene = ls;\n");
									sb7.append(
											"							GecaoshoulieGameManager.Instance.currentLogicSceneShowParam = showParam;\n");

									sb7.append("						}\n");

									sb7.append("					}\n");
								}
							}
						}
					}
					// sb.append(" const string SCENE_SPLASH = \"Splash\";\n");
					sb.append("\n");
					sb.append("		public virtual string getMoudleName ()\n");
					sb.append("		{\n");
					sb.append("			return \"" + moduleName + "GameModule\";\n");
					sb.append("		}\n");
					sb.append("\n");
					sb.append("		public System.Collections.Generic.HashSet<string> getSupportedLogicSceneList ()\n");
					sb.append("		{\n");
					sb.append("			HashSet<string> hs = new HashSet<string> ();\n");
					sb.append(sb3);
					sb.append("			return hs;\n");
					sb.append("		}\n");
					sb.append("\n");
					sb.append(
							"		public virtual string getUnityScenebyLogicSceneName (string logicSceneName, LogicSceneShowParam showParam)\n");
					sb.append("		{\n");
					sb.append(sb5);
					sb.append("			return \"\";\n");
					sb.append("		}\n");

					sb.append("public LogicScene newLogicScene (string logicSceneName)\n");
					sb.append("{\n");
					sb.append("	switch (logicSceneName) {\n");
					sb.append(sb4);
					sb.append("	default:\n");
					sb.append("		return null;\n");
					sb.append("\n");
					sb.append("	}\n");
					sb.append("	//return null;\n");
					sb.append("}\n");
					sb.append(
							"		public virtual LogicSceneShowResult showLogicScene (string logicSceneName, LogicSceneShowParam showParam)\n");
					sb.append("		{\n");
					sb.append("					LogicSceneShowResult showResult = null;\n");

					sb.append(sb7);
					sb.append("					if (showResult == null) {\n");
					sb.append("						showResult = new LogicSceneShowResult ();\n");
					sb.append("						showResult.showResult = false;\n");
					sb.append("					}\n");
					sb.append("					return showResult;		\n");
					sb.append("		}\n");

					sb.append("		public LogicSceneShowParam newLogicSceneShowParam (string logicSceneName)\n");
					sb.append("		{\n");
					sb.append(sb6);
					sb.append("			return null;\n");
					sb.append("		}\n");

					sb.append(
							"		public void onHiddenLogicScene (string logicSceneName, LogicSceneShowParam showParam)\n");
					sb.append("		{\n");
					sb.append("			LogicScene ls = null;\n");
					sb.append("			if (showedSceneMap.TryGetValue (logicSceneName, out ls)) {\n");
					sb.append("				ls.onHideNotify ();\n");
					sb.append("				showedSceneMap.Remove (logicSceneName);\n");
					sb.append("			}\n");
					sb.append("		}\n");

					sb.append("	}\n");
					sb.append("}\n");

					String javaFileDir = CodeGenConsts.PROJUNITY_MODULE_SRCROOT + "/" + moduleName.toLowerCase();
					String javaFileDir2 = CodeGenConsts.PROJUNITY_MODULE_SRCROOT + "/" + moduleName.toLowerCase()
							+ "/abstracts";
					writeFile(new File(javaFileDir2, amFileName).getAbsolutePath(), sb.toString());
					if (!new File(javaFileDir, mFileName).exists()) {
						writeFile(new File(javaFileDir, mFileName).getAbsolutePath(), sb2.toString());
					}
				}
			}

		}
	}

	/**
	 * 生成游戏模块枚举定义
	 * 
	 * @param csvFile
	 * @param tableName
	 * @param colList
	 */
	private static void genUnityModuleEnumFile(File csvFile, String tableName, List<String[]> colList) {
		if (gameModuleInfoTableName.equalsIgnoreCase(tableName)) {// 生成模块定义的枚举

			String protoFileName = "GameModuleEnum.proto";
			StringBuilder sb = new StringBuilder();
			sb.append("package " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".common;").append("\n\n");
			sb.append("enum GameModuleType {\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					sb.append("GM_" + CSVUtil.getColValue("module_name", s, colList) + " = "
							+ CSVUtil.getColValue("id", s, colList) + ";//"
							+ CSVUtil.getColValue("module_title", s, colList)
							+ CSVUtil.getColValue("module_desc", s, colList) + "\n\n");
					genBillOpHelperJavaFile(CSVUtil.getColValue("module_name", s, colList));
				}
			}
			sb.append("}").append("\n");

			String javaFileDir = CodeGenConsts.PROJPROTOFILE_DIRROOT + "/";
			writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}
	}

	public static void genBillOpHelperJavaFile(String game_module_name) {
		StringBuilder sb = new StringBuilder();
		sb.append("package net.bilinkeji.gecaoshoulie.mgameprotorpc.billophelpers;\n");
		sb.append("\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.common.GameModuleEnum.GameModuleType;\n");
		sb.append("\n");
		sb.append("public class " + game_module_name + "BillOpHelper extends BillOpHelper {\n");
		sb.append("\n");
		sb.append("	@Override\n");
		sb.append("	public GameModuleType getGameModuleType() {\n");
		sb.append("		return GameModuleType.GM_" + game_module_name + ";\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public static " + game_module_name + "BillOpHelper newInstance() {\n");
		sb.append("		return new " + game_module_name + "BillOpHelper();\n");
		sb.append("	}\n");
		sb.append("}\n");
		String filePath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_server/WEB-INF/protojavarpcimpls/net/bilinkeji/gecaoshoulie/mgameprotorpc/billophelpers/"
				+ game_module_name + "BillOpHelper.java";
		writeFile(filePath, sb.toString());

	}

	private static void genMysqlDBPoolConfigFile(File csvFile, String tableName, List<String[]> colList) {
		if (gameZoneTableName.equalsIgnoreCase(tableName)) {// 根据游戏区的表，生成mysql
															// db pool的配置
			{
				System.err.println("run|genMysqlDBPoolConfigFile");
				StringBuilder sb4HostName = new StringBuilder();
				StringBuilder sb4Createdb = new StringBuilder();
				String redisCfgTempfile = "/com/lizongbo/codegentool/templeles/mysqldbpool_cfg.properties";
				URL redisCfgTempUrl = GameCSV2DB.class.getResource(redisCfgTempfile);
				String cfgTxtTemp = GenAll.readFile(redisCfgTempUrl.getFile(), "UTF-8");
				String redisCfgcolNames[] = new String[] { "mysql_server_maindb", "mysql_server_log",
						"mysql_server_stat" };
				for (int i = 0; i < redisCfgcolNames.length; i++) {
					String redisCfgColName = redisCfgcolNames[i];
					for (int j = 4; j < colList.size(); j++) {
						String cfgTxt = cfgTxtTemp;
						String[] colValues = colList.get(j);
						String redisServerUrl = CSVUtil.getColValue(redisCfgColName, colValues, colList);
						if (redisServerUrl != null && redisServerUrl.length() > 0) {
							try {
								URI uri = new URI(redisServerUrl);
								String mysqldbHostName = redisCfgColName.replaceAll("_", "") + "4zone"
										+ CSVUtil.getColValue("zone_id", colValues, colList);
								cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "3306", "" + uri.getPort());
								if ("10.0.0.16".equals(uri.getHost())) {
									cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "localhost", mysqldbHostName);
								} else {
									cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "localhost", "" + uri.getHost());
								}
								cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "mgamedb_gecaoshoulie",
										"" + uri.getPath().substring(1));
								cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "rootpwd", "mysqlpwdbilinkejinet");// 密码统一成这个

								sb4HostName.append(uri.getHost()).append(" ").append(mysqldbHostName).append("\n");

								sb4Createdb.append("create database " + uri.getPath().substring(1) + ";\n");
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
							String cfgDir = redisCfgColName.replaceAll("mysql_server_", "dbpool_4");
							String cfgFilename = cfgDir + "_zone" + CSVUtil.getColValue("zone_id", colValues, colList)
									+ ".properties";
							writeFile(new File(new File(CodeGenConsts.PROJ_MYSQLDB_CONFIG_ROOT, cfgDir), cfgFilename)
									.getAbsolutePath(), cfgTxt);
						} else {
							System.err.println("genMysqlDBPoolConfigFile|err|nodar|for:" + redisCfgColName + "|" + j);
						}
					}
				}
				writeFile(new File(CodeGenConsts.PROJ_MYSQLDB_CONFIG_ROOT, "mysqlhosts.txt").getAbsolutePath(),
						sb4HostName.toString());
				writeFile(new File(CodeGenConsts.PROJ_MYSQLDB_CONFIG_ROOT, "createdb.txt").getAbsolutePath(),
						sb4Createdb.toString());

			}
		}
	}

	private static void genRedisPoolConfigFile(File csvFile, String tableName, List<String[]> colList) {
		if (gameZoneTableName.equalsIgnoreCase(tableName)) {// 根据游戏区的表，生成redis的配置
			{
				StringBuilder sb4HostName = new StringBuilder();
				String redisCfgTempfile = "/com/lizongbo/codegentool/templeles/redispool_cfg.txt";
				URL redisCfgTempUrl = GameCSV2DB.class.getResource(redisCfgTempfile);
				String cfgTxtTemp = GenAll.readFile(redisCfgTempUrl.getFile(), "UTF-8");
				String redisCfgcolNames[] = new String[] { "redis_server_dbcache", "redis_server_counter",
						"redis_server_ranklist", "redis_server_common" };
				for (int i = 0; i < redisCfgcolNames.length; i++) {
					String redisCfgColName = redisCfgcolNames[i];
					for (int j = 4; j < colList.size(); j++) {
						String cfgTxt = cfgTxtTemp;
						String[] colValues = colList.get(j);
						String redisServerUrl = CSVUtil.getColValue(redisCfgColName, colValues, colList);
						if (redisServerUrl != null && redisServerUrl.length() > 0) {
							try {
								URI uri = new URI(redisServerUrl);
								String redisServerHostName = redisCfgColName.replaceAll("_", "") + "4zone"
										+ CSVUtil.getColValue("zone_id", colValues, colList);
								cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "redisServer.port = 6379",
										"redisServer.port = " + uri.getPort());
								if ("10.0.0.16".equals(uri.getHost())) {
									cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "redisServer.host = 127.0.0.1",
											"redisServer.host = " + redisServerHostName);
								} else {
									cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "redisServer.host = 127.0.0.1",
											"redisServer.host = " + uri.getHost());
								}
								sb4HostName.append(uri.getHost()).append(" ").append(redisServerHostName).append("\n");
								cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "jmxNamePrefix=redispool4zone0",
										"jmxNamePrefix=redispool4" + redisCfgColName + "zone"
												+ CSVUtil.getColValue("zone_id", colValues, colList));
								cfgTxt = ServerContainerGenTool.replaceAll(cfgTxt, "jmxNameBase=redispool4zone0",
										"jmxNameBase=redispool4" + redisCfgColName + "zone"
												+ CSVUtil.getColValue("zone_id", colValues, colList));

								// 在这里把redis服务器的配置文件也生成出来
								{
									String redisServerConfObjPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/serverconfs/redis/usr/local/apps"
											+ "/redis_44002/conf/redis_44002.conf";
									int redisPort = uri.getPort();
									String redisConfTmpFile = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/redisconftemp/conf/redis_3.2.3.conf";
									String credisConfTxtTemp = GenAll.readFile(redisConfTmpFile, "UTF-8");
									credisConfTxtTemp = ServerContainerGenTool.replaceAll(credisConfTxtTemp, "44002",
											"" + redisPort);
									redisServerConfObjPath = ServerContainerGenTool.replaceAll(redisServerConfObjPath,
											"44002", "" + redisPort);
									writeFile(redisServerConfObjPath, credisConfTxtTemp);
								}

							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							String cfgDir = redisCfgColName.replaceAll("redis_server_", "redispool_4");
							String cfgFilename = cfgDir + "_zone" + CSVUtil.getColValue("zone_id", colValues, colList)
									+ ".properties";
							writeFile(new File(new File(CodeGenConsts.PROJ_REDIS_CONFIG_ROOT, cfgDir), cfgFilename)
									.getAbsolutePath(), cfgTxt);

						}
					}
				}
				writeFile(new File(CodeGenConsts.PROJ_REDIS_CONFIG_ROOT, "redishosts.txt").getAbsolutePath(),
						sb4HostName.toString());
			}
		}
	}

	private static void genCommonChannelEnumProtoFile(File csvFile, String tableName, List<String[]> colList) {
		if (protoCommChannelTableName.equalsIgnoreCase(tableName)) {// 特殊表潜规则生成proto文件提交到svn.把渠道做成常量定义
			{
				String protoFileName = "CommChannelCodeEnum.proto";
				StringBuilder sb = new StringBuilder();
				// String packageName =
				// CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT
				// + ".common";
				sb.append("package " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".common;").append("\n\n");
				sb.append("enum CommChannelCode {\n");
				sb.append("UNKNOW_CHANEL=0;//未登记的渠道 \n\n");
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					// if ("0".equals(s[4]))
					{
						// repeated MapEntry clientInfo = 1; //字符串keyvalue方式的信息
						// optional bool gzipData = 4 [default = false];
						/*
						 * sb.append("\toptional string ").append(s[2]).append(
						 * " = " ) .append(s[0]).append("; //" + s[1] + "\n");
						 */
						sb.append("CHANNEL_" + CSVUtil.getColValue("channel_enum", s, colList) + " = "
								+ CSVUtil.getColValue("channel_id", s, colList) + ";//"
								+ CSVUtil.getColValue("client_title", s, colList)
								+ CSVUtil.getColValue("channel_desc", s, colList) + "\n\n");
					}
				}
				sb.append("}").append("\n");

				String javaFileDir = CodeGenConsts.PROJPROTOFILE_DIRROOT + "/";
				writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());

			}
		}
	}

	/**
	 * 生成commonconf的java变量定义方式， 便于生成对应的代码信息, 还需要生成一个辅助的设置值的Helper类,另外还需要扩建一张表 叫
	 * TComm_Commconf4ZoneServer,用来存放各个服务器自己的自定义配置，,将默认值进行重载
	 * 
	 * @param csvFile
	 * @param tableName
	 * @param colList
	 */
	private static void genJavaCommonConfKeyConstsFile(File csvFile, String tableName, List<String[]> colList) {
		if (protoCommConfTableName.equalsIgnoreCase(tableName)) {// 特殊表潜规则生成proto文件提交到svn.
			// 先检测变量名的id有没有重复，有重复就强行停止
			{
				Set<String> setCheck = new HashSet<String>();
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					String cmdEnum = s[2].toLowerCase();
					if ("0".equals(s[4]) && setCheck.contains(cmdEnum)) {
						GameCSV2DB.addErrMailMsgList(csvFile + "|Error:JavaCommonConfKey|" + "repeated ：" + cmdEnum
								+ "|" + Arrays.toString(s));
						GameCSV2DB.sendMailAndExit();
					} else {
						setCheck.add(cmdEnum);
					}
				}
			}

			{
				Set<String> setCheck = new HashSet<String>();
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					String cmdEnum = s[0].trim().toLowerCase();
					if ("0".equals(s[4]) && setCheck.contains(cmdEnum)) {
						System.err.println(
								"Error:JavaCommonConfKey enumid repeated ：" + cmdEnum + "|" + Arrays.toString(s));
						GameCSV2DB.addErrMailMsgList(csvFile + "|Error:JavaCommonConfKey|enumid " + "repeated ："
								+ cmdEnum + "|" + Arrays.toString(s));
						GameCSV2DB.sendMailAndExit();
					} else {
						setCheck.add(cmdEnum);
					}
				}
			}
			{
				String protoFileName = "CommConfKeyEnum.java";
				StringBuilder sb = new StringBuilder();
				String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".common";
				sb.append("package " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".common;").append("\n\n");
				sb.append("public class CommConfKeyEnum {\n");
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					// if ("0".equals(s[4]))
					{
						// repeated MapEntry clientInfo = 1; //字符串keyvalue方式的信息
						// optional bool gzipData = 4 [default = false];
						/*
						 * sb.append("\toptional string ").append(s[2]).append(
						 * " = " ) .append(s[0]).append("; //" + s[1] + "\n");
						 */
						sb.append("public static final String "
								+ CSVUtil.getColValue("module_name", s, colList).toUpperCase() + "_"
								+ CSVUtil.getColValue("key_name", s, colList).toUpperCase() + " = \""
								// + CSVUtil.getColValue("module_name", s,
								// colList) + "_"
								+ CSVUtil.getColValue("key_name", s, colList) + "\";//" + s[1] + "\n\n");
					}
				}
				sb.append("}").append("\n");

				String javaFileDir = CodeGenConsts.PROJPROTO_JAVASRCROOT + "/" + packageName.replace('.', '/') + "/";
				writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());

			}
			{
				String protoFileName = "CommConfKeyEnum.cs";
				StringBuilder sb = new StringBuilder();
				String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".common";
				sb.append("namespace " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".common ").append("\n{\n");
				sb.append("\tpublic class CommConfKeyEnum \n\t{\n");
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					// if ("0".equals(s[4]))
					{
						// repeated MapEntry clientInfo = 1; //字符串keyvalue方式的信息
						// optional bool gzipData = 4 [default = false];
						/*
						 * sb.append("\toptional string ").append(s[2]).append(
						 * " = " ) .append(s[0]).append("; //" + s[1] + "\n");
						 */

						sb.append("\t\t/// <summary>\n");
						sb.append("\t\t/// " + s[1] + "\n");
						sb.append("\t\t/// </summary>\n");
						sb.append("\t\tpublic const string "
								+ CSVUtil.getColValue("module_name", s, colList).toUpperCase() + "_"
								+ CSVUtil.getColValue("key_name", s, colList).toUpperCase() + " = \""
								// + CSVUtil.getColValue("module_name", s,
								// colList) + "_"
								+ CSVUtil.getColValue("key_name", s, colList) + "\";\n\n");
					}
				}
				sb.append("\t}").append("\n");
				sb.append("}").append("\n");

				String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT + "/"
						+ packageName.replace('.', '/') + "/";
				writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());

			}
		}
	}

	private static void genUnityMotionStateMachineBehaviourFile(File csvFile, String tableName,
			List<String[]> colList) {
		if ("TModel_Actiondescinfo".equalsIgnoreCase(tableName)) {// 动作描述表
			StringBuilder sbTmp = new StringBuilder();
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				System.err.println("Motion ============" + s[0]);
				String umsmbTempfile = "/com/lizongbo/codegentool/templeles/UnityMotionStateMachineBehaviour.cs.txt";
				URL umsmbTempUrl = GameCSV2DB.class.getResource(umsmbTempfile);
				System.err.println(umsmbTempUrl);
				if (umsmbTempUrl != null) {
					System.out.println("umsmbTempUrl.getFile()=" + umsmbTempUrl.getFile());
					String csTxt = GenAll.readFile(umsmbTempUrl.getFile(), "UTF-8");
					csTxt = ServerContainerGenTool.replaceAll(csTxt, "${MotionClassName}", CSVUtil.capFirst(s[0]));
					csTxt = ServerContainerGenTool.replaceAll(csTxt, "${motionDesc}", (s[1]));

					GameCSV2DB.writeFile(CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
							+ "/net/bilinkeji/gecaoshoulie/motionstatemachinebehaviours/" + CSVUtil.capFirst(s[0])
							+ "MotionStateMachineBehaviour.cs", csTxt);
				}

				sbTmp.append("case \"" + s[0] + "\":	return typeof(" + CSVUtil.capFirst(s[0])
						+ "MotionStateMachineBehaviour); break;\n");
			}
			System.err.println(sbTmp);
		}
	}

	public static void genProtoFile(File csvFile, String tableNameOld, List<String[]> colList) {
		if (protorpcCmdTableName.equalsIgnoreCase(tableNameOld)) {// 特殊表潜规则生成proto文件提交到svn.

			String tableName = "TProto_Cmd";
			// 先检测命令字有没有重复，有重复就强行停止
			{
				Set<String> setCheck = new HashSet<String>();
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
					if (setCheck.contains(cmdEnum)) {
						System.err.println("Error:cmd repeated：" + cmdEnum + "|" + Arrays.toString(s));
						GameCSV2DB.addErrMailMsgList(
								csvFile + "|Error:cmd repeated " + " ：" + cmdEnum + "|" + Arrays.toString(s));
						GameCSV2DB.sendMailAndExit();
					} else {
						setCheck.add(cmdEnum);
					}
				}
			}

			{
				Set<String> setCheck = new HashSet<String>();
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					String cmdEnum = s[0].trim().toLowerCase();
					if (setCheck.contains(cmdEnum)) {
						System.err.println();
						System.err.println("big Error ---------------------------------------------");
						System.err.println("cmd enumid repeated：" + cmdEnum + "|" + Arrays.toString(s));
						System.err.println("big Error ---------------------------------------------");
						System.err.println("Error:");
						GameCSV2DB.addErrMailMsgList(
								csvFile + "|Error:enumid repeated" + " ：" + cmdEnum + "|" + Arrays.toString(s));

						GameCSV2DB.sendMailAndExit();
					} else {
						setCheck.add(cmdEnum);
					}
				}
			}
			String protoFileName = tableName + ".proto";
			StringBuilder sb = new StringBuilder();
			sb.append("package " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common;").append("\n\n");
			sb.append("//后台与客户端交互的命令字枚举\n");
			sb.append("enum ").append(tableName).append("{\n");
			sb.append("\tCOMMON_UNKNOWCMD = 0 ;\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
				sb.append("\t").append(cmdEnum).append(" = ").append(s[0])
						.append(";//" + s[3].replace('\n', ' ').replace('\r', ' ') + ",对于请求类为：" + s[1] + s[2]
								+ "ProtoBufRequest" + ",对应应答类为：" + s[1] + s[2] + "ProtoBufResponse" + "\n");
			}
			sb.append("}").append("\n");

			// 在这里插入datanotify枚举的生成
			appendDataNotifyEnum(sb);
			CSVUtil.appendSyncDataEnum(sb);
			// 在这里插入玩家计数器枚举的生成
			appendUserCounterEnum(sb);
			CSVUtil.appendPlayerActivityEnum(sb);
			CSVUtil.appendMoneyTypeEnum(sb);
			writeFile(new File(protoFileRootDir, protoFileName).getAbsolutePath(), sb.toString());
			genProtobufCmdXMLFile(tableName, colList);
			genServerEventFromProtobufCmd(tableName, colList);
			// System.exit(0);// 测试一下

			Map<String, List<String>> cmdModmap = new HashMap<String, List<String>>();

			Map<String, String> cmdModDescmap = new HashMap<String, String>();

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				String modName = s[1];
				String cmdName = s[1] + "" + s[2];
				List<String> cmdList = cmdModmap.get(modName);
				if (cmdList == null) {
					cmdList = new ArrayList<String>();
					cmdModmap.put(modName, cmdList);
				}
				cmdList.add(cmdName);
				cmdModDescmap.put(modName + "|" + cmdName, s[3].replace('\r', ' ').replace('\n', ' '));
			}

			genProtoBufProtoFile(cmdModmap);
			genProtoBufProtohandlerJavaFile(tableName, colList);
			genProtoBufProtoJavaServiceFile(cmdModmap, cmdModDescmap);
			genProtoBufProtoResponseSyncCsFile(cmdModmap, cmdModDescmap);
			genProtoBufProtoJavaAbstractServiceFile(cmdModmap);
			genProtoBufProtoJavaServiceImplFile(cmdModmap);
			genProtoBufProtoJavaLogicImplFile(cmdModmap, cmdModDescmap);
			String protoCmdCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/Public/World/TProto(前后台数据协议)_Cmd(命令字).csv";
			List<String[]> cmdInfoColList = CSVUtil.getDataFromCSV2(protoCmdCsvPath);
			Set<String> cmdTestExcludeInfoSet = new HashSet<String>();
			for (int k = 5; k < cmdInfoColList.size(); k++) {
				String[] s2 = cmdInfoColList.get(k);
				String cmdName = CSVUtil.getColValue("cmdMod", s2, cmdInfoColList)
						+ CSVUtil.getColValue("cmdEnumName", s2, cmdInfoColList);
				String isStandardTestExcluded = CSVUtil.getColValue("isStandardTestExcluded", s2, cmdInfoColList);
				if (null != isStandardTestExcluded && !"".equals(isStandardTestExcluded)
						&& Integer.parseInt(isStandardTestExcluded) > 0) {
					cmdTestExcludeInfoSet.add(cmdName);
				}
			}
			genProtoBufProtoJavaStandardClientTestFile(cmdModmap, cmdModDescmap, cmdTestExcludeInfoSet);
			genFullSizeCmdClientTest(cmdModmap, cmdModDescmap);
			genProtoBufProtoCsClientTestFile(cmdModmap);
			genAbstractProtoBufProtoJavaServiceManagerFile(tableName, colList);
			genProtoBuf2javaCmdFile(cmdModmap);
			genProtoBufDBBeansProtoFile(tableName, colList);

			genProtoBufCmdUtilJavaFile(tableName, colList);
			genProtoBufUtilLoginCheckUtilJavaFile(tableName, colList);

			genProtoBufProtoManagerCsFile(cmdModmap, cmdModDescmap);
			genProtoBufProtoNetCsFile(cmdModmap, cmdModDescmap);

		} else {// gen dbbeans proto
			genProtoBufDBBeansProtoFile(tableNameOld, colList);

			genProtoBufDBBeansProtoBufnetCSVHelperFile(csvFile, tableNameOld, colList);
		}
	}

	private static void appendUserCounterEnum(StringBuilder sb) {
		File csvFile = new File(CodeGenConsts.PROJCSVFILE_DIRROOT + "Public/Common/TComm(基础配置)_Counter(计数器信息).csv");

		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		sb.append("//玩家的计数器的枚举定义\n");
		sb.append("enum UserCounterType{\n");
		sb.append("\tCOMMON_UNKNOW_COUNTER = 0 ;\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = CSVUtil.getColValue("module_name", s, colList) + "_"
					+ CSVUtil.getColValue("counter_name", s, colList) + "_"
					+ CSVUtil.getColValue("counter_period", s, colList);
			cmdEnum = cmdEnum.toUpperCase();
			sb.append("\t").append(cmdEnum).append(" = ").append(CSVUtil.getColValue("counter_id", s, colList))
					.append(";//"
							+ CSVUtil.getColValue("counter_title", s, colList).replace('\n', ' ').replace('\r', ' ')
							+ "\n");
		}
		sb.append("}").append("\n");
	}

	private static void appendDataNotifyEnum(StringBuilder sb) {
		File csvFile = new File(
				CodeGenConsts.PROJCSVFILE_DIRROOT + "Public/World/TProto(前后台数据协议)_Datanotfiy(数据变更通知).csv");

		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		sb.append("//数据变更通知的枚举定义\n");
		sb.append("enum DataNotifyType{\n");
		sb.append("\tCOMMON_UNKNOW_DATANOTIFY = 0 ;\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_CHANGED";
			sb.append("\t").append(cmdEnum).append(" = ").append(s[0])
					.append(";//" + s[2].replace('\n', ' ').replace('\r', ' ') + "\n");
			String dnClassName = "DataNotify" + CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()));
			// 一起默认输出到一个指定的proto文件里

			String pp = new File(CodeGenConsts.PROJPROTOFILE_DIRROOT + "/Common/CommonDataNotify4Proto.proto")
					.getAbsolutePath();
			String CommonDataNotify4ProtoTxt = GenAll.readFile(pp, "UTF-8");
			if (!CommonDataNotify4ProtoTxt.contains(dnClassName)) {// 如果是新类，就插进去
				CommonDataNotify4ProtoTxt = CommonDataNotify4ProtoTxt + "\n//"
						+ s[2].replace('\n', ' ').replace('\r', ' ') + " 的数据信息,对应 " + cmdEnum;
				CommonDataNotify4ProtoTxt = CommonDataNotify4ProtoTxt + "\nmessage " + dnClassName + "{\n";
				CommonDataNotify4ProtoTxt = CommonDataNotify4ProtoTxt + "\n}\n";
				writeFile(pp, CommonDataNotify4ProtoTxt, "UTF-8");
			}

		}
		sb.append("}").append("\n");
		// 定义固定的类
		sb.append("//通知客户端做数据变更的具体数据信息\n");
		sb.append("message DataNotifyData{\n");
		sb.append("	optional DataNotifyType  dataNotifyType= 1 ;//数据变更类型\n");
		sb.append("	optional bytes  dataNotifyBytes= 2 ; //具体变更对象的pb字节数组\n");
		sb.append(" optional string  jsonDesc= 3 ; //debug模式下把变更数据用json存在这里，便于统一的toJson查看\n");
		sb.append("}\n");
		sb.append("\n");
		sb.append("//通知客户端做数据变更的具体信息数组\n");
		sb.append("message DataNotifyDataArray{\n");
		sb.append("	repeated DataNotifyData  notifys= 1 ;//0到多个数据变更信息\n");
		sb.append("}\n");
		genClientDataNotifyEventsCsFile(colList);
		genServerDataNotifyUtilJavaFile(colList);
	}

	private static void genServerDataNotifyUtilJavaFile(List<String[]> colList) {
		StringBuilder sb = new StringBuilder();

		sb.append("package net.bilinkeji.gecaoshoulie.mgameprotorpc.serverutils;\n");
		sb.append("\n");
		sb.append("import com.google.protobuf.MessageOrBuilder;\n");
		sb.append("\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.CommonDataNotify4Proto.*;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.TProtoCmd.DataNotifyData;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.TProtoCmd.DataNotifyType;\n");
		sb.append("import net.bilinkeji.utils.StringUtil;\n");
		sb.append("\n");
		sb.append("public class DataNotifyDataUtil {\n");
		sb.append("\n");
		sb.append("	public static DataNotifyData getDataNotifyData(MessageOrBuilder dataChange) {\n");
		sb.append("\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_CHANGED";
			String dnClassName = "DataNotify" + CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()));

			sb.append("\n");

			sb.append("		if (dataChange instanceof " + dnClassName + ") {\n");
			sb.append("			" + dnClassName + " event = (" + dnClassName + ") dataChange;\n");
			sb.append("			DataNotifyData dnd = DataNotifyData.newBuilder()\n");
			sb.append("					.setDataNotifyType(DataNotifyType." + cmdEnum + ")\n");
			sb.append("					.setDataNotifyBytes(event.toByteString()).build();\n");
			sb.append("			return dnd;\n");
			sb.append("		}\n");
			sb.append("		if (dataChange instanceof " + dnClassName + ".Builder) {\n");
			sb.append("			" + dnClassName + ".Builder builder = (" + dnClassName + ".Builder) dataChange;\n");
			sb.append("			DataNotifyData dnd = DataNotifyData.newBuilder()\n");
			sb.append("					.setDataNotifyType(DataNotifyType." + cmdEnum + ")\n");
			sb.append("					.setDataNotifyBytes(builder.build().toByteString())\n");
			sb.append("					.build();\n");
			sb.append("			return dnd;\n");
			sb.append("		}\n");
			sb.append("\n");

		}

		sb.append("\n");
		sb.append("		return null;\n");
		sb.append("\n");
		sb.append("	}\n");
		sb.append("}\n");
		String protoFileName = "DataNotifyDataUtil.java";
		String javaFileDir = CodeGenConsts.PROJSERVER_JAVASRCROOT + "/../basesrc/"
				+ "/net.bilinkeji.gecaoshoulie.mgameprotorpc.serverutils/".replace('.', '/');
		GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());

	}

	private static void genClientDataNotifyEventsCsFile(List<String[]> colList) {
		StringBuilder sb = new StringBuilder();

		sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.common;\n");
		sb.append("using net.bilinkeji.common.util;\n");
		sb.append("using ProtoBuf;\n");
		sb.append("\n");

		sb.append("/// <summary>\n");
		sb.append("/// 由代码生成的数据变更通知的事件名称常量，来自TProto(前后台数据协议)_Datanotfiy(数据变更通知).csv.\n");
		sb.append("///  by quickli\n");
		sb.append("/// </summary>\n");

		sb.append("public static class DataNotifyEvents\n");
		sb.append("{\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_CHANGED";
			String dnClassName = "DataNotify" + CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()));

			sb.append("\n");
			sb.append("	/// <summary>\n");
			sb.append("	/// 收到服务器通知的 " + s[2].replace('\n', ' ').replace('\r', ' ') + "的数据信息 之后的客户端回调事件\n");
			sb.append("	/// 参数{0 " + dnClassName + " " + s[2].replace('\n', ' ').replace('\r', ' ') + " 的数据信息}\n");
			sb.append("	/// </summary>\n");
			sb.append("	public const string On" + dnClassName + " = \"On" + dnClassName + "\";\n");
			sb.append("\n");

		}

		sb.append("\n");
		sb.append("	/// <summary>\n");
		sb.append("	/// 将打包的数据变更信息的字节数组解析出来\n");
		sb.append("	/// </summary>\n");
		sb.append("	/// <returns>解析后得到的对象.</returns>\n");
		sb.append("	/// <param name=\"dnd\">数据变更通知的信息</param>\n");
		sb.append("	public static IExtensible GetDataNotifyDataObject (DataNotifyData dnd)\n");
		sb.append("	{\n");
		sb.append("		if (dnd == null || dnd.dataNotifyBytes == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		sb.append("		switch (dnd.dataNotifyType) {\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_CHANGED";
			String dnClassName = "DataNotify" + CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()));
			sb.append("		case DataNotifyType." + cmdEnum + ":\n");
			sb.append("			return ProtoBufUtil.ProtoBufFromBytes<" + dnClassName + "> (dnd.dataNotifyBytes);\n");
		}
		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");
		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	/// <summary>\n");
		sb.append("	/// 获取数据变化信息的事件通知名称\n");
		sb.append("	/// </summary>\n");
		sb.append("	/// <returns>The data notify event name.</returns>\n");
		sb.append("	/// <param name=\"dnd\">Dnd.</param>\n");
		sb.append("	public static string GetDataNotifyEventName (DataNotifyData dnd)\n");
		sb.append("	{\n");
		sb.append("		if (dnd == null || dnd.dataNotifyBytes == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		sb.append("		switch (dnd.dataNotifyType) {\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_CHANGED";
			String dnClassName = "DataNotify" + CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()));
			sb.append("		case DataNotifyType." + cmdEnum + ":\n");
			sb.append("			return DataNotifyEvents.On" + dnClassName + ";\n");
		}
		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");
		sb.append("		return null;\n");
		sb.append("	}\n");

		sb.append("\n");
		sb.append("}\n");

		String pp = new File(CodeGenConsts.PROJUI_HANDLER_SRCROOT + "/../Bilinkeji/Common/DataNotifyEvents.cs")
				.getAbsolutePath();
		writeFile(pp, sb.toString());
	}

	private static void genProtoBufProtoFile(Map<String, List<String>> cmdModmap) {

		StringBuilder sbproto2net = new StringBuilder();
		sbproto2net.append("rem copy all *.proto to a folder ,then gen csharp code file.\r\n");
		sbproto2net.append(
				"for /f \"delims=\" %%i in ('dir /b /a-d /s \"*.proto\"') do copy  \"%%i\" \"../protofilestmp/%%~nxi\" /Y\r\n");
		sbproto2net.append("@rem first gen code for current dir\r\n");
		sbproto2net.append(
				"for /f \"delims=\" %%i in ('dir /b /a-d \"*.proto\"') do \"../protobufs/protogen\"  -i:%%~nxi -o:../protonets/%%~ni.cs\r\n");
		sbproto2net.append(
				"for /f \"delims=\" %%i in ('dir /b /a-d \"*.proto\"') do \"../protobufs/protoc\"  --descriptor_set_out=%%~ni.pbbin ./protonets/%%~nxi\r\n");

		sbproto2net.append("cd ../protofilestmp/\r\n");
		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String proto2netbatDir = protoFileRootDir;
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			{
				String className = modName + "ProtoBufService";
				String protoFileDir = protoFileRootDir + modName;
				String protoFileName = className + ".proto";

				StringBuilder sbImport = new StringBuilder();
				StringBuilder sb = new StringBuilder();
				String packageName = CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "." + modName.toLowerCase();
				sb.append("package " + packageName).append(";").append("\n\n");
				sb.append("option java_generate_equals_and_hash = true;\n");
				sb.append("option java_multiple_files = true;\n");
				sb.append("option java_generic_services = true;\n");
				sb.append("option java_outer_classname = \"" + className + "OuterClass\";\n");
				sb.append("service ").append(className).append("{\n");
				// sbproto2net.append("cd %protofileRootDir%/").append(modName)
				// .append("\n");
				// sbproto2net.append("echo currentdir=%cd%\n");

				sbproto2net.append("mkdir \"../protonets/").append(packageName.replace('.', '/')).append("\"\r\n");
				for (String cmdName : me.getValue()) {
					genProtoBufReuestProtoCSVFile(modName, cmdName);
					// genProtoBufReuestValidatorAbstractJavaFile(modName,
					// cmdName);
					// genProtoBufReuestValidatorJavaFile(modName, cmdName);
					genProtoBufResponseProtoCSVFile(modName, cmdName, cmdModmap);
					// 还可以生成对应的服务器端的Handler代码。
					// import "bcl.proto";
					// rpc dealCaiquan (CaiquanRequest) returns
					// (CaiquanResponse);
					String reqClassName = cmdName + "ProtoBufRequest";
					String respClassName = cmdName + "ProtoBufResponse";

					sbImport.append("import \"") // .append(modName).append("/")
							.append(reqClassName).append(".proto\";\n");
					sbImport.append("import \"") // .append(modName).append("/")
							.append(respClassName).append(".proto\";\n");
					sb.append("rpc deal").append(cmdName).append("(").append(reqClassName).append(") returns (")
							.append(respClassName).append(");\n");
					{// 检查md5是否有发生变化，如果没发生，则不运行生产c#代码的操作

						String zengliangFlag = System.getenv("zenglianggoujian");
						boolean needrem = false;
						if (zengliangFlag != null && "true".equalsIgnoreCase(zengliangFlag.trim())) {
							File f = new File(new File(CodeGenConsts.PROJPROTOFILE_DIRROOT + "/" + modName),
									reqClassName + ".proto");
							String fileMd5 = HashCalc.md5(f);
							String oldMd5 = GenAll.readFile(f.getAbsolutePath() + ".md5.txt", "UTF-8").trim();
							if (fileMd5.equalsIgnoreCase(oldMd5)) {// 如果md5相同则不执行生成
								System.err.println("md5notchange for:" + f);
								sbproto2net.append("echo \"notrun ").append("/")
										.append(f.getName().subSequence(0, f.getName().indexOf(".")))
										.append(".cs\"\r\n");
								needrem = true;
								// sbproto2net.append("rem ");
							} else {
								System.err.println("update md5 for:" + f + "|fileMd5=" + fileMd5 + "|oldMd5=" + oldMd5);
								writeFile(f.getAbsolutePath() + ".md5.txt", fileMd5);
							}
						}
						if (needrem) {
							sbproto2net.append("rem ");
						}
						sbproto2net.append("\"../protobufs/protogen\" -i:\"./").append(reqClassName).append(".proto\"")
								.append(" -o:\"../protonets/").append(packageName.replace('.', '/')).append("/")
								.append(reqClassName).append(".cs\"\r\n");
						if (needrem) {
							sbproto2net.append("rem ");
						}
						sbproto2net.append("\"../protobufs/protoc\" --descriptor_set_out=\"./").append(reqClassName)
								.append(".pbbin\"").append(" \"./").append(reqClassName).append(".proto\"\r\n");

					}
					{
						String zengliangFlag = System.getenv("zenglianggoujian");
						boolean needrem = false;
						if (zengliangFlag != null && "true".equalsIgnoreCase(zengliangFlag.trim())) {
							File f = new File(new File(CodeGenConsts.PROJPROTOFILE_DIRROOT + "/" + modName),
									respClassName + ".proto");
							String fileMd5 = HashCalc.md5(f);
							String oldMd5 = GenAll.readFile(f.getAbsolutePath() + ".md5.txt", "UTF-8").trim();
							if (fileMd5.equalsIgnoreCase(oldMd5)) {// 如果md5相同则不执行生成
								System.err.println("md5notchange for:" + f);
								sbproto2net.append("echo \"notrun ").append("/")
										.append(f.getName().subSequence(0, f.getName().indexOf(".")))
										.append(".cs\"\r\n");
								needrem = true;
								// sbproto2net.append("rem ");
							} else {
								System.err.println("update md5 for:" + f + "|fileMd5=" + fileMd5 + "|oldMd5=" + oldMd5);
								writeFile(f.getAbsolutePath() + ".md5.txt", fileMd5);
							}
						}
						if (needrem) {
							sbproto2net.append("rem ");
						}
						sbproto2net.append("\"../protobufs/protogen\" -i:\"./").append(respClassName).append(".proto\"")
								.append(" -o:\"../protonets/").append(packageName.replace('.', '/')).append("/")
								.append(respClassName).append(".cs\"\r\n");
						// if (needrem) {//不需要生成pbbin文件了
						sbproto2net.append("rem ");
						// }
						sbproto2net.append("\"../protobufs/protoc\" --descriptor_set_out=\"./").append(respClassName)
								.append(".pbbin\"").append(" \"./").append(respClassName).append(".proto\"\r\n");

					}

				}

				sb.append("}").append("\n");
				sbImport.append(sb);
				File protoFile = new File(protoFileDir, protoFileName);
				// if (!protoFile.exists())
				{
					writeFile(protoFile.getAbsolutePath(), sbImport.toString());
				}

			}
			{
				String className = modName + "Module4Proto";
				String protoFileDir = protoFileRootDir + modName;
				String protoFileName = className + ".proto";
				StringBuilder sb = new StringBuilder();
				sb.append("package net.bilinkeji.gecaoshoulie.mgameproto." + modName.toLowerCase() + ";\n");
				sb.append("\n");
				sb.append("//模块 ： " + modName + "\n");
				sb.append("option optimize_for = SPEED;\n");
				sb.append("option java_generate_equals_and_hash = true;\n");
				sb.append("\n");
				sb.append("import \"ProtobufRpcBase.proto\";\n");
				sb.append("import \"GecaoshoulieEnumDef.proto\";\n");

				File protoFile = new File(protoFileDir, protoFileName);
				if (!protoFile.exists()) {
					writeFile(protoFile.getAbsolutePath(), sb.toString());
				}
			}

		}

		sbproto2net.append("cd ../protobufs\r\n");
		writeFile(new File(proto2netbatDir, "proto2netbat.bat").getAbsolutePath(), sbproto2net.toString());
	}

	private static void genProtoBufProtoJavaServiceFile(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDescmap) {

		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String className = modName + "ProtoBufRPCService";
			String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".services";
			String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
			String javaFileName = className + ".java";

			StringBuilder sb = new StringBuilder();

			sb.append("package " + packageName + ";\n");
			sb.append("\n");
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".*;\n");

			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
					.append(".*;\n");

			for (String cmdName : me.getValue()) {
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";

				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(reqClassName).append("OuterClass.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(respClassName).append("OuterClass.*;\n");
			}

			sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".*;\n");
			sb.append("\n");
			sb.append("/**\n");
			sb.append(" * codegen by tool,do not edit \n");
			sb.append(" * \n");
			sb.append(" * @author lizongbo\n");
			sb.append(" *\n");
			sb.append(" */\n");
			sb.append("public interface ").append(className).append(" extends ProtoRpcService{\n");
			sb.append("\n");

			for (String cmdName : me.getValue()) {
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";
				sb.append("	/**\n");
				sb.append("	 * " + cmdModDescmap.get(modName + "|" + cmdName) + "\n");
				sb.append("	 * @param session RPCSession \n");
				sb.append("	 * @param req  \n");
				sb.append("	 * @param respHeader\n");
				sb.append("	 * @return\n");
				sb.append("	 */\n");
				sb.append("\t").append(respClassName);// .append(".").append(respClassName)
				sb.append(".Builder deal").append(cmdName).append("(");
				sb.append("RPCSession session, \n\t\t\t").append(reqClassName).append(" req,");
				sb.append("RPCHeader respHeader);\n");
			}

			sb.append("}\n");

			writeFile(new File(javaFileDir, javaFileName).getAbsolutePath(), sb.toString());

		}
	}

	private static void genProtoBufProtoResponseSyncCsFile(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDescmap) {

		String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT + "/../Bilinkeji/Common/";
		String javaFileName = "SyncServerDataEnable.cs";
		StringBuilder sb = new StringBuilder();

		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			sb.append("using net.bilinkeji.gecaoshoulie.mgameproto." + me.getKey().toLowerCase() + ";\n");
		}
		sb.append("\n");
		sb.append("namespace Bilinkeji.Common\n");
		sb.append("{\n");
		sb.append("	public abstract class SyncServerDataEnable\n");
		sb.append("	{\n");
		sb.append("\n");
		sb.append("\n");

		sb.append("		public void syncServerDataFromProtoBufResponse (global::ProtoBuf.IExtensible resp)\n");
		sb.append("		{\n");
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {

			for (String cmdName : me.getValue()) {
				String respClassName = cmdName + "ProtoBufResponse";
				/// if (respClassName.equals("LoginbyGuestProtoBufResponse")
				/// ||
				/// respClassName.equals("PVEGetPVEChapterListProtoBufResponse")
				/// ||
				/// respClassName.equals("MainEnterZoneServerProtoBufResponse")
				/// ||
				/// respClassName.equals("HeroGrowGetHeroListProtoBufResponse"))
				/// {
				sb.append("			if (resp is " + respClassName + ") {\n");
				sb.append("				var rs = resp as " + respClassName + ";\n");
				sb.append("				syncServerDataFrom" + respClassName + " (rs);\n");
				sb.append("			}\n");
				sb.append("\n");
				/// }
			}
		}

		sb.append("		}\n");
		sb.append("\n");

		// sb.append(
		// " public void syncServerDataFromProtoBufResponse
		// (global::ProtoBuf.IExtensible req, global::ProtoBuf.IExtensible
		// resp)\n");
		// sb.append(" {\n");
		// for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
		//
		// for (String cmdName : me.getValue()) {
		// String respClassName = cmdName + "ProtoBufResponse";
		// String reqClassName = cmdName + "ProtoBufRequest";
		// sb.append(" if (resp is " + respClassName + " && req is " +
		// reqClassName + ") {\n");
		// sb.append(" var rq = req as " + reqClassName + ";\n");
		// sb.append(" var rs = resp as " + respClassName + ";\n");
		// sb.append(" syncServerDataFrom" + respClassName + " (rq,rs);\n");
		// sb.append(" }\n");
		// sb.append("\n");
		//
		// }
		// }
		//
		// sb.append(" }\n");
		// sb.append("\n");

		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {

			for (String cmdName : me.getValue()) {
				String respClassName = cmdName + "ProtoBufResponse";
				/// if (respClassName.equals("LoginbyGuestProtoBufResponse")
				/// ||
				/// respClassName.equals("PVEGetPVEChapterListProtoBufResponse")
				/// ||
				/// respClassName.equals("MainEnterZoneServerProtoBufResponse")
				/// ||
				/// respClassName.equals("HeroGrowGetHeroListProtoBufResponse"))
				/// {
				sb.append(" public virtual void syncServerDataFrom" + respClassName + " (" + respClassName + " rs)\n");
				sb.append(" {\n");
				sb.append(" }\n");
				sb.append("\n");
				/// }
				// String reqClassName = cmdName + "ProtoBufRequest";
				// sb.append(" public virtual void syncServerDataFrom" +
				// respClassName + " (" + reqClassName + " rq,"
				// + respClassName + " rs)\n");
				// sb.append(" {\n");
				// sb.append(" }\n");
				// sb.append("\n");

			}

		}

		{
			File csvFile = new File(
					CodeGenConsts.PROJCSVFILE_DIRROOT + "Public/World/TProto(前后台数据协议)_Datanotfiy(数据变更通知).csv");

			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			sb.append("		/// <summary>\n");
			sb.append("		/// 同步数据通知\n");
			sb.append("		/// </summary>\n");
			sb.append("		/// <param name=\"pbd\">Pbd.</param>\n");
			sb.append("		public void syncServerDataNotify (ProtoBuf.IExtensible pbd)\n");
			sb.append("		{\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				String nofityEnumClass = "DataNotify" + CSVUtil.capFirst(DBUtil.camelName(s[1].toLowerCase()))
						+ "Changed";
				sb.append("			if (pbd is " + nofityEnumClass + ") {\n");
				sb.append("				" + nofityEnumClass + " notify = pbd as " + nofityEnumClass + ";\n");
				sb.append("				this.syncServerDataNotifyFrom" + nofityEnumClass + " (notify);\n");
				sb.append("			}\n");

			}
			sb.append("		}\n");
			sb.append("\n");

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				String nofityEnumClass = "DataNotify" + CSVUtil.capFirst(DBUtil.camelName(s[1])) + "Changed";
				sb.append("		public virtual void syncServerDataNotifyFrom" + nofityEnumClass + " (" + nofityEnumClass
						+ " notify)\n");
				sb.append("		{\n");
				sb.append("		}\n");
			}
		}

		sb.append("	}\n");
		sb.append("}\n");

		writeFile(new File(javaFileDir, javaFileName).getAbsolutePath(), sb.toString());
	}

	private static void genProtoBufProtoJavaAbstractServiceFile(Map<String, List<String>> cmdModmap) {

		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String className = "Abstract" + modName + "ProtoBufRPCService";
			String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".abstracts";
			String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
			String javaFileName = className + ".java";
			StringBuilder sb = new StringBuilder();

			sb.append("package " + packageName + ";\n");
			sb.append("\n");
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
					.append(".*;\n");
			sb.append("import net.bilinkeji.common.log.LoggerWraper;\n");

			for (String cmdName : me.getValue()) {
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";

				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(reqClassName).append("OuterClass.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(respClassName).append("OuterClass.*;\n");
			}

			sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".services" + ".*;\n");
			sb.append("\n");
			sb.append("/**\n");
			sb.append(" * codegen by tool,do not edit \n");
			sb.append(" * \n");
			sb.append(" * @author lizongbo\n");
			sb.append(" *\n");
			sb.append(" */\n");

			String serviceClassName = modName + "ProtoBufRPCService";
			sb.append("public abstract class ").append(className).append(" implements\n").append(serviceClassName)
					.append(" {\n");
			sb.append("\n");

			sb.append("protected LoggerWraper log = LoggerWraper.getLogger(\"" + serviceClassName + "Impl\");\n");
			for (String cmdName : me.getValue()) {
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";
				sb.append("\t@Override\n");
				sb.append("\tpublic ").append(respClassName).append(".Builder deal").append(cmdName)
						.append("(RPCSession session,\n");
				sb.append("\t		").append(reqClassName).append(" req, RPCHeader respHeader) {\n");
				sb.append("\t	").append(respClassName).append(".Builder builder = ").append(respClassName)
						.append("\n");
				sb.append("\t			.newBuilder();\n");
				sb.append("\t	return builder;\n");
				sb.append("\t}\n\n");
			}

			sb.append("}\n");

			// writeFile(new File(javaFileDir, javaFileName).getAbsolutePath(),
			// sb.toString());

		}
	}

	private static void genProtoBufProtoJavaServiceImplFile(Map<String, List<String>> cmdModmap) {

		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".impl";
			String className = modName + "ProtoBufRPCServiceImpl";
			String javaFileDir = protoRpcJavaServiceCodeRootDir + "/../protojavarpcimpls" + "/"
					+ packageName.replace('.', '/') + "/";
			String javaFileName = className + ".java";

			StringBuilder sb = new StringBuilder();

			sb.append("package " + packageName + ";\n");
			sb.append("\n");
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
					.append(".*;\n");

			for (String cmdName : me.getValue()) {
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";

				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(reqClassName).append("OuterClass.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(respClassName).append("OuterClass.*;\n");
			}
			sb.append("import net.bilinkeji.common.log.LoggerWraper;\n");
			sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".services.*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".abstracts.*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".logics." + modName.toLowerCase()
					+ ".*;\n");
			sb.append("\n");
			sb.append("/**\n");
			sb.append(" * codegen by tool,only gen once \n");
			sb.append(" * \n");
			sb.append(" * @author lizongbo\n");
			sb.append(" *\n");
			sb.append(" */\n");

			String serviceClassName = "Abstract" + modName + "ProtoBufRPCService";
			sb.append("public class ").append(className).append(" extends \n\t\t").append(serviceClassName)
					.append(" {\n");
			sb.append("\n");

			sb.append("			LoggerWraper log = LoggerWraper.getLogger(\"ProtoRpcService\");\n");
			for (String cmdName : me.getValue()) {
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";
				sb.append("\t@Override\n");
				sb.append("\tpublic ").append(respClassName).append(".Builder deal").append(cmdName)
						.append("(RPCSession session,\n");

				sb.append("\t		").append(reqClassName).append(" req, RPCHeader respHeader) {\n");

				sb.append("\t").append(respClassName).append(".Builder builder = null;\n");
				sb.append("\ttry {\n");
				sb.append(
						"\t\tnet.bilinkeji.gecaoshoulie.mgameprotorpc.redisbeanhelpers.PbRedisDAOUtil.BILLING_DETAIL_REQ.set(req);\n");
				sb.append("\t  builder = ");
				sb.append("new " + cmdName + "Logic().deal" + cmdName + "(session, req, respHeader);\n");
				if ("MainEnterZoneServer".equalsIgnoreCase(cmdName)
						|| "CommonreportClientInfo".equalsIgnoreCase(cmdName)
						|| "CommonsyncConfig".equalsIgnoreCase(cmdName)) {
					// 这三个命令不把req写回去,因为req比较大
					sb.append("				//if (builder != null) {\n");
					sb.append("				//	builder.setOrgReqObj(req);\n");
					sb.append("				//}\n");
				} else {
					sb.append("				if (builder != null) {\n");
					sb.append("					builder.setOrgReqObj(req);\n");
					sb.append("				}\n");
				}
				sb.append("\t} finally {\n");
				sb.append("\tlog.info(\"").append(className).append(".deal").append(cmdName)
						.append("|{}|{}=={},respHeader={}\",\n");
				sb.append("\t		session.toDebugString(), req, null, respHeader);\n");
				sb.append("\t}\n");
				sb.append("\t	return builder;\n");
				sb.append("\t}\n\n");
			}

			sb.append("}\n");
			File javaFile = new File(javaFileDir, javaFileName);
			if (true) {
				writeFile(javaFile.getAbsolutePath(), sb.toString());
			} else {
				System.err.println(javaFile + " exists so ignore it");
			}

		}
	}

	private static void genProtoBufProtoJavaLogicImplFile(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDescmap) {
		// TODO 例子
		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".logics." + modName.toLowerCase();

			for (String cmdName : me.getValue()) {
				String className = cmdName + "Logic";
				String javaFileDir = protoRpcJavaServiceCodeRootDir + "/../protojavarpcimpls" + "/"
						+ packageName.replace('.', '/') + "/";
				String javaFileName = className + ".java";

				StringBuilder sb = new StringBuilder();

				sb.append("package " + packageName + ";\n");
				sb.append("\n");

				sb.append("import java.util.*;\n");
				sb.append("import net.bilinkeji.common.log.LoggerWraper;\n");

				sb.append("import net.bilinkeji.gecaoshoulie.mgamedbbeans.db.*;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgamedbbeans.dbbeans.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".*;\n");

				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";

				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(reqClassName).append("OuterClass.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(respClassName).append("OuterClass.*;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgameproto." + modName.toLowerCase() + "." + modName
						+ "Module4Proto.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".services.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".abstracts.*;\n");
				sb.append("import net.bilinkeji.utils.StringUtil;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.common.CommErrorCodeEnum.CommErrorCode;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.serverutils.CommErrorInfoUtil;\n");
				sb.append("\n");
				sb.append("/**\n");
				sb.append(" * codegen by tool,only gen once \n");
				sb.append(" * \n");
				sb.append(" * @author lizongbo\n");
				sb.append(" *\n");
				sb.append(" */\n");

				// String serviceClassName = "Abstract" + modName +
				// "ProtoBufRPCService";
				sb.append("public class ").append(className)
						// .append(" extends \n\t\t").append(serviceClassName)
						.append(" {\n");
				sb.append("\n");

				sb.append("\tprivate static LoggerWraper log = LoggerWraper.getLogger(\"" + className + "\");\n");

				sb.append("	/**\n");
				sb.append("	 * " + cmdModDescmap.get(modName + "|" + cmdName) + "\n");
				sb.append("	 * @param session RPCSession \n");
				sb.append("	 * @param req  \n");
				sb.append("	 * @param respHeader\n");
				sb.append("	 * @return\n");
				sb.append("	 */\n");
				sb.append("\tpublic ").append(respClassName).append(".Builder deal").append(cmdName)
						.append("(RPCSession session,\n");
				sb.append("\t		").append(reqClassName).append(" req, RPCHeader respHeader) {\n");
				sb.append("\t	").append(respClassName).append(".Builder builder = ");

				// sb.append("new " + cmdName +
				// "Logic().dealVIPgetVipInfo(session, req, respHeader);\n");

				sb.append(respClassName).append("\n");
				sb.append("\t .newBuilder();\n");
				sb.append(
						"		respHeader.setRespErrorInfo(CommErrorInfoUtil.buildCommErrorInfo(CommErrorCode.ERR_FAIL));\n");
				sb.append("		int zoneId = session.getCommonGPDUHeader().getZoneId();\n");
				sb.append("		int playerId = session.getCommonGPDUHeader().getPlayerId();\n");
				String cmdReqCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/" + modName + "/"
						+ reqClassName + ".csv";

				List<String[]> reqColList = CSVUtil.getDataFromCSV2(cmdReqCsvPath);
				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					// 默认生成注释方式，有需要的自己去取消注释，省的构建编译出错
					sb.append("//		" + getJavaTypebyProtoType(s2[2], s2[1]) + " " + DBUtil.camelName(s2[3])
							+ " = req.get" + CSVUtil.capFirst(DBUtil.camelName(s2[3])));
					if ("repeated".equalsIgnoreCase(s2[1])) {
						sb.append("List");
					}
					sb.append("();\n");
				}
				sb.append("		try {\n");
				sb.append(
						"		//respHeader.setRespErrorInfo(CommErrorInfoUtil.buildCommErrorInfo(CommErrorCode.ERR_SUCCESS));\n");
				sb.append("		} catch (Throwable th) {\n");
				sb.append("			log.error(\"deal" + cmdName
						+ "|\" + zoneId + \"|\" + playerId + \"|\" + req, th);\n");
				sb.append("			respHeader.putAttach(\"exception\", StringUtil.toStr(th));\n");
				sb.append("			respHeader.setRespErrorInfo(\n");
				sb.append(
						"					CommErrorInfoUtil.buildCommErrorInfo(CommErrorCode.ERR_SERVER_ERR_TEMP));\n");
				sb.append("		}\n");
				sb.append("\t	return builder;\n");
				sb.append("\t}\n\n");
				sb.append("}\n");
				File javaFile = new File(javaFileDir, javaFileName);
				if (!javaFile.exists()) {
					writeFile(javaFile.getAbsolutePath(), sb.toString());
				} else {
					System.err.println(javaFile + " exists so ignore it");
				}

			}
		}
	}

	private static void genProtoBufProtoCsClientTestFile(Map<String, List<String>> cmdModmap) {

		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			for (String cmdName : me.getValue()) {
				String className = cmdName + "CmdClientTest";
				String javaFileDir = CodeGenConsts.PROJPROTO_UnityEditorSRCROOT + "/unittest/clientcmdtest/"
						+ modName.toLowerCase() + "/";
				String javaFileName = className + ".cs";

				StringBuilder sb = new StringBuilder();

				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";

				sb.append("using UnityEngine;\n");
				sb.append("using System.Collections;\n");
				sb.append("using NUnit.Framework;\n");
				sb.append("using net.bilinkeji.gecaoshoulie.mgameproto." + modName.toLowerCase() + ";\n");
				sb.append("using net.bilinkeji.common;\n");
				sb.append("using net.bilinkeji.common.util;\n");
				sb.append("using net.bilinkeji.utils;\n");
				sb.append("using UnityEditor;\n");
				sb.append("\n");
				sb.append("public class " + className + "\n");
				sb.append("{\n");
				sb.append("	[Test]\n");

				sb.append(" [MenuItem(\"接口命令字测试/" + modName + "/" + cmdName + "\")]\n");
				sb.append("	public static void test" + cmdName + " ()\n");
				sb.append("	{	\n");
				sb.append("\n");
				sb.append("MainEnterZoneServerCmdClientTest.testMainEnterZoneServer();\n");

				sb.append("		" + reqClassName + " pbReq = new " + reqClassName + " ();\n");
				sb.append("		//\n");
				sb.append("		ProtoBufRPCUtil.addReqTask (pbReq, testCallBack4" + cmdName + ");\n");
				sb.append("		ClientCmdTestUtil.sendRpcRequest ();\n");
				sb.append("\n");
				sb.append("	}\n");
				sb.append("\n");
				sb.append("	public static void testCallBack4" + cmdName + " (object obj)\n");
				sb.append("	{\n");
				sb.append("		if (obj is " + respClassName + ") {\n");
				sb.append("			" + respClassName + " resp = (" + respClassName + ")obj;\n");
				sb.append("			Debug.LogError (\"testCallBack4" + cmdName
						+ "|for|\" + StringUtil.toXML (resp));\n");
				sb.append("		} else {	\n");
				sb.append("			Debug.LogError (\"testCallBack4" + cmdName
						+ "|Fail|for|\" + StringUtil.toXML (obj));\n");
				sb.append("		}\n");
				sb.append("\n");
				sb.append("	}\n");
				sb.append("\n");
				sb.append("}\n");
				sb.append("\n");

				File javaFile = new File(javaFileDir, javaFileName);
				if (!javaFile.exists()) {
					writeFile(javaFile.getAbsolutePath(), sb.toString());
				} else {
					// System.err.println(javaFile + " exists so ignore it");
				}

			}
		}
	}

	private static void genProtoBufProtoManagerCsFile(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDesMap) {

		String moduleinfoCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
				+ "/Public/Common/TComm(基础配置)_Moduleinfo(模块信息).csv";
		List<String[]> moduleInfoColList = CSVUtil.getDataFromCSV2(moduleinfoCsvPath);
		Map<String, String> moduleInfoMap = new HashMap<String, String>();
		for (int k = 5; k < moduleInfoColList.size(); k++) {
			String[] s2 = moduleInfoColList.get(k);
			moduleInfoMap.put(CSVUtil.getColValue("module_name", s2, moduleInfoColList),
					CSVUtil.getColValue("module_title", s2, moduleInfoColList));
		}

		String protoCmdCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/Public/World/TProto(前后台数据协议)_Cmd(命令字).csv";
		List<String[]> cmdInfoColList = CSVUtil.getDataFromCSV2(protoCmdCsvPath);
		Map<String, String> cmdInfoMap = new HashMap<String, String>();
		for (int k = 5; k < cmdInfoColList.size(); k++) {
			String[] s2 = cmdInfoColList.get(k);
			cmdInfoMap.put(
					CSVUtil.getColValue("cmdMod", s2, cmdInfoColList)
							+ CSVUtil.getColValue("cmdEnumName", s2, cmdInfoColList),
					CSVUtil.getColValue("cmdDesc", s2, cmdInfoColList));
		}

		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String className = modName + "Manager";
			String javaFileDir = CodeGenConsts.PROJPROTO_UnityEditorSRCROOT + "/netmanagertemp/" + modName.toLowerCase()
					+ "/";
			String javaFileName = className + ".cs.txt";

			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("using System.Collections.Generic;\n");
			sb.append("using Bilinkeji.Common;\n");
			sb.append("using Bilinkeji.Net;\n");
			sb.append("using net.bilinkeji.common;\n");
			sb.append("using net.bilinkeji.utils;\n");
			sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto;\n");
			sb.append("using net.bilinkeji.gecaoshoulie.mgameproto." + modName.toLowerCase() + ";\n");
			sb.append("\n");
			sb.append("namespace Bilinkeji.UI\n");
			sb.append("{\n");
			sb.append("	/// <summary>\n");
			sb.append("	/// " + moduleInfoMap.getOrDefault(modName, " ") + "服务类\n");
			sb.append("	/// </summary>\n");
			sb.append("	public class " + modName + "Manager : GecaoMonoBehaviourSingleton4UnityGolbal<" + modName
					+ "Manager>\n");
			sb.append("	{\n");
			sb.append(" /// <summary>\n");
			sb.append(" /// 网络层\n");
			sb.append(" /// </summary>\n");
			sb.append(" private " + modName + "Net net;\n");
			sb.append("\n");
			sb.append("		/// <summary>\n");
			sb.append("		/// 初始化\n");
			sb.append("		/// </summary>\n");
			sb.append("		protected override void Init ()\n");
			sb.append("		{\n");
			sb.append("			base.Init ();\n");
			sb.append(" net = new " + modName + "Net ();\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("\n");

			for (String cmdName : me.getValue()) {
				String cmdReqCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/" + modName + "/"
						+ cmdName + "ProtoBufRequest.csv";
				List<String[]> reqColList = CSVUtil.getDataFromCSV2(cmdReqCsvPath);
				if (reqColList.size() > 5) {
					String[] s2 = reqColList.get(5);
					if (s2[2].contains("CommErrorInfo")) {// 把纯粹占位的对象干掉
						reqColList.remove(5);
					}
				}

				sb.append("		/// <summary>\n");
				sb.append("		/// " + cmdInfoMap.get(cmdName) + "\n");
				sb.append("		/// </summary>\n");
				sb.append("		public void " + cmdName + " (");
				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if (k > 5) {
						sb.append(",");
					}
					sb.append(getCSharpTypebyProtoType(s2[2], s2[1]) + " " + (s2[3]));
				}

				sb.append(")\n");
				sb.append("		{\n");
				sb.append("			net.Request" + cmdName + " (");
				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if (k > 5) {
						sb.append(",");
					}
					sb.append((s2[3]));
				}

				sb.append(");\n");
				sb.append("		}\n");
				sb.append("\n");
				sb.append("		/// <summary>\n");
				sb.append("		///  处理 " + cmdInfoMap.get(cmdName) + "的应答\n");
				sb.append("		/// </summary>\n");
				sb.append("		/// <param name=\"resp\"></param>\n");
				sb.append("		public void Handle" + cmdName + " (" + cmdName + "ProtoBufResponse resp)\n");
				sb.append("		{\n");
				sb.append("			\n");
				sb.append("		}\n");
				sb.append("\n");
				sb.append("\n");

			}

			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("		//==============4CallBack================\n");
			sb.append("\n");
			sb.append("\n");

			for (String cmdName : me.getValue()) {
				String cmdReqCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/" + modName + "/"
						+ cmdName + "ProtoBufRequest.csv";
				List<String[]> reqColList = CSVUtil.getDataFromCSV2(cmdReqCsvPath);
				if (reqColList.size() > 5) {
					String[] s2 = reqColList.get(5);
					if (s2[2].contains("CommErrorInfo")) {// 把纯粹占位的对象干掉
						reqColList.remove(5);
					}
				}

				sb.append("		/// <summary>\n");
				sb.append("		/// " + cmdInfoMap.get(cmdName) + "\n");
				sb.append("		/// </summary>\n");
				sb.append("		public void " + cmdName + "4CallBack (");
				boolean haveReqParam = false;
				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if (k > 5) {
						sb.append(",");
					}
					haveReqParam = true;
					sb.append(getCSharpTypebyProtoType(s2[2], s2[1]) + " " + (s2[3]));
				}
				if (haveReqParam) {
					sb.append(",");
				}
				sb.append("System.Action<" + cmdName + "ProtoBufResponse> callBack");

				sb.append(")\n");
				sb.append("		{\n");

				sb.append("            Action<IExtensible> mixCallback = response =>\n");
				sb.append("            {\n");
				sb.append("                " + cmdName + "ProtoBufResponse rsp = response as " + cmdName
						+ "ProtoBufResponse;\n");
				sb.append("                Handle" + cmdName + "(rsp);\n");
				sb.append("                CustomHandle" + cmdName + "(rsp);\n");
				sb.append("                if (callBack != null)\n");
				sb.append("                {\n");
				sb.append("                    callBack(rsp);\n");
				sb.append("                }\n");
				sb.append("            };\n");

				sb.append("			net.Request" + cmdName + "4CallBack (");
				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if (k > 5) {
						sb.append(",");
					}
					sb.append((s2[3]));
				}
				if (haveReqParam) {
					sb.append(",");
				}
				sb.append("mixCallback");
				sb.append(");\n");
				sb.append("		}\n");
				sb.append("\n");

				sb.append("        private void CustomHandle" + cmdName + "(" + cmdName + "ProtoBufResponse resp)\n");
				sb.append("        {\n");
				sb.append("        }\n");
				sb.append("\n");
				sb.append("\n");

			}

			sb.append("\n");
			sb.append("\n");
			sb.append("       \n");
			sb.append("	}\n");
			sb.append("}\n");
			sb.append("\n");
			sb.append("\n");

			File javaFile = new File(javaFileDir, javaFileName);
			writeFile(javaFile.getAbsolutePath(), sb.toString());
		}
	}

	private static void genProtoBufProtoNetCsFile(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDesMap) {

		String moduleinfoCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
				+ "/Public/Common/TComm(基础配置)_Moduleinfo(模块信息).csv";
		List<String[]> moduleInfoColList = CSVUtil.getDataFromCSV2(moduleinfoCsvPath);
		Map<String, String> moduleInfoMap = new HashMap<String, String>();
		for (int k = 5; k < moduleInfoColList.size(); k++) {
			String[] s2 = moduleInfoColList.get(k);
			moduleInfoMap.put(CSVUtil.getColValue("module_name", s2, moduleInfoColList),
					CSVUtil.getColValue("module_title", s2, moduleInfoColList));
		}

		String protoCmdCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/Public/World/TProto(前后台数据协议)_Cmd(命令字).csv";
		List<String[]> cmdInfoColList = CSVUtil.getDataFromCSV2(protoCmdCsvPath);
		Map<String, String> cmdInfoMap = new HashMap<String, String>();
		for (int k = 5; k < cmdInfoColList.size(); k++) {
			String[] s2 = cmdInfoColList.get(k);
			cmdInfoMap.put(
					CSVUtil.getColValue("cmdMod", s2, cmdInfoColList)
							+ CSVUtil.getColValue("cmdEnumName", s2, cmdInfoColList),
					CSVUtil.getColValue("cmdDesc", s2, cmdInfoColList));
		}

		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String className = modName + "Net";
			String javaFileDir = CodeGenConsts.PROJPROTO_UnityEditorSRCROOT + "/netmanagertemp/" + modName.toLowerCase()
					+ "/";
			String javaFileName = className + ".cs.txt";

			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("using System.Collections.Generic;\n");
			sb.append("using Bilinkeji.Common;\n");
			sb.append("using Bilinkeji.UI;\n");
			sb.append("using net.bilinkeji.common;\n");
			sb.append("using net.bilinkeji.utils;\n");
			sb.append("using net.bilinkeji.common.util;\n");
			sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto;\n");
			sb.append("using net.bilinkeji.gecaoshoulie.mgameproto." + modName.toLowerCase() + ";\n");
			sb.append("using ProtoBuf;\n");
			sb.append("\n");
			sb.append("namespace Bilinkeji.Net\n");
			sb.append("{\n");
			sb.append("    /// <summary>\n");
			sb.append("	/// " + moduleInfoMap.getOrDefault(modName, " ") + "的云服务类\n");
			sb.append("    /// </summary>\n");
			sb.append("    public class " + modName + "Net\n");
			sb.append("    {\n");

			for (String cmdName : me.getValue()) {
				String cmdReqCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/" + modName + "/"
						+ cmdName + "ProtoBufRequest.csv";
				List<String[]> reqColList = CSVUtil.getDataFromCSV2(cmdReqCsvPath);
				if (reqColList.size() > 5) {
					String[] s2 = reqColList.get(5);
					if (s2[2].contains("CommErrorInfo")) {// 把纯粹占位的对象干掉
						reqColList.remove(5);
					}
				}

				sb.append("        /// <summary>\n");
				sb.append("        /// 向服务器请求 " + cmdInfoMap.get(cmdName) + "\n");
				sb.append("        /// </summary>\n");
				sb.append("		public void Request" + cmdName + " (");
				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if (k > 5) {
						sb.append(",");
					}
					sb.append(getCSharpTypebyProtoType(s2[2], s2[1]) + " " + (s2[3]));
				}

				sb.append(")\n");
				sb.append("		{\n");

				sb.append("            var request = new " + cmdName + "ProtoBufRequest();\n");

				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if ("repeated".equalsIgnoreCase(s2[1].trim())) {
						sb.append("			request." + (s2[3]) + ".AddRange( " + (s2[3]) + ");\n");
					} else {
						sb.append("			request." + (s2[3]) + " = " + (s2[3]) + ";\n");
					}
				}

				sb.append("            ProtoBufRPCUtil.addReqTask(request, Handle" + cmdName + "ProtoBufResponse);\n");
				sb.append("		}\n");
				sb.append("\n");

				sb.append("        /// <summary>\n");
				sb.append("        /// 收到服务器 " + cmdInfoMap.get(cmdName) + " 的应答数据\n");
				sb.append("        /// </summary>\n");
				sb.append("        /// <param name=\"obj\"></param>\n");
				sb.append("        private void Handle" + cmdName + "ProtoBufResponse(IExtensible obj)\n");
				sb.append("        {\n");
				sb.append("            var received = obj as " + cmdName + "ProtoBufResponse;\n");
				sb.append("            " + modName + "Manager.Instance.Handle" + cmdName + "(received);\n");
				sb.append("        }\n");
				sb.append("\n");
				sb.append("\n");

			}

			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("		//==============4CallBack================\n");
			sb.append("\n");
			sb.append("\n");

			for (String cmdName : me.getValue()) {
				String cmdReqCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/" + modName + "/"
						+ cmdName + "ProtoBufRequest.csv";
				List<String[]> reqColList = CSVUtil.getDataFromCSV2(cmdReqCsvPath);
				if (reqColList.size() > 5) {
					String[] s2 = reqColList.get(5);
					if (s2[2].contains("CommErrorInfo")) {// 把纯粹占位的对象干掉
						reqColList.remove(5);
					}
				}

				sb.append("        /// <summary>\n");
				sb.append("        /// 向服务器请求 " + cmdInfoMap.get(cmdName) + "\n");
				sb.append("        /// </summary>\n");
				sb.append("		public void Request" + cmdName + "4CallBack(");
				boolean haveReqParam = false;
				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if (k > 5) {
						sb.append(",");
					}
					haveReqParam = true;
					sb.append(getCSharpTypebyProtoType(s2[2], s2[1]) + " " + (s2[3]));
				}
				if (haveReqParam) {
					sb.append(",");
				}
				sb.append("System.Action<global::ProtoBuf.IExtensible> callBack");
				sb.append(")\n");
				sb.append("		{\n");

				sb.append("            var request = new " + cmdName + "ProtoBufRequest();\n");

				for (int k = 5; k < reqColList.size(); k++) {
					String[] s2 = reqColList.get(k);
					if ("repeated".equalsIgnoreCase(s2[1].trim())) {
						sb.append("			request." + (s2[3]) + ".AddRange( " + (s2[3]) + ");\n");
					} else {
						sb.append("			request." + (s2[3]) + " = " + (s2[3]) + ";\n");
					}
				}

				sb.append("            ProtoBufRPCUtil.addReqTask(request, callBack);\n");
				sb.append("		}\n");
				sb.append("\n");

			}

			sb.append("       \n");
			sb.append("\n");
			sb.append("    }\n");
			sb.append("}\n");

			File javaFile = new File(javaFileDir, javaFileName);
			writeFile(javaFile.getAbsolutePath(), sb.toString());
		}
	}

	private static void genProtoBufProtoJavaClientTestFile(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDescmap) {
		// TODO 插入标准测试用例生成步骤
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".clientunittest."
					+ modName.toLowerCase();

			for (String cmdName : me.getValue()) {
				String className = cmdName + "CustomedCmdClientTest";
				String javaFileDir = protoRpcJavaServiceCodeRootDir + "/../protojavarpcimpls" + "/"
						+ packageName.replace('.', '/') + "/";
				String javaFileName = className + ".java";

				StringBuilder sb = new StringBuilder();

				sb.append("package " + packageName + ";\n");
				sb.append("\n");
				sb.append("import java.util.Map;\n");
				sb.append("import java.util.List;\n");
				sb.append("import java.util.LinkedList;\n");
				sb.append("\n");
				sb.append("import com.google.protobuf.MessageOrBuilder;\n");
				sb.append("\n");
				sb.append("import net.bilinkeji.data.Pair;\n");
				sb.append("import net.bilinkeji.utils.StringUtil;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.MultiGameProtocalDataUnit;\n");
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";

				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(reqClassName).append("OuterClass.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(respClassName).append("OuterClass.*;\n");
				sb.append(
						"import net.bilinkeji.gecaoshoulie.mgameprotorpc.apachemina.tcp.GameProtocalDataUnitClientHandler;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.ClientCmdTestUtil;\n");
				sb.append(
						"import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.login.LoginbyGuestCmdClientTest;\n");
				sb.append(
						"import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.main.MainEnterZoneServerCmdClientTest;\n");
				sb.append("\n");
				sb.append("public class " + className + " {\n");
				sb.append("\n");
				sb.append("	public static void main(String[] args) {\n");

				sb.append("		LoginbyGuestCmdClientTest.testLoginbyGuest();\n");
				sb.append("		MainEnterZoneServerCmdClientTest.testMainEnterZoneServer();\n");
				sb.append("		test" + cmdName + "();\n");
				sb.append("		System.exit(0);\n");
				sb.append("	}\n");
				sb.append("\n");
				sb.append("	public static void test" + cmdName + "() {\n");
				sb.append(
						"		List<Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>>> testCaseList = genTestCaseList();\n");
				sb.append(
						"			for(Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>> mgpduPair : testCaseList){\n");
				sb.append("				ClientCmdTestUtil.sendRpcRequest(ClientCmdTestUtil.serverUrl, mgpduPair);\n");
				sb.append("			}\n	" + "		}\n");
				sb.append("\n");
				sb.append("	public static GameProtocalDataUnitClientHandler " + cmdName
						+ "Handler = new GameProtocalDataUnitClientHandler() {\n");
				sb.append("		@Override\n");
				sb.append("		public void dealGameProtocalDataUnit(MessageOrBuilder mob) {\n");
				sb.append("			if (mob instanceof " + cmdName + "ProtoBufResponse) {\n");
				sb.append("				" + cmdName + "ProtoBufResponse pbResp = (" + cmdName
						+ "ProtoBufResponse) mob;\n");
				sb.append("				System.out.println(\"" + cmdName + "CmdTest|" + cmdName
						+ "Handler|for|\" + StringUtil.toJsonString(pbResp));\n");
				sb.append("\n");
				sb.append("			} else {\n");
				sb.append("				System.err.println(\n");
				sb.append("						\"" + cmdName + "Handler.dealGameProtocalDataUnit|but|mob|isnot|"
						+ cmdName + "ProtoBufResponse=\" + mob);\n");
				sb.append("			}\n");
				sb.append("\n");
				sb.append("		}\n");
				sb.append("	};\n");

				// TODO 构建测试用例
				sb.append(
						"	public static List<Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>>> genTestCaseList() {\n");
				sb.append(
						"		List<Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>>> testCaseList = new LinkedList<>();\n");
				// TODO 构建自定义用例开始
				sb.append("		{\n");
				sb.append("			//	" + cmdName + "ProtoBufRequest pbReq = " + cmdName
						+ "ProtoBufRequest.newBuilder()\n");
				sb.append("				//\n");
				sb.append("		\n");
				sb.append("						//\n");
				sb.append("			//			.build();\n");
				sb.append("\n");
				sb.append("		//		@SuppressWarnings(\"unchecked\")\n");
				sb.append(
						"			//	Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>> mgpduPair = ClientCmdTestUtil\n");
				sb.append("			//			.genMGPDURequest(Pair.makePair(pbReq, " + cmdName + "Handler));\n");
				sb.append("		 //testCaseList.add(mgpduPair);");
				sb.append("\n");
				sb.append("		}\n");
				// TODO 构建自定义用例结束
				sb.append("		return testCaseList;\n");
				sb.append("	}\n");
				sb.append("\n");
				// TODO 构建标准用例
				// TODO 构建自定义用例

				sb.append("}\n");

				File javaFile = new File(javaFileDir, javaFileName);
				// 自定义用例是不能覆盖的
				if (!javaFile.exists()) {
					writeFile(javaFile.getAbsolutePath(), sb.toString());
				} else {
					System.out.println(javaFile + " exists so ignore it");
				}
				// writeFile(javaFile.getAbsolutePath(), sb.toString());

			}
		}

	}

	private static void genProtoBufProtoJavaStandardClientTestFile(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDescmap, Set<String> cmdTestExcludeInfoSet) {
		// TODO 插入自定义测试用例生成步骤
		genProtoBufProtoJavaClientTestFile(cmdModmap, cmdModDescmap);
		// TODO 生成标准用例
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".clientunittest."
					+ modName.toLowerCase();

			for (String cmdName : me.getValue()) {
				String className = cmdName + "StandardCmdClientTestv1";
				String javaFileDir = protoRpcJavaServiceCodeRootDir + "/../protojavarpcimpls" + "/"
						+ packageName.replace('.', '/') + "/";
				String javaFileName = className + ".java";

				StringBuilder sb = new StringBuilder();

				sb.append("package " + packageName + ";\n");
				sb.append("\n");
				sb.append("import java.util.Map;\n");
				sb.append("import java.util.List;\n");
				sb.append("import java.util.LinkedList;\n");
				sb.append("\n");
				sb.append("import com.google.protobuf.MessageOrBuilder;\n");
				sb.append("import com.google.protobuf.GeneratedMessage.Builder;\n");
				sb.append("\n");
				sb.append("import net.bilinkeji.data.Pair;\n");
				sb.append("import net.bilinkeji.utils.StringUtil;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.MultiGameProtocalDataUnit;\n");
				String reqClassName = cmdName + "ProtoBufRequest";
				String respClassName = cmdName + "ProtoBufResponse";

				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(reqClassName).append("OuterClass.*;\n");
				sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase())
						.append(".").append(respClassName).append("OuterClass.*;\n");
				sb.append(
						"import net.bilinkeji.gecaoshoulie.mgameprotorpc.apachemina.tcp.GameProtocalDataUnitClientHandler;\n");
				sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.ClientCmdTestUtil;\n");
				sb.append(
						"import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.login.LoginbyGuestCmdClientTest;\n");
				sb.append(
						"import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.main.MainEnterZoneServerCmdClientTest;\n");
				sb.append("\n");
				sb.append("public class " + className + " {\n");
				sb.append("\n");
				sb.append("	public static void main(String[] args) {\n");

				sb.append("		LoginbyGuestCmdClientTest.testLoginbyGuest();\n");
				sb.append("		MainEnterZoneServerCmdClientTest.testMainEnterZoneServer();\n");
				sb.append("		test" + cmdName + "();\n");
				sb.append("		System.exit(0);\n");
				sb.append("	}\n");
				sb.append("\n");
				sb.append("	public static void test" + cmdName + "() {\n");
				// TODO 兼容旧逻辑
				sb.append(
						"		List<Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>>> testCaseList = genTestCaseList();\n");
				sb.append(
						"			for(Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>> mgpduPair : testCaseList){\n");
				sb.append("				ClientCmdTestUtil.sendRpcRequest(ClientCmdTestUtil.serverUrl, mgpduPair);\n");
				sb.append("			}\n	" + "		}\n");
				sb.append("\n");
				sb.append("	public static GameProtocalDataUnitClientHandler " + cmdName
						+ "Handler = new GameProtocalDataUnitClientHandler() {\n");
				sb.append("		@Override\n");
				sb.append("		public void dealGameProtocalDataUnit(MessageOrBuilder mob) {\n");
				sb.append("			if (mob instanceof " + cmdName + "ProtoBufResponse) {\n");
				sb.append("				" + cmdName + "ProtoBufResponse pbResp = (" + cmdName
						+ "ProtoBufResponse) mob;\n");
				sb.append("				System.out.println(\"" + cmdName + "CmdTest|" + cmdName
						+ "Handler|for|\" + StringUtil.toJsonString(pbResp));\n");
				sb.append("\n");
				sb.append("			} else {\n");
				sb.append("				System.err.println(\n");
				sb.append("						\"" + cmdName + "Handler.dealGameProtocalDataUnit|but|mob|isnot|"
						+ cmdName + "ProtoBufResponse=\" + mob);\n");
				sb.append("			}\n");
				sb.append("\n");
				sb.append("		}\n");
				sb.append("	};\n");

				// TODO 构建测试用例
				sb.append(
						"	public static List<Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>>> genTestCaseList() {\n");
				sb.append(
						"		List<Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>>> customedTestCaseList = "
								+ cmdName + "CustomedCmdClientTest" + ".genTestCaseList();\n");
				sb.append(
						"		List<Pair<MultiGameProtocalDataUnit, Map<Long, GameProtocalDataUnitClientHandler>>> testCaseList = new LinkedList<>();\n");
				// TODO 循环各种情况
				System.out.println(cmdName);
				if (!cmdTestExcludeInfoSet.contains(cmdName)) {
					sb.append("		{\n");
					sb.append("				Builder pbReq = " + cmdName + "ProtoBufRequest.newBuilder();\n");
					sb.append("\n");
					sb.append("				ClientCmdTestUtil.standardTest(pbReq, " + cmdName + "Handler);\n");
					sb.append("\n");
					sb.append("		}\n");
				} else {
					System.out.println(cmdName + " is skipped from standard test");
					sb.append("	// " + cmdName + " is skipped from standard test");
				}
				// TODO 构建标准测试用例循环结束
				sb.append("		testCaseList.addAll(customedTestCaseList);\n");
				sb.append("		return testCaseList;\n");
				sb.append("	}\n");
				sb.append("\n");
				// TODO 构建标准用例
				sb.append("}\n");

				File javaFile = new File(javaFileDir, javaFileName);
				writeFile(javaFile.getAbsolutePath(), sb.toString());

			}
		}

	}

	public static int getTestCaseVarious(String[] strings) {
		// TODO Auto-generated method stub
		String type = getJavaTypebyProtoType(strings[2], strings[1]);
		// TODO string类型
		if ("String".equals(type))
			return CodeGenConsts.stringTestCaseOptions.length;
		// TODO int类型
		if ("int".equals(type))
			return CodeGenConsts.intTestCaseOptions.length;
		// TODO long类型
		if ("long".equals(type))
			return CodeGenConsts.longTestCaseOptions.length;
		return 1;
	}

	private static int[] testCaseArray(Stack<Integer> argStack, int[] testCaseHolder) {
		for (int i = 0; i < argStack.size(); i++) {
			testCaseHolder[i] = argStack.get(i);
		}
		return testCaseHolder;
	}

	private static void genFullSizeCmdClientTest(Map<String, List<String>> cmdModmap,
			Map<String, String> cmdModDescmap) {
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		// TODO 生成整体的单元测试运行类
		String fullSizeClassName = "FullSizeCmdClientTest";
		StringBuilder sb = new StringBuilder();
		String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".clientunittest";
		sb.append("package " + packageName + ";\n");
		sb.append("\n");
		sb.append("import java.util.Map;\n");
		sb.append("\n");
		sb.append("import com.google.protobuf.MessageOrBuilder;\n");
		sb.append("\n");
		sb.append("import net.bilinkeji.data.Pair;\n");
		sb.append("import net.bilinkeji.utils.StringUtil;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.MultiGameProtocalDataUnit;\n");
		sb.append("import net.bilinkeji.common.log.LoggerWraper;\n");
		sb.append(
				"import net.bilinkeji.gecaoshoulie.mgameprotorpc.apachemina.tcp.GameProtocalDataUnitClientHandler;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.ClientCmdTestUtil;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.login.LoginbyGuestCmdClientTest;\n");
		sb.append(
				"import net.bilinkeji.gecaoshoulie.mgameprotorpc.clientunittest.main.MainEnterZoneServerCmdClientTest;\n");
		sb.append("\n");
		sb.append("public class " + fullSizeClassName + " {\n");
		sb.append("\n");
		sb.append("		private static LoggerWraper logger = LoggerWraper.getLogger(\"FullSizeCmdClientTest\");\n");
		sb.append("	public static void main(String[] args) {\n");

		sb.append("		LoginbyGuestCmdClientTest.testLoginbyGuest();\n");
		sb.append("		MainEnterZoneServerCmdClientTest.testMainEnterZoneServer();\n");
		for (Map.Entry<String, List<String>> me : cmdModmap.entrySet()) {
			String modName = me.getKey();
			// String packageName =
			// CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT +
			// ".clientunittest."
			// + modName.toLowerCase();

			for (String cmdName : me.getValue()) {
				String methodName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".clientunittest."
						+ modName.toLowerCase() + "." + cmdName + "StandardCmdClientTestv1." + "test" + cmdName
						+ "();\n";
				sb.append("		try {");
				sb.append(methodName);
				sb.append("} catch (Throwable e)  {logger.error(\"ERROR|\",e);;}\n\n");
				sb.append("\n");
				sb.append("logger.info(\"test " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".clientunittest."
						+ modName.toLowerCase() + "." + cmdName + "CmdClientTest." + " finished \");\n");
			}
		}
		sb.append(
				"		try {Thread.sleep(10000);} catch (InterruptedException e) {e.printStackTrace();}finally{System.exit(0);};\n\n");
		sb.append("	}\n");
		sb.append("	}\n");
		String javaFileName = "FullSizeCmdClientTest" + ".java";
		String javaFileDir = protoRpcJavaServiceCodeRootDir + "/../protojavarpcimpls" + "/"
				+ packageName.replace('.', '/') + "/";
		File javaFile = new File(javaFileDir, javaFileName);
		writeFile(javaFile.getAbsolutePath(), sb.toString());
	}

	private static void genAbstractProtoBufProtoJavaServiceManagerFile(String tableName, List<String[]> colList) {

		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		String className = "AbstractProtobufRPCServiceManager";
		String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + "";
		String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
		String javaFileName = className + ".java";

		StringBuilder sb = new StringBuilder();

		sb.append("package " + packageName + ";\n");
		sb.append("\n");
		sb.append("import java.util.*;\n");
		sb.append("import java.lang.reflect.Method;\n");
		;
		sb.append("\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".impl.*;\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".services.*;\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".abstracts.*;\n");

		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".*;\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common.TProtoCmd;\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common.TProtoCmd.TProto_Cmd;\n");
		sb.append("\n");
		sb.append("/**\n");
		sb.append(" * codegen by tool,only gen once \n");
		sb.append(" * \n");
		sb.append(" * @author lizongbo\n");
		sb.append(" *\n");
		sb.append(" */\n");

		sb.append("public class ").append(className).append(" {\n");
		sb.append("\n");

		Set<String> modSets = new HashSet<String>();
		sb.append("\n");
		sb.append("		protected Map<Class, Object> serviceImplMap = new HashMap<Class, Object>();\n");
		sb.append("		protected Map<Integer, Class> serviceClassCmdMap = new HashMap<Integer, Class>();\n");
		sb.append("		protected Map<Integer, String> serviceMethodNameCmdMap = new HashMap<Integer, String>();\n");
		sb.append("		protected Map<Integer, Method> serviceMethodMap = new HashMap<Integer, Method>();\n");
		sb.append("\n");
		sb.append("		public boolean loadAllServiceCmd() {\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String modName = s[1];
			modSets.add(modName);
			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String cmdName = s[1] + s[2];
			sb.append("			serviceClassCmdMap.put(TProtoCmd.TProto_Cmd." + cmdEnum + "_VALUE,\n");
			sb.append("					" + modName + "ProtoBufRPCService.class);\n");
		}

		sb.append("			return true;\n");
		sb.append("			\n");
		sb.append("		}\n");

		sb.append("		public boolean loadAllServiceMethodName() {\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String modName = s[1].toLowerCase();
			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String cmdName = s[1] + s[2];
			sb.append("			serviceMethodNameCmdMap.put(\n");
			sb.append("					TProtoCmd.TProto_Cmd." + cmdEnum.toUpperCase() + "_VALUE,\n");
			sb.append("					\"deal" + cmdName + "\");\n");
		}

		sb.append("			return true;\n");
		sb.append("			\n");
		sb.append("		}\n");

		sb.append("public boolean loadAllService() {\n");

		for (String modName : modSets) {
			// String modName = me.getKey();
			String serviceClassname = modName + "ProtoBufRPCService";
			String serviceImplClassName = modName + "ProtoBufRPCServiceImpl";
			sb.append("	").append(serviceClassname).append(" ").append(serviceClassname).append("Stub = new ")
					.append(serviceImplClassName).append("();\n");
			sb.append("	serviceImplMap.put(").append(serviceClassname).append(".class,\n");
			sb.append("			").append(serviceClassname).append("Stub);\n");

		}
		sb.append("	return true;\n");
		sb.append("}\n");
		sb.append("\n");

		sb.append("}\n");
		File javaFile = new File(javaFileDir, javaFileName);
		if (true || !javaFile.exists()) {
			writeFile(javaFile.getAbsolutePath(), sb.toString());
		} else {
			System.out.println(javaFile + " exists so ignore it");
		}

	}

	private static void genProtoBufUtilLoginCheckUtilJavaFile(String tableName, List<String[]> colList) {

		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		String className = "ProtoRPCLoginCheckUtil";
		String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + "";
		String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
		String javaFileName = className + ".java";

		StringBuilder sb = new StringBuilder();

		sb.append("package " + packageName + ";\n");
		sb.append("import java.util.HashSet;\n");
		sb.append("import java.util.Set;\n");
		sb.append("\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common.TProtoCmd;\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common.TProtoCmd.TProto_Cmd;\n");

		sb.append("\n");
		sb.append("public class " + className + " {\n");
		sb.append("\n");

		sb.append("private static Set<Integer> needLoginCheckCmds = new HashSet<Integer>();\n");
		sb.append("static {\n");
		sb.append("	loadNeedLoginCmds();\n");
		sb.append("}\n");

		sb.append("private static void loadNeedLoginCmds() {\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);

			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			if (s[4] != null && "yes".equalsIgnoreCase(s[4].trim())) {
				sb.append("	needLoginCheckCmds.add(TProto_Cmd." + cmdEnum + "_VALUE);\n");
			}
		}

		sb.append("}\n");
		sb.append("\n");
		sb.append("public static boolean isNeedLoginCheck(int cmdValue) {\n");
		sb.append("	return needLoginCheckCmds.contains(cmdValue);\n");
		sb.append("}\n");

		sb.append("\n");
		sb.append("}\n");
		File javaFile = new File(javaFileDir, javaFileName);

		writeFile(javaFile.getAbsolutePath(), sb.toString());

	}

	private static void genProtoBufProtohandlerJavaFile(String tableName, List<String[]> colList) {

		// sbproto2net.append("echo proto2netCmd=%proto2netCmd%\n");
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		String className = "DefaultProtoRpcHandler";
		String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + "";
		String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
		String javaFileName = className + ".java";

		StringBuilder sb = new StringBuilder();

		sb.append("package " + packageName + ";\n");
		sb.append("\n");
		sb.append("import java.util.*;\n");
		sb.append("import net.bilinkeji.common.log.*;\n");
		sb.append("import net.bilinkeji.utils.StringUtil;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.common.CommErrorCodeEnum.CommErrorCode;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameprotorpc.serverutils.CommErrorInfoUtil;\n");
		sb.append("import com.google.protobuf.InvalidProtocolBufferException;\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common.TProtoCmd;\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common.TProtoCmd.TProto_Cmd;\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String modName = s[1].toLowerCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufRequest";
			String protoRespClassName = s[1] + "" + s[2] + "ProtoBufResponse";
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "." + modName + "." + protoReqClassName
					+ "OuterClass.*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "." + modName + "." + protoRespClassName
					+ "OuterClass.*;\n");
		}

		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".services.*;\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".abstracts.*;\n");
		sb.append("\n");
		sb.append("public class " + className + " {\n");
		sb.append("\n");
		sb.append("	private LoggerWraper log = LoggerWraper.getLogger(\"" + className + "\");\n");

		sb.append("	public byte[] dealbyRPCService(RPCSession session, RPCHeader respHeader,\n");
		sb.append("			byte[] bytes, TProto_Cmd cmd) {\n");
		sb.append("		try {\n");
		sb.append("			switch (cmd.getNumber()) {\n");
		sb.append("\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String modName = s[1].toLowerCase();

			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufRequest";
			String protoRespClassName = s[1] + "" + s[2] + "ProtoBufResponse";
			sb.append("			case TProtoCmd.TProto_Cmd." + cmdEnum + "_VALUE: {\n");
			sb.append("				return deal" + s[1] + "" + s[2] + "(session, respHeader, bytes);\n");
			sb.append("			}\n");
		}

		sb.append("			default:\n");
		sb.append("				break;\n");
		sb.append("			}\n");
		sb.append("		} catch (Throwable e) {\n");
		sb.append(
				"		log.error(\"dealbyRPCServiceerr|session=\" + session + \"|respHeader=\" + respHeader + \"|cmd=\" + cmd, e);\n");
		sb.append("		respHeader.putAttach(\"dealbyRPCServiceExp\", StringUtil.toStr(e));\n");
		sb.append("		return null;\n");
		sb.append("		}\n");
		sb.append("		log.error(\"dealbyRPCService|but nofunc4cmd:\" + cmd);\n");
		sb.append("		respHeader.putAttach(\"dealbyRPCServiceErr\", \"nofunc4cmd\"+cmd);\n");
		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String modName = s[1].toLowerCase();

			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufRequest";
			String protoRespClassName = s[1] + "" + s[2] + "ProtoBufResponse";
			String cmdName = s[1] + "" + s[2];
			sb.append("	private byte[] deal" + cmdName + "(RPCSession session,\n");
			sb.append("			RPCHeader respHeader, byte[] bytes)\n");
			sb.append("			throws InvalidProtocolBufferException {\n");
			sb.append("		" + s[1] + "ProtoBufRPCService service = ProtobufRPCServiceManager\n");
			sb.append("				.getInstance().getService(" + s[1] + "ProtoBufRPCService.class);\n");
			sb.append("		" + cmdName + "ProtoBufRequest req = " + cmdName + "ProtoBufRequest\n");
			sb.append("				.parseFrom(bytes);\n");

			sb.append(
					"		List<ProtoBufRequestValidatorErrorInfo> validateErrorList = service.validateProtoRequest(req);\n");
			sb.append("		if (validateErrorList != null && validateErrorList.size() > 0) {\n");
			sb.append("			respHeader.setValidateErrorInfos(validateErrorList);\n");
			sb.append(
					"			respHeader.setRespErrorInfo(CommErrorInfoUtil.buildCommErrorInfo(CommErrorCode.ERR_PARAM_ERROR));\n");
			sb.append("			return null;\n");
			sb.append("		}\n");

			sb.append("		" + cmdName + "ProtoBufResponse.Builder resp = (" + cmdName
					+ "ProtoBufResponse.Builder) service\n");
			sb.append("				.deal" + cmdName + "(session, req, respHeader);\n");
			sb.append("		return resp.build().toByteArray();\n");
			sb.append("\n");
			sb.append("	}\n");
		}

		sb.append("\n");
		sb.append("}\n");
		File javaFile = new File(javaFileDir, javaFileName);

		writeFile(javaFile.getAbsolutePath(), sb.toString());

	}

	private static void genProtoBuf2javaCmdFile(Map<String, List<String>> cmdModmap) {
		StringBuilder sb = new StringBuilder();
		sb.append("rm ../protofilestmp/*.proto\n");
		sb.append("cp ./*.proto ../protofilestmp/\n");
		sb.append("cp ./dbbeans4proto/*.proto ../protofilestmp/\n");
		sb.append("cp ./CRUD/*.proto ../protofilestmp/\n");
		for (String cmd : cmdModmap.keySet()) {
			sb.append("cp ./" + cmd + "/*.proto ../protofilestmp/\n");
		}

		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/CommErrorCodeEnum.proto\n");
		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/CommChannelCodeEnum.proto\n");
		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/GecaoshoulieEnumDef.proto\n");
		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/GameModuleEnum.proto\n");

		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/ProtobufRpcBase.proto\n");
		sb.append("protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/TProto_Cmd.proto\n");
		sb.append("protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/*4Proto.proto\n");

		for (String cmd : cmdModmap.keySet()) {
			sb.append("protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/" + cmd
					+ "*.proto\n");
		}

		File javaFile = new File(CodeGenConsts.PROJPROTOFILE_DIRROOT, "proto2java.sh");

		writeFile(javaFile.getAbsolutePath(), sb.toString());
		genProtoBuf2javaCmdBatFile(cmdModmap);
	}

	private static void genProtoBuf2javaCmdBatFile(Map<String, List<String>> cmdModmap) {
		StringBuilder sb = new StringBuilder();
		sb.append("del /Q ..\\protofilestmp\\*.proto\r\n");
		sb.append("copy /Y .\\*.proto ..\\protofilestmp\\\r\n");
		sb.append("copy /Y .\\dbbeans4proto\\*.proto ..\\protofilestmp\\\r\n");
		sb.append("copy /Y .\\CRUD\\*.proto ..\\protofilestmp\\\r\n");
		for (String cmd : cmdModmap.keySet()) {
			sb.append("copy /Y .\\" + cmd + "\\*.proto ..\\protofilestmp\\\r\n");
		}

		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/CommChannelCodeEnum.proto\r\n");
		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/CommErrorCodeEnum.proto\r\n");

		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/GecaoshoulieEnumDef.proto\r\n");
		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/GameModuleEnum.proto\r\n");
		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/TProto_Cmd.proto\r\n");
		sb.append(
				"protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/ProtobufRpcBase.proto\r\n");
		sb.append("protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/*4Proto.proto\r\n");

		for (String cmd : cmdModmap.keySet()) {
			sb.append("protoc --proto_path=../protofilestmp/ --java_out=../protojavas ../protofilestmp/" + cmd
					+ "*.proto\r\n");
		}
		sb.append("echo \"proto2java runend\"\r\n");
		File javaFile = new File(CodeGenConsts.PROJPROTOFILE_DIRROOT, "proto2java.bat");

		writeFile(javaFile.getAbsolutePath(), sb.toString());
	}

	private static void genProtoBufDBBeansDbbeansConfig4ProtoUtilJavaFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("package net.bilinkeji.gecaoshoulie.mgameprotorpc.logics.common;\n");
		sb.append("\n");
		sb.append("import java.util.*;\n");
		sb.append("import net.bilinkeji.data.DataPage;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgamedbbeans.db.*;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgamedbbeans.dbbeans.*;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgamedbbeans.dbbeans.dbbeanhelpers.protobufconverters.*;\n");
		sb.append(
				"import net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto.DbbeansConfig4ProtoOuterClass.DbbeansConfig4Proto;\n");

		for (String dbbeanClassName : dbbeansList) {
			String propNameTmp = dbbeanClassName.replaceAll("4Proto", "");
			propNameTmp = Character.toLowerCase(propNameTmp.charAt(0)) + "" + propNameTmp.substring(1);
			sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto." + dbbeanClassName
					+ "OuterClass.*;\n");
		}

		sb.append("\n");
		sb.append("public class DbbeansConfig4ProtoUtil {\n");
		sb.append("\n");
		sb.append(
				"	public static DbbeansConfig4Proto.Builder addDbcpBuilder(int zoneId, DbbeansConfig4Proto.Builder dbcb) {\n");

		for (String dbbeanClassName : dbbeansList) {
			String dbBeanname = dbbeanClassName.replaceAll("4Proto", "");
			String propNameTmp = dbbeanClassName.replaceAll("4Proto", "");
			propNameTmp = Character.toLowerCase(propNameTmp.charAt(0)) + "" + propNameTmp.substring(1);
			String noClientCsv = NoClientCSVUtil.getDbColNoClientCSVIndexByClassName(dbbeanClassName);
			if ("no".equals(noClientCsv)) {// 不能传给客户端的表需要过滤掉
				sb.append("		//" + dbBeanname + "4ProtoArray a" + dbBeanname + "4ProtoArray = get" + dbBeanname
						+ "4ProtoArray(zoneId);\n");
				sb.append("		//dbcb.set" + dbBeanname + "Array(a" + dbBeanname + "4ProtoArray);\n");
			} else {
				sb.append("		" + dbBeanname + "4ProtoArray a" + dbBeanname + "4ProtoArray = get" + dbBeanname
						+ "4ProtoArray(zoneId);\n");
				sb.append("		dbcb.set" + dbBeanname + "Array(a" + dbBeanname + "4ProtoArray);\n");
			}
		}
		sb.append("		return dbcb;\n");
		sb.append("	}\n");
		sb.append("\n");

		for (String dbbeanClassName : dbbeansList) {
			String dbBeanname = dbbeanClassName.replaceAll("4Proto", "");
			String propNameTmp = dbbeanClassName.replaceAll("4Proto", "");
			propNameTmp = Character.toLowerCase(propNameTmp.charAt(0)) + "" + propNameTmp.substring(1);
			String noClientCsv = NoClientCSVUtil.getDbColNoClientCSVIndexByClassName(dbbeanClassName);
			if ("no".equals(noClientCsv)) {// 不能传给客户端的表需要过滤掉
				sb.append("//nomethod for	get" + dbBeanname + "4ProtoArray(int zoneId)\n");
				continue;
			}
			sb.append("	public static " + dbBeanname + "4ProtoArray get" + dbBeanname + "4ProtoArray(int zoneId) {\n");
			sb.append("		DataPage<" + dbBeanname + "> dpTmp = " + dbBeanname + "DAOAdvance."
					+ XmlCodeGen.getTheDAOStatament(dbBeanname) + "\n");
			sb.append("				.getAll();\n");
			sb.append("		" + dbBeanname + "ProtoBufConverter pbc = new " + dbBeanname + "ProtoBufConverter();\n");
			sb.append("		" + dbBeanname + "4ProtoDataPage.Builder b = pbc.dataPageBean2ProtoBean(dpTmp);\n");
			sb.append("		" + dbBeanname + "4ProtoArray.Builder tppab = b.get" + dbBeanname
					+ "Array().toBuilder();\n");
			// sb.append("/*\n");
			sb.append("		List<" + dbBeanname + "4Proto.Builder> list=tppab.get" + dbBeanname
					+ "ItemsBuilderList();\n");
			sb.append("		list = new ArrayList<" + dbBeanname + "4Proto.Builder>(list);\n");
			sb.append("		tppab.clear" + dbBeanname + "Items();\n");
			sb.append("		for(" + dbBeanname + "4Proto.Builder item:list){\n");
			// 在这里插入清理冗余字段的操作
			Set<String> setTmp = NoClientCSVUtil.getDbColNoClientCSVColumnsByClassName(dbbeanClassName);
			for (String propNameTmpAA : setTmp) {
				sb.append("item.clear" + CSVUtil.capFirst(DBUtil.camelName(propNameTmpAA)) + "(); // nocllientcsv\n");
			}
			sb.append("			tppab.add" + dbBeanname + "Items(item);\n");
			sb.append("		}\n");

			// sb.append("*/\n");
			sb.append("		tppab.clearLastUpdateTime();\n");
			sb.append("		tppab.clearLastUpdatedTime();\n");
			sb.append("		return tppab.build();\n");
			sb.append("	}\n");
			sb.append("\n");

		}
		sb.append("	public static void main(String[] args) {\n");
		sb.append("		int zoneId = 40001;\n");
		sb.append("		DbbeansConfig4Proto.Builder dbcb = DbbeansConfig4Proto.newBuilder();\n");
		sb.append("		addDbcpBuilder(zoneId, dbcb);\n");
		sb.append("\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public static void fillDb(DbbeansConfig4Proto.Builder dbcb, int zoneId) {\n");
		sb.append("\n");

		for (String dbbeanClassName : dbbeansList) {
			String dbBeanname = dbbeanClassName.replaceAll("4Proto", "");
			String propNameTmp = dbbeanClassName.replaceAll("4Proto", "");
			propNameTmp = Character.toLowerCase(propNameTmp.charAt(0)) + "" + propNameTmp.substring(1);
			sb.append("		{\n");
			sb.append("			DataPage<" + dbBeanname + "> dp = " + dbBeanname + "DAOAdvance."
					+ XmlCodeGen.getTheDAOStatament(dbBeanname) + ".getAll();\n");
			sb.append("			" + dbBeanname + "ProtoBufConverter pbc = new " + dbBeanname
					+ "ProtoBufConverter();\n");
			sb.append("			" + dbBeanname + "4ProtoDataPage.Builder b = pbc.dataPageBean2ProtoBean(dp);\n");
			sb.append("			dbcb.set" + dbBeanname + "Array(b.get" + dbBeanname + "Array());\n");
			sb.append("		}\n");
		}
		sb.append("	}\n");

		sb.append("\n");
		sb.append("}\n");
		File protoFile = new File(CodeGenConsts.PROJSERVER_JAVASRCROOT
				+ "/../protojavarpcimpls/net/bilinkeji/gecaoshoulie/mgameprotorpc/logics/common/DbbeansConfig4ProtoUtil.java");
		System.out.println("DbbeansConfig4ProtoUtil|genfile=" + protoFile.getAbsolutePath());
		writeFile(protoFile.getAbsolutePath(), sb.toString());
	}

	private static void genProtoBufDBBeansProtoBufConfigUtilFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("using UnityEditor;\n");
		sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto;\n");
		sb.append("\n");
		sb.append("public class GecaoshoulieConfigUtil {\n");
		sb.append("	\n");

		// sb.append(" public static bool forceLoadBytes=false;\n");
		sb.append("	[MenuItem(\"DailyBuild/SaveProtobufFiles\")]\n");
		sb.append("	public static void SaveProtobufFiles ()\n");
		sb.append("	{\n");
		for (String dbbeanClassName : dbbeansList) {
			String noClientCsv = NoClientCSVUtil.getDbColNoClientCSVIndexByClassName(dbbeanClassName);
			if ("no".equals(noClientCsv)) {
				sb.append("//");
			}
			sb.append("		" + dbbeanClassName + "Array " + dbbeanClassName.toLowerCase() + "Array = "
					+ dbbeanClassName + "ArrayCsvHelper.load" + dbbeanClassName + "ArrayFromCsvFile ();\n");
			sb.append("		//" + dbbeanClassName + "ArrayCsvHelper.saveProtobufBytes (" + dbbeanClassName.toLowerCase()
					+ "Array);\n");

		}

		sb.append("		DbbeansConfig4Proto dbbeansConfig4Proto = new DbbeansConfig4Proto ();\n");

		for (String dbbeanClassName : dbbeansList) {
			String propNameTmp = dbbeanClassName.replaceAll("4Proto", "");
			propNameTmp = Character.toLowerCase(propNameTmp.charAt(0)) + "" + propNameTmp.substring(1);
			String noClientCsv = NoClientCSVUtil.getDbColNoClientCSVIndexByClassName(dbbeanClassName);
			if ("no".equals(noClientCsv)) {
				sb.append("		//dbbeansConfig4Proto." + propNameTmp + "Array= " + propNameTmp.toLowerCase()
						+ "4protoArray;\n");
			} else {
				sb.append("		dbbeansConfig4Proto." + propNameTmp + "Array= " + propNameTmp.toLowerCase()
						+ "4protoArray;\n");
			}

		}
		sb.append(
				"		Bilinkeji.Common.ProtoBufConfigUtil.saveDbbeansConfig4ProtoProtobufBytes (dbbeansConfig4Proto);\n");
		sb.append("\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("}\n");

		File protoFile = new File(new File(CodeGenConsts.PROJPROTO_UnityEditorSRCROOT, "CITools"),
				"GecaoshoulieConfigUtil.cs");
		writeFile(protoFile.getAbsolutePath(), sb.toString());
	}

	private static void genProtoBufDBBeansProtoBufConfigUtilFile2() {
		StringBuilder sb = new StringBuilder();
		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("using System.IO;\n");
		sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto;\n");
		sb.append("\n");
		sb.append("namespace Bilinkeji.Common{\n");
		sb.append("public class ProtoBufConfigUtil {\n");
		sb.append("	\n");
		sb.append("\n");
		sb.append("		public static bool localConfigLoadDone = false;\n");
		sb.append("		public static DbbeansConfig4Proto dbbeansConfigLocal = null; \n");
		sb.append("\n");
		sb.append("		public static void saveDbbeansConfigLocal (int zoneId)\n");
		sb.append("		{\n");
		sb.append("			try{\n");
		sb.append("				if(dbbeansConfigLocal!=null){\n");
		sb.append(
				"					string bytesFilePath = BLDebug.unityPersistentDataPath + \"/DbbeansConfig4Proto_\"+zoneId+\".bytes\";\n");
		sb.append(
				"					string bytesDefFilePath = BLDebug.unityPersistentDataPath + \"/DbbeansConfig4Proto_0.bytes\";\n");
		sb.append("					FileInfo sourcedi = new FileInfo (bytesFilePath);\n");
		sb.append("					string fn = sourcedi.FullName;\n");
		sb.append("					BLDebug.Log (\"saveDbbeansConfigLocal==\" + bytesFilePath + \"|\" + fn);\n");
		sb.append(
				"					byte[] bytes = net.bilinkeji.common.util.ProtoBufUtil.ProtoBufToBytes (dbbeansConfigLocal);\n");
		sb.append(
				"					net.bilinkeji.common.util.FileUtil.WriteAllBytes (bytesFilePath, bytes);				\n");
		sb.append(
				"					net.bilinkeji.common.util.FileUtil.WriteAllBytes (bytesDefFilePath, bytes);				\n");

		sb.append("					//Debug.Log (showItemCountInfo(dcp));\n");
		sb.append("				}\n");
		sb.append("\n");
		sb.append("			}catch(System.Exception ex){\n");
		sb.append("				Debug.LogException(ex);\n");
		sb.append("			}\n");
		sb.append("		}\n");

		sb.append("		public static string showItemCountInfo(DbbeansConfig4Proto dcp){\n");
		sb.append("			System.Text.StringBuilder sb = new System.Text.StringBuilder();\n");

		for (String dbbeanClassName : dbbeansList) {
			String propNameTmp = dbbeanClassName.replaceAll("4Proto", "");
			propNameTmp = Character.toLowerCase(propNameTmp.charAt(0)) + "" + propNameTmp.substring(1);
			sb.append("			{	\n");
			sb.append("				int itemCount = 0;\n");
			sb.append("				if (dcp." + propNameTmp + "Array != null) {\n");
			sb.append("					itemCount = dcp." + propNameTmp + "Array." + propNameTmp + "Items.Count;\n");
			sb.append("				}\n");
			sb.append("				sb.Append(\"dcp." + propNameTmp + "Array.itemCount=\"+itemCount).Append(\"|\");\n");
			sb.append("			}\n");

		}
		sb.append("		\n");

		sb.append("		return sb.ToString ();\n");
		sb.append("		}\n");

		sb.append("		public static bool loadDbbeansConfigLocal (int zoneId)\n");
		sb.append("		{	\n");
		sb.append("			try{				\n");
		sb.append(
				"			string bytesFilePath = BLDebug.unityPersistentDataPath + \"/DbbeansConfig4Proto_\"+zoneId+\".bytes\";\n");
		sb.append("			FileInfo sourcedi = new FileInfo (bytesFilePath);\n");
		sb.append("			if(net.bilinkeji.common.util.FileUtil.Exists(bytesFilePath)){\n");
		sb.append("				byte[] bytes = net.bilinkeji.common.util.FileUtil.ReadAllBytes (bytesFilePath);\n");
		sb.append(
				"				DbbeansConfig4Proto dbbeansConfig = net.bilinkeji.common.util.ProtoBufUtil.ProtoBufFromBytes<DbbeansConfig4Proto>(bytes);\n");

		sb.append("			    syncAllDbbeansConfig(dbbeansConfig);\n");
		sb.append("			    return true;\n");
		sb.append("			}\n");
		sb.append("			}catch(System.Exception ex){\n");
		sb.append("				Debug.LogException(ex);\n");
		sb.append("			}\n");
		sb.append("		return false;\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append("	public static void saveDbbeansConfig4ProtoProtobufBytes (DbbeansConfig4Proto dcp)\n");
		sb.append("	{\n");
		sb.append(
				"		if (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer) {\n");
		sb.append("			return;\n");
		sb.append("		}\n");
		sb.append(
				"		string bytesFilePath = Application.streamingAssetsPath + \"/ProtoBufBytes/DbbeansConfig4Proto.bytes\";\n");
		sb.append("		FileInfo sourcedi = new FileInfo (bytesFilePath);\n");
		sb.append("		string fn = sourcedi.FullName;\n");
		sb.append("		byte[] bytes = net.bilinkeji.common.util.ProtoBufUtil.ProtoBufToBytes (dcp);\n");
		sb.append(
				"		BLDebug.Log (\"saveDbbeansConfig4ProtoProtobufBytes.len=\" + bytes.Length + \"|\" + bytesFilePath + \"|\" + fn);\n");
		sb.append("		net.bilinkeji.common.util.FileUtil.WriteAllBytes (bytesFilePath, bytes);\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public static void loadAllProtobufFilesByDbbeansConfig4Proto ()\n");
		sb.append("	{\n");
		sb.append("\n");
		sb.append(" localConfigLoadDone = false;\n");
		sb.append(
				"		if (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer) {\n");
		sb.append("			loadDbbeansConfig4ProtoProtobufBytes (delegate(DbbeansConfig4Proto obj) {\n");
		sb.append("				syncAllDbbeansConfig (obj);\n");
		sb.append(" 			localConfigLoadDone = true;\n");
		sb.append("			}, delegate(string msg) {\n");

		sb.append(
				"				BLDebug.LogError (\"ProtoBufConfigUtil.loadAllProtobufFilesByDbbeansConfig4Proto error:\" + msg);\n");
		sb.append("			});\n");
		sb.append("		} else {\n");
		sb.append("			loadAllProtobufFiles ();\n");
		sb.append(" 		localConfigLoadDone = true;\n");
		sb.append("		}\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("\n");
		sb.append(
				"	public static void loadDbbeansConfig4ProtoProtobufBytes (System.Action<DbbeansConfig4Proto> loadSuccCallBack, System.Action<string > failCallBack)\n");
		sb.append("	{\n");
		sb.append(
				"		if (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer) {\n");
		sb.append(
				"			string bytesFilePath = Application.streamingAssetsPath + \"/ProtoBufBytes/DbbeansConfig4Proto.bytes\";\n");

		sb.append(
				"			string bytesSavedFilePath = BLDebug.unityPersistentDataPath + \"/DbbeansConfig4Proto_0.bytes\";\n");
		sb.append("			Debug.LogError (\"看看有没有保存了的默认配置文件，优先加载\"+bytesSavedFilePath);\n");
		sb.append("			if(net.bilinkeji.common.util.FileUtil.Exists(bytesSavedFilePath)){\n");
		sb.append("				Debug.LogError (\"已经有保存了的默认配置文件，优先加载\"+bytesSavedFilePath);\n");
		sb.append("				bytesFilePath = bytesSavedFilePath;\n");
		sb.append("			}\n");
		sb.append("			if (!bytesFilePath.Contains (\"://\")) {\n");
		sb.append("				bytesFilePath = \"file://\" + bytesFilePath;\n");
		sb.append("			}\n");
		sb.append(
				"			net.bilinkeji.common.util.WWWResLoadUtil.loadProtoBufObjectByWWW<DbbeansConfig4Proto> (bytesFilePath, loadSuccCallBack, failCallBack);\n");
		sb.append("		} \n");
		sb.append("	}\n");
		sb.append("\n");

		sb.append("	public static void loadAllProtobufFiles ()\n");
		sb.append("	{\n");
		for (String dbbeanClassName : dbbeansList) {
			sb.append("		" + dbbeanClassName + "ArrayCsvHelper.loadBytesOnAndroidOrIOS ();\n");
		}
		sb.append("	}\n");

		sb.append("		public static void syncAllDbbeansConfig (DbbeansConfig4Proto dbbeansConfig)\n");
		sb.append("		{\n");
		sb.append("			if (dbbeansConfig != null) {\n");
		sb.append("\n");
		sb.append("				if(dbbeansConfigLocal == null){\n");
		sb.append("					BLDebug.LogWarning(\"dbbeansConfigLocal not init \");\n");
		sb.append("					dbbeansConfigLocal = new DbbeansConfig4Proto();\n");
		sb.append("				}\n");

		sb.append("				dbbeansConfigLocal.dbVerList.Clear();\n");
		sb.append("				dbbeansConfigLocal.dbVerList.AddRange(dbbeansConfig.dbVerList);\n");
		for (String dbbeanClassName : dbbeansList) {
			String propNameTmp = dbbeanClassName.replaceAll("4Proto", "");
			propNameTmp = Character.toLowerCase(propNameTmp.charAt(0)) + "" + propNameTmp.substring(1);

			String noClientCsv = NoClientCSVUtil.getDbColNoClientCSVIndexByClassName(dbbeanClassName);
			if ("no".equals(noClientCsv)) {// 不能传给客户端的表需要过滤掉
				sb.append("// no clientcsv for dbbeansConfig." + propNameTmp + "\n");
			} else {
				sb.append("				if (dbbeansConfig." + propNameTmp + "Array != null && dbbeansConfig."
						+ propNameTmp + "Array." + propNameTmp + "Items != null && dbbeansConfig." + propNameTmp
						+ "Array." + propNameTmp + "Items.Count > 0) {\n");
				sb.append("					" + dbbeanClassName + "ArrayCsvHelper.syncServerConfig (dbbeansConfig."
						+ propNameTmp + "Array);						\n");

				sb.append("					dbbeansConfigLocal." + propNameTmp + "Array = dbbeansConfig." + propNameTmp
						+ "Array;\n");
				sb.append("				}\n");
			}
		}

		sb.append("			}\n");
		sb.append("		}\n");

		sb.append("}\n");
		sb.append("\n");
		sb.append("}\n");

		File protoFile = new File(CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
				+ "/../Bilinkeji/Common/ProtoBufConfigUtil.cs");
		writeFile(protoFile.getAbsolutePath(), sb.toString());
		System.err.println(protoFile);
	}

	private static void genProtoBufDBBeansProtoBufnetCSVHelperFile(File csvFile, String tableName,
			List<String[]> colList) {
		if (tableName == null || !tableName.toLowerCase().startsWith("t")) {
			return;
		}

		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		dbbeansList.add(className);
		String protoFileDir = CodeGenConsts.PROJPROTO_UnitySRCROOT + "/"
				+ (CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".dbbeans4proto").replace(".", "/");
		String protoCsvHelperFileName = className + "ArrayCsvHelper.cs";

		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		String propName4Class = Character.toLowerCase(className.charAt(0))
				+ className.substring(1, className.length() - 6);
		StringBuilder sb = new StringBuilder();
		sb.append("using System;\n");
		sb.append("using System.Collections;\n");
		sb.append("using System.IO;\n");
		sb.append("using System.Collections.Generic;\n");
		sb.append("using System.Text;\n");
		sb.append("using UnityEngine;\n");
		sb.append("using " + CodeGenConsts.PROJUTILS_CSHARPPACKAGEROOT + ";\n");
		sb.append("using net.bilinkeji.common.util;\n");
		sb.append("\n");
		sb.append("namespace " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".dbbeans4proto\n{\n");
		sb.append("public class " + className + "ArrayCsvHelper  {\n");
		sb.append("	\n");

		sb.append("		private static " + className + "Array localArray = null;\n");
		sb.append("		private static Dictionary<" + getFixCsharpTypebyJavaType(colTypes[0]) + "," + className
				+ "> localDic = null;\n");
		sb.append("		public static void loadBytesOnAndroidOrIOS ()\n");
		sb.append("		{\n");
		sb.append("			loadProtobufBytes (delegate(" + className + "Array obj) {\n");
		sb.append("				" + className + "ArrayCsvHelper.localArray = obj;\n");
		sb.append("			}, delegate(string msg) {\n");
		sb.append("				BLDebug.LogError (\"" + className
				+ "ArrayCsvHelper.loadBytesOnAndroidOrIOS error:\" + msg);\n");
		sb.append("			});\n");
		sb.append("		}\n");
		sb.append("\n");

		sb.append("		public static void syncServerConfig (" + className + "Array arr)\n");
		sb.append("		{\n");
		sb.append("			if (arr != null) {\n");
		sb.append("				localArray = arr;\n");

		sb.append("				if (localDic == null) {\n");
		sb.append("					localDic = new Dictionary<" + getFixCsharpTypebyJavaType(colTypes[0]) + ", "
				+ className + "> ();\n");
		sb.append("				} else {\n");
		sb.append("					localDic.Clear ();\n");
		sb.append("				}\n");
		sb.append("				for(int i=0;i<arr." + propName4Class + "Items.Count;i++){\n");
		sb.append("					" + className + " tcp=arr." + propName4Class + "Items[i];\n");
		sb.append("					localDic [tcp." + DBUtil.camelName(colNames[0].toLowerCase()) + "] = tcp;\n");
		sb.append("				}\n");
		sb.append("			}\n");
		sb.append("		}\n");

		sb.append("		public static List<" + className + ">  GetList ()\n");
		sb.append("		{\n");
		sb.append("			//List<" + className + "> list = new List<" + className + "> ();\n");
		sb.append("			//list.AddRange (load" + className + "ArrayFromCsvFile ()." + propName4Class + "Items);\n");
		sb.append("			//return list;\n");
		sb.append("			return load" + className + "ArrayFromCsvFile ()." + propName4Class + "Items;\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append("		public static " + className + "  GetById (" + getFixCsharpTypebyJavaType(colTypes[0]) + " "
				+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp)\n");
		sb.append("		{\n");
		sb.append("			if(localArray!=null && localDic!=null && localDic.Count>0){\n");
		sb.append("				" + className + " val = null;\n");
		sb.append("				if(localDic.TryGetValue(" + DBUtil.camelName(colNames[0].toLowerCase())
				+ "Tmp,out val)){\n");
		sb.append("					return val;\n");
		sb.append("				}\n");
		sb.append("			}\n");

		sb.append("			return get" + className + "byId (load" + className + "ArrayFromCsvFile (), "
				+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp);\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append("		public static " + className + "  GetBy (System.Func<" + className + ",bool> compareFunc)\n");
		sb.append("		{\n");
		sb.append("			return get" + className + " (load" + className + "ArrayFromCsvFile (), compareFunc);\n");
		sb.append("		}\n");

		sb.append("		public static bool ExistsById (" + getFixCsharpTypebyJavaType(colTypes[0]) + " "
				+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp)\n");
		sb.append("		{\n");
		sb.append("			return Exists" + className + "ById (load" + className + "ArrayFromCsvFile (), "
				+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp);\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append("		public static bool ExistsBy (System.Func<" + className + ",bool> compareFunc)\n");
		sb.append("		{\n");
		sb.append("			return get" + className + " (load" + className
				+ "ArrayFromCsvFile (), compareFunc) != null;\n");
		sb.append("		}\n");

		String csvFullPath = csvFile.getAbsoluteFile().toString().replace('\\', '/');
		String csvTmpPath = csvFullPath.substring(csvFullPath.indexOf("csvfiles"));
		sb.append("	public static " + className + "Array load" + className + "ArrayFromCsvFile(){\n");
		sb.append("			if (localArray != null ) {\n");
		sb.append("				return localArray;\n");
		sb.append("			}\n");
		sb.append("	String csvFilePath = \"/" + csvTmpPath + "\";\n");
		sb.append("	return load" + className + "ArrayFromCsvFile( csvFilePath );\n");

		sb.append("	}\n");
		sb.append("	public static " + className + "Array load" + className + "ArrayFromCsvFile2Memory(){\n");
		sb.append("			if (localArray != null ) {\n");
		sb.append("				return localArray;\n");
		sb.append("			}\n");
		sb.append("	String csvFilePath = \"/" + csvTmpPath + "\";\n");
		sb.append("	localArray = load" + className + "ArrayFromCsvFile( csvFilePath );\n");
		sb.append("	return localArray;\n");

		sb.append("	}\n");
		sb.append("#if UNITY_EDITOR\n");
		sb.append("public static void CleanLocalArray(){\n");
		sb.append("	localArray = null;\n");
		sb.append("}\n");
		sb.append("	public static " + className + "Array load" + className + "ArrayFromCsvFile(String csvFilePath){\n");
		sb.append(
				"		string SourcePath = Application.streamingAssetsPath + \"/../../../../../gecaoshoulie_configs/\";\n");
		sb.append(
				"			if (new DirectoryInfo (\"/Users\").Exists && new DirectoryInfo (\"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs\").Exists) {\n");
		sb.append("				SourcePath = \"/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/\";\n");
		sb.append("			}\n");
		sb.append(
				"		if(new DirectoryInfo (\"D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs\").Exists){\n");
		sb.append("			SourcePath = \"D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/\";\n");
		sb.append("			//Debug.LogError (\"windwos下 csv 文件夹路径：\" + SourcePath);\n");
		sb.append("		}\n");
		sb.append("		DirectoryInfo sourcedi = new DirectoryInfo (SourcePath);\n");
		sb.append("		SourcePath = sourcedi.FullName;\n");
		sb.append("		CsvParser2 cp2 = new CsvParser2 ();\n");
		sb.append("		string csvPath = SourcePath+csvFilePath;\n");
		sb.append(
				"		//Debug.Log (\"loadTuserReginfo4ProtoArrayFromCsvFile|csvDir=\" + sourcedi.FullName + \",csvPath=\" + csvPath);\n");
		sb.append("		Encoding enc = Encoding.UTF8;\n");
		sb.append(
				"		if (Application.platform == RuntimePlatform.OSXEditor || Application.platform == RuntimePlatform.OSXPlayer\n");
		sb.append(
				"		   || Application.platform == RuntimePlatform.WindowsEditor || Application.platform == RuntimePlatform.WindowsPlayer) {\n");
		sb.append("			enc = Encoding.GetEncoding (\"UTF-8\");\n");
		sb.append("		} else {\n");
		sb.append("			csvPath = Application.streamingAssetsPath + \"/" + csvTmpPath + "\";\n");
		sb.append("		}		\n");

		sb.append("		byte[] csvBytes=FileUtil.ReadAllBytes (csvPath);\n");
		sb.append("		string csvUTF-8Str = enc.GetString(csvBytes);\n");
		sb.append("		" + className + "Array tcpa = new " + className + "Array (); \n");
		sb.append("		tcpa.lastUpdateTime = TimeUtil.currentTimeMillis ();\n");
		sb.append("		using (StringReader reader = new StringReader(csvUTF-8Str))\n");
		sb.append("		{\n");
		sb.append("			string[][] csvRows = cp2.Parse(reader);\n");
		sb.append("			csvRows = StringUtil.TrimGameCsvRows (csvRows);\n");
		sb.append("			for(int i=4;i<csvRows.Length;i++){\n");
		sb.append("				string[] cols=csvRows[i];\n");
		sb.append("				" + className + " tcp=new " + className + "();\n");

		Map<String, String> localeKeyMap = I18NUtil.getI18NDbColLocaleKey(tableName, colList);
		for (int i = 0; i < colNames.length; i++) {
			if (colNames[i].trim().length() > 0) {

				String propName = DBUtil.camelName(colNames[i].toLowerCase());
				String noClienCsvFlag = NoClientCSVUtil.getDbColNoClientCSVIndex(tableName, colNames[i].toLowerCase());
				if ("noaaaaa".equals(noClienCsvFlag)) {
					sb.append("/// no used4client for " + propName + "\n");
					continue;
				}
				String propType = getProtoTypebyJavaType(colTypes[i]);
				if ("int32".equalsIgnoreCase(propType)) {
					if (colTypes[i].contains("[]")) {
						sb.append("				tcp." + propName + ".Clear ();\n");
						sb.append("				tcp." + propName + ".AddRange(StringUtil.ToInt32Array(cols[" + i
								+ "]));\n");
					} else {
						sb.append("				int " + propName + "Tmp=0;\n");
						sb.append("				int.TryParse(cols[" + i + "],out " + propName + "Tmp);\n");
						sb.append("				tcp." + propName + " = " + propName + "Tmp;\n");
					}
				} else if ("int64".equalsIgnoreCase(propType)) {
					if (colTypes[i].contains("[]")) {
						sb.append("				tcp." + propName + ".Clear ();\n");
						sb.append("				tcp." + propName + ".AddRange(StringUtil.ToInt64Array(cols[" + i
								+ "]));\n");
					} else {
						sb.append("				long " + propName + "Tmp=0;\n");
						sb.append("				long.TryParse(cols[" + i + "],out " + propName + "Tmp);\n");
						sb.append("				tcp." + propName + " = " + propName + "Tmp;\n");
					}
				} else if ("float".equalsIgnoreCase(propType)) {
					if (colTypes[i].contains("[]")) {
						sb.append("				tcp." + propName + ".Clear ();\n");
						sb.append("				tcp." + propName + ".AddRange(StringUtil.ToSingleArray(cols[" + i
								+ "]));\n");
					} else {
						sb.append("				float " + propName + "Tmp=0f;\n");
						sb.append("				float.TryParse(cols[" + i + "],out " + propName + "Tmp);\n");
						sb.append("				tcp." + propName + " = " + propName + "Tmp;\n");
					}
				} else if ("double".equalsIgnoreCase(propType)) {
					if (colTypes[i].contains("[]")) {
						sb.append("				tcp." + propName + ".Clear ();\n");
						sb.append("				tcp." + propName + ".AddRange(StringUtil.ToSingleArray(cols[" + i
								+ "]));\n");
					} else {
						sb.append("				double " + propName + "Tmp=0f;\n");
						sb.append("				double.TryParse(cols[" + i + "],out " + propName + "Tmp);\n");
						sb.append("				tcp." + propName + " = " + propName + "Tmp;\n");
					}
				} else {
					if (colTypes[i].contains("[]")) {
						sb.append("				tcp." + propName + ".Clear ();\n");
						sb.append("				tcp." + propName + ".AddRange(StringUtil.ToStringArray(cols[" + i
								+ "]));\n");
					} else {
						sb.append("				tcp." + propName + " = cols[" + i + "].Trim();\n");
						sb.append("				if(tcp." + propName + ".Equals(\"-1\")){\n");
						sb.append("					tcp." + propName + " = \"\";\n");

						sb.append("				} else {\n");
						sb.append("					tcp." + propName + " = StringUtil.TryParse2UnityRichText (tcp."
								+ propName + ");\n");
						sb.append("				}\n");
						// 在这里插入多语言的支持。
						String localeKey = localeKeyMap.getOrDefault(colNames[i].toLowerCase(), "");
						if (localeKey.length() > 0) {
							sb.append("					{\n");
							sb.append("						string localeKey = \"" + localeKey + "\" + tcp."
									+ DBUtil.camelName(colNames[0].toLowerCase()) + ";\n");
							sb.append(
									"						string localeValue = BilinCsvLocaleUtil.GetLocaleValue (localeKey);\n");
							sb.append("						if(localeValue.Length>0){\n");
							sb.append("							tcp." + propName + " = localeValue;\n");
							sb.append("						}\n");
							sb.append("					}\n");
						}

					}

				}
			}
		}

		/*
		 * sb.append("				tcp.clientChannelKey = cols[0];\n");
		 * sb.append("				tcp.channelValue = cols[1];\n"); sb.append(
		 * "				tcp.channelDesc = cols[2];\n");
		 */

		sb.append("				tcpa." + propName4Class + "Items.Add(tcp);\n");
		sb.append("			}\n");
		sb.append("		}\n");
		sb.append("		return tcpa;\n");
		sb.append("	}\n");

		sb.append("		#else\n");
		sb.append("	public static " + className + "Array load" + className + "ArrayFromCsvFile(String csvFilePath){\n");
		sb.append("		" + className + "Array tcpa = new " + className + "Array (); \n");
		sb.append("			return tcpa;\n");
		sb.append("		}\n");
		sb.append("		#endif\n");

		if (false) {
			sb.append("		public static bool setValueByPropertyName(" + className
					+ " obj,String propertyName,object value){\n");
			sb.append("			if(obj == null ||propertyName ==null ||value ==null){\n");
			sb.append("				return false;\n");
			sb.append("			}\n");

			sb.append("			switch (propertyName) {\n");
			for (int i = 0; i < colNames.length; i++) {
				if (colNames[i].trim().length() > 0) {
					String propNameTmp = DBUtil.camelName(colNames[i].toLowerCase());
					sb.append("			case \"" + propNameTmp + "\":\n");
					sb.append("				{\n");

					String propTypeTmp = getFixCsharpTypebyJavaType(colTypes[i]);
					if (colTypes[i].contains("[]")) {
						sb.append("			var valueTmp = value as " + colTypes[i] + ";\n");
						sb.append("			if (valueTmp != null) {\n");
						sb.append("				obj." + propNameTmp + ".Clear ();\n");
						sb.append("				obj." + propNameTmp + ".AddRange (valueTmp);\n");
						sb.append("				return true;\n");
						sb.append("			}\n");

					} else {

						sb.append("			if(typeof(" + propTypeTmp + ").Equals(value.GetType())){\n");
						sb.append("				obj." + propNameTmp + " = (" + propTypeTmp + ")value;\n");
						sb.append("				return true;\n");
						sb.append("			}\n");
					}

					sb.append("					return false;\n");
					sb.append("				}\n");
				}
			}

			sb.append("			default:\n");
			sb.append("				{\n");
			sb.append(
					"			BLDebug.LogWarningFormat(\"no propertyName {0}|or no type {1}\",propertyName,value.GetType());\n");
			sb.append("			return false;\n");
			sb.append("				}\n");
			sb.append("			}\n");

			sb.append("\n");
			sb.append("		}\n");
		}
		if ("TskillAnimationinfo4Proto".equals(className)) {
			sb.append("\n");
			sb.append("		public static T getValueByPropertyName<T>(" + className + " obj,String propertyName)\n");
			sb.append("		{\n");
			sb.append("			T rs = default(T);\n");
			sb.append("			if(obj == null ||propertyName ==null ){\n");
			sb.append("				return rs;\n");
			sb.append("			}\n");
			sb.append("			switch (propertyName) {\n");
			for (int i = 0; i < colNames.length; i++) {
				if (colNames[i].trim().length() > 0) {
					String propNameTmp = DBUtil.camelName(colNames[i].toLowerCase());
					sb.append("			case \"" + propNameTmp + "\":\n");
					sb.append("				{\n");
					sb.append("					return (T)((object)obj." + propNameTmp + ");\n");
					sb.append("				}\n");
				}
			}

			sb.append("			default:\n");
			sb.append("				{\n");
			sb.append("					BLDebug.LogWarning (\"no propertyName|\" + propertyName);\n");
			sb.append("					return rs;\n");
			sb.append("				}\n");
			sb.append("			}\n");

			sb.append("		}\n");
		}

		sb.append("		//把不存在的id记录一下，仅用于打日志判断，同一个不存在的id，只打一次日志\n");
		sb.append("		static HashSet<" + getFixCsharpTypebyJavaType(colTypes[0]) + "> notExistIdList=new HashSet<"
				+ getFixCsharpTypebyJavaType(colTypes[0]) + ">();\n");
		sb.append("		public static " + className + " get" + className + "byId(" + className + "Array tcpa,"
				+ getFixCsharpTypebyJavaType(colTypes[0]) + " " + DBUtil.camelName(colNames[0].toLowerCase())
				+ "Tmp){\n");
		// sb.append(" return get" + className + " (tcpa, (tcp) =>{\n");
		// sb.append(" return tcp != null && tcp." +
		// DBUtil.camelName(colNames[0].toLowerCase()) + " == "
		// + DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp;\n");
		// sb.append(" });\n");

		sb.append("			" + className + " tmp = get" + className + " (tcpa, (tcp) => {\n");
		sb.append("				return tcp != null && tcp." + DBUtil.camelName(colNames[0].toLowerCase()) + " == "
				+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp;\n");
		sb.append("			});\n");
		sb.append("			if (tmp == null) {\n");
		sb.append("				tmp = get" + className + " (tcpa, (tcp) => {\n");
		if ("string".equals(getFixCsharpTypebyJavaType(colTypes[0]))) {
			sb.append("					return tcp != null && tcp." + DBUtil.camelName(colNames[0].toLowerCase())
					+ " == \"null\";//潜规则记录\n");
		} else {
			sb.append("					return tcp != null && tcp." + DBUtil.camelName(colNames[0].toLowerCase())
					+ " == -1000;//潜规则记录\n");

		}
		sb.append("				});\n");
		sb.append("			}\n");

		sb.append("		#if UNITY_EDITOR\n");
		sb.append("		if(tmp==null){\n");
		if ("string".equals(getFixCsharpTypebyJavaType(colTypes[0]))) {
			sb.append("			if(!notExistIdList.Contains(" + DBUtil.camelName(colNames[0].toLowerCase())
					+ "Tmp)){\n");
			sb.append("				notExistIdList.Add(" + DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp);\n");
			sb.append("			BLDebug.LogWarningFormat(\"CSV|get" + className + "byId|isnull|for|{0}\","
					+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp);	\n");
			sb.append("		}	\n");
		} else {
			sb.append("			//暂时只对大于0的打警告日志\n");
			sb.append("			if(" + DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp > 0){\n");

			sb.append("			if(!notExistIdList.Contains(" + DBUtil.camelName(colNames[0].toLowerCase())
					+ "Tmp)){\n");
			sb.append("				notExistIdList.Add(" + DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp);\n");
			sb.append("			BLDebug.LogWarningFormat(\"CSV|get" + className + "byId|isnull|for|{0}\","
					+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp);	\n");

			sb.append("		}	\n");
			sb.append("			}	\n");

		}

		sb.append("		}\n");
		sb.append("		#endif\n");

		sb.append("			return tmp;\n");

		sb.append("		}\n");

		sb.append("		public static bool Exists" + className + "ById(" + className + "Array tcpa,"
				+ getFixCsharpTypebyJavaType(colTypes[0]) + " " + DBUtil.camelName(colNames[0].toLowerCase())
				+ "Tmp){\n");
		sb.append("			" + className + " tmp = get" + className + " (tcpa, (tcp) => {\n");
		sb.append("				return tcp != null && tcp." + DBUtil.camelName(colNames[0].toLowerCase()) + " == "
				+ DBUtil.camelName(colNames[0].toLowerCase()) + "Tmp;\n");
		sb.append("			});\n");
		sb.append("			if (tmp == null) {\n");
		sb.append("				tmp = get" + className + " (tcpa, (tcp) => {\n");
		if ("string".equals(getFixCsharpTypebyJavaType(colTypes[0]))) {
			sb.append("					return tcp != null && tcp." + DBUtil.camelName(colNames[0].toLowerCase())
					+ " == \"null\";//潜规则记录\n");
		} else {
			sb.append("					return tcp != null && tcp." + DBUtil.camelName(colNames[0].toLowerCase())
					+ " == -1000;//潜规则记录\n");

		}
		sb.append("				});\n");
		sb.append("			}\n");
		sb.append("			return tmp != null;\n");
		sb.append("		}\n");

		sb.append("		public static " + className + " get" + className + "(" + className + "Array tcpa,System.Func<"
				+ className + ",bool> compareFunc){\n");
		sb.append("			if(tcpa ==null || tcpa." + propName4Class + "Items==null || compareFunc ==null){\n");
		sb.append("				return null;\n");
		sb.append("			}\n");
		sb.append("			for(int i=0;i<tcpa." + propName4Class + "Items.Count;i++){\n");
		sb.append("				" + className + " tcp=tcpa." + propName4Class + "Items[i];\n");
		sb.append("				if(compareFunc(tcp)){\n");
		// sb.append(" return Clone" + className + "(tcp); // must clone\n");
		sb.append("					return tcp; // must clone\n");
		sb.append("				}\n");
		sb.append("			}\n");
		sb.append("			return null;\n");
		sb.append("		}\n");

		sb.append("		public static List<" + className + "> get" + className + "List(" + className
				+ "Array tcpa,System.Func<" + className + ",bool> compareFunc){\n");
		sb.append("			if(tcpa ==null || tcpa." + propName4Class + "Items==null || compareFunc ==null){\n");
		sb.append("				return null;\n");
		sb.append("			}\n");
		sb.append("			List<" + className + "> list = new List<" + className + "> ();");
		sb.append("			for(int i=0;i<tcpa." + propName4Class + "Items.Count;i++){\n");
		sb.append("				" + className + " tcp=tcpa." + propName4Class + "Items[i];\n");
		sb.append("				if(compareFunc(tcp)){\n");
		// sb.append(" list.Add(Clone" + className + "(tcp)); // must clone\n");
		sb.append("					list.Add(tcp); // must clone\n");
		sb.append("				}\n");
		sb.append("			}\n");
		sb.append("			return list;\n");
		sb.append("		}\n");

		sb.append("\n");
		sb.append("\n");
		sb.append("		public static " + className + " Clone" + className + " (" + className + " src)\n");
		sb.append("		{\n");
		sb.append("			if (src == null) {\n");
		sb.append("				return null;\n");
		sb.append("			}\n");
		sb.append("			" + className + "	 obj = new " + className + " ();\n");
		sb.append("			obj.added_time_long = src.added_time_long;\n");
		for (int i = 0; i < colNames.length; i++) {
			if (colNames[i].trim().length() > 0) {
				String propNameTmp = DBUtil.camelName(colNames[i].toLowerCase());

				String noClienCsvFlag = NoClientCSVUtil.getDbColNoClientCSVIndex(tableName, colNames[i].toLowerCase());
				if ("noaaa".equals(noClienCsvFlag)) {
					sb.append("/* no used4client\n");
				}
				if (colTypes[i].contains("[]")) {
					sb.append("			obj." + propNameTmp + ".AddRange(src." + propNameTmp + ")	;\n");
				} else {
					sb.append("			obj." + propNameTmp + " = src." + propNameTmp + "	;\n");
				}
				if ("noaaaa".equals(noClienCsvFlag)) {
					sb.append("*/\n");
				}
			}
		}

		sb.append("			return obj;\n");
		sb.append("		}\n");

		sb.append("		public static void saveProtobufBytes (" + className + "Array tcpa)\n");
		sb.append("		{\n");
		sb.append(
				"			if (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer) {\n");
		sb.append("				return;\n");
		sb.append("			}\n");
		sb.append("			tcpa.lastUpdateTime = 0;\n");
		sb.append("			string bytesFilePath = Application.streamingAssetsPath + \"/ProtoBufBytes/" + className
				+ "Array.bytes\";\n");
		sb.append("			FileInfo sourcedi = new FileInfo (bytesFilePath);\n");
		sb.append("			string fn = sourcedi.FullName;\n");
		sb.append("			BLDebug.LogWarning (\"saveProtobufBytes==\" + sourcedi.Name + \"|元素个数：\"+tcpa."
				+ propName4Class + "Items.Count + bytesFilePath + \"|\" + fn );\n");
		sb.append("			byte[] bytes = net.bilinkeji.common.util.ProtoBufUtil.ProtoBufToBytes (tcpa);\n");
		sb.append("			FileUtil.WriteAllBytes (bytesFilePath, bytes);\n");
		sb.append("		}\n");
		sb.append("		\n");
		sb.append("		public static void loadProtobufBytes (Action<" + className
				+ "Array> loadSuccCallBack, Action<string > failCallBack)\n");
		sb.append("		{\n");
		sb.append(
				"			if (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer) {\n");
		sb.append("				string bytesFilePath = Application.streamingAssetsPath + \"/ProtoBufBytes/" + className
				+ "Array.bytes\";\n");
		sb.append("			if (!bytesFilePath.Contains (\"://\")) {\n");
		sb.append("				bytesFilePath = \"file://\" + bytesFilePath;\n");
		sb.append("			}\n");
		sb.append("				net.bilinkeji.common.util.WWWResLoadUtil.loadProtoBufObjectByWWW<" + className
				+ "Array> (bytesFilePath, loadSuccCallBack, failCallBack);\n");
		sb.append("			} else {\n");
		sb.append("				" + className + "Array tcpa = load" + className + "ArrayFromCsvFile ();\n");
		sb.append("				if (tcpa != null) {\n");
		sb.append("					if (loadSuccCallBack != null) {\n");
		sb.append("						loadSuccCallBack (tcpa);\n");
		sb.append("					}\n");
		sb.append("					\n");
		sb.append("				} else {\n");
		sb.append("					if (failCallBack != null) {\n");
		sb.append("						failCallBack (\"obj is null\");\n");
		sb.append("					}\n");
		sb.append("				}\n");
		sb.append("			}\n");
		sb.append("		}\n");

		sb.append("}\n");
		sb.append("}\n");

		File protoFile = new File(protoFileDir, protoCsvHelperFileName);
		if (true || !protoFile.exists()) {
			writeFile(protoFile.getAbsolutePath(), sb.toString());
		} else {
			System.out.println(protoFile + " exists so ignore it");
		}

	}

	private static void genProtoBufDBBeansProtoFile(String tableName, List<String[]> colList) {
		if (tableName == null || !tableName.toLowerCase().startsWith("t")) {
			return;
		}
		// 避免诡异bug
		if (tableColMap.containsKey(tableName)) {
			System.out.println("waring!!!!!genProtoBufDBBeansProtoFile==" + tableName + "|repeated");
			return;
		}

		I18NUtil.genI18NDbColIndex(tableName, colList);// 在这里插入生成多远key的索引
		NoClientCSVUtil.genDbColNoClientCSVIndex(tableName, colList);
		I18NUtil.extractI18NDbColValue(tableName, colList);
		System.out.println("genProtoBufDBBeansProtoFile==" + tableName + "|" + LocalDateTime.now());
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String protoFileDir = protoFileRootDir + "/dbbeans4proto";
		String protoFileName = className + ".proto";
		StringBuilder sb = new StringBuilder();
		// sb.append("import \"bcl.proto\";\n");
		sb.append("package " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".dbbeans4proto" + ";").append("\n\n");
		// optional bcl.Decimal UnitPrice = 3;

		sb.append("option optimize_for = SPEED;\n");
		sb.append("option java_generate_equals_and_hash = true;\n");
		sb.append("option java_outer_classname = \"" + className + "OuterClass\";\n");
		sb.append("message ").append(className).append("{\n");

		// sb.append("\toptional bcl.Decimal UnitPrice = 3;\n");

		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		String protoIndexFileDir = protoFileRootDir + "/dbbeans4protopbindex";
		String protoIndexFileName = className + ".pbindex.txt";
		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (pp.size() < 1) {// 尚无数据，则生成一次
				for (int i = 0; i < colNames.length; i++) {
					if (colNames[i].trim().length() > 0) {
						String propName = DBUtil.camelName(colNames[i].toLowerCase().trim());
						pp.setProperty(propName, "" + (i + 1));
					}
				}
				pbIndexFile.getParentFile().mkdirs();
				try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
					pp.store(fos, "nocmt");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < colNames.length; i++) {
				if (colNames[i].trim().length() > 0) {
					String propName = DBUtil.camelName(colNames[i].toLowerCase().trim());
					boolean isArray = colTypes[i].trim().endsWith("[]");
					String propType = getProtoTypebyJavaType(colTypes[i]);
					// String propDefaultVal = "";
					boolean required = (i <= 0);
					String optType = (required ? "required" : "optional");
					if (isArray) {
						optType = "repeated";
					}
					String pbIndex = pp.getProperty(propName);
					if (pbIndex == null) { // 是新字段，则需要分配新序号
						for (int k = 1; k < 500; k++) {
							if (k >= 189 && k <= 199) {
								// 潜规则用掉的id不能再拿来分配了
								continue;
							}
							if (!pp.values().contains("" + k)) {
								pbIndex = "" + k;
								pp.setProperty(propName, "" + k);
								try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
									pp.store(fos, "nocmt");
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
							}
						}
					}
					sb.append("\t" + optType + " " + propType + " " + propName + " = " + pbIndex + "; // "
							+ colCmts[i].replace('\n', ' ').replace('\r', ' ') + "\n");
					// allColNameList.add(colNames[i].toLowerCase().trim());//
					// 字段名统一小写
				}
			}
		}
		sb.append("optional int64 added_time_long = 189; //数据入库时间精确到秒整数格式是yyyyMMddHHmmss \n");
		sb.append("optional int64 lastupdated_time_long = 190; //数据入库时间精确到秒整数格式是yyyyMMddHHmmss \n");
		sb.append("optional int32 del_flag = 191; //数据逻辑删除标志位，小于0表示删除了 \n");
		sb.append("}").append("\n");

		sb.append("\n//列表数组容器");
		sb.append("\nmessage " + className + "Array\n");
		sb.append("{\n");
		sb.append("optional int64 lastUpdateTime = 1;\n");
		sb.append("\n");
		String propName = Character.toLowerCase(className.charAt(0)) + className.substring(1, className.length() - 6);
		sb.append("repeated " + className + " " + propName + "Items = 2;\n");
		sb.append("optional int64 lastUpdatedTime = 8 [default = 0]; //最后一次保存到redis的时间戳，毫秒值，用作版本校验\n");
		sb.append("\n");
		sb.append("}").append("\n");

		sb.append("\n//分页查询结果容器");
		sb.append("\nmessage " + className + "DataPage\n");
		sb.append("{\n");
		sb.append("	required " + className + "Array " + propName + "Array = 1;\n");
		sb.append("\n");
		sb.append("	required int32 totalCount = 2;\n");
		sb.append("\n");
		sb.append("	required int32 pageSize = 3;\n");
		sb.append("\n");
		sb.append("	required int32 pageNo = 4;\n");
		sb.append("\n");
		sb.append("}").append("\n");
		sbDbBeanConfigProto4Import.append("import \"" + className + ".proto\";\n");

		String proto4IndexFileName = "DbbeansConfig4Proto.pbindex.txt";
		File pb4IndexFile = new File(protoIndexFileDir, proto4IndexFileName);
		Properties pp4Index = new Properties();
		if (pbIndexFile.exists()) {
			try (FileInputStream fis = new FileInputStream(pb4IndexFile)) {
				pp4Index.load(fis);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String pbIndexNew = pp4Index.getProperty(className + "Array");
		if (pbIndexNew == null) { // 是新字段，则需要分配新序号
			for (int k = 2; k < 600; k++) {
				if (k >= 189 && k <= 199) {
					// 潜规则用掉的id不能再拿来分配了
					continue;
				}
				if (!pp4Index.values().contains("" + k)) {
					pbIndexNew = "" + k;
					pp4Index.setProperty(className + "Array", "" + k);
					try (FileOutputStream fos = new FileOutputStream(pb4IndexFile)) {
						pp4Index.store(fos, "nocmthaha");
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		sbDbBeanConfigProto.append("	optional " + className + "Array " + propName + "Array = " + pbIndexNew + ";\n");

		File protoFile = new File(protoFileDir, protoFileName);
		if (true || !protoFile.exists()) {
			writeFile(protoFile.getAbsolutePath(), sb.toString());
		} else {
			System.out.println(protoFile + " exists so ignore it");
		}

	}

	private static void genProtoBufCmdUtilJavaFile(String tableName, List<String[]> colList) {
		String className = "ProtoBufCmdUtil";
		String packageName = CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT;
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
		String javaFileName = className + ".java";

		StringBuilder sb = new StringBuilder();

		sb.append("package " + packageName + ";\n");
		sb.append("\n");
		sb.append("import com.google.protobuf.MessageOrBuilder;\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".common.TProtoCmd.TProto_Cmd;\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String modName = s[1].toLowerCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufRequest";
			String protoRespClassName = s[1] + "" + s[2] + "ProtoBufResponse";
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "." + modName + "." + protoReqClassName
					+ "OuterClass.*;\n");
			sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "." + modName + "." + protoRespClassName
					+ "OuterClass.*;\n");
		}
		sb.append("\n");
		sb.append("public class " + className + " {\n");
		sb.append("\n");
		sb.append("	public static MessageOrBuilder newProtoBufRPCRequest(TProto_Cmd cmd) {\n");

		sb.append("		if (cmd == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		sb.append("		try {\n");
		sb.append("		switch (cmd.getNumber()) {\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufRequest";
			sb.append("		case TProto_Cmd." + cmdEnum + "_VALUE:\n");
			sb.append("			return " + protoReqClassName + ".newBuilder();\n");

		}

		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");
		sb.append("		} catch (Exception e) {\n");
		sb.append("			e.printStackTrace();\n");
		sb.append("		}		\n");
		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");

		sb.append("	public static MessageOrBuilder getProtoBufRequestFrom(TProto_Cmd cmd,byte[] bytes) {\n");

		sb.append("		if (cmd == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");

		sb.append("		try {\n");

		sb.append("		switch (cmd.getNumber()) {\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufRequest";
			sb.append("		case TProto_Cmd." + cmdEnum + "_VALUE:\n");
			sb.append("			return " + protoReqClassName + ".parseFrom(bytes);\n");

		}

		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");

		sb.append("		} catch (Exception e) {\n");
		sb.append("			e.printStackTrace();\n");
		sb.append("		}		\n");
		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");

		sb.append("	public static MessageOrBuilder getProtoBufResponseFrom(TProto_Cmd cmd,byte[] bytes) {\n");

		sb.append("		if (cmd == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		sb.append("		try {\n");
		sb.append("		switch (cmd.getNumber()) {\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufResponse";
			sb.append("		case TProto_Cmd." + cmdEnum + "_VALUE:\n");
			sb.append("			return " + protoReqClassName + ".parseFrom(bytes);\n");

		}

		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");
		sb.append("		} catch (Exception e) {\n");
		sb.append("			e.printStackTrace();\n");
		sb.append("		}		\n");
		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");

		sb.append("	public static MessageOrBuilder newProtoBufRPCResponse(TProto_Cmd cmd) {\n");
		sb.append("		if (cmd == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		sb.append("		switch (cmd.getNumber()) {\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String protoRespClassName = s[1] + "" + s[2] + "ProtoBufResponse";
			sb.append("		case TProto_Cmd." + cmdEnum + "_VALUE:\n");
			sb.append("			return " + protoRespClassName + ".newBuilder();\n");
		}
		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");
		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public static TProto_Cmd getCmdFromRequest(Object obj) {\n");
		sb.append("		TProto_Cmd cmd = null;\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = s[1].toUpperCase() + "_" + s[2].toUpperCase();
			String protoReqClassName = s[1] + "" + s[2] + "ProtoBufRequest";
			String protoRespClassName = s[1] + "" + s[2] + "ProtoBufResponse";
			sb.append("		if (obj instanceof " + protoReqClassName + "OrBuilder\n");
			sb.append("				|| obj instanceof " + protoRespClassName + "OrBuilder) {\n");
			sb.append("			\n");
			sb.append("				return TProto_Cmd." + cmdEnum + ";\n");
			sb.append("			}\n");
		}

		sb.append("		return cmd;\n");
		sb.append("		}\n");
		sb.append("	}\n");

		File protoFile = new File(javaFileDir, javaFileName);
		if (true || !protoFile.exists()) {
			writeFile(protoFile.getAbsolutePath(), sb.toString());
		} else {
			System.out.println(protoFile + " exists so ignore it");
		}

	}

	private static void genProtoBufReuestProtoCSVFile(String modName, String cmdName) {
		String fileName = cmdName + "ProtoBufRequest";
		String protoFileDir = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/" + modName;
		String protoFileName = fileName + ".csv";

		File protoFile = new File(protoFileDir, protoFileName);
		File src = new File(CodeGenConsts.PROJCSVFILE_DIRROOT + "templatefiles/ProtoBufRequestAndResponse_temp.csv");
		if (!protoFile.exists()) {
			copyFile(src, protoFile);
		} else {
			// // System.out.println(protoFile + " exists so ignore it");
		}
	}

	@SuppressWarnings("unchecked")
	private static void genProtoBufResponseProtoCSVFile(String modName, String cmdName,
			Map<String, List<String>> cmdModmap) {
		String fileName = cmdName + "ProtoBufResponse";
		String protoFileDir = CodeGenConsts.PROJCSVFILE_DIRROOT + "/ProtobufFiles/TprotoCmds/" + modName;
		String protoFileName = fileName + ".csv";
		File protoFile = new File(protoFileDir, protoFileName);
		File src = new File(CodeGenConsts.PROJCSVFILE_DIRROOT + "templatefiles/ProtoBufRequestAndResponse_temp.csv");
		if (!protoFile.exists()) {
			String csvText = GenAll.readFile(src.getAbsolutePath(), "UTF-8");
			String imp = "ProtobufRpcBase.proto\"\"";
			imp = imp + ";import \"\"GecaoshoulieEnumDef.proto\"\""; // import
																		// "GecaoshoulieEnumDef.proto";
			TreeSet<String> st = new TreeSet<String>(cmdModmap.keySet());
			for (String modNameTmp : st) {
				imp = imp + ";import \"\"" + modNameTmp + "Module4Proto.proto\"\"";
			}
			csvText = csvText.replaceAll("ProtobufRpcBase.proto\"\"", imp);
			// copyFile(src, protoFile);
			writeFile(protoFile.getAbsolutePath(), csvText, "UTF-8");
		} else {
			// // System.out.println(protoFile + " exists so ignore it");
		}
	}

	/**
	 * 
	 * <b>功能：根据csv生成创建表的sql语句</b><br>
	 * <br>
	 * <b>实现步骤：</b><br>
	 * <b>1.</b> <br>
	 * <b>2.</b> <br>
	 * 
	 * @修改者 ~ , quickli 2015-3-30
	 * @param tableName
	 * @param colList
	 * @return String
	 */
	public static String genCreateTableSql(String tableName, String tableCmt, List<String[]> colList) {
		if (tableName == null || !tableName.toLowerCase().startsWith("t")) {// 非t开头的表不创建
			return "";
		}

		tableCmt = tableCmt + ",redis dbIndex默认为：" + Math.abs(tableName.hashCode() % 37);
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n");
		String[] colNames = colList.get(0);
		String[] colJavaTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		boolean needSetai = false;
		for (int i = 0; i < colNames.length; i++) {
			String colName = colNames[i].toLowerCase().trim();
			String colJavaType = colJavaTypes[i].toLowerCase().trim();
			String colCmt = colCmts[i].trim();
			String colSqlType = getSqlTypebyJavaType(colJavaType, colName, tableName);
			if (colJavaType.contains("[]")) {// 是数组类型
				colCmt = colCmt + "|" + colJavaType + "|";
			}
			String colSqlDefault = " DEFAULT " + getSqlDefaultJavaType(colJavaType);
			if (i == 0
					&& (colJavaType.toLowerCase().startsWith("int") || colJavaType.toLowerCase().startsWith("long"))) {// 第一列整数的是自增id
				colSqlDefault = " AUTO_INCREMENT ";
				needSetai = true;
			}
			if (colJavaType.startsWith("text") || colJavaType.contains("[]")) {
				colSqlDefault = "  ";
			}
			String colSql = "  `" + colName + "` " + colSqlType + " NOT NULL " + colSqlDefault + "  COMMENT '"
					+ encodeSQL(colCmt) + "',\n";
			if (colName.length() > 0 && colJavaType.length() > 0 && colCmt.length() > 0) {// 有字段名,有字段值，有备注，s才认为是合法的字段
				sb.append(colSql);
			} else if (colName.length() <= 0 && colJavaType.length() <= 0 && colCmt.length() <= 0) {// 全空说明是空白列，可以跳过了
																									// sb.append(colSql);
			} else {
				// //// System.err.println("error: col info not full:colName=" +
				// //// colName + "|colJavaType=" + colJavaType
				// //// + "|colCmt=" + colCmt);
			}
		}

		sb.append("  `added_time_long` bigint(20)  not null DEFAULT 0 COMMENT '数据入库时间精确到秒整数格式是yyyyMMddHHmmss',\n");
		sb.append(
				"  `lastupdated_time_long` bigint(20) not null DEFAULT 0 COMMENT '数据最近更新时间，精确到秒整数格式是yyyyMMddHHmmss',\n");
		sb.append(
				"  `del_flag` int  not null DEFAULT 0 COMMENT '数据逻辑删除标志位，小于0表示删除了，不能执行update sql ,生成的update语句需要带上这个查询条件',\n");

		String fidColName = colNames[0];
		sb.append("  PRIMARY KEY (`" + fidColName + "`)\n");
		sb.append(")");
		if (needSetai) {
			sb.append(" AUTO_INCREMENT=20001 ");
		}
		sb.append(" ENGINE=MyISAM  COMMENT='" + tableCmt + "'  DEFAULT CHARSET=utf8mb4;\n");
		// sb.append("ALTER TABLE `tcomm_channel` ;\n");

		return sb.toString();
	}

	public static List<Pair<String, String>> genUpdateSql(String tableName, List<String[]> colList) {
		List<Pair<String, String>> sqlList = new ArrayList<Pair<String, String>>();
		if (colList != null && colList.size() > CSV_DATA_COL_START_INDEX) {
			for (int k = CSV_DATA_COL_START_INDEX; k < colList.size(); k++) {
				StringBuilder sb = new StringBuilder();
				String[] colNames = colList.get(0);
				String[] colJavaTypes = colList.get(1);
				String[] colValues = colList.get(k);

				String sqlPrefix = " update " + tableName + " set ";
				StringBuilder colNamesb = new StringBuilder();
				// StringBuilder colValuesb = new StringBuilder();
				int cc = 0;
				for (int i = 0; i < colNames.length; i++) {
					String colName = colNames[i].toLowerCase().trim();
					if (colName.length() > 0) {
						String colJavaType = colJavaTypes[i].toLowerCase().trim();
						String colValue = "";
						if (colValues.length > i) {
							colValue = colValues[i];
						}
						// 数组类型的不不能补0占位
						if (colValue.length() < 1 && !colJavaType.contains("[")
								&& (colJavaType.toLowerCase().startsWith("int")
										|| colJavaType.toLowerCase().startsWith("long")
										|| colJavaType.toLowerCase().startsWith("float"))) {
							colValue = "0";
						}
						// 在加载csv的地方已经严格限制了字段类型和字段说明等信息，因此这里不需要再输出信息了
						if (colNames.length != colValues.length || colJavaTypes.length != colValues.length) {
							// System.out.println(colNames.length + "|" +
							// colJavaTypes.length + "|" + colValues.length
							// + "|colName=" + colName + "|colValue=" +
							// colValue);
						}
						String colSqlType = getSqlTypebyJavaType(colJavaType, colName, tableName);
						String colSqlDefault = getSqlDefaultJavaType(colJavaType);
						// 有字段名,有字段值，有备注，才认为是合法的字段
						if (cc > 0) {
							colNamesb.append(",");
						}
						colNamesb.append(colName).append(" = ");
						if (cc > 0) {
							// / colValuesb.append(",");
						}
						cc++;
						if (colJavaType.contains("[]") || "String".equalsIgnoreCase(colJavaType)
								|| "Text".equalsIgnoreCase(colJavaType) || "str".equalsIgnoreCase(colJavaType)) {
							colNamesb.append("\"");
						}
						colNamesb.append(encodeSQL(colValue));
						if (colJavaType.contains("[]") || "String".equalsIgnoreCase(colJavaType)
								|| "Text".equalsIgnoreCase(colJavaType) || "str".equalsIgnoreCase(colJavaType)) {
							colNamesb.append("\"");
						}
					} else {
						// ////// System.err.println("error: col info not
						// ////// full:colName=" + colName);
						// System.err.println("error: col info not
						// full:colName="
						// + colName
						// + "|colJavaType="
						// + colJavaType + "|colValue=" + colValue);
					}
				}

				sb.append(sqlPrefix).append(colNamesb).append(",lastupdated_time_long = ")
						.append(currentSqlTimestamp4Long()).append(" where ").append(colNames[0].toLowerCase().trim())
						.append(" = ");
				String colJavaType = colJavaTypes[0].toLowerCase().trim();
				if ("String".equalsIgnoreCase(colJavaType) || "Text".equalsIgnoreCase(colJavaType)
						|| "str".equalsIgnoreCase(colJavaType)) {
					sb.append("\"");
				}
				sb.append(colValues[0]);
				if ("String".equalsIgnoreCase(colJavaType) || "Text".equalsIgnoreCase(colJavaType)
						|| "str".equalsIgnoreCase(colJavaType)) {
					sb.append("\"");
				}
				sb.append("\n");
				sqlList.add(Pair.makePair(colValues[0], sb.toString()));

			}
		}
		return sqlList;

	}

	public static List<Pair<String, String>> genReplaceInsertSql(String tablePrefix, String tableName,
			List<String[]> colList) {
		List<Pair<String, String>> sqlList = new ArrayList<Pair<String, String>>();
		if (colList != null && colList.size() > CSV_DATA_COL_START_INDEX) {
			for (int k = CSV_DATA_COL_START_INDEX; k < colList.size(); k++) {
				StringBuilder sb = new StringBuilder();
				String[] colNames = colList.get(0);
				String[] colJavaTypes = colList.get(1);
				String[] colValues = colList.get(k);

				String sqlPrefix = " REPLACE INTO " + tableName + " (";
				StringBuilder colNamesb = new StringBuilder();
				StringBuilder colValuesb = new StringBuilder();
				int cc = 0;
				for (int i = 0; i < colNames.length; i++) {
					String colName = colNames[i].toLowerCase().trim();
					if (colName.length() > 0) {
						String colJavaType = colJavaTypes[i].toLowerCase().trim();
						String colValue = "";
						if (colValues.length > i) {
							colValue = colValues[i].trim();
						}

						// 数组类型的不不能补0占位
						if (colValue.length() < 1 && !colJavaType.contains("[")
								&& (colJavaType.toLowerCase().startsWith("int")
										|| colJavaType.toLowerCase().startsWith("long")
										|| colJavaType.toLowerCase().startsWith("float"))) {
							colValue = "0";
						}

						// 2017-07-15 linyaoheng 处理string == -1的情况
						if ("-1".equals(colValue) && "String".equalsIgnoreCase(colJavaType)) {
							colValue = "";
						}

						if (colNames.length != colValues.length || colJavaTypes.length != colValues.length) {
							// System.out.println(colNames.length + "|" +
							// colJavaTypes.length + "|" + colValues.length
							// + "|colName=" + colName + "|colValue=" +
							// colValue);
						}
						String colSqlType = getSqlTypebyJavaType(colJavaType, colName, tableName);
						String colSqlDefault = getSqlDefaultJavaType(colJavaType);
						// 有字段名,有字段值，有备注，才认为是合法的字段
						if (cc > 0) {
							colNamesb.append(",");
						}
						colNamesb.append(colName);
						if (cc > 0) {
							colValuesb.append(" , ");
						}
						cc++;
						if (colJavaType.contains("[]") || "String".equalsIgnoreCase(colJavaType)
								|| "Text".equalsIgnoreCase(colJavaType) || "str".equalsIgnoreCase(colJavaType)) {
							colValuesb.append("\"");
						}
						colValuesb.append(encodeSQL(colValue));
						if (colJavaType.contains("[]") || "String".equalsIgnoreCase(colJavaType)
								|| "Text".equalsIgnoreCase(colJavaType) || "str".equalsIgnoreCase(colJavaType)) {
							colValuesb.append("\"");
						}
					} else {
						// //////// System.err.println("error: col info not
						// //////// full:colName=" + colName);
						// System.err.println("error: col info not
						// full:colName="
						// + colName
						// + "|colJavaType="
						// + colJavaType + "|colValue=" + colValue);
					}
				}

				sb.append(sqlPrefix).append(colNamesb).append(",added_time_long,lastupdated_time_long,del_flag")
						.append(") VALUES(");
				sb.append(colValuesb).append("," + (currentSqlTimestamp4Long() / 1000000 * 1000000) + ","
						+ (currentSqlTimestamp4Long() / 1000000 * 1000000)).append(",0);\n");
				sbInsertTable.append(sb).append("\n");
				sbInsertTableMap.get(tablePrefix).append(sb).append("\n");
				sqlList.add(Pair.makePair(colValues[0], sb.toString()));

			}
		}
		return sqlList;

	}

	public static long currentSqlTimestamp4Long() {
		return getSqlTimestamp4Long(System.currentTimeMillis());
	}

	/**
	 * 将指定时间转成20151109153800这样格式的长整数
	 * 
	 * @param timeMillis
	 * @return
	 */
	public static long getSqlTimestamp4Long(long timeMillis) {
		String ts = formatDate(new java.sql.Timestamp(timeMillis), "yyyyMM") + "01000000";
		return Long.valueOf(ts);
	}

	public static String genDropTableSql(String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE IF EXISTS `" + tableName + "`;");
		return sb.toString();
	}

	public static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 根据java类型转化获取对应的sql类型，注意，当mysql使用utf8mb4做字符集的时候，则主键为字符串的情况下，key太长会出错，
	 * 因此在那会死调整成默认的191,问题原因可以参考附录链接
	 * 
	 * @param javaType
	 * @return
	 * @see "http://stackoverflow.com/questions/1814532/1071-specified-key-was-too-long-max-key-length-is-767-bytes"
	 */
	public static String getSqlTypebyJavaType(String javaType, String colName, String tableName) {
		String sqlType = "varchar(191)";
		if (javaType != null && javaType.trim().endsWith("[]")) {// 数组的当字符串
			sqlType = "TEXT";
			return sqlType;
		}
		if ("int".equalsIgnoreCase(javaType)) {
			sqlType = "int(10)";
			return sqlType;
		}
		if ("long".equalsIgnoreCase(javaType)) {
			sqlType = "bigint(20)";
			return sqlType;
		}
		if (javaType.toLowerCase().startsWith("str")) {
			sqlType = "varchar(191)";
			return sqlType;
		}
		if ("String".equalsIgnoreCase(javaType)) {
			sqlType = "varchar(191)";
			return sqlType;
		}
		if ("Text".equalsIgnoreCase(javaType)) {
			sqlType = "TEXT";
			return sqlType;
		}
		if ("float".equalsIgnoreCase(javaType)) {
			sqlType = "FLOAT";
			return sqlType;
		}
		if ("double".equalsIgnoreCase(javaType)) {
			sqlType = "DOUBLE";
			return sqlType;
		}
		if (colName.length() > 0) {// 有字段名的才告警
			System.err.println(
					"getSqlTypebyJavaType|errorColType|for|type:" + javaType + "|for|" + colName + "@" + tableName);
			MailTest.sendErrorMail(
					"getSqlTypebyJavaType|errorColType|for|type:" + javaType + "|for|" + colName + "@" + tableName,
					"getSqlTypebyJavaType|errorColType|for|type:" + javaType + "|for|" + colName + "@" + tableName);
		}
		// System.exit(122);
		return sqlType;
	}

	public static int getSqlDataTypebyJavaType(String javaType) {
		int sqlType = Types.VARCHAR;
		if ("int".equalsIgnoreCase(javaType)) {
			sqlType = Types.INTEGER;
		}
		if ("long".equalsIgnoreCase(javaType)) {
			sqlType = Types.BIGINT;
		}
		if (javaType.toLowerCase().startsWith("str")) {
			sqlType = Types.VARCHAR;
		}
		if ("String".equalsIgnoreCase(javaType)) {
			sqlType = Types.VARCHAR;
		}
		if ("Text".equalsIgnoreCase(javaType)) {
			sqlType = Types.LONGVARCHAR;
		}

		if ("float".equalsIgnoreCase(javaType)) {
			sqlType = Types.REAL;
		}
		if ("double".equalsIgnoreCase(javaType)) {
			sqlType = Types.DOUBLE;
		}
		return sqlType;
	}

	public static String getFixCsharpTypebyJavaType(String javaType) {
		String sqlType = "string";
		if ("int".equalsIgnoreCase(javaType)) {
			sqlType = "int";
		}
		if ("long".equalsIgnoreCase(javaType)) {
			sqlType = "long";
		}
		if (javaType.toLowerCase().startsWith("str")) {
			sqlType = "string";
		}
		if ("String".equalsIgnoreCase(javaType)) {
			sqlType = "string";
		}
		if ("Text".equalsIgnoreCase(javaType)) {
			sqlType = "string";
		}

		if ("float".equalsIgnoreCase(javaType)) {
			sqlType = "float";
		}
		if ("double".equalsIgnoreCase(javaType)) {
			sqlType = "double";
		}
		return sqlType;
	}

	public static String getFixJavaTypebyJavaType(String javaType) {
		String sqlType = "string";
		if ("int".equalsIgnoreCase(javaType)) {
			sqlType = "int";
		}
		if ("long".equalsIgnoreCase(javaType)) {
			sqlType = "long";
		}
		if (javaType.toLowerCase().startsWith("str")) {
			sqlType = "String";
		}
		if ("String".equalsIgnoreCase(javaType)) {
			sqlType = "String";
		}
		if ("Text".equalsIgnoreCase(javaType)) {
			sqlType = "String";
		}

		if ("float".equalsIgnoreCase(javaType)) {
			sqlType = "float";
		}
		if ("double".equalsIgnoreCase(javaType)) {
			sqlType = "double";
		}
		return sqlType;
	}

	public static String getCSharpTypebyProtoType(String ptotoType, String repeate) {
		ptotoType = ptotoType.trim();
		String javsTypetmp = ptotoType;
		if ("int32".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "int";
		}
		if ("int64".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "long";
		}
		if (ptotoType.toLowerCase().startsWith("string")) {
			javsTypetmp = "string";
		}
		if ("float".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "float";
		}
		if ("double".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "double";
		}
		if ("bytes".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "byte[]";
		}
		if ("repeated".equalsIgnoreCase(repeate.trim())) {
			if ("int32".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "int";
			}
			if ("int64".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "long";
			}
			if ("float".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "float";
			}
			if ("double".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "double";
			}

			return "List<" + javsTypetmp + ">";
		} else {
			return javsTypetmp;
		}
	}

	public static String getJavaTypebyProtoType(String ptotoType, String repeate) {
		ptotoType = ptotoType.trim();
		String javsTypetmp = ptotoType;
		if ("int32".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "int";
		}
		if ("int64".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "long";
		}
		if (ptotoType.toLowerCase().startsWith("string")) {
			javsTypetmp = "String";
		}
		if ("float".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "float";
		}
		if ("double".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "double";
		}
		if ("bytes".equalsIgnoreCase(ptotoType)) {
			javsTypetmp = "byte[]";
		}
		if ("repeated".equalsIgnoreCase(repeate.trim())) {
			if ("int32".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "Integer";
			}
			if ("int64".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "Long";
			}
			if ("float".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "Float";
			}
			if ("double".equalsIgnoreCase(ptotoType)) {
				javsTypetmp = "Double";
			}

			return "java.util.List<" + javsTypetmp + ">";
		} else {
			return javsTypetmp;
		}
	}

	public static String getProtoTypebyJavaType(String javaType) {
		String sqlType = "string";
		javaType = javaType.replace('[', ' ').replace(']', ' ').trim();
		if ("int".equalsIgnoreCase(javaType)) {
			sqlType = "int32";
		}
		if ("long".equalsIgnoreCase(javaType)) {
			sqlType = "int64";
		}
		if (javaType.toLowerCase().startsWith("str")) {
			sqlType = "string";
		}
		if ("String".equalsIgnoreCase(javaType)) {
			sqlType = "string";
		}
		if ("Text".equalsIgnoreCase(javaType)) {
			sqlType = "string";
		}

		if ("float".equalsIgnoreCase(javaType)) {
			sqlType = "float";
		}
		if ("double".equalsIgnoreCase(javaType)) {
			sqlType = "double";
		}
		return sqlType;
	}

	public static String getSqlDefaultJavaType(String javaType) {
		String defValue = "''";
		if ("int".equalsIgnoreCase(javaType)) {
			defValue = " 0";
		}
		if ("long".equalsIgnoreCase(javaType)) {
			defValue = " 0";
		}
		if ("float".equalsIgnoreCase(javaType)) {
			defValue = "0.0";
		}
		if ("double".equalsIgnoreCase(javaType)) {
			defValue = "0.0";
		}
		if (javaType.toLowerCase().startsWith("str")) {
			defValue = "''";
		}
		if ("String".equalsIgnoreCase(javaType)) {
			defValue = "''";
		}
		if ("Text".equalsIgnoreCase(javaType)) {
			defValue = "''";
		}
		return defValue;
	}

	/**
	 * 根据csv和数据库，对比表结构是否一致
	 * 
	 * @param csvFilePath
	 * @param conn
	 * @return
	 */
	public static boolean isDiffTableDescbyCsvAndTable(String csvFilePath, java.sql.Connection conn) {
		File csvFile = new File(csvFilePath);
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		// String tableCmt = getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> csvColList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		// List<String[]> dbColList = getDataFromDB(conn, tableName,
		// csvColList);
		Map<String, String> dbColMap = new HashMap<String, String>();
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet clrs = dbmd.getColumns(null, null, tableName, null);
			while (clrs.next()) {
				String cl_name = clrs.getString("COLUMN_NAME");
				String dataType = clrs.getString("DATA_TYPE");
				if (!cl_name.equals("lastupdated_time_long") && !cl_name.equals("added_time_long")
						&& !cl_name.equals("del_flag")) {
					dbColMap.put(cl_name, dataType);
				}
			}
			clrs.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, String> csvColMap = new HashMap<String, String>();
		String[] colNames = csvColList.get(0);
		String[] colJavaTypes = csvColList.get(1);
		String[] colCmts = csvColList.get(2);
		for (int i = 0; i < colNames.length; i++) {
			String colName = colNames[i].toLowerCase().trim();
			String colJavaType = colJavaTypes[i].toLowerCase().trim();
			// String colSqlType = getSqlTypebyJavaType(colJavaType);
			String colCmt = colCmts[i].trim();
			if (colName.length() > 0 && colJavaType.length() > 0 && colCmt.length() > 0) {// 有字段名,有字段值，有备注，s才认为是合法的字段
				csvColMap.put(colName, "" + getSqlDataTypebyJavaType(colJavaType));
			}
		}
		List<String> keyList = new ArrayList<String>();
		keyList.addAll(dbColMap.keySet());
		// System.err.println("isDiffTableDescbyCsvAndTable==" + tableName +
		// ",dbColMap=" + dbColMap);
		// System.err.println("isDiffTableDescbyCsvAndTable==" + tableName +
		// ",csvColMap=" + csvColMap);
		for (String key : keyList) {
			String dbColType = dbColMap.get(key);
			String csvColType = csvColMap.get(key);
			if (dbColType.equals(csvColType)) {
				dbColMap.remove(key);
				csvColMap.remove(key);
			}
		}
		boolean rs = csvColMap.size() > 0 || dbColMap.size() > 0;
		if (rs) {
			System.err.println("isDiffTableDescbyCsvAndTable==" + tableName + ",dbColMap=" + dbColMap);
			System.err.println("isDiffTableDescbyCsvAndTable==" + tableName + ",csvColMap=" + csvColMap);
		}
		return rs;
	}

	/**
	 * 
	 * <b>功能：比较csv文件和已经导入数据的数据，列出差异的地方</b><br>
	 * <br>
	 * <b>实现步骤：</b><br>
	 * <b>1.</b> <br>
	 * <b>2.</b> <br>
	 * 
	 * @修改者 ~ , quickli 2015-3-30
	 * @param csvFilePath
	 * @param conn
	 * @return List<String> 每条记录是有不同数据的整行记录
	 */
	public static List<DiffInfo> genDiffbyCsvAndTable(String csvFilePath, java.sql.Connection conn) {
		File csvFile = new File(csvFilePath);
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		// String tableCmt = getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> csvColList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		List<String[]> dbColList = getDataFromDB(conn, tableName, csvColList);
		String allColNames[] = getAllColnames(conn, tableName, csvColList);
		Set<String> allFidSet = getAllFids(csvColList, dbColList);

		List<DiffInfo> diffList = new ArrayList<DiffInfo>();
		System.out.println(tableName + "|csvColNames=|" + Arrays.toString(csvColList.get(0)));
		System.out.println(tableName + "|allColNames=|" + Arrays.toString(allColNames));
		System.out.println(tableName + "|fid=|" + allFidSet);
		for (String fid : allFidSet)// 遍历数据记录，找出缺失的记录
		{
			String[] csvData = getDatabyFid(fid, csvColList);
			String[] dbData = getDatabyFid(fid, dbColList);
			DiffInfo di = new DiffInfo();
			di.setFid(fid);
			for (int i = 0; i < allColNames.length; i++) {

				String colName = allColNames[i];
				String csvValue = "";
				String dbValue = "";
				if (csvData.length > i) {
					csvValue = csvData[i];
					csvValue = csvValue == null ? "" : csvValue;
				}
				if (dbData.length > i) {
					dbValue = dbData[i];
					dbValue = dbValue == null ? "" : dbValue;
				}
				if (!csvValue.equals(dbValue)) {// 不相等
					DiffColInfo dci = new DiffColInfo();
					dci.setColName(colName);
					dci.setCsvValue(csvValue);
					dci.setDbValue(dbValue);
					di.getDiffCols().add(dci);
				} else {
					SameColInfo sci = new SameColInfo();
					sci.setColName(colName);
					sci.setCsvValue(csvValue);
					di.getSameCols().add(sci);
				}
			}
			if (di.getDiffCols().size() > 0) {
				diffList.add(di);
			}
		}

		return diffList;
	}

	private static String[] getDatabyFid(String fid, List<String[]> csvColList) {
		String[] s = new String[0];
		for (String[] a : csvColList) {
			if (a[0] != null && a[0].length() > 0 && a[0].equals(fid)) {
				return a;
			}
		}
		return s;
	}

	private static Set<String> getAllFids(List<String[]> csvColList, List<String[]> dbColList) {
		Set<String> set = new HashSet<String>();
		for (int i = CSV_DATA_COL_START_INDEX; i < csvColList.size(); i++) {
			set.add(csvColList.get(i)[0]);
		}
		for (String[] s : dbColList) {
			set.add(s[0]);
		}
		return set;

	}

	private static List<String[]> getDataFromDB(java.sql.Connection conn, String tableName, List<String[]> colList) {
		List<String[]> dbColList = new ArrayList<String[]>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			String sql = "select * from " + tableName;
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsm = rs.getMetaData();
			int count = rsm.getColumnCount();
			List<String> dbColnameList = new ArrayList<String>();
			for (int k = 1; k <= count; k++) {
				String columnName = rsm.getColumnName(k);
				dbColnameList.add(columnName);
			}
			List<String> allColNameList = getAllColnames(colList, dbColnameList);
			while (rs.next()) {
				List<String> dbColValueList = new ArrayList<String>();
				for (String cName : allColNameList) {
					String val = null;
					try {

						val = rs.getString(cName);
					} catch (Exception e) {
						// e.printStackTrace();
					}
					if (val == null) {
						val = "";
					}
					dbColValueList.add(val);
				}
				dbColList.add(dbColValueList.toArray(new String[0]));
			}

		} catch (SQLException e) {
			// e.printStackTrace();
		} finally {
			closeSqlConnStmtRs(null, stmt, rs);
		}
		return dbColList;
	}

	private static String[] getAllColnames(java.sql.Connection conn, String tableName, List<String[]> colList) {
		List<String> dbColnameList = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select * from " + tableName;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsm = rs.getMetaData();
			int count = rsm.getColumnCount();
			for (int k = 1; k <= count; k++) {
				String columnName = rsm.getColumnName(k);
				dbColnameList.add(columnName);
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			System.err.println("getAllColnames|slq=" + sql);
			e.printStackTrace();
		} finally {
			closeSqlConnStmtRs(null, stmt, rs);
		}

		List<String> allColNameList = getAllColnames(colList, dbColnameList);
		// 入库时间和更新时间不拿来比较
		allColNameList.remove("added_time_long");
		allColNameList.remove("lastupdated_time_long");
		allColNameList.remove("del_flag");
		return allColNameList.toArray(new String[0]);
	}

	private static List<String> getAllColnames(List<String[]> colList, List<String> dbColnameList) {
		Set<String> csvNoColNameSet = CSVUtil.getCSVNoColNameSet(colList.get(0), dbColnameList);
		List<String> allColNameList = new ArrayList<String>();
		String[] colNames = colList.get(0);
		for (int i = 0; i < colNames.length; i++) {
			if (colNames[i].trim().length() > 0) {
				allColNameList.add(colNames[i].toLowerCase().trim());// 字段名统一小写
			}
		}
		for (String cn : csvNoColNameSet) {
			allColNameList.add(cn);
		}
		return allColNameList;
	}

	public static Set<String> getDBNoColNameSet(String[] csvColNames, List<String> dbColNames) {
		Set<String> allSet = new HashSet<String>();
		Set<String> dbNoSet = new HashSet<String>();// csv有，但是数据库没有的列名，说明是csv新增字段了
		for (String s : csvColNames) {
			if (s != null && s.trim().length() > 0) {
				allSet.add(s);
			}
		}
		for (String s : dbColNames) {
			if (s != null && s.trim().length() > 0) {
				allSet.add(s);
			}
		}
		dbNoSet.addAll(allSet);
		for (String s : csvColNames) {
			if (s != null && s.trim().length() > 0) {
				dbNoSet.remove(s);
			}
		}
		return dbNoSet;

	}

	public static Set<String> getDiffColNameSet(String[] csvColNames, List<String> dbColNames) {
		Set<String> allSet = new HashSet<String>();
		Set<String> colSet = new HashSet<String>();
		Set<String> dbSet = new HashSet<String>();
		Set<String> diffSet = new HashSet<String>();// csv和db不同时有的列
		for (String s : csvColNames) {
			if (s != null && s.trim().length() > 0) {
				allSet.add(s);
				colSet.add(s);
			}
		}
		for (String s : dbColNames) {
			if (s != null && s.trim().length() > 0) {
				allSet.add(s);
				dbSet.add(s);
			}
		}
		for (String s : allSet) {
			if (colSet.contains(s) && dbSet.contains(s)) {
				// 都有就不管
			} else {
				diffSet.add(s);
			}

		}
		return diffSet;

	}

	public static String encodeSQL(String sql) {
		if (sql == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sql.length(); ++i) {
			char c = sql.charAt(i);
			switch (c) {
			case '\\':
				sb.append("\\\\");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\'':
				sb.append("\'\'");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\u200B':
			case '\uFEFF':
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static java.sql.Connection getDbCoon() {
		Connection conn = null;
		try {
			String dburl = "jdbc:mysql://localhost:3306/" + CodeGenConsts.PROJDBNAME
					+ "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
			String dbuser = "root";
			String dbPass = "";
			if (new File("/Users/quickli").exists() || new File("/Users/linyaoheng").exists()
					|| new File("D:\\mgamedev\\tools\\mysql-5.6.26-winx64\\rootpwd.txt").exists()) {
				System.err.println("getDbCoon|use mysql pwd!!!!!!!!!!!!!!!!");
				dbPass = "mysqlpwdbilinkejinet";
			} else {
				System.err.println("getDbCoon|dontuse mysql pwd!!!!!!!!!!!!!!!!");
			}
			Class.forName(com.mysql.jdbc.Driver.class.getName());
			conn = DriverManager.getConnection(dburl, dbuser, dbPass);
			// TODO 测试
			// System.out.println("-----------------ShowConnectionConfig------------------");
			// System.out.println(dburl);
			// System.out.println(dbuser);
			// System.out.println(dbPass);
			return conn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conn;
	}

	public static void closeSqlConnStmtRs(java.sql.Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}

		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
			}
		}

	}

	/**
	 * 创建目录
	 */
	public static boolean createDir(String filePath) {
		File d = new File(filePath);
		if (!d.exists()) {
			return d.mkdirs();
		}

		return true;
	}

	public static boolean writeFile(String filePath, String content) {
		return writeFile(filePath, content, "UTF-8");
	}

	public static boolean writeFile(String filePath, String content, String encoding) {
		if (filePath != null
				&& (filePath.endsWith(".proto") || filePath.endsWith(".cs") || filePath.endsWith("Impl.java"))) {
			// System.out.println("writeFile to:" + filePath);
		}
		System.out.println("writeFile to:" + filePath);
		try {
			java.io.File f = new File(filePath);
			if (f.exists() && f.length() > 0) {// 文件已经存在，且有内容，则做一次md5比对
				byte[] fbs = new byte[(int) f.length()];
				FileInputStream fis = new FileInputStream(f);
				fis.read(fbs);
				fis.close();
				String strMd5 = HashCalc.md5(content);
				String fileMd5 = HashCalc.md5(fbs);
				if (strMd5.equals(fileMd5)) {
					// System.err.println("md5 xiangdeng =" + strMd5 + "|" + f);
					return false;
				} else {
					System.err.println("md5 new:strMd5=" + strMd5 + "|fileMd5=" + fileMd5 + "|for|" + f);
				}
			}
			f.getParentFile().mkdirs();
			// java.io.FileWriter fw = new FileWriter(f);
			java.io.PrintWriter pw = new java.io.PrintWriter(f, encoding);
			pw.print(content);
			pw.flush();
			pw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean appendFile(String filePath, String content) {
		// System.out.println("appendFile to:" + filePath);
		try {
			java.io.File f = new File(filePath);
			f.getParentFile().mkdirs();
			// java.io.FileWriter fw = new FileWriter(f);
			FileOutputStream fos = new FileOutputStream(f, true);
			java.io.PrintWriter pw = new java.io.PrintWriter(fos);
			pw.print(content);
			pw.print("\n");
			pw.flush();
			pw.close();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean copyFile(File src, File target) {
		if ((src == null) || (target == null) || !src.exists()) {
			return false;
		}

		InputStream ins = null;
		try {
			target.getParentFile().mkdirs();
			ins = new BufferedInputStream(new FileInputStream(src));
			OutputStream ops = new BufferedOutputStream(new FileOutputStream(target));
			int b;
			while (-1 != (b = ins.read())) {
				ops.write(b);
			}

			GenAll.safeClose(ins);
			GenAll.safeFlush(ops);
			GenAll.safeClose(ops);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return target.setLastModified(src.lastModified());
	}

}
