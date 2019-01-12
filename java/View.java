import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
public class View extends JFrame {
   private Timer drawTime;
   private DrawCanvas canvas = new DrawCanvas();
   private int viewY;
   private int viewX;
   private int zoomFactor = 1;
   public View(int w,int h){
      super();
      viewY = 0;
      viewX = 0;
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
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,KeyEvent.SHIFT_DOWN_MASK), ZOOMIN);
      canvas.getActionMap().put(ZOOMIN,zoomin);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,0), ZOOMOUT);
      canvas.getActionMap().put(ZOOMOUT,zoomout);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R,0), RESET);
      canvas.getActionMap().put(RESET,reset);
   }
   private static final String LEFT = "LEFT";
   private static final String RIGHT = "RIGHT";
   private static final String UP = "UP";
   private static final String DOWN = "DOWN";
   private static final String ZOOMIN = "ZOOMIN";
   private static final String ZOOMOUT = "ZOOMOUT";
   private static final String RESET = "RESET";

   private Action reset = new AbstractAction(RESET){
      @Override
      public void actionPerformed(ActionEvent e){
         Controller.reset();
      }
   };
   private Action zoomin = new AbstractAction(ZOOMIN){
      @Override
      public void actionPerformed(ActionEvent e){
         zoomFactor--;
      }
   };
   private Action zoomout = new AbstractAction(ZOOMOUT){
      @Override
      public void actionPerformed(ActionEvent e){
         zoomFactor++;
      }
   };
   private Action down = new AbstractAction(DOWN){
      @Override
      public void actionPerformed(ActionEvent e){
         viewY+=canvas.getHeight()/2 * zoomFactor;
         if(viewY + canvas.getHeight() > Model.nrows){
            viewY = Model.nrows - canvas.getHeight();
         }
      }
   };
   private Action right = new AbstractAction(RIGHT){
      @Override
      public void actionPerformed(ActionEvent e){
         viewX+=canvas.getWidth()/2 * zoomFactor;
         if(viewX + canvas.getWidth() > Model.ncols){
            viewX = Model.ncols - canvas.getWidth();
         }
      }
   };
   private Action left = new AbstractAction(LEFT){
      @Override
      public void actionPerformed(ActionEvent e){
         viewX-=canvas.getWidth()/2 * zoomFactor;
         if(viewX < 0){
            viewX = 0;
         }
      }
   };
   private Action up = new AbstractAction(UP){
      @Override
      public void actionPerformed(ActionEvent e){
         viewY-= canvas.getHeight()/2 * zoomFactor;
         if(viewY < 0){
            viewY = 0;
         }
      }
   };

   private class DrawCanvas extends JPanel implements MouseListener, ActionListener{
      @Override
      public void paintComponent(Graphics g){
         int zoom = zoomFactor;
         super.paintComponent(g);
         for(int y = viewY; y   < (this.getHeight() * zoom) + viewY; y+=zoom){
            for(int x = viewX; x < (this.getWidth() * zoom) + viewX; x+=zoom){
               try{
                  Model.cells.get(y).get(x).draw(g, viewX, viewY, zoom);
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
         try{
            int x = (e.getX() * zoomFactor) + viewX;
            int y = (e.getY() * zoomFactor) + viewY;
            Model.cells.get(y).get(x).setType("fire");
            Model.cells.get(y).get(x).setAge(0);
            Model.activeFires.add(Model.cells.get(y).get(x));
         } catch (IndexOutOfBoundsException obe){
            //System.out.println("oob");
         }
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
