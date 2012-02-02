
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.*;
import javax.media.opengl.awt.AWTGLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.FPSAnimator;

import util.gl.*;
  
/*
 * JOGL 2.0 Program Template For AWT applications
 */
public class JOGL2Template extends Frame implements GLEventListener,
MouseMotionListener,MouseListener,MouseWheelListener,KeyListener{
   private static final int CANVAS_WIDTH = 1664;  // Width of the drawable
   private static final int CANVAS_HEIGHT = 1024; // Height of the drawable
   private static final int FPS = 60;   // Animator's target frames per second
   SceneRender sr;
   
   // Constructor to create profile, caps, drawable, animator, and initialize Frame
   public JOGL2Template() {
      // Get the default OpenGL profile that best reflect your running platform.
      GLProfile glp = GLProfile.getDefault();
      // Specifies a set of OpenGL capabilities, based on your profile.
      GLCapabilities caps = new GLCapabilities(glp);
      // Allocate a GLDrawable, based on your OpenGL capabilities.
      GLCanvas canvas = new GLCanvas(caps);
      //canvas = new GLCanvas(arg0, arg1);
      canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
      canvas.addGLEventListener(this);
      
      // Create a animator that drives canvas' display() at 60 fps. 
      final FPSAnimator animator = new FPSAnimator(canvas, FPS);
      
      addWindowListener(new WindowAdapter() {     // For the close button
         @Override
         public void windowClosing(WindowEvent e) {
            // Use a dedicate thread to run the stop() to ensure that the
            // animator stops before program exits.
            new Thread() {
               @Override
               public void run() {
                  animator.stop();
                  System.exit(0);
               }
            }.start();
         }
      });
      add(canvas);
      pack();
      setTitle("OpenGL 2 Test");
      setVisible(true);
      animator.start();   // Start the animator
   }
   
   public static void main(String[] args) {
      new JOGL2Template();
   }
  
   @Override
   public void init(GLAutoDrawable drawable) {
     GL4bc gl = drawable.getGL().getGL4bc();
     // Your OpenGL codes to perform one-time initialization tasks 
     // such as setting up of lights and display lists.
     TexUnitManager tum = TexUnitManager.getInstance();
     tum.init(gl);
     gl.glEnable(GL2.GL_DEPTH_TEST);
     sr = new SceneRender();
     sr.init(gl);
     if (drawable instanceof AWTGLAutoDrawable) {
       AWTGLAutoDrawable awtDrawable = (AWTGLAutoDrawable) drawable;
       awtDrawable.addMouseMotionListener(this);
       awtDrawable.addMouseListener(this);
       awtDrawable.addMouseWheelListener(this);
       awtDrawable.addKeyListener(this);
     }
   }

   int count = 0;
   int nFrame = 0; 
   long nLastUpdate = 0;
   @Override
   public void display(GLAutoDrawable drawable) {
     // Your OpenGL graphic rendering codes for each refresh.
     GL4 gl = drawable.getGL().getGL4();
     //System.out.println("is EDT?"+SwingUtilities.isEventDispatchThread());
     sr.rendering(gl);
     if(nFrame++ > 200) {
       if(nLastUpdate != 0) {
   long nTimeMS = System.currentTimeMillis() - nLastUpdate ;
   System.out.println(nFrame + " in " + nTimeMS + "msec (" +
          nFrame * 1000 / nTimeMS + " FPS)") ;
       } 
       nFrame = 0 ;
       nLastUpdate = System.currentTimeMillis() ;
     }
   }
   
   @Override
   public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
     // Your OpenGL codes to set up the view port, projection mode and view volume. 
   }
   
   @Override
   public void dispose(GLAutoDrawable drawable) {
     // Hardly used.
   }

  @Override
  public void mouseDragged(MouseEvent e){
    // TODO Auto-generated method stub
    sr.mouseDragged(e);
  }

  @Override
  public void mouseMoved(MouseEvent e){
    // TODO Auto-generated method stub
    
  }


  @Override
  public void keyPressed(java.awt.event.KeyEvent arg0){
    // TODO Auto-generated method stub
    sr.keyPressed(arg0);
  }

  @Override
  public void keyReleased(java.awt.event.KeyEvent arg0){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void keyTyped(java.awt.event.KeyEvent arg0){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent arg0){
    // TODO Auto-generated method stub
    sr.mouseWheelMoved(arg0);
  }

  @Override
  public void mouseClicked(MouseEvent arg0){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseEntered(MouseEvent arg0){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseExited(MouseEvent arg0){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mousePressed(MouseEvent arg0){
    // TODO Auto-generated method stub
    sr.mousePressed(arg0);
  }

  @Override
  public void mouseReleased(MouseEvent arg0){
    // TODO Auto-generated method stub
    sr.mouseReleased(arg0);
  }
}