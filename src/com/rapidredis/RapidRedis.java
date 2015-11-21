package com.rapidredis;
import java.sql.*;

import com.rapidredis.extra.ExtraAccounts;
import com.rapidredis.extra.ExtraUsers;
import com.rapidtrade.util.Logging;
import com.rapidtrade.util.Properties;
import com.rapidtrade.util.SQL;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RapidRedis {
   	private static JedisPool jpool;
   	
	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;
		Logging.init();
		Logging.logInfo("Initialte JPOOL");
		jpool = new JedisPool(new JedisPoolConfig(), "localhost");
		
		try {
			
			String dbUrl = Properties.getInstance().get("connection");
			conn = SQL.connect(dbUrl);
			
			// get suppliers to cache
			CallableStatement cs = conn.prepareCall("{call cache_suppliers_readlist()}");
			ResultSet rs = cs.executeQuery();
			CacheData cache = new CacheData();
			Jedis jedis = jpool.getResource();
			int lastVersion = jedis.exists("LastVersion") ? Integer.parseInt(jedis.get("LastVersion")) : 0;
			
			while(rs.next()){	
				String supplierID = rs.getString("SupplierID");
				cache.run(jedis, conn, supplierID, "Users", "SupplierID", lastVersion, new ExtraUsers() );
				cache.run(jedis, conn, supplierID, "Accounts", "UserID", lastVersion, new ExtraAccounts() );
				cache.run(jedis, conn, supplierID, "Pricelist", "pl", lastVersion, null);
				cache.run(jedis, conn, supplierID, "Stock", "Warehouse", lastVersion, null);
				cache.run(jedis, conn, supplierID, "CallCycle", "UserID", lastVersion, null);
				cache.run(jedis, conn, supplierID, "Planograms", "UserID", lastVersion, null);
				cache.run(jedis, conn, supplierID, "PlanogramItems", "UserID", lastVersion, null);
				
			}
			
			// Save the latest version for next time
			cs = conn.prepareCall("{call usp_table_getversion2()}");
			rs = cs.executeQuery();
			rs.next();
			jedis.set("LastVersion", rs.getString(1));
			jedis.expire("LastVersion", 518400); //expire in 6 days
			
			Logging.logInfo("Finished");
			rs.close();
		    conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			Logging.logError(e.toString());
		} finally {
			jpool.destroy();
			try{
				if(stmt!=null) stmt.close();
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null) conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}   
	}
}

//callableStatement.setString(1, "param1");
//callableStatement.setInt   (2, 123);

/*
stmt = conn.createStatement();
String sql;
sql = "SELECT top 10 * FROM Users";
ResultSet rs = stmt.executeQuery(sql);
//STEP 5: Extract data from result set
while(rs.next()){
     //Retrieve by column name
     //int id  = rs.getInt("id");
     //int age = rs.getInt("age");
     String name = rs.getString("Name");
     //String last = rs.getString("last");

     //Display values
     System.out.print("ID: " + name);
}
rs.close();
stmt.close();
*/
//STEP 6: Clean-up environment



