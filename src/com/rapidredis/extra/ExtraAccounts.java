package com.rapidredis.extra;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

public class ExtraAccounts implements IExtra {
	
	/**
	 * Create two SET's
	 * 1. A set which lists all the pricelists the user can see.
	 * 2. A set which lists all the branchid's/warehouses the user can see
	 */
	@Override
	public void run(String supplierID, Jedis jedis, JSONObject jo) {
		
		String key = supplierID + "|UserPricelist|" + jo.getString("UserID");
		jedis.sadd(key, jo.getString("Pricelist"));
		
		key = supplierID + "|UserStock|" + jo.getString("UserID");
		jedis.sadd(key, jo.getString("BranchID"));
	}

	

}
