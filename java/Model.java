import java.util.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class Model{
   private static final int MAX_FIRE_AGE = 10;
   private static Random rand = new Random();
   public static int nrows;
   public static int ncols;
   public static ArrayList<ArrayList<Cell>> cells;
   public static ArrayList<Cell> activeFires;
   public static void init(String dem, String cover, String wind){
      activeFires = new ArrayList<Cell>();
      cells = new ArrayList<ArrayList<Cell>>();
      fillCells(dem, cover, wind);
      nrows = cells.size();
      ncols = nrows > 0 ? cells.get(0).size() : 0;
   }
   public static void reset(){
      for(int y = 0; y < cells.size(); y++){
         for(int x = 0; x < cells.get(0).size(); x++){
            try{
               cells.get(y).get(x).reset();
            } catch(IndexOutOfBoundsException e){
               //System.out.println("oob");
            }
         }
      }
      activeFires.clear();
   }
   private static void fillCells(String dem, String cover, String wind){
      try {
         File inputDem = new File(dem);
         Scanner demlineScan = new Scanner(inputDem);
         File inputCover = new File(cover);
         Scanner coverlineScan = new Scanner(inputCover);
         int y = 0;
         Cell.max = 0;
         Cell.wind = wind;
         while(demlineScan.hasNextLine()){
            cells.add(new ArrayList<Cell>());
            String demline = demlineScan.nextLine();
            demline = demline.replace('[',' ');
            demline = demline.replace(']',' ');
            String[] strdems = demline.split(",");
            

            String coverline = coverlineScan.nextLine();
            coverline = coverline.replace('[',' ');
            coverline = coverline.replace(']',' ');
            String[] strcovers = coverline.split(",");


            for(int x = 0; x < strdems.length; x++){
               String thiselevation = strdems[x];
               String thiscover = strcovers[x];
               float elevation = Float.parseFloat(thiselevation);
               int coverType = Integer.parseInt(thiscover.trim());
               if(elevation > Cell.max){
                  Cell.max = elevation;
               }

               Cell c = new Cell(x,y,elevation, coverType);
               cells.get(y).add(c); 
            }
            y++;
         }
         if(demlineScan.ioException() != null){
            demlineScan.ioException().printStackTrace();
         }
         coverlineScan.close();
         demlineScan.close();
      } catch(FileNotFoundException e){
         System.out.println("file not found exception");
         e.printStackTrace();
         System.exit(1);
      }
   }
   public static class Cell{
      private int[] loc;
      private String type;
      private float elevation;
      private int fuelType;
      private String weather;
      private String moisture;
      public static String wind;
      private int age;
      public static float max;
      private void reset(){
         type = "normal";
         age = 0;
      }
      public Cell(int x, int y, float elevation, int cover){
         this.fuelType = cover;
         this.loc = new int[] {x,y};
         this.elevation = elevation;
         this.type = "normal";
      }
      public String getType(){
         return type;
      }
      public Cell(Cell o){
         this.loc = o.loc;
         this.type = o.type;
         this.elevation = o.elevation;
         this.age = o.age;
         this.fuelType = o.fuelType;
         this.weather = o.weather;
         this.moisture = o.moisture;
      }
      public int getX(){
         return loc[0];
      }
      public int getY(){
         return loc[1];
      }
      public void draw(Graphics g, int xoffset, int yoffset, int zoom){
         if(this.type.equalsIgnoreCase("fire")){
            g.setColor(Color.RED);
         } else if(type.equalsIgnoreCase("burnt")){
            g.setColor(Color.GREEN);
         } else if(type.equalsIgnoreCase("break")){
            g.setColor(Color.YELLOW);
         } else {
            float cv = elevation/max;
            g.setColor(new Color(cv, cv, cv));
         }
         g.fillRect((loc[0] - xoffset) / zoom, (loc[1] - yoffset) / zoom,1,1);
      }
      public void setType(String t){
         this.type = t;
      }
      public void setAge(int i){
         age = i;
      }
      public void incrementAge(){
         age++;
      }
      private boolean inWindDirection(int cx, int cy){
         if(cx > getX() && cy < getY()){
            return true;
         }
         return false;
      }
      private int findNear(){
         int total = 0;
         int cx = getX() - 1; 
         for(;cx<=getX()+1;cx++){
            int cy = getY() - 1;
            for(;cy<=getY()+1;cy++){
               try{
                  if(cells.get(cy).get(cx).getType().equalsIgnoreCase("fire")){
                     total++;
                     if(inWindDirection(cx,cy)){
                        total+=2;
                     } else {
                        total-=2;
                     }
                  }
               } catch (ArrayIndexOutOfBoundsException e){
                  continue;
               }
            }
         }
         return total;
      }
      private float getElevation(){
         return elevation;
      }
      private boolean onRidge() {
         int offset = 10;
         int cx = getX() - offset; 
         int cy = getY() - offset;
         float max = this.elevation;
         for(;cx<=getX()+offset;cx++){
            for(;cy<=getY()+offset;cy++){
               try{
                  max = cells.get(cy).get(cx).getElevation() > max ? cells.get(cy).get(cx).getElevation() : max;
               } catch (ArrayIndexOutOfBoundsException e){
                  continue;
               }
            }
         }
         return max == this.elevation;
      }
      private float getProb(){
         float prob = 25;
         int near = findNear();
         prob+=near*10;
         prob-=onRidge() ? 15 : 0;
         return prob;
         //return 100;
      }
      public Cell update(){
         if(type.equalsIgnoreCase("fire")){
            incrementAge();
            if(age > MAX_FIRE_AGE){
               type = "burnt";
            }
            return null;
         } else if(type.equalsIgnoreCase("break") || type.equalsIgnoreCase("burnt")){
            return null;
         } else if(fuelType == 3
               || fuelType == 21
               || fuelType == 2
               || fuelType == 6){
            return null;
         }
         float prob = getProb();
         if(rand.nextInt(100) + 1 < prob){
            Cell c = new Cell(this); 
            c.setType("fire");
            c.setAge(0);
            return c;
         }
         return null;
      }
      public boolean equals(Object o){
         Cell other = (Cell)o;
         return other.getX() == getX() && other.getY() == getY();
      }
   }
}
