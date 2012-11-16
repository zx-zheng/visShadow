package scene;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import util.ColorUtil;
import za.co.luma.geom.Vector3DDouble;



public abstract class SceneOrganizer{
  
  protected Vector3DDouble clearColor;
  
  protected int prevMouseX, prevMouseY;
  
  protected long elapsedTime = 0;
  protected long startTime = 0;
  protected long sumOfAnswerTime = 0;
  protected boolean isMeasuring = false;
  protected boolean isAnswered = false;
  protected boolean isInterval = false;
  
  protected boolean nextProblem = false;
  protected boolean isQuestioning = false;
  
  protected int numberOfQuestion;
  protected int numberOfAnsweredQuestion = 0;
  protected int numberOfCorrectAnswer = 0;
  protected String answerOutput = "";
  
  public int CANVAS_WIDTH;
  public int CANVAS_HEIGHT;

  abstract public void rendering(GL2GL3 gl);
  abstract public void iterate(GL2GL3 gl);
  abstract public void keyPressed(KeyEvent e);
  abstract public void mouseDragged(MouseEvent e);
  abstract public void mousePressed(MouseEvent e);
  abstract public void mouseReleased(MouseEvent e);
  abstract public void mouseMoved(MouseEvent e);
  abstract public void switchSO(GL2GL3 gl);
  
  protected void initClearColor(int L){
    clearColor = ColorUtil.LabtoRGB(L, 0, 0);
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
  
  public void showQuestion(GL2GL3 gl){
    
  }
  
  public void interval(GL2GL3 gl, long millis){
    isAnswered = false;
    clearWindow(gl, clearColor);
    try{
      Thread.sleep(millis);
    }catch(InterruptedException e){
      e.printStackTrace();
    };
  }
  
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    CANVAS_WIDTH = width;
    CANVAS_HEIGHT = height;
  }
  
  public void startQuestion(){
    isQuestioning = true;
    isAnswered = false;
    isInterval = false;
    newProblem();
    startMeasureTime();
  }
  
  public void endQuestion(){
    stopMeasureTime();
    answerOutput += elapsedTime + "\n";
    isQuestioning = false;
    isAnswered = true;
  }
  
  protected void correct(){
    answerOutput += "1, ";
    numberOfCorrectAnswer++;
    numberOfAnsweredQuestion++;
    System.out.println("Correct");
  }
  
  protected void wrong(){
    answerOutput += "0, ";
    numberOfAnsweredQuestion++;
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
  
  public void saveAnswer(String answers){
    File file = new File("answer.csv");
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
    System.exit(0);
  }
  
}
