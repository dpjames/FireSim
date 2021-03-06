import java.util.ArrayList;
   public class Controller{
   private static boolean reset = false;
   private static boolean running = true;
   private static final int INIT_WIDTH = 1000;
   private static final int INIT_HEIGHT = 1000;
   private static final String DEM_FILE_NAME = "data/YOSEMITE/dem.jpg";
   private static final String LAND_COVER_FILE_NAME = "data/YOSEMITE/lc.jpg";
   private static final String WIND_DIRECTION = "SW";
   private static final double[][] BBOX = {{-122.0005555550000054,38.9994444450000017},{-120.9994444449999946,40.0005555549999983}};
   private static final String OUTPUT = "out";
   public static void main(String[] args) throws InterruptedException{
      Model.init(DEM_FILE_NAME, LAND_COVER_FILE_NAME, WIND_DIRECTION);
      View v = new View(INIT_WIDTH, INIT_HEIGHT);

      while(true){ //going to change this to a key listener TODO
         if (reset) {
            reset = false;
            Model.reset();
         }
         if(running){
            update();
         }
         Thread.sleep(running ? 1 : 500);
      }
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
}
