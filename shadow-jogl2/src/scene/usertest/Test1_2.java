package scene.usertest;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.*;

import main.Main;

import scene.SceneOrganizer;
import scene.obj.Billboard;
import scene.oldTypeScene.Scene;
import scene.oldTypeScene.Scene1;
import scene.usertest.Test3_2.MarkTypeAndAlpha;
import util.DataSet2D;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.geom.Vector3DDouble;

public class Test1_2 extends SceneOrganizer{
  
  private final String TEST_VERSION = "1.1.0";
  private final String TEST_NAME = "Test1_2";
  
  int L = 69;
  
  long centerShowTime = 1500;
  long intervalTime = 1000;
  boolean SHOW_QUESTION = true;
  Scene1 scene;
  private int[][] SCENE_VIEWPORT = new int[5][4];
  
  //ビューポートの原点は左下
  final int[][] SUBSCENE_OFFSET =
    {{1, 0}, {0, 1}, {2, 1}, {1, 2}, {1, 1}};
  final int SCENE_UP_IDX = 3, SCENE_LEFT_IDX = 1, 
      SCENE_RIGHT_IDX = 2, SCENE_DOWN_IDX = 0;
  final int[] CENTER_SCENE_OFFSET = {1, 1};
  int numberOfChoice = 4;
  
  ArrayList<Vector2DInt> scenePosSet = new ArrayList<Vector2DInt>();
  ArrayList<Vector2DInt> originalScenePosSet = new ArrayList<Vector2DInt>();
  
  int PROGRESS_FRAME = 0;
  boolean NEXT_PROBLEM = false;
  int CHOICE_NUM = 2;
  int answerNum;
  MarkTypeAndAlpha currentProblem;
  ArrayList<MarkTypeAndAlpha> problemList;
  
  ArrayList<Integer> choiceList;
  ArrayList<DataSet2D> choiceDataList;
  
  int numberOfQuestionPerType;
  
  int typeOfPattern = 3;
  //Billboard whiteCircle, blackCircle, whiteShadow, blackShadow;
  float billBoardSize = 
      (float) (Scene1.shadowTexSize * billBoardTexSizeRatio);

  float[] blackAlphaArray = {0.14f, 0.16f, 0.18f, 0.2f};
  float[] whiteAlphaArray = {0.16f, 0.18f, 0.20f, 0.22f};
  int[] shadowRangeArray = {10, 10, 14, 14};
  
  ScheduledExecutorService scheduler;
  ScheduledFuture scheduledFuture;
  HideSchedule hideSchedule;
  
  public Test1_2(int numberOfQuestionPerType) {
    this.numberOfQuestionPerType = numberOfQuestionPerType;
    //this.numberOfQuestion = 0;
    this.numberOfQuestion = numberOfQuestionPerType * typeOfPattern * blackAlphaArray.length;
    SetTestNameVersion(TEST_NAME, TEST_VERSION);
  }
  
  public void init(GL2GL3 gl, Scene1 scene, int width, int height){
    initClearColor(L);
    initoriginalProblemSet();
    //initMark(gl);
    super.initBillBoard(gl);
    this.scene = scene;
    setSceneViewport(width, height);
    hideSchedule = new HideSchedule();
    scheduler = Executors.newSingleThreadScheduledExecutor();
    problemList = new ArrayList<Test1_2.MarkTypeAndAlpha>();
    //newProblem(gl);
  }
  
//  public void initMark(GL2GL3 gl){
//    whiteCircle = new Billboard(gl, 
//        "resources/Image/TextureImage/circlewhite.png", billBoardSize);
//    whiteCircle.setAlpha(0.2f);
//    whiteShadow = new Billboard(gl, "resources/Image/TextureImage/whiteshadow4.png",
//        new Vector2DDouble(0, 0.8), 1.5);
//    whiteShadow.setAlpha(0.2f);
//    
//    blackCircle = new Billboard(gl, 
//        "resources/Image/TextureImage/circle.png", billBoardSize);
//    blackCircle.setAlpha(0.2f);
//    blackShadow = new Billboard(gl, "resources/Image/TextureImage/shadow4.png",
//        new Vector2DDouble(0, 0.8), 1.5);
//    blackShadow.setAlpha(0.2f);
//  }
  
  private void initoriginalProblemSet(){
    originalScenePosSet.add(new Vector2DInt(0, 0));
    originalScenePosSet.add(new Vector2DInt(0, 1));
    originalScenePosSet.add(new Vector2DInt(1, 0));
    originalScenePosSet.add(new Vector2DInt(1, 1));
  }
  
  @Override
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    super.setCanvasSize(gl, width, height);
    scene.setCameraAspect(gl, (float) CANVAS_HEIGHT / (float) CANVAS_WIDTH);
    setSceneViewport(width, height);
  }
  
  private void setSceneViewport(int width, int height) {
    int offset = 10;
    int subSceneWidth = (width - 2 * offset) / 3;
    int subSceneHeight = (height - 2 * offset) / 3;
    for(int i = 0; i < SUBSCENE_OFFSET.length; i++) {
      SCENE_VIEWPORT[i][0] = subSceneWidth * SUBSCENE_OFFSET[i][0] + offset;
      SCENE_VIEWPORT[i][1] = subSceneHeight * SUBSCENE_OFFSET[i][1] + offset;
      SCENE_VIEWPORT[i][2] = subSceneWidth;
      SCENE_VIEWPORT[i][3] = subSceneHeight;
    }
    scene.setShadowTexCoordSize(subSceneWidth, subSceneHeight);
  }
  

  @Override
  public void rendering(GL2GL3 gl) {
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    clearWindow(gl, clearColor);
    
    if(isQuestioning){
      showQuestion(gl);
    }
    gl.glFlush();
    
  }
  
  @Override
  public void showQuestion(GL2GL3 gl){
    
    if(hideSchedule.show){
      //中心の表示
      //scene.ShadowMap(gl, true);
//      scene.setTargetData(gl, 
//          choiceList.get(scenePosSet.get(answerNum).x),
//          choiceList.get(scenePosSet.get(answerNum).y));
      scene.setTargetData(gl, 
          choiceDataList.get(scenePosSet.get(answerNum).x),
          choiceDataList.get(scenePosSet.get(answerNum).y));
      gl.glViewport(SCENE_VIEWPORT[4][0], SCENE_VIEWPORT[4][1],
          SCENE_VIEWPORT[4][2], SCENE_VIEWPORT[4][3]);
      scene.test1Rendering(gl, currentProblem);
    }
    
    if(!hideSchedule.show & hideSchedule.resampled){
      //周辺の表示
      for (int i = 0; 
          i < numberOfChoice; i++) {
//        scene.setTargetData(gl, 
//            choiceList.get(scenePosSet.get(i).x),
//            choiceList.get(scenePosSet.get(i).y));
        scene.setTargetData(gl, 
            choiceDataList.get(scenePosSet.get(i).x),
            choiceDataList.get(scenePosSet.get(i).y));
        gl.glViewport(SCENE_VIEWPORT[i][0], SCENE_VIEWPORT[i][1],
            SCENE_VIEWPORT[i][2], SCENE_VIEWPORT[i][3]);
        scene.test1Rendering(gl, currentProblem);
      }
    }
    
  }
  
  @Override
  protected void startTest(){
    genProblemList();
    Main.requestFocus();
    super.startTest();
  }
  
  private void genProblemList(){
    numberOfQuestion = 0;
    problemList.clear();
    for(int j = 0; j < numberOfQuestionPerType; j++){
//      problemList.add(new MarkTypeAndAlpha(blackCircle, 0, blackAlpha, "blackCircle"));
//      problemList.add(new MarkTypeAndAlpha(blackHardCircle, 0, blackAlpha, "blackHardCircle"));
//      problemList.add(new MarkTypeAndAlpha(shadow, 0, blackAlpha, "shadow"));
//      problemList.add(new MarkTypeAndAlpha(hardShadow, 0, blackAlpha, "hardshadow"));
//      problemList.add(new MarkTypeAndAlpha(blackCircle, 1, lDown, "blackCircle"));
//      problemList.add(new MarkTypeAndAlpha(blackHardCircle, 1, lDown, "blackHardCircle"));
      problemList.add(new MarkTypeAndAlpha(shadow, 1, lDown, "shadow"));
//      problemList.add(new MarkTypeAndAlpha(hardShadow, 1, lDown, "hardshadow"));
      numberOfQuestion += 8;
    }
  }
 
  @Override
  public void endQuestion(){
    super.endQuestion();
  }
  
  //表示時間の精度上げるために別の場所
  @Override
  public void newProblem(){
    scheduledFuture = 
        scheduler.schedule(hideSchedule, centerShowTime, TimeUnit.MILLISECONDS);
    hideSchedule.resampled = false;
  }
  
  private void newProblem(GL2GL3 gl){
    System.out.println("Question" + (numberOfAnsweredQuestion+1) + "/" + numberOfQuestion);
    for (Vector2DInt problem : scenePosSet){
      originalScenePosSet.add(problem);
    }
    scenePosSet.clear();
    
    //choiceList = scene.resetAndGetChoiceList(gl, CHOICE_NUM);
    choiceDataList = scene.getTwoDataSet(gl);
    
    answerNum = (int)(Math.random() * numberOfChoice);
    
    while (originalScenePosSet.size() > 0){
      int index = (int) (Math.random() * originalScenePosSet.size());
      scenePosSet.add(originalScenePosSet.get(index));
      originalScenePosSet.remove(index);
    }  
    
    hideSchedule.show = true;
    int index = (int) (Math.random() * problemList.size());
    
    currentProblem = problemList.get(index);
    if(currentProblem.billBoard != null){
      currentProblem.billBoard.setAlpha(currentProblem.alpha);
    }
    System.out.println(currentProblem.name);
    problemList.remove(index);
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
    scene.iterate(gl);
    
    if (!isDemo & numberOfAnsweredQuestion == numberOfQuestion) {
      endTest();
    } else if (nextProblem) {
      newProblem(gl);
      startQuestion();
    }
  }
  
  protected void initOutFile(){
    super.initOutFile();
    answerOutput
        .append("lab_l = ").append(scene.lab_l.getValue()).append("\n")
        .append("lab_a = ").append(scene.lab_a.getValue()).append("\n")
        .append("lab_b = ").append(scene.lab_b.getValue()).append("\n")
        .append("shadeRange = ").append(scene.shaderange.getValue()).append("\n")
        .append("poissonInterval = ").append(scene.possioninterval.getValue()).append("\n")
        .append("viewScale = ").append(scene.viewScale.getValue()).append("\n")
        .append("centerShowTime = ").append(centerShowTime).append("\n")
//        +"marktype, error, alpha, correct, posx, posy, temperature, time\n";
        .append("模様の種類, マークの表示法 0:alpha 1:L, alpha, 間違えたもの, 正誤, 解答時間\n");
  }
  
  private void answer(int ans){
    if(!isQuestioning | hideSchedule.show) return;
    String answerCheck = "";
    if(scenePosSet.get(answerNum).x != scenePosSet.get(ans).x){
      answerCheck += "Mark";
    }
    if(scenePosSet.get(answerNum).y != scenePosSet.get(ans).y){
      answerCheck += "Color";
    }
    System.out.println(answerCheck);
    answerOutput.append(currentProblem.name)
        .append(", ")
        .append(currentProblem.combineType)
        .append(", ")
        .append(currentProblem.alpha) 
        .append(", ") 
        .append(answerCheck)
        .append(", ");
    if(ans == answerNum){
      correct();
    } else {
      wrong();
    }
    endQuestion();   
    System.out.println(ans);
    System.out.println(answerNum);
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
    scene.actionPerformed(e);
  }
  
  public class MarkTypeAndAlpha{
    public String name;
    public Billboard billBoard;
    public int combineType;
    public float alpha;
    
    public MarkTypeAndAlpha(Billboard billBoard, int combineType, float alpha, String name){
      this.billBoard = billBoard;
      this.combineType = combineType;
      this.alpha = alpha;
      this.name = name;
    }
    
  }
  
  
  class HideSchedule implements Runnable{

    public boolean show;
    public boolean resampled = false;
    @Override
    public void run(){
      //System.out.println("run");
      show = false;
      resampled = scene.resamplePoisson(choiceDataList);
    }   
  }

}
