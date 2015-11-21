package com.rapidtrade.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQL {
	private static Connection conn;
	public static Connection connect(String dbUrl) throws Exception {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		conn = DriverManager.getConnection(dbUrl);
		Logging.logInfo("Connecting to " + dbUrl);
		return conn;
	}

}
