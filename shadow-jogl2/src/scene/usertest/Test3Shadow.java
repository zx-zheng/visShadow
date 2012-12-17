package scene.usertest;

import gui.Ctrlpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.media.opengl.GL2GL3;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JSlider;

import com.sun.media.imageioimpl.plugins.jpeg2000.Box;

import scene.SceneOrganizer;
import scene.oldTypeScene.Scene1;
import za.co.luma.geom.Vector2DInt;

public class Test3Shadow extends SceneOrganizer{

  //ver 1.3 point range 30~90
  //ver 1.3.1 point range 50~95
  //ver 1.3.2 point range 65~98
  private final String TEST_VERSION = "1.3.1";
  private final String TEST_NAME = "ShadowTest3";
  //スライダーを大きくする
//バックグラウンドの明度
  int L = 70;
  
  boolean newProblem = false;
  boolean newProblemSet = false;
  
  Scene1 scene;
  
  int[] viewport1, viewport2;
  
  float[] viewpos1, viewpos2;
  
  ArrayList<Integer> choiceList;
  
  long intervalTime = 1000;
  
  protected JSlider answerSlider;
  
  private double correctAnsValue;
  
  protected JButton answerButton;
  
  private int numberOfQuestionPerShadowRange;
  
  public Test3Shadow(){
    super();
    this.numberOfQuestion = 20;
  }
  
  public Test3Shadow(int numberOfQuestionPerShadowRange){
    super();
    SetTestNameVersion(TEST_NAME, TEST_VERSION);
    this.numberOfQuestionPerShadowRange = numberOfQuestionPerShadowRange;
    this.numberOfQuestion = numberOfQuestionPerShadowRange * 8;
  }
  
  public void init(GL2GL3 gl, Scene1 scene){
    initClearColor(L);
    this.scene = scene;
    initView();
    setSceneViewport();
    setGUI();
    scene.test3genShadowRangeList(numberOfQuestionPerShadowRange);
    Ctrlpanel.getInstance().getCtrlPanel().setVisible(true);
  }
  
  protected void initView(){
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
  
  protected void setSceneViewport(){
    viewport1 = new int[4];
    viewport2 = new int[4];
    int offsetx = 10, offsety = 200;
    int width = Math.min(CANVAS_HEIGHT, CANVAS_WIDTH / 2);
    viewport1[0] = offsetx;
    viewport1[1] = offsety;
    viewport1[2] = width - 2 * offsetx;
    viewport1[3] = width - 2 * offsetx;
    
    viewport2[0] = viewport1[0] + viewport1[2] + offsetx;
    viewport2[1] = offsety;
    viewport2[2] = width - 2 * offsetx;
    viewport2[3] = width - 2 * offsetx;
  }
  
  protected void setGUI(){
    answerSlider = new JSlider(0, 200, 200);
    answerSlider.setPreferredSize(new Dimension(400, 50));
    answerSlider.setVisible(false);
//    Ctrlpanel.getInstance().addSlider(
//        Ctrlpanel.getInstance().getUserTestPane(), answerSlider, "Test3ScaleSlider");
    Ctrlpanel.getInstance().getUserTestPane().add(answerSlider);
    Ctrlpanel.getInstance().getUserTestPane().add(javax.swing.Box.createRigidArea(new Dimension(50, 10)));
    answerButton = new JButton("Answer");
    answerButton.setVisible(false);
    Ctrlpanel.getInstance().addButton(Ctrlpanel.getInstance().getUserTestPane(), answerButton);
  }
  
  @Override
  public void setCanvasSize(GL2GL3 gl, int width, int height){
    super.setCanvasSize(gl, width, height);
    setSceneViewport();
  }
  
  @Override
  protected void showAnswerButton(){
    answerSlider.setVisible(true);
    answerButton.setVisible(true);
  }
  
  @Override
  protected void hideAnswerButton(){
    answerSlider.setVisible(false);
    answerButton.setVisible(false);
  }
  
  @Override
  public void newProblem(){
    
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

    if(isQuestioning){
      showQuestion(gl);
    }

    gl.glFlush();

  }
  
  
  @Override
  protected void showQuestion(GL2GL3 gl){
    
    gl.glViewport(viewport1[0], viewport1[1], 
        viewport1[2], viewport1[3]);
    scene.setShadowTexCoordSize(viewport1[2], viewport1[3]);
    scene.lookat(viewpos1[0], viewpos1[1], viewpos1[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.test3ShadowRendering1(gl);
    
    
    gl.glViewport(viewport2[0], viewport2[1], 
        viewport2[2], viewport2[3]);
    scene.setShadowTexCoordSize(viewport2[2], viewport2[3]);
    scene.lookat(viewpos2[0], viewpos2[1], viewpos2[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.test3ShadowRendering2(gl);
  }

  @Override
  public void iterate(GL2GL3 gl){
    scene.iterate();
    scene.test3SetAnswer(sliderValueConvert(answerSlider.getValue()));
    
    if (!isDemo & numberOfAnsweredQuestion == numberOfQuestion) {
      endTest();
    } else if (nextProblem) {
      newProblem(gl);
      startQuestion();
    }
    
  }
  
  private float sliderValueConvert(int value){
    return value / 100f;
  }
  
  
  protected void initOutFile(){
    super.initOutFile();
    answerOutput +=
        "lab_l = " + Integer.toString(scene.lab_l.getValue()) + "\n"
        + "lab_a = " + Integer.toString(scene.lab_a.getValue()) + "\n"
        + "lab_b = " + Integer.toString(scene.lab_b.getValue()) + "\n"
        + "shadeRange = " + Integer.toString(scene.shaderange.getValue()) + "\n"
        + "poissonInterval = " + Integer.toString(scene.possioninterval.getValue()) + "\n"
        + "viewScale = " + Integer.toString(scene.viewScale.getValue()) + "\n"
        +"error, correct, posx, posy, temperature, shadowRange, time\n";
  }
  
  private void answer(){
    if(!answerCheck()) return;
    double error = 
        sliderValueConvert(answerSlider.getValue()) - correctAnsValue;
    answerOutput += Double.toString(error) 
        + ", " 
        + Double.toString(correctAnsValue)
        + ", "
        + Double.toString(scene.currentData.getChosenPoint().x)
        + ", "
        + Double.toString(scene.currentData.getChosenPoint().y)
        + ", "
        + Double.toString(scene.currentData.funcTemp.getDouble(
                scene.currentData.getChosenPoint().x, 
                scene.currentData.getChosenPoint().y))
        + ", "
        + Integer.toString(scene.shadowrange.getValue()) 
        + ", ";
    System.out.println(error);
    answerSlider.setValue(answerSlider.getMaximum());
    endQuestion();
  }
  
  @Override
  protected void resetTest(){
    super.resetTest();
    answerSlider.setValue(answerSlider.getMaximum());
    scene.test3genShadowRangeList(numberOfQuestionPerShadowRange);
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
