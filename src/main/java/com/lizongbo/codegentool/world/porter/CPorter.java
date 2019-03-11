package com.lizongbo.codegentool.world.porter;

import java.io.File;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;
import com.lizongbo.codegentool.world.ScpCommandUtil;

/**
 * 搬运工基类定义
 */
public abstract class CPorter {
	
	protected String worldName = null;
	protected ServerDeployConfig deployConfig = null;
	
	/**
	 * 安装Resin,只有依赖Resin的才需要安装
	 */
	public void InstallResin(){
		
	}
	
	/**
	 * 安装Redis,只有依赖Redis的才需要安装
	 */
	public void InstallRedis(){
		
	}
	
	/**
	 * 构建版本,为统一行为,所以这是必须要实现的.如果不需要的则实现一个空的函数体
	 */
	public abstract void BuildVersion();
	
	/**
	 * 发布版本
	 */
	public abstract void DeployVersion();
	
	/**
	 * 在远程服务器的根目录
	 */
	protected abstract String GetAppRemoteRoot();
	
	/**
	 * 帧同步服务器 在远程服务器 的根目录
	 */
	protected abstract String FrameServerRemoteRoot(int seq);
	
	/**
	 * 服务器启动有关的几个脚本
	 */
	protected abstract String[] GetBinPaths();
	
	/**
	 * 本地服务器配置文件所在的根目录
	 */
	protected abstract String ServerLocalCfgPath();
	
	//主服务器有关的几个定义
	/**
	 * 主服务器的启动类
	 */
	protected abstract String MainServerClassName();
	
	/**
	 * 主服务器本地的路径
	 */
	protected abstract String MainServerLocalCfgPath();
	
	/**
	 * 主服务器的类型
	 */
	protected abstract SERVER_TYP MainServerType();
	
	/**
	 * 主服务器进程监听的端口
	 */
	protected abstract int MainServerPort();
	
	//帧同步服务器有关的几个定义
	/**
	 * 本地启动脚本的存储位置
	 */
	protected abstract String FrameSyncServerLoalCfgPath(int seq);
	
	/**
	 * 帧同步服务器的类型
	 */
	protected abstract I18NUtil.SERVER_TYP GetFrameSyncServerTypeBySeq(int seq);
	
	/**
	 * 创建,导入数据表,releaseTag只是为了兼容GameServer
	 * 大部分发布都用多线程来跑,而解压sql只做一次,故将解压放在上层来处理,所以要切记解压sql的操作
	 */
	protected abstract void ImportDatabase();
	
	/**
	 * 在本地生成主服务器(GAME SERVER/WORLD SERVER)对应的目录结构和文件
	 */
	protected void GenMainServerCfg(){
		String gameServerFilePath = MainServerLocalCfgPath();
		
		String mainClassName = MainServerClassName();
		
		// 开始解析世界内的csv，生成对应的游戏服务器目录结构和文件
		String bl_startShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/bl_start.sh", "UTF-8");
		String bl_reinitShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/bl_reinit2.sh", "UTF-8");
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");

		bl_startShText = ServerContainerGenTool.replaceConfigVar(bl_startShText, deployConfig, mainClassName, MainServerType());
		
		bl_reinitShText = ServerContainerGenTool.replaceConfigVar(bl_reinitShText, deployConfig, 
				"net.bilinkeji.gecaoshoulie.mgameprotorpc.apachemina.tcp.GameZoneReInit", MainServerType());
		
		killShText = ServerContainerGenTool.replaceConfigVar(killShText, deployConfig, mainClassName, MainServerType());

		GameCSV2DB.writeFile(gameServerFilePath + "/bin/bl_start.sh", bl_startShText);
		GameCSV2DB.writeFile(gameServerFilePath + "/bin/bl_reinit.sh", bl_reinitShText);
		GameCSV2DB.writeFile(gameServerFilePath + "/bin/kill.sh", killShText);
		
		GameCSV2DB.createDir(gameServerFilePath + "/businesslog");
		GameCSV2DB.createDir(gameServerFilePath + "/conf");
		
		GameCSV2DB.copyFile(new File(ServerContainerGenTool.javaserverconftempdir + "/endorsed/jmxext_lizongbo.jar"), 
				new File(gameServerFilePath + "/endorsed/jmxext_lizongbo.jar"));
		
		GameCSV2DB.createDir(gameServerFilePath + "/log");
		GameCSV2DB.createDir(gameServerFilePath + "/WEB-INF");	
	}
	
	/**
	 * 在本地生成对应的帧同步目录结构和文件
	 * @param count 帧同步服务器的数量
	 */
	protected void GenFrameSyncServerCfg(int count){
		for (int i = 1; i <= count; i++){
			genFrameSyncServerCfgBySeq(i);
		}
	}
	
	/**
	 * 统一调用的方法
	 */
	private void genFrameSyncServerCfgBySeq(int seq) {
		String mainClassName = "net.bilinkeji.gecaoshoulie.mgameprotorpc.framesync.FrameSyncServer";
		
		String frameSyncServerFilePath = FrameSyncServerLoalCfgPath(seq);
		
		String bl_startShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/bl_start.sh", "UTF-8");
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");
		
		I18NUtil.SERVER_TYP serverType = GetFrameSyncServerTypeBySeq(seq);
		
		bl_startShText = ServerContainerGenTool.replaceConfigVar(bl_startShText, deployConfig, mainClassName, serverType);
		
		killShText = ServerContainerGenTool.replaceConfigVar(killShText, deployConfig, mainClassName, serverType);

		GameCSV2DB.writeFile(frameSyncServerFilePath + "/bin/bl_start.sh", bl_startShText);
		GameCSV2DB.writeFile(frameSyncServerFilePath + "/bin/kill.sh", killShText);
		
		GameCSV2DB.createDir(frameSyncServerFilePath + "/businesslog");
		GameCSV2DB.createDir(frameSyncServerFilePath + "/conf");

		GameCSV2DB.copyFile(new File(ServerContainerGenTool.javaserverconftempdir + "/endorsed/jmxext_lizongbo.jar"), 
				new File(frameSyncServerFilePath + "/endorsed/jmxext_lizongbo.jar"));

		GameCSV2DB.createDir(frameSyncServerFilePath + "/log");
		GameCSV2DB.createDir(frameSyncServerFilePath + "/WEB-INF");
	}
	
	/**
	 * 发送服务器的配置文件到远程服务器
	 */
	protected void PubServerConfigs(){
		//压缩目录
		long startTime = System.currentTimeMillis();
		
		String serverCfgPath = ServerLocalCfgPath();
		String serverCfgZipFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/serverconfs/" + worldName + "_" + deployConfig.zone_id + ".zip";
		ServerContainerGenTool.zipDir(serverCfgPath, serverCfgZipFile, ".");
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("PubServerConfigs|zipDir|worldName|" + worldName + "|zoneId|" + deployConfig.zone_id + "|usedtime|" + (endTime - startTime));
		
		//复制到远程
		startTime = System.currentTimeMillis();
		
		//远程登录信息
		ScpCommandUtil.scpToGameServer(worldName, deployConfig.zone_id, serverCfgZipFile, 
				BilinGameWorldConfig.appsRoot + "/" + new File(serverCfgZipFile).getName());
		DeployCommandUtil.UnzipGameServerFile(worldName, deployConfig.zone_id, 
				BilinGameWorldConfig.appsRoot + "/" + new File(serverCfgZipFile).getName(),
				GetAppRemoteRoot());
		
		//添加执行权限
		String[] binPaths = GetBinPaths();
		
		for (String item : binPaths){
			DeployCommandUtil.zoneChmodAddX(worldName, deployConfig.zone_id, 
					GetAppRemoteRoot() + "/" + item + "/bin/*.sh");
		}
		
		endTime = System.currentTimeMillis();
		LogUtil.printLog("PubServerConfigs|worldName|" + worldName + "|zoneId|" + deployConfig.zone_id + "|usedtime|" + (endTime - startTime));
	}
	
	/**
	 * 返回监控用到格式字符串
	 * @return
	 */
	public String toPsMonitorConfString(){
		return "";
	}
	
}
