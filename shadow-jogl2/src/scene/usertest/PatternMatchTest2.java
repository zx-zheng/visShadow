package scene.usertest;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GL2GL3;

import main.Main;

public class PatternMatchTest2 extends PatternMatchTest{
  
  private final String TEST_VERSION = "1.0.0";
  private final String TEST_NAME = "PatternMatch2";

  final int typeOfQuestion = 5;
  /*
   * 0:同じ
   * 1:同じ影あり
   * 2:色相1ます違い
   * 3:色相違い1ます同じ色のマスに影あり
   * 4:色相違い1ます違う色のマスに影あり
   * 5:明度1ます違い
   * 6:明度ダウン影あり
   * 7:明度アップ影あり
   */
  int numberOfQuestionPerType;
  int currentProblemType;
  int mosaicGridSize = 3;
  boolean isShadowed = true;
  int mosaicViewportSize = 150;
  int mosaicInterval = 300;
  long mosaicShowTime = 1500;
  
  ArrayList<Integer> problemList;
  ScheduledExecutorService scheduler;
  ScheduledFuture scheduledFuture;
  HideSchedule hideSchedule = new HideSchedule();
  
  public PatternMatchTest2(int numberOfQuestionPerType){
    super();
    SetTestNameVersion(TEST_NAME, TEST_VERSION);
    this.numberOfQuestionPerType = numberOfQuestionPerType;
    this.numberOfQuestion = numberOfQuestionPerType * typeOfQuestion;
  }
  
  @Override
  public void init(GL2GL3 gl){
    super.init(gl);
    colorMosaic.initForPmt2(gl);
    scheduler = Executors.newSingleThreadScheduledExecutor();
    problemList = new ArrayList<Integer>();
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
    for(int i = 0; i < typeOfQuestion; i++){
      for(int j = 0; j < numberOfQuestionPerType; j++){
        problemList.add(i);
      }
    }
  }
  
  @Override
  public void showQuestion(GL2GL3 gl){
    gl.glViewport(viewport1[0], viewport1[1], viewport1[2], viewport1[3]);
    colorMosaic.setShadowTexCoordSize(viewport1[2], viewport1[3]);
    colorMosaic.pmt2Rendering1(gl);
    
    gl.glViewport(viewport2[0], viewport2[1], viewport2[2], viewport2[3]);
    colorMosaic.setShadowTexCoordSize(viewport2[2], viewport2[3]);
    colorMosaic.pmt2Rendering2(gl);
  }
  
  @Override
  public void newProblem(){
    int index = (int) (Math.random() * problemList.size());
    currentProblemType = problemList.get(index);
    answerOutput += Integer.toString(currentProblemType) + ", ";
    problemList.remove(index);
    colorMosaic.genColorMosaic(mosaicGridSize, isShadowed);
    colorMosaic.genComparisonColorMosaic(currentProblemType);
    hideSchedule.show = true;
    //非表示のスケジュール
    scheduledFuture = scheduler.schedule(hideSchedule, mosaicShowTime, TimeUnit.MILLISECONDS);
  }
  
  private boolean answer(KeyEvent e){
    System.out.println("ans");
    switch(e.getKeyCode()){
    case KeyEvent.VK_LEFT:
      if(currentProblemType == 0 || currentProblemType == 1){
        correct();
      }else{
        wrong();
      }
      return true;
    case KeyEvent.VK_RIGHT:
      if(currentProblemType == 2 || currentProblemType == 3
          || currentProblemType == 4){
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
  
  class HideSchedule implements Runnable{

    public boolean show;
    @Override
    public void run(){
      //System.out.println("run");
      show = false;
    }   
  }

}
