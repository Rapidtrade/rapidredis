package com.rapidredis.extra;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

public interface IExtra {
	/**
	 * This method allows you to save additional objects out of the norm
	 * @param jedis
	 * @param jo
	 */
	public void run(String supplierID, Jedis jedis, JSONObject jo);
	
	
	
}
