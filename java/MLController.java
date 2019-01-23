import java.util.ArrayList;
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
   private static final String OUTPUT_BASE = "LEARN/learning";
   private static String OUTPUT = "";
   private static final int N_CHILDREN = 10;
   private static final int N_GEN = 10;
   public static void main(String[] args) throws InterruptedException{
      Model.init(DEM_FILE_NAME, LAND_COVER_FILE_NAME, WIND_DIRECTION);
      ArrayList<ModelVars> children = new ArrayList<>();
      for(int i = 0; i < N_CHILDREN; i++){
         children.add(new ModelVars());
      }
      for(int i = 0; i < N_GEN; i++) {
         System.out.println("starting gen " + i);
         int count = 0;
         for (ModelVars child : children) {
            OUTPUT = "gen" + i + "child" + count;
            Model.setVariables(child.MAX_FIRE_AGE, child.SEARCH_BOX_OFFSET, child.WIND_MOD, child.BASE_PROB, child.START_THRESHOLD);
            Model.reset();
            Model.startFire(START[0], START[1]);
            while (running) { //going to change this to a key listener TODO
               update();
            }
            count++;
         }
         System.out.println("done with gen " + i);
      }
      ModelVars best = children.get(rand.nextInt(N_CHILDREN));
      for(ModelVars child : children){
         if(child.equals(best)){
            continue;
         }
         child.mutate(best);
      }
      System.out.println("done running");
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
      private int MAX_FIRE_AGE = 10;
      private int SEARCH_BOX_OFFSET = 10;
      private double WIND_MOD = 6;
      private double BASE_PROB = 1.5;
      private int START_THRESHOLD = 30;
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
            MAX_FIRE_AGE      = m.MAX_FIRE_AGE + rand.nextInt(6) - 3;
            SEARCH_BOX_OFFSET = m.SEARCH_BOX_OFFSET + rand.nextInt(6) - 3;
            WIND_MOD          = m.WIND_MOD + rand.nextInt(6) - 3;
            BASE_PROB         = m.BASE_PROB + rand.nextDouble() * 10;
            START_THRESHOLD   = m.START_THRESHOLD + rand.nextInt(20) - 20;
         }
      }
   }
}
