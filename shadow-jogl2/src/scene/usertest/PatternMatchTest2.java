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

public class PatternMatchTest2 extends PatternMatchTest{
  
  private final String TEST_VERSION = "1.0.0";
  private final String TEST_NAME = "PatternMatch2";

  final int typeOfQuestion = 5;
  /*
   * 0:同じ
   * 1:同じ模様あり
   * 2:色相1ます違い
   * 3:色相違い1ます同じ色のマスに模様あり
   * 4:色相違い1ます違う色のマスに模様あり
   * 5:模様による変化と同じ色の変化模様表示なし
   * 6:真の色(模様による変化と同じ変化がかかっている)と(地の色＋模様)が同じ色
   */
  int typeOfMark = 2 + 1;
  Billboard whiteCircle, blackCircle;
  float billBoardSize = (float) (Math.sqrt(2) / 3 * 1.3);
  Vector3DDouble white = new Vector3DDouble(1, 1, 1);
  Vector3DDouble black = new Vector3DDouble(0, 0, 0);
  Vector3DDouble[] markArray;
  
  float[] blackAlphaArray = {0.14f, 0.16f, 0.18f, 0.2f};
  float[] whiteAlphaArray = {0.16f, 0.18f, 0.20f, 0.22f};
  int[] shadowRangeArray = {10, 10, 14, 14};
  
  int numberOfQuestionPerType;
  Problem currentProblem;
  int mosaicGridSize = 3;
  
  //今回はfalse
  boolean isShadowed = false;
  int mosaicViewportSize = 150;
  int mosaicInterval = 300;
  long mosaicShowTime = 1500 * 1;
  
  ArrayList<Problem> problemList;
  ScheduledExecutorService scheduler;
  ScheduledFuture scheduledFuture;
  HideSchedule hideSchedule = new HideSchedule();
  
  public PatternMatchTest2(int numberOfQuestionPerType){
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
    problemList = new ArrayList<Problem>();
  }
  
  public void initMark(GL2GL3 gl){
    markArray = new Vector3DDouble[typeOfMark];
    markArray[0] = white;
    markArray[1] = black;
    whiteCircle = new Billboard(gl, 
        "resources/Image/TextureImage/circlewhite.png", billBoardSize);
    whiteCircle.setAlpha(0.2f);
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
      problemList.add(new Problem(0, -1, null, null, 0));
      problemList.add(new Problem(2, -1, null, null, 0));
      numberOfQuestion += 2;
    }
    
    for(int j = 0; j < numberOfQuestionPerType; j++){
      for(int k = 0; k < blackAlphaArray.length; k++){
        problemList.add(new Problem(1, 1, blackCircle, black, blackAlphaArray[k]));
        problemList.add(new Problem(1, 2, whiteCircle, white, whiteAlphaArray[k]));
        problemList.add(new Problem(1, 0, null, null, shadowRangeArray[k]));
        problemList.add(new Problem(3, 1, blackCircle, black, blackAlphaArray[k]));
        problemList.add(new Problem(3, 2, whiteCircle, white, whiteAlphaArray[k]));
        problemList.add(new Problem(3, 0, null, null, shadowRangeArray[k]));
        problemList.add(new Problem(4, 1, blackCircle, black, blackAlphaArray[k]));
        problemList.add(new Problem(4, 2, whiteCircle, white, whiteAlphaArray[k]));
        problemList.add(new Problem(4, 0, null, null, shadowRangeArray[k]));
        numberOfQuestion += 9;
      }
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
    colorMosaic.pmt2Rendering1(gl, currentProblem);
    
    gl.glViewport(viewport2[0], viewport2[1], viewport2[2], viewport2[3]);
    colorMosaic.setShadowTexCoordSize(viewport2[2], viewport2[3]);
    colorMosaic.pmt2Rendering2(gl, currentProblem);
  }
  
  @Override
  public void newProblem(){
    System.out.println("Question" + (numberOfAnsweredQuestion+1) + "/" + numberOfQuestion);
    int index = (int) (Math.random() * problemList.size());
    currentProblem = problemList.get(index);
    if(currentProblem.billBoard != null){
      currentProblem.billBoard.setAlpha(currentProblem.alphaOfBillBoard);
      System.out.println(currentProblem.alphaOfBillBoard);
    }
    answerOutput.append(currentProblem.problemType).append(", ")
    .append(currentProblem.markType).append(", ")
    .append(currentProblem.alphaOfBillBoard).append(", ");
    problemList.remove(index);
    colorMosaic.genColorMosaic(mosaicGridSize, isShadowed);
    colorMosaic.genComparisonColorMosaic(currentProblem.problemType);
    hideSchedule.show = true;
    //非表示のスケジュール
    scheduledFuture = scheduler.schedule(hideSchedule, mosaicShowTime, TimeUnit.MILLISECONDS);
  }
  
  private boolean answer(KeyEvent e){
    
    switch(e.getKeyCode()){
    case KeyEvent.VK_LEFT:
      if(currentProblem.problemType == 0 || currentProblem.problemType == 1){
        correct();
      }else{
        wrong();
      }
      return true;
    case KeyEvent.VK_RIGHT:
      if(currentProblem.problemType == 2 || currentProblem.problemType == 3
          || currentProblem.problemType == 4){
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
  
  class Problem{
    int problemType;
    int markType;
    Billboard billBoard;
    Vector3DDouble color;
    float alphaOfBillBoard;
    
    public Problem(int problemType, int markType,
        Billboard billBoard, Vector3DDouble color, float alpha){
      this.problemType = problemType;
      this.markType = markType;
      this.billBoard = billBoard;
      this.color = color;
      this.alphaOfBillBoard  = alpha;
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
