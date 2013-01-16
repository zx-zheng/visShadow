package scene.usertest;

import gui.Ctrlpanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.swing.JButton;

import com.jogamp.common.util.IntIntHashMap;

import scene.SceneOrganizer;
import scene.obj.Billboard;
import scene.usertest.Test1_2.HideSchedule;
import util.ColorUtil;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.geom.Vector3DDouble;

public class SearchColor extends SceneOrganizer{
  
  private final String TEST_VERSION = "1.0.0";
  private final String TEST_NAME = "SearchColor";
  
  //バックグラウンドの明度
  int L = 55;
  int lab_a = 80;
  long intervalTime = 1000;
  ColorMosaic colorMosaic; 
  int mosaicGridSize = 15;
  int mosaicViewportSize;
  long ansShowTime = 2000;
  long mosaicStartShowTime = 3000;
  double[] radiusLimit, radiusLimitShadow;
  Problem currentProblem;
  
  ArrayList<Problem> problemList;
  
  ScheduledExecutorService scheduler1, scheduler2;
  ScheduledFuture scheduledFuture;
  HideSchedule hideSchedule;
  ShowSchedule showSchedule;
  
  int numberOfCorrectAnswer = 3;
  
  protected JButton answerButton;
  
  int[] viewport1 = new int[4], viewport2 = new int[4];
  
  public SearchColor(){
    super();
    super.TEST_VERSION = this.TEST_VERSION;
    super.TEST_NAME = this.TEST_NAME;
  }
  
  public SearchColor(int numberOfQuestion){
    super();
    super.TEST_VERSION = this.TEST_VERSION;
    super.TEST_NAME = this.TEST_NAME;
    this.numberOfQuestion = numberOfQuestion;
  }
  
  public void init(GL2GL3 gl){
    initBillBoard(gl);
    initClearColor(L);
    loadRadiusLimit();
    setGUI();
    colorMosaic = new ColorMosaic();
    colorMosaic.init(gl);
    problemList = new ArrayList<SearchColor.Problem>();
    
    hideSchedule = new HideSchedule();
    showSchedule = new ShowSchedule();
    scheduler1 = Executors.newSingleThreadScheduledExecutor();
    scheduler2 = Executors.newSingleThreadScheduledExecutor();
    
    genProblemList();
  }
  
  private void setGUI(){
    answerButton = new JButton("Answer");
    answerButton.setVisible(false);
    Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getUserTestPane(), answerButton);
  }
  
  private void loadRadiusLimit(){
    radiusLimit = new double[3600];
    radiusLimitShadow = new double[3600];
    Scanner scanner;
    try{
      scanner = new Scanner(new File("resources/LabsRGBLimitData/labLimitTable" + L + ".txt"));
      scanner.useDelimiter(",\\s+|\\s+|E\\+|#\\s");
      for(int i = 0; i < 3600; i++){
        radiusLimit[i] = scanner.nextDouble();
      }
    }catch(FileNotFoundException e){
      e.printStackTrace();
    }
    
    try{
      scanner = new Scanner(new File("resources/LabsRGBLimitData/labLimitTable" + (int)(L-lDown) + ".txt"));
      scanner.useDelimiter(",\\s+|\\s+|E\\+|#\\s");
      for(int i = 0; i < 3600; i++){
        radiusLimitShadow[i] = scanner.nextDouble();
      }
    }catch(FileNotFoundException e){
      e.printStackTrace();
    }
   
  }
  
  @Override
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    super.setCanvasSize(gl, width, height);
    setSceneViewport();
  }
  
  protected void setSceneViewport(){
    int offset = 50;
    mosaicViewportSize = Math.min(CANVAS_WIDTH, CANVAS_HEIGHT) - offset * 2;
    viewport1[0] = (CANVAS_WIDTH - mosaicViewportSize)/2;
    viewport1[1] = offset;
    viewport1[2] = mosaicViewportSize;
    viewport1[3] = mosaicViewportSize;
  }

  @Override
  public void rendering(GL2GL3 gl){
    clearWindow(gl, clearColor);
   
    if(isQuestioning){
      showQuestion(gl);
    }
    
    gl.glFlush();
  }
  
  @Override
  public void showQuestion(GL2GL3 gl){
    
    
    if(showSchedule.show){      
      gl.glViewport(viewport1[0], viewport1[1], viewport1[2], viewport1[3]);
      colorMosaic.setShadowTexCoordSize(mosaicViewportSize, mosaicViewportSize);
      colorMosaic.serachColorRendering(gl, currentProblem);
      colorMosaic.renderingRect(gl, mosaicGridSize);
      colorMosaic.renderingRectList(gl, currentProblem.answeredList, mosaicGridSize);
    } 
    
    if(hideSchedule.show){
      int width = 100;
      gl.glViewport( (CANVAS_WIDTH - width)/2,
          (CANVAS_HEIGHT - width)/2, width, width);
      colorMosaic.renderingMosaicLab(gl, currentProblem.ansColor);
    }
  }
  
  @Override
  public void newProblem(){
    showSchedule.show = false;
    hideSchedule.show = true;
    System.out.println("Question" + (numberOfAnsweredQuestion+1) + "/" + numberOfQuestion);
    int index = (int) (problemList.size() * Math.random());
    currentProblem = problemList.get(index);
    problemList.remove(index);
    colorMosaic.genPoissionDisk(0.05);
    if(currentProblem.blendType == 0){
      currentProblem.mark.setAlpha(blackAlpha);
    } else if(currentProblem.blendType == 1){
      currentProblem.mark.setAlpha(1);
    }
    scheduler1.schedule(hideSchedule, ansShowTime, TimeUnit.MILLISECONDS);
    scheduler2.schedule(showSchedule, mosaicStartShowTime, TimeUnit.MILLISECONDS);
  }
  
  public void genProblemList(){
    int numOfColor = 70;
    for(int i= 0; i<numberOfQuestion; i++){
      int[][] valueArray = new int[mosaicGridSize][mosaicGridSize];
      
      for(int j= 0; j<mosaicGridSize; j++){
        for(int j2= 0; j2<mosaicGridSize; j2++){
          valueArray[j][j2] = (int) (numOfColor * Math.random());
        }
      }
      
      List<Vector2DInt> answerList = new ArrayList<Vector2DInt>();
      //正解箇所の生成
      int countOfAns = 0;
      while(countOfAns < numberOfCorrectAnswer){
        int indexx = (int) ((mosaicGridSize - 2) * Math.random()) + 1;
        int indexy = (int) ((mosaicGridSize - 2) * Math.random()) + 1;
        if(valueArray[indexx][indexy] < numOfColor){
           valueArray[indexx][indexy] = numOfColor;
          countOfAns++;
          answerList.add(new Vector2DInt(indexx, indexy));
        }
      }
      
//      for(int j = 0; j < 10; j++){
//        valueArray[0][j] = 0.1 * j;
//      }
      problemList.add(
          new Problem(valueArray, shadow, i % 2, (int)(360 * Math.random()),
              answerList, numOfColor));
      problemList.add(
          new Problem(valueArray, hardShadow, i % 2, (int)(360 * Math.random()),
              answerList, numOfColor));
    }
  }
  
  private void answer(){
    if(currentProblem.checkAnswer()){
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
    colorMosaic.iterate();
    if (!isDemo & numberOfAnsweredQuestion == numberOfQuestion) {
      endTest();
    } else if (nextProblem) {
      startQuestion();
      return;
    }
  }
  
  @Override
  protected void showAnswerButton(){
    answerButton.setVisible(true);
  }
  
  @Override
  protected void hideAnswerButton(){
    answerButton.setVisible(false);
  }
  
  @Override
  protected void resetTest(){
    super.resetTest();
    genProblemList();
  }
  
  @Override
  protected void initOutFile(){
    super.initOutFile();
  }

  @Override
  public void keyPressed(KeyEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseDragged(MouseEvent e){
    colorMosaic.setMarkPos(
        windowCoordToWorldCoord(e.getX() - viewport1[0], e.getY() - viewport1[1]), mosaicGridSize);
  }

  @Override
  public void mousePressed(MouseEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseReleased(MouseEvent e){
    if(showSchedule.show && isQuestioning){
      Vector2DDouble pos = windowCoordToWorldCoord(e.getX() - viewport1[0], e.getY() - viewport1[1]);
      currentProblem.addAnswer(new Vector2DInt((int) (pos.x * mosaicGridSize), (int) (pos.y * mosaicGridSize)));
    }
      
  }

  @Override
  public void switchSO(GL2GL3 gl){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseMoved(MouseEvent e){
    colorMosaic.setMarkPos(
        windowCoordToWorldCoord(e.getX() - viewport1[0], e.getY() - viewport1[1]), mosaicGridSize);
  }

  @Override
  public void actionPerformed(ActionEvent e){
    super.actionPerformed(e); 
    Object src = e.getSource();
    if (src == answerButton){
      answer();
    }
  }
  
  class Problem{
    Vector3DDouble[][] colorArray;
    Vector3DDouble ansColor;
    List<Vector3DDouble> colorList;
    double angleOffset;
    int[][] valueArray;
    Billboard mark;
    int blendType;
    public List<Vector2DInt> answerList, answeredList;
    
    public Problem(int[][] valueArray, Billboard mark, int blendType,
        int angleOffset, List<Vector2DInt> answerList, int numOfColor){
      this.valueArray = valueArray;
      this.mark = mark;
      this.blendType = blendType;
      this.angleOffset = angleOffset;
      this.answerList = answerList;
      this.answeredList = new ArrayList<Vector2DInt>();
      this.colorList = new ArrayList<Vector3DDouble>();
      this.colorArray = new Vector3DDouble[valueArray.length][valueArray[0].length];
      genColorArray(numOfColor);
    }
    
    private void genColorArray(int numOfColor){
      int countOfColor = 0;
      double r = 10, colorAngle = angleOffset;
      while(countOfColor <= numOfColor){
        if(Math.min(radiusLimit[(int)(colorAngle* 100) % 3600],
            radiusLimitShadow[(int)(colorAngle * 100) % 3600]) < r){         
          double a = r * Math.cos(colorAngle);
          double b = r * Math.sin(colorAngle);
          colorList.add(new Vector3DDouble(L, a, b));
          countOfColor++;
        }
        r++;
        colorAngle+=36;
      }
      
      for(int i= 0; i<colorArray.length; i++){
        for(int j= 0; j<colorArray[0].length; j++){
          colorArray[i][j] = colorList.get(valueArray[i][j]);
        }
      }
      ansColor = colorList.get(numOfColor);
//      double ansAngle = 2 * Math.PI * 0.9 + angleOffset;
//      ansColor = 
//          new Vector3DDouble(L, lab_a * Math.cos(ansAngle), lab_a * Math.sin(ansAngle));
//      colorArray = new Vector3DDouble[valueArray.length][valueArray[0].length];
//      for(int i= 0; i<colorArray.length; i++){
//        for(int j= 0; j<colorArray[0].length; j++){
//          double angle = 2 * Math.PI * valueArray[i][j] + angleOffset;
//          double radius = Math.min(radiusLimit[(int)(angle * 100) % 3600],
//              radiusLimitShadow[(int)(angle * 100) % 3600]);
//          colorArray[i][j] = 
//              //new Vector3DDouble(L, (valueArray[i][j] - 0.5) * lab_a, 4);
//              new Vector3DDouble(L, radius * Math.cos(angle), radius * Math.sin(angle));
//        }
//      }
    }
    
    public void addAnswer(Vector2DInt answerdPoint){
      for(int i= 0; i<answeredList.size(); i++){
        Vector2DInt ansPoint = answeredList.get(i);
        if(ansPoint.x == answerdPoint.x
            && ansPoint.y == answerdPoint.y){
          deleteAnsweredPoint(i);
          return;
        }
      }
      addAnsweredPoint(answerdPoint);
    }
    
    private void addAnsweredPoint(Vector2DInt answerdPoint){
      answeredList.add(answerdPoint);
    }
    
    private void deleteAnsweredPoint(int index){
      answeredList.remove(index);
    }
    
    public boolean checkAnswer(){
      int countOfCorrect = 0;
      for(int i= 0; i<answeredList.size(); i++){
        Vector2DInt ansPoint = answeredList.get(i);
        
        for(Vector2DInt correctPoint: answerList){
          if(ansPoint.x == correctPoint.x
              && ansPoint.y == correctPoint.y){
            countOfCorrect++;
            break;
          }
        }
      }
      if(countOfCorrect == numberOfCorrectAnswer
          && answeredList.size() == numberOfCorrectAnswer){
        return true;
      }else if(countOfCorrect < numberOfCorrectAnswer){
        System.out.println("not enough");
        return false;
      }else if(answeredList.size() > numberOfCorrectAnswer){
        System.out.println("too many answer");
        return false;
      }else{
        return false;
      }
    }
  }
    
  class HideSchedule implements Runnable{

    public boolean show;

    @Override
    public void run(){
      show = false;
    }   
  }
  
  class ShowSchedule implements Runnable{

    public boolean show;

    @Override
    public void run(){
      System.out.println("run");
      show = true;
    }   
  }

}
