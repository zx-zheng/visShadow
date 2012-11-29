package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import gl.*;
import gui.Ctrlpanel;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import javax.media.opengl.*;
import javax.media.opengl.awt.AWTGLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;

import com.jogamp.opengl.util.FPSAnimator;

import scene.SceneRender;

/*
 * JOGL 2.0 Program Template For AWT applications
 */
public class JOGL2Template implements
MouseMotionListener, MouseListener, MouseWheelListener, KeyListener{
  private static final int CANVAS_WIDTH = 1920;  // Width of the drawable
  private static final int CANVAS_HEIGHT = 1024; // Height of the drawable
  private static final int FPS = 30;   // Animator's target frames per second
  SceneRender sr;
  Ctrlpanel ctrlpanel;
  GLCanvas glcanvas;

  // Constructor to create profile, caps, drawable, animator, and initialize Frame
  public JOGL2Template() {
    // Get the default OpenGL profile that best reflect your running platform.
    GLProfile glp = GLProfile.get(GLProfile.GL4bc);
    // Specifies a set of OpenGL capabilities, based on your profile.
    GLCapabilities caps = new GLCapabilities(glp);
    caps.setNumSamples(2); // enable anti aliasing - just as a example
    // Allocate a GLDrawable, based on your OpenGL capabilities.
    glcanvas = new GLCanvas(caps);
    glcanvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

    final FPSAnimator animator = new FPSAnimator(glcanvas, FPS, true);

    final JFrame jframe = new JFrame( "One Triangle Swing GLCanvas" ); 

    glcanvas.addGLEventListener( new GLEventListener() {

      @Override
      public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {

      }

      @Override
      public void init( GLAutoDrawable glautodrawable ) {
        init_(glautodrawable);
        SwingUtilities.invokeLater(new Runnable(){
          @Override
          public void run(){
            // TODO 自動生成されたメソッド・スタブ
            jframe.setVisible(true);
          }
        });
      }

      @Override
      public void dispose( GLAutoDrawable glautodrawable ) {
      }

      @Override
      public void display( GLAutoDrawable glautodrawable ) {
        display_(glautodrawable);
      }

    });

    jframe.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        // Use a dedicate thread to run the stop() to ensure that the
        // animator stops before program exits.
        new Thread() {
          @Override
          public void run() {
            if (animator.isStarted()) animator.stop();
            System.exit(0);
          }
        }.start();
      }
    });

    jframe.getContentPane().add(glcanvas, BorderLayout.CENTER);
    jframe.getContentPane().add(Ctrlpanel.getInstance().getPanel(), 
        BorderLayout.EAST);
    jframe.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
    jframe.setVisible(true);
    animator.start();
  }

  public static void main(String[] args) {
    new JOGL2Template();
  }

  public void init_(GLAutoDrawable drawable) {
    GL4bc gl = drawable.getGL().getGL4bc();
    // Your OpenGL codes to perform one-time initialization tasks 
    // such as setting up of lights and display lists.
    TexUnitManager tum = TexUnitManager.getInstance();
    tum.init(gl);
    gl.glEnable(GL2.GL_DEPTH_TEST);
    sr = new SceneRender();
    sr.init(gl);
    sr.CANVAS_WIDTH = glcanvas.getWidth();
    sr.CANVAS_HEIGHT = glcanvas.getHeight();
    
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

  public void display_(GLAutoDrawable drawable) {
    sr.CANVAS_WIDTH = glcanvas.getWidth();
    sr.CANVAS_HEIGHT = glcanvas.getHeight();
    // Your OpenGL graphic rendering codes for each refresh.
    GL4 gl = drawable.getGL().getGL4();
    //System.out.println("is EDT?"+SwingUtilities.isEventDispatchThread());
    sr.rendering(gl);
//    if(nFrame++ > 200) {
//      if(nLastUpdate != 0) {
//        long nTimeMS = System.currentTimeMillis() - nLastUpdate ;
//        System.out.println(nFrame + " in " + nTimeMS + "msec (" +
//            nFrame * 1000 / nTimeMS + " FPS)") ;
//      } 
//      nFrame = 0 ;
//      nLastUpdate = System.currentTimeMillis() ;
//    }
  }

  @Override
  public void mouseDragged(MouseEvent e){
    sr.mouseDragged(e);
  }

  @Override
  public void mouseMoved(MouseEvent e){
    sr.mouseMoved(e);
  }


  @Override
  public void keyPressed(java.awt.event.KeyEvent arg0){
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
    sr.mousePressed(arg0);
  }

  @Override
  public void mouseReleased(MouseEvent arg0){
    sr.mouseReleased(arg0);
  }
}