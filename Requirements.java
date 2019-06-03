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
		    String sql = "SELECT * FROM lab7_rooms INNER JOIN lab7_reservations ON RoomCode = Room";

		    // Step 3: (omitted in this example) Start transaction

		    // Step 4: Send SQL statement to DBMS
		    try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			// Step 5: Receive results
			while (rs.next()) {
				String RoomCode = rs.getString("RoomCode");
				String RoomName = rs.getString("RoomName");
				int Beds = rs.getInt("Beds");
				String bedType = rs.getString("bedType");
				int maxOcc = rs.getInt("maxOcc");
				float basePrice = rs.getFloat("basePrice");
				String decor = rs.getString("decor");
				int Popularity = rs.getInt("Popularity");
				String NextDateAvailable = rs.getString("NextDateAvailable");
				System.out.format("%s %s %i %s %i %f %s %i %s", RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor, Popularity, NextDateAvailable);
			}
		    }

		}

	    }

}

