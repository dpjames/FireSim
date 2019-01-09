import java.util.ArrayList;
public class Controller{
   public static void main(String[] args) throws InterruptedException{
      //input window w, window h, elevationFile, landCoverFile
      Model.init(args[2], args[3]);
      //View v = new View(Model.ncols,Model.nrows);
      View v = new View(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
      while(true){ //going to change this to a key listener
         update();
         Thread.sleep(5);
      }
   }
   public static void update(){
      ArrayList<Model.Cell> newCells = new ArrayList<Model.Cell>();
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
