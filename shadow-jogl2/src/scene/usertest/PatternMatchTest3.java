package scene.usertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GL2GL3;

import scene.obj.Billboard;

import za.co.luma.geom.Vector3DDouble;

import main.Main;

public class PatternMatchTest3 extends PatternMatchTest{
  
  private final String TEST_VERSION = "1.0.0";
  private final String TEST_NAME = "PatternMatch3";

  final int typeOfQuestion = 5;
  /*
   * 0:同じ
   * 1:同じ模様あり
   * 5:模様による変化と同じ色の変化模様表示なし
   * 6:真の色(模様による変化と同じ変化がかかっている)と(地の色＋模様)が同じ色
   */
  int typeOfMark = 2 + 1;
  Billboard blackCircle;
  float billBoardSize = (float) (Math.sqrt(2) / 3 * 1.3);
  
  Vector3DDouble black = new Vector3DDouble(0, 0, 0);
  Vector3DDouble[] markArray;
  
  float[] blackAlphaArray = {0.14f, 0.16f, 0.18f, 0.2f};
  
  int[] shadowRangeArray = {10, 10, 14, 14};
  
  int numberOfQuestionPerType;
  ProblemSet currentProblem;
  int mosaicGridSize = 1;
  
  //今回はfalse
  boolean isShadowed = false;
  int mosaicViewportSize = 150;
  int mosaicInterval = 300;
  long mosaicShowTime = 300 * 1;
  
  ArrayList<ProblemSet> problemList;
  ScheduledExecutorService scheduler;
  ScheduledFuture scheduledFuture;
  HideSchedule hideSchedule = new HideSchedule();
  
  public PatternMatchTest3(int numberOfQuestionPerType){
    super();
    SetTestNameVersion(TEST_NAME, TEST_VERSION);
    this.numberOfQuestionPerType = numberOfQuestionPerType;
    this.numberOfQuestion = numberOfQuestionPerType * typeOfQuestion * typeOfMark * blackAlphaArray.length;
  }
  
  @Override
  public void init(GL2GL3 gl){
    super.init(gl);
    initMark(gl);
    colorMosaic.initForPmt2(gl);
    scheduler = Executors.newSingleThreadScheduledExecutor();
    problemList = new ArrayList<ProblemSet>();
  }
  
  public void initMark(GL2GL3 gl){
    markArray = new Vector3DDouble[typeOfMark];
  
    markArray[1] = black;
        blackCircle = new Billboard(gl, 
        "resources/Image/TextureImage/circle.png", billBoardSize);
    blackCircle.setAlpha(0.2f);
  }
  
  @Override
  public void rendering(GL2GL3 gl){
    clearWindow(gl, clearColor);
    
    if(isQuestioning & hideSchedule.show){
      showQuestion(gl);
    }
    
    gl.glFlush();
  }
  
  @Override
  protected void setSceneViewport(){
    int offsetx1 = (CANVAS_WIDTH - 2 * mosaicViewportSize - mosaicInterval) / 2;
    int offsetx2 = CANVAS_WIDTH - offsetx1 - mosaicViewportSize;
    int offsety = (CANVAS_HEIGHT - mosaicViewportSize) / 2;
    viewport1[0] = offsetx1;
    viewport1[1] = offsety;
    viewport1[2] = viewport1[3] = mosaicViewportSize;
    viewport2[0] = offsetx2;
    viewport2[1] = offsety;
    viewport2[2] = viewport2[3] = mosaicViewportSize;
  }
  
  @Override
  protected void startTest(){
    genProblemList();
    Main.requestFocus();
    super.startTest();
  }
  
  @Override
  protected void initOutFile(){
    super.initOutFile();
  }
  
  private void genProblemList(){
    problemList.clear();
    numberOfQuestion = 0;
    for(int i = 0; i < numberOfQuestionPerType; i++){
      Vector3DDouble color = new Vector3DDouble(L, 100 * Math.random(), 4);
      Vector3DDouble secondColor = new Vector3DDouble(color.x, color.y - 5, color.z);
      problemList.add(new ProblemSet(0, TEST_NAME, blackCircle, color, secondColor, false));
      numberOfQuestion++;
    }

  }
  
  @Override
  public void showQuestion(GL2GL3 gl){
    if(isDemo){
      gl.glViewport(0, 0, viewport1[2], viewport1[3]);
      colorMosaic.setShadowTexCoordSize(viewport1[2], viewport1[3]);
      colorMosaic.rendeingAllColor(gl);
    }
    
    gl.glViewport(viewport1[0], viewport1[1], viewport1[2], viewport1[3]);
    colorMosaic.setShadowTexCoordSize(viewport1[2], viewport1[3]);
    colorMosaic.pmt3Rendering1(gl, currentProblem);
    
    gl.glViewport(viewport2[0], viewport2[1], viewport2[2], viewport2[3]);
    colorMosaic.setShadowTexCoordSize(viewport2[2], viewport2[3]);
    colorMosaic.pmt3Rendering2(gl, currentProblem);
  }
  
  @Override
  public void newProblem(){
    System.out.println("Question" + (numberOfAnsweredQuestion+1) + "/" + numberOfQuestion);
    int index = (int) (Math.random() * problemList.size());
    currentProblem = problemList.get(index);
    answerOutput.append(currentProblem.markType).append(", ");
    problemList.remove(index);
    hideSchedule.show = true;
    //非表示のスケジュール
    scheduledFuture = scheduler.schedule(hideSchedule, mosaicShowTime, TimeUnit.MILLISECONDS);
  }
  
  private boolean answer(KeyEvent e){
    
    switch(e.getKeyCode()){
    case KeyEvent.VK_LEFT:
      if(currentProblem.isSame){
        correct();
      }else{
        wrong();
      }
      return true;
    case KeyEvent.VK_RIGHT:
      if(!currentProblem.isSame){
        correct();
      }else{
        wrong();
      }
      return true;
    default:
      return false;
    }
  }
  
  
  @Override
  public void keyPressed(KeyEvent e){
    if(isQuestioning){
      if(answer(e)){
        scheduledFuture.cancel(false);
        endQuestion();
      }
    }
  }
  
  @Override
  public void mouseReleased(MouseEvent e){

  }
  
  class ProblemSet{
    String markType;
    Billboard billBoard;
    Vector3DDouble color, secondColor;
    float alphaOfBillBoard;
    boolean isSame;
    
    public ProblemSet(int problemType, String markType,
        Billboard billBoard, Vector3DDouble color, Vector3DDouble secondColor, 
        boolean isSame){
      this.markType = markType;
      this.billBoard = billBoard;
      this.color = color;
      this.secondColor = secondColor;
      this.isSame = isSame;
    }
  }
  
  class HideSchedule implements Runnable{

    public boolean show;
    @Override
    public void run(){
      //System.out.println("run");
      show = false;
    }   
  }

}
