package main

import java.util.Scanner;

public final class R2Query {
   final String firstName;
   final String lastName;
   final String roomCode;
   final String bedType;
   final String startDate;
   final String endDate;
   final int numChildren;
   final int numAdults;
   final int occupancy;
   
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