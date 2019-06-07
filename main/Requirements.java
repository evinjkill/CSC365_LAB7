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

import java.time.LocalDate;
import java.time.DayOfWeek;

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
        
         String startDate, endDate;
         boolean is_valid = false;
         do {
            System.out.print("Start date of stay (yyyy-mm-dd): ");
            startDate = sc.nextLine();
         
            System.out.print("End date of stay (yyyy-mm-dd): ");
            endDate = sc.nextLine();

            is_valid = check_dates(startDate, endDate);

            if (!is_valid)
               System.out.println("Duration of stay is not valid!\n");
         } while (!is_valid);

         System.out.print("Number of children during stay: ");
         int numChildren = Integer.valueOf(sc.nextLine());
         
         System.out.print("Number of adults during stay: ");
         int numAdults = Integer.valueOf(sc.nextLine());
         
         int occupancy = numChildren + numAdults;
         
         List<Object> params = new ArrayList<Object>();
         params.add(endDate);
         params.add(startDate);
         StringBuilder sb = new StringBuilder(
            "SELECT * FROM lab7_rooms WHERE roomcode NOT IN (SELECT DISTINCT roomcode FROM lab7_rooms" +
            " JOIN lab7_reservations ON roomcode = room WHERE checkin <= ? AND checkout >= ?)"
         );

         if (!"any".equalsIgnoreCase(bedType)) {
            sb.append(" AND bedType = ?");
            params.add(bedType);
         }

         if (!"any".equalsIgnoreCase(roomCode)) {
            sb.append(" AND roomcode = ?");
            params.add(roomCode);
         }

         conn.setAutoCommit(false);
         
         try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object p : params) {
               pstmt.setObject(i++, p);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
               
               System.out.println("Matching Rooms:");
               int matchCount = 0;
               ArrayList<Room> availRooms = new ArrayList<>();

               while (rs.next()) {
                  if (rs.getInt("maxOcc") < (numAdults + numChildren)) {
                     System.out.format("   %s %s %n", rs.getString("roomcode"), " room size exceeded");
                  }
                  else {
                     Room r = new Room(rs.getString("roomcode"), rs.getString("roomname"), rs.getInt("beds"),
                                       rs.getString("bedtype"), rs.getInt("maxocc"), rs.getDouble("basePrice"),
                                       rs.getString("decor"));
                     availRooms.add(r);
                     System.out.format("%d: %s %n", matchCount + 1, rs.getString("roomcode"));
                     matchCount++;
                  }
               }
               
               if (matchCount == 0) {
                  System.out.println("No matches found!");
                  //rs = find_similar_types(roomCode, bedType, endDate, startDate, occupancy);
               }

               
               System.out.print("Select a room by option number (or 0 to cancel): ");
               int option = Integer.valueOf(sc.nextLine());

               if (option == 0) return;
               Room r = availRooms.get(option - 1);

               System.out.printf("Confirm reservation: %n" +
                  "%s %s%nRoom: %s (%s), bed: %s%nCheck in: %s, Check out: %s%nAdults: %d, Children: %d%nTotal Cost: %.2f" +
                  "%n[y/n]: ",
                     firstName, lastName, r.getRoomCode(), r.getRoomName(), r.getBedType(),
                     startDate, endDate, numAdults, numChildren,
                     total_cost(startDate, endDate, r.getBasePrice()));

               if ("y".equalsIgnoreCase(sc.nextLine())) {
                  String query = "INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName," +
                     " FirstName, Adults, Kids) VALUES (?, ?, DATE(?), DATE(?), ?, ?, ?, ?, ?)";

                  List<Object> reserve_params = new ArrayList<Object>();
                  reserve_params.add(get_max_code(conn) + 1);
                  reserve_params.add(r.getRoomCode());
                  reserve_params.add(startDate);
                  reserve_params.add(endDate);
                  reserve_params.add(r.getBasePrice());
                  reserve_params.add(lastName.toUpperCase());
                  reserve_params.add(firstName.toUpperCase());
                  reserve_params.add(numAdults);
                  reserve_params.add(numChildren);

                  reserve_room(query, reserve_params, conn);
               }
            }

            conn.commit();
         }
         catch (SQLException e) {
            conn.rollback();
         }
      }
   }

   private void reserve_room(String query, List<Object> params, Connection conn) {

      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
         int i = 1;
         for (Object p : params) {
            pstmt.setObject(i++, p);
         }

         int rows = pstmt.executeUpdate();
         System.out.printf("Reservation %s!\n", rows > 0 ? "successful" : "failed");
      }
      catch (SQLException e) {
         System.err.println("SQLException: " + e.getMessage());
      }
   }

   private int get_max_code(Connection conn) {
      String query = "SELECT MAX(CODE) MAXCODE FROM lab7_reservations";

      try (Statement stmt = conn.createStatement()) {
         ResultSet rs = stmt.executeQuery(query);

         while (rs.next()) {
            return rs.getInt("MAXCODE");
         }

         return 0;
      }
      catch (SQLException e) {
         System.err.println("SQLException: " + e.getMessage());
      }

      return 0;
   }

   private boolean check_dates(String start, String end) {
      String match = "\\d{4}-\\d{2}-\\d{2}";
      if (!start.matches(match) || !end.matches(match))
         return false;

      String[] start_split = start.split("-");
      String[] end_split = end.split("-");

      int year_diff = Integer.valueOf(end_split[0]) - Integer.valueOf(start_split[0]);

      if (year_diff < 0)
         return false;
      else if (year_diff == 0) {
         int month_diff = Integer.valueOf(end_split[1]) - Integer.valueOf(start_split[1]);
         
         if (month_diff < 0)
            return false;
         else if (month_diff == 0) {
            int day_diff = Integer.valueOf(end_split[2]) - Integer.valueOf(start_split[2]);

            if (day_diff < 0)
               return false;
         }
      }

      return true;
   }

   private double total_cost(String checkin, String checkout, double basePrice) {
      double cost = 0;
      int numWeekend = 0;
      int numWeekdays = 0;
      LocalDate start = LocalDate.parse(checkin);
      LocalDate end = LocalDate.parse(checkout);

      while (start.compareTo(end) != 0) {
         if (start.getDayOfWeek() == DayOfWeek.SATURDAY ||
             start.getDayOfWeek() == DayOfWeek.SUNDAY)
            numWeekend++;
         else
            numWeekdays++;

         start = start.plusDays(1);
      }

      cost += numWeekdays * basePrice;
      cost += numWeekend * (1.1 * basePrice);
      cost *= 1.18;
      return cost;
   }

   private ResultSet find_similar_types(String roomCode, String bedType, String endDate, String startDate, int occupancy) {
      // This will create a more intelligent search that includes similar rooms
      // and returns the result set from the query
      return null;
   }

}

