package scene.usertest;

import gui.Ctrlpanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.media.opengl.GL2GL3;
import javax.swing.JButton;
import javax.swing.JSlider;

import scene.SceneOrganizer;
import scene.oldTypeScene.Scene1;
import za.co.luma.geom.Vector2DInt;

public class Test3 extends SceneOrganizer{
  
//バックグラウンドの明度
  int L = 70;
  
  boolean newProblem = false;
  boolean newProblemSet = false;
  
  Scene1 scene;
  
  int[] viewport1, viewport2;
  
  float[] viewpos1, viewpos2;
  
  ArrayList<Integer> choiceList;
  
  long intervalTime = 1000;
  
  private JSlider answerSlider;
  
  private double correctAnsValue;
  
  private JButton answerButton;
  
  public Test3(){
    super();
    this.numberOfQuestion = 20;
  }
  
  public Test3(int numberOfQuestion){
    super();
    this.numberOfQuestion = numberOfQuestion;
  }
  
  public void init(GL2GL3 gl, Scene1 scene){
    initClearColor(L);
    this.scene = scene;
    initView();
    setSceneViewport();
    setGUI();
    newProblem(gl);
  }
  
  private void initView(){
    viewpos1 = new float[3];
    viewpos2 = new float[3];
    
    double r = 30;
    double rad = 150d / 360d * 2 * Math.PI;
    
    viewpos1[0] = 0; 
    viewpos1[1] = (float) (r * Math.cos(rad));
    viewpos1[2] = (float) (r * Math.sin(rad));
    viewpos2[0] = 0; 
    viewpos2[1] = 0; 
    viewpos2[2] = (float) r;
  }
  
  private void setSceneViewport(){
    viewport1 = new int[4];
    viewport2 = new int[4];
    int offset = 10;
    int width = Math.min(CANVAS_HEIGHT, CANVAS_WIDTH / 2);
    viewport1[0] = offset;
    viewport1[1] = offset;
    viewport1[2] = width - 2 * offset;
    viewport1[3] = width - 2 * offset;
    
    viewport2[0] = viewport1[0] + viewport1[2] + offset;
    viewport2[1] = offset;
    viewport2[2] = width - 2 * offset;
    viewport2[3] = width - 2 * offset;
  }
  
  private void setGUI(){
    answerSlider = new JSlider(0, 200, 200);
    Ctrlpanel.getInstance().addSlider(answerSlider, "scaleSlider");
    
    answerButton = new JButton("Test3 Answer");
    Ctrlpanel.getInstance().addButton(answerButton);
  }
  
  @Override
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    super.setCanvasSize(gl, width, height);
    setSceneViewport();
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
    choiceList = scene.resetAndGetChoiceList(gl, 1);
    scene.setTargetData(gl, choiceList.get(0));
    scene.settingTest3();
    correctAnsValue = scene.test3SetProblem();
    newProblemSet = true;
  }

  @Override
  public void rendering(GL2GL3 gl){

    clearWindow(gl, clearColor);

    if (newProblem){
      return;
    }
    
    if(isInterval){
      interval(gl, intervalTime);
      System.out.println("interval");
      return;
    }

    if(isQuestioning & newProblemSet){
      showQuestion(gl);
    }

    gl.glFlush();

  }
  
  
  @Override
  public void showQuestion(GL2GL3 gl){
    
    gl.glViewport(viewport1[0], viewport1[1], 
        viewport1[2], viewport1[3]);
    scene.setShadowTexCoordSize(viewport1[2], viewport1[3]);
    scene.lookat(viewpos1[0], viewpos1[1], viewpos1[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.test3Rendering1(gl);
    
    
    gl.glViewport(viewport2[0], viewport2[1], 
        viewport2[2], viewport2[3]);
    scene.setShadowTexCoordSize(viewport2[2], viewport2[3]);
    scene.lookat(viewpos2[0], viewpos2[1], viewpos2[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.test3Rendering2(gl);
  }

  @Override
  public void iterate(GL2GL3 gl){
    if (numberOfAnsweredQuestion == numberOfQuestion) {
      endTest();
    }
    
    scene.iterate();
    scene.test3SetAnswer(sliderValueConvert(answerSlider.getValue()));
    
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
  
  private float sliderValueConvert(int value){
    return value / 100f;
  }
  
  private void answer(){
    double error = 
        sliderValueConvert(answerSlider.getValue()) - correctAnsValue;
    answerOutput += Double.toString(error) + ", ";
    System.out.println(error);
    answerSlider.setValue(answerSlider.getMaximum());
    endQuestion();
  }

  @Override
  public void keyPressed(KeyEvent e){
    // TODO Auto-generated method stub

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
  public void mouseMoved(MouseEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void switchSO(GL2GL3 gl){
    // TODO Auto-generated method stub

  }

  @Override
  public void clickButton(ActionEvent e){
    super.clickButton(e);
    Object src = e.getSource();
    if (src == answerButton){
      answer();
    }
  }

}
