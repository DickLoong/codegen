package com.lizongbo.codegentool.world.porter;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;
import com.lizongbo.codegentool.world.ScpCommandUtil;

public class CommonServerPorter extends CPorter {
	
	public CommonServerPorter(String worldName, ServerDeployConfig deployConfig){
		this.worldName = worldName;
		this.deployConfig = deployConfig;
	}

	@Override
	public void BuildVersion() {
		// 不需实现,因为是和GAME SERVER在一起的
	}

	@Override
	public void DeployVersion() {
		long startTime = System.currentTimeMillis();
		
		String releaseTag = System.getenv("releaseTag");
		
		//按世界读取世界目录下的csv生成对应的公共服务器目录结构和文件
		//生成,并上传启动文件
		
		//读取本地发布用的配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName"); 
		String loginPwd = deployProp.getProperty("linuxUserPwd");

		String commonServerFilePath = I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/serverconfs/javaserver_common_server/" + deployConfig.zone_id;
	
		String bl_startShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/single_server_bl_start.sh", "UTF-8");
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");
	
		bl_startShText = ServerContainerGenTool.replaceSingleServerConfigVar(worldName, deployConfig, bl_startShText, MainServerClassName(), MainServerType());
		killShText = ServerContainerGenTool.replaceSingleServerConfigVar(worldName, deployConfig, killShText, MainServerClassName(), MainServerType());

		String saveStartFile = commonServerFilePath + "/bin/bl_start.sh";
		GameCSV2DB.writeFile(saveStartFile, bl_startShText);
	
		String saveKillFile= commonServerFilePath + "/bin/kill.sh";
		GameCSV2DB.writeFile(saveKillFile, killShText);
	
		DeployCommandUtil.Mkdir(worldName, deployConfig.server_public_ip, BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/bin");
		DeployCommandUtil.Mkdir(worldName, deployConfig.server_public_ip, BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/endorsed");
		DeployCommandUtil.Mkdir(worldName, deployConfig.server_public_ip, BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/businesslog");
		DeployCommandUtil.Mkdir(worldName, deployConfig.server_public_ip, BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/log");
	
		//上传文件
		ScpCommandUtil.ScpTo(deployConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				saveStartFile, 
				BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/bin/bl_start.sh");
		DeployCommandUtil.chmodAddX(worldName, deployConfig.server_public_ip, 
				BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/bin/bl_start.sh");
	
		ScpCommandUtil.ScpTo(deployConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				saveKillFile, 
				BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/bin/kill.sh");
		DeployCommandUtil.chmodAddX(worldName, deployConfig.server_public_ip, 
				BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/bin/kill.sh");
	
		ScpCommandUtil.ScpTo(deployConfig.server_public_ip, 
				loginUser, 
				loginPwd, 
				ServerContainerGenTool.javaserverconftempdir + "/endorsed/jmxext_lizongbo.jar", 
				BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/endorsed/jmxext_lizongbo.jar");
		
		//发布文件
		pubCommonJavaServer(releaseTag);
		
		LogUtil.printLog(this.getClass().getSimpleName() + "|DeployVersion|worldName|" + worldName + "|releaseTag|" + releaseTag + "|usedtime|" + (System.currentTimeMillis() - startTime));
	}

	@Override
	protected String GetAppRemoteRoot() {
		return BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id);
	}

	@Override
	protected String FrameServerRemoteRoot(int seq) {
		// 无帧同步
		return null;
	}

	@Override
	protected String[] GetBinPaths() {
		// TODO
		return new String[]{
				"javaserver_gecaoshoulie_server",
			};
	}

	@Override
	protected String ServerLocalCfgPath() {
		return I18NUtil.worldRootDir + "/" + worldName + "/forServer/serverconfs/javaserver_common_server/" + deployConfig.zone_id;
	}

	@Override
	protected String MainServerClassName() {
		return "net.bilinkeji.gecaoshoulie.mgameprotorpc.commonserver.CommonServer";
	}

	@Override
	protected String MainServerLocalCfgPath() {
		// TODO
		return ServerLocalCfgPath() + "/javaserver_gecaoshoulie_server";
	}

	@Override
	protected SERVER_TYP MainServerType() {
		return SERVER_TYP.COMMON_SERVER;
	}

	@Override
	protected int MainServerPort() {
		// TODO
		return deployConfig.game_server_port;
	}

	@Override
	protected String FrameSyncServerLoalCfgPath(int seq) {
		// 没有帧同步服务器
		return null;
	}

	@Override
	protected SERVER_TYP GetFrameSyncServerTypeBySeq(int seq) {
		// 没有帧同步服务器
		return null;
	}

	@Override
	protected void ImportDatabase() {
		// 没有数据库
	}
	
	/**
	 * 发布公共服务器由于公共服务器是和GameServer不在同一台机,所以它的操作是完全独立的
	 */
	private void pubCommonJavaServer(String releaseTag) {
		long startTime = System.currentTimeMillis();
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		LogUtil.printLog(this.getClass().getSimpleName() + "|pubCommonJavaServer|worldName|" + worldName + "|start");
		
		//生成data/bilin/opconf/skip_zoneId.conf,用于跳过监控
		DeployCommandUtil.CreateSkipFile(worldName, deployConfig.zone_id);
		
		DeployCommandUtil.CreateWorldTmpRoot(worldName, deployConfig.server_public_ip);
	
		//从版本服务器SCP文件
		List<String> theFiles = Arrays.asList(BilinGameWorldConfig.getNeedFiles()).subList(0, 1);
		LogUtil.printLog("pubCommonJavaServer|scp server.zip starting");
		for (String item : theFiles){
			DeployCommandUtil.copyFileFromVersionServer(worldName, 
					deployConfig.server_public_ip, 
					BilinGameWorldConfig.getWorldReleaseTagRoot(worldName, releaseTag) + "/" + item, 
					BilinGameWorldConfig.getWorldTmpRoot(worldName) + "/" + item);
		}
		LogUtil.printLog(this.getClass().getSimpleName() + "|pubCommonJavaServer|scp server.zip completely");
		
		//停止所有CommonServer进程
		DeployCommandUtil.StopGameCommonServer(worldName, deployConfig);
		
		//解压
		String tmpZipFile = BilinGameWorldConfig.getWorldTmpRoot(worldName) + "/gecaoshoulie_game_server_pub.zip";
		DeployCommandUtil.Unzip(deployConfig.server_public_ip, loginUser, loginPwd, tmpZipFile, BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id));
	
		//将javalib4server的jar复制到Common Server的WEB-INF/lib目录
		String libPath = BilinGameWorldConfig.getCommonServerRoot(deployConfig.zone_id) + "/WEB-INF/lib";
		DeployCommandUtil.cpGameServerLibTo(worldName, deployConfig.server_public_ip, libPath);
		
		//启动所有CommonServer进程
		DeployCommandUtil.startGameCommonServer(worldName, deployConfig);
		
		//删除跳过监控的占位文件
		DeployCommandUtil.RemoveSkipFile(worldName, deployConfig.zone_id);
		
		LogUtil.printLog(this.getClass().getSimpleName() + "|pubCommonJavaServer|worldName|" + worldName + "|usedtime|" + (System.currentTimeMillis() - startTime));
	}

}
