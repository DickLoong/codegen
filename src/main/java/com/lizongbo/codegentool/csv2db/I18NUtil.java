package com.lizongbo.codegentool.csv2db;

import java.io.*;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.util.*;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.db2java.dbsql2xml.DBUtil;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 生成I18N字段的映射信息
 * 
 * @author quickli
 *
 */
public class I18NUtil {
	/**
	 * 服务器类型
	 */
	public enum SERVER_TYP {
		UNKNOWN,
		COMMON_SERVER, 
		
		WORLD_SERVER, WORLD_FRAME_SYNC_SERVER1, WORLD_FRAME_SYNC_SERVER2, WORLD_FRAME_SYNC_SERVER3, WORLD_FRAME_SYNC_SERVER4,
		WORLD_REDIS_DBCACHE, WORLD_REDIS_COUNTER, WORLD_REDIS_RANKLIST, WORLD_REDIS_COMMON,
		
		GAME_SERVER, MAP_SERVER, FRAME_SYNC_SERVER1, FRAME_SYNC_SERVER2, FRAME_SYNC_SERVER3, FRAME_SYNC_SERVER4, 
		REDIS_DBCACHE, REDIS_COUNTER, REDIS_RANKLIST, REDIS_COMMON,
	}

	public static String worldRootDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N";
	private static String localeCollectcsvpath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/I18Ncsv/TLocale(文本本地化)_Collect(表字段汇总).csv";

	// 场景服务器源码路径
	public static String mapServerSrcPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_map_server";

	// 游戏服务器源码路径
	public static String gameServerSrcPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_server";
	
	public static String versionServerSoftwareFilesPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_version_server_softwares";

	public static boolean switchWorld(String worldName) {
		if (!I18NUtil.worldExists(worldName)) {
			System.err.println("I18NUtil.switchWorld|fail|" + worldName);
			return false;
		}
		String localeCollectcsvpathTmp = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_I18N/" + worldName
				+ "/gecaoshoulie_configs/csvfiles/I18Ncsv/TLocale(文本本地化)_Collect(表字段汇总).csv";
		if (!(new File(localeCollectcsvpathTmp).isFile())) {

			System.err.println(
					"I18NUtil.switchWorld|fileNotFound|so|useDefault|" + worldName + "|" + localeCollectcsvpathTmp);
			return true;
		}
		localeCollectcsvpath = localeCollectcsvpathTmp;
		return true;
	}

	public static boolean worldExists(String worldName) {
		File rootDir = new File(worldRootDir);
		if (!rootDir.isDirectory()) {
			LogUtil.printLogErr("世界的顶层根目录不存在|" + rootDir + "|" + worldRootDir);
			return false;
		}

		File worldDir = new File(rootDir, worldName);
		if (!worldDir.isDirectory()) {
			File worldNames[] = rootDir.listFiles();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < worldNames.length; i++) {
				sb.append(worldNames[i].getName()).append(",");

			}
			LogUtil.printLogErr("世界的目录不存在" + worldDir + "|目前支持的世界为：" + sb);
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String csvpath =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public/Common/Ttransformer(机甲养成配置)_Propertyinfo(机甲属性表).csv";
		// String tableName = CSVUtil.getTableNameFromCSVFile(csvpath);
		// List<String[]> list = CSVUtil.getDataFromCSV2(csvpath);
		// genI18NDbColIndex(tableName, list);
		// List<String[]> localeTablecolList =
		// CSVUtil.getDataFromCSV2(localeCollectcsvpath);
		// extractI18NDbColValue(tableName, list, localeTablecolList);
		// checkI18NDbIndex();
		//
		// for (int i = 4; i < localeTablecolList.size() && i < 10; i++) {
		// System.out.println(Arrays.toString(localeTablecolList.get(i)));
		// }
		//
		// for (int i = 4; i < list.size() && i < 10; i++) {
		// System.out.println(Arrays.toString(list.get(i)));
		// }
		// System.out.println(getI18NDbColLocaleKey(tableName, list));
		// I18NUtil.saveCsv(localeCollectcsvpath, localeTablecolList);
		switchWorld("zh_TW");
		String worldDir = I18NUtil.worldRootDir + "/" + "zh_TW";
		String orgDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles";
		// 开始解析世界内的csv，生成对应的sql
		Map<String, List<String>> map = SQLGen4WorldUtil.getCsvFileNameMap(new File(orgDir), null);
		map = SQLGen4WorldUtil.getCsvFileNameMap(new File(worldDir), map);
		System.out.println(map.keySet().toString().replace(',', '\n'));

		// String orgDir =
		// "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles/Public";
		// Map<String, List<String>> map =
		// SQLGen4WorldUtil.getCsvFileNameMap(new File(orgDir), null);
		List<String[]> localeTablecolList = CSVUtil.getDataFromCSV2(localeCollectcsvpath);
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));

			String csvpath = csvFile.getAbsolutePath();
			String tableName = CSVUtil.getTableNameFromCSVFile(csvpath);
			List<String[]> list = CSVUtil.getDataFromCSV2(csvpath);
			genI18NDbColIndex(tableName, list);
			System.out.println("localeTablecolList.size====" + localeTablecolList.size());
			System.out.println("localeTablecolList|" + localeTablecolList.size() + "|csvpath|" + csvpath);
			extractI18NDbColValue(tableName, list, localeTablecolList);
			checkI18NDbIndex();

			// for (int i = 4; i < localeTablecolList.size() && i < 10; i++) {
			// System.out.println(Arrays.toString(localeTablecolList.get(i)));
			// }
			//
			// for (int i = 4; i < list.size() && i < 10; i++) {
			// System.out.println(Arrays.toString(list.get(i)));
			// }
			// System.out.println(getI18NDbColLocaleKey(tableName, list));
			// I18NUtil.saveCsv(localeCollectcsvpath, localeTablecolList);
		}

		saveCsv(localeCollectcsvpath, localeTablecolList);
		System.exit(0);
	}

	public static void saveCsv(String csvFilePath, List<String[]> colList) {
		try {
			if (csvFilePath.length() > 0) {
				// System.out.println("暂时不保存" + csvFilePath);
				// return;
			}
			System.out.println("saveCsv|" + csvFilePath + "|" + colList.size());
			java.io.FileOutputStream fis = new FileOutputStream(csvFilePath);
			java.io.OutputStreamWriter isr = new OutputStreamWriter(fis, "UTF-8");
			org.apache.commons.csv.CSVPrinter csvp = new CSVPrinter(isr, org.apache.commons.csv.CSVFormat.EXCEL);
			int liechang = 0;
			for (String[] s : colList) {
				if (liechang == 0) {
					liechang = s.length;
				}
				if (s.length != liechang) {
					System.err.println("列长度不一样:" + liechang + "|but|" + s.length + "|" + Arrays.toString(s));
				}
				List<String> aList = new ArrayList<String>();
				for (String str : s) {
					if (str.length() < 1) {
						str = "-1";
					}
					aList.add(str);
				}
				csvp.printRecord(aList);
			}
			csvp.flush();
			csvp.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkI18NDbIndex() {
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/I18Ndbindexs";
		String protoIndexFileName = "alldbprefix.I18Nkey.txt";
		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
					Map<String, String> valueMap = new TreeMap<String, String>();
					Set<String> keySet = pp.stringPropertyNames();
					for (String key : keySet) {
						String value = pp.getProperty(key, "12345678");
						if (value.length() > 4 || valueMap.containsKey(value)) {
							GameCSV2DB.addErrMailMsgList("checkI18NDbIndex|值不合法,超过4字节，或是重复值" + key + "==" + value);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getI18NDbIndex(String tableName) {

		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/I18Ndbindexs";
		String protoIndexFileName = "alldbprefix.I18Nkey.txt";

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
			String propName = className;
			String pbIndex = pp.getProperty(propName, "");
			return pbIndex;
		}
	}

	public static void genI18NDbIndex(String tableName) {
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/I18Ndbindexs";
		String protoIndexFileName = "alldbprefix.I18Nkey.txt";

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
			String propName = className;
			String pbIndex = pp.getProperty(propName);
			if (pbIndex == null) { // 是新字段，则需要分配新序号，粗暴一点自动分配
				String val = propName.toLowerCase().substring(0, 2);// 取前两个字符，然后拼数字
				for (int i = 10; i < 99; i++) {
					if (!pp.containsValue(val + "" + i)) {
						val = val + i;
						break;
					}
				}
				System.out.println("genI18NDbIndex|update|" + tableName + "." + propName + "==" + val);
				// 这里需要告警一下，让人工及时分配短的前缀
				pp.setProperty(propName, val);
				try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
					storeProperties(pp, fos);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("genI18NDbIndex|exits|" + tableName + "." + propName + "==" + pbIndex);
			}
		}

	}

	private static void storeProperties(Properties pp, OutputStream fos) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));
		Set<String> keyList = new TreeSet<String>(pp.stringPropertyNames());
		System.err.println("storeProperties ==" + keyList);
		for (String key : keyList) {
			String val = pp.getProperty(key);
			bw.write(key + "=" + val);
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}

	public static void genI18NDbColIndex(String tableName, List<String[]> colList) {
		genI18NDbIndex(tableName);
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/I18Ndbindexs";
		String protoIndexFileName = className + ".I18Nkey.txt";
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
					if (colNames[i].trim().length() > 0 && (colTypes[i].toLowerCase().contains("str")
							|| colTypes[i].toLowerCase().contains("text"))) {
						String propName = colNames[i].toLowerCase().trim();
						pp.setProperty(propName, "none." + colTypes[i]);
					}
				}
				pbIndexFile.getParentFile().mkdirs();
				try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
					storeProperties(pp, fos);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < colNames.length; i++) {
				if (colNames[i].trim().length() > 0
						&& (colTypes[i].toLowerCase().contains("str") || colTypes[i].toLowerCase().contains("text"))) {
					String propName = colNames[i].toLowerCase().trim();
					String pbIndex = pp.getProperty(propName);
					if (pbIndex == null) { // 是新字段，则需要分配新序号
						System.out.println("genI18NDbIndex|update|" + tableName + "." + propName);
						pp.setProperty(propName, "none." + colTypes[i]);
						try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
							storeProperties(pp, fos);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			try (FileOutputStream fos = new FileOutputStream(pbIndexFile)) {
				storeProperties(pp, fos);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Map<String, String> getI18NDbColLocaleKey(String tableName, List<String[]> colList) {
		Map<String, String> localeKeyMap = new TreeMap<String, String>();
		if (tableName.startsWith("tlocale")) {// tlocale开头的表不参与
			return localeKeyMap;
		}
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/I18Ndbindexs";
		String protoIndexFileName = className + ".I18Nkey.txt";
		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
					String tb_prefix = getI18NDbIndex(tableName);// 多语言key的前缀
					for (int i = 0; i < colNames.length; i++) {
						String colName = colNames[i];
						String key = pp.getProperty(colName, "none.none");
						if (!key.startsWith("none.") && key.length() > 0) {// 是需要替换多语言的
							String localeKey = tb_prefix + "_" + key + "_";// 得到多语言的key
							localeKeyMap.put(colName, localeKey);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return localeKeyMap;
	}

	/**
	 * 读指定的多语言表，将多语言的文本替换到对应的csv的位置
	 * 
	 * @param tableName
	 * @param colList
	 */
	public static void fixI18NDbColValue(String tableName, List<String[]> colList) {
		if (tableName == null || tableName.length() < 1) {// 没有表名的，直接跳过
			return;
		}
		List<String[]> localeTablecolList = CSVUtil.getDataFromCSV2(localeCollectcsvpath);
		Map<String, String> localeMap = new HashMap<String, String>();
		for (int k = 4; k < localeTablecolList.size(); k++) {
			String values[] = localeTablecolList.get(k);
			// String localeKey = tb_prefix + "_" + key + "_" + values[0];//
			// 得到多语言的key
			String localeKey = CSVUtil.getColValue("locale_key", values, localeTablecolList);
			String localeValue = CSVUtil.getColValue("label", values, localeTablecolList);
			localeMap.put(localeKey, localeValue);
		}
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/I18Ndbindexs";
		String protoIndexFileName = className + ".I18Nkey.txt";
		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
					String tb_prefix = getI18NDbIndex(tableName);// 多语言key的前缀
					for (int i = 0; i < colNames.length; i++) {
						String colName = colNames[i];
						String key = pp.getProperty(colName, "none.none");
						if (!key.startsWith("none.") && key.length() > 0) {// 是需要替换多语言的
							for (int k = 4; k < colList.size(); k++) {
								String values[] = colList.get(k);
								String localeKey = tb_prefix + "_" + key + "_" + values[0];// 得到多语言的key
								String localeValue = localeMap.get(localeKey);
								if (localeValue != null) {// 替换到指定字段
									values[i] = localeValue;
								}
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void extractI18NDbColValue(String tableName, List<String[]> colList) {
		List<String[]> localeTablecolList = CSVUtil.getDataFromCSV2(localeCollectcsvpath);
		extractI18NDbColValue(tableName, colList, localeTablecolList);
		saveCsv(localeCollectcsvpath, localeTablecolList);
	}

	/**
	 * 
	 * @param tableName
	 * @param colList
	 * @param localeTablecolList
	 */
	public static void extractI18NDbColValue(String tableName, List<String[]> colList,
			List<String[]> localeTablecolList) {
		extractI18NDbColValue(tableName, colList, localeTablecolList, false);
	}

	public static void extractI18NDbColValue(String tableName, List<String[]> colList,
			List<String[]> localeTablecolList, boolean replace) {
		if (tableName == null || tableName.length() < 1) {// 没有表名的，直接跳过
			return;
		}
		int maxlocaleSeq = 0;
		Map<String, Pair<Integer, String[]>> localeMap = new HashMap<String, Pair<Integer, String[]>>();
		for (int k = 4; k < localeTablecolList.size(); k++) {
			String values[] = localeTablecolList.get(k);
			// String localeKey = tb_prefix + "_" + key + "_" + values[0];//
			// 得到多语言的key
			String localeKey = CSVUtil.getColValue("locale_key", values, localeTablecolList);
			// System.out.println("已经有的多语言key==" + localeKey);
			localeMap.put(localeKey, Pair.makePair(k, values));
			int maxlocaleSeqTmp = Integer.parseInt(values[0]);
			if (maxlocaleSeqTmp > maxlocaleSeq) {
				maxlocaleSeq = maxlocaleSeqTmp;
			}
		}
		System.out.println("extractI18NDbColValue|" + tableName + "|maxlocaleSeq=" + maxlocaleSeq);
		String className = DBUtil.getPojoClassName(tableName) + "4Proto";
		String[] colNames = colList.get(0);
		String[] colTypes = colList.get(1);
		String[] colCmts = colList.get(2);
		String protoIndexFileDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/I18Ndbindexs";
		String protoIndexFileName = className + ".I18Nkey.txt";
		File pbIndexFile = new File(protoIndexFileDir, protoIndexFileName);
		{
			Properties pp = new Properties();
			if (pbIndexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(pbIndexFile)) {
					pp.load(fis);
					String tb_prefix = getI18NDbIndex(tableName);// 多语言key的前缀
					for (int i = 0; i < colNames.length; i++) {
						String colName = colNames[i];
						String key = pp.getProperty(colName, "none.none");
						if (!key.startsWith("none.") && key.length() > 0) {// 是需要提取多语言的
							for (int k = 4; k < colList.size(); k++) {
								String values[] = colList.get(k);
								String localeKey = tb_prefix + "_" + key + "_" + values[0];// 得到多语言的key
								Pair<Integer, String[]> pair = localeMap.get(localeKey);
								if (pair == null) {// 完全没有，就生成
									maxlocaleSeq = maxlocaleSeq + 1;
									String localeCol[] = new String[] { "" + (maxlocaleSeq), localeKey, values[i], "-1",
											"-1", "-1" };
									localeTablecolList.add(localeCol);
								} else {
									if (replace) {// 需要替换
										pair.getSecond()[2] = values[i];
										pair.getSecond()[3] = "-1";
										pair.getSecond()[4] = tableName + "." + colName + "|" + values[0];
										pair.getSecond()[5] = "-1";
									}
								}
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// if (maxlocaleSeq >= 12){
		// System.exit(1);
		// }
	}

	/**
	 * 将 简体中文的多语言和其它国家或地区的多语言进行纯数字比较
	 * 
	 * @param zhCNCsvPath
	 * @param otherLocaeCsvPath
	 */
	public static void compareI18NDbColValue(String zhCNCsvPath, String otherLocaeCsvPath) {

	}
	
	public static String getServercodegenJarName(){
		return "gecaoshoulie_servercodegen.jar";
	}
	
	public static String getWorldServercodegenJarPath(String worldName){
		return I18NUtil.getWorldServerBuildRoot(worldName) + "/" + getServercodegenJarName();
	}
	
	/**
	 * 服务器本地构建时的目录
	 * @param worldName
	 * @return
	 */
	public static String getWorldServerBuildRoot(String worldName){
		return I18NUtil.worldRootDir + "/" + worldName + "/forServer/appversionfiles";
	}

	/**
	 * 游戏服务器的编译目录
	 */
	public static String getGameServerbuildPath(String worldName) {
		return getWorldServerBuildRoot(worldName) + "/gecaoshoulie_game_server_pub";
	}
	
	/**
	 * 场景服务器的编译目录
	 */
	public static String getMapServerBuildPath(String worldName) {
		return getWorldServerBuildRoot(worldName) + "/gecaoshoulie_map_server_pub";
	}

	/**
	 * 场景服务器发布到远程的统一路径
	 */
	public static String getServerRemotePath(String appName, int zoneId) {
		return BilinGameWorldConfig.appsRoot + "/" + zoneId + "/javaserver_" + appName;
	}

	/**
	 * 删除发布包不需要的目录
	 */
	public static void delNoNeedDir(String buildPath) {
		ServerContainerGenTool.delAllFile(buildPath + "/bin");
		ServerContainerGenTool.delAllFile(buildPath + "/businesslog");
		ServerContainerGenTool.delAllFile(buildPath + "/endorsed");
		ServerContainerGenTool.delAllFile(buildPath + "/log");
		ServerContainerGenTool.delAllFile(buildPath + "/redis_config");
		ServerContainerGenTool.delAllFile(buildPath + "/src");

		ServerContainerGenTool.delAllFile(buildPath + "/WEB-INF/src");

		ServerContainerGenTool.delAllFile(buildPath + "/WEB-INF/classes/dbpoolcfg");
		ServerContainerGenTool.delAllFile(buildPath + "/WEB-INF/classes/dbpoolcfg4mac");
		ServerContainerGenTool.delAllFile(buildPath + "/WEB-INF/classes/keywords");
		ServerContainerGenTool.delAllFile(buildPath + "/WEB-INF/classes/redispoolcfg");
		ServerContainerGenTool.delAllFile(buildPath + "/WEB-INF/classes/redispoolcfg4mac");
		ServerContainerGenTool.delAllFile(buildPath + "/WEB-INF/classes/statemachinecfg");
	}
	
	/**
	 * 给运营DB用的SQL目录,这是和配置表生成相关的
	 */
	public static String getWorldOperateSQLRoot(String worldName){
		return I18NUtil.worldRootDir + "/" + worldName + "/forServer/operate";
	}
}
