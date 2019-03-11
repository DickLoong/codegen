package com.lizongbo.codegentool.world;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lizongbo.codegentool.DeployUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;
import com.lizongbo.codegentool.db2java.GenAll;

/**
 * 构建游戏服务器
 * @author linyaoheng
 *
 */
public class ServerReleaseVersion {
	
	private static String releasePath = null;
	private static String releaseTag = null;
	
	private static ThreadLocal<SimpleDateFormat> tlSdf = new ThreadLocal<>();

	public static void main(String[] args) {
		//构建到相应的服务器
		String worldName = System.getenv("worldName");
		String gameServerPath = System.getenv("GAME_SERVER_PATH");
		String envReleaseTag = System.getenv("releaseTag");
		
		DeployUtil.ShowEnv();
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		SimpleDateFormat sdf = GetSimpleDateFormat();
		releaseTag = sdf.format(new Date(System.currentTimeMillis()));
		if (envReleaseTag != null && !envReleaseTag.equals("latest")){
			releaseTag = envReleaseTag;
		}
		
		//本地路径
		releasePath = I18NUtil.worldRootDir + "/" + worldName + "/release_version/" + releaseTag + "/";
		new File(releasePath).mkdirs();
		
		//构建GS,同时用于FS, Common Server
		genJavaServer(worldName, gameServerPath);
		
		//构建MS
		genMapJavaServer(worldName);
		
		//生成SQL文件
		genSqlFiles(worldName);
		
		//在Version Server创建文件夹
		DeployCommandUtil.CreateReleaseVersionTagRoot(worldName, releaseTag);
		
		//同步版本文件过去
		for (String item : BilinGameWorldConfig.getNeedFiles()){
			ScpCommandUtil.scpServerReleaseFileToVersionServer(worldName, releaseTag, releasePath + item, item);
		}
		
		//创建一个latest目录
		if ("latest".equals(envReleaseTag)){
			DeployCommandUtil.CreateReleaseVersionLatestTag(worldName, releaseTag);
		}
		
		LogUtil.printLog("Please remember the release tag:" + releaseTag);
	}
	
	/**
	 * 构建游戏服务器
	 */
	private static void genJavaServer(String worldName, String gameServerPath) {
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("genJavaServer|worldName|" + worldName + "|starting");
		
		String localAppDir = I18NUtil.gameServerSrcPath;
		if (gameServerPath != null && gameServerPath.length() > 0){
			localAppDir = gameServerPath;
		}
		LogUtil.printLog("use gamserver path|" + localAppDir);
		
		if (!new File(localAppDir).isDirectory()) {
			return;
		}
		if (!new File(new File(localAppDir), "WEB-INF").isDirectory()) {
			return;
		}
		
		String resinNewDir = I18NUtil.getGameServerbuildPath(worldName);

		String tmpZipFile = resinNewDir + ".zip";

		LogUtil.printLog("try Copy!!!!!!!");

		try {
			ServerContainerGenTool.delAllFile(resinNewDir);
			ServerContainerGenTool.copyDir(new File(localAppDir), new File(resinNewDir));
			ServerContainerGenTool.copyDir(new File(ServerContainerGenTool.javaserverconftempdir), new File(resinNewDir));
			
			//在复制文件后生成新的版本信息的文件
			GenServerVersionFile(resinNewDir, releaseTag);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Process process;
		try {
			ServerContainerGenTool
					.runShellCmd("/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + resinNewDir + "/WEB-INF/build.xml");

			//根据build.xml这两个是有先后顺序的
			//构建gecaoshoulie_servercodegen.jar
			ServerContainerGenTool
					.runShellCmd("/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + resinNewDir + "/WEB-INF/build.xml compileAutoGenCode");
			
			//将生成的gecaoshoulie_servercodegen.jar先复制release_version目录
			ServerContainerGenTool.copyFile(new File(resinNewDir + "/WEB-INF/dist/" + I18NUtil.getServercodegenJarName()), 
					new File(releasePath + I18NUtil.getServercodegenJarName()));
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		ServerContainerGenTool.delAllFile4Need(resinNewDir);
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/lib");
		
		//删除构建包不需要包含的文件
		I18NUtil.delNoNeedDir(resinNewDir);
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/pojoclasses");
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/build.xml");
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/buildPojo.xml");
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/buildPojoV2.xml");
		
		ServerContainerGenTool.zipDir(resinNewDir, tmpZipFile, ".");
		
		LogUtil.printLog("tmpZipFile|" + tmpZipFile);
		
		try {
			ServerContainerGenTool.copyFile(new File(tmpZipFile), new File(releasePath + "" + new File(tmpZipFile).getName()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("releasePath|" + releasePath);
		LogUtil.printLog("genJavaServer|worldName|" + worldName + "|usedtime|" + (endTime - startTime) + "ms");
				
	}
	
	/**
	 * 构建场景服务器
	 */
	private static void genMapJavaServer(String worldName) {
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("genMapJavaServer|worldName|" + worldName + "|starting");
		
		// 构建到相应目录
		String localAppDir = I18NUtil.mapServerSrcPath;
				
		if (!new File(localAppDir).isDirectory()) {
			return;
		}
		if (!new File(new File(localAppDir), "WEB-INF").isDirectory()) {
			return;
		}
		
		// 前期做成传本地目录的，后面做成直接拉svn的代码来发布
		
		String resinNewDir = I18NUtil.getMapServerBuildPath(worldName);

		String tmpZipFile = resinNewDir + ".zip";
		
		LogUtil.printLog("try Copy!!!!!!!");

		try {
			ServerContainerGenTool.delAllFile(resinNewDir);
			ServerContainerGenTool.copyDir(new File(localAppDir), new File(resinNewDir));
			ServerContainerGenTool.copyDir(new File(ServerContainerGenTool.javaserverconftempdir), new File(resinNewDir));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Process process;
		try {
			LogUtil.printLog("try /mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + resinNewDir + "/WEB-INF/build.xml");

			ServerContainerGenTool
					.runShellCmd("/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + resinNewDir + "/WEB-INF/build.xml");

		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}

		ServerContainerGenTool.delAllFile4Need(resinNewDir);
		
		//删除构建包不需要包含的文件
		I18NUtil.delNoNeedDir(resinNewDir);
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/build.xml");
		
		ServerContainerGenTool.zipDir(resinNewDir, tmpZipFile, ".");
		
		try {
			ServerContainerGenTool.copyFile(new File(tmpZipFile), new File(releasePath + "" + new File(tmpZipFile).getName()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("releasePath|" + releasePath);
		LogUtil.printLog("genMapJavaServer|worldName|" + worldName + "|usedtime|" + (endTime - startTime) + "ms");
		
	}
	
	/**
	 * 生成SQL文件
	 */
	private static void genSqlFiles(String worldName){
		long timeStart = System.currentTimeMillis();
		LogUtil.printLog("genSqlFiles starting");
		
		//根据CSV生成相应的SQL
		SQLGen4WorldUtil.genWorldSql(worldName);
		
		String sqlFilesPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/sqlfiles";
		String sqlFilesZipFile = sqlFilesPath + ".zip";
		ServerContainerGenTool.zipDir(sqlFilesPath, sqlFilesZipFile, "sqlfiles");
		
		LogUtil.printLog("sqlFilesZipFile|" + sqlFilesZipFile);
		
		try {
			ServerContainerGenTool.copyFile(new File(sqlFilesZipFile), new File(releasePath + "" + new File(sqlFilesZipFile).getName()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		LogUtil.printLog("releasePath|" + releasePath);
		long timeEnd = System.currentTimeMillis();
		LogUtil.printLog("genSqlFiles completely. usedtime: " + (timeEnd - timeStart));
		
	}
	
	private static void GenServerVersionFile(String serverSrcRoot, String tag){
		//先读再写
		File saveTo = new File(serverSrcRoot, "WEB-INF/protojavarpcimpls/net/bilinkeji/gecaoshoulie/GameServerBuildInfo.java");
		String content = GenAll.readFile(saveTo.getAbsolutePath(), "UTF-8");
		
		String buildId = System.getenv("BUILD_ID");
		
		SimpleDateFormat sdf = GetSimpleDateFormat();
		content = ServerContainerGenTool.replaceAll(content, "CRT_BUILD_VERSION", buildId + "_" + tag);
		content = ServerContainerGenTool.replaceAll(content, "CRT_BUILD_DATE", sdf.format(new Date(System.currentTimeMillis())));
		
		GameCSV2DB.writeFile(saveTo.getAbsolutePath(), content);
	}
	
	private static SimpleDateFormat GetSimpleDateFormat(){
		SimpleDateFormat sdf = tlSdf.get();
		if (sdf == null){
			sdf = new SimpleDateFormat("yyyyMMddHHmm");
			tlSdf.set(sdf);
		}
		
		return sdf;
	}

}
