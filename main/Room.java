package main;

public class Room {
   private String roomCode;
   private String roomName;
   private int beds;
   private String bedType;
   private int maxOcc;
   private double basePrice;
   private String decor;
   
   public Room(String roomCode, String roomName, int beds, String bedType,
               int maxOcc, double basePrice, String decor) {
      this.roomCode = roomCode;
      this.roomName = roomName;
      this.beds = beds;
      this.bedType = bedType;
      this.maxOcc = maxOcc;
      this.basePrice = basePrice;
      this.decor = decor;
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
