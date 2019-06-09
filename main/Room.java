package main;

import java.time.LocalDate;

public class Room {
   private String roomCode;
   private String roomName;
   private int beds;
   private String bedType;
   private int maxOcc;
   private double basePrice;
   private String decor;

   private String start;
   private String end;
   
   public Room(String roomCode, String roomName, int beds, String bedType,
               int maxOcc, double basePrice, String decor, String start, String end) {
      this.roomCode = roomCode;
      this.roomName = roomName;
      this.beds = beds;
      this.bedType = bedType;
      this.maxOcc = maxOcc;
      this.basePrice = basePrice;
      this.decor = decor;
      this.start = start;
      this.end = end;
   }

   public void increaseResStartDay(int numDays) {
      LocalDate sdate = LocalDate.parse(start);
      LocalDate edate = LocalDate.parse(end);
      sdate = sdate.plusDays(numDays);
      edate = edate.plusDays(numDays);
      start = sdate.toString();
      end = edate.toString();
   }

   public String getStart() {
      return start;
   }

   public String getEnd() {
      return end;
   }

   public String getRoomCode() {
      return roomCode;
   }

   public String getRoomName() {
      return roomName;
   }

   public int getBeds() {
      return beds;
   }

   public String getBedType() {
      return bedType;
   }

   public int getMaxOcc() {
      return maxOcc;
   }

   public double getBasePrice() {
      return basePrice;
   }

   public String getDecor() {
      return decor;
   }
}
