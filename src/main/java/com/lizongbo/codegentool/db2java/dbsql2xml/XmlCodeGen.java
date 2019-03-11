package com.lizongbo.codegentool.db2java.dbsql2xml;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.lizongbo.codegentool.CodeGenConsts;
import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;

public class XmlCodeGen {
	
	/**
	 * 用于记录本次的各个表的类型(世界,游戏服,运营),暂时在生成DbbeansConfig4ProtoUtil时用到
	 */
	public static TreeMap<String, String> tableRegionMap = new TreeMap<>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String dbName = "tcbus";
//		dbName = "mgamedb_sango";
//		genXml(dbName);
		
		genTablesRegion();
	}

	/**
	 * 把数据库里的表全部删除
	 * 
	 * @param dbName
	 */
	public static void dropAllTables(String dbName) {
		DbConfig dbConfig = new DbConfig();
		dbConfig.setDburl("jdbc:mysql://localhost:3306/" + dbName + "?useUnicode=true&characterEncoding=UTF-8");
		dbConfig.setDbuser("root");
		dbConfig.setDbpass("");
		if (new File("/Users/quickli").exists() || new File("/Users/linyaoheng").exists()
				|| new File("D:\\mgamedev\\tools\\mysql-5.6.26-winx64\\rootpwd.txt").exists()) {
			System.err.println("genXml｜dbConfig|use mysql pwd!!!!!!!!!!!!!!!!");
			dbConfig.setDbpass("mysqlpwdbilinkejinet");
		} else {
			System.err.println("genXml｜dbConfig|dontuse mysql pwd!!!!!!!!!!!!!!!!");
		}
		/*
		 * dbConfig.setDburl("jdbc:postgresql://127.0.0.1:5432/" + dbName);
		 * dbConfig.setDbuser("lizongbo"); dbConfig.setDbpass("");
		 */
		String[] tbs = new String[] {};
		tbs = DBUtil.listTableNames(dbConfig);
		System.out.println("dropAllTables|try|" + Arrays.toString(tbs));
		Connection conn = null;
		try {
			conn = DBUtil.getConn(dbConfig);
			Statement stmt = null;
			stmt = conn.createStatement();
			for (String tableName : tbs) {
				String dropTableSql = GameCSV2DB.genDropTableSql(tableName);
				System.out.println(dropTableSql);
				int rs = stmt.executeUpdate(dropTableSql);
			}
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 *  把已经删除excel之后导致没有csv的数据库表格drop掉
	 * 
	 */
	public static void dropNoCSVTables(String dbName) {
		//
		Set<String> tbSet4csv = GameCSV2DB.getTableNamesbyCSV(CodeGenConsts.PROJCSVFILE_DIRROOT);
		System.out.println("tbSet4csv==" + tbSet4csv);
		DbConfig dbConfig = new DbConfig();
		dbConfig.setDburl("jdbc:mysql://localhost:3306/" + dbName + "?useUnicode=true&characterEncoding=UTF-8");
		dbConfig.setDbuser("root");
		dbConfig.setDbpass("");
		if (new File("/Users/quickli").exists() || new File("/Users/linyaoheng").exists()
				|| new File("D:\\mgamedev\\tools\\mysql-5.6.26-winx64\\rootpwd.txt").exists()) {
			System.err.println("genXml｜dbConfig|use mysql pwd!!!!!!!!!!!!!!!!");
			dbConfig.setDbpass("mysqlpwdbilinkejinet");
		} else {
			System.err.println("genXml｜dbConfig|dontuse mysql pwd!!!!!!!!!!!!!!!!");
		}
		/*
		 * dbConfig.setDburl("jdbc:postgresql://127.0.0.1:5432/" + dbName);
		 * dbConfig.setDbuser("lizongbo"); dbConfig.setDbpass("");
		 */
		String[] tbs = new String[] {};
		tbs = DBUtil.listTableNames(dbConfig);
		System.out.println("dropNoCSVTables|try|" + Arrays.toString(tbs));
		Connection conn = null;
		try {
			conn = DBUtil.getConn(dbConfig);
			Statement stmt = null;
			stmt = conn.createStatement();
			for (String tableName : tbs) {
				if (!tbSet4csv.contains(tableName)) {
					String dropTableSql = GameCSV2DB.genDropTableSql(tableName);
					System.out.println("dropNoCSVTables ===============" + dropTableSql);
					int rs = stmt.executeUpdate(dropTableSql);
				} else {
					/// System.out.println("dropNoCSVTables |have csvfile for|"
					/// + tableName);
				}
			}
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 生成一个XML,用于指明各个表是属于世界,玩家服,还是运营的
	 */
	public static void genTablesRegion(){
		StringBuilder sbXmlContent = new StringBuilder();
		
		String orgDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles";
		
		// 开始解析世界内的csv，生成对应的sql
		Map<String, List<String>> map = SQLGen4WorldUtil.getCsvFileNameMap(new File(orgDir), null);
		
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			System.out.println("e|" + e.getKey() + "|v|" + e.getValue());
			
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			
			if (tableName == null || tableName.length() < 1) {
				continue;
			}
			
			if (csvFile.getAbsolutePath().contains("\\World\\")){
				sbXmlContent.append("<table>\n");
				sbXmlContent.append("<tableName><![CDATA[" + tableName + "]]></tableName>\n");
				sbXmlContent.append("<region>rootdb</region>\n");
				sbXmlContent.append("</table>\n");
				
				tableRegionMap.put(tableName, "rootdb");
				
				continue;
			}
			
			sbXmlContent.append("<table>\n");
			sbXmlContent.append("<tableName><![CDATA[" + tableName + "]]></tableName>\n");
			sbXmlContent.append("<region>gamedb</region>\n");
			sbXmlContent.append("</table>\n");
			
			tableRegionMap.put(tableName, "gamedb");
		}
		
		//写入的文件目录
		File dataFileDir = new File(CodeGenConsts.fmppDir4DB + "/templates/data/ignoredir.fmpp").getParentFile();
		String xmlFile = new File(dataFileDir, "tableregions_mgamedb_gecaoshoulie.xml").toString();
		
		DBUtil.writeFile(xmlFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		DBUtil.appendFile(xmlFile, "<tables>\n");
		DBUtil.appendFile(xmlFile, sbXmlContent.toString());
		DBUtil.appendFile(xmlFile, "</tables>\n");
	}
	
	/**
	 * 根据className从Table中知道调用的是哪个DAO
	 * @param beanName
	 * @return
	 */
	public static String getTheDAOStatament(String beanName){
		System.out.println("beanName|" + beanName + "|tableRegionMap|" + tableRegionMap);
		for (Entry<String, String> item : tableRegionMap.entrySet()){
			if (DBUtil.getPojoClassName(item.getKey()).equals(beanName)){
				if (item.getValue().equals("rootdb")){
					return "getRootDAO()";
				}
				else if (item.getValue().equals("gamedb")){
					return "getDAO(zoneId)";
				}
				else if (item.getValue().equals("operatedb")){
					return "getYunyingDAO()";
				}
				
				break;
			}
		}
		
		return "";
	}

	public static void genXml(String dbName) {
		DbConfig dbConfig = new DbConfig();
		dbConfig.setDburl("jdbc:mysql://localhost:3306/" + dbName + "?useUnicode=true&characterEncoding=UTF-8");
		dbConfig.setDbuser("root");
		dbConfig.setDbpass("");
		if (new File("/Users/quickli").exists() || new File("/Users/linyaoheng").exists()
				|| new File("D:\\mgamedev\\tools\\mysql-5.6.26-winx64\\rootpwd.txt").exists()) {
			System.err.println("genXml｜dbConfig|use mysql pwd!!!!!!!!!!!!!!!!");
			dbConfig.setDbpass("mysqlpwdbilinkejinet");
		} else {
			System.err.println("genXml｜dbConfig|dontuse mysql pwd!!!!!!!!!!!!!!!!");
		}

		/*
		 * dbConfig.setDburl("jdbc:postgresql://127.0.0.1:5432/" + dbName);
		 * dbConfig.setDbuser("lizongbo"); dbConfig.setDbpass("");
		 */
		String[] tbs = new String[] {};
		tbs = DBUtil.listTableNames(dbConfig);
		System.out.println("genXml|try|" + Arrays.toString(tbs));
		for (String table_name : tbs) {
			List<String> pks = DBUtil.getPkColumns(table_name, dbConfig);
			if (pks.size() > 1) {
				System.out.println(table_name + "的主键有多个" + pks);
			}
			if (pks.size() < 1) {
				System.out.println(table_name + "的主键是0个" + pks);
			}
		}
		File dataFileDir = new File(CodeGenConsts.fmppDir4DB + "/templates/data/ignoredir.fmpp").getParentFile();
		System.out.println("dataFileDir==" + dataFileDir);
		if (!new File(dataFileDir, "tableuis_" + dbName + ".xml").exists()) {
			String tableuixml = new File(dataFileDir, "tableuis_" + dbName + ".xml").toString();
			DBUtil.writeFile(tableuixml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			DBUtil.appendFile(tableuixml, "<tables>\n");
			DBUtil.appendFile(tableuixml, "<table>\n");
			DBUtil.appendFile(tableuixml, "<tableName><![CDATA[t_adconf]]></tableName>\n");
			DBUtil.appendFile(tableuixml, "<columns>\n");
			DBUtil.appendFile(tableuixml, "<column>\n");
			DBUtil.appendFile(tableuixml, "<name><![CDATA[fOnline]]></name>\n");
			DBUtil.appendFile(tableuixml, "<inputType>checkbox</inputType>\n");
			DBUtil.appendFile(tableuixml, "<lv label=\"是\" value=\"Y\"/>\n");
			DBUtil.appendFile(tableuixml, "<lv label=\"否\" value=\"N\"/>\n");
			DBUtil.appendFile(tableuixml, "</column>\n");
			DBUtil.appendFile(tableuixml, "<column>\n");
			DBUtil.appendFile(tableuixml, "<name><![CDATA[fPushMethod]]></name>\n");
			DBUtil.appendFile(tableuixml, "<inputType>select</inputType>\n");
			DBUtil.appendFile(tableuixml, "<lv label=\"全部\" value=\"0\"/>\n");
			DBUtil.appendFile(tableuixml, "</column>\n");
			DBUtil.appendFile(tableuixml, "</columns>\n");
			DBUtil.appendFile(tableuixml, "</table>\n");
			DBUtil.appendFile(tableuixml, "</tables>\n");
		}

		List<TableInfo> tableList = new ArrayList<TableInfo>();
		for (String table_name : tbs) {// 遍历表，写到一个xml文件里

			TableInfo table = getTableInfo(dbName, dbConfig, table_name);
			if (table.getPkColumns().length < 1) {// 不写入xml
				continue;
			}
			tableList.add(table);
		}
		String tableDescFilePath = new File(dataFileDir, "tabledesc_" + dbName + ".xml").toString();
		genTableDescFile(tableList, tableDescFilePath);
		String tablexml = new File(dataFileDir, "tables_" + dbName + ".xml").toString();
		DBUtil.writeFile(tablexml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		DBUtil.appendFile(tablexml, "<tables>\n");
		for (TableInfo table : tableList) {// 遍历表，写到一个xml文件里
			DBUtil.appendFile(tablexml, table.toTableXMLString());
			if(table.getTableName().endsWith("derrecordstatus")){
				System.out.println("derrecordstatusXML="+table.toTableXMLString());
			}
		}
		DBUtil.appendFile(tablexml, "</tables>");

		String fmppconfigStr = readFile(CodeGenConsts.fmppDir4DB + "/config_templates.fmpp", "UTF-8");
		genFmppConfig(fmppconfigStr, CodeGenConsts.fmppDir4DB + "/config_" + CodeGenConsts.PROJDBNAME + ".fmpp");

		fmppconfigStr = readFile(CodeGenConsts.fmppDir4ProtoCmd + "/config_templates.fmpp", "UTF-8");
		genFmppConfig(fmppconfigStr, CodeGenConsts.fmppDir4ProtoCmd + "/config_" + CodeGenConsts.PROJNAME + ".fmpp");

		fmppconfigStr = readFile(CodeGenConsts.fmppDir4ProtoCmd2Unity + "/config_templates.fmpp", "UTF-8");
		genFmppConfig(fmppconfigStr,
				CodeGenConsts.fmppDir4ProtoCmd2Unity + "/config_" + CodeGenConsts.PROJNAME + ".fmpp");

	}

	public static TableInfo getTableInfo(String dbName, DbConfig dbConfig, String table_name) {
		TableInfo table = new TableInfo();
		table.setTableName(table_name);
		table.setPojoClassName(DBUtil.getPojoClassName(table_name));
		table.setTableComment(DBUtil.getMysqlTableComments(table_name, dbConfig));
		table.setAbstractPojoClassName(DBUtil.getAbstractPojoClassName(table_name));
		table.setPkColumns(DBUtil.getPkColumns(table_name, dbConfig).toArray(new String[0]));
		table.setNoPkColumns(DBUtil.getNotPkColumns(table_name, dbConfig).toArray(new String[0]));
		table.setUniqueKeyColumns(DBUtil.getUniquekeys(table_name, dbConfig));
		List<ColumnInfo> cls = new ArrayList<ColumnInfo>();
		if (table.getPkColumns().length > 0) {
			for (String cn : DBUtil.listColumns(table_name, dbConfig)) {
				cls.add(DBUtil.getMysqlColumnInfo(dbName, table_name, cn, dbConfig));
			}
		}
		table.setColumns(cls.toArray(new ColumnInfo[0]));
		return table;
	}

	private static void genFmppConfig(String fmppconfigStr, String fmppConfigFile) {
		System.out.println("fmppconfigStr==" + fmppconfigStr);
		fmppconfigStr = fmppconfigStr.replaceAll("COMMON_JAVAPACKAGEROOT", CodeGenConsts.COMMON_JAVAPACKAGEROOT);

		fmppconfigStr = fmppconfigStr.replaceAll("PROJDBNAME", CodeGenConsts.PROJDBNAME);
		fmppconfigStr = fmppconfigStr.replaceAll("PROJDBBEANS_JAVAPACKAGEROOT",
				CodeGenConsts.PROJDBBEANS_JAVAPACKAGEROOT);

		fmppconfigStr = fmppconfigStr.replaceAll("COMMONDATA_JAVAPACKAGEROOT",
				CodeGenConsts.COMMONDATA_JAVAPACKAGEROOT);

		fmppconfigStr = fmppconfigStr.replaceAll("UTILS_JAVAPACKAGEROOT", CodeGenConsts.UTILS_JAVAPACKAGEROOT);

		fmppconfigStr = fmppconfigStr.replaceAll("COMMONDB_JAVAPACKAGEROOT", CodeGenConsts.COMMONDB_JAVAPACKAGEROOT);
		fmppconfigStr = fmppconfigStr.replaceAll("DBBEANHELPERS_JAVAPACKAGEROOT",
				CodeGenConsts.DBBEANHELPERS_JAVAPACKAGEROOT);
		fmppconfigStr = fmppconfigStr.replaceAll("PROJPROTO_JAVAPACKAGEROOT", CodeGenConsts.PROJPROTO_JAVAPACKAGEROOT);
		fmppconfigStr = fmppconfigStr.replaceAll("PROJNAME", CodeGenConsts.PROJNAME);
		fmppconfigStr = fmppconfigStr.replaceAll("PROJPROTORPCBASE_JAVAPACKAGEROOT",
				CodeGenConsts.PROJPROTORPCBASE_JAVAPACKAGEROOT);

		DBUtil.writeFile(fmppConfigFile, fmppconfigStr);
	}

	/**
	 * 
	 * <b>功能：读取定义的字段说明和注释，如果没有定义的，默认将数据库里查到的合并进去，已经存在的，则替代数据库的注释</b><br>
	 * <br>
	 * <b>实现步骤：</b><br>
	 * <b>1.</b> <br>
	 * <b>2.</b> <br>
	 * 
	 * @修改者 ~ , quickli 2012-12-25
	 * @param tableList
	 * @param tableDescXmlFile
	 * @return boolean
	 */
	public static boolean genTableDescFile(List<TableInfo> tableList, String tableDescXmlFile) {
		// 先读取该文件，然后将已经定义的标题和注释保留，
		// 然后删除不存在的字段，并合并进新字段的默认注释，方便手工修改
		if (!new File(tableDescXmlFile).exists()) {// 文件不存在，则写入内容
			DBUtil.writeFile(tableDescXmlFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			DBUtil.appendFile(tableDescXmlFile, "<tables>\n");
			DBUtil.appendFile(tableDescXmlFile, "</tables>");
		}
		try {
			// 读取文件
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(tableDescXmlFile);
			Element root = doc.getRootElement();
			// List<Element> tableEList = root.getChildren("table");
			for (TableInfo tb : tableList) {
				if (tb.getTableName().startsWith("rsync4_")) {
					continue;// 跳过自动外挂的表
				}
				Element tablee = getTableElement(tb.getTableName(), root.getChildren("table"));
				if (tablee == null) {
					tablee = newTableElement(tb.getTableName(), tb.getTitle(), tb.getSubcmt());
					root.addContent(tablee);
				} else {
					tb.setTitle(tablee.getChildTextTrim("title"));
					tb.setSubcmt(tablee.getChildTextTrim("subcmt"));
					tb.setTableComment(tablee.getChildTextTrim("title") + "|" + tablee.getChildTextTrim("subcmt"));
				}
				Element columnse = tablee.getChild("columns");
				if (columnse == null) {
					columnse = new Element("columns");
					tablee.addContent(columnse);
				}
				// 开始遍历column元素
				for (ColumnInfo columnInfo : tb.getColumns()) {
					Element columne = getColumnElement(columnInfo.getName(), columnse.getChildren("column"));
					if (columne == null) {
						columne = newColumnElement(columnInfo.getName(), columnInfo.getTitle(), columnInfo.getSubcmt(),
								columnInfo.isNullable());
						columnse.addContent(columne);
					} else {
						columnInfo.setTitle(columne.getChildTextTrim("title"));
						columnInfo.setSubcmt(columne.getChildTextTrim("subcmt"));
						/*
						 * columnInfo.setComments(
						 * columne.getChildTextTrim("title") + "|" +
						 * columne.getChildTextTrim("subcmt"));
						 */
						String nullable = columne.getChildTextTrim("nullable");
						// 有些字段在数据库设置的是不能为空，但实际是可以为空的，表单上不想填写这个，因此允许在此覆盖设置
						if ((nullable != null)) {
							columnInfo.setNullable(nullable.equalsIgnoreCase("true"));
						} else {
							Element nullablee = new Element("nullable");
							nullablee.addContent("" + columnInfo.isNullable());
							columne.addContent(nullablee);
						}
					}
				}
			}
			// 把table对象给填上了

			org.jdom2.output.Format format = org.jdom2.output.Format.getPrettyFormat();
			org.jdom2.output.XMLOutputter xo = new XMLOutputter(format);
			xo.output(doc, new FileOutputStream(tableDescXmlFile));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static Element newColumnElement(String columnName, String title, String subcmt, boolean nullable) {
		if (title == null) {
			title = " ";
		}
		if (subcmt == null) {
			subcmt = " ";
		}
		Element e = new Element("column");
		Element tableNamee = new Element("name");
		tableNamee.addContent(columnName);
		e.addContent(tableNamee);
		Element titlee = new Element("title");
		titlee.addContent(new org.jdom2.CDATA(title));
		e.addContent(titlee);
		Element subcmte = new Element("subcmt");
		subcmte.addContent(new org.jdom2.CDATA(subcmt));
		e.addContent(subcmte);
		Element nullablee = new Element("nullable");
		nullablee.addContent("" + nullable);
		e.addContent(nullablee);

		return e;
	}

	private static Element newTableElement(String tableName, String title, String subcmt) {
		if (title == null) {
			title = " ";
		}
		if (subcmt == null) {
			subcmt = " ";
		}
		Element e = new Element("table");
		Element tableNamee = new Element("tableName");
		tableNamee.addContent(tableName);
		e.addContent(tableNamee);
		Element titlee = new Element("title");
		titlee.addContent(new org.jdom2.CDATA(title));
		e.addContent(titlee);
		Element subcmte = new Element("subcmt");
		subcmte.addContent(new org.jdom2.CDATA(subcmt));
		e.addContent(subcmte);
		return e;
	}

	private static Element newTitleElement(String title) {
		Element titlee = new Element("title");
		titlee.addContent(new org.jdom2.CDATA(title));
		return titlee;
	}

	private static Element getColumnElement(String columnName, List<Element> columnEList) {
		if (columnEList != null) {
			for (Element e : columnEList) {
				if (columnName.equals(e.getChildTextTrim("name"))) {
					return e;
				}
			}
		}
		return null;
	}

	private static Element getTableElement(String tableName, List<Element> tableEList) {
		if (tableEList != null) {
			for (Element e : tableEList) {
				if (tableName.equals(e.getChildTextTrim("tableName"))) {
					return e;
				}
			}
		}
		return null;
	}

	private static void fillTableElement(List<Element> tableEList, List<TableInfo> tableList) {
		List<Element> tableEListTmp = new ArrayList<Element>(tableEList);

	}

	public static String readFile(String path, String encoding) {
		StringBuilder sb = new StringBuilder();
		File readFile;
		try {
			readFile = new File(path);
			// 如果文本文件不存在则返回空串
			if (!readFile.exists()) {
				return "";
			}
			java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.InputStreamReader(new java.io.FileInputStream(path), encoding));

			String data = null;
			while ((data = br.readLine()) != null) {
				sb.append(data).append("\n");
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}
}
