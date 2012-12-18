package scene.usertest;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.*;

import scene.SceneOrganizer;
import scene.oldTypeScene.Scene;
import scene.oldTypeScene.Scene1;
import za.co.luma.geom.Vector2DInt;

public class Test1 extends SceneOrganizer{
  
  int L = 70;
  
  long centerShowTime = 1500;
  long intervalTime = 1000;
  boolean SHOW_QUESTION = true;
  boolean newProblem = false;
  boolean newProblemSet = false;
  Scene1 scene;
  private int[][] SCENE_VIEWPORT = new int[5][4];
  
  //ビューポートの原点は左下
  final int[][] SUBSCENE_OFFSET =
    {{1, 0}, {0, 1}, {2, 1}, {1, 2}, {1, 1}};
  final int SCENE_UP_IDX = 3, SCENE_LEFT_IDX = 1, 
      SCENE_RIGHT_IDX = 2, SCENE_DOWN_IDX = 0;
  final int[] CENTER_SCENE_OFFSET = {1, 1};
  
  ArrayList<Vector2DInt> problemSet = new ArrayList<Vector2DInt>();
  ArrayList<Vector2DInt> originalProblemSet = new ArrayList<Vector2DInt>();
  
  int PROGRESS_FRAME = 0;
  boolean NEXT_PROBLEM = false;
  int CHOICE_NUM = 2;
  int answerNum;
  
  ArrayList<Integer> choiceList;
  
  ScheduledExecutorService scheduler;
  ScheduledFuture scheduledFuture;
  HideSchedule hideSchedule = new HideSchedule();
  
  public Test1() {

  }
  
  public void init(GL2GL3 gl, Scene1 scene, int width, int height){
    initClearColor(L);
    initoriginalProblemSet();
    this.scene = scene;
    setSceneViewport(width, height);
    newProblem(gl);
  }
  
  private void initoriginalProblemSet(){
    originalProblemSet.add(new Vector2DInt(0, 0));
    originalProblemSet.add(new Vector2DInt(0, 1));
    originalProblemSet.add(new Vector2DInt(1, 0));
    originalProblemSet.add(new Vector2DInt(1, 1));
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
    scene.setShadowTexCoordSize(subSceneWidth, subSceneHeight);
  }

  @Override
  public void rendering(GL2GL3 gl) {
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    clearWindow(gl, clearColor);
    
    if (newProblem){
      return;
    }
    
    if(isInterval){
      interval(gl, intervalTime);
      return;
    }
    
    if(isQuestioning & newProblemSet){
      showQuestion(gl);
    }
    gl.glFlush();
    
  }
  
  @Override
  public void showQuestion(GL2GL3 gl){
    if(!hideSchedule.show){
      //周辺の表示
      for (int i = 0; 
          i < Math.min(SUBSCENE_OFFSET.length, problemSet.size()); i++) {
        scene.setTargetData(gl, 
            choiceList.get(problemSet.get(i).x),
            choiceList.get(problemSet.get(i).y));
        gl.glViewport(SCENE_VIEWPORT[i][0], SCENE_VIEWPORT[i][1],
            SCENE_VIEWPORT[i][2], SCENE_VIEWPORT[i][3]);
        scene.test1Rendering(gl);
      }
    }
    
    if(hideSchedule.show){
      //中心の表示
      //scene.ShadowMap(gl, true);
      scene.setTargetData(gl, choiceList.get(answerNum));
      gl.glViewport(SCENE_VIEWPORT[4][0], SCENE_VIEWPORT[4][1],
          SCENE_VIEWPORT[4][2], SCENE_VIEWPORT[4][3]);
      scene.test1Rendering(gl);
    }
  }
  
  public void questionOld(GL2GL3 gl){
    //周辺の表示
    for (int i = 0; 
        i < Math.min(SUBSCENE_OFFSET.length, choiceList.size()); i++) {
      scene.setTargetData(gl, choiceList.get(i));
      scene.renderingToWindow(gl, SCENE_VIEWPORT[i][0], SCENE_VIEWPORT[i][1],
          SCENE_VIEWPORT[i][2], SCENE_VIEWPORT[i][3], false);
    }
    //中心の表示
    //scene.ShadowMap(gl, true);
    scene.setTargetData(gl, choiceList.get(answerNum));
    scene.renderingToWindow(gl, SCENE_VIEWPORT[4][0], SCENE_VIEWPORT[4][1],
        SCENE_VIEWPORT[4][2], SCENE_VIEWPORT[4][3], false);
  }
  
  @Override
  public void newProblem(){
    newProblem = true;
  }
  
  @Override
  public void endQuestion(){
    super.endQuestion();
    newProblemSet = false;
  }
  
  private void newProblem(GL2GL3 gl){
    for (Vector2DInt problem : problemSet){
      originalProblemSet.add(problem);
    }
    problemSet.clear();
    choiceList = scene.resetAndGetChoiceList(gl, CHOICE_NUM);
    answerNum = (int)(Math.random() * choiceList.size());
    while (originalProblemSet.size() > 0){
      int index = (int) (Math.random() * originalProblemSet.size());
      problemSet.add(originalProblemSet.get(index));
      originalProblemSet.remove(index);
    }
    newProblemSet = true;
    //startQuestion();
    scheduledFuture = scheduler.schedule(hideSchedule, centerShowTime, TimeUnit.MILLISECONDS);
  }
  
  private void answer(int ans){
    if(ans == answerNum){
      correct();
    } else {
      wrong();
    }
    endQuestion();   
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
    
    if(newProblem){
      newProblem(gl);
      newProblem = false;
    }
    
    if (isAnswered) {
      isInterval = true; 
      return;
    } else if (isInterval) {
      startQuestion();
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

  @Override
  public void actionPerformed(ActionEvent e){
    super.actionPerformed(e);    
  }
  
  class HideSchedule implements Runnable{

    public boolean show;
    @Override
    public void run(){
      System.out.println("run");
      show = false;
    }   
  }

}
