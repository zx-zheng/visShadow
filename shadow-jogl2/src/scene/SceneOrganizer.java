package scene;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL2GL3;



public abstract class SceneOrganizer{
  
  protected int prevMouseX, prevMouseY;
  
  public int CANVAS_WIDTH;
  public int CANVAS_HEIGHT;

  abstract public void rendering(GL2GL3 gl);
  abstract public void iterate(GL2GL3 gl);
  abstract public void keyPressed(KeyEvent e);
  abstract public void mouseDragged(MouseEvent e);
  abstract public void mousePressed(MouseEvent e);
  abstract public void mouseReleased(MouseEvent e);
  abstract public void mouseMoved(MouseEvent e);
  abstract public void switchSO(GL2GL3 gl);
  
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    CANVAS_WIDTH = width;
    CANVAS_HEIGHT = height;
  }
  
}
