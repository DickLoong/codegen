package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.lizongbo.codegentool.CodeGenConsts;
import com.lizongbo.codegentool.GuidCompare;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.db2java.dbsql2xml.DBUtil;
import com.lizongbo.codegentool.tools.StringUtil;

public class CSVUtil {

	private static Map<String, String> checkClassNameMap = new HashMap<String, String>();
	private static Set<String> checkPackageNameSet = new TreeSet<String>();
	static final String[] allMySQLKeywords = new String[] { "ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS",
			"ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE",
			"CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONNECTION",
			"CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
			"CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE",
			"DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE",
			"DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF",
			"ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR",
			"FORCE", "FOREIGN", "FROM", "FULLTEXT", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND",
			"HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE",
			"INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE",
			"JOIN", "KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD",
			"LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MATCH",
			"MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD",
			"MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE", "OPTION",
			"OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE",
			"RANGE", "READ", "READS", "READ_ONLY", "READ_WRITE", "REAL", "REFERENCES", "REGEXP", "RELEASE", "RENAME",
			"REPEAT", "REPLACE", "REQUIRE", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS",
			"SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SMALLINT", "SPATIAL", "SPECIFIC",
			"SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS",
			"SQL_SMALL_RESULT", "SSL", "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB",
			"TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED",
			"UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY",
			"VARCHAR", "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE", "WITH", "WRITE", "X509", "XOR",
			"YEAR_MONTH", "ZEROFILL",
			// adding for 5.5.8:
			"GENERAL", "IGNORE_SERVER_IDS", "MASTER_HEARTBEAT_PERIOD", "MAXVALUE", "RESIGNAL", "SIGNAL", "SLOW"

	};

	public static void main(String[] args) {
		genEffectList4UnitestTest();
	}

	public static void maina(String[] args) {
		String csvpath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/TGuideInfo(引导)_Guideinfo(引导步骤).csv";
		List<String[]> list = getDataFromCSV2(csvpath);
		for (int i = 0; i < list.size() && i < 20; i++) {
			System.out.println(Arrays.toString(list.get(i)));
		}
		System.exit(0);
		CodeGenConsts.switchPlat();
		GameCSV2DB.checkIsGapp();
		genEffectReqFiles();
		System.exit(0);
		File fs[] = new File(
				"/Users/lizongbo/Documents/workspace/sango_configs/csvfiles/ProtobufFiles/TprotoCmds/CRUD/")
						.listFiles();
		for (File f : fs) {
			// File f = new File("CRUDaddDbBeanforIdProtoBufRequest.csv");
			protoBufCSV2ProtoFile(f);
			if (f.getName().endsWith("ProtoBufRequest.csv")) {
				protoBufCSV2ProtobufReqValidatorFile(f);

			}
		}

	}

	/**
	 * 把模块下的proto文件合并成一个大的协议文件文本，便于查看协议
	 * 
	 * @param protoFileRootDir
	 *            /mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/protobufs
	 */
	public static void protoReqRespFileCombine(String protoFileRootDir) {
		File dir = new File(protoFileRootDir);
		if (dir.isDirectory()) {
			File[] fs = dir.listFiles();
			for (int i = 0; i < fs.length; i++) {
				File file = fs[i];
				if (file.isDirectory()) {// 扫描 req 和resp

					StringBuilder sb = new StringBuilder();

					File[] protoFiles = file.listFiles();
					for (int j = 0; j < protoFiles.length; j++) {
						File protoFile = protoFiles[j];
						if (protoFile.getName().endsWith("ProtoBufRequest.proto")
								|| protoFile.getName().endsWith("ProtoBufResponse.proto")) {
							String fileText = GenAll.readFile(protoFile.getAbsolutePath(), "UTF-8");
							// System.err.println(protoFile+"|length==="+fileText.length()
							// +" |" +fileText.contains(s));
							if (sb.length() < 1) {
								sb.append(fileText);
							} else {
								if (fileText.indexOf("message") > fileText.indexOf("//")) {
									sb.append("\n").append(fileText.substring(fileText.indexOf("//")));

								} else {
									sb.append("\n").append(fileText.substring(fileText.indexOf("message")));
								}
							}

						}
					}

					GameCSV2DB.writeFile(
							new File(file, "AllPbReqAndResp4" + file.getName() + ".proto.txt").getAbsolutePath(),
							sb.toString(), "UTF-8");

				}
			}
		}

	}

	public static void protoBufCSV2ProtoFile(String protoFileRootDir) {
		File f = new File(protoFileRootDir);
		if (f.isFile() && f.getName().endsWith("ProtoBufRequest.csv")) {
			protoBufCSV2ProtobufReqValidatorFile(f);
		}
		if (f.isDirectory()) {
			File subfs[] = f.listFiles();
			if (subfs != null) {
				for (File subf : subfs) {
					protoBufCSV2ProtoFile(subf.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * 把表格形式定义proto协议转换成proto文件
	 * 
	 * @param csvFilePath
	 */
	public static void protoBufCSV2ProtoFile(File csvFile) {
		String tableName = getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		String protoFileName = csvFile.getName();
		protoFileName = protoFileName.substring(0, protoFileName.lastIndexOf("."));
		File protoFileDir = new File(CodeGenConsts.PROJPROTOFILE_DIRROOT, csvFile.getParentFile().getName());
		String modName = csvFile.getParentFile().getName();
		File destProtoFile = new File(protoFileDir, protoFileName + ".proto");
		System.out.println("destProtoFile==" + destProtoFile);

		StringBuilder sb = new StringBuilder();
		sb.append("package " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + ".").append(modName.toLowerCase()).append(";")
				.append("\n\n");
		sb.append("message ").append(protoFileName).append("{\n");
		for (int i = 5; i < colList.size(); i++) {
			String[] s = colList.get(i);
			if (s[5] != null && s[5].length() > 0) {
				sb.append("\t//" + s[5].replace('\n', ' ') + ";\n");
			}
			sb.append("\t" + s[1] + " " + s[2] + " " + s[3] + " = " + s[0]);
			if (s[4] != null && s[4].length() > 0) {
				sb.append(" [default = " + s[4] + "]");
			}
			sb.append(";\n");
		}

		sb.append("}").append("\n");
		GameCSV2DB.writeFile(destProtoFile.getAbsolutePath(), sb.toString());

	}

	public static void protoBufCSV2ProtobufReqValidatorFile(File csvFile) {
		genProtoBufReuestValidatorAbstractJavaFile(csvFile);
		genProtoBufReuestValidatorJavaFile(csvFile);
	}

	public static void locale2CsFile(File csvFile) {
		if ("TLocale(文本本地化)_Zh(中文信息).csv".equals(csvFile.getName())) {
			String moduleinfoCsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
					+ "/Public/Common/TComm(基础配置)_Moduleinfo(模块信息).csv";
			List<String[]> moduleInfoColList = CSVUtil.getDataFromCSV2(moduleinfoCsvPath);
			Map<String, String> moduleInfoMap = new HashMap<String, String>();
			for (int k = 5; k < moduleInfoColList.size(); k++) {
				String[] s2 = moduleInfoColList.get(k);
				moduleInfoMap.put(CSVUtil.getColValue("id", s2, moduleInfoColList),
						CSVUtil.getColValue("module_name", s2, moduleInfoColList));
			}
			moduleInfoMap.put("0", "Common");
			Map<String, StringBuilder> bianliangMap = new HashMap<String, StringBuilder>();

			String tableName = getTableNameFromCSVFile(csvFile.getAbsolutePath());
			String tableCmt = getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			for (int i = 5; i < colList.size(); i++) {
				String[] s = colList.get(i);
				String id = CSVUtil.getColValue("id", s, colList);
				int idInt = toInt(id) / 1000;
				String mod = "" + idInt;
				String enum_name = CSVUtil.getColValue("enum_name", s, colList);
				String label = CSVUtil.getColValue("label", s, colList);
				if (enum_name.length() > 0 && moduleInfoMap.containsKey(mod)) {// 是需要生成变量的
					StringBuilder sb = bianliangMap.getOrDefault(moduleInfoMap.get(mod), new StringBuilder());
					bianliangMap.put(moduleInfoMap.get(mod), sb);
					sb.append("        /// <summary>\n");
					sb.append("        /// " + StringUtil.replaceAll(label, "\n", "\n///") + "\n");
					sb.append("        /// </summary>\n");
					sb.append("        public const int " + uncapFirst(moduleInfoMap.get(mod))
							+ capFirst(DBUtil.camelName(enum_name)) + " = " + id + ";\n");
				}
			}

			for (Map.Entry<String, StringBuilder> me : bianliangMap.entrySet()) {

				String modName = me.getKey();
				String className = modName + "Locale";
				String javaFileDir = CodeGenConsts.PROJPROTO_UnityEditorSRCROOT + "/netmanagertemp/"
						+ modName.toLowerCase() + "/";
				String javaFileName = className + ".cs.txt";

				File javaFile = new File(javaFileDir, javaFileName);
				GameCSV2DB.writeFile(javaFile.getAbsolutePath(), me.getValue().toString());

			}
		}
	}

	private static void genProtoBufReuestValidatorAbstractJavaFile(File csvFile) {
		String modName = csvFile.getParentFile().getName();
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		String reqClassName = csvFile.getName().substring(0, csvFile.getName().indexOf("."));
		String className = "Abstract" + reqClassName + "Validator";
		String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".protoreqvalidator."
				+ modName.toLowerCase() + ".abstracts";
		String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
		String javaFileName = className + ".java";
		String tableName = getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		StringBuilder sb = new StringBuilder();
		sb.append("package " + packageName + ";\n");
		sb.append("\n");
		sb.append("import java.util.*;\n");
		sb.append("import " + CodeGenConsts.UTILS_JAVAPACKAGEROOT + ".*;\n");
		sb.append("\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "." + modName.toLowerCase() + "." + reqClassName
				+ "OuterClass.*;\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".ProtoBufRequestValidator;\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".ProtoBufRequestValidatorErrorInfo;\n");
		sb.append("\n");
		sb.append("public abstract class " + className + "\n");
		sb.append("		implements ProtoBufRequestValidator<" + reqClassName + "> {\n");
		sb.append("\n");
		sb.append("	@Override\n");
		sb.append("	public List<ProtoBufRequestValidatorErrorInfo> validate(\n");
		sb.append("			" + reqClassName + " protoBufRequest) {\n");
		sb.append(
				"		List<ProtoBufRequestValidatorErrorInfo> errors = validateInternalbyAutoGenCode(protoBufRequest);\n");
		sb.append("		return validateInternal(protoBufRequest, errors);\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public List<ProtoBufRequestValidatorErrorInfo> validateInternalbyAutoGenCode(\n");
		sb.append("			" + reqClassName + " protoBufRequest) {\n");
		sb.append(
				"List<ProtoBufRequestValidatorErrorInfo> errorInfoList = new ArrayList<ProtoBufRequestValidatorErrorInfo>();");

		for (int i = 5; i < colList.size(); i++) {
			String[] s = colList.get(i);
			// 整数大于

			//
			for (int kk = 6; kk <= 7; kk++) {
				if (s.length > kk && s[kk] != null && s[kk].trim().length() > 0) {
					sb.append("\nif (!(protoBufRequest.get" + capFirst(DBUtil.camelName(s[3])) + "() " + s[kk]
							+ ")) {\n");
					sb.append("errorInfoList.add(" + "ProtoBufRequestValidatorErrorInfo.getNumberValidateErrorInfo(\""
							+ s[3] + "\", protoBufRequest.get" + capFirst(DBUtil.camelName(s[3])) + "()," + "\"" + s[kk]
							+ "\"" + "));\n");
					sb.append("}\n");
				}

			}

			for (int kk = 8; kk <= 9; kk++) {
				if (s.length > kk && s[kk] != null && s[kk].trim().length() > 0) {
					sb.append("\nif (!(protoBufRequest.get" + capFirst(DBUtil.camelName(s[3])) + "().length() " + s[kk]
							+ ")) {\n");
					sb.append("errorInfoList.add("
							+ "ProtoBufRequestValidatorErrorInfo.getStringLengthMoreThanErrorInfo(\"" + s[3]
							+ "\", protoBufRequest.get" + capFirst(DBUtil.camelName(s[3])) + "().length()," + "\""
							+ s[kk] + "\"" + "));\n");
					sb.append("}\n");
				}
			}

			if (s.length > 10 && s[10] != null && s[10].trim().length() > 0) {
				sb.append("\nif (!(ValidateUtil.isEmail(protoBufRequest.get" + capFirst(DBUtil.camelName(s[3]))
						+ "())) {\n");
				sb.append("errorInfoList.add(" + "ProtoBufRequestValidatorErrorInfo.getStringisNotEmailErrorInfo(\""
						+ s[3] + "\", protoBufRequest.get" + capFirst(DBUtil.camelName(s[3])) + "().length()," + "\""
						+ s[10] + "\"" + "));\n");
				sb.append("}\n");
			}

			if (s.length > 11 && s[11] != null && s[11].trim().length() > 0) {
				sb.append("\nif (!(ValidateUtil.isURL(protoBufRequest.get" + capFirst(DBUtil.camelName(s[3]))
						+ "()))) {\n");
				sb.append("errorInfoList.add(" + "ProtoBufRequestValidatorErrorInfo.getStringisNotURLErrorInfo(\""
						+ s[3] + "\", protoBufRequest.get" + capFirst(DBUtil.camelName(s[3])) + "().length()," + "\""
						+ s[1] + "\"" + "));\n");
				sb.append("}\n");
			}

			if (s[1].trim().equalsIgnoreCase("repeated"))
			// && s.length > 12 && s[12] != null&& s[12].trim().length() > 0)
			{// 这里由于设置阵容的时候机甲id是可以重复的，因此还是需要做成配置项
				sb.append("		// 检查List里的重复数据,目前允许0重复用来占位\n");
				sb.append("		if (protoBufRequest.get" + capFirst(DBUtil.camelName(s[3])) + "List().size() > 0) {\n");
				sb.append("			Set<Object> set = new HashSet<Object>();\n");

				if ("int32".equalsIgnoreCase(s[2].trim())) {
					sb.append("			for (Integer item : protoBufRequest\n");
				} else if ("int64".equalsIgnoreCase(s[2].trim())) {
					sb.append("			for (Long item : protoBufRequest\n");
				} else {
					sb.append("			for (Object item : protoBufRequest\n");
				}
				sb.append("					.get" + capFirst(DBUtil.camelName(s[3])) + "List()) {\n");
				if ("int32".equalsIgnoreCase(s[2].trim())) {
					sb.append("				if (!Integer.valueOf(0).equals(item) && set.contains(item)) {\n");
				} else if ("int64".equalsIgnoreCase(s[2].trim())) {
					sb.append("				if (!Long.valueOf(0).equals(item) && set.contains(item)) {\n");

				} else {

					sb.append("				if (set.contains(item)) {\n");
				}

				sb.append(
						"					errorInfoList.add(ProtoBufRequestValidatorErrorInfo.getNumberValidateErrorInfo(\""
								+ DBUtil.camelName(s[3]) + "\",\n");
				sb.append("							protoBufRequest.get" + capFirst(DBUtil.camelName(s[3]))
						+ "List(), String.valueOf(item)));\n");
				sb.append("				} else {\n");
				sb.append("					set.add(item);\n");
				sb.append("				}\n");
				sb.append("			}\n");
				sb.append("		}\n");
			}
		}

		sb.append("		return errorInfoList;\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	// need impl\n");
		sb.append("	public abstract List<ProtoBufRequestValidatorErrorInfo> validateInternal(\n");
		sb.append("			" + reqClassName + " protoBufRequest,\n");
		sb.append("			List<ProtoBufRequestValidatorErrorInfo> errors);\n");
		sb.append("\n");
		sb.append("}\n");
		File protoFile = new File(javaFileDir, javaFileName);
		GameCSV2DB.writeFile(protoFile.getAbsolutePath(), sb.toString());
	}

	private static void genProtoBufReuestValidatorJavaFile(File csvFile) {
		String modName = csvFile.getParentFile().getName();
		String protoRpcJavaServiceCodeRootDir = CodeGenConsts.PROJPROTO_JAVASRCROOT;
		String reqClassName = csvFile.getName().substring(0, csvFile.getName().indexOf("."));
		String className = reqClassName + "Validator";
		String packageName = CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".protoreqvalidator."
				+ modName.toLowerCase();
		String javaFileDir = protoRpcJavaServiceCodeRootDir + "/" + packageName.replace('.', '/') + "/";
		String javaFileName = className + ".java";

		StringBuilder sb = new StringBuilder();
		sb.append("package " + packageName + ";\n");
		sb.append("\n");
		sb.append("import java.util.List;\n");
		sb.append("import " + packageName + ".abstracts.*;\n");
		sb.append("\n");
		sb.append("import " + CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT + "." + modName.toLowerCase() + "." + reqClassName
				+ "OuterClass.*;\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".ProtoBufRequestValidator;\n");
		sb.append("import " + CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT + ".ProtoBufRequestValidatorErrorInfo;\n");
		sb.append("\n");
		sb.append("public class " + className + "\n");
		sb.append("		extends Abstract" + className + " {\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("	@Override\n");
		sb.append("	public List<ProtoBufRequestValidatorErrorInfo> validateInternal(\n");
		sb.append("			" + reqClassName + " protoBufRequest,\n");
		sb.append("			List<ProtoBufRequestValidatorErrorInfo> errors){\n");
		sb.append("			return errors;");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("}\n");
		File protoFile = new File(javaFileDir, javaFileName);
		if (true || !protoFile.exists()) {
			GameCSV2DB.writeFile(protoFile.getAbsolutePath(), sb.toString());
		} else {
			System.out.println(protoFile + " exists so ignore it");
		}
	}

	private static Map<String, List<String[]>> csvCachemap = new HashMap<String, List<String[]>>();

	/**
	 * 解析csv文件
	 * 
	 * @param csvFilePath
	 * @return
	 */
	public static List<String[]> getDataFromCSV2(String csvFilePath) {
		List<String[]> list = null;
		String tbn = getTableNameFromCSVFile(csvFilePath);
		if (!tbn.startsWith("tlocale")) {
			list = csvCachemap.get(csvFilePath);
		}

		if (list == null) {
			list = getDataFromCSV2Internal(csvFilePath);
			csvCachemap.put(csvFilePath, list);
		}
		if ("tlocale_collect".equals(tbn)) {//
			{
				Set<String> idSet = new TreeSet<String>();
				for (int k = 4; k < list.size(); k++) {
					String[] arr = list.get(k);
					String id = "";
					if (arr.length > 0) {
						id = getColValue("locale_key", arr, list);
						if (idSet.contains(id)) {
							GameCSV2DB.addErrMailMsgList("tlocale_collect|idRepeat|locale_key|" + (k + 1)
									+ "行locale_key重复|" + id + "|for|" + csvFilePath + "|idSet=" + idSet.size());
						} else {
							idSet.add(id);
						}
					}
				}
			}

		}
		return list;
	}

	public static List<String[]> getDataFromCSV2Internal(String csvFilePath) {
		try {
			java.io.FileInputStream fis = new FileInputStream(csvFilePath);
			java.io.InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			// CSVReader cr = new CSVReader(isr);
			// List<String[]> colList = cr.readAll();//
			// 前四行是潜规则的，第一行是表的字段名，第二行是字段类型，第三行是表的字段说明，第一列是主键。
			// 第四行约定是程序专用注释行，第四行第一列必须是井号开头的
			// csv需要支持井号档注释行

			org.apache.commons.csv.CSVParser csvp = new CSVParser(isr, org.apache.commons.csv.CSVFormat.EXCEL);
			List<CSVRecord> csvrList = csvp.getRecords();

			List<String[]> colList = new ArrayList<String[]>();
			for (CSVRecord csvr : csvrList) {
				List<String> valueList = new ArrayList<String>();
				java.util.Iterator<String> vi = csvr.iterator();
				while (vi.hasNext()) {
					valueList.add(vi.next());
				}
				colList.add(valueList.toArray(new String[0]));
			}
			// 真的t开头的且不是protbuf协议的csv，则需要按照注释规范来检查
			if (new File(csvFilePath).getName().toLowerCase().startsWith("t") && !csvFilePath.contains("ProtobufFiles")
					&& !(new File(csvFilePath).getName().endsWith("ProtoBufRequest.csv"))
					&& !(new File(csvFilePath).getName().endsWith("ProtoBufResponse.csv"))) {
				if (colList.size() > 3) {// 第一行的全部修正为小写且去除空格
					String[] arr = colList.get(0);
					for (int i = 0; i < arr.length; i++) {
						arr[i] = arr[i].trim().toLowerCase();
					}
				}
				for (int i = colList.size() - 1; i >= 4; i--) {
					// 第4行及以后的，井号开头的行表示是注释行，需要滤掉
					if (colList.get(i) != null && colList.get(i)[0].trim().startsWith("#")) {
						System.out.println("xuyaohulvezhushihang:" + csvFilePath + "|hang=" + i + "|values="
								+ Arrays.toString(colList.get(i)));
						colList.remove(i);
					}
				}
				// 至少4行的t开头的表，则检查第四行第一列是不是井号开头
				if (new File(csvFilePath).getName().toLowerCase().startsWith("t") && colList.size() >= 4) {
					if (colList.get(3) == null || !colList.get(3)[0].trim().startsWith("#")) {
//						GameCSV2DB.addErrMailMsgList("getDataFromCSV2|checkError|bushijinghao|" + csvFilePath
//								+ "|hang=3|values=" + Arrays.toString(colList.get(3)));
					}
				}
				// 找出注释列，然后过滤掉

				String[] colNames = colList.get(0);
				List<Integer> cmtColIndexList = new ArrayList<Integer>();// 找出是注释的列，把这些列清除掉
				for (int i = 0; i < colNames.length; i++) {
					String colName = colNames[i];
					if (colName.trim().startsWith("#")) {
						cmtColIndexList.add(Integer.valueOf(i));
						System.out.println("zhushilieyou|" + csvFilePath + "|name=" + colName + "|at|" + i);
					}
				}
				for (int i = 0; i < colList.size(); i++) {
					String[] as = colList.get(i);
					colList.set(i, trimArray(as, cmtColIndexList));
				}
				if (cmtColIndexList.size() > 0) {
					System.out.println("cmtColIndexList==" + cmtColIndexList + "|zhushilieqingchuhou:"
							+ Arrays.toString(colList.get(0)));
				}
				// 扫一下数值是否填满了
				checkGameCSVNumber(csvFilePath, colList);
			}
			// 插一步多语言替换，优先使用多语言表里的值
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFilePath);
			if (!tableName.startsWith("tlocale")) {// 只有不是多语言的表，才需要做替换，否则就搞成死循环了
				System.out.println("需要加载替换多语言的信息" + tableName + "|" + csvFilePath);
				I18NUtil.fixI18NDbColValue(tableName, colList);
			}
			return colList;
		} catch (Exception e) {
			// gecaoshoulie_configs/csvfiles//ProtobufFiles/TprotoCmds/
			// 下会自动生成文件，所以第一次找不到是正常的，不告警了
			if (!csvFilePath.contains("TprotoCmds") && !csvFilePath.contains("ProtobufFiles")) {
				GameCSV2DB.addErrMailMsgList("getDataFromCSV2|error|for|" + csvFilePath + e);
			} else {
				System.err.println("getDataFromCSV2|error|for|" + csvFilePath + e);
			}
			e.printStackTrace();
		}
		return new ArrayList<String[]>();
	}

	/**
	 * 解析csv文件
	 * 
	 * @param csvFilePath
	 * @return
	 */
	public static List<String[]> getDataFromCSV2WithoutCheck(String csvFilePath) {
		try {
			java.io.FileInputStream fis = new FileInputStream(csvFilePath);
			java.io.InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			// CSVReader cr = new CSVReader(isr);
			// List<String[]> colList = cr.readAll();//
			// 前四行是潜规则的，第一行是表的字段名，第二行是字段类型，第三行是表的字段说明，第一列是主键。
			// 第四行约定是程序专用注释行，第四行第一列必须是井号开头的
			// csv需要支持井号档注释行

			org.apache.commons.csv.CSVParser csvp = new CSVParser(isr, org.apache.commons.csv.CSVFormat.EXCEL);
			List<CSVRecord> csvrList = csvp.getRecords();

			List<String[]> colList = new ArrayList<String[]>();
			for (CSVRecord csvr : csvrList) {
				List<String> valueList = new ArrayList<String>();
				java.util.Iterator<String> vi = csvr.iterator();
				while (vi.hasNext()) {
					valueList.add(vi.next());
				}
				// System.out.println(valueList);
				colList.add(valueList.toArray(new String[0]));
			}
			// 真的t开头的且不是protbuf协议的csv，则需要按照注释规范来检查
			if (new File(csvFilePath).getName().toLowerCase().startsWith("t") && !csvFilePath.contains("ProtobufFiles")
					&& !(new File(csvFilePath).getName().endsWith("ProtoBufRequest.csv"))
					&& !(new File(csvFilePath).getName().endsWith("ProtoBufResponse.csv"))) {
				for (int i = colList.size() - 1; i >= 4; i--) {
					// 第4行及以后的，井号开头的行表示是注释行，需要滤掉
					if (colList.get(i) != null && colList.get(i)[0].trim().startsWith("#")) {
						System.out.println("xuyaohulvezhushihang:" + csvFilePath + "|hang=" + i + "|values="
								+ Arrays.toString(colList.get(i)));
						colList.remove(i);
					}
				}
				// 至少4行的t开头的表，则检查第四行第一列是不是井号开头
				if (new File(csvFilePath).getName().toLowerCase().startsWith("t") && colList.size() >= 4) {
					if (colList.get(3) == null || !colList.get(3)[0].trim().startsWith("#")) {
//						System.err.println("getDataFromCSV2|checkError|bushijinghao|" + csvFilePath + "|hang=3|values="
//								+ Arrays.toString(colList.get(3)));
					}
				}
				// 找出注释列，然后过滤掉

				String[] colNames = colList.get(0);
				List<Integer> cmtColIndexList = new ArrayList<Integer>();// 找出是注释的列，把这些列清除掉
				for (int i = 0; i < colNames.length; i++) {
					String colName = colNames[i];
					if (colName.trim().startsWith("#")) {
						cmtColIndexList.add(Integer.valueOf(i));
						System.out.println("zhushilieyou|" + csvFilePath + "|name=" + colName + "|at|" + i);
					}
				}
				for (int i = 0; i < colList.size(); i++) {
					String[] as = colList.get(i);
					colList.set(i, trimArray(as, cmtColIndexList));
				}
				if (cmtColIndexList.size() > 0) {
					System.out.println("cmtColIndexList==" + cmtColIndexList + "|zhushilieqingchuhou:"
							+ Arrays.toString(colList.get(0)));
				}
			}
			return colList;
		} catch (Exception e) {
			System.err.println("getDataFromCSV2|error|for|" + csvFilePath + e);
			e.printStackTrace();
		}
		return new ArrayList<String[]>();
	}

	/**
	 * 滤掉注释列的数据内容
	 * 
	 * @param arr
	 * @param cmtColIndexList
	 * @return
	 */
	private static String[] trimArray(String[] arr, List<Integer> cmtColIndexList) {
		if (cmtColIndexList == null || cmtColIndexList.size() < 1) {
			if (arr != null) {
				for (int i = 0; i < arr.length; i++) {
					String string = arr[i];
					arr[i] = StringUtil.tryParse2UnityRichText(string);
				}
			}
			return arr;
		}
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < arr.length; i++) {
			String string = arr[i];
			if (cmtColIndexList.contains(Integer.valueOf(i))) {
				// System.out.println(
				// "trimArray|needTrim|" + i + "|for|" + cmtColIndexList +
				// "|value=" + Arrays.toString(arr));
			} else {
				list.add(StringUtil.tryParse2UnityRichText(string));
			}
		}
		return list.toArray(new String[0]);
	}

	private static Set<String> csvFilePathSet = new HashSet<String>();
	public static Map<String, Set<String>> interrColMap = new TreeMap<String, Set<String>>();

	private static void addErrCol(String fileName, String colName) {
		Set<String> listCol = interrColMap.get(fileName);
		if (listCol == null) {
			listCol = new TreeSet<String>();
			interrColMap.put(fileName, listCol);
		}
		listCol.add(colName);
	}

	private static Set<String> mysqlKeywordSet = new HashSet<String>();

	public static boolean checkGameCSVNumber(String csvFilePath, List<String[]> colList) {
		if (mysqlKeywordSet.size() < 1) {
			for (String k : allMySQLKeywords) {
				mysqlKeywordSet.add(k.trim().toLowerCase());
			}
		}
		try {
			// 确保只需要运行一次
			if (csvFilePathSet.contains(csvFilePath)) {
				return true;
			}
			csvFilePathSet.add(csvFilePath);
			Set<String> colTypeSet = new TreeSet<String>();
			colTypeSet.add("int");
			colTypeSet.add("long");
			colTypeSet.add("float");
			colTypeSet.add("double");
			colTypeSet.add("str");
			colTypeSet.add("string");
			colTypeSet.add("text");
			colTypeSet.add("int[]");
			colTypeSet.add("long[]");
			colTypeSet.add("string[]");
			colTypeSet.add("float[]");
			// colTypeSet.add("double[]");
			// colTypeSet.add("int[][]");
			// colTypeSet.add("long[][]");
			// colTypeSet.add("float[][]");
			// colTypeSet.add("double[][]");
			{// 在这里先清除空列
				String[] types = colList.get(1);
				int realTypeLen = types.length;
				for (int i = 0; i < types.length; i++) {
					String type = types[i];
					if (type.trim().length() > 0 || colList.get(0)[i].trim().length() > 0
							|| colList.get(0)[i].trim().length() > 0) {// 字段名，字段类型或字段说明，三者任何一个有，就认为是合法字段
						realTypeLen = i + 1;
					}
				}

				String[] namesNew = Arrays.copyOf(colList.get(0), realTypeLen);
				String[] typesNew = Arrays.copyOf(colList.get(1), realTypeLen);
				String[] cmtsNew = Arrays.copyOf(colList.get(2), realTypeLen);
				if (realTypeLen != types.length) {// 表格有空列
					// System.out.println("youkonglie|name|" + csvFilePath +
					// "|org=" + Arrays.toString(colList.get(0))
					// + "|new=" + Arrays.toString(namesNew));
					// System.out.println("youkonglie|type|" + csvFilePath +
					// "|org=" + Arrays.toString(colList.get(1))
					// + "|new=" + Arrays.toString(typesNew));
					// System.out.println("youkonglie|cmts|" + csvFilePath +
					// "|org=" + Arrays.toString(colList.get(2))
					// + "|new=" + Arrays.toString(cmtsNew));
					colList.set(0, namesNew);
					colList.set(1, typesNew);
					colList.set(2, cmtsNew);
				}
			}
			String[] types = colList.get(1);
			// 先检查字段类型是否正确
			for (int i = 0; i < types.length; i++) {
				String type = types[i];
				if (i == 0) {
					if (!("int".equals(type)) && !("string".equals(type))) {
						GameCSV2DB.addErrMailMsgList(
								"checkGameCSVNumber|typeerror|第一列字段类型必须int或string|" + type + "|for|" + csvFilePath + "|"
										+ i + "|name=" + colList.get(0)[i] + "|合法的类型为" + colTypeSet);

					}
				}
				if (!colTypeSet.contains(type)) {
					GameCSV2DB.addErrMailMsgList("checkGameCSVNumber|typeerror|值类型不对|" + type + "|for|" + csvFilePath
							+ "|" + i + "|name=" + colList.get(0)[i] + "|合法的类型为" + colTypeSet);
				}
			}

			for (int i = 0; i < colList.get(0).length; i++) {
				String colName = colList.get(0)[i].trim();
				if (colName.length() != colList.get(0)[i].length()) {
					GameCSV2DB.addErrMailMsgList(
							"checkGameCSVNumber|colNameerror|needTrim|字段名不能有空格|" + colName + "|for|" + csvFilePath + "|"
									+ i + "|name=" + colName + "|i=" + i + "|from|" + Arrays.toString(colList.get(0)));
				}
				if (colName.length() < 1) {
					GameCSV2DB.addErrMailMsgList(
							"checkGameCSVNumber|colNameerror|字段名不能为空|" + colName + "|for|" + csvFilePath + "|" + i
									+ "|name=" + colName + "|i=" + i + "|from|" + Arrays.toString(colList.get(0)));
				}
				if (mysqlKeywordSet.contains(colName)) {
					GameCSV2DB.addErrMailMsgList("checkGameCSVNumber|colNameerror|isMysqlKeywords|字段名不能用数据库关键词|"
							+ colName + "|for|" + csvFilePath + "|" + i + "|name=" + colName + "|i=" + i + "|from|"
							+ Arrays.toString(colList.get(0)));
				}
			}
			for (int i = 0; i < colList.get(2).length; i++) {
				String colName = colList.get(2)[i];
				if (colName.trim().length() < 1) {
					GameCSV2DB.addErrMailMsgList(
							"checkGameCSVNumber|colCmterror|字段注释不能为空|" + colName + "|for|" + csvFilePath + "|" + i
									+ "|name=" + colName + "|i=" + i + "|from|" + Arrays.toString(colList.get(2)));
				}
			}
			{
				Set<String> idSet = new TreeSet<String>();
				for (int k = 4; k < colList.size(); k++) {
					String[] arr = colList.get(k);
					String id = "";
					if (arr.length > 0) {
						id = arr[0].trim();
						if (idSet.contains(id)) {
							GameCSV2DB.addErrMailMsgList("checkGameCSVNumber|idRepeat|" + (k + 1) + "行主键id重复|" + id
									+ "|for|" + csvFilePath + "|idSet=" + idSet);
						} else {
							idSet.add(id);
						}
					}
				}
			}

			for (int i = 0; !csvFilePath.contains("TProto") && i < types.length; i++) {
				String type = types[i];
				for (int k = 4; k < colList.size(); k++) {
					String val = colList.get(k)[i];
					// 跳过题库表和多语言表
					if (!csvFilePath.contains("TGuideInfo") && !csvFilePath.contains("TLocale")
							&& !csvFilePath.contains("_Questionbank") && HasSpaceAtBeginOrEnd(val)) {
						String fileName = new File(csvFilePath).getName();
						addErrCol(fileName, colList.get(0)[i]);
						GameCSV2DB.addErrMailMsgList("checkGameCSVNumber|valueerror|" + type + "=" + val
								+ "|duoyukongge|for|" + csvFilePath + "|" + (k + 1) + "行|name=" + colList.get(0)[i]);
					}
					if (type.equals("int")) {
						if (!isInteger(val)) {
							String fileName = new File(csvFilePath).getName();
							addErrCol(fileName, colList.get(0)[i]);
							GameCSV2DB.addErrMailMsgList("checkGameCSVNumber|valueerror|" + type + "=" + val
									+ "|isnot|int|for|" + csvFilePath + "|" + (k + 1) + "行|name=" + colList.get(0)[i]);
						}
					}
					if (type.equals("long")) {
						if (!isLong(val)) {
							String fileName = new File(csvFilePath).getName();
							addErrCol(fileName, colList.get(0)[i]);
							GameCSV2DB.addErrMailMsgList("checkGameCSVNumber|valueerror|" + type + "=" + val
									+ "|isnot|long|for|" + csvFilePath + "|" + (k + 1) + "行|name=" + colList.get(0)[i]);
						}
					}
					if (type.equals("float")) {
						if (!isFloat(val)) {
							String fileName = new File(csvFilePath).getName();
							addErrCol(fileName, colList.get(0)[i]);
							GameCSV2DB.addErrMailMsgList(
									"checkGameCSVNumber|valueerror|" + type + "=" + val + "|isnot|float|for|"
											+ csvFilePath + "|" + (k + 1) + "行|name=" + colList.get(0)[i]);
						}
					}
					if (type.equals("double")) {
						if (!isDouble(val)) {
							String fileName = new File(csvFilePath).getName();
							addErrCol(fileName, colList.get(0)[i]);
							GameCSV2DB.addErrMailMsgList(
									"checkGameCSVNumber|valueerror|" + type + "=" + val + "行|isnot|double|for|"
											+ csvFilePath + "|" + (k + 1) + "|name=" + colList.get(0)[i]);
						}
					}
					if (type.startsWith("str") || type.equals("text") || type.contains("[]")) {
						if (type.startsWith("str") && val.getBytes(StandardCharsets.UTF_8).length > 199) {
							GameCSV2DB.addErrMailMsgList(
									"checkGameCSVNumber|valueerror|" + type + "=" + val + "行|string类型值太长，请改成text类型|for|"
											+ csvFilePath + "|" + (k + 1) + "|name=" + colList.get(0)[i]);
						}
						if (val.trim().equals("-1")) {// 字符串的-1需要吞掉
							colList.get(k)[i] = "";
						}
					}

					if (type.equals("int[]")) {// 数组检查
						if (!isIntegerArray(val)) {
							String fileName = new File(csvFilePath).getName();
							addErrCol(fileName, colList.get(0)[i]);
							GameCSV2DB.addErrMailMsgList(
									"checkGameCSVNumber|valueerror|" + type + "=" + val + "|isnot|int[]|for|"
											+ csvFilePath + "|" + (k + 1) + "行|name=" + colList.get(0)[i]);
						}
					}

					if (type.equals("long[]")) {
						if (!isLongArray(val)) {
							String fileName = new File(csvFilePath).getName();
							addErrCol(fileName, colList.get(0)[i]);
							GameCSV2DB.addErrMailMsgList(
									"checkGameCSVNumber|valueerror|" + type + "=" + val + "|isnot|long[]|for|"
											+ csvFilePath + "|" + (k + 1) + "行|name=" + colList.get(0)[i]);
						}
					}
					if (type.equals("float[]")) {
						if (!isFloatArray(val)) {
							String fileName = new File(csvFilePath).getName();
							addErrCol(fileName, colList.get(0)[i]);
							GameCSV2DB.addErrMailMsgList(
									"checkGameCSVNumber|valueerror|" + type + "=" + val + "|isnot|float[]|for|"
											+ csvFilePath + "|" + (k + 1) + "行|name=" + colList.get(0)[i]);
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean HasSpaceAtBeginOrEnd(String str) {
		try {
			return str.length() != str.trim().length();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean isInteger(String str) {
		try {
			int i = Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean isIntegerArray(String line) {
		try {
			if (line != null) {
				line = line.trim().replace(';', ',').replace('；', ',').replace('，', ',');
			}
			String[] ss = StringUtil.split(line, ",");
			for (int i = 0; i < ss.length; ++i) {
				int k = Integer.parseInt(ss[i]);
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean isLong(String str) {
		try {
			long i = Long.parseLong(str);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean isLongArray(String line) {
		try {
			if (line != null) {
				line = line.trim().replace(';', ',').replace('；', ',').replace('，', ',');
			}
			String[] ss = StringUtil.split(line, ",");
			for (int i = 0; i < ss.length; ++i) {
				long k = Long.parseLong(ss[i]);
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean isFloat(String str) {
		try {
			float i = Float.parseFloat(str);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean isFloatArray(String line) {
		try {
			if (line != null) {
				line = line.trim().replace(';', ',').replace('；', ',').replace('，', ',');
			}
			String[] ss = StringUtil.split(line, ",");
			for (int i = 0; i < ss.length; ++i) {
				float k = Float.parseFloat(ss[i]);
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean isDouble(String str) {
		try {
			double i = Double.parseDouble(str);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	/**
	 * 
	 * <b>功能：获取csv缺失的字段名列表</b><br>
	 * <br>
	 * <b>实现步骤：</b><br>
	 * <b>1.</b> <br>
	 * <b>2.</b> <br>
	 * 
	 * @修改者 ~ , quickli 2015-3-31
	 * @param csvColNames
	 * @param dbColNames
	 * @return Set<String>
	 */
	public static Set<String> getCSVNoColNameSet(String[] csvColNames, List<String> dbColNames) {
		Set<String> allSet = new HashSet<String>();
		Set<String> csvNoSet = new HashSet<String>();// 数据库有，但是csv却干掉了的列名
		for (String s : csvColNames) {
			if (s != null && s.trim().length() > 0) {
				allSet.add(s.toLowerCase().trim());
			}
		}
		for (String s : dbColNames) {
			if (s != null && s.trim().length() > 0) {
				allSet.add(s.toLowerCase().trim());
			}
		}
		csvNoSet.addAll(allSet);
		for (String s : csvColNames) {
			if (s != null && s.trim().length() > 0) {
				csvNoSet.remove(s.toLowerCase().trim());
			}
		}
		return csvNoSet;

	}

	public static String getTableNamePrefixCSVFile(String csvFilePath) {
		try {
			File f = new File(csvFilePath);
			String name = f.getName();// 过滤掉中文，截取掉大表前缀名。
			name = name.replace("（", "(");
			name = name.replace("—", "_");
			if (!name.contains("(")) {
				return "";
			}
			if (!name.contains("_")) {
				return "";
			}
			if (name.contains("__")) {// 连续下划线肯定不行
				return "";
			}
			String tbprefix = name.substring(0, name.indexOf("_"));
			// System.out.println("tbprefix=" + tbprefix);
			String tbsufix = name.substring(name.indexOf("_") + 1, name.indexOf("."));
			tbprefix = tbprefix.substring(0, tbprefix.indexOf("("));
			tbsufix = tbsufix.substring(0, tbsufix.indexOf("("));
			// System.out.println("getTableNameFromCSVFile|" + tableName +
			// "|for|" + csvFilePath);
			return tbprefix.toLowerCase();
		} catch (Exception e) {
			System.err.println("getTableNameFromCSVFile|not table|for|" + csvFilePath + "|" + e);
			// e.printStackTrace();
		}
		return "";
	}

	public static String getTableNameFromCSVFile(String csvFilePath) {
		try {
			File f = new File(csvFilePath);
			String name = f.getName();// 过滤掉中文，截取掉大表前缀名。
			name = name.replace("（", "(");
			name = name.replace("—", "_");
			if (!name.contains("(")) {
				return "";
			}
			if (!name.contains("_")) {
				return "";
			}
			if (name.contains("__")) {// 连续下划线肯定不行
				return "";
			}
			String tbprefix = name.substring(0, name.indexOf("_"));
			// System.out.println("tbprefix=" + tbprefix);
			String tbsufix = name.substring(name.indexOf("_") + 1, name.indexOf("."));
			tbprefix = tbprefix.substring(0, tbprefix.indexOf("("));
			tbsufix = tbsufix.substring(0, tbsufix.indexOf("("));
			String tableName = tbprefix + "_" + tbsufix;
			// System.out.println("getTableNameFromCSVFile|" + tableName +
			// "|for|" + csvFilePath);
			return tableName.toLowerCase();
		} catch (Exception e) {
			System.err.println("getTableNameFromCSVFile|not table|for|" + csvFilePath + "|" + e);
			// e.printStackTrace();
		}
		return "";
	}

	public static String getTableCmtFromCSVFile(String csvFilePath) {
		try {
			File f = new File(csvFilePath);
			String name = f.getName();// 过滤掉中文，截取掉大表前缀名。

			name = name.replace('（', '(');
			name = name.replace('—', '_');
			if (!name.contains("(")) {
				return "nocmtC";
			}
			if (!name.contains("_")) {
				return "nocmt_";
			}
			String tbprefix = name.substring(0, name.indexOf("_"));
			String tbsufix = name.substring(name.indexOf("_") + 1, name.indexOf("."));

			tbprefix = tbprefix.substring(tbprefix.indexOf("("));
			tbprefix = tbprefix.replace('(', ' ').replace(')', ' ').trim();

			tbsufix = tbsufix.substring(tbsufix.indexOf("("));
			tbsufix = tbsufix.replace('(', ' ').replace(')', ' ').trim();
			// System.out.println("getTableNameFromCSVFile|" + tableName +
			// "|for|" + csvFilePath);
			return tbprefix + "_" + tbsufix;
		} catch (Exception e) {
			System.err.println("getTableCmtFromCSVFile|not table|for|" + csvFilePath + "|" + e);
			// e.printStackTrace();
		}
		return "";
	}

	/**
	 * 首字母大写
	 * 
	 * @param s
	 * @return
	 */
	public static String capFirst(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	/**
	 * 首字母小写
	 * 
	 * @param s
	 * @return
	 */
	public static String uncapFirst(String s) {
		return Character.toLowerCase(s.charAt(0)) + s.substring(1);
	}

	/**
	 * 根据字段名从csv中找到字段对应的位置 ,针对策划专用csv
	 * 
	 * @param colName
	 * @param colList
	 * @return
	 */
	public static int getColIndex(String colName, List<String[]> colList) {
		if (colName != null && colName.length() > 0 && colList != null && colList.size() > 0) {
			String[] colNames = colList.get(0);
			for (int i = 0; colNames != null && i < colNames.length; i++) {
				String string = colNames[i];
				if (colName.equalsIgnoreCase(string)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 根据字段名找到当前行数据的对应字段值
	 * 
	 * @param colName
	 *            字段名
	 * @param s
	 *            当前行数据
	 * @param colList
	 *            csv 原始数据,第一行为字段名称,策划专用配置csv
	 * @return
	 */
	public static String getColValue(String colName, String[] s, List<String[]> colList) {

		if (colName != null && colName.length() > 0 && colList != null && colList.size() > 0 && s != null
				&& s.length > 0) {
			int index = getColIndex(colName, colList);
			if (index >= 0 && index < s.length) {
				return s[index];
			}
		}

		return "";
	}

	public static String getReqRespColValue(String colName, String[] s, List<String[]> colList) {
		if (colName != null && colName.length() > 0 && colList != null && colList.size() > 0 && s != null
				&& s.length > 0) {
			int index = getReqRespColIndex(colName, colList);
			if (index >= 0 && index < s.length) {
				return s[index];
			}
		}

		return "";
	}

	/**
	 * 根据字段名从csv中找到字段对应的位置 ,针对protobuf协议的csv
	 * 
	 * @param colName
	 * @param colList
	 * @return
	 */
	public static int getReqRespColIndex(String colName, List<String[]> colList) {
		if (colName != null && colName.length() > 0 && colList != null && colList.size() > 0) {
			String[] colNames = colList.get(4);
			for (int i = 0; colNames != null && i < colNames.length; i++) {
				String string = colNames[i];
				if (colName.equalsIgnoreCase(string)) {
					return i;
				}
			}
		}
		return -1;
	}

	private static String getColDatabyColValue(String colName4Query, String colValue, String colName4Get,
			List<String[]> csvColList) {
		// String[] s = new String[0];
		for (int i = 4; csvColList != null && i < csvColList.size(); i++) {
			String[] a = csvColList.get(i);
			if (getColValue(colName4Query, a, csvColList).equals(colValue)) {
				return getColValue(colName4Get, a, csvColList);
			}
		}
		return "";
	}

	/**
	 * 遍历3个csv文件按周按特效美术负责人生成需求列表
	 */
	public static void genEffectReqFiles() {
		String csv1 = (CodeGenConsts.PROJCSVFILE_DIRROOT
				+ "/Data/DRoleEffectReqs(角色特效需求信息)_Effectreqinfo(特效需求信息维护表).csv");
		String[] csv2 = new String[] {
				CodeGenConsts.PROJCSVFILE_DIRROOT + "/Data/DSceneEffectReqs(场景特效需求信息)_Effectreqinfo(特效需求信息维护表).csv",
				CodeGenConsts.PROJCSVFILE_DIRROOT + "/Data/DUIEffectReqs(UI特效需求信息)_Effectreqinfo(特效需求信息维护表).csv" };
		List<String[]> colList = CSVUtil.getDataFromCSV2(csv1);
		for (int i = 0; i < csv2.length; i++) {
			String string = csv2[i];
			List<String[]> colListTmp = CSVUtil.getDataFromCSV2(string);
			for (int j = 4; j < colListTmp.size(); j++) {
				String[] strs = colListTmp.get(j);
				colList.add(strs);// 合并成一个表的数据再来处理
			}
		}
		String userColName = "effect_art_rtx";
		String timeColName = "req_end_day";
		Set<String> userSet = new HashSet<String>();
		for (int i = 4; i < colList.size(); i++) {
			String[] vals = colList.get(i);
			String userid = getColValue(userColName, vals, colList);
			System.err.println("userid===" + userid);
			if (userid != null && userid.trim().length() > 0) {
				userSet.add(userid);
			}
		}
		for (String userId : userSet) {
			List<String[]> colList4User = getColListGroupByuaserAndTime(colList, userColName, timeColName, userId);
			Map<String, List<String[]>> dayMap = new HashMap<String, List<String[]>>();
			for (int i = 0; i < colList4User.size(); i++) {
				String[] vals = colList4User.get(i);
				String dayVal = getColValue(timeColName, vals, colList);
				List<String[]> list4Day = dayMap.get(dayVal);
				if (list4Day == null) {
					list4Day = new ArrayList<String[]>();
					dayMap.put(dayVal, list4Day);
				}
				list4Day.add(vals);
			}

			for (Map.Entry<String, List<String[]>> e : dayMap.entrySet()) {
				StringBuilder sb = new StringBuilder();
				String fileName = userId + "_" + e.getKey().replace('/', '-') + ".txt";
				for (int i = 0; i < e.getValue().size(); i++) {
					String[] vals = e.getValue().get(i);
					String preabPath = "Assets/Resources/forClient/Effects/"
							+ getColValue("effect_module_name", vals, colList) + "/"
							+ getColValue("effect_feature_name", vals, colList) + "/"
							+ getColValue("effect_topic_name", vals, colList) + "/"
							+ getColValue("effect_prefab_name", vals, colList) + ".prefab";
					sb.append("特效名称：").append(getColValue("effect_title", vals, colList)).append("\n");
					sb.append("特效需求描述：").append(getColValue("effect_desc", vals, colList)).append("\n");
					sb.append("需求负责人：").append(getColValue("req_rtx", vals, colList)).append("\n");
					sb.append("特效制作负责人：").append(getColValue("effect_art_rtx", vals, colList)).append("\n");
					sb.append("特效制作工期开始时间：").append(getColValue("req_start_day", vals, colList)).append("\n");
					sb.append("特效验收时间：").append(getColValue("req_end_day", vals, colList)).append("\n");
					sb.append("是否循环特效(0不循环,1循环)：").append(getColValue("need_loop", vals, colList)).append("\n");
					sb.append("特效播放时长：").append(getColValue("effect_play_time", vals, colList)).append("秒\n");
					sb.append("特效范围类型：").append(getColValue("effect_fanwei_type", vals, colList)).append("\n");
					sb.append("是否飞行特效(0不是,1是)：").append(getColValue("is_fly", vals, colList)).append("\n");
					sb.append("特效预制名称：").append(getColValue("effect_prefab_name", vals, colList)).append("\n");
					sb.append("特效预制文件路径：").append(preabPath).append("\n\n");
				}
				File effectRedRootDir = new File(CodeGenConsts.PROJEFFECTREQFILE_DIRROOT);

				// System.out.println(fileName);
				// System.out.println(sb);
				GameCSV2DB.writeFile(new File(new File(effectRedRootDir, userId), fileName).getAbsolutePath(),
						sb.toString());
			}
		}
	}

	/**
	 * 按人按时间分组得到归类后的需求文档,按人可以按提出者和特效制作负责人来分，按时间可以按天为纬度生成文件
	 * 
	 * @param colList
	 * @param userColName
	 * @param timeColname
	 * @return
	 */
	private static List<String[]> getColListGroupByuaserAndTime(List<String[]> colList, String userColName,
			String timeColName, String userColValue) {
		System.err.println("getColListGroupByuaserAndTime|" + userColName + "|" + timeColName + "|" + userColValue);
		List<String[]> list = new ArrayList<String[]>();
		for (int i = 0; colList != null && i < colList.size(); i++) {
			String[] strs = colList.get(i);
			if (userColValue.equalsIgnoreCase(getColValue(userColName, strs, colList))) {
				list.add(strs);
			}
		}
		return list;
	}

	public static String skillStatusinfoTableName = "TSkill_Statusinfo";

	public static void genSkillStatusEnumFile(File csvFile, String tableName, List<String[]> colList) {
		if (skillStatusinfoTableName.equalsIgnoreCase(tableName)) {// 生成模块定义的枚举

			String protoFileName = "SkillStatus.cs";
			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");

			sb.append("namespace net.bilinkeji.gecaoshoulie.skills\n");
			sb.append("{\n");
			sb.append("	public enum SkillStatus\n");
			sb.append("	{\n");
			boolean isFirst = false;

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					if (!isFirst) {
						isFirst = true;
					} else {
						sb.append(",\n");
					}
					sb.append("		/// <summary>\n");
					sb.append("		/// " + CSVUtil.getColValue("status_title", s, colList) + " 状态.\n");
					sb.append("		/// </summary>\n");
					sb.append("		SS_" + CSVUtil.getColValue("status_name", s, colList).toUpperCase());

				}
			}
			sb.append("	}\n");
			sb.append("}\n");

			String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
					+ "/net/bilinkeji/gecaoshoulie/skills/";
			GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}
	}

	public static void genSkillModelActionEnumFile(File csvFile, String tableName, List<String[]> colList) {
		if ("TModel_Actiondescinfo".equalsIgnoreCase(tableName)) {// 生成动作枚举字符串

			String protoFileName = "SkillModelAction.cs";
			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");

			sb.append("namespace net.bilinkeji.gecaoshoulie.skills\n");
			sb.append("{\n");
			sb.append("	public enum SkillModelAction\n");
			sb.append("	{\n");
			boolean isFirst = false;

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					if (!isFirst) {
						isFirst = true;
					} else {
						sb.append(",\n");
					}
					sb.append("		/// <summary>\n");
					sb.append("		/// " + CSVUtil.getColValue("action_title", s, colList) + " 动作.\n");
					sb.append("		/// </summary>\n");
					sb.append("		" + CSVUtil.getColValue("action_name", s, colList).toLowerCase());

				}
			}
			sb.append("	}\n");
			sb.append("}\n");

			String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
					+ "/net/bilinkeji/gecaoshoulie/skills/";
			GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}

		if ("TModel_Actiondescinfo".equalsIgnoreCase(tableName)) {// 生成动作枚举字符串

			String protoFileName = "SkillModelActionUtil.cs";
			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("\n");
			sb.append("namespace net.bilinkeji.gecaoshoulie.skills\n");
			sb.append("{\n");
			sb.append("	public class SkillModelActionUtil\n");
			sb.append("	{\n");
			sb.append("\n");

			sb.append("		public static SkillModelAction[] allSkillModelActions = new SkillModelAction[] {\n");
			{
				boolean isFirst = false;
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					{
						if (!isFirst) {
							isFirst = true;
						} else {
							sb.append(",\n");
						}
						sb.append("			SkillModelAction."
								+ CSVUtil.getColValue("action_name", s, colList).toLowerCase());
					}
				}
			}
			sb.append("		};\n");
			sb.append("\n");

			sb.append("		public static string[] allSkillModelActionNames = new string[] {\n");
			{
				boolean isFirst = false;
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					{
						if (!isFirst) {
							isFirst = true;
						} else {
							sb.append(",\n");
						}
						sb.append("			\"" + CSVUtil.getColValue("action_name", s, colList).toLowerCase() + "\"");
					}
				}
			}
			sb.append("		};\n");
			sb.append("\n");
			sb.append("		public static string getSkillModelActionName (SkillModelAction sma)\n");
			sb.append("		{\n");
			sb.append("			string str = \"\";\n");
			sb.append("			switch (sma) {\n");
			{
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					{

						sb.append("			case SkillModelAction."
								+ CSVUtil.getColValue("action_name", s, colList).toLowerCase() + ":\n");
						sb.append("				{\n");
						sb.append("					str = \""
								+ CSVUtil.getColValue("action_name", s, colList).toLowerCase() + "\";\n");
						sb.append("					break;\n");
						sb.append("				}\n");
					}
				}
			}
			sb.append("			default:\n");
			sb.append("				{\n");
			sb.append("					str = sma.ToString ();\n");
			sb.append("					break;\n");
			sb.append("				}\n");
			sb.append("			}\n");
			sb.append("			return str;\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("		public static SkillModelAction getSkillModelAction (string skillModelActionName)\n");
			sb.append("		{\n");
			sb.append("			SkillModelAction sma = SkillModelAction.idle;\n");
			sb.append("			switch (skillModelActionName) {\n");
			{
				for (int i = 4; i < colList.size(); i++) {
					String[] s = colList.get(i);
					{

						sb.append("			case \"" + CSVUtil.getColValue("action_name", s, colList).toLowerCase()
								+ "\":\n");
						sb.append("				{\n");
						sb.append("					sma = SkillModelAction."
								+ CSVUtil.getColValue("action_name", s, colList).toLowerCase() + ";\n");
						sb.append("					break;\n");
						sb.append("				}\n");
					}
				}
			}

			sb.append("			default:\n");
			sb.append("				{\n");
			sb.append("					break;\n");
			sb.append("				}\n");
			sb.append("			}\n");
			sb.append("			return sma;\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("	}\n");
			sb.append("}\n");

			String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
					+ "/net/bilinkeji/gecaoshoulie/skills/";
			GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}

	}

	public static String skillStatusconditionTableName = "TSkill_Statuscondition";

	public static void genSkillStatusUtilFile(File csvFile, String tableName, List<String[]> colListTmp) {
		genSkillStateMachineActionFile(csvFile, tableName, colListTmp);
		genAbstractSkillStateMachineActionFile(csvFile, tableName, colListTmp);
		if (skillStatusconditionTableName.equalsIgnoreCase(tableName)) {// 生成模块定义的枚举

			String protoFileName = "SkillStatusUtil.cs";
			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("\n");
			sb.append("using System.Collections.Generic;\n");

			sb.append("namespace net.bilinkeji.gecaoshoulie.skills\n");
			sb.append("{\n");
			sb.append("	public class SkillStatusUtil\n");
			sb.append("	{\n");
			sb.append("		private const string STATUS_SPLIT_STR = \"|2|\";\n");
			sb.append("		private static HashSet<string> skillStatusForwardSet = new  HashSet<string> ();\n");
			sb.append(
					"		private static Dictionary<SkillStatus, HashSet<SkillStatus>> skillStatusForwardMap = new Dictionary<SkillStatus, HashSet<SkillStatus>> (new SkillStatusEnumComparer ());\n");
			sb.append("\n");
			sb.append("		static SkillStatusUtil ()\n");
			sb.append("		{\n");
			sb.append("			initSkillStatusForwardSet ();\n");
			sb.append("			initSkillStatusForwardMap ();\n");
			sb.append("		}\n");

			sb.append("		private static void initSkillStatusForwardSet ()\n");
			sb.append("		{\n");
			sb.append("		skillStatusForwardSet.Clear ();\n");

			String skillStatuscsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
					+ "/Public/Common/TSkill(技能配置)_Statusinfo(技能状态表).csv";
			List<String[]> skillStatusColList = CSVUtil.getDataFromCSV2(skillStatuscsvPath);
			List<String[]> colList = addDeadSkillStatus(colListTmp, skillStatusColList);
			// boolean isFirst = false;

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String fromTitle = getColValue("status_name_from", s, colList);
					String toTitle = getColValue("status_name_to", s, colList);
					// getColDatabyColValue
					String from = getColDatabyColValue("status_title", fromTitle, "status_name", skillStatusColList);
					String to = getColDatabyColValue("status_title", toTitle, "status_name", skillStatusColList);

					System.err.println("hhhhhhhhhhhhhhhhhhhhhhhh " + from + "=" + to + "|" + fromTitle + "=" + toTitle);
					if (from.length() > 0 && to.length() > 0) {
						sb.append("skillStatusForwardSet.Add (SkillStatus.SS_" + from.toUpperCase()
								+ " + STATUS_SPLIT_STR + SkillStatus.SS_" + to.toUpperCase() + ");\n");

					} else {
						System.err.println("hhhhhhhhhhhhhhhhhhhhhhhh " + from + "=" + to);
					}

				}
			}
			sb.append("}\n");

			sb.append("		private static void initSkillStatusForwardMap ()\n");
			sb.append("		{\n");
			sb.append("			skillStatusForwardMap.Clear ();\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String fromTitle = getColValue("status_name_from", s, colList);
					String toTitle = getColValue("status_name_to", s, colList);
					// getColDatabyColValue
					String from = getColDatabyColValue("status_title", fromTitle, "status_name", skillStatusColList);
					String to = getColDatabyColValue("status_title", toTitle, "status_name", skillStatusColList);
					if (from.length() > 0 && to.length() > 0) {
						sb.append("			addStatus4ForwardMap (SkillStatus.SS_" + from.toUpperCase()
								+ ", SkillStatus.SS_" + to.toUpperCase() + ");\n");
					}

				}
			}

			sb.append("		}\n");
			sb.append("\n");
			sb.append(
					"		public static void bindStateAction (SkillStateMachine ssm, SkillStateMachineAction actionProxy)\n");
			sb.append("		{\n");
			sb.append("			if (ssm != null && actionProxy != null) {\n");
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String fromTitle = getColValue("status_name_from", s, colList);
					String toTitle = getColValue("status_name_to", s, colList);
					// getColDatabyColValue
					String from = getColDatabyColValue("status_title", fromTitle, "status_name", skillStatusColList);
					String to = getColDatabyColValue("status_title", toTitle, "status_name", skillStatusColList);
					if (from.length() > 0 && to.length() > 0) {
						sb.append("			addStatus4ForwardMap (SkillStatus.SS_" + from.toUpperCase()
								+ ", SkillStatus.SS_" + to.toUpperCase() + ");\n");
						sb.append("				ssm.setOnExitAction (SkillStatus.SS_" + from.toUpperCase()
								+ ", SkillStatus.SS_" + to.toUpperCase() + ", actionProxy.onExit4" + capFirst(from)
								+ "2" + capFirst(to) + "State);\n");

						sb.append("				ssm.setOnEnterAction (SkillStatus.SS_" + from.toUpperCase()
								+ ", SkillStatus.SS_" + to.toUpperCase() + ", actionProxy.onEnter4" + capFirst(to)
								+ "From" + capFirst(from) + "State);\n");

						sb.append("				ssm.setCheckStateAction (SkillStatus.SS_" + from.toUpperCase()
								+ ", SkillStatus.SS_" + to.toUpperCase() + ", actionProxy.needForward" + capFirst(from)
								+ "2" + capFirst(to) + "State);\n");
					}

				}
			}

			sb.append("			}\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("		private static void addStatus4ForwardMap (SkillStatus from, SkillStatus to)\n");
			sb.append("		{\n");
			sb.append("			if (!skillStatusForwardMap.ContainsKey (from)) {\n");
			sb.append("				skillStatusForwardMap.Add (from, new HashSet<SkillStatus> ());\n");
			sb.append("			} \n");
			sb.append("			skillStatusForwardMap [from].Add (to);\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("		public static bool canForward (SkillStatus from, SkillStatus to)\n");
			sb.append("		{\n");
			// sb.append(" return skillStatusForwardSet.Contains (from +
			// STATUS_SPLIT_STR + to);\n");
			sb.append("			if(skillStatusForwardMap.ContainsKey(from)){\n");
			sb.append("				return skillStatusForwardMap [from].Contains (to);\n");
			sb.append("			}\n");
			sb.append("			return false;\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("		public static bool tryForwardState (SkillStateMachine ssm)\n");
			sb.append("		{\n");
			sb.append("			HashSet<SkillStatus> hs = null;\n");
			sb.append("			if (skillStatusForwardMap.TryGetValue (ssm.CurrentState, out hs)) {\n");
			sb.append("				SkillStatus[] ssArr = new SkillStatus[hs.Count];\n");
			sb.append("				hs.CopyTo (ssArr);\n");
			sb.append("				for (int i = 0; i < ssArr.Length; i++) {\n");
			sb.append("					if (ssm.needFroward (ssArr [i])) {\n");
			sb.append("						ssm.changeState (ssArr [i]);\n");
			sb.append("						return true;\n");
			// sb.append(" }\n");

			sb.append("					} else {\n");
			sb.append(
					"						BLDebug.LogError (\"tryForwardState|\"+ssm.CurrentState+\"|to|\"+ssArr[i]+\"|notneed!!!!!!\");\n");
			sb.append("					}\n");
			sb.append("				}\n");
			sb.append("			}\n");
			sb.append("			return false;\n");
			sb.append("		}\n");
			sb.append("\n");
			sb.append("	}\n");
			sb.append("}\n");
			String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
					+ "/net/bilinkeji/gecaoshoulie/skills/";
			GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}
	}

	public static void genSkillStateMachineActionFile(File csvFile, String tableName, List<String[]> colListTmp) {
		if (skillStatusconditionTableName.equalsIgnoreCase(tableName)) {// 生成模块定义的枚举

			String protoFileName = "SkillStateMachineAction.cs";
			StringBuilder sb = new StringBuilder();

			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("\n");
			sb.append("namespace net.bilinkeji.gecaoshoulie.skills\n");
			sb.append("{\n");
			sb.append("	public interface SkillStateMachineAction\n");
			sb.append("	{\n");

			String skillStatuscsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
					+ "/Public/Common/TSkill(技能配置)_Statusinfo(技能状态表).csv";
			List<String[]> skillStatusColList = CSVUtil.getDataFromCSV2(skillStatuscsvPath);

			List<String[]> colList = addDeadSkillStatus(colListTmp, skillStatusColList);

			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String fromTitle = getColValue("status_name_from", s, colList);
					String toTitle = getColValue("status_name_to", s, colList);
					// getColDatabyColValue
					String from = getColDatabyColValue("status_title", fromTitle, "status_name", skillStatusColList);
					String to = getColDatabyColValue("status_title", toTitle, "status_name", skillStatusColList);
					if (from.length() > 0 && to.length() > 0) {
						from = capFirst(from);
						to = capFirst(to);
						sb.append("		void onEnter4" + to + "From" + from + "State ();\n");
						sb.append("\n");
						sb.append("		void onExit4" + from + "2" + to + "State ();\n");
						sb.append("\n");
						sb.append("		bool needForward" + from + "2" + to + "State ();\n");
						sb.append("\n");
					}

				}
			}
			sb.append("	}\n");
			sb.append("}\n");

			String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
					+ "/net/bilinkeji/gecaoshoulie/skills/";
			GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}
	}

	public static void genAbstractSkillStateMachineActionFile(File csvFile, String tableName,
			List<String[]> colListTmp) {
		if (skillStatusconditionTableName.equalsIgnoreCase(tableName)) {// 生成模块定义的枚举

			String protoFileName = "AbstractSkillStateMachineAction.cs";
			StringBuilder sb = new StringBuilder();
			sb.append("using UnityEngine;\n");
			sb.append("using System.Collections;\n");
			sb.append("\n");
			sb.append("namespace net.bilinkeji.gecaoshoulie.skills\n");
			sb.append("{\n");
			sb.append("	public class AbstractSkillStateMachineAction : SkillStateMachineAction\n");
			sb.append("	{\n");
			sb.append("\n");
			String skillStatuscsvPath = CodeGenConsts.PROJCSVFILE_DIRROOT
					+ "/Public/Common/TSkill(技能配置)_Statusinfo(技能状态表).csv";
			List<String[]> skillStatusColList = CSVUtil.getDataFromCSV2(skillStatuscsvPath);
			List<String[]> colList = addDeadSkillStatus(colListTmp, skillStatusColList);
			for (int i = 4; i < colList.size(); i++) {
				String[] s = colList.get(i);
				{
					String fromTitle = getColValue("status_name_from", s, colList);
					String toTitle = getColValue("status_name_to", s, colList);
					// getColDatabyColValue
					String from = getColDatabyColValue("status_title", fromTitle, "status_name", skillStatusColList);
					String to = getColDatabyColValue("status_title", toTitle, "status_name", skillStatusColList);
					if (from.length() > 0 && to.length() > 0) {
						from = capFirst(from);
						to = capFirst(to);
						sb.append("		public virtual void onEnter4" + to + "From" + from + "State ()\n");
						sb.append("		{\n");
						sb.append("			BLDebug.Log (\"run:\" + this + \".onEnter4" + to + "From" + from
								+ "State\");\n");
						sb.append("		}\n");
						sb.append("\n");
						sb.append("		public virtual void onExit4" + from + "2" + to + "State ()\n");
						sb.append("		{\n");
						sb.append("			BLDebug.Log (\"run:\" + this + \".onExit4" + from + "2" + to
								+ "State\");\n");
						sb.append("			\n");
						sb.append("		}\n");
						sb.append("\n");
						sb.append("		public virtual bool needForward" + from + "2" + to + "State ()\n");
						sb.append("		{\n");
						sb.append("			return true;\n");
						sb.append("		}\n");
					}

				}
			}
			sb.append("	}\n");
			sb.append("}\n");

			String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
					+ "/net/bilinkeji/gecaoshoulie/skills/";
			GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());
		}
	}

	/**
	 * 自动把非死亡状态到死亡状态的流转补进去
	 * 
	 * @param colListTmp
	 * @param skillStatusColList
	 * @return
	 */
	private static List<String[]> addDeadSkillStatus(List<String[]> colListTmp, List<String[]> skillStatusColList) {
		List<String[]> colList = new ArrayList<String[]>(colListTmp);
		// 在这里插入所有状态到死亡状态的流转
		if (colList.size() > 4) {
			for (int i = 4; i < skillStatusColList.size(); i++) {
				String[] s = skillStatusColList.get(i);
				{
					String status_name = getColValue("status_title", s, skillStatusColList);
					if (!status_name.equals("死亡")) {// 除了死亡的，都往里面加
						String[] newSTmp = colList.get(4);
						String[] newS = Arrays.copyOf(newSTmp, newSTmp.length);
						newS[1] = status_name;
						newS[2] = "死亡";
						newS[3] = "任意状态可跳转至死亡状态";
						colList.add(newS);
						// System.err.println("addDeadSkillStatus|" +
						// status_name + "|to|dead|" + Arrays.toString(newS));
					}
				}
			}
		}
		return colList;
	}

	public static void genTskillSkillinfoConvertUtilCSFile(List<String[]> colList4TskillSkillinfo4Proto,
			List<String[]> colList4TskillMainskillunitinfo4Proto) {
		if (colList4TskillSkillinfo4Proto == null || colList4TskillMainskillunitinfo4Proto == null) {
			return;
		}

		Set<String> aSet = new HashSet<String>();
		Set<String> bSet = new HashSet<String>();
		Set<String> allSet = new TreeSet<String>();
		String[] colNames4a = colList4TskillSkillinfo4Proto.get(0);
		String[] colNames4b = colList4TskillMainskillunitinfo4Proto.get(0);
		for (String aaa : colNames4a) {
			if (aaa != null && aaa.length() > 0) {
				aSet.add(aaa);

			}
		}
		for (String bbb : colNames4b) {
			if (bbb != null && bbb.length() > 0) {
				bSet.add(bbb);
			}
		}
		for (String aaa : aSet) {
			if (bSet.contains(aaa)) {
				allSet.add(aaa);
			}
		}

		String csFileName = "TskillSkillinfoConvertUtil.cs";
		StringBuilder sb = new StringBuilder();
		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.dbbeans4proto;\n");
		sb.append("\n");
		sb.append("namespace net.bilinkeji.gecaoshoulie.modules.pk\n");
		sb.append("{\n");
		sb.append("	public class TskillSkillinfoConvertUtil\n");
		sb.append("	{\n");
		sb.append(
				"		public static TskillSkillinfo4Proto toTskillSkillinfo4Proto (TskillMainskillunitinfo4Proto obj)\n");
		sb.append("		{\n");
		sb.append("			if (obj == null) {\n");
		sb.append("				return null;\n");
		sb.append("			}\n");
		sb.append("			TskillSkillinfo4Proto a = new TskillSkillinfo4Proto ();\n");
		sb.append("			a.id = obj.bigSkillUid;\n");
		for (String ccc : allSet) {
			sb.append("			a." + DBUtil.camelName(ccc.trim().toLowerCase()) + " = obj."
					+ DBUtil.camelName(ccc.trim().toLowerCase()) + ";\n");
		}
		sb.append("			return a;\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append(
				"		public static TskillMainskillunitinfo4Proto toTskillMainskillunitinfo4Proto (TskillSkillinfo4Proto obj)\n");
		sb.append("		{\n");
		sb.append("\n");
		sb.append("			if (obj == null) {\n");
		sb.append("				return null;\n");
		sb.append("			}\n");
		sb.append("			TskillMainskillunitinfo4Proto a = new TskillMainskillunitinfo4Proto ();\n");
		for (String ccc : allSet) {
			sb.append("			a." + DBUtil.camelName(ccc.trim().toLowerCase()) + " = obj."
					+ DBUtil.camelName(ccc.trim().toLowerCase()) + ";\n");
		}
		sb.append("			return a;\n");
		sb.append("		}\n");
		sb.append("	}\n");
		sb.append("}\n");

		String javaFileDir = CodeGenConsts.PROJPROTO_UnityMotionStateBehaviourSRCROOT
				+ "/net/bilinkeji/gecaoshoulie/modules/pk/";
		GameCSV2DB.writeFile(new File(javaFileDir, csFileName).getAbsolutePath(), sb.toString());

	}

	public static void genCsvCheckerClass(String csvPath) {
		File csvFile = new File(csvPath);
		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		if (tableName == null || tableName.length() < 1) {
			return;
		}
		String className = capFirst(DBUtil.camelName(tableName)) + "CsvChecker";
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());

		checkClassNameMap.put(tableName, className);
		StringBuilder sb = new StringBuilder();
		checkPackageNameSet.add(tablePrefix.toLowerCase());
		sb.append("package com.lizongbo.codegentool.configverification." + tablePrefix.toLowerCase() + ";\n");
		sb.append("\n");
		sb.append("import java.io.*;\n");
		sb.append("import java.util.*;\n");
		sb.append("\n");
		sb.append("import com.lizongbo.codegentool.configverification.*;\n");
		sb.append("import com.lizongbo.codegentool.csv2db.*;\n");
		sb.append("\n");
		sb.append("public class " + className + " implements CsvChecker {\n");
		sb.append("\n");
		sb.append("	@Override\n");
		sb.append("	public List<String> checkCsv(String csvPath) {\n");
		sb.append("		List<String> errorList = new ArrayList<String>();\n");
		sb.append("		File csvFile = new File(csvPath);\n");
		sb.append("		if (!csvFile.exists() || !csvFile.isFile()) {\n");
		sb.append("			errorList.add(\"File Error:\" + csvFile);\n");
		sb.append("			return errorList;\n");
		sb.append("		}\n");
		sb.append("		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());\n");
		sb.append("		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());\n");
		sb.append("		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());\n");
		sb.append("		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());\n");
		sb.append("\n");
		sb.append("		return errorList;\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("}\n");
		String fileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/src/com/lizongbo/codegentool/configverification/"
				+ tablePrefix.toLowerCase();
		String protoCsvHelperFileName = className + ".java";
		File protoFile = new File(fileDir, protoCsvHelperFileName);
		if (!protoFile.exists()) {
			GameCSV2DB.writeFile(protoFile.getAbsolutePath(), sb.toString());
		} else {
			// System.out.println(protoFile + " exists so ignore it");
		}
	}

	public static void genCsvCheckerManager() {
		StringBuilder sb = new StringBuilder();

		sb.append("package com.lizongbo.codegentool.configverification;\n");
		sb.append("\n");
		sb.append("import java.io.*;\n");
		sb.append("import java.util.*;\n");
		sb.append("import java.util.concurrent.ConcurrentHashMap;\n");
		sb.append("\n");
		for (String p : checkPackageNameSet) {
			sb.append("import com.lizongbo.codegentool.configverification." + p + ".*;\n");
		}
		sb.append("import com.lizongbo.codegentool.csv2db.*;\n");
		sb.append("\n");
		sb.append("public class CsvCheckerManager {\n");
		sb.append("\n");
		sb.append(
				"	private static ConcurrentHashMap<String, CsvChecker> checkerMap = new ConcurrentHashMap<String, CsvChecker>();\n");
		sb.append("\n");
		sb.append("	public static void initCheckerMap() {\n");
		for (Map.Entry<String, String> e : checkClassNameMap.entrySet()) {
			sb.append("		checkerMap.put(\"" + e.getKey() + "\", new " + e.getValue() + "());\n");
		}

		sb.append("\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public static Map<String, List<String>> errorMap = new HashMap<String, List<String>>();\n");
		sb.append("\n");
		sb.append("	public static void CheckCsv(String csvPath) {\n");
		sb.append("		File csvFile = new File(csvPath);\n");
		sb.append("		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());\n");
		sb.append("\n");
		sb.append("		CsvChecker cc = checkerMap.get(tableName);\n");

		sb.append("		System.err.println(tableName + \"|\" + cc);\n");
		sb.append("		if (cc != null) {\n");
		sb.append("			List<String> list = cc.checkCsv(csvPath);\n");
		sb.append("			if (list != null && list.size() > 0) {\n");
		sb.append("				errorMap.put(csvPath, list);\n");
		sb.append("			}\n");
		sb.append("		}\n");
		sb.append("\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public static void main(String[] args) {\n");
		sb.append("		// TODO Auto-generated method stub\n");
		sb.append("\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("}\n");

		String fileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_codegentool/WEB-INF/src/com/lizongbo/codegentool/configverification/";
		String protoCsvHelperFileName = "CsvCheckerManager.java";
		File protoFile = new File(fileDir, protoCsvHelperFileName);

		GameCSV2DB.writeFile(protoFile.getAbsolutePath(), sb.toString());

	}

	public static int toInt(String s) {
		return toInt(s, 0);
	}

	public static int toInt(String s, int def) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return def;
	}

	public static void appendSyncDataEnum(StringBuilder sb) {
		File csvFile = new File(
				CodeGenConsts.PROJCSVFILE_DIRROOT + "Public/World/TProto(前后台数据协议)_Syncdata(数据变更通知).csv");

		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		sb.append("//帧同步数据的枚举定义\n");
		sb.append("enum FrameSyncDataType{\n");
		sb.append("\tCOMMON_UNKNOW_SYNCTYPE = 0 ;\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "FRAME_SYNC_" + s[1].toUpperCase();
			sb.append("\t").append(cmdEnum).append(" = ").append(s[0])
					.append(";//" + s[2].replace('\n', ' ').replace('\r', ' ') + "\n");
			String dnClassName = CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()) + "Data");
			// 一起默认输出到一个指定的proto文件里

			String pp = new File(CodeGenConsts.PROJPROTOFILE_DIRROOT + "/Common/CommonFrameSync4Proto.proto")
					.getAbsolutePath();
			String CommonFrameSync4ProtoTxt = GenAll.readFile(pp, "UTF-8");
			if (!CommonFrameSync4ProtoTxt.contains(dnClassName)) {// 如果是新类，就插进去
				CommonFrameSync4ProtoTxt = CommonFrameSync4ProtoTxt + "\n//"
						+ s[2].replace('\n', ' ').replace('\r', ' ') + " 的数据信息,对应 " + cmdEnum;
				CommonFrameSync4ProtoTxt = CommonFrameSync4ProtoTxt + "\nmessage " + dnClassName + "{\n";
				CommonFrameSync4ProtoTxt = CommonFrameSync4ProtoTxt + "\n}\n";
				GameCSV2DB.writeFile(pp, CommonFrameSync4ProtoTxt, "UTF-8");
			}

		}
		sb.append("}").append("\n");
		// 定义固定的类
		// 需要同步的机甲信息
		sb.append("message SyncMechaInfo{\n");
		sb.append("	optional int32 zoneId = 1 [default = 0]; \n");
		sb.append("	optional int32 playerId = 2 [default = 0]; \n");
		sb.append("	optional int32 mechaId = 3 [default = 0]; \n");
		sb.append("	//optional int32 sceneId = 4 [default = 0]; //1.主场景，2,工会场景,3及其它对应战斗场景id \n");
		sb.append("	//optional int32 uid = 5 [default = 0]; //随机唯一id，只要同一玩家自身是不重复的即可 \n");
		sb.append("}\n");
		sb.append("//通知客户端做数据变更的具体数据信息\n");
		sb.append("message FrameSyncData{\n");
		sb.append("	optional SyncMechaInfo  syncObj = 4 ;//帧同步数据对象信息\n");
		sb.append("	optional FrameSyncDataType  frameSyncDataType= 1 ;//帧同步数据类型\n");
		sb.append("	optional bytes frameSyncBytes= 2 ; //具体同步对象的pb字节数组\n");
		sb.append(" optional string  jsonDesc= 3 ; //debug模式下把变更数据用json存在这里，便于统一的toJson查看\n");
		sb.append(" //optional int32 unityFrameCount = 5 [default = 0];//记录一下当时客户端Unity运行的帧数\n");
		sb.append(" //optional int32 unityRenderedFrameCount = 6 [default = 0];//记录一下当时客户端Unity渲染的帧数\n");
		sb.append(" //optional float unityRealtimeSinceStartup = 7 [default = 0];//记录一下当时的时间\n");
		sb.append("	//optional int32 cmdSeq = 8 [default = 0];//本地生成操作时的递增序号\n");
		sb.append("}\n");
		sb.append("\n");
		sb.append("//通知客户端做数据变更的具体信息数组\n");
		sb.append("message FrameSyncDataArray{\n");
		// sb.append(" optional int64 deltaTime= 7 ;
		// //距离上一帧的时间差值，精确到毫秒,客户端帧间时差以这个为准来运算，废弃该字段\n");
		sb.append("	optional float deltaTimeFloat= 15 ; //距离上一帧的时间差值，以秒为单位,客户端帧间时差以这个为准来运算\n");
		sb.append("	optional int64 totalTime = 8 [default = 0]; //战斗持续的总时间，单位毫秒\n");
		sb.append("	optional int32 randomSeed = 9 [default = 0];//同步随机数种子\n");
		sb.append("	optional SyncMechaInfo syncObj = 5; //客户端上报的时候填这里，可以不填同步数据信息内的，节省网络带宽\n");
		sb.append("	optional int32 pkSessionId= 3 [default = 0]; //战斗sessionId\n");
		sb.append("	optional int32 frameIndex= 2 [default = 0]; //战斗服务器同步的服务器帧id，客户端上报时则表示是客户端收到过的服务器最近一次帧id\n");
		sb.append("	optional int32 clientSeq= 4 [default = 0]; //客户端上报专用的本地帧序号，用于服务器过滤重复帧或旧帧\n");
		sb.append("	repeated FrameSyncData  syncs= 1 ;//0到多个同步数据信息\n");
		sb.append(
				"	repeated StringStringKeyValue playerAI = 13;//key：掉线转AI的玩家playerId@zoneId;value：负责跑该AI的玩家playerId@zoneId\n");
		sb.append(
				"	repeated IntStringKeyValue npcAI = 14;//key:需要跑ai的小怪id除以5得到的余数，即01234;value:负责跑这些小怪AI的玩家玩家playerId@zoneId\n");

		sb.append("	repeated int32  playerAIArray= 6 [packed = true];//用整数数组精简表示的玩家ai映射关系\n");
		sb.append("	repeated int32  npcAIArray= 7 [packed = true];//用整数数组精简表示的npc ai映射关系\n");
		
		sb.append("}\n");
		sb.append("\n");
		sb.append("message IntStringKeyValue{\n");
		sb.append("	required int32 key = 1 [default = 0]; //键值对的整数Key \n");
		sb.append("	required string value = 2 [default = \"\"]; //键值对的字符串Value \n");
		sb.append("}\n");
		sb.append("\n");
		sb.append("message StringStringKeyValue{\n");
		sb.append("	required string key = 1 [default = \"\"]; //键值对的字符串Key \n");
		sb.append("	required string value = 2 [default = \"\"]; //键值对的字符串Value \n");
		sb.append("}\n");
		sb.append("\n");
		genClientFrameSyncEventsCsFile(colList);
		genServerFrameSyncUtilJavaFile(colList);
	}

	private static void genServerFrameSyncUtilJavaFile(List<String[]> colList) {
		StringBuilder sb = new StringBuilder();

		sb.append("package net.bilinkeji.gecaoshoulie.mgameprotorpc.serverutils;\n");
		sb.append("\n");
		sb.append("import com.google.protobuf.MessageOrBuilder;\n");
		sb.append("\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.CommonFrameSync4Proto.*;\n");
		sb.append("import net.bilinkeji.gecaoshoulie.mgameproto.common.TProtoCmd.*;\n");
		sb.append("import net.bilinkeji.utils.StringUtil;\n");
		sb.append("\n");
		sb.append("public class FrameSyncDataUtil {\n");
		sb.append("\n");
		sb.append("	public static FrameSyncData getFrameSyncData(MessageOrBuilder dataChange) {\n");
		sb.append("\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "FRAME_SYNC_" + s[1].toUpperCase();
			String dnClassName = CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase())) + "Data";

			sb.append("\n");

			sb.append("		if (dataChange instanceof " + dnClassName + ") {\n");
			sb.append("			" + dnClassName + " event = (" + dnClassName + ") dataChange;\n");
			sb.append("			FrameSyncData dnd = FrameSyncData.newBuilder()\n");
			sb.append("					.setFrameSyncDataType(FrameSyncDataType." + cmdEnum + ")\n");
			sb.append("					.setFrameSyncBytes(event.toByteString()).build();\n");
			sb.append("			return dnd;\n");
			sb.append("		}\n");
			sb.append("		if (dataChange instanceof " + dnClassName + ".Builder) {\n");
			sb.append("			" + dnClassName + ".Builder builder = (" + dnClassName + ".Builder) dataChange;\n");
			sb.append("			FrameSyncData dnd = FrameSyncData.newBuilder()\n");
			sb.append("					.setFrameSyncDataType(FrameSyncDataType." + cmdEnum + ")\n");
			sb.append("					.setFrameSyncBytes(builder.build().toByteString())\n");
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
		String protoFileName = "FrameSyncDataUtil.java";
		String javaFileDir = CodeGenConsts.PROJSERVER_JAVASRCROOT + "/../basesrc/"
				+ "/net.bilinkeji.gecaoshoulie.mgameprotorpc.serverutils/".replace('.', '/');
		GameCSV2DB.writeFile(new File(javaFileDir, protoFileName).getAbsolutePath(), sb.toString());

	}

	private static void genClientFrameSyncEventsCsFile(List<String[]> colList) {
		StringBuilder sb = new StringBuilder();

		sb.append("using net.bilinkeji.gecaoshoulie.mgameproto.common;\n");
		sb.append("using net.bilinkeji.common.util;\n");
		sb.append("using ProtoBuf;\n");
		sb.append("\n");

		sb.append("/// <summary>\n");
		sb.append("/// 由代码生成的数据变更通知的事件名称常量，来自TProto(前后台数据协议)_Syncdata(数据变更通知).csv.\n");
		sb.append("///  by quickli\n");
		sb.append("/// </summary>\n");

		sb.append("public static class FrameSyncEvents\n");
		sb.append("{\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "FRAME_SYNC_" + s[1].toUpperCase();
			String dnClassName = CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()) + "Data");

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
		sb.append("	public static IExtensible GetFrameSyncDataObject (FrameSyncData dnd)\n");
		sb.append("	{\n");
		sb.append("		if (dnd == null || dnd.frameSyncBytes == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		sb.append("		switch (dnd.frameSyncDataType) {\n");

		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "FRAME_SYNC_" + s[1].toUpperCase();
			String dnClassName = CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()) + "Data");
			sb.append("		case FrameSyncDataType." + cmdEnum + ":\n");
			// sb.append(" return ProtoBufUtil.ProtoBufFromBytes<" + dnClassName
			// + "> (dnd.frameSyncBytes);\n");
			sb.append("			return " + dnClassName + ".Deserialize (dnd.frameSyncBytes);\n");
		}
		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");
		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");

		sb.append("\n");
		sb.append("	/// <summary>\n");
		sb.append("	/// 根据需要同步的操作，生成打包的统一数据结构\n");
		sb.append("	/// </summary>\n");
		sb.append("	/// <returns>统一的数据结构.</returns>\n");
		sb.append("	/// <param name=\"dnd\">各种需要同步的操作类型.</param>\n");
		sb.append("	public static FrameSyncData GetFrameSyncData (IExtensible dnd)\n");
		sb.append("	{\n");
		sb.append("		if (dnd == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "FRAME_SYNC_" + s[1].toUpperCase();
			String dnClassName = CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()) + "Data");
			sb.append("		if (dnd is " + dnClassName + ") {\n");
			sb.append("			" + dnClassName + " data = dnd as " + dnClassName + ";\n");
			sb.append("			FrameSyncData fsd = new FrameSyncData ();\n");
			sb.append("			fsd.frameSyncDataType = FrameSyncDataType." + cmdEnum + ";\n");
			// sb.append(" fsd.frameSyncBytes = ProtoBufUtil.ProtoBufToBytes<" +
			// dnClassName + "> (data);\n");
			sb.append("			fsd.frameSyncBytes = " + dnClassName + ".SerializeToBytes (data);\n");
			sb.append("			return fsd;\n");
			sb.append("		}\n");
		}

		sb.append("		return null;\n");
		sb.append("	}\n");
		sb.append("\n");

		sb.append("	/// <summary>\n");
		sb.append("	/// 获取数据变化信息的事件通知名称\n");
		sb.append("	/// </summary>\n");
		sb.append("	/// <returns>The data notify event name.</returns>\n");
		sb.append("	/// <param name=\"dnd\">Dnd.</param>\n");
		sb.append("	public static string GetFrameSyncEventName (FrameSyncData dnd)\n");
		sb.append("	{\n");
		sb.append("		if (dnd == null || dnd.frameSyncBytes == null) {\n");
		sb.append("			return null;\n");
		sb.append("		}\n");
		sb.append("		switch (dnd.frameSyncDataType) {\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "FRAME_SYNC_" + s[1].toUpperCase();
			String dnClassName = CSVUtil.capFirst(DBUtil.camelName(cmdEnum.toLowerCase()) + "Data");
			sb.append("		case FrameSyncDataType." + cmdEnum + ":\n");
			sb.append("			return FrameSyncEvents.On" + dnClassName + ";\n");
		}
		sb.append("		default:\n");
		sb.append("			break;\n");
		sb.append("		}\n");
		sb.append("		return null;\n");
		sb.append("	}\n");

		sb.append("\n");
		sb.append("}\n");

		String pp = new File(CodeGenConsts.PROJUI_HANDLER_SRCROOT + "/../Bilinkeji/Common/FrameSyncEvents.cs")
				.getAbsolutePath();
		GameCSV2DB.writeFile(pp, sb.toString());
	}

	public static void appendPlayerActivityEnum(StringBuilder sb) {
		File csvFile = new File(
				CodeGenConsts.PROJCSVFILE_DIRROOT + "Public/Common/TComm(基础配置)_Playeractivity(玩家活跃度).csv");

		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		sb.append("//玩家每日活跃度的枚举定义\n");
		sb.append("enum PlayerActivityType{\n");
		sb.append("\tCOMMON_UNKNOW_ACTIVITY_TYPE = 0 ;\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "ACTIVITY_" + CSVUtil.getColValue("activity_enum", s, colList) + "_count";
			cmdEnum = cmdEnum.toUpperCase();
			sb.append("\t").append(cmdEnum).append(" = ").append(CSVUtil.getColValue("activity_id", s, colList))
					.append(";//"
							+ CSVUtil.getColValue("activity_name", s, colList).replace('\n', ' ').replace('\r', ' ')
							+ ", "
							+ CSVUtil.getColValue("activity_desc", s, colList).replace('\n', ' ').replace('\r', ' ')
							+ "\n");
		}
		sb.append("}").append("\n");
	}

	public static void appendMoneyTypeEnum(StringBuilder sb) {
		File csvFile = new File(
				CodeGenConsts.PROJCSVFILE_DIRROOT + "Public/Common/TComm(基础配置)_Moneyenumeration(货币枚举).csv");

		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		sb.append("//游戏内的货币类型枚举\n");
		sb.append("enum MoneyType{\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String cmdEnum = "MONEY_" + CSVUtil.getColValue("money_enum", s, colList);
			cmdEnum = cmdEnum.toUpperCase();
			sb.append("\t").append(cmdEnum).append(" = ").append(CSVUtil.getColValue("money_id", s, colList))
					.append(";//" + CSVUtil.getColValue("money_name", s, colList).replace('\n', ' ').replace('\r', ' ')
							+ ", " + CSVUtil.getColValue("money_desc", s, colList).replace('\n', ' ').replace('\r', ' ')
							+ "\n");
		}
		sb.append("}").append("\n");
	}

	public static void genClientUIJumpEnableFile() {
		File csvFile = new File(CodeGenConsts.PROJCSVFILE_DIRROOT + "/Client/Tui(UI配置)_Windowinfo(ui窗口配置信息).csv");
		if (!csvFile.exists()) {
			GameCSV2DB.addErrMailMsgList("Error:notfound=" + csvFile);
			GameCSV2DB.sendMailAndExit();
		}

		String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
		String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
		StringBuilder sb = new StringBuilder();

		sb.append("using UnityEngine;\n");
		sb.append("using System.Collections;\n");
		sb.append("using System.Collections.Generic;\n");
		sb.append("\n");
		sb.append("namespace Bilinkeji.UI\n");
		sb.append("{\n");
		sb.append("	/// <summary>\n");
		sb.append("	/// 界面跳转基类\n");
		sb.append("	/// </summary>\n");
		sb.append("	public abstract class UIJumpEnable\n");
		sb.append("	{\n");
		sb.append("		public delegate UIWindow ShowUIAction (string[]  paramArr);\n");
		sb.append("\n");
		sb.append(
				"		protected Dictionary<string,ShowUIAction> m_ShowUIActionMap = new Dictionary<string, ShowUIAction> ();\n");
		sb.append("\n");
		sb.append("       protected UIJumpEnable()\n");
		sb.append("        {\n");
		sb.append("           initShowUIActionMap();\n");
		sb.append("        }\n");

		sb.append("        /// <summary>\n");
		sb.append("\n");
		sb.append("        /// 转换成Dictionary格式\n");
		sb.append("\n");
		sb.append("        /// </summary>\n");
		sb.append("\n");
		sb.append("        /// <param name=\"jumpParams\"></param>\n");
		sb.append("\n");
		sb.append("        /// <returns></returns>\n");
		sb.append("\n");
		sb.append("        protected Dictionary<string, object> GetKeyValueDict(string[] jumpParams)\n");
		sb.append("\n");
		sb.append("        {\n");
		sb.append("\n");
		sb.append("            var dict = new Dictionary<string, object>();\n");
		sb.append("\n");
		sb.append("            for (var i = 0; i < jumpParams.Length; i++)\n");
		sb.append("\n");
		sb.append("            {\n");
		sb.append("\n");
		sb.append(
				"                // here become an array of such as [\"level\",\"3\"] then next string to be [\"tip\",\"附加提示文本\"]\n");
		sb.append("\n");
		sb.append("                // so the odd number is key, the even number is value\n");
		sb.append("\n");
		sb.append("                var keyValue = jumpParams[i].Split('=');\n");
		sb.append("\n");
		sb.append("                dict[keyValue[0]] = keyValue[1];\n");
		sb.append("\n");
		sb.append("            }\n");
		sb.append("\n");
		sb.append("            return dict;\n");
		sb.append("\n");
		sb.append("        }\n");
		sb.append("\n");

		/*
		 * sb.
		 * append("		public bool ShowUI (string uiJumpName, params object[] paramArr)\n"
		 * ); sb.append("		{\n");
		 * sb.append("			if (string.IsNullOrEmpty (uiJumpName)) {\n");
		 * sb.
		 * append("				Debug.LogError (\"showUI|but|none|uiJumpName=\" + uiJumpName);\n"
		 * ); sb.append("				return false;\n");
		 * sb.append("			}\n");
		 * sb.append("			ShowUIAction action = null;\n"); sb.
		 * append("			if (m_ShowUIActionMap.TryGetValue (uiJumpName, out action)) {\n"
		 * ); sb.append("				return action (paramArr);\n");
		 * sb.append("			}\n"); sb.
		 * append("			Debug.LogError (\"showUI|but|noAction|uiJumpName=\" + uiJumpName);\n"
		 * ); sb.append("			return false;		\n");
		 * sb.append("		}\n");
		 * 
		 */
		sb.append("\n");
		sb.append("		protected void initShowUIActionMap ()\n");
		sb.append("		{	\n");
		sb.append("			m_ShowUIActionMap.Clear ();\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String windowName = CSVUtil.getColValue("window_name", s, colList);
			String windowClass = CSVUtil.getColValue("full_class_name", s, colList);
			String jumpName = CSVUtil.getColValue("jump_name", s, colList);
			if (jumpName == null || jumpName.trim().length() < 1) {
				continue;
			}
			sb.append("			m_ShowUIActionMap [\"" + jumpName + "\"] = ShowUI4" + windowName + ";\n");
			sb.append("			m_ShowUIActionMap [\"" + windowName + "\"] = ShowUI4" + windowName + ";\n");
		}

		sb.append("		}\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		for (int i = 4; i < colList.size(); i++) {
			String[] s = colList.get(i);
			String windowName = CSVUtil.getColValue("window_name", s, colList);
			String windowClass = CSVUtil.getColValue("full_class_name", s, colList);
			String jumpName = CSVUtil.getColValue("jump_name", s, colList);
			if (jumpName == null || jumpName.trim().length() < 1) {
				continue;
			}
			sb.append("		protected virtual UIWindow ShowUI4" + windowName + " (string[] paramArr)\n");
			sb.append("		{\n");

			sb.append("            var nextWin = BilinWinManager.GetWindow<" + windowClass + ">();\n");
			sb.append("            var dict = GetKeyValueDict(paramArr);\n");
			sb.append("            nextWin.JumpTo(dict);\n");
			sb.append("            return nextWin;\n");
			// sb.append(" return null;\n");
			// sb.append(" WindowManager.Instance.GetWindow<" + windowClass + ">
			// ().Open ();\n");
			// sb.append(" return false; \n");
			sb.append("		}\n");
		}
		sb.append("\n");
		sb.append("	}\n");
		sb.append("}\n");

		// TODO 此处也要加配置
		String pp = new File(CodeGenConsts.PROJUI_HANDLER_SRCROOT + "/../Bilinkeji/Util/UIJumpEnable.cs")
				.getAbsolutePath();
		GameCSV2DB.writeFile(pp, sb.toString());
	}

	public static Map<Integer, Map<String, Object>> getDataCacheMap(List<String[]> colList) {
		// TODO Auto-generated method stub
		String[] colNames = colList.get(0);
		String id = colNames[0];
		Map<Integer, Map<String, Object>> dataCache = new HashMap<>();
		for (String[] datas : colList) {
			// TODO 构建MAP
			int colId;
			try {
				// 检测是否一个配置项
				colId = Integer.parseInt(CSVUtil.getColValue(id, datas, colList));
			} catch (NumberFormatException ex) {
				continue;
			}
			Map<String, Object> dataMap = new HashMap<>();
			for (String colName : colNames) {
				dataMap.put(colName, CSVUtil.getColValue(colName, datas, colList));
			}
			dataCache.put(colId, dataMap);
		}
		return dataCache;
	}

	/**
	 * 遍历pr做的特效，然后生成列表，
	 */
	public static void genEffectList4UnitestTest() {
		String dir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_levelres/gecaoshoulieu3d/gecaoshouliepkmaps/Assets/Prefabs/Effect";
		Map<String, String> map = getEffectPrefabMap(new File(dir), null);
		StringBuilder sb = new StringBuilder();
		for (String key : map.keySet()) {
			sb.append(key).append("\n");
		}
		String resDir = "D:/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_client/gecaoshoulieu3d/gecaoshouliedemo/Assets/Resources/";
		if (!(new File(resDir).isDirectory())) {
			System.err.println("genEffectList4UnitestTest|" + resDir + "不是文件夹");
		}
		System.out.println(sb);
		GameCSV2DB.writeFile(resDir + "/effectlist4pr.txt", sb.toString(), "UTF-8");
		// System.out.println(map.keySet());
		// System.out.println(sb);
	}

	private static Map<String, String> getEffectPrefabMap(File f, Map<String, String> effectPrefabMap) {
		if (effectPrefabMap == null) {
			effectPrefabMap = new TreeMap<String, String>();
		}
		if (!f.exists()) {
			System.err.println(f + "不存在");
		}
		if (f.isFile() && f.getName().endsWith(".prefab") && f.getName().toLowerCase().startsWith("effect_")) {
			String assetPath = f.getAbsolutePath();
			assetPath = assetPath.substring(assetPath.indexOf("Assets"));
			String guid = f.getName().replace(".prefab", "");
			if (effectPrefabMap.containsKey(guid)) {
				System.err.println("已经存在：" + effectPrefabMap.get(guid) + "|但是现在指向" + assetPath);
			}
			effectPrefabMap.put(guid, assetPath);
		}
		if (f.isDirectory()) {
			File subfs[] = f.listFiles();
			for (File subf : subfs) {
				getEffectPrefabMap(subf, effectPrefabMap);
			}
		}
		return effectPrefabMap;
	}
}
