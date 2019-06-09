package main;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
   
   private static String
         ADD_ONE_STR = "Add 1 Week",
         ADD_TWO_STR = "Add 2 Weeks",
         SAME_DATE_STR = "Same Date",
         SAME_BED_STR = "Same Bed",
         SEARCH_METHOD = "SearchMethod";
	
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
         
         R2Query r2 = new R2Query();
         
         List<Object> params = new ArrayList<Object>();
         params.add(r2.endDate);
         params.add(r2.startDate);
         StringBuilder sb = new StringBuilder(
            "SELECT * FROM lab7_rooms WHERE roomcode NOT IN (SELECT DISTINCT roomcode FROM lab7_rooms" +
            " JOIN lab7_reservations ON roomcode = room WHERE checkin <= ? AND checkout >= ?)"
         );

         if (!"any".equalsIgnoreCase(r2.bedType)) {
            sb.append(" AND bedType = ?");
            params.add(r2.bedType);
         }

         if (!"any".equalsIgnoreCase(r2.roomCode)) {
            sb.append(" AND roomcode = ?");
            params.add(r2.roomCode);
         }

         // start transaction
         conn.setAutoCommit(false);
         
         try {
            // Try finding any matching room
            List<Room> availRooms = execute_query(conn, r2, sb.toString(), params);
            // if no matching rooms found, try an advanced query
            if (availRooms.isEmpty()) {
               System.out.println("No matches found!");
               System.out.println("Performing advanced search...");
               params.clear();
               String query = create_advanced_query(r2, params);
               availRooms = execute_query(conn, r2, query, params);

               if (availRooms.isEmpty()) {
                  System.out.println("No matches found!");
                  return;
               }
            }
               
            System.out.print("\nSelect a room by option number (or 0 to cancel): ");
            Scanner sc = new Scanner(System.in);
            int option = Integer.valueOf(sc.nextLine());

            if (option == 0) return;
               
            Room r = availRooms.get(option - 1);
            System.out.printf("Confirm reservation: %n" +
               "%s %s%nRoom: %s (%s), bed: %s%nCheck in: %s, Check out: %s%nAdults: %d, Children: %d%nTotal Cost: %.2f" +
               "%n[y/n]: ",
                  r2.firstName, r2.lastName, r.getRoomCode(), r.getRoomName(), r.getBedType(),
                  r.getStart(), r.getEnd(), r2.numAdults, r2.numChildren,
                  total_cost(r.getStart(), r.getEnd(), r.getBasePrice()));

            if ("y".equalsIgnoreCase(sc.nextLine())) {
               reserve_room(r2, r, conn);
            }

            conn.commit();
         }
         catch (SQLException e) {
            conn.rollback();
            System.err.println("SQLException: " + e.getMessage());
         }
      }
   }

   public void requirement3() throws SQLException {
      try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                                        System.getenv("HP_JDBC_USER"),
                                                        System.getenv("HP_JDBC_PW"))) {
         Scanner sc = new Scanner(System.in);
         System.out.print("Enter your reservation code: ");
         String resCode = sc.nextLine();

         System.out.println("Fill in new values for each of the following fields\n(ENTER for no change)\n");

         System.out.print("First name: ");
         String firstName = sc.nextLine();

         System.out.print("Last name: ");
         String lastName = sc.nextLine();

         System.out.print("Begin date (mm-dd-yyyy): ");
         String startDate = sc.nextLine();

         System.out.print("End date (mm-dd-yyyy): ");
         String endDate = sc.nextLine();

         System.out.print("Number of children: ");
         int numChildren = Integer.valueOf(sc.nextLine());

         System.out.print("Number of adults: ");
         int numAdults = Integer.valueOf(sc.nextLine());

         List<Object> params = new ArrayList<Object>();

         String query = "UPDATE lab7_leservations SET ";

         if (!"".equalsIgnoreCase(firstName)) {
            sb.append("FirstName = ?, ");
            params.add(firstName);
         }

         if (!"".equalsIgnoreCase(lastName)) {
            sb.append("LastName = ?, ");
            params.add(lastName);
         }

         if (!"".equalsIgnoreCase(startDate)) {
            sb.append("CheckIn = ?, ");
            params.add(startDate);
         }

         if (!"".equalsIgnoreCase(endDate)) {
            sb.append("CheckOut = ?, ");
            params.add(endDate);
         }
         
         sb.append("WHERE CODE = ?;");
         params.add(resCode);

         try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object p : params) {
               pstmt.setObject(i++, p);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
               

            }
         }


   }
   
   // returns true if any rooms were found
   private List<Room> execute_query(Connection conn, R2Query r2, String query, List<Object> params) throws SQLException {
      ArrayList<Room> availRooms = new ArrayList<>();
      
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
         int i = 1;
         for (Object p : params) {
            pstmt.setObject(i++, p);
         }
         
         // Try finding any matching room
         try (ResultSet rs = pstmt.executeQuery()) {
            Scanner sc = new Scanner(System.in);
            System.out.println("\nMatching Rooms:");

            if (has_column(rs, SEARCH_METHOD))
               get_adv_available_rooms(rs, r2, availRooms);
            else
               get_available_rooms(rs, r2, availRooms);
         }
      }
      
      return availRooms;
   }

   private boolean has_column(ResultSet rs, String col_name) throws SQLException {
      ResultSetMetaData rsmd = rs.getMetaData();
      int columns = rsmd.getColumnCount();

      for (int i = 1; i < columns; i++) {
         if (col_name.equals(rsmd.getColumnName(i)))
            System.out.println("true");
            return true;
      }
      System.out.println("false");
      return false;
   }
   
   /*
    * Gets all available rooms based on the user's choices
    */
   private void get_available_rooms(ResultSet rs, R2Query r2, List<Room> availRooms) throws SQLException {
      int matchCount = 0;
      
      while (rs.next()) {
         if (rs.getInt("maxOcc") < (r2.numAdults + r2.numChildren)) {
            System.out.format("   %s %s %n", rs.getString("roomcode"), " room size exceeded");
         }
         else {
            Room r;

            r = new Room(rs.getString("roomcode"), rs.getString("roomname"), rs.getInt("beds"),
                           rs.getString("bedtype"), rs.getInt("maxocc"), rs.getDouble("basePrice"),
                           rs.getString("decor"), r2.startDate, r2.endDate);

            availRooms.add(r);
            System.out.format("%d: %s %n", matchCount + 1, rs.getString("roomcode"));
            matchCount++;
         }
      }
   }

   /*
    * Advanced query which takes into account that the reservation date can change
    */
   private void get_adv_available_rooms(ResultSet rs, R2Query r2, List<Room> availRooms) throws SQLException {
      int matchCount = 0;

      while (rs.next()) {
         if (rs.getInt("maxocc") < (r2.numAdults + r2.numChildren))
            System.out.format("   %s %s %n", rs.getString("roomcode"), " room size exceeded");
         else {
            Room r = new Room(rs.getString("roomcode"), rs.getString("roomname"), rs.getInt("beds"),
                              rs.getString("bedtype"), rs.getInt("maxocc"), rs.getDouble("basePrice"),
                              rs.getString("decor"), r2.startDate, r2.endDate);

            if (rs.getString(SEARCH_METHOD).equals(ADD_ONE_STR)) {
               r.increaseResStartDay(7);
            }
            else if (rs.getString(SEARCH_METHOD).equals(ADD_TWO_STR)) {
               r.increaseResStartDay(14);
            }
            availRooms.add(r);
            System.out.format("%d: %s (%s to %s)%n", matchCount + 1, rs.getString("roomcode"), r.getStart(), r.getEnd());
            matchCount++;
         }
      }
   }

   private void reserve_room(R2Query r2, Room room, Connection conn) {
      String query = "INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName," +
                     " FirstName, Adults, Kids) VALUES (?, ?, DATE(?), DATE(?), ?, ?, ?, ?, ?)";
                  
      List<Object> params = new ArrayList<Object>();
      params.add(get_max_code(conn) + 1);
      params.add(room.getRoomCode());
      params.add(room.getStart());
      params.add(room.getEnd());
      params.add(room.getBasePrice());
      params.add(r2.lastName.toUpperCase());
      params.add(r2.firstName.toUpperCase());
      params.add(r2.numAdults);
      params.add(r2.numChildren);

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

   /*
    * Creates an advanced query that looks for all rooms available
    * at the requested time and 1 and 2 weeks ahead of the requested date.
    * Also looks for any rooms with matching bed type
    */
   private String create_advanced_query(R2Query r2, List<Object> params) {
      // This will create a more intelligent search that includes similar
      StringBuilder sb = new StringBuilder();
      
      // Finds all rooms available in 1 week (or just the requested one)
      String searchAddOneWeek = "SELECT *, " + "'" + ADD_ONE_STR + "'" + " SearchMethod FROM lab7_rooms" +
         " WHERE roomcode NOT IN (" +
         "SELECT roomcode FROM lab7_rooms JOIN lab7_reservations ON roomcode = room" +
         " WHERE checkin <= DATE_ADD(?, INTERVAL 7 DAY) AND checkout >= DATE_ADD(?, INTERVAL 7 DAY))";
      params.add(r2.endDate);
      params.add(r2.startDate);
      
      sb.append(searchAddOneWeek);
      sb.append(" UNION ");
      
      // Finds all rooms available in 2 weeks (or just the requested one)
      String searchAddTwoWeek = "SELECT *, " + "'" + ADD_TWO_STR + "'" + " SearchMethod FROM lab7_rooms" +
      " WHERE roomcode NOT IN (" +
         "SELECT roomcode FROM lab7_rooms JOIN lab7_reservations ON roomcode = room" +
         " WHERE checkin <= DATE_ADD(?, INTERVAL 14 DAY) AND checkout >= DATE_ADD(?, INTERVAL 14 DAY))";
      params.add(r2.endDate);
      params.add(r2.startDate);
         
      sb.append(searchAddTwoWeek);
      
      // Expanding roomcode search takes precedence over bed type
      if (!"any".equalsIgnoreCase(r2.roomCode)) {
         // Finds all same date rooms
         String searchMatchSame = "SELECT *, " + "'" + SAME_DATE_STR + "'" + " SearchMethod FROM lab7_rooms" +
            " WHERE roomcode NOT IN (" +
            "SELECT roomcode FROM lab7_rooms JOIN lab7_reservations ON roomcode = room" +
            " WHERE checkin <= ? AND checkout >= ?)";
         params.add(r2.endDate);
         params.add(r2.startDate);

         sb.append(" UNION ");
         sb.append(searchMatchSame);
      }
      else if (!"any".equalsIgnoreCase(r2.bedType)) {
         // Finds same date rooms that match the bed type
         String searchMatchBed = "SELECT *, " + "'" + SAME_BED_STR + "'" + " SearchMethod FROM lab7_rooms" +
            " WHERE roomcode NOT IN (" +
            "SELECT roomcode FROM lab7_rooms JOIN lab7_reservations ON roomcode = room" +
            " WHERE checkin <= ? AND checkout >= ?) AND bedtype = ?";
         params.add(r2.endDate);
         params.add(r2.startDate);
         params.add(r2.bedType);

         sb.append(" UNION ");
         sb.append(searchMatchBed);
      }
      
      return sb.toString();
   }

   private static class R2Query {
      String firstName;
      String lastName;
      String roomCode;
      String bedType;
      String startDate;
      String endDate;
      int numChildren;
      int numAdults;
      int occupancy;
      
      R2Query() {
         Scanner sc = new Scanner(System.in);
         System.out.print("Enter a first name: ");
         firstName = sc.nextLine();
         
         System.out.print("Enter a last name: ");
         lastName = sc.nextLine();
         
         System.out.print("Enter a room code (or 'Any' for no preference): ");
         roomCode = sc.nextLine();
         
         System.out.print("Enter a bed type (or 'Any' for no preference): ");
         bedType = sc.nextLine();
        
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
         numChildren = Integer.valueOf(sc.nextLine());
         
         System.out.print("Number of adults during stay: ");
         numAdults = Integer.valueOf(sc.nextLine());
         
         occupancy = numChildren + numAdults;
      }
      
      private static boolean check_dates(String start, String end) {
         // confirm user input the correct date structure
         String match = "\\d{4}-\\d{2}-\\d{2}";
         if (!start.matches(match) || !end.matches(match))
            return false;

         String[] start_split = start.split("-");
         String[] end_split = end.split("-");

         // check that the start date isn't after the end date
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
   }
}

