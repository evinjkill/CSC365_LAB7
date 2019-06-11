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
	
   /*---------------------------------------------------------------------------
   *
   * Requirement 1
   *
   ---------------------------------------------------------------------------*/

	public void requirement1() throws SQLException {

		// Step 1: Establish connection to RDBMS
      try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
								   System.getenv("HP_JDBC_USER"),
								   System.getenv("HP_JDBC_PW"))) {
         // Step 2: Construct SQL statement
			StringBuilder sb = new StringBuilder("select pop.*, NextAvailableDate from ");
			sb.append("(select r.*, round(sum(DATEDIFF(checkout, checkin)) / 180, 2) as Popularity ");
			sb.append("from lab7_rooms as r ");
			sb.append("inner join lab7_reservations as res on r.roomcode = res.room ");
			sb.append("where DATEDIFF(CURDATE(), checkin) < 180 and DATEDIFF(CURDATE(), checkin) > 0 ");
			sb.append("group by room) pop ");
			sb.append("inner join ");
			sb.append("(select r.*, min(checkout) as NextAvailableDate ");
			sb.append("from lab7_rooms as r ");
			sb.append("inner join lab7_reservations as res on r.roomcode = res.room ");
			sb.append("where checkout >= curdate() ");
			sb.append("group by room) ava ");
			sb.append("on pop.RoomCode = ava.RoomCode ");
         sb.append("inner join ");
         sb.append("(select * from lab7_rooms) full ");
         sb.append("on full.RoomCode = ava.RoomCode ");
         sb.append("order by popularity desc; ");

		    // Step 3: (omitted in this example) Start transaction

		    // Step 4: Send SQL statement to DBMS
         try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            try(ResultSet rs = pstmt.executeQuery()) {

				   // Step 5: Receive results
               System.out.format("%5s |%30s |%4s |%8s |%3s |%10s |%20s |%3s  |%15s%n",
                                 "room", "room name", "beds", "bed type", "occ",
                                 "base price", "decor", "pop", "next avail date");
               System.out.println("-------" + "---------------------------------" + "------" +
                  "----------" + "-----" + "------------" + "----------------------" + "------" +
                  "---------------");
               while (rs.next()) {
					   String RoomCode = rs.getString("roomcode");
					   String RoomName = rs.getString("roomname");
					   int Beds = rs.getInt("beds");
					   String bedType = rs.getString("bedType");
					   int maxOcc = rs.getInt("maxOcc");
					   float basePrice = rs.getFloat("basePrice");
					   String decor = rs.getString("decor");
					   double Popularity = rs.getDouble("Popularity");
					   String NextDateAvailable = rs.getString("NextAvailableDate");
                  System.out.format("%5s |%30s |%4d |%8s |%3d |%10.2f |%20s |%4.2f |%15s%n",
                     RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor, Popularity, NextDateAvailable);
               }
            }
         }
      }
   }
   
   /*---------------------------------------------------------------------------
   *
   * Requirement 2
   *
   ---------------------------------------------------------------------------*/

   public void requirement2() throws SQLException {
      try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                                        System.getenv("HP_JDBC_USER"),
                                                        System.getenv("HP_JDBC_PW"))) {
         
         R2Query r2 = new R2Query();
         
         List<Object> params = new ArrayList<Object>();
         params.add(r2.getEndDate());
         params.add(r2.getStartDate());
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

         System.out.print("Number of children (-1 for no change): ");
         int numChildren = Integer.valueOf(sc.nextLine());

         System.out.print("Number of adults (-1 for no change): ");
         int numAdults = Integer.valueOf(sc.nextLine());

         List<Object> params = new ArrayList<Object>();

         StringBuilder sb = new StringBuilder("UPDATE lab7_reservations SET ");

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

         if(numChildren >= 0) {
            sb.append("Kids = ?, ");
            params.add(numChildren);
         }

         if(numAdults >= 0) {
            sb.append("Adults = ? ");
            params.add(numAdults);
         }

         
         sb.append("WHERE CODE = ?;");
         params.add(resCode);

         try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object p : params) {
               pstmt.setObject(i++, p);
            }
            
            int res = pstmt.executeUpdate();
            System.out.format("UPDATE PROBS WENT THROUGH, %d\n", res);

            
         }
      }

   }
   
   public void requirement4() throws SQLException {
      try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                                        System.getenv("HP_JDBC_USER"),
                                                        System.getenv("HP_JDBC_PW"))) {
         Scanner sc = new Scanner(System.in);
         System.out.print("Enter your reservation code: ");
         String resCode = sc.nextLine();

         System.out.print("BRUH ARE YOU SURE YOU WANT TO DELETE YOUR RESERVATION?! [Y/n]: ");
         String response = sc.nextLine();
         if(!"Y".equalsIgnoreCase(response)) {
            System.out.println("You won't regret this!");
            return;
         }
         
         System.out.print("Bruuuuuuuuuh. You really want to do this... [Y/n]: ");
         response = sc.nextLine();
         if(!"Y".equalsIgnoreCase(response)) {
            System.out.println("I knew you'd change your mind!");
            return;
         }

         System.out.println("Fuck it, we didn't want you there anyways");
         
         List<Object> params = new ArrayList<Object>();

         StringBuilder sb = new StringBuilder("DELETE FROM lab7_reservations WHERE CODE = ?;");
         params.add(resCode);

         try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object p : params) {
               pstmt.setObject(i++, p);
            }
            
            boolean res = pstmt.execute();
            
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
                           rs.getString("decor"), r2.getStartDate(), r2.getEndDate());

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
                              rs.getString("decor"), r2.getStartDate(), r2.getEndDate());

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
      int code = get_max_code(conn) + 1;
      params.add(code);
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
         System.out.printf("Reservation %s!", rows > 0 ? "successful" : "failed");
         System.out.printf(" %s\n", rows > 0 ? "(code: " + code + ")" : "");
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
      params.add(r2.getEndDate());
      params.add(r2.getStartDate());
      
      sb.append(searchAddOneWeek);
      sb.append(" UNION ");
      
      // Finds all rooms available in 2 weeks (or just the requested one)
      String searchAddTwoWeek = "SELECT *, " + "'" + ADD_TWO_STR + "'" + " SearchMethod FROM lab7_rooms" +
      " WHERE roomcode NOT IN (" +
         "SELECT roomcode FROM lab7_rooms JOIN lab7_reservations ON roomcode = room" +
         " WHERE checkin <= DATE_ADD(?, INTERVAL 14 DAY) AND checkout >= DATE_ADD(?, INTERVAL 14 DAY))";
      params.add(r2.getEndDate());
      params.add(r2.getStartDate());
         
      sb.append(searchAddTwoWeek);
      
      // Expanding roomcode search takes precedence over bed type
      if (!"any".equalsIgnoreCase(r2.roomCode)) {
         // Finds all same date rooms
         String searchMatchSame = "SELECT *, " + "'" + SAME_DATE_STR + "'" + " SearchMethod FROM lab7_rooms" +
            " WHERE roomcode NOT IN (" +
            "SELECT roomcode FROM lab7_rooms JOIN lab7_reservations ON roomcode = room" +
            " WHERE checkin <= ? AND checkout >= ?)";
         params.add(r2.getEndDate());
         params.add(r2.getStartDate());

         sb.append(" UNION ");
         sb.append(searchMatchSame);
      }
      else if (!"any".equalsIgnoreCase(r2.bedType)) {
         // Finds same date rooms that match the bed type
         String searchMatchBed = "SELECT *, " + "'" + SAME_BED_STR + "'" + " SearchMethod FROM lab7_rooms" +
            " WHERE roomcode NOT IN (" +
            "SELECT roomcode FROM lab7_rooms JOIN lab7_reservations ON roomcode = room" +
            " WHERE checkin <= ? AND checkout >= ?) AND bedtype = ?";
         params.add(r2.getEndDate());
         params.add(r2.getStartDate());
         params.add(r2.bedType);

         sb.append(" UNION ");
         sb.append(searchMatchBed);
      }
      
      return sb.toString();
   }

   public void requirement5() throws SQLException {
      try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                                         System.getenv("HP_JDBC_USER"),
                                                         System.getenv("HP_JDBC_PW"))) {

         List<Object> params = new ArrayList<Object>();
         StringBuilder sb = new StringBuilder("SELECT lab7_reservations.*, roomname FROM lab7_reservations" +
         " JOIN lab7_rooms ON roomcode = room WHERE ");
         Scanner sc = new Scanner(System.in);
         boolean first_filter = false;

         System.out.println("For any option, press ENTER to skip");
         System.out.print("Enter a first name: ");
         String fname = sc.nextLine();

         if (fname.length() > 0) {
            params.add(fname);
            sb.append("firstname LIKE ? ");
            first_filter = true;
         }

         System.out.print("Enter a last name: ");
         String lname = sc.nextLine();

         if (lname.length() > 0) {
            params.add(lname);
            sb.append((first_filter ? "AND " : "") + "lastname LIKE ? ");
            first_filter = true;
         }

         String start = "", end = "";
         do {
            System.out.print("Enter a start for a date range (yyyy-mm-dd): ");
            start = sc.nextLine();
            if (start.length() == 0)
               break;

            System.out.print("Enter an end for a date range (yyyy-mm-dd):  ");
            end = sc.nextLine();
            if (end.length() == 0)
               break;
         }
         while(!R2Query.check_dates(start, end));

         if (start.length() > 0 && end.length() > 0) {
            params.add(start);
            sb.append((first_filter ? "AND " : "") + "checkin >= ? ");
            params.add(end);
            sb.append("AND checkout <= ? ");
            first_filter = true;
         }

         System.out.print("Enter a room code: ");
         String roomcode = sc.nextLine();

         if (roomcode.length() > 0) {
            params.add(roomcode);
            sb.append((first_filter ? "AND " : "") + "roomcode LIKE ? ");
            first_filter = true;
         }

         System.out.print("Enter a reservation code: ");
         String rescode = sc.nextLine();

         if (rescode.length() > 0) {
            params.add(rescode);
            sb.append((first_filter ? "AND " : "") + "CODE LIKE ?");
         }

         conn.setAutoCommit(false);

         try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            int i = 1;
            for (Object o : params) {
               pstmt.setObject(i++, o);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
               System.out.println("Matching reservations:\n");
               System.out.format("%6s |%4s |%10s |%10s |%6s |%20s |%20s |%6s |%4s |%30s\n",
                  "CODE", "room", "checkin", "checkout", "rate", "last name", "first name",
                  "adults", "kids", "room name");
               System.out.println("--------" + "------" + "------------" + "------------" +
                  "--------" + "----------------------" + "----------------------" + "--------" +
                  "------" + "------------------------------");

               int count = 0;
               while (rs.next()) {
                  System.out.format("%6s |%4s |%10s |%10s |%6.2f |%20s |%20s |%6d |%4d |%30s\n",
                     rs.getString("CODE"), rs.getString("room"), rs.getDate("checkin").toString(),
                     rs.getDate("checkout").toString(), rs.getDouble("rate"), rs.getString("lastname"),
                     rs.getString("firstname"), rs.getInt("adults"), rs.getInt("kids"), rs.getString("roomname"));
                  count++;
               }

               System.out.println();
               if (count == 0)
                  System.out.println("No matches found!");
            }
         }
      }

   }
   
   /*---------------------------------------------------------------------------
   *
   * Requirement 6
   *
   ---------------------------------------------------------------------------*/
   
   public void requirement6() throws SQLException {
      try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                                        System.getenv("HP_JDBC_USER"),
                                                        System.getenv("HP_JDBC_PW"))) {
         
         String query =
            "SELECT COALESCE(roomcode, 'totals') roomcode," +
            "m1 January,m2 February,m3 March,m4 April,m5 May,m6 June," +
            "m7 July,m8 August,m9 September,m10 October,m11 November,m12 December," +
            "m1+m2+m3+m4+m5+m6+m7+m8+m9+m10+m11+m12 AS TotalMonthRevenue " +
            
            "FROM (SELECT roomcode," +
            "sum(m1) m1,sum(m2) m2,sum(m3) m3,sum(m4) m4,sum(m5) m5,sum(m6) m6," +
            "sum(m7) m7,sum(m8) m8,sum(m9) m9,sum(m10) m10,sum(m11) m11,sum(m12) m12 " +
            
            "FROM (SELECT roomcode," + months_to_col() + "FROM lab7_reservations " +
            "JOIN lab7_rooms ON roomcode = room) month_reservation_cost " +
            "GROUP BY roomcode WITH ROLLUP) monthly_revenue";
            
         try (Statement stmt = conn.createStatement();
               ResultSet rs = stmt.executeQuery(query)) {
                  
                  System.out.format("%9s |%9s |%9s |%9s |%9s |%9s |%9s |" +
                                    "%9s |%9s |%9s |%9s |%9s |%9s |%15s%n",
                                    "roomcode", "January", "February", "March", "April", "May",
                                    "June", "July", "August", "September", "October",
                                    "November", "December",
                                    "Monthly Revenue");
                  
                  print_row_delim();
                  
                  while (rs.next()) {
                     String roomcode = rs.getString("roomcode");
                     int jan_rev = rs.getInt("January");
                     int feb_rev = rs.getInt("February");
                     int mar_rev = rs.getInt("March");
                     int apr_rev = rs.getInt("April");
                     int may_rev = rs.getInt("May");
                     int jun_rev = rs.getInt("June");
                     int jul_rev = rs.getInt("July");
                     int aug_rev = rs.getInt("August");
                     int sep_rev = rs.getInt("September");
                     int oct_rev = rs.getInt("October");
                     int nov_rev = rs.getInt("November");
                     int dec_rev = rs.getInt("December");
                     int total = rs.getInt("TotalMonthRevenue");
                     
                     System.out.format("%9s |%9d |%9d |%9d |%9d |%9d |%9d |" +
                                       "%9d |%9d |%9d |%9d |%9d |%9d |%15d%n",
                                       roomcode,
                                       jan_rev, feb_rev, mar_rev, apr_rev, may_rev,
                                       jun_rev, jul_rev, aug_rev, sep_rev, oct_rev,
                                       nov_rev, dec_rev, total);
                  }
               }
      }
   }
   
   private void print_row_delim() {
      String roomcode_str = "-----------";
      String month_str = "-----------";
      String total_str = "----------------";
      System.out.print(roomcode_str);
      for (int i = 0; i < 12; i++)
         System.out.print(month_str);
      System.out.println(total_str);
   }
   
   private String months_to_col() {
      String res = "";
      for (int i = 1; i <= 12; i++) {
         res += "CASE MONTH(checkout) WHEN " + i +
                " THEN ROUND(rate * DATEDIFF(checkout, checkin), 0) END AS m" + i;

         if (i < 12)
            res += ",";
         else
            res += " ";
      }
      
      return res;
   }
}

