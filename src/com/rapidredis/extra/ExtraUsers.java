package com.rapidredis.extra;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

public class ExtraUsers implements IExtra {
	/**
	 * Get the roles for this user and save then in <SupplierID>|UserRoles<UserID>
	 */
	@Override
	public void run(String supplierID, Jedis jedis, JSONObject jo) {
		try {
			String sroles = jo.getString("Roles");
			if (sroles == null) return;
			String[] roles = sroles.split(",");
			if (roles.length == 0) return;
			
			String key = supplierID + "|UserRoles|" + jo.getString("UserID");
			for (int x=0; x< roles.length; x++){
				if (roles[x].length() > 0) jedis.sadd(key, roles[x]);
			}			
		} catch (Exception ex){
			
		}
		
	}

	
}
