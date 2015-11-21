package com.rapidtrade.util;


import java.io.File;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import com.rapidredis.RapidRedis;

public class Logging {
	static private FileHandler fileTxt;
	static private SimpleFormatter formatterTxt;
	static boolean canLog = true;
	private final static Logger LOGGER = Logger.getLogger(RapidRedis.class .getName()); 
	private static String logFile;
	
	private static void msgbox(String msg){
		JOptionPane.showMessageDialog(null, msg);
	}
	
	static public void init()  {
		try {
			//File jarFile = new File(Setup.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			File logFolder = new File(new java.io.File( "." ).getCanonicalPath()  + "/Logs"); //jarFile.getAbsoluteFile() + "/Logs");
			if (Properties.getInstance().get("logfolder") != null) {
				logFolder = new File(Properties.getInstance().get("logfolder"));
			}
			if (!logFolder.isDirectory())
				(logFolder).mkdirs();
			
			Calendar dte =  Calendar.getInstance();
			fileTxt = new FileHandler(logFolder.getAbsolutePath() + "/RapidBISDK_" + dte.get(Calendar.YEAR) + (dte.get(Calendar.MONTH)+1) + dte.get(Calendar.DAY_OF_MONTH)+ "_" + dte.get(Calendar.HOUR_OF_DAY) + "h" + dte.get(Calendar.MINUTE)  + ".log");
			//+ "_" + dte.get(Calendar.HOUR_OF_DAY) + "h" + dte.get(Calendar.MINUTE)
			
			// Create txt Formatter
			formatterTxt = new SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);
			LOGGER.addHandler(fileTxt);
		} catch (Exception ex) {
			msgbox(ex.getMessage());
			canLog = false;
		}
	}	
	
	
	
	static public void logError(String msg){
		LOGGER.setLevel(Level.SEVERE);
		LOGGER.severe(msg);
	}

	static public void logError(String msg, Exception ex){
		LOGGER.setLevel(Level.SEVERE);
		LOGGER.severe(msg + ex.getMessage());
	}

	static public void logInfo(String msg){
		LOGGER.setLevel(Level.INFO);
		LOGGER.info(msg);
	}
	static public void logWarning(String msg){
		LOGGER.setLevel(Level.WARNING);
		LOGGER.warning(msg);
	}



	public static String getLogFile() {
		return logFile;
	}
}