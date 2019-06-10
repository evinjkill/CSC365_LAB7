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
			StringBuilder sb = new StringBuilder("select RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor, Popularity, NextDateAvailable from");
			sb.append("(select room, round (sum(DATEDIFF(checkout, checkin)) / 180, 2) as Popularity ");
			sb.append("from lab7_rooms as r ");
			sb.append("inner join lab7_reservations as res on r.roomcode = res.room ");
			sb.append("where DATEDIFF(CURDATE(), checkin) < 180 and DATEDIFF(CURDATE(), checkin) > 0 ");
			sb.append("group by room) pop ");
			sb.append("inner join ");
			sb.append("(select room, min(checkout) as NextDateAvailable ");
			sb.append("from lab7_rooms as r ");
			sb.append("inner join lab7_reservations as res on r.roomcode = res.room ");
			sb.append("where checkout >= curdate() ");
			sb.append("group by room) ava ");
			sb.append("on pop.room = ava.room ");
         sb.append("inner join ");
         sb.append("(select * from lab7_rooms) full ");
         sb.append("on full.RoomCode = ava.room; ");
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
   
   public void requirement2() throws SQLException {
      
      try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                                        System.getenv("HP_JDBC_USER"),
                                                        System.getenv("HP_JDBC_PW"))) {
         Scanner sc = new Scanner(System.in);
         System.out.print("Enter a first name: ");
         String firstName = sc.nextLine();
         
         System.out.print("Enter a last name: ");
         String lastName = sc.nextLine();
         
         System.out.print("Enter a room code (or 'Any' for no preference): ");
         String roomCode = sc.nextLine();
         
         System.out.print("Enter a bed type (or 'Any' for no preference): ");
         String bedType = sc.nextLine();
         
         System.out.print("Start date of stay (yyyy-mm-dd): ");
         String startDate = sc.nextLine();
         
         System.out.print("End date of stay (yyyy-mm-dd): ");
         String endDate = sc.nextLine();
         
         System.out.print("Number of children during stay: ");
         int numChildren = Integer.valueOf(sc.nextLine());
         
         System.out.print("Number of adults during stay: ");
         int numAdults = Integer.valueOf(sc.nextLine());
         
         int occupancy = numChildren + numAdults;
         
         List<Object> params = new ArrayList<Object>();
         params.add(endDate);
         params.add(startDate);
         params.add(occupancy);
         //StringBuilder sb = new StringBuilder("SELECT * FROM lab7_rooms JOIN lab7_reservations ON roomcode = room");
         //sb.append(" WHERE checkin <= ? AND checkout >= ? AND maxOcc >= ?");
         String query = "SELECT * FROM lab7_rooms WHERE roomcode NOT IN (SELECT roomcode FROM lab7_rooms";
         query += "JOIN lab7_reservations ON roomcode = room WHERE checkin <= ? AND checkout >= ? and maxOcc >= ?";
         StringBuilder sb = new StringBuilder(query);

         if (!"any".equalsIgnoreCase(bedType)) {
            sb.append(" AND bedType = ?");
            params.add(bedType);
         }
         sb.append(")");

         if (!"any".equalsIgnoreCase(roomCode)) {
            sb.append(" AND roomcode = ?");
            params.add(roomCode);
         }
         
         try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object p : params) {
               pstmt.setObject(i++, p);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
               
               System.out.println("Matching Rooms:");
               int matchCount = 0;
               
               while (rs.next()) {
                  //System.out.format("%s %s ($%.2f) %n", rs.getString("Flavor"), rs.getString("Food"), rs.getDouble("price"));
                  matchCount++;
               }
               
               if (matchCount == 0) {
                  System.out.println("No matches found!");
                  //rs = find_similar_types(roomCode, bedType, endDate, startDate, occupancy);
               }
            }
         }
      }
   }
   
   private ResultSet find_similar_types(String roomCode, String bedType, String endDate, String startDate, int occupancy) {
      // This will create a more intelligent search that includes similar rooms
      // and returns the result set from the query
      return null;
   }

}

