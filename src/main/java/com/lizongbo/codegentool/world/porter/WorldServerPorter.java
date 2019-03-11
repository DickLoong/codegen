package com.lizongbo.codegentool.world.porter;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;
import com.lizongbo.codegentool.world.RemotePerlCommandUtil;

/**
 * 世界服务器发布 定义
 * @author linyaoheng
 */
public class WorldServerPorter extends CPorter {
	
	public WorldServerPorter(String worldName, ServerDeployConfig deployConfig){
		this.worldName = worldName;
		this.deployConfig = deployConfig;
	}

	@Override
	public void DeployVersion() {
		String releaseTag = System.getenv("releaseTag");
		
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog(this.getClass().getSimpleName() + "|DeployVersion|worldName|" + worldName + "|start");
		
		GenMainServerCfg();
		GenFrameSyncServerCfg(4);
		
		ImportDatabase();
		
		PubServerConfigs();
		
		//发布文件
		RemotePerlCommandUtil.execPerlScript(worldName, deployConfig.server_public_ip, "remoteWorldServerDeploy.pl",
				deployConfig.zone_id, releaseTag);
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog(this.getClass().getSimpleName() + "|DeployVersion|worldName|" + worldName + "|usedtime|" + (endTime - startTime));
	}
	
	@Override
	protected void ImportDatabase() {
		LogUtil.printLog("ImportGameDatabase starting");
		
		String mysqlHost = BilinGameWorldConfig.getWorldServerDBHost(worldName, deployConfig.zone_id);
		String mysqlPort = BilinGameWorldConfig.getWorldServerDBPort(worldName, deployConfig.zone_id);
		String dbName = deployConfig.getDbName();
		
		DeployCommandUtil.CreateDatabase(worldName, mysqlHost, mysqlPort, dbName);
		
		DeployCommandUtil.ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, BilinGameWorldConfig.appsRoot + "/sqlfiles/createdb4user_" + worldName + ".sql");
		DeployCommandUtil.ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, BilinGameWorldConfig.appsRoot + "/sqlfiles/dropandcreatedb_" + worldName + ".sql");
		DeployCommandUtil.ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, BilinGameWorldConfig.appsRoot + "/sqlfiles/insertdb_" + worldName + ".sql");
		
		LogUtil.printLog("ImportGameDatabase completely");
	}
	
	@Override
	public void BuildVersion() {
		//不需实现,因为是和GAME SERVER在一起的
	}
	
	@Override
	public String GetAppRemoteRoot() {
		return BilinGameWorldConfig.appsRoot + "/javaserver_world_server/" + deployConfig.zone_id;
	}
	
	@Override
	protected String FrameServerRemoteRoot(int seq) {
		return BilinGameWorldConfig.appsRoot + "/javaserver_world_server/" + deployConfig.zone_id + "/javaserver_gecaoshoulie_framesync_server" + seq;
	}
	
	@Override
	public String[] GetBinPaths() {
		return new String[]{
				"javaserver_gecaoshoulie_server",
				
				"javaserver_gecaoshoulie_framesync_server1",
				"javaserver_gecaoshoulie_framesync_server2",
				"javaserver_gecaoshoulie_framesync_server3",
				"javaserver_gecaoshoulie_framesync_server4",
				
				"redis_" + deployConfig.redis1_port,
				"redis_" + deployConfig.redis2_port,
				"redis_" + deployConfig.redis3_port,
				"redis_" + deployConfig.redis4_port,
			};
	}
	
	@Override
	protected String MainServerLocalCfgPath() {
		return ServerLocalCfgPath() + "/javaserver_gecaoshoulie_server";
	}
	
	@Override
	protected String MainServerClassName() {
		return "net.bilinkeji.gecaoshoulie.mgameprotorpc.worldserver.WorldServer";
	}
	
	@Override
	protected SERVER_TYP MainServerType() {
		return SERVER_TYP.WORLD_SERVER;
	}
	
	@Override
	protected int MainServerPort() {
		return deployConfig.game_server_port;
	}

	
	@Override
	protected String ServerLocalCfgPath() {
		return I18NUtil.worldRootDir + "/" + worldName + "/forServer/serverconfs/" + deployConfig.zone_id;
	}
	
	@Override
	public String FrameSyncServerLoalCfgPath(int seq) {
		return ServerLocalCfgPath() + "/javaserver_gecaoshoulie_framesync_server" + seq;
	}
	
	@Override
	public SERVER_TYP GetFrameSyncServerTypeBySeq(int seq) {
		I18NUtil.SERVER_TYP serverType = SERVER_TYP.UNKNOWN;
		switch (seq){
		case 1:
			serverType = SERVER_TYP.WORLD_FRAME_SYNC_SERVER1;
			break;
		case 2:
			serverType = SERVER_TYP.WORLD_FRAME_SYNC_SERVER2;
			break;
		case 3:
			serverType = SERVER_TYP.WORLD_FRAME_SYNC_SERVER3;
			break;
		case 4:
			serverType = SERVER_TYP.WORLD_FRAME_SYNC_SERVER4;
			break;
		}
		
		return serverType;
	}
	
	@Override
	public String toPsMonitorConfString() {
		StringBuilder sb = new StringBuilder();
		
		if (null == worldName || null == deployConfig){
			return "";
		}
		
		sb.append(deployConfig.zone_id
				+ ":::" + deployConfig.server_inner_ip 
				+ ":::" + MainServerType() 
				+ ":::" + GetAppRemoteRoot() + "/javaserver_gecaoshoulie_server/" 
				+ "\n");
		
		sb.append(deployConfig.zone_id
				+ ":::" + deployConfig.server_inner_ip 
				+ ":::" + I18NUtil.SERVER_TYP.WORLD_FRAME_SYNC_SERVER1
				+ ":::" + FrameServerRemoteRoot(1) + "/" 
				+ "\n");
		
		int frameServerCount = BilinGameWorldConfig.getFrameServerCountByZoneId(worldName, deployConfig.zone_id);
		
		if (frameServerCount >= 2)
		{
			sb.append(deployConfig.zone_id
					+ ":::" + deployConfig.server_inner_ip 
					+ ":::" + I18NUtil.SERVER_TYP.WORLD_FRAME_SYNC_SERVER2
					+ ":::" + FrameServerRemoteRoot(2) + "/" 
					+ "\n");
		}
		
		if (frameServerCount >= 3) {
			sb.append(deployConfig.zone_id + ":::" + deployConfig.server_inner_ip + ":::"
					+ I18NUtil.SERVER_TYP.WORLD_FRAME_SYNC_SERVER3 + ":::" + FrameServerRemoteRoot(3) + "/"
					+ "\n");
		}
		
		if (frameServerCount >= 4) {
			sb.append(deployConfig.zone_id + ":::" + deployConfig.server_inner_ip + ":::"
					+ I18NUtil.SERVER_TYP.WORLD_FRAME_SYNC_SERVER4 + ":::" + FrameServerRemoteRoot(4) + "/"
					+ "\n");
		}
		
		sb.append(deployConfig.zone_id
				+ ":::" + deployConfig.server_inner_ip 
				+ ":::" + I18NUtil.SERVER_TYP.WORLD_REDIS_DBCACHE 
				+ ":::" + GetAppRemoteRoot() + "/redis_" + deployConfig.redis1_port + "/" 
				+ "\n");
		
		sb.append(deployConfig.zone_id
				+ ":::" + deployConfig.server_inner_ip 
				+ ":::" + I18NUtil.SERVER_TYP.WORLD_REDIS_COUNTER 
				+ ":::" + GetAppRemoteRoot() + "/redis_" + deployConfig.redis2_port + "/" 
				+ "\n");
		
		sb.append(deployConfig.zone_id
				+ ":::" + deployConfig.server_inner_ip 
				+ ":::" + I18NUtil.SERVER_TYP.WORLD_REDIS_RANKLIST 
				+ ":::" + GetAppRemoteRoot() + "/redis_" + deployConfig.redis3_port + "/" 
				+ "\n");
		
		sb.append(deployConfig.zone_id
				+ ":::" + deployConfig.server_inner_ip 
				+ ":::" + I18NUtil.SERVER_TYP.WORLD_REDIS_COMMON 
				+ ":::" + GetAppRemoteRoot() + "/redis_" + deployConfig.redis4_port + "/" 
				+ "\n");
		
		return sb.toString();
	}

}
