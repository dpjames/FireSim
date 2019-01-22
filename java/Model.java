import java.awt.image.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;

public class Model{
   private static int stepCount = 0;
   private static final int TARGET_WIDTH = 2000;
   private static final int TARGET_HEIGHT = 2000;
   private static final int MAX_FIRE_AGE = 10;
   private static final int SEARCH_BOX_OFFSET = 10;
   private static final double WIND_MOD = 3.5;
   private static final double BASE_PROB = 4.5;
   private static final int START_THRESHOLD = 10;
   private static Color FIRE_COLOR = Color.RED;
   private static Color BURNT_COLOR = new Color(139,69,19);
   private static Color BREAK_COLOR = Color.YELLOW;
   private static Random rand = new Random();
   public static int nrows;
   public static int ncols;
   public static ArrayList<ArrayList<Cell>> cells;
   public static ArrayList<Cell> activeFires;
   public static void init(String dem, String cover, String wind){
      activeFires = new ArrayList<>();
      cells = new ArrayList<>();
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
      stepCount = 0;
   }
   public static void clearStepCount(){
      stepCount = 0;
   }
   public static void incrementStepCount(){
      stepCount++;
   }
   private static void fillCells(String dem, String cover, String wind){
      Cell.wind = wind;
      int[][] coverArr = readTiff(cover);
      int[][] demArr = readTiff(dem);
      try {
         Cell.cm = Imaging.getBufferedImage(new File(dem)).getColorModel();
      }catch(Exception e){
         System.out.println(e);
         System.exit(-1);
      }
      System.out.println(demArr.length);
      System.out.println(coverArr.length);
      for(int y = 0; y < demArr[0].length; y++){
         if(y%(demArr[0].length/10) == 0){
            System.out.println(1.0*y/demArr[0].length);
         }
         cells.add(new ArrayList<>());
         for(int x = 0; x < demArr.length; x++){
            cells.get(y).add(new Cell(x, y, demArr[x][y], coverArr[x][y]));
         }
      }

   }
   public static BufferedImage toBufferedImage(Image img)
   {
      if (img instanceof BufferedImage)
      {
         return (BufferedImage) img;
      }
      BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
      Graphics2D bGr = bimage.createGraphics();
      bGr.drawImage(img, 0, 0, null);
      bGr.dispose();
      return bimage;
   }
   public static void export(String dest, double[][] bbox){
      BufferedImage img = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics pen = img.getGraphics();
      for(int y = 0; y < cells.size(); y++){
         for(int x = 0; x < cells.get(0).size(); x++){
            Cell c = cells.get(y).get(x);
            if(c.getType().equalsIgnoreCase("burnt")){
               pen.setColor(Color.BLACK);
            } else {
               pen.setColor(Color.WHITE);
            }
            pen.fillRect(x,y,1,1);
         }
      }
      double lats = (Math.abs(bbox[0][0] - bbox[1][0]))/cells.size();
      double longs = (Math.abs(bbox[0][1] - bbox[1][1]))/cells.get(0).size();
      try {
         ImageIO.write(img, "png", new File(dest + ".png"));
         FileWriter fw = new FileWriter(new File(dest+".wld"));
         fw.write(longs +"\n");
         fw.write("0\n0\n");
         fw.write("-"+lats +"\n");
         fw.write(bbox[0][0]+"\n");
         fw.write(bbox[1][1]+"\n");
         fw.close();
      } catch(Exception e){
         e.printStackTrace();
         System.out.println("cannot write the file sorry :( ");
      }
   }
   public static int[][] readTiff(String fname){
      try {
         BufferedImage img = toBufferedImage(Imaging.getBufferedImage(new File(fname)).getScaledInstance(TARGET_WIDTH,TARGET_HEIGHT,0));
         ColorModel cm = img.getColorModel();
         int w = img.getWidth();
         int h = img.getHeight();
         int[][] pixels = new int[w][h];
         for( int i = 0; i < w; i++ ) {
            for (int j = 0; j < h; j++) {
               pixels[i][j] = (img.getRGB(i, j));
            }
         }

         return pixels;
      }catch(Exception e){
         System.out.println("we found an error");
         e.printStackTrace();
         System.exit(-1);
      }
      return null;
   }
   public static class Cell{
      private int[] loc;
      private String type;
      private int elevation;
      private int fuelType;
      private String weather;
      private String moisture;
      private static String wind;
      private int age;
      private static float max;
      private static ColorModel cm;
      private void reset(){
         type = "normal";
         age = 0;
      }
      public Cell(int x, int y, int elevation, int cover){
         this.fuelType = cover;
         this.loc = new int[] {x,y};
         this.elevation = cm.getRed(elevation);
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
      public void draw(Graphics g, int xoffset, int yoffset, int zoom, int skip){
         if(this.type.equalsIgnoreCase("fire")){
            g.setColor(FIRE_COLOR);
         } else if(type.equalsIgnoreCase("burnt")){
            g.setColor(BURNT_COLOR);
         } else if(type.equalsIgnoreCase("break")){
            g.setColor(BREAK_COLOR);
         } else {
            int cv = (int)elevation;
            //g.setColor(new Color(cm.getRed(cv), cm.getBlue(cv), cm.getGreen(cv)));
            g.setColor(new Color(cv,cv,cv));
         }
         g.fillRect((loc[0] - xoffset) / zoom, (loc[1] - yoffset) / zoom,1+skip,1+skip);
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
         return
                 (wind.contains("N")  && cy > getY()) ||
                 (wind.contains("E")  && cx < getX()) ||
                 (wind.contains("S")  && cy < getY()) ||
                 (wind.contains("W")  && cx > getX());
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
                  }
               } catch (ArrayIndexOutOfBoundsException e){
                  continue;
               }
            }
         }
         return total;
      }
      private int getElevation(){
         return elevation;
      }
      private boolean onRidge() {
         int offset = 10;
         int cx = getX() - offset/2;
         int cy = getY() - offset/2;
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
      private double getCellContribution(int x, int y, ArrayList<Integer> elevations){
         Cell c = cells.get(y).get(x);
         elevations.add(c.getElevation());
         int dx = Math.abs(x - getX());
         int dy = Math.abs(y - getY());
         double distance = Math.sqrt(dx * dx + dy * dy);
         if(!c.type.equalsIgnoreCase("fire")){ //if its not on fire, then it cant catch you on fire.
            return 0;
         }
         double prob = BASE_PROB;
         prob += inWindDirection(x,y) ? WIND_MOD : -1 * WIND_MOD;

         return prob/distance;
      }
      private double getProb(){
         double total = 0;
         int y = getY();
         int x = getX();
         ArrayList<Integer> elevations = new ArrayList<Integer>();
         for(int cx = x - SEARCH_BOX_OFFSET;cx < x + SEARCH_BOX_OFFSET; cx++){
            for(int cy = y - SEARCH_BOX_OFFSET; cy < y + SEARCH_BOX_OFFSET; cy++){
               total+=getCellContribution(cx,cy,elevations);
            }
         }
         total = getElevationImpact(elevations, total);
         return total;
      }
      private double getElevationImpact(ArrayList<Integer> elevations, double total){
         int ltcount = 0;
         int gtcount = 0;
         for(int e : elevations){
            ltcount+= this.elevation < e ? 1 : 0;
            gtcount+= this.elevation > e ? 1 : 0;
         }
         double gtpercent = (double) gtcount / elevations.size() * 100;
         double ltpercent = (double) ltcount / elevations.size() * 100;
         double diff = Math.abs(gtpercent - ltpercent);
         if(diff < 10){
            return total;
         } else if(diff < 30) {
            return total / 2;
         } else if(diff < 60){
            return total/4;
         } else if (diff < 90){
            return total/6;
         }
         return 0;
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
         } else if(fuelType == -1){
            return null;
         }
         double prob = getProb();
         if(rand.nextInt(100) + 1 < prob || stepCount < START_THRESHOLD){
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
