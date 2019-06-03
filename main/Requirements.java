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
         StringBuilder sb = new StringBuilder("SELECT * FROM lab7_rooms JOIN lab7_reservations ON roomcode = room");
         sb.append(" WHERE checkin <= ? AND checkout >= ? AND maxOcc >= ?");
         
         if (!"any".equalsIgnoreCase(roomCode)) {
            sb.append(" AND roomcode = ?");
            params.add(roomCode);
         }
         
         if (!"any".equalsIgnoreCase(bedType)) {
            sb.append(" AND bedType = ?");
            params.add(bedType);
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

