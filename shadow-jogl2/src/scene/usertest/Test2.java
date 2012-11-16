package scene.usertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import scene.SceneOrganizer;
import scene.oldTypeScene.Scene1;
import util.DataSet2D;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.geom.Vector3DDouble;
import za.co.luma.geom.Vector3DInt;
import za.co.luma.math.sampling.PoissonDiskSampler;

public class Test2 extends SceneOrganizer{
  
  Scene1 scene;
  boolean SHOW_QUESTION = true;
  int PROGRESS_FRAME = 0;
  boolean NEXT_PROBLEM = false;
  DataSet2D data;
  Vector3DDouble ansPoint;
  
  public Test2(GL2GL3 gl, Scene1 scene){
    this.scene = scene;
    int dataIndex = scene.updateChoiceList(gl);
    data = scene.dataList.get(dataIndex);
    ansPoint = data.poisson.Max;
  }
  
  @Override
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    super.setCanvasSize(gl, width, height);
    scene.setCameraAspect(gl, (float) CANVAS_HEIGHT / (float) CANVAS_WIDTH);
  }

  @Override
  public void rendering(GL2GL3 gl){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

    if(SHOW_QUESTION){
      //scene.setDataShaderUniform(gl, choiceList.get(answerNum));
      scene.renderingToWindow(gl, 0, 0,
          CANVAS_WIDTH, CANVAS_HEIGHT, false);
      if(!isMeasuring){
        startTime = System.currentTimeMillis();
        isMeasuring = true;
      }
    }
  }

  @Override
  public void iterate(GL2GL3 gl){
    scene.iterate();
    if(!SHOW_QUESTION){
      PROGRESS_FRAME++;
      if(PROGRESS_FRAME > 30){
        PROGRESS_FRAME = 0;
        SHOW_QUESTION = true;
      }
    }
    if(NEXT_PROBLEM){
      int dataIndex = scene.updateChoiceList(gl);
      data = scene.dataList.get(dataIndex);
      ansPoint = data.poisson.Max;
      System.out.println("Answer" + ansPoint.x + " " + ansPoint.y);
      NEXT_PROBLEM = false;
    }
  }
  
  private void answer(Vector2DInt pos){
    if(checkAns(scene.viewportToWorld(pos, CANVAS_WIDTH, CANVAS_HEIGHT))){
      System.out.println("Correct");
    } else {
      System.out.println("Wrong");
    }
      
    SHOW_QUESTION = false;
    elapsedTime = System.currentTimeMillis() - startTime;
    System.out.println(elapsedTime / 1000.0);
    isMeasuring = false;
    NEXT_PROBLEM = true;
  }
  
  private boolean checkAns(Vector2DDouble pos){
    System.out.println(pos.x + " " + pos.y);
    double ansDist = Vector2DDouble.distance(pos, 
        new Vector2DDouble(data.poisson.Max.x, data.poisson.Max.y));
    double minDist = Double.MAX_VALUE;
    for(int i = 0; i < data.dist.size(); i++){
      double dist = Vector2DDouble.distance(pos, data.dist.get(i));
      if(dist < minDist){
        minDist = dist;
      }
    }
    if(Math.abs(ansDist - minDist) < 0.1){
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void keyPressed(KeyEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseDragged(MouseEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mousePressed(MouseEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseReleased(MouseEvent e){
    answer(new Vector2DInt(e.getX(), e.getY()));
  }

  @Override
  public void switchSO(GL2GL3 gl){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseMoved(MouseEvent e){
    // TODO Auto-generated method stub
    
  }

}
