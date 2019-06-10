package main;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Scanner;

public class InnReservations {
   
   public static void main(String[] args) {
      main_menu();
   }
   
   private static void main_menu() {
      Scanner sc = new Scanner(System.in);
      print_options();
      Requirements req = new Requirements();

      while (true) {
         System.out.print("\tOption: ");
         String option = sc.nextLine();
         try {
            if (option.equalsIgnoreCase("R") || option.equalsIgnoreCase("Room")) {
               req.requirement1();
            }

            else if (option.equalsIgnoreCase("Re") || option.equalsIgnoreCase("Reservation")) {
               req.requirement2();
            }

            else if (option.equalsIgnoreCase("C") || option.equalsIgnoreCase("Change")) {
               req.requirement3();
            }

            else if (option.equalsIgnoreCase("Ca") || option.equalsIgnoreCase("Cancle")) {
               req.requirement4();
            }

            else if (option.equalsIgnoreCase("Rev") || option.equalsIgnoreCase("Revenue")) {
               req.requirement6();
            }
      
            else if (option.equalsIgnoreCase("Q") || option.equalsIgnoreCase("Quit")) {
               System.exit(0);
            }
            else if (option.equalsIgnoreCase("H") || option.equalsIgnoreCase("Help")) {
               print_options();
            }
            else
               System.out.println("Not a valid option!");
         }
         catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
         }
      }
   }

   private static void print_options() {
      System.out.println("Select an option:");
      System.out.println("\tR[oom]: Display rooms ordered by popularity.");
      System.out.println("\tRe[servation]: Reserve a room.");
      System.out.println("\tC[hange] reservation: Make changes to a reservation.");
      System.out.println("\tCa[ncel] reservation: Cancel your reservation.");
      System.out.println("\tF[ind] Reservations: Find reservations that match a query.");
      System.out.println("\tRev[enue]: Get month to month room revenue and grand totals.");
      System.out.println("\tH[elp]");
      System.out.println("\tQ[uit]");
   }
}
