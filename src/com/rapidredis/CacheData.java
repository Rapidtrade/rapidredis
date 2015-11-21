package com.rapidredis;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rapidredis.extra.IExtra;
import com.rapidtrade.util.Logging;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CacheData {
	private JedisPool jpool;
	private Connection conn;
	private String supplierID;
	private String tableName;
	private String keyField;
	private Jedis jedis;
	
	/**
	 * 
	 * @param jpool
	 * @param conn
	 * @param supplierID
	 * @param tableName
	 */
	public void run(Jedis jedis, Connection conn, String supplierID, String tableName, String keyField, int lastVersion, IExtra extra){
		//this.jpool = jpool;
		this.jedis = jedis; //jpool.getResource();
		this.conn = conn;
		this.supplierID = supplierID;
		this.tableName = tableName;
		this.keyField = keyField;
		
		// Get zero version
		getNext100(0, 0, 600, extra);
		if (lastVersion == 0) return;
		
		// Get changes since the last version
		if (supplierID.equals("DEMO")){
			this.keyField = keyField;
		}
		getNext100(lastVersion, 0, 600, extra);
		
	}
	
	/**
	 * Get data for this version, loop to create JSON objects and add to redis
	 * @param version
	 * @param skip
	 * @param top
	 */
	private void getNext100(int version, int skip, int top, IExtra extra){
		try {
			//* call stored procedure to get new version of data			
			CallableStatement cs = conn.prepareCall("{call cache_" + tableName + "_readlist(?,?,?,?)}");
			cs.setString(1, supplierID);
			cs.setInt(2, version);
			cs.setInt(3, skip);
			cs.setInt(4, top);
			ResultSet rs = cs.executeQuery();
			ResultSetMetaData rmd = rs.getMetaData();
			
			//* loop to create jsonarray
			int cnt = 0;
			while(rs.next()){
				cnt++;
				String keyValue = "";
				
				//* Create a new JsonObject and add the fields to it
				JSONObject jo = new JSONObject();
				for (int x=0; x<rmd.getColumnCount();x++){					
					switch (rmd.getColumnType(x + 1)){
						case 12 :
							if (rmd.getColumnLabel(x + 1).equals(keyField)) 
								keyValue = rs.getString(x + 1);  
							jo.put(rmd.getColumnLabel(x + 1), rs.getString(x + 1));
							break;
						case 4 :
							jo.put(rmd.getColumnLabel(x + 1), rs.getInt(x + 1));
							break;		
						case 3 :
							jo.put(rmd.getColumnLabel(x + 1), rs.getDouble(x + 1));
							break;	
						case -7 :
							jo.put(rmd.getColumnLabel(x + 1), rs.getBoolean(x + 1));
							break;	
						default :
							jo.put(rmd.getColumnLabel(x + 1), rs.getString(x + 1));
					}
				}
				String key = supplierID + "|" + tableName + "|" + keyValue + "|" + version;
				
				//* first check if we already have a zero version for this KeyValue, if we do, then skip this keyValue
				if (cnt == 1 && skip == 0 && version == 0 && jedis.exists(key)) return;
				if (cnt == 1) Logging.logInfo(supplierID + "|version:" + version + "|table:" + tableName + "|skip:" + skip + "|top:" + top);
				
				//* push into redis
				jedis.rpush(key, jo.toString());	//store the json values
				jedis.expire(key, version > 0 ? 518400 : 86400); 			//expire after 1 day
								
				//* if not zero version, then keep record of the version number
				if (version > 0) {
					String versions = "Versions|" + supplierID + "|" + tableName + '|' + keyValue ;
					jedis.sadd  (versions, Integer.toString(version));
					jedis.expire(versions, 518400); 	
				}
				
				//If more data should be saved
				if (extra != null) extra.run(supplierID, jedis, jo);
			}
			
			if (cnt > 298) {
				// recursive call for next 300, if less than 300, this will end recursive read
				skip += 300;
				getNext100(version, skip, top, extra);
			} 
			
		} catch (SQLException e) {
			if (e.getMessage() != "The statement did not return a result set.") Logging.logError("Issue in getNext100", e);
		}
	}
	
	/**
	 * Save
	 * @param key
	 */
	public void saveVersion(String key){
		try {
			CallableStatement cs = conn.prepareCall("{call usp_table_getversion()}");
			ResultSet rs = cs.executeQuery();
			rs.next();
			double ver = rs.getDouble(0);
			jedis.sadd(key, Double.toString(ver));
			jedis.expire(key, 86400);
		} catch (SQLException e) {
			Logging.logError("Issue in storeVersion", e);
		}
	}
}
