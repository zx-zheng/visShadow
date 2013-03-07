package scene;

import gui.Ctrlpanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;

import com.jogamp.opengl.util.awt.Screenshot;

import main.Main;

import scene.obj.Billboard;
import scene.oldTypeScene.Scene1;
import util.ColorUtil;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector3DDouble;



public abstract class SceneOrganizer{
  
  protected String TEST_VERSION = "1.0";
  protected String TEST_NAME = "TEST_BASE";
  
  protected Vector3DDouble clearColor;
  
  protected int prevMouseX, prevMouseY;
  
  protected boolean isDemo = false;
  protected long elapsedTime = 0;
  protected long startTime = 0;
  protected long sumOfAnswerTime = 0;
  protected long intervalTime = 1000;
  protected long offsetTime = 0;
  protected boolean isMeasuring = false;
  protected boolean isAnswered = false;
  protected boolean isInterval = false;
  
  protected boolean isStart = false;
  protected boolean nextProblem = false;
  protected boolean isQuestioning = false;
  
  protected int numberOfQuestion;
  protected int numberOfAnsweredQuestion = 0;
  protected int numberOfCorrectAnswer = 0;
  protected StringBuilder answerOutput = new StringBuilder();
  
  public int CANVAS_WIDTH;
  public int CANVAS_HEIGHT;
  
  protected static JButton startTestButton, startDemoButton, endDemoButton;
  protected static JCheckBox unableToAnswer;
  private static boolean isFirst = true;
  private static boolean isBillBoardInit = false;
  
  protected static double billBoardTexSizeRatio = 0.5 * 1.25;
  protected static double shadowTexSize = 1.5;
  protected static float billBoardSize = 
      (float) (shadowTexSize * billBoardTexSizeRatio);
  
  protected static Billboard blackHardCircle, blackCircle,
  shadow, hardShadow;
  protected static float blackAlpha = 0.18f;
  protected static float lDown = 10;
  
  protected String outFileName;
  
  static ScheduledExecutorService intervalScheduler;
  static IntervalSchedule intervalSchedule;
  static ScheduledFuture scheduledFuture;

  abstract public void rendering(GL2GL3 gl);
  abstract public void iterate(GL2GL3 gl);
  abstract public void keyPressed(KeyEvent e);
  abstract public void mouseDragged(MouseEvent e);
  abstract public void mousePressed(MouseEvent e);
  abstract public void mouseReleased(MouseEvent e);
  abstract public void mouseMoved(MouseEvent e);
  abstract public void switchSO(GL2GL3 gl);
  
  public SceneOrganizer(){
    if(isFirst){
      startTestButton = new JButton("Start Test");
      Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getCtrlPanel(), startTestButton);
      startDemoButton = new JButton("Start Demo");
      Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getCtrlPanel(), startDemoButton);
      endDemoButton = new JButton("End");
      endDemoButton.setVisible(false);
      Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getCtrlPanel(), endDemoButton);
      unableToAnswer = new JCheckBox("Unable To Answer");
      unableToAnswer.setVisible(false);
      Ctrlpanel.getInstance().addCheckBox(Ctrlpanel.getInstance().getUserTestPane(), unableToAnswer);
      intervalScheduler = Executors.newSingleThreadScheduledExecutor();
      intervalSchedule = new IntervalSchedule();
      isFirst = false;
      Ctrlpanel.getInstance().hideCtrlPanel();
      Ctrlpanel.getInstance().showCtrlPanel();
    }
  }
  
  protected void initBillBoard(GL2GL3 gl){
    if(isBillBoardInit) return;
    
    double centeryPixel = 22, widthPixel = 200;
    double centery = (shadowTexSize / 2) - (centeryPixel / widthPixel) * shadowTexSize;
    System.out.println(centery);
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow10.png",
        new Vector2DDouble(0, centery), shadowTexSize);
    shadow.setAlpha(blackAlpha);
    
    hardShadow = new Billboard(gl, "resources/Image/TextureImage/hardshadow4.png",
        new Vector2DDouble(0, centery), shadowTexSize);
    hardShadow.setAlpha(blackAlpha);
    
    blackCircle = new Billboard(gl, 
        "resources/Image/TextureImage/circle3.png", billBoardSize);
    blackCircle.setAlpha(blackAlpha);
    
    blackHardCircle = new Billboard(gl, 
        "resources/Image/TextureImage/blackHardCircle2.png", billBoardSize);
    blackHardCircle.setAlpha(blackAlpha);
    isBillBoardInit = true;
  }
  
  protected void initClearColor(int L){
    clearColor = ColorUtil.LabtoRGB(L, 0, 0);
  }
  
  protected void SetTestNameVersion(String Name, String Version){
    this.TEST_NAME = Name;
    this.TEST_VERSION = Version;
  }
  
  public void clearWindow(GL2GL3 gl){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
  }
  
  public void clearWindow(GL2GL3 gl, Vector3DDouble color){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glClearColor((float) color.x, (float) color.y, (float) color.z, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
  }
  
  protected void showQuestion(GL2GL3 gl){
    
  }
  
  public void interval(GL2GL3 gl, long millis){
    isAnswered = false;
    clearWindow(gl, clearColor);
    try{
      Thread.sleep(millis);
    }catch(InterruptedException e){
      e.printStackTrace();
    };
    //isInterval = false;
  }
  
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    CANVAS_WIDTH = width;
    CANVAS_HEIGHT = height;
  }
  
  protected void startTest(){
    if(isStart) return;
    isStart = true;
    startTestButton.setVisible(false);
    startDemoButton.setVisible(false);
    Main.setUndecorated(true);
    //unableToAnswer.setVisible(true);
    if(isDemo){
      endDemoButton.setVisible(true);
    }else{
      endDemoButton.setVisible(true);
      //Ctrlpanel.getInstance().getUserTestPane().setVisible(false);
    }
    //Ctrlpanel.getInstance().getCtrlPanel().setVisible(true);
    showAnswerButton();
    scheduledFuture = 
        intervalScheduler.schedule(intervalSchedule, intervalTime, TimeUnit.MILLISECONDS);
    //startQuestion();
  }
  
  protected void showAnswerButton(){
    
  }
  
  protected void hideAnswerButton(){
    
  }
  
  protected void startQuestion(){
    isQuestioning = true;
    isAnswered = false;
    isInterval = false;
    nextProblem = false;
    newProblem();
    startMeasureTime();
  }
  
  public void endQuestion(){
    stopMeasureTime();
    isQuestioning = false;
    numberOfAnsweredQuestion++;
    if(unableToAnswer.isSelected()){
      answerOutput.append("invalid\n");
    }else{
      answerOutput.append(elapsedTime).append("\n");
    }
    unableToAnswer.setSelected(false);
    isAnswered = true;
    intervalSchedule.isInterval = true;
    scheduledFuture = 
        intervalScheduler.schedule(intervalSchedule, intervalTime, TimeUnit.MILLISECONDS);
  }
  
  protected void correct(){
    answerOutput.append("1, ");
    numberOfCorrectAnswer++; 
    System.out.println("Correct");
  }
  
  protected void wrong(){
    answerOutput.append("0, ");
    System.out.println("wrong");
  }
  
  public void newProblem(){
    
  }
  
  public void startMeasureTime(){
    startTime = System.currentTimeMillis();
    isMeasuring = true;
  }
  
  public long stopMeasureTime(){
    elapsedTime = System.currentTimeMillis() - startTime - offsetTime;
    sumOfAnswerTime += elapsedTime;
    isMeasuring = false;
    return elapsedTime;
  }
  
  protected void initOutFile(){
    answerOutput.append("Test Name = ").append(TEST_NAME).append("\n");
    answerOutput.append("Version = ").append(TEST_VERSION).append("\n");
    Calendar now = Calendar.getInstance();
    outFileName =
        TEST_NAME
        + Integer.toString(now.get(Calendar.YEAR)) + "-"
        + Integer.toString(now.get(Calendar.MONTH) + 1) + "-"
        + Integer.toString(now.get(Calendar.DATE)) + "-"
        + Integer.toString(now.get(Calendar.HOUR_OF_DAY)) + "-"
        + Integer.toString(now.get(Calendar.MINUTE));
  }
  
  protected boolean answerCheck(){
    return isQuestioning;
  }
  
  public void saveAnswer(String answers){    
    File dir = new File("log/" + TEST_NAME);
    if(!dir.exists()){
      dir.mkdir();
    }
    File file = new File("log/" + TEST_NAME + "/" + outFileName + ".csv");
    try{
      PrintWriter pw = 
          new PrintWriter(new BufferedWriter(new FileWriter(file)));
      pw.write(answers);
      pw.close();
      System.out.println(outFileName + " saved");
    }catch(IOException e){
      e.printStackTrace();
    }  
  }
  
  public void endTest(){
    saveAnswer(new String(answerOutput));
    System.out.println("Result : " + numberOfCorrectAnswer + " / " + numberOfQuestion);
    System.out.println("Average answer time : " + ((double) sumOfAnswerTime / numberOfQuestion) + " ms");
    
    resetTest();
  }
  
  public void end(){
    System.out.println("End");
    resetTest();
  }
  
  protected void resetTest(){
    scheduledFuture.cancel(true);
    elapsedTime = 0;
    startTime = 0;
    sumOfAnswerTime = 0;
    isMeasuring = false;
    isAnswered = false;
    isInterval = false;
    
    isStart = false;
    nextProblem = false;
    isQuestioning = false;
    
    numberOfAnsweredQuestion = 0;
    numberOfCorrectAnswer = 0;
    answerOutput = new StringBuilder();
    
    isDemo = false;
    isStart = false;
    hideAnswerButton();
    startTestButton.setVisible(true);
    startDemoButton.setVisible(true);
    endDemoButton.setVisible(false);
    unableToAnswer.setVisible(false);
    Main.setUndecorated(false);
    Ctrlpanel.getInstance().getUserTestPane().setVisible(true);
    Ctrlpanel.getInstance().showCtrlPanel();
  }
  
  public void actionPerformed(ActionEvent e){
    Object src = e.getSource();
    if (src == startTestButton){
      initOutFile();
      startTest();
    } 
    else if (src == startDemoButton){
      isDemo = true;
      initOutFile();
      startTest();
    } 
    else if (src == endDemoButton){
      end();
    }
    else if (src == unableToAnswer){
      Main.requestFocus();
    }
  }
  
  public void stateChanged(ChangeEvent e) {
    
  }
  

  
  class IntervalSchedule implements Runnable{

    public boolean isInterval = false;
    @Override
    public void run(){
      isInterval = false;
      nextProblem = true;
    }
    
  }
}
