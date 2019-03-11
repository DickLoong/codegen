package com.lizongbo.codegentool.csv2db;

public class ServerDeployConfig {

	public int zone_id;
	
	public int game_server_memory;
	public int frame_sync_server_memory;
	public int scene_map_server_memory;
	public int redis_memory;
	public String server_inner_ip;
	public String server_public_ip;
	public int game_server_port;
	public int game_server_jmx_port;
	public int game_server_hessian_port;
	public int frame_sync_server1_port;
	public int frame_sync_server1_jmx_port;
	public int frame_sync_server1_hessian_port;
	public int frame_sync_server2_port;
	public int frame_sync_server2_jmx_port;
	public int frame_sync_server2_hessian_port;
	public int frame_sync_server3_port;
	public int frame_sync_server3_jmx_port;
	public int frame_sync_server3_hessian_port;
	public int frame_sync_server4_port;
	public int frame_sync_server4_jmx_port;
	public int frame_sync_server4_hessian_port;
	public int scene_map_server_port;
	public int secne_map_server_jmx_port;
	public int secne_map_server_hessian_port;
	
	public int warzone_id;
	
	/**
	 * 帧同步服务器的数量
	 */
	public int frameServerNum;
	
	/**
	 * dbcache
	 */
	public int redis1_port;
	
	/**
	 * counter
	 */
	public int redis2_port;
	
	/**
	 * ranklist
	 */
	public int redis3_port;
	
	/**
	 * common
	 */
	public int redis4_port;
	
	/**
	 * 是否显示玩家服的标志位
	 */
	public int idShow;
	
	public String getDbName(){
		if (zone_id == 0){
			return "mgamedb_gecaoshoulie";
		}
		
		return "mgamedb_gecaoshoulie_maindb_zone" + zone_id;
	}
}
