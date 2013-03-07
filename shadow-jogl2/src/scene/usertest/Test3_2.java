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
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import com.sun.media.imageioimpl.plugins.jpeg2000.Box;

import scene.SceneOrganizer;
import scene.obj.Billboard;
import scene.oldTypeScene.Scene1;
import scene.usertest.Test1_2.MarkTypeAndAlpha;
import za.co.luma.geom.Vector2DInt;

public class Test3_2 extends SceneOrganizer{

  private final String TEST_VERSION = "1.0.0";
  private final String TEST_NAME = "Test3_2";
  //スライダーを大きくする
//バックグラウンドの明度
  int L = 69;  
  Scene1 scene;
  
  int[] viewport1, viewport2;
  
  float[] viewpos1, viewpos2;
  
  int SLIDER_OFFSET = 13;
  
  ArrayList<Integer> choiceList;
  
  long intervalTime = 1000;
  
  protected JSlider answerSlider;
  protected JLabel answerValueLabel;
  
  private double correctAnsValue;
  
  protected JButton answerButton;
  
  private int numberOfQuestionPerMark;
  
//  private Billboard blackHardCircle, whiteHardCircle, 
//  blackCircle, whiteCircle;
  
  //private Billboard[] billBoardArray;
  private int numberOfMarkType = 2;

//  billBoardArray[2] = blackHardCircle;
//  billBoardArray[3] = whiteHardCircle;
//  billBoardArray[0] = blackCircle;
//  billBoardArray[1] = whiteCircle;
  private float billBoardSize = 
      (float) (Scene1.shadowTexSize * billBoardTexSizeRatio);
  
  private MarkTypeAndAlpha currentTA;
  private ArrayList<MarkTypeAndAlpha> problemList;
  
  public Test3_2(){
    super();
    this.numberOfQuestion = 20;
  }
  
  public Test3_2(int numberOfQuestionPerMark){
    super();
    SetTestNameVersion(TEST_NAME, TEST_VERSION);
    this.numberOfQuestionPerMark = numberOfQuestionPerMark;
    this.numberOfQuestion = numberOfQuestionPerMark * 8 * numberOfMarkType;
  }
  
  public void init(GL2GL3 gl, Scene1 scene){
    initClearColor(L);
    this.scene = scene;
    initView();
    initBillBoard(gl);
    setGUI();
    setSceneViewport();
    scene.test3genShadowRangeList(numberOfQuestionPerMark);
    Ctrlpanel.getInstance().getCtrlPanel().setVisible(true);
    problemList = new ArrayList<Test3_2.MarkTypeAndAlpha>();
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
    super.initBillBoard(gl);
//    whiteHardCircle = new Billboard(gl, 
//        "resources/Image/TextureImage/whiteHardCircle.png", billBoardSize);
//    blackHardCircle = new Billboard(gl, 
//        "resources/Image/TextureImage/blackHardCircle2.png", billBoardSize);
//    whiteCircle = new Billboard(gl, 
//        "resources/Image/TextureImage/circlewhite.png", billBoardSize);
//    blackCircle = new Billboard(gl, 
//        "resources/Image/TextureImage/circle.png", billBoardSize);
//    billBoardArray = new Billboard[4];
//    
//    billBoardArray[2] = blackHardCircle;
//    billBoardArray[3] = whiteHardCircle;
//    billBoardArray[0] = blackCircle;
//    billBoardArray[1] = whiteCircle;
  }
  
  protected void setSceneViewport(){
    viewport1 = new int[4];
    viewport2 = new int[4];
    int offsetx = 10, offsety = 200;
    int legendWidth = answerSlider.getWidth();
    int legendHeight = 100;
    int width = Math.min(CANVAS_HEIGHT, CANVAS_WIDTH) - legendHeight - offsety;
    
    viewport1[0] = (int) ((CANVAS_WIDTH - legendWidth) * 0.5);
    viewport1[1] = CANVAS_HEIGHT - legendHeight;
    viewport1[2] = legendWidth;
    viewport1[3] = legendHeight;
    
    
    viewport2[0] = (int) ((CANVAS_WIDTH - width) * 0.5);
    viewport2[1] = offsety;
    viewport2[2] = width - 2 * offsetx;
    viewport2[3] = width - 2 * offsetx;
  }
  
  protected void setGUI(){
    answerSlider = new JSlider(0, 100, 100);
    answerSlider.setPaintLabels(true);
    answerSlider.setLabelTable(answerSlider.createStandardLabels(20));
    answerSlider.setPreferredSize(new Dimension(600, 50));
    answerSlider.setVisible(false);
    answerSlider.setBackground(Color.WHITE);
    answerSlider.addChangeListener(Ctrlpanel.getInstance());
//    Ctrlpanel.getInstance().addSlider(
//        Ctrlpanel.getInstance().getUserTestPane(), answerSlider, "Test3ScaleSlider");
    Ctrlpanel.getInstance().getUserTestPane().add(answerSlider);
    answerValueLabel = new JLabel(" Answer : " + Integer.toString(answerSlider.getValue()));
    answerValueLabel.setPreferredSize(new Dimension(100, 50));
    answerValueLabel.setVisible(false);
    Ctrlpanel.getInstance().getUserTestPane().add(answerValueLabel);
    
    //すきまを追加
    //Ctrlpanel.getInstance().getUserTestPane().add(javax.swing.Box.createRigidArea(new Dimension(50, 10)));
    
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
  protected void startTest(){
    Ctrlpanel.getInstance().getUserTestPane().setVisible(true);
    super.startTest();
  }
  
  @Override
  protected void showAnswerButton(){
    answerSlider.setVisible(true);
    answerValueLabel.setVisible(true);
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
    System.out.println("Question" + (numberOfAnsweredQuestion+1) + "/" + numberOfQuestion);
    choiceList = scene.resetAndGetChoiceList(gl, 1);
    scene.setTargetData(gl, choiceList.get(0));
    scene.settingTest3();
    correctAnsValue = scene.test3CompareSetProblem();
    int index = (int) (Math.random() * problemList.size());
    currentTA = problemList.get(index);
    problemList.remove(index);
  }
  
  private void newProblemSet(){
    numberOfQuestion = 0;
    problemList.clear();
    for(int i = 0; i < numberOfQuestionPerMark; i++){
//      problemList.add(new MarkTypeAndAlpha(blackCircle, 0, blackAlpha, "blackCircle"));
//      problemList.add(new MarkTypeAndAlpha(blackHardCircle, 0, blackAlpha, "blackHardCircle"));
//      problemList.add(new MarkTypeAndAlpha(shadow, 0, blackAlpha, "shadow"));
//      problemList.add(new MarkTypeAndAlpha(hardShadow, 0, blackAlpha, "hardshadow"));
      problemList.add(new MarkTypeAndAlpha(blackCircle, 1, lDown, "blackCircle"));
      problemList.add(new MarkTypeAndAlpha(blackHardCircle, 1, lDown, "blackHardCircle"));
      problemList.add(new MarkTypeAndAlpha(shadow, 1, lDown, "shadow"));
      problemList.add(new MarkTypeAndAlpha(hardShadow, 1, lDown, "hardshadow"));
      numberOfQuestion += 4;
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
    //System.out.println(answerSlider.getLocation());
    int mergin = 100;
    int sliderSize = answerSlider.getWidth() - SLIDER_OFFSET * 2;
    float offsety = 0;
    if(currentTA.name.contains("shadow")){
      offsety = 0.7f;
    }
    
    viewport1[2] = answerSlider.getWidth() + mergin * 2;
    viewport1[0] = (int) ((CANVAS_WIDTH - viewport1[2]) * 0.5);
    
    gl.glViewport(answerSlider.getLocation().x - mergin, viewport1[1], 
        viewport1[2], viewport1[3]);
    
    scene.setShadowTexCoordSize(viewport1[2], viewport1[3]);
    scene.lookat(viewpos2[0], viewpos2[1], viewpos2[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.renderingLegend(gl, currentTA.billBoard, 
        (float) (viewport2[2] / (double) viewport1[2]),
        (float) (viewport1[2] / (double) viewport1[3]),
        (float) ((double) sliderSize / viewport1[2]), 
        (float) ((double)(mergin + SLIDER_OFFSET) / (double) (viewport1[2])), offsety,
        2);
    
    
    gl.glViewport(viewport2[0], viewport2[1], 
        viewport2[2], viewport2[3]);
    scene.setShadowTexCoordSize(viewport2[2], viewport2[3]);
    scene.lookat(viewpos2[0], viewpos2[1], viewpos2[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    if(currentTA.billBoard != null){
      currentTA.billBoard.setAlpha(currentTA.alpha);
    } else {
      scene.shadowrange.setValue((int) currentTA.alpha);
    }
    scene.test3_2Rendering(gl, currentTA);
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
//        +"marktype, error, alpha, correct, posx, posy, temperature, time\n";
        .append("模様の種類, マークの表示法 0:alpha 1:L, 誤差, alpha, 正解値, 解答時間\n");
  }
  
  private void answer(){
    if(!answerCheck()) return;
    double error = 
        sliderValueConvert(answerSlider.getValue()) - correctAnsValue;
    
    if(Math.abs(error) < 0.08){
      correct();
    } else {
      wrong();
    }
    answerOutput.append(currentTA.name)
    .append(", ")
    .append(currentTA.combineType)
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
    Ctrlpanel.getInstance().getUserTestPane().setVisible(false);
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
  public void stateChanged(ChangeEvent e) {
    answerValueLabel.setText(" Answer : " + Integer.toString(answerSlider.getValue()));
  }

  @Override
  public void actionPerformed(ActionEvent e){
    super.actionPerformed(e);
    Object src = e.getSource();
    if (src == answerButton){
      answer();
    }
  }
  
  public class MarkTypeAndAlpha{
    public float alpha;
    //public int type;
    public Billboard billBoard;
    public int combineType;
    public String name;
    
    public MarkTypeAndAlpha(Billboard billBoard, int combineType, float alpha, String name){
      this.billBoard = billBoard;
      this.combineType = combineType;
      this.alpha = alpha;
      this.name = name;
    }
  }

}
