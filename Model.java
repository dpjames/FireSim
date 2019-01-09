import java.util.*;
import java.awt.*;
import java.io.*;
public class Model{
   private static final int MAX_FIRE_AGE = 10;
   private static Random rand = new Random();
   public static int nrows;
   public static int ncols;
   public static ArrayList<ArrayList<Cell>> cells;
   public static ArrayList<Cell> activeFires;
   public static void init(String filename){
      activeFires = new ArrayList<Cell>();
      cells = new ArrayList<ArrayList<Cell>>();
      fillCells(filename);
      nrows = cells.size();
      ncols = nrows > 0 ? cells.get(0).size() : 0;
   }
   private static void fillCells(String filename){
      try {
         File input = new File(filename);
         Scanner lineScan = new Scanner(input);
         int y = 0;
         Cell.max = 0;
         while(lineScan.hasNextLine()){
            cells.add(new ArrayList<Cell>());
            String line = lineScan.nextLine();
            line = line.replace('[',' ');
            line = line.replace(']',' ');
            String[] strnums = line.split(",");
            for(int x = 0; x < strnums.length; x++){
               String thisnum = strnums[x];
               float elevation = Float.parseFloat(thisnum);
               if(elevation > Cell.max){
                  Cell.max = elevation;
               }
               cells.get(y).add(new Cell(x,y,elevation)); 
            }
            y++;
         }
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
      private int age;
      public static float max;
      public Cell(int x, int y, float elevation){
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
      }
      public int getX(){
         return loc[0];
      }
      public int getY(){
         return loc[1];
      }
      public void draw(Graphics g, int xoffset, int yoffset){
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
         g.fillRect(loc[0] - xoffset, loc[1] - yoffset,1,1);
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
      private int findNear(){
         int total = 0;
         int cx = getX() - 1; 
         int cy = getY() - 1;
         for(;cx<=getX()+1;cx++){
            for(;cy<=getY()+1;cy++){
               try{
                  if(cells.get(cy).get(cx).getType().equalsIgnoreCase("fire")){
                     total++;
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
         int cx = getX() - 1; 
         int cy = getY() - 1;
         float max = this.elevation;
         for(;cx<=getX()+1;cx++){
            for(;cy<=getY()+1;cy++){
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
         float prob = 10;
         //int near = findNear();
         //prob+=near*12.5;
         prob-=onRidge() ? 8 : 0;
         return prob;
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
