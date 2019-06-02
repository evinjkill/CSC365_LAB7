package main;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;


public class Requirements {
	
	public void requirement1() throws SQLException {

		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
								   System.getenv("HP_JDBC_USER"),
								   System.getenv("HP_JDBC_PW"))) {
		    // Step 2: Construct SQL statement
		    String sql = "SELECT * FROM lab7_rooms";

		    // Step 3: (omitted in this example) Start transaction

		    // Step 4: Send SQL statement to DBMS
		    try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			// Step 5: Receive results
			while (rs.next()) {
			
			}
		    }

		}

	    }

}

