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

      System.out.println("Select an option:");
      System.out.println("\tR[oom]: Display rooms ordered by popularity.");
      System.out.println("\tRe[servation]: Reserve a room.");
      System.out.println("\tC[hange] reservation: Make changes to a reservation.");
      System.out.println("\tCa[ncel] reservation: Cancel your reservation.");
      System.out.println("\tF[ind] Reservations: Find reservations that match a query.");
      
      String option = sc.nextLine();
      Requirements req = new Requirements();

      if (option.equalsIgnoreCase("R") || option.equalsIgnoreCase("Room")) {
         try {
            req.requirement2();
         }
         catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
         }
      }
   }
}