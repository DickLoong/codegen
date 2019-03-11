package com.lizongbo.codegentool.world.porter;

import java.io.File;
import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.SCPUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;
import com.lizongbo.codegentool.world.ScpCommandUtil;

public class MonitorCenterPorter extends CPorter {

	public MonitorCenterPorter(String worldName) {
		this.worldName = worldName;
	}

	/**
	 * 构建监控中心
	 */
	@Override
	public void BuildVersion() {
		long startTime = System.currentTimeMillis();
		
		//执行ant build
		String buildXmlPath = ServerLocalCfgPath() + "/WEB-INF/build.xml";
		ServerContainerGenTool.runShellCmd("/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + buildXmlPath);
		
		LogUtil.printLog(this + "|BuildVersion|completely|use|time|" + (System.currentTimeMillis() - startTime));
	}

	/**
	 * 发布监控中心,上传到版本服务器
	 */
	@Override
	public void DeployVersion() {
		long startTime = System.currentTimeMillis();
		
		ScpMonitorJarFile(worldName);
		
		scpMonitorOtherFiles(worldName);
		
		LogUtil.printLog("DeployVersion|complete|use|time|" + (System.currentTimeMillis() - startTime));
	}

	/**
	 * 复制监控中心的jar包到版本服务器
	 */
	private void ScpMonitorJarFile(String worldName){
		long startTime = System.currentTimeMillis();
		
		//复制监控中心的jar包
		String monitorCenterJarLocalFile = ServerLocalCfgPath() + "/WEB-INF/dist/gecaoshoulie_monitor_center.jar";
		String monitorCenterJarRemoteFile = "/gecaoshoulie_monitor_center.jar";
		ScpCommandUtil.scpWorldFileToVersionServer(worldName, monitorCenterJarLocalFile, monitorCenterJarRemoteFile);
		
		LogUtil.printLog("ScpMonitorJarFile|complete|use|time|" + (System.currentTimeMillis() - startTime));
	}
	
	/**
	 * 上传.txt有关的文件
	 * 添加一个启动脚本 bl_start.sh
	 * 目录结构在/data/bilin/monitor_center
	 */
	private void scpMonitorOtherFiles(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String cmdHost = deployProp.getProperty("releaseVersionHost");
		
		long startTime = System.currentTimeMillis();
		
		String requestList = ServerLocalCfgPath() + "/WEB-INF/src/net/bilinkeji/gecaoshoulie/mgameprotorpc/MonitorCenter/request_list.txt";
		SCPUtil.doSCPTo(requestList, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				cmdHost,
				GetAppRemoteRoot() + "/" + new File(requestList).getName());
		
		//也先将文件上传到目录下.因为现在文件并不大.所以这样来处理没问题.
		String monitorCenterJarLocalFile = ServerLocalCfgPath() + "/WEB-INF/dist/gecaoshoulie_monitor_center.jar";
		String monitorCenterJarRemoteFile = GetAppRemoteRoot() + "/gecaoshoulie_monitor_center.jar";
		SCPUtil.doSCPTo(monitorCenterJarLocalFile, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				cmdHost,
				monitorCenterJarRemoteFile);
		
		// kill.sh
		String killShLocalFile = genKillSh();
		String killShRemoteFile = GetAppRemoteRoot() + "/bin/kill.sh";
		SCPUtil.doSCPTo(killShLocalFile, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				cmdHost,
				killShRemoteFile);
		DeployCommandUtil.chmodAddX(worldName, cmdHost, killShRemoteFile);
		
		// bl_start.sh
		String startShLocalFile = genStartSh();
		String startShRemoteFile = GetAppRemoteRoot() + "/bin/bl_start.sh";
		SCPUtil.doSCPTo(startShLocalFile, 
				deployProp.getProperty("releaseVersionUser"), 
				deployProp.getProperty("releaseVersionPwd"), 
				cmdHost,
				startShRemoteFile);
		DeployCommandUtil.chmodAddX(worldName, cmdHost, startShRemoteFile);
		
		LogUtil.printLog("scpMonitorOtherFiles|complete|use|time|" + (System.currentTimeMillis() - startTime));
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"), 
				startShRemoteFile);
	}
	
	/**
	 * 在本地生成bl_start.sh,之后上传到远程机器
	 * @return 本地路径
	 */
	private String genStartSh(){
		String bl_startShText = "#!/bin/sh\n\n" 
				+ "CRTBINDIR=\"$(cd $(dirname $0) && pwd)\"\n" 
				+ "JAVA_HOME=/data/bilin/apps/jdk\n"
				+ "sh $CRTBINDIR/kill.sh\n"
		 
				+ "echo \"CRTBINDIR	=$CRTBINDIR\"\n"
		
				+ "run_cmd=\"nohup $JAVA_HOME/bin/java -Djavaserver.home=" + GetAppRemoteRoot() 
				+ " -cp \\\"" + GetAppRemoteRoot() + "/gecaoshoulie_monitor_center.jar:/data/bilin/release_version/" + worldName + "/latest/gecaoshoulie_servercodegen.jar:/data/bilin/software/javalib4server/*\\\""
				+ " " + MainServerClassName() + "\"\n"
				+ "echo \"try run $run_cmd\"\n\n"
				
				+ "nohup $JAVA_HOME/bin/java -Djavaserver.home=" + GetAppRemoteRoot() 
				+ " -cp \"" + GetAppRemoteRoot() + "/gecaoshoulie_monitor_center.jar:/data/bilin/release_version/" + worldName + "/latest/gecaoshoulie_servercodegen.jar:/data/bilin/software/javalib4server/*\""
				+ " " + MainServerClassName() + " > " + GetAppRemoteRoot() + "/monitor_center.txt 2>&1 &\n\n";

		String localFile = ServerLocalCfgPath() + "/WEB-INF/dist/bl_start.sh";
		GameCSV2DB.writeFile(localFile, bl_startShText);
		
		return localFile;
	}
	
	/**
	 * 在本地生成kill.sh,之后上传到远程机器
	 * @return 本地路径
	 */
	private String genKillSh(){
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");
		killShText = ServerContainerGenTool.replaceAll(killShText, "net.binlin.MainClass", MainServerClassName());
		
		String localFile = ServerLocalCfgPath() + "/WEB-INF/dist/kill.sh";
		
		GameCSV2DB.writeFile(localFile, killShText);
		
		return localFile;
	}
	

	@Override
	protected String GetAppRemoteRoot() {
		return "/data/bilin/monitor_center";
	}

	@Override
	protected String FrameServerRemoteRoot(int seq) {
		return null;
	}

	@Override
	protected String[] GetBinPaths() {
		return null;
	}

	@Override
	protected String ServerLocalCfgPath() {
		return "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_monitor_center";
	}

	@Override
	protected String MainServerClassName() {
		return "net.bilinkeji.gecaoshoulie.mgameprotorpc.MonitorCenter.MonitorCenter";
	}

	@Override
	protected String MainServerLocalCfgPath() {
		return null;
	}

	@Override
	protected SERVER_TYP MainServerType() {
		return null;
	}

	@Override
	protected int MainServerPort() {
		return 0;
	}

	@Override
	protected String FrameSyncServerLoalCfgPath(int seq) {
		return null;
	}

	@Override
	protected SERVER_TYP GetFrameSyncServerTypeBySeq(int seq) {
		return null;
	}

	@Override
	protected void ImportDatabase() {
	}

}
