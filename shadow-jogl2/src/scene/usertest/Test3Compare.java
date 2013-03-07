package scene.usertest;

import gui.Ctrlpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.media.opengl.GL2GL3;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JSlider;

import com.sun.media.imageioimpl.plugins.jpeg2000.Box;

import scene.SceneOrganizer;
import scene.obj.Billboard;
import scene.oldTypeScene.Scene1;
import za.co.luma.geom.Vector2DInt;

public class Test3Compare extends SceneOrganizer{

  private final String TEST_VERSION = "1.0.0";
  private final String TEST_NAME = "CompareTest3";
  //スライダーを大きくする
//バックグラウンドの明度
  int L = 70;  
  Scene1 scene;
  
  int[] viewport1, viewport2;
  
  float[] viewpos1, viewpos2;
  
  ArrayList<Integer> choiceList;
  
  long intervalTime = 1000;
  
  protected JSlider answerSlider;
  
  private double correctAnsValue;
  
  protected JButton answerButton;
  
  private int numberOfQuestionPerAlpha;
  
  private Billboard blackHardCircle, whiteHardCircle, 
  blackCircle, whiteCircle;
  
  private Billboard[] billBoardArray;
  private int numberOfMarkType = 2;

//  billBoardArray[2] = blackHardCircle;
//  billBoardArray[3] = whiteHardCircle;
//  billBoardArray[0] = blackCircle;
//  billBoardArray[1] = whiteCircle;
  private float billBoardSize = 0.8f;
  
  private MarkTypeAndAlpha currentTA;
  private ArrayList<MarkTypeAndAlpha> problemList;
  
  public Test3Compare(){
    super();
    this.numberOfQuestion = 20;
  }
  
  public Test3Compare(int numberOfQuestionPerAlpha){
    super();
    SetTestNameVersion(TEST_NAME, TEST_VERSION);
    this.numberOfQuestionPerAlpha = numberOfQuestionPerAlpha;
    this.numberOfQuestion = numberOfQuestionPerAlpha * 8 * numberOfMarkType;
  }
  
  public void init(GL2GL3 gl, Scene1 scene){
    initClearColor(L);
    this.scene = scene;
    initView();
    initBillBoard(gl);
    setSceneViewport();
    setGUI();
    scene.test3genShadowRangeList(numberOfQuestionPerAlpha);
    Ctrlpanel.getInstance().getCtrlPanel().setVisible(true);
    problemList = new ArrayList<Test3Compare.MarkTypeAndAlpha>();
    newProblemSet();
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
  
  @Override
  protected void initBillBoard(GL2GL3 gl){
    whiteHardCircle = new Billboard(gl, 
        "resources/Image/TextureImage/whiteHardCircle.png", billBoardSize);
    blackHardCircle = new Billboard(gl, 
        "resources/Image/TextureImage/blackHardCircle.png", billBoardSize);
    whiteCircle = new Billboard(gl, 
        "resources/Image/TextureImage/circlewhite.png", billBoardSize);
    blackCircle = new Billboard(gl, 
        "resources/Image/TextureImage/circle.png", billBoardSize);
    billBoardArray = new Billboard[4];
    
    billBoardArray[2] = blackHardCircle;
    billBoardArray[3] = whiteHardCircle;
    billBoardArray[0] = blackCircle;
    billBoardArray[1] = whiteCircle;
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
    correctAnsValue = scene.test3CompareSetProblem();
    int index = (int) (Math.random() * problemList.size());
    currentTA = problemList.get(index);
    problemList.remove(index);
  }
  
  private void newProblemSet(){
    problemList.clear();
    for(int i = 0; i < numberOfMarkType; i++){
      for(int j = 1; j <= 8; j++){
        for(int k = 0; k < numberOfQuestionPerAlpha; k++){
          problemList.add(new MarkTypeAndAlpha(i, j * 0.02f + 0.04f));
        }
      }
    }
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
    billBoardArray[currentTA.type].setAlpha(currentTA.alpha);
    //scene.test3CompareRendering(gl, billBoardArray[currentTA.type], 0);
  }

  @Override
  public void iterate(GL2GL3 gl){
    scene.iterate(gl);
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
    answerOutput
        .append("lab_l = ").append(scene.lab_l.getValue()).append("\n")
        .append("lab_a = ").append(scene.lab_a.getValue()).append("\n")
        .append("lab_b = ").append(scene.lab_b.getValue()).append("\n")
        .append("shadeRange = ").append(scene.shaderange.getValue()).append("\n")
        .append("poissonInterval = ").append(scene.possioninterval.getValue()).append("\n")
        .append("viewScale = ").append(scene.viewScale.getValue()).append("\n")
        .append("marktype, error, alpha, correct, time\n");
  }
  
  private void answer(){
    if(!answerCheck()) return;
    double error = 
        sliderValueConvert(answerSlider.getValue()) - correctAnsValue;
    answerOutput.append(currentTA.type) 
        .append(", ")
        .append(error) 
        .append(", ") 
        .append(currentTA.alpha) 
        .append(", ")
        .append(correctAnsValue)
        .append(", ");
    System.out.println(error);
    answerSlider.setValue(answerSlider.getMaximum());
    endQuestion();
  }
  
  @Override
  protected void resetTest(){
    super.resetTest();
    answerSlider.setValue(answerSlider.getMaximum());
    newProblemSet();
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
  public void actionPerformed(ActionEvent e){
    super.actionPerformed(e);
    Object src = e.getSource();
    if (src == answerButton){
      answer();
    }
  }
  
  class MarkTypeAndAlpha{
    public float alpha;
    public int type;
    
    public MarkTypeAndAlpha(int type, float alpha){
      this.type = type;
      this.alpha = alpha;
    }
  }

}
