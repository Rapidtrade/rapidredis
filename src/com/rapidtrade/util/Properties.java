package com.rapidtrade.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Properties {
	static java.util.Properties prop = new java.util.Properties();
	static Properties properties;
	
	private void loadProperties(){
		try {
			String path = "/etc/rapidredis/";
			InputStream input = null;
			File f = new File("RapidRedis.properties");
			if (!f.exists()){
				f = new File(path + "RapidRedis.properties");
				if (!f.exists()) System.out.println(path + "RapidRedis.properties no found");
			}
			input = new FileInputStream(f);
			prop.load(input);
		} catch (Exception e){
			Logging.logError(e.getMessage());
		}
	}
	
	public static Properties getInstance() {
		if (properties == null) {
			properties = new Properties();
			try {
				properties.loadProperties();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return properties;	
	}
	
	public String get(String name){
		return prop.getProperty(name);
	}
	
	/*
	public  void save(){
		Properties prop = new Properties();
		OutputStream output = null;
		
		Properties configFile = new Properties();
		try {
			output = new FileOutputStream("config.properties");
			
			//configFile.load(this.getClass().getClassLoader().getResourceAsStream("/rapidbi.properties"));
			prop.put("supplierid", currentSupplierID);
			prop.put("userid", currentUserID);
			prop.put("password", currentPassword);
			prop.put("connection", currentConnection);
			prop.store(output, null);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
}
