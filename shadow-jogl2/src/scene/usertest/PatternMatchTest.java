package scene.usertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import scene.SceneOrganizer;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;

public class PatternMatchTest extends SceneOrganizer{
  
  boolean NEXT_PROBLEM = false;
  
  ColorMosaic colorMosaic; 
  int mosaicGridSize = 30;
  int mosaicViewportSize;
  //探索するパターンのサイズ
  int patternSize = 5;
  Vector2DInt correctAnsPoint = new Vector2DInt(0, 0);
  
  public PatternMatchTest(){
    // TODO Auto-generated constructor stub
  }
  
  public void init(GL2GL3 gl){
    colorMosaic = new ColorMosaic();
    colorMosaic.init(gl);
    colorMosaic.genColorMosaic(gl, mosaicGridSize);
    colorMosaic.setPatternSize(patternSize);
    newProblem();
  }

  @Override
  public void rendering(GL2GL3 gl){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    mosaicViewportSize = Math.min(CANVAS_WIDTH, CANVAS_HEIGHT);
    gl.glViewport(0, 0, mosaicViewportSize, mosaicViewportSize);
    colorMosaic.rendering(gl);
    
    int patternWindowSize =
        (int) (((double) patternSize / mosaicGridSize) * mosaicViewportSize);
    gl.glViewport(mosaicViewportSize + 10,
         10,
        patternWindowSize, patternWindowSize);
    colorMosaic.renderingSubMosaic(gl, correctAnsPoint, patternSize);
    gl.glFlush();
  }
  
  private void newProblem(){
    correctAnsPoint.x = (int) (Math.random() * (mosaicGridSize - patternSize));
    correctAnsPoint.y = (int) (Math.random() * (mosaicGridSize - patternSize));
  }
  
  private void answer(MouseEvent e){
    Vector2DDouble ansPoint = windowCoordToWorldCoord(e.getX(), e.getY());
    System.out.println(ansPoint);
    ansPoint = Vector2DDouble.scale(ansPoint, mosaicGridSize);
    if((int)ansPoint.x == correctAnsPoint.x
        && (int) ansPoint.y == correctAnsPoint.y ){
      System.out.println("Correct");
    } else {
      System.out.println("wrong");
    }
    NEXT_PROBLEM = true;
  }
  
  private Vector2DDouble windowCoordToWorldCoord(int x, int y){
    return new Vector2DDouble(
        (double) x / mosaicViewportSize,
        (double) y / mosaicViewportSize);
  }

  @Override
  public void iterate(GL2GL3 gl){
    // TODO Auto-generated method stub
    if(NEXT_PROBLEM){
      newProblem();
      colorMosaic.genColorMosaic(gl, mosaicGridSize);
      NEXT_PROBLEM = false;
    }
  }

  @Override
  public void keyPressed(KeyEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseDragged(MouseEvent e){
    colorMosaic.setMarkPos(windowCoordToWorldCoord(e.getX(), e.getY()));
  }

  @Override
  public void mousePressed(MouseEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseReleased(MouseEvent e){
    answer(e);
  }

  @Override
  public void switchSO(GL2GL3 gl){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseMoved(MouseEvent e){
    colorMosaic.setMarkPos(windowCoordToWorldCoord(e.getX(), e.getY()));
  }

}
