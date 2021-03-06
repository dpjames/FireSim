import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
public class View extends JFrame {
   public static Color VALLEY_COLOR = Color.GREEN;
   public static Color RIDGE_COLOR = Color.BLUE;
   public static Color EQ_COLOR = Color.ORANGE;
   private static Color TEXT_COLOR = Color.BLUE;
   private int curView = 0;
   private Timer drawTime;
   private DrawCanvas canvas = new DrawCanvas();
   private int viewY;
   private int viewX;
   private int zoomFactor = 1;
   private int skipValue = 1;
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
      drawTime = new Timer(0, canvas);
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
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_DOWN_MASK), RESET);
      canvas.getActionMap().put(RESET,reset);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R,0), TOGGLE_RUN);
      canvas.getActionMap().put(TOGGLE_RUN,toggle_run);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_M,0), SKIP_MORE);
      canvas.getActionMap().put(SKIP_MORE,skip_more);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_N,0), SKIP_LESS);
      canvas.getActionMap().put(SKIP_LESS,skip_less);
      canvas.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C,0), CHANGE_VIEW);
      canvas.getActionMap().put(CHANGE_VIEW,change_view);
   }
   private static final String TOGGLE_RUN = "TOGGLE_RUN";
   private static final String LEFT = "LEFT";
   private static final String RIGHT = "RIGHT";
   private static final String UP = "UP";
   private static final String DOWN = "DOWN";
   private static final String ZOOMIN = "ZOOMIN";
   private static final String ZOOMOUT = "ZOOMOUT";
   private static final String RESET = "RESET";
   private static final String SKIP_MORE = "SKIP_MORE";
   private static final String SKIP_LESS = "SKIP_LESS";
   private static final String CHANGE_VIEW = "CHANGE_VIEW";
   private Action change_view = new AbstractAction(CHANGE_VIEW) {
      @Override
      public void actionPerformed(ActionEvent e) {
         if(curView == 0 || curView == 1){
            curView++;
         } else {
            curView = 0;
         }
      }
   };
   private Action skip_more = new AbstractAction(SKIP_MORE){
      @Override
      public void actionPerformed(ActionEvent e){
         skipValue++;
      }
   };
   private Action skip_less = new AbstractAction(SKIP_LESS){
      @Override
      public void actionPerformed(ActionEvent e){
         skipValue = skipValue - 1 <= 0 ? 1 : skipValue - 1;
      }
   };
   private Action reset = new AbstractAction(RESET){
      @Override
      public void actionPerformed(ActionEvent e){
         Controller.reset();
      }
   };
   private Action toggle_run = new AbstractAction(TOGGLE_RUN){
      @Override
      public void actionPerformed(ActionEvent e){
         Controller.toggleRun();
      }
   };
   private Action zoomin = new AbstractAction(ZOOMIN){
      @Override
      public void actionPerformed(ActionEvent e){
         zoomFactor = zoomFactor - 1 < 1 ? 1 : zoomFactor - 1;
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
      private long lastTime = System.currentTimeMillis();
      private int fc = 0;
      @Override
      public void paintComponent(Graphics g) {

         super.paintComponent(g);
         int zoom = zoomFactor;
         for (int y = viewY; y < (this.getHeight() * (zoom)) + viewY; y += (zoom + skipValue)) {
            for (int x = viewX; x < (this.getWidth() * (zoom)) + viewX; x += (zoom + skipValue)) {
               try {
                  Model.cells.get(y).get(x).draw(g, viewX, viewY, zoom, skipValue, curView);
               } catch (Exception e) {
                  continue;
               }
            }
         }

         long now = System.currentTimeMillis();
         g.setColor(TEXT_COLOR);
         long delta = now - lastTime;
         delta = delta <= 0 ? 1 : delta;
         g.drawString(1000 / delta + "",20,20);
         lastTime = now;
      }
      @Override
      public void actionPerformed(ActionEvent e){
         repaint();
      }
      public void mouseClicked(MouseEvent e){
         try{
            int x = (e.getX() * zoomFactor) + viewX;
            int y = (e.getY() * zoomFactor) + viewY;
            System.out.println("starting fire at " + x + ", " + y);
            Model.startFire(x,y);

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
