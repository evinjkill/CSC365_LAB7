import java.util.Scanner;

public class InnReservations {
   
   public static void main(String[] args) {
      InnReservations res = new InnReservations();
      res.main_menu();
   }
   
   private void main_menu() {
      System.out.println("Select an option:");
      System.out.println("\tR[oom]: Display rooms ordered by popularity.");
      System.out.println("\tRe[servation]: Reserve a room.");
      System.out.println("\tC[hange] reservation: Make changes to a reservation.");
      System.out.println("\tCa[ncel] reservation: Cancel your reservation.");
      System.out.println("\tF[ind] Reservations: Find reservations that match a query.");
      
      Scanner sc = new Scanner(System.in);
   }
}
