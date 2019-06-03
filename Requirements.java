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
			StringBuilder sb = new StringBuilder("select pop.room, Popularity, NextAvailableDate from ");
			sb.append("(select room, round (sum(DATEDIFF(checkout, checkin)) / 180, 2) as Popularity ");
			sb.append("from lab7_rooms as r ");
			sb.append("inner join lab7_reservations as res on r.roomcode = res.room ");
			sb.append("where DATEDIFF(CURDATE(), checkin) < 180 and DATEDIFF(CURDATE(), checkin) > 0 ");
			sb.append("group by room) pop ");
			sb.append("inner join ");
			sb.append("(select room, min(checkout) as NextAvailableDate ");
			sb.append("from lab7_rooms as r ");
			sb.append("inner join lab7_reservations as res on r.roomcode = res.room ");
			sb.append("where checkout >= curdate() ");
			sb.append("group by room) ava ");
			sb.append("on pop.room = ava.room;");

		    // Step 3: (omitted in this example) Start transaction

		    // Step 4: Send SQL statement to DBMS
		    try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
		    	try(ResultSet rs = pstmt.executeQuery()) {

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
					System.out.format("%s %s %d %s %d %f %s %d %s", RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor, Popularity, NextDateAvailable);
				}
		    }

		}
		}
	    }

}

