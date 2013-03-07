package scene.usertest;

import gui.Ctrlpanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import main.Main;

import com.jogamp.common.util.IntIntHashMap;

import scene.SceneOrganizer;
import scene.obj.Billboard;
import scene.usertest.Test1_2.HideSchedule;
import util.ColorUtil;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.geom.Vector3DDouble;
import za.co.luma.math.sampling.UniformPoissonDiskSampler;

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
  long ansShowTime = 1500;
  long mosaicStartShowTime = 2500;
  long limitTime = 60000;
  double[] radiusLimit, radiusLimitShadow, radiusLimitDarkShadow;
  Problem currentProblem;
  double poissonInterval = 0.15;
  int numberOfCorrectAnswer = 3;
  int numberOfFake = 2;
  double texScale = 1.8;
  double shadowHideOffset = shadowTexSize * texScale 
      / mosaicGridSize * 0.1 * 2;
  double markHideOffset = billBoardSize * texScale 
      / mosaicGridSize * 0.1 * 2;
  
  ArrayList<Problem> problemList;
  
  int[] answerColorIndex = {1, 4, 12, 28, 34, 36};
  //int[] answerColorIndex = {1, 4, 7, 10, 13, 20, 30};
  
  ScheduledExecutorService scheduler1, scheduler2, timeSchedule;
  ScheduledFuture scheduledFuture, limitFuture;
  HideSchedule hideSchedule;
  ShowSchedule showSchedule;
  TimeLimit timeLimit;
  
  Vector3DDouble clearColorLab;
  
  Vector2DDouble currentPos;
  
  int amountOfCorrect = 0;
  int amountOfWrong = 0;
  
  protected JButton answerButton;
  
  int[] viewport1 = new int[4], viewport2 = new int[4];
  
  private int numberOfQuestionPerMark;
  
  public SearchColor(){
    super();
    super.TEST_VERSION = this.TEST_VERSION;
    super.TEST_NAME = this.TEST_NAME;
  }
  
  public SearchColor(int numberOfQuestionPerMark){
    super();
    super.TEST_VERSION = this.TEST_VERSION;
    super.TEST_NAME = this.TEST_NAME;
    this.numberOfQuestionPerMark = numberOfQuestionPerMark;
    this.numberOfQuestion = 10;
    offsetTime = mosaicStartShowTime;
  }
  
  public void init(GL2GL3 gl){
    initBillBoard(gl);
    initClearColor(L);
    clearColorLab = new Vector3DDouble(L, 0, 0);
    loadRadiusLimit();
    setGUI();
    colorMosaic = new ColorMosaic();
    colorMosaic.init(gl);
    problemList = new ArrayList<SearchColor.Problem>();
    
    hideSchedule = new HideSchedule();
    showSchedule = new ShowSchedule();
    timeLimit = new TimeLimit(this);
    scheduler1 = Executors.newSingleThreadScheduledExecutor();
    scheduler2 = Executors.newSingleThreadScheduledExecutor();
    timeSchedule = Executors.newSingleThreadScheduledExecutor();
    
    genProblemList();
  }
  
  @Override
  protected void initBillBoard(GL2GL3 gl){
    //if(isBillBoardInit) return;
    
    double centeryPixel = 22, widthPixel = 200;
    double centery = (shadowTexSize / 2) - (centeryPixel / widthPixel) * shadowTexSize;
    System.out.println(centery);
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow10.png",
        new Vector2DDouble(0, 0), shadowTexSize * texScale);
    shadow.setAlpha(blackAlpha);
    
    hardShadow = new Billboard(gl, "resources/Image/TextureImage/hardshadow4.png",
        new Vector2DDouble(0, 0), shadowTexSize * texScale);
    hardShadow.setAlpha(blackAlpha);
    
    blackCircle = new Billboard(gl, 
        "resources/Image/TextureImage/circle3.png", (float)(billBoardSize * texScale));
    blackCircle.setAlpha(blackAlpha);
    
    blackHardCircle = new Billboard(gl, 
        "resources/Image/TextureImage/blackHardCircle2.png", (float) (billBoardSize * texScale));
    blackHardCircle.setAlpha(blackAlpha);
  }
  
  private void setGUI(){
    answerButton = new JButton("Answer");
    answerButton.setVisible(false);
    Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getUserTestPane(), answerButton);
  }
  
  private void loadRadiusLimit(){
    radiusLimit = new double[3600];
    radiusLimitShadow = new double[3600];
    radiusLimitDarkShadow = new double[3600];
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
    
    try{
      scanner = new Scanner(new File("resources/LabsRGBLimitData/labLimitTable" + (int)(L-lDown * 2) + ".txt"));
      scanner.useDelimiter(",\\s+|\\s+|E\\+|#\\s");
      for(int i = 0; i < 3600; i++){
        radiusLimitDarkShadow[i] = scanner.nextDouble();
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
      int answerWidth = 100, answerOffset = 150;
      int viewportOffset = (int) (viewport1[2] * colorMosaic.projOffset);
      gl.glViewport(viewport1[0] - viewportOffset, viewport1[1] - viewportOffset,
          viewport1[2] + viewportOffset * 2, viewport1[3] + viewportOffset * 2);
      colorMosaic.setShadowTexCoordSize( viewport1[2] + viewportOffset * 2, viewport1[3] + viewportOffset * 2);
      
      colorMosaic.serachColorRendering(gl, currentProblem, clearColorLab, 0.6f);
      
      colorMosaic.renderingRect(gl, mosaicGridSize);
      colorMosaic.renderingRectList(gl, currentProblem.answeredList, mosaicGridSize);
      
      //正解色を表示
      gl.glViewport(answerOffset, (CANVAS_HEIGHT - answerWidth)/2, answerWidth, answerWidth);
      colorMosaic.fboClear(gl);
      colorMosaic.renderingMosaicLab(gl, currentProblem.ansColor);
      gl.glViewport(CANVAS_WIDTH - answerWidth - answerOffset, (CANVAS_HEIGHT - answerWidth)/2, answerWidth, answerWidth);
      colorMosaic.renderingMosaicLab(gl, currentProblem.ansColor);
      
      if(currentPos.x > 0 && currentPos.x < 1
          && currentPos.y > 0 && currentPos.y < 1){
        gl.glViewport(100, 0, 100, 100);
        //colorMosaic.renderingMosaicLab(gl, currentProblem.colorArray[(int) (currentPos.x * mosaicGridSize)][(int) (currentPos.y * mosaicGridSize)]);
      }
    } 
    
    if(hideSchedule.show){
      int width = 100;
      gl.glViewport( (CANVAS_WIDTH - width)/2,
          (CANVAS_HEIGHT - width)/2, width, width);
      colorMosaic.renderingMosaicLab(gl, currentProblem.ansColor);
    }
  }
  
  Vector3DDouble prevColor;
  
  @Override
  public void newProblem(){
    
    showSchedule.show = false;
    hideSchedule.show = true;
    System.out.println("Question" + (numberOfAnsweredQuestion+1) + "/" + numberOfQuestion);
    int count = 0, index = 0;
    while(count < 5){
      index = (int) (problemList.size() * Math.random());
      if(prevColor == null || problemList.get(index).ansColor != prevColor){
        break;
      }
      count++;
    }
    currentProblem = problemList.get(index);
    prevColor = currentProblem.ansColor;
    problemList.remove(index);
    //colorMosaic.genPoissionDisk(poissonInterval);
    for(Vector2DInt point: currentProblem.answerList){
      System.out.println(point);
    }
    if(currentProblem.mark != null){
      if(currentProblem.blendType == 0){
        blackAlpha = 0.19f;
        currentProblem.mark.setAlpha(blackAlpha);
      } else if(currentProblem.blendType == 1){
        currentProblem.mark.setAlpha(1);
      }
    }
    scheduler1.schedule(hideSchedule, ansShowTime, TimeUnit.MILLISECONDS);
    scheduler2.schedule(showSchedule, mosaicStartShowTime, TimeUnit.MILLISECONDS);
    limitFuture = timeSchedule.schedule(timeLimit, mosaicStartShowTime + limitTime, TimeUnit.MILLISECONDS);
  }
  
  public void genProblemList(){

    numberOfQuestion = 0;
    
    for(int j= 0; j<answerColorIndex.length; j++){
      int tmp = j;
      j = 5;
      for(int i= 0; i<numberOfQuestionPerMark; i++){
//        problemList.add(
//            new Problem("shadowL", shadow, 1, shadowHideOffset, answerColorIndex[j]));
//        problemList.add(
//            new Problem("hardShadowL", hardShadow, 1, shadowHideOffset, answerColorIndex[j]));
        problemList.add(
            new Problem("shadowAlpha", shadow, 0, shadowHideOffset, answerColorIndex[j]));
//        problemList.add(
//            new Problem("hardShadowAlpha", hardShadow, 0, shadowHideOffset, answerColorIndex[j]));
//        problemList.add(
//            new Problem("softCircleL", blackCircle, 1, markHideOffset, answerColorIndex[j]));
//        problemList.add(
//            new Problem("hardCircleL", blackHardCircle, 1, markHideOffset, answerColorIndex[j]));
        
//              problemList.add(
//                  new Problem("softCircleAlpha", blackCircle, 0, markHideOffset, j));
        //      problemList.add(
        //          new Problem("hardCircleAlpha", blackHardCircle, 0, markHideOffset, j));
//        problemList.add(
//            new Problem("none", null, 0, 0, answerColorIndex[j]));
      numberOfQuestion += 7;
    }j=tmp;}
  }
  
  private void answer(){
    limitFuture.cancel(false);
    if(currentProblem.checkAnswer()){
      correct();
    } else {
      wrong();
    }
    
    answerOutput.append(currentProblem.name)
        .append(", ")
        .append(currentProblem.blendType)
        .append(", ")
        .append(currentProblem.ansColor)
        .append(", ")
        .append(currentProblem.countOfCorrect)
        .append(", ")
        .append(currentProblem.answerdMarkState)
        .append(", ");
    
    //回答した色を保存
    for(Vector2DInt answerdColor: currentProblem.answeredList){
      answerOutput.append(currentProblem.colorArray[answerdColor.x][answerdColor.y]);
    }
    answerOutput.append(", ");
    
    endQuestion();
    if(!isDemo){
      Main.screenshot("log/" + TEST_NAME + "/" + outFileName + "/",
          numberOfAnsweredQuestion + ".png", CANVAS_WIDTH, CANVAS_HEIGHT);
    }
    isQuestioning = false;
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
  public void endTest(){
    System.out.println("Result : " + amountOfCorrect + " / "+ (numberOfQuestion * 3));
    System.out.println("Amount of wrong : " + amountOfWrong);
    super.endTest();
  }
  
  @Override
  protected void resetTest(){
    super.resetTest();
    limitFuture.cancel(false);
    amountOfCorrect = 0;
    amountOfWrong = 0;
    problemList.clear();
    genProblemList();
  }
  
  @Override
  protected void initOutFile(){
    super.initOutFile();
    answerOutput.append("Color List\n");
    for(Vector3DDouble color: problemList.get(0).colorList){
      answerOutput.append(color).append(", ");
    }
    answerOutput.append("\n");
  }

  @Override
  public void keyPressed(KeyEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseDragged(MouseEvent e){
    currentPos = windowCoordToWorldCoord(e.getX() - viewport1[0], e.getY() - viewport1[1]);
    colorMosaic.setMarkPos(
        currentPos, mosaicGridSize);
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
    currentPos = windowCoordToWorldCoord(e.getX() - viewport1[0], e.getY() - viewport1[1]);
    colorMosaic.setMarkPos(
        currentPos, mosaicGridSize);
  }

  @Override
  public void actionPerformed(ActionEvent e){
    super.actionPerformed(e); 
    Object src = e.getSource();
    if (isQuestioning & showSchedule.show & src == answerButton){
      answer();
    }
  }
  
  class Problem{
    Vector3DDouble[][] colorArray;
    Vector3DDouble ansColor;
    public List<Vector3DDouble> colorList;
    public String name;
    Billboard mark;
    int blendType;
    public List<Vector2DInt> answerList, answeredList, fakeList;
    double offset;
    String[] markState = {"inMark", "outOfMark", "random"};
    public String answerdMarkState = "";
    int ansColorIndex;
    
    UniformPoissonDiskSampler pds;
    List<Vector2DDouble> poissonList;
    public int countOfCorrect = 0;
    
    public Problem(String name, Billboard mark, int blendType, 
        double hideOffset, int ansColorIndex){
      this.mark = mark;
      this.blendType = blendType;      
      this.answeredList = new ArrayList<Vector2DInt>();
      this.colorList = new ArrayList<Vector3DDouble>();
      this.colorArray = new Vector3DDouble[mosaicGridSize][mosaicGridSize];
      this.name = name;
      this.offset = hideOffset;
      this.ansColorIndex = ansColorIndex;
      pds = new UniformPoissonDiskSampler(-0.5, -0.5, 1.5, 1.5, poissonInterval);
      genAnswerList();
      genColorArray();
      genPoisson();
    }
    
    private void genColorArray(){
      int countOfColor = 0;
      
      double gridWidth = 25;
      double gridHeight = gridWidth* Math.sqrt(3) / 2;
      int offset = 0;
      for(double j= -100; j<=100; j+=gridHeight ){
        offset++;
        for(double i= -100; i<= 100; i+=gridWidth){
          double x = i + gridWidth * 0.5 * (offset % 2);
          int angle = (int) (360 * Math.atan2(j, x) / (2 * Math.PI) * 10);
          if(angle < 0){
            angle = 3600 + angle;
          }
          if(Math.min(radiusLimit[angle % 3600], radiusLimitShadow[angle % 3600]) > Math.sqrt(x*x+j*j)){
            colorList.add(new Vector3DDouble(L, x, j));
            countOfColor++;
           }  
          
          if(Math.min(radiusLimitDarkShadow[angle % 3600], radiusLimitShadow[angle % 3600]) > Math.sqrt(x*x+j*j)){
            colorList.add(new Vector3DDouble(L - lDown, x, j));
            countOfColor++;
           }  
         }
        
       }
      System.out.println(countOfColor);
      //sortColorList();
      
      //int index = (int) (Math.random() * countOfColor);
      //int index = (int) (((double) countOfColor / numOfSeledtedAnsColor) * ansColorIndex);
      //System.out.println(index);
      ansColor = colorList.get(ansColorIndex);
      //ansColor = colorList.get(0);
      colorList.remove(ansColor);
      
      ArrayList<Vector3DDouble> colorPoolList = new ArrayList<Vector3DDouble>();
      while(colorPoolList.size() < mosaicGridSize * mosaicGridSize){
        for(Vector3DDouble color: colorList){
          colorPoolList.add(color);
        }
      }
      
      for(int i= 0; i<colorArray.length; i++){
        for(int j= 0; j<colorArray[0].length; j++){
          
          boolean hasSameNear = false;
          int sameCount = 0;
          int colorPoolIndex = 0;
          
          while(sameCount < 5){
            colorPoolIndex = (int) (colorPoolList.size() * Math.random());          

            if(i > 0){
              if(colorPoolList.get(colorPoolIndex) == colorArray[i - 1][j]){
                hasSameNear = true;
              }}
            if(j > 0){
              if(colorPoolList.get(colorPoolIndex) == colorArray[i][j - 1]){
                hasSameNear = true;
              }}
            if(!hasSameNear)break;
            sameCount++;
          }
          
          //colorPoolIndex = 0;
          colorArray[i][j] = colorPoolList.get(colorPoolIndex);
          //colorArray[i][j] = colorList.get(answerColorIndex[i % answerColorIndex.length]);
          colorPoolList.remove(colorPoolIndex);
        }
      }
      
      for(Vector2DInt pos: answerList){
        colorArray[pos.x][pos.y] = ansColor; 
      }
//      
//      while(countOfColor <= numOfColor){
//        if(Math.min(radiusLimit[(int)(colorAngle* 100) % 3600],
//            radiusLimitShadow[(int)(colorAngle * 100) % 3600]) < r){         
//          double a = r * Math.cos(colorAngle);
//          double b = r * Math.sin(colorAngle);
//          colorList.add(new Vector3DDouble(L, a, b));
//          countOfColor++;
//        }
//        r++;
//        colorAngle+=40;
//      }
      
      
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
    
    private void sortColorList(){
      Collections.sort(colorList, new ColorComparator());
    }
    
    private void genAnswerList(){
      answerList = new ArrayList<Vector2DInt>();
      fakeList = new ArrayList<Vector2DInt>();
      //正解箇所の生成
      int countOfAns = 0;
      int countOfFake = 0;
      while(countOfAns < numberOfCorrectAnswer){
        int indexx = (int) ((mosaicGridSize - 2) * Math.random()) + 1;
        int indexy = (int) ((mosaicGridSize - 2) * Math.random()) + 1;
        int countDiff = 0;
        for(Vector2DInt ans: answerList){
          if(Math.max(1, Math.abs(ans.x - indexx))
              * Math.max(1, Math.abs(ans.y - indexy)) > 5){
            countDiff++;
          }}
        if(countDiff == answerList.size()){
          countOfAns++;
          answerList.add(new Vector2DInt(indexx, indexy));
        }
      }
      while(countOfFake < numberOfFake){
        int indexx = (int) ((mosaicGridSize - 2) * Math.random()) + 1;
        int indexy = (int) ((mosaicGridSize - 2) * Math.random()) + 1;
        int countDiff = 0;
        for(Vector2DInt ans: answerList){
          if(Math.max(1, Math.abs(ans.x - indexx)) 
              * Math.max(1, Math.abs(ans.y - indexy)) > 3){
            countDiff++;
          }}
        for(Vector2DInt ans: fakeList){
          if(Math.abs(ans.x - indexx) * Math.abs(ans.y - indexy) > 3){
            countDiff++;
          }}
        if(countDiff == (answerList.size() + fakeList.size())){
          countOfFake++;
          fakeList.add(new Vector2DInt(indexx, indexy));
        }
        //System.out.println(countOfFake);
      }
    }
    
    
    private void genPoisson(){
//      for(Vector2DInt point: answerList){
//        pds.addFixPoint(new Vector2DDouble( (point.x + 0.5) / mosaicGridSize,
//            (point.y + 0.5) / mosaicGridSize));
//      }
      Vector2DInt point = answerList.get(0);
      
      //offset *= (Math.random() - 0.5) * 2;
      
      pds.addFixPoint(new Vector2DDouble( (point.x + 0.5) / mosaicGridSize - offset,
          (point.y + 0.5) / mosaicGridSize + offset));
      point = answerList.get(1);
      pds.addExclusionPoint(new Vector2DDouble( (point.x + 0.5) / mosaicGridSize,
          (point.y + 0.5) / mosaicGridSize));
      poissonList = pds.sample();
      for(Vector2DInt fakepoint: fakeList){
        pds.addExclusionPoint(new Vector2DDouble( (fakepoint.x + 0.5) / mosaicGridSize,
            (fakepoint.y + 0.5) / mosaicGridSize));
      }
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
      countOfCorrect = 0;
      for(int i= 0; i<answeredList.size(); i++){
        Vector2DInt ansPoint = answeredList.get(i);
        
        for(int j = 0; j < answerList.size(); j++){
          Vector2DInt correctPoint = answerList.get(j);
          if(ansPoint.x == correctPoint.x
              && ansPoint.y == correctPoint.y){
            countOfCorrect++;
            answerdMarkState += markState[j];
            break;
          }
        }
      }
      
      amountOfCorrect += countOfCorrect;
      
      System.out.println("answerd " + countOfCorrect + "/"+ numberOfCorrectAnswer);
      if(countOfCorrect == numberOfCorrectAnswer
          && answeredList.size() == numberOfCorrectAnswer){
        return true;
      }else if(countOfCorrect < numberOfCorrectAnswer 
          || answeredList.size() > numberOfCorrectAnswer){
        amountOfWrong += (answeredList.size() - countOfCorrect);
        return false;
      }else{
        return false;
      }
    }
    
    class ColorComparator implements java.util.Comparator<Vector3DDouble> {
      @Override
      public int compare(Vector3DDouble color1, Vector3DDouble color2){     
        double diff = - (color1.y * color1.y + color1.z * color1.z)
            + (color2.y * color2.y + color2.z * color2.z);
        if(diff > 0){
          return 1;
        }else if(diff < 0){
          return -1;
        }
        return 0;
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
  
  class TimeLimit implements Runnable{

    SearchColor sc;
    public TimeLimit(SearchColor sc){
      this.sc = sc;
    }
    @Override
    public void run(){
      System.out.println("Time limit");
      sc.answer();
    }   
  }

}
