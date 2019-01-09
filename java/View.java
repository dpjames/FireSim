import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
public class View extends JFrame {
   private Timer drawTime;
   private DrawCanvas canvas = new DrawCanvas();
   private int width;
   private int height;
   private int viewY;
   private int viewX;
   public View(int w,int h){
      super();
      viewY = 0;
      viewX = 0;
      height = h;
      width = w;
      setLayout(new BorderLayout());
      setSize(w,h);
      canvas.addMouseListener(canvas);
      setTitle("fire");
      add("Center", canvas);
      //setResizable(false);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      drawTime = new Timer(100, canvas);
      drawTime.start();
      setVisible(true);
      setupKeyBindings();
   }
   public void setupKeyBindings(){
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0), LEFT);
      canvas.getActionMap().put(LEFT,left);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0), RIGHT);
      canvas.getActionMap().put(RIGHT,right);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0), UP);
      canvas.getActionMap().put(UP,up);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0), DOWN);
      canvas.getActionMap().put(DOWN,down);
   }
   private static final String LEFT = "LEFT";
   private static final String RIGHT = "RIGHT";
   private static final String UP = "UP";
   private static final String DOWN = "DOWN";

   private Action down = new AbstractAction(DOWN){
      @Override
      public void actionPerformed(ActionEvent e){
         viewY+=height/2;
         if(viewY + height > Model.nrows){
            viewY = Model.nrows - height;
         }
      }
   };
   private Action right = new AbstractAction(RIGHT){
      @Override
      public void actionPerformed(ActionEvent e){
         viewX+=width/2;
         if(viewX + width > Model.ncols){
            viewX = Model.ncols - width;
         }
      }
   };
   private Action left = new AbstractAction(LEFT){
      @Override
      public void actionPerformed(ActionEvent e){
         viewX-=width/2;
         if(viewX < 0){
            viewX = 0;
         }
      }
   };
   private Action up = new AbstractAction(UP){
      @Override
      public void actionPerformed(ActionEvent e){
         viewY-= height/2;
         if(viewY < 0){
            viewY = 0;
         }
      }
   };

   private class DrawCanvas extends JPanel implements MouseListener, ActionListener{
      @Override
      public void paintComponent(Graphics g){
         super.paintComponent(g);
         for(int y = viewY; y < height + viewY; y++){
            for(int x = viewX; x < width+viewX; x++){
               try{
                  Model.cells.get(y).get(x).draw(g, viewX, viewY);
               } catch (Exception e){
                  continue;
               }
            }
         }
      }
      @Override
      public void actionPerformed(ActionEvent e){
         repaint();
      }
      public void mouseClicked(MouseEvent e){
         int x = e.getX() + viewX;
         int y = e.getY() + viewY;
         Model.cells.get(y).get(x).setType("fire");
         Model.cells.get(y).get(x).setAge(0);
         Model.activeFires.add(Model.cells.get(y).get(x));
      }
      
      public void mouseEntered(MouseEvent e){
         requestFocus();
      }
      public void mouseExited(MouseEvent e){
      }
      public void mouseReleased(MouseEvent e){
      }
      public void mousePressed(MouseEvent e){
      }
      
   }
}
