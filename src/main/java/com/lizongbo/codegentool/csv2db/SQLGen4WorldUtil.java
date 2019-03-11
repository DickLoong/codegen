package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lizongbo.codegentool.CodeGenConsts;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

public class SQLGen4WorldUtil {

	public static void main(String[] args) {
		//String worldName = "xjws_mainland_alpha_test";
		//String worldName = "iOS_Audit";
		
		//createOnlyWorldSql(worldName);

		//createOperateDBSql(worldName);
		
		//SQLGen4WorldUtil.genWorldSql(worldName);
		
//		String serverCsvPath = I18NUtil.worldRootDir + "/" + worldName
//				+ "/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_Gamezone(游戏服务区).csv";
//		List<String[]> list = CSVUtil.getDataFromCSV2(serverCsvPath);
		
//		for (int i = 4; i < list.size(); i++) {
//			String s[] = list.get(i);
//			int zoneId = StringUtil.toInt(CSVUtil.getColValue("zone_id", s, list));
//			genWorldZoneSql(worldName, zoneId);
//		}
	}

	/**
	 * 按世界读取世界目录下的csv生成对应的追加sql
	 * 
	 * @param worldName
	 */
	public static void genWorldSql(String worldName) {
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		I18NUtil.switchWorld(worldName);

		LogUtil.printLog("run|genWorldSql|for|" + worldName);
		String worldDir = I18NUtil.worldRootDir + "/" + worldName;
		String orgDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles";
		// 开始解析世界内的csv，生成对应的sql
		Map<String, List<String>> map = getCsvFileNameMap(new File(orgDir), null);
		map = getCsvFileNameMap(new File(worldDir), map);
		LogUtil.printLog(map.keySet().toString());
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			if (tableName == null || tableName.length() < 1) {
				LogUtil.printLog("genWorldSql|skip|not|db|csv|" + csvFile);
				continue;
			}
			if (isNoDropInsertTable(tableName)) {
				LogUtil.printLog("genWorldSql|skip|user|csv|" + csvFile);
				continue;
			}
			
			//跳过ROOT DB的表
			if (csvFile.getAbsolutePath().contains("\\World\\")){
				continue;
			}
			
			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			if (!GameCSV2DB.sbDropTableMap.containsKey(tablePrefix)) {
				GameCSV2DB.sbDropTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbCreateTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbInsertTableMap.put(tablePrefix, new StringBuilder());
			}

			String dropTableSql = GameCSV2DB.genDropTableSql(tableName);
			GameCSV2DB.sbDropTable.append(dropTableSql).append("\n");
			GameCSV2DB.sbDropTableMap.get(tablePrefix).append(dropTableSql).append("\n");
			String createTableSql = GameCSV2DB.genCreateTableSql(tableName, tableCmt, colList);
			GameCSV2DB.sbCreateTable.append(createTableSql).append("\n");
			GameCSV2DB.sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
			// LogUtil.printLog("createTableSql == " + createTableSql);
			//2017-08-11 linyaoheng 不生成tlocale_collect的REPLACE INTO语句
			if (!"tlocale_collect".equals(tableName)){
				List<Pair<String, String>> rsqlList = GameCSV2DB.genReplaceInsertSql(tablePrefix, tableName, colList);
			}
			
			List<Pair<String, String>> usqlList = GameCSV2DB.genUpdateSql(tableName, colList);
		}

		String worldSqlFilesDir = I18NUtil.worldRootDir + "/" + worldName + "/forServer/sqlfiles";
		if (new File(worldSqlFilesDir).exists()){
			LogUtil.printLog("create|directory|delAllFile" + worldSqlFilesDir);
			ServerContainerGenTool.delAllFile(worldSqlFilesDir);
		}
		else{
			new File(worldSqlFilesDir).mkdir();
			LogUtil.printLog("create|directory|mkdir" + worldSqlFilesDir);
		}
		
		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "dropdb_" + worldName + ".sql").getAbsolutePath(),
				GameCSV2DB.sbDropTable.toString());
		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "createdb_" + worldName + ".sql").getAbsolutePath(),
				GameCSV2DB.sbCreateTable.toString());

		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "dropandcreatedb_" + worldName + ".sql").getAbsolutePath(),
				GameCSV2DB.sbDropTable.toString() + GameCSV2DB.sbCreateTable.toString());
		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "insertdb_" + worldName + ".sql").getAbsolutePath(),
				" set sql_mode= \'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n"
						+ GameCSV2DB.sbInsertTable.toString());
		
		//2017-08-12 linyaoheng 按Excel来生来单独的insert sql语句文件
		for (Map.Entry<String, StringBuilder> e : GameCSV2DB.sbInsertTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(worldSqlFilesDir + "/insertsqls", "" + e.getKey() + "_insert.sql").getAbsolutePath(),
					" set sql_mode= \'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n"
						+ e.getValue().toString());
		}

		GameCSV2DB.sbDropTableMap.clear();
		GameCSV2DB.sbCreateTableMap.clear();
		GameCSV2DB.sbInsertTableMap.clear();
		
		// 下面只生成user有关的表的创建sql，不存在drop
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			if (tableName == null || tableName.length() < 1) {
				LogUtil.printLog("genWorldSql|skip|not|db|csv|" + csvFile);
				continue;
			}
			if (!isNoDropInsertTable(tableName)) {
				LogUtil.printLog("genWorldSql|skip|not|user|csv|" + csvFile);
				continue;
			}
			
			//跳过ROOT DB的表
			if (csvFile.getAbsolutePath().contains("\\World\\")){
				continue;
			}
			
			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			if (!GameCSV2DB.sbDropTableMap.containsKey(tablePrefix)) {
				GameCSV2DB.sbDropTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbCreateTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbInsertTableMap.put(tablePrefix, new StringBuilder());
			}
			
			String dropTableSql = GameCSV2DB.genDropTableSql(tableName);
			GameCSV2DB.sbDropTable.append(dropTableSql).append("\n");
			GameCSV2DB.sbDropTableMap.get(tablePrefix).append(dropTableSql).append("\n");
			String createTableSql = GameCSV2DB.genCreateTableSql(tableName, tableCmt, colList);
			GameCSV2DB.sbCreateTable.append(createTableSql).append("\n");
			GameCSV2DB.sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
			// LogUtil.printLog("createTableSql == " + createTableSql);
			List<Pair<String, String>> rsqlList = GameCSV2DB.genReplaceInsertSql(tablePrefix, tableName, colList);
			List<Pair<String, String>> usqlList = GameCSV2DB.genUpdateSql(tableName, colList);
		}

		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "createdb4user_" + worldName + ".sql").getAbsolutePath(),
				GameCSV2DB.sbCreateTable.toString());
	}
	
	/**
	 * 仅生成ROOT DB的CREATE/INSERT语句,暂时请不要与genWorldSql在同个进程内调用
	 */
	public static void createOnlyWorldSql(String worldName){
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		I18NUtil.switchWorld(worldName);

		LogUtil.printLog("run|createOnlyWorldSql|for|" + worldName);
		String worldDir = I18NUtil.worldRootDir + "/" + worldName;
		String orgDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles";
		
		String worldSqlFilesDir = I18NUtil.worldRootDir + "/" + worldName + "/forServer/worldSqls";
		String sqlFilesZipFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/worldSqlFile.zip";
		
		// 先删除文件
		if (new File(worldSqlFilesDir).exists()){
			LogUtil.printLog("create|directory|delAllFile" + worldSqlFilesDir);
			ServerContainerGenTool.delAllFile(worldSqlFilesDir);
		}
		else{
			new File(worldSqlFilesDir).mkdir();
			LogUtil.printLog("create|directory|mkdir" + worldSqlFilesDir);
		}
		
		// 开始解析世界内的csv，生成对应的sql
		Map<String, List<String>> map = getCsvFileNameMap(new File(orgDir), null);
		map = getCsvFileNameMap(new File(worldDir), map);
		LogUtil.printLog(map.keySet().toString());
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			if (tableName == null || tableName.length() < 1) {
				LogUtil.printLog("createOnlyWorldSql|skip|not|db|csv|" + csvFile);
				continue;
			}
			if (isNoDropInsertTable(tableName)) {
				LogUtil.printLog("createOnlyWorldSql|skip|user|csv|" + csvFile);
				continue;
			}
			
			if (csvFile.getAbsolutePath().contains("\\World\\") == false){
				continue;
			}
			
			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			if (!GameCSV2DB.sbDropTableMap.containsKey(tablePrefix)) {
				GameCSV2DB.sbDropTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbCreateTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbInsertTableMap.put(tablePrefix, new StringBuilder());
			}

			String dropTableSql = GameCSV2DB.genDropTableSql(tableName);
			GameCSV2DB.sbDropTable.append(dropTableSql).append("\n");
			GameCSV2DB.sbDropTableMap.get(tablePrefix).append(dropTableSql).append("\n");
			String createTableSql = GameCSV2DB.genCreateTableSql(tableName, tableCmt, colList);
			GameCSV2DB.sbCreateTable.append(createTableSql).append("\n");
			GameCSV2DB.sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
			// LogUtil.printLog("createTableSql == " + createTableSql);
			List<Pair<String, String>> rsqlList = GameCSV2DB.genReplaceInsertSql(tablePrefix, tableName, colList);
			List<Pair<String, String>> usqlList = GameCSV2DB.genUpdateSql(tableName, colList);
		}
		
		String tmpDropTableString = GameCSV2DB.sbDropTable.toString();

		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "createworlddb.sql").getAbsolutePath(),
				GameCSV2DB.sbCreateTable.toString());

		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "insertworlddb.sql").getAbsolutePath(),
				" set sql_mode= \'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n"
						+ GameCSV2DB.sbInsertTable.toString());
		
		//2017-08-12 linyaoheng 按Excel来生来单独的insert sql语句文件
		for (Map.Entry<String, StringBuilder> e : GameCSV2DB.sbInsertTableMap.entrySet()) {
			GameCSV2DB.writeFile(
					new File(worldSqlFilesDir + "/insertsqls", "" + e.getKey() + "_insert.sql").getAbsolutePath(),
					" set sql_mode= \'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n"
					+ e.getValue().toString());
		}
		
		GameCSV2DB.sbDropTableMap.clear();
		GameCSV2DB.sbCreateTableMap.clear();
		GameCSV2DB.sbInsertTableMap.clear();
		
		// 下面只生成user有关的表的创建sql，不存在drop
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			if (tableName == null || tableName.length() < 1) {
				LogUtil.printLog("createOnlyWorldSql|skip|not|db|csv|" + csvFile);
				continue;
			}
			if (!isNoDropInsertTable(tableName)) {
				LogUtil.printLog("createOnlyWorldSql|skip|not|user|csv|" + csvFile);
				continue;
			}
			
			if (csvFile.getAbsolutePath().contains("\\World\\") == false){
				continue;
			}
			
			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			if (!GameCSV2DB.sbDropTableMap.containsKey(tablePrefix)) {
				GameCSV2DB.sbDropTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbCreateTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbInsertTableMap.put(tablePrefix, new StringBuilder());
			}
			
			String dropTableSql = GameCSV2DB.genDropTableSql(tableName);
			GameCSV2DB.sbDropTable.append(dropTableSql).append("\n");
			GameCSV2DB.sbDropTableMap.get(tablePrefix).append(dropTableSql).append("\n");
			String createTableSql = GameCSV2DB.genCreateTableSql(tableName, tableCmt, colList);
			GameCSV2DB.sbCreateTable.append(createTableSql).append("\n");
			GameCSV2DB.sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
			// LogUtil.printLog("createTableSql == " + createTableSql);
			List<Pair<String, String>> rsqlList = GameCSV2DB.genReplaceInsertSql(tablePrefix, tableName, colList);
			List<Pair<String, String>> usqlList = GameCSV2DB.genUpdateSql(tableName, colList);
		}

		GameCSV2DB.writeFile(new File(worldSqlFilesDir, "createworlddb.sql").getAbsolutePath(),
				tmpDropTableString + "\n" + GameCSV2DB.sbCreateTable.toString());
		
		LogUtil.printLog("createOnlyWorldSql|writeFile|..............");
		LogUtil.printLog("sqlsql:" + tmpDropTableString + "\n" + GameCSV2DB.sbCreateTable.toString());
		
		//生成压缩文件
		ServerContainerGenTool.zipDir(worldSqlFilesDir, sqlFilesZipFile, ".");
		LogUtil.printLog("sucessfully zip sql:" + sqlFilesZipFile);
	}
	
	/**
	 * 仅生成运营 DB的CREATE/INSERT语句,暂时请不要与其它的生成SQL在同个进程内调用
	 */
	public static void createOperateDBSql(String worldName){
		List<String> operateTables = Arrays.asList(
			"TWebAdmin(运营管理平台)_Statsresult(统计结果表管理).csv",
			"TWebAdmin(运营管理平台)_Serveronlinestatussnapshot(在线人).csv"
		);
		
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		I18NUtil.switchWorld(worldName);

		LogUtil.printLog("run|createOperateDBSql|for|" + worldName);
		String worldDir = I18NUtil.worldRootDir + "/" + worldName;
		String orgDir = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_configs/csvfiles";
		
		// 开始解析世界内的csv，生成对应的sql
		Map<String, List<String>> map = getCsvFileNameMap(new File(orgDir), null);
		map = getCsvFileNameMap(new File(worldDir), map);
		LogUtil.printLog(map.keySet().toString());
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			LogUtil.printLog("csvFilecsvFilecsvFilecsvFilecsvFilecsvFilecsvFilecsvFilecsvFile|" + csvFile);
			if (tableName == null || tableName.length() < 1) {
				LogUtil.printLog("createOperateDBSql|跳过非db的csv|" + csvFile);
				continue;
			}
			
			if (operateTables.contains(csvFile.getName()) == false){
				LogUtil.printLog("createOperateDBSql|跳过非运营的csv|" + csvFile);
				continue;
			}
			
			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			if (!GameCSV2DB.sbCreateTableMap.containsKey(tablePrefix)) {
				GameCSV2DB.sbCreateTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbInsertTableMap.put(tablePrefix, new StringBuilder());
			}

			String createTableSql = GameCSV2DB.genCreateTableSql(tableName, tableCmt, colList);
			GameCSV2DB.sbCreateTable.append(createTableSql).append("\n");
			GameCSV2DB.sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
			List<Pair<String, String>> rsqlList = GameCSV2DB.genReplaceInsertSql(tablePrefix, tableName, colList);
			List<Pair<String, String>> usqlList = GameCSV2DB.genUpdateSql(tableName, colList);
		}

		GameCSV2DB.writeFile(new File(I18NUtil.getWorldOperateSQLRoot(worldName), "createoperatefromconfig.sql").getAbsolutePath(),
				GameCSV2DB.sbCreateTable.toString());

		GameCSV2DB.writeFile(new File(I18NUtil.getWorldOperateSQLRoot(worldName), "insertoperatefromconfig.sql").getAbsolutePath(),
				" set sql_mode= \'NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n"
						+ GameCSV2DB.sbInsertTable.toString());
		
		GameCSV2DB.sbCreateTableMap.clear();
		GameCSV2DB.sbInsertTableMap.clear();

		// 下面只生成user有关的表的创建sql，不存在drop
		for (Map.Entry<String, List<String>> e : map.entrySet()) {
			File csvFile = new File(e.getValue().get(e.getValue().size() - 1));
			String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());
			if (tableName == null || tableName.length() < 1) {
				LogUtil.printLog("createOperateDBSql|跳过非db的csv|" + csvFile);
				continue;
			}
			
			if (operateTables.contains(csvFile.getName()+ ".csv") == false){
				LogUtil.printLog("createOperateDBSql|跳过非运营的csv|" + csvFile);
				continue;
			}
			
			String tablePrefix = CSVUtil.getTableNamePrefixCSVFile(csvFile.getAbsolutePath());
			String tableCmt = CSVUtil.getTableCmtFromCSVFile(csvFile.getAbsolutePath());
			List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());
			if (!GameCSV2DB.sbCreateTableMap.containsKey(tablePrefix)) {
				GameCSV2DB.sbCreateTableMap.put(tablePrefix, new StringBuilder());
				GameCSV2DB.sbInsertTableMap.put(tablePrefix, new StringBuilder());
			}
			
			String createTableSql = GameCSV2DB.genCreateTableSql(tableName, tableCmt, colList);
			GameCSV2DB.sbCreateTable.append(createTableSql).append("\n");
			GameCSV2DB.sbCreateTableMap.get(tablePrefix).append(createTableSql).append("\n");
			// LogUtil.printLog("createTableSql == " + createTableSql);
			List<Pair<String, String>> rsqlList = GameCSV2DB.genReplaceInsertSql(tablePrefix, tableName, colList);
			List<Pair<String, String>> usqlList = GameCSV2DB.genUpdateSql(tableName, colList);
		}

		GameCSV2DB.writeFile(new File(I18NUtil.getWorldOperateSQLRoot(worldName), "createoperatefromconfig.sql").getAbsolutePath(),
				GameCSV2DB.sbCreateTable.toString());
	}

	private static boolean isNoDropInsertTable(String tableName) {
		boolean rs = tableName.toUpperCase().startsWith("TUSER") 
				|| tableName.toUpperCase().startsWith("TWEBADMIN");
		if (!rs) {
			LogUtil.printLog("isNoDropInsertTable|for|" + tableName + "|==" + rs);
		}
		return rs;
	}

	/**
	 * 按世界和服务区生成对应的追加sql
	 * 
	 * @param worldName
	 * @param zoneId
	 */
	public static void genWorldZoneSql(String worldName, int zoneId) {
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		if (zoneId < 40000) {
			LogUtil.printLog("genWorldZoneSql|zoneid bixu dayu 40000|but|zoneId=" + zoneId);
			return;
		}
		I18NUtil.switchWorld(worldName);
		LogUtil.printLog("run|genWorldZoneSql|for|" + worldName + "|" + zoneId);
		String sql = getAlterStatement(zoneId);
		String worldZoneSqlFilesDir = I18NUtil.worldRootDir + "/" + worldName + "/forServer/sqlfiles/" + zoneId;
		GameCSV2DB.writeFile(
				new File(worldZoneSqlFilesDir, "alterdb_" + worldName + "_" + zoneId + ".sql").getAbsolutePath(), sql);
	}
	
	public static String getAlterStatement(int zoneId){
		String sql = "ALTER TABLE tuser4zoneserver_gameplayer AUTO_INCREMENT=" + ((zoneId - 40001) * 100000 + 20001)
				+ ";";
		return sql;
	}

	public static Map<String, List<String>> getCsvFileNameMap(File f, Map<String, List<String>> effectPrefabMap) {
		if (effectPrefabMap == null) {
			effectPrefabMap = new TreeMap<String, List<String>>();
		}
		if (!f.exists()) {
			LogUtil.printLogErr(f + "不存在");
		}
		String fileName = f.getName();
		if (fileName.endsWith(".csv") && fileName.toLowerCase().startsWith("t")) { // f.getName().endsWith(".unity")
			String guid = CSVUtil.getTableNameFromCSVFile(f.getAbsolutePath());
			if (!effectPrefabMap.containsKey(guid)) {
				effectPrefabMap.put(guid, new ArrayList<String>());
			}
			effectPrefabMap.get(guid).add(f.getAbsolutePath());
		}
		if (f.isDirectory()) {
			File subfs[] = f.listFiles();
			for (File subf : subfs) {
				getCsvFileNameMap(subf, effectPrefabMap);
			}
		}
		return effectPrefabMap;
	}
}
