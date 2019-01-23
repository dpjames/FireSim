import org.apache.commons.imaging.Imaging;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class MLController {
   private static Random rand = new Random();
   private static boolean reset = false;
   private static boolean running = true;
   private static final String DEM_FILE_NAME = "data/PARADISE/dem.jpg";
   private static final String LAND_COVER_FILE_NAME = "data/PARADISE/lc.jpg";
   private static final String WIND_DIRECTION = "SW";
   private static final double[][] BBOX = {{-122.0005555550000054,38.9994444450000017},{-120.9994444449999946,40.0005555549999983}};
   private static final int[] START = {1160,322};
   //private static final int[] START = {230, 60};
   private static final String OUTPUT_BASE = "LEARN/learning";
   private static String OUTPUT = "";
   private static final int N_CHILDREN = 40;
   private static final int N_GEN = 200;
   private static BufferedImage SOL_IMG;
   public static void main(String[] args) throws InterruptedException{
      try {
         SOL_IMG = Model.toBufferedImage(Imaging.getBufferedImage(new File("LEARN/SOLUTION.PNG")).getScaledInstance(Model.TARGET_WIDTH,Model.TARGET_HEIGHT,0));
      } catch(Exception e){
         System.out.println(e);
         System.exit(-1);
      }
      Model.init(DEM_FILE_NAME, LAND_COVER_FILE_NAME, WIND_DIRECTION);
      ArrayList<ModelVars> children = new ArrayList<>();
      for(int i = 0; i < N_CHILDREN; i++){
         children.add(new ModelVars());
      }

      long timer = 0;
      for(int i = 0; i < N_GEN; i++) {
         System.out.println("starting gen " + i);
         int count = 0;
         for (ModelVars child : children) {
            OUTPUT = OUTPUT_BASE + "gen" + i + "child" + count + child.toString();
            System.out.println(OUTPUT);
            Model.setVariables(child.MAX_FIRE_AGE, child.SEARCH_BOX_OFFSET, child.WIND_MOD, child.BASE_PROB, child.START_THRESHOLD);
            Model.reset();
            Model.startFire(START[0], START[1]);
            running = true;
            timer = System.nanoTime();
            while (running) { //going to change this to a key listener TODO
               update();
               if(System.nanoTime() - timer > 15000000000L){
                  System.out.println("killed early");
                  Model.export(OUTPUT, BBOX);
                  running = false;
               }
            }
            count++;
         }
         System.out.println("done with gen " + i);
         findBest(children, i);
         children.sort((ModelVars o1, ModelVars o2) -> o1.LIKE_CELLS < o2.LIKE_CELLS ? -1 : 1);
         int cutoff = (int)(children.size() * .8);
         for(int j = 0; j < cutoff; j++){
            children.get(j).mutate(children.get(children.size() - 1));
         }
      }

      System.out.println("done running");
   }
   private static int findLike(BufferedImage i1, BufferedImage i2){
      int width = i1.getWidth();
      int height = i1.getHeight();
      ColorModel c1 = i1.getColorModel();
      ColorModel c2 = i2.getColorModel();
      int count = 0;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            int p1 = i1.getRGB(x,y);
            int p2 = i2.getRGB(x,y);
            if(c1.getRed(p1) == c2.getRed(p2)){
               count++;
            }
         }
      }
      System.out.println("count is " + count);
      return count;
   }
   public static ModelVars findBest(ArrayList<ModelVars> children, int gen){
      ModelVars best = children.get(0);
      int max = 0;
      for(int i = 0; i < children.size(); i++){
         try {
            String thisName = OUTPUT_BASE + "gen" + gen + "child" + i + ".png";
            BufferedImage thisImage = Model.toBufferedImage(Imaging.getBufferedImage(new File(thisName)).getScaledInstance(Model.TARGET_WIDTH,Model.TARGET_HEIGHT,0));
            int likePixels = findLike(thisImage, SOL_IMG);
            children.get(i).LIKE_CELLS = likePixels;
            if(likePixels > max){
               best = children.get(i);
               max = likePixels;
            }
         } catch(Exception e){
            System.out.println(e);
         }
      }
      System.out.println("best count is " + max);
      return best;
   }
   public static void reset(){
      reset = true;
   }
   public static void toggleRun(){
      running = !running;
   }
   public static void update() throws NullPointerException{
      if(Model.activeFires.size() == 0){
         running = false;
         System.out.println("exporting!");
         Model.export(OUTPUT, BBOX);
      }
      ArrayList<Model.Cell> newCells = new ArrayList<Model.Cell>();
      Model.incrementStepCount();
      for(int i = 0; i < Model.activeFires.size(); i++){
         Model.Cell c = Model.activeFires.get(i);
         int cx = c.getX() - 1;
         int cy = c.getY() - 1;
         for(int xoffset = 0; xoffset < 3; xoffset++){
            for(int yoffset = 0; yoffset < 3; yoffset++){
               try{
                  Model.Cell tempCell = Model.cells.get(cy + yoffset).get(cx + xoffset).update();
                  if(tempCell != null && !newCells.contains(tempCell)){
                     newCells.add(tempCell);
                  }
               } catch (ArrayIndexOutOfBoundsException e){
                  continue;
               } catch(IndexOutOfBoundsException e){
                  continue;
               }
            }
         }
      }
      for(Model.Cell c : newCells){
         Model.cells.get(c.getY()).set(c.getX(), c);
         Model.activeFires.add(c);
      }
      int rem = 0;
      for(int i = 0; i < Model.activeFires.size(); i++){
         if(!Model.activeFires.get(i).getType().equalsIgnoreCase("fire")){
            rem++;
            Model.activeFires.remove(i);
            i--;
         }
      }
   }
   private static class ModelVars{
      private int LIKE_CELLS;
      private int MAX_FIRE_AGE = 10;
      private int SEARCH_BOX_OFFSET = 10;
      private double WIND_MOD = 6;
      private double BASE_PROB = 1.5;
      private int START_THRESHOLD = 15;
      private ModelVars(){
         int p = rand.nextInt(100);
         if(p < 50){
            MAX_FIRE_AGE += rand.nextInt(6) - 3;
            SEARCH_BOX_OFFSET += rand.nextInt(6) - 3;
            WIND_MOD += rand.nextInt(6) - 3;
            BASE_PROB += rand.nextDouble() * 10;
            START_THRESHOLD += rand.nextInt(20) - 20;
         }
      }
      private void mutate(ModelVars m){
         int p = rand.nextInt(100);
         if(p < 50){
            MAX_FIRE_AGE      = Math.min(m.MAX_FIRE_AGE + rand.nextInt(6) - 3,30);
            SEARCH_BOX_OFFSET = Math.min(m.SEARCH_BOX_OFFSET + rand.nextInt(6) - 3,25);
            WIND_MOD          = Math.min(m.WIND_MOD + rand.nextInt(6) - 3,15);
            BASE_PROB         = Math.min(m.BASE_PROB + rand.nextDouble() * 10 - .5,6);
            START_THRESHOLD   = Math.max(Math.min(m.START_THRESHOLD + rand.nextInt(20) - 20,30), 0);
         }
      }
      public String toString(){
         return "N"+MAX_FIRE_AGE+"N"+SEARCH_BOX_OFFSET+"N"+WIND_MOD+"N"+BASE_PROB+"N"+START_THRESHOLD;
      }
   }
}
