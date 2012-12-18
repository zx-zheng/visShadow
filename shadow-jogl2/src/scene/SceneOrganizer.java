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

import main.Main;

import util.ColorUtil;
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
  protected boolean isMeasuring = false;
  protected boolean isAnswered = false;
  protected boolean isInterval = false;
  
  protected boolean isStart = false;
  protected boolean nextProblem = false;
  protected boolean isQuestioning = false;
  
  protected int numberOfQuestion;
  protected int numberOfAnsweredQuestion = 0;
  protected int numberOfCorrectAnswer = 0;
  protected String answerOutput = "";
  
  public int CANVAS_WIDTH;
  public int CANVAS_HEIGHT;
  
  protected static JButton startTestButton, startDemoButton, endDemoButton;
  protected static JCheckBox unableToAnswer;
  private static boolean isFirst = true;
  
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
      Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getUserTestPane(), startTestButton);
      startDemoButton = new JButton("Start Demo");
      Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getUserTestPane(), startDemoButton);
      endDemoButton = new JButton("End Demo");
      endDemoButton.setVisible(false);
      Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getUserTestPane(), endDemoButton);
      unableToAnswer = new JCheckBox("Unable To Answer");
      unableToAnswer.setVisible(false);
      Ctrlpanel.getInstance().addCheckBox(Ctrlpanel.getInstance().getUserTestPane(), unableToAnswer);
      intervalScheduler = Executors.newSingleThreadScheduledExecutor();
      intervalSchedule = new IntervalSchedule();
      isFirst = false;
    }
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
    unableToAnswer.setVisible(true);
    if(isDemo){
      endDemoButton.setVisible(true);
    }else{
      endDemoButton.setVisible(false);
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
    numberOfAnsweredQuestion++;
    if(unableToAnswer.isSelected()){
      answerOutput += "invalid\n";
    }else{
      answerOutput += elapsedTime + "\n";
    }
    unableToAnswer.setSelected(false);
    isQuestioning = false;
    isAnswered = true;
    intervalSchedule.isInterval = true;
    scheduledFuture = 
        intervalScheduler.schedule(intervalSchedule, intervalTime, TimeUnit.MILLISECONDS);
  }
  
  protected void correct(){
    answerOutput += "1, ";
    numberOfCorrectAnswer++; 
    System.out.println("Correct");
  }
  
  protected void wrong(){
    answerOutput += "0, ";
    System.out.println("wrong");
  }
  
  public void newProblem(){
    
  }
  
  public void startMeasureTime(){
    startTime = System.currentTimeMillis();
    isMeasuring = true;
  }
  
  public long stopMeasureTime(){
    elapsedTime = System.currentTimeMillis() - startTime;
    sumOfAnswerTime += elapsedTime;
    isMeasuring = false;
    return elapsedTime;
  }
  
  protected void initOutFile(){
    answerOutput += "Test Name = " + TEST_NAME + "\n";
    answerOutput += "Version = " + TEST_VERSION + "\n";
  }
  
  protected boolean answerCheck(){
    return isQuestioning;
  }
  
  public void saveAnswer(String answers){
    Calendar now = Calendar.getInstance();
    String fileName =
        TEST_NAME
        + Integer.toString(now.get(Calendar.YEAR))
        + Integer.toString(now.get(Calendar.MONTH) + 1)
        + Integer.toString(now.get(Calendar.DATE))
        + Integer.toString(now.get(Calendar.HOUR_OF_DAY))
        + Integer.toString(now.get(Calendar.MINUTE))
        + ".csv";
    File dir = new File("log/" + TEST_NAME);
    if(!dir.exists()){
      dir.mkdir();
    }
    File file = new File("log/" + TEST_NAME + "/" + fileName);
    try{
      PrintWriter pw = 
          new PrintWriter(new BufferedWriter(new FileWriter(file)));
      pw.write(answers);
      pw.close();
    }catch(IOException e){
      e.printStackTrace();
    }  
  }
  
  public void endTest(){
    saveAnswer(answerOutput);
    System.out.println("Result : " + numberOfCorrectAnswer + " / " + numberOfQuestion);
    System.out.println("Average answer time : " + ((double) sumOfAnswerTime / numberOfQuestion) + " ms");
    
    resetTest();
  }
  
  public void endDemo(){
    System.out.println("End Demo");
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
    answerOutput = "";
    
    isDemo = false;
    isStart = false;
    hideAnswerButton();
    startTestButton.setVisible(true);
    startDemoButton.setVisible(true);
    endDemoButton.setVisible(false);
    unableToAnswer.setVisible(false);
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
      endDemo();
    }
    else if (src == unableToAnswer){
      Main.requestFocus();
    }
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
