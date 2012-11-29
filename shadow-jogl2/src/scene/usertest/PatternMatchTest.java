package scene.usertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import com.jogamp.common.util.IntIntHashMap;

import scene.SceneOrganizer;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;

public class PatternMatchTest extends SceneOrganizer{
  
  //バックグラウンドの明度
  int L = 70;
  long intervalTime = 1000;
  ColorMosaic colorMosaic; 
  int mosaicGridSize = 15;
  int mosaicViewportSize;
  boolean isShadowed = false;
  //探索するパターンのサイズ
  int patternSize = 3;
  Vector2DInt correctAnsPoint = new Vector2DInt(0, 0);
  //フェイクの数
  int fakeCount = 8;
  
  public PatternMatchTest(int numberOfQuestion){
    this.numberOfQuestion = numberOfQuestion;
  }
  
  public void init(GL2GL3 gl){
    initClearColor(L);
    colorMosaic = new ColorMosaic();
    colorMosaic.init(gl);
    colorMosaic.genColorMosaic(mosaicGridSize, isShadowed);
    colorMosaic.setPatternSize(patternSize);
    startQuestion();
  }

  @Override
  public void rendering(GL2GL3 gl){
    clearWindow(gl, clearColor);
    
    if(isInterval){
      interval(gl, intervalTime);
    }
   
    if(isQuestioning){
      showQuestion(gl);
    }
    
    gl.glFlush();
  }
  
  @Override
  public void showQuestion(GL2GL3 gl){
    mosaicViewportSize = Math.min(CANVAS_WIDTH, CANVAS_HEIGHT);
    gl.glViewport(0, 0, mosaicViewportSize, mosaicViewportSize);
    colorMosaic.rendering(gl);
    
    int patternWindowSize =
        (int) (((double) patternSize / mosaicGridSize) * mosaicViewportSize);
    gl.glViewport(mosaicViewportSize + 10,
         10,
        patternWindowSize, patternWindowSize);
    colorMosaic.renderingMosaic(gl, correctAnsPoint, patternSize);
  }
  
  @Override
  public void newProblem(){
    correctAnsPoint.x = (int) (Math.random() * (mosaicGridSize - patternSize));
    correctAnsPoint.y = (int) (Math.random() * (mosaicGridSize - patternSize));
    colorMosaic.genColorMosaic(mosaicGridSize, isShadowed);
    colorMosaic.genFakeAnswer(fakeCount, correctAnsPoint, isShadowed);
  }
  
  private void answer(MouseEvent e){
    Vector2DDouble ansPoint = windowCoordToWorldCoord(e.getX(), e.getY());
    //System.out.println(ansPoint);
    ansPoint = Vector2DDouble.scale(ansPoint, mosaicGridSize);
    if((int)ansPoint.x == correctAnsPoint.x
        && (int) ansPoint.y == correctAnsPoint.y ){
      correct();
    } else {
      wrong();
    }
    
    endQuestion();
//    System.out.println(answerOutput);
//    System.out.println(elapsedTime);
  }
  
  private Vector2DDouble windowCoordToWorldCoord(int x, int y){
    return new Vector2DDouble(
        (double) x / mosaicViewportSize,
        (double) y / mosaicViewportSize);
  }

  @Override
  public void iterate(GL2GL3 gl) {
    if (numberOfAnsweredQuestion == numberOfQuestion) {
      endTest();
    } else if (isAnswered) {
      isInterval = true; 
      return;
    } else if (isInterval) {
      startQuestion();
      return;
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
