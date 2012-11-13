package scene.usertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.media.opengl.*;

import render.Scene;
import scene.Scene1;
import scene.SceneOrganizer;

public class Test1 extends SceneOrganizer{
  
  boolean SHOW_QUESTION = true;
  Scene1 scene;
  private int[][] SCENE_VIEWPORT = new int[5][4];
  
  //ビューポートの原点は左下
  final int[][] SUBSCENE_OFFSET =
    {{1, 0}, {0, 1}, {2, 1}, {1, 2}, {1, 1}};
  final int SCENE_UP_IDX = 3, SCENE_LEFT_IDX = 1, 
      SCENE_RIGHT_IDX = 2, SCENE_DOWN_IDX = 0;
  final int[] CENTER_SCENE_OFFSET = {1, 1};
  
  int PROGRESS_FRAME = 0;
  long ELAPSED_TIME = 0;
  long START_TIME = 0;
  boolean measuring = false;
  boolean NEXT_PROBLEM = false;
  int CHOICE_NUM = 4;
  int answerNum;
  
  ArrayList<Integer> choiceList;
  
  public Test1(GL2GL3 gl, Scene1 scene, int width, int height) {
    this.scene = scene;
    setSceneViewport(width, height);
    initAnswer(gl);
  }
  
  @Override
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    super.setCanvasSize(gl, width, height);
    scene.setCameraAspect(gl, (float) CANVAS_HEIGHT / (float) CANVAS_WIDTH);
    setSceneViewport(width, height);
  }
  
  private void setSceneViewport(int width, int height) {
    int subSceneWidth = width / 3;
    int subSceneHeight = height / 3;
    for(int i = 0; i < SUBSCENE_OFFSET.length; i++) {
      SCENE_VIEWPORT[i][0] = subSceneWidth * SUBSCENE_OFFSET[i][0];
      SCENE_VIEWPORT[i][1] = subSceneHeight * SUBSCENE_OFFSET[i][1];
      SCENE_VIEWPORT[i][2] = subSceneWidth;
      SCENE_VIEWPORT[i][3] = subSceneHeight;
    }
  }

  @Override
  public void rendering(GL2GL3 gl) {
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    
    if(SHOW_QUESTION){
      //周辺の表示
      for (int i = 0; 
          i < Math.min(SUBSCENE_OFFSET.length, choiceList.size()); i++) {
        scene.setDataShaderUniform(gl, choiceList.get(i));
        scene.renderingToWindow(gl, SCENE_VIEWPORT[i][0], SCENE_VIEWPORT[i][1],
            SCENE_VIEWPORT[i][2], SCENE_VIEWPORT[i][3], false);
      }
      //中心の表示
      //scene.ShadowMap(gl, true);
      scene.setDataShaderUniform(gl, choiceList.get(answerNum));
      scene.renderingToWindow(gl, SCENE_VIEWPORT[4][0], SCENE_VIEWPORT[4][1],
          SCENE_VIEWPORT[4][2], SCENE_VIEWPORT[4][3], false);
      if(!measuring){
        START_TIME = System.currentTimeMillis();
        measuring = true;
      }
    }
    gl.glFlush();
    
  }
  
  private void initAnswer(GL2GL3 gl){
    choiceList = scene.resetAndGetChoiceList(gl, CHOICE_NUM);
    answerNum = (int)(Math.random() * choiceList.size());
  }
  
  private void answer(int ans){
    if(ans == answerNum){
      System.out.println("Correct");
    } else {
      System.out.println("Wrong");
    }
    SHOW_QUESTION = false;
    ELAPSED_TIME = System.currentTimeMillis() - START_TIME;
    System.out.println(ELAPSED_TIME / 1000.0);
    measuring = false;
    NEXT_PROBLEM = true;
    //次の答えを設定
    answerNum = (int)(Math.random() * choiceList.size());
  }
  
  private void saveAnswer(boolean ans, long time){
    
  }
  
  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case KeyEvent.VK_UP:
      answer(SCENE_UP_IDX);
      break;
    case KeyEvent.VK_LEFT:
      answer(SCENE_LEFT_IDX);
      break;
    case KeyEvent.VK_DOWN:
      answer(SCENE_DOWN_IDX);
      break;
    case KeyEvent.VK_RIGHT:
      answer(SCENE_RIGHT_IDX);
      break;
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
      choiceList = scene.resetAndGetChoiceList(gl, CHOICE_NUM);
      NEXT_PROBLEM = false;
    }
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
    // TODO Auto-generated method stub
    
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
