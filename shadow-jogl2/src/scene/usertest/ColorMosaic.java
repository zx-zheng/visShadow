package scene.usertest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gl.FBO;
import gl.Shader;
import gl.TexBindSet;
import gl.Shader.MatrixType;
import gui.Ctrlpanel;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.swing.JSlider;

import oekaki.util.Tex2D;

import com.jogamp.opengl.util.PMVMatrix;

import scene.SceneBase;
import scene.obj.Billboard;
import scene.usertest.PatternMatchTest2.Problem;
import util.ColorUtil;
import util.loader.PlyLoader;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.geom.Vector3DDouble;
import za.co.luma.math.sampling.PoissonDiskSampler;
import za.co.luma.math.sampling.UniformPoissonDiskSampler;

public class ColorMosaic extends SceneBase{

  PMVMatrix model, view;
  UniformPoissonDiskSampler pds;
  List<Vector2DDouble> poissonList;
  double shadewide = 0;
  double L = 69;
  double R = 90;
  int defalutShadow = 10;
  
  float pmt1Rotate = 45f, pmt1Scale = 0.1f;
  float pmt2Rotate = 0f, pmt2Scale = 1;
  
  static boolean guiInit = false;
  static JSlider colorMosaicShadowSlider;
  
  public float projOffset = 0.1f;
  //左上が(0,0)になるような射影
  float[] PROJ = {0f - projOffset, 1f + projOffset,
      1f + projOffset, 0f - projOffset, -10, 10};
//  float[][] COLOR_LIST = {
//      {1, 0, 0},
//      {0, 1, 0},
//      {0, 0, 1},
//      {1, 1, 0},
//      {1, 0, 1},
//      {0, 1, 1},
//      {1, 1, 1}
//  };
  float[][] COLOR_LIST;
  
  float[][] SHADOWED_COLOR_LIST;
  
  ArrayList<Integer> colorIndexList, usedColorIndexList;
  
  Shader monoColorLabShader, billBoardShader, monoColorRGBShader;
  int monoColorLabShaderUniform, monoColorRGBShaderUniform;
  Billboard whiteRect, blackRect, shadow;
  float alphaOfTexture = 1f;
  Vector2DDouble rectPos = new Vector2DDouble(0, 0);
  
  int[][] colorMosaic;
  int[][] comparisonColorMosaic;
  int[][] isChangedColor;
  int[][] comparisonIsChangedColor;
  float widthscale;
  float heightscale;
  
  int patternSize;
  
  public TexBindSet shadowTbs;
  private Tex2D shadowTex;
  private FBO shadowFBO;
  private float[] shadowTexCoordSize = {1, 1};
  
  int numberOfColor = 15;
 
  ArrayList<Vector2DDouble> pmt2SMarkPosList;
  
  PlyLoader board;
  
  @Override
  public void init(GL2GL3 gl){
    initGUI();
    initCOLOR_LIST(numberOfColor);
    initShadowedColorList();
    initShader(gl);
    initMatrix(gl);
    initShadowFBO(gl);
    initObj(gl);
    genPoissionDisk();
  }
  
  public void initForPmt2(GL2GL3 gl){
    pmt2SMarkPosList = new ArrayList<Vector2DDouble>();
    initObjPmt2(gl);
  }
  
  private void initGUI(){
    if(guiInit) return;
    colorMosaicShadowSlider = new JSlider(0, 30, defalutShadow);
    Ctrlpanel.getInstance().addSlider(colorMosaicShadowSlider, "PMT2 shadow");
    guiInit = true;
  }
  
  private void initCOLOR_LIST(int length){
    colorIndexList = new ArrayList<Integer>();
    usedColorIndexList = new ArrayList<Integer>();
    COLOR_LIST = new float[length][3];
//    for(int i = 0; i < length; i++){
//      Vector3DDouble color =
//          ColorUtil.LabtoRGB(L, 
//              R * Math.cos(2 * Math.PI * i / length), 
//              R * Math.sin(2 * Math.PI * i / length));
//      COLOR_LIST[i][0] = (float) color.x;
//      COLOR_LIST[i][1] = (float) color.y;
//      COLOR_LIST[i][2] = (float) color.z;
//    }
    for(int i = 0; i < length; i++){
      COLOR_LIST[i][0] = (float) (L + shadewide * 15);
      COLOR_LIST[i][1] = (float) (R * Math.cos(2 * Math.PI * i / length));
      COLOR_LIST[i][2] = (float) (R * Math.sin(2 * Math.PI * i / length));
      colorIndexList.add(i);
    }
  }
  
  private void initShadowedColorList(){
    SHADOWED_COLOR_LIST = new float[COLOR_LIST.length][COLOR_LIST[0].length];
    for (int i = 0; i < COLOR_LIST.length; i++) {
      for (int j = 0; j < COLOR_LIST[0].length; j++) {
        SHADOWED_COLOR_LIST[i][j] = alphaOfTexture * COLOR_LIST[i][j];
      }}
  }
  
  private void initShader(GL2GL3 gl) {
    monoColorLabShader = new Shader(
        "resources/ShaderSource/MonoColor/vert.c",
        null, 
        null, 
        "resources/ShaderSource/MonoColor/geom.c", 
        "resources/ShaderSource/MonoColor/fragshadow.c",
        "monoColor");
    monoColorLabShader.init(gl);
    monoColorLabShader.adduniform(gl, "shadowRange", colorMosaicShadowSlider.getValue());
    monoColorLabShaderUniform = 
        gl.glGetUniformLocation(monoColorLabShader.getID(), "inColorLab");
    
    monoColorRGBShader = new Shader(
        "resources/ShaderSource/MonoColor/vert.c",
        null, 
        null, 
        "resources/ShaderSource/MonoColor/geom.c", 
        "resources/ShaderSource/MonoColor/frag.c",
        "monoColor");
    monoColorRGBShader.init(gl);
    monoColorRGBShaderUniform = 
        gl.glGetUniformLocation(monoColorRGBShader.getID(), "inColor");
    
    billBoardShader = new Shader(
        "resources/ShaderSource/Billboard/vert.c",
        null, 
        null, 
        "resources/ShaderSource/Billboard/geom.c", 
        "resources/ShaderSource/Billboard/frag.c",
        "billBoard");
    billBoardShader.init(gl);
    
  }
  
  private void initMatrix(GL2GL3 gl){
    model = new PMVMatrix();
    model.glLoadIdentity();
    view = new PMVMatrix();
    view.glLoadIdentity();
    view.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
    view.glOrthof(PROJ[0], PROJ[1], PROJ[2], PROJ[3],
        PROJ[4], PROJ[5]);
    monoColorLabShader.use(gl);
    monoColorLabShader.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
    
    monoColorRGBShader.use(gl);
    monoColorRGBShader.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
    
    billBoardShader.use(gl);
    billBoardShader.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
    Shader.unuse(gl);
  }
  
  private void initShadowFBO(GL2GL3 gl){
    shadowTex = new Tex2D(GL2.GL_RGBA, GL2.GL_RGBA,
        GL2.GL_FLOAT, 1920, 1200,
        GL2.GL_LINEAR, GL2.GL_CLAMP, GL2.GL_CLAMP,
         null, "shadowTex");
    shadowTex.init(gl);
    
    shadowTbs = new TexBindSet(shadowTex);
    shadowTbs.bind(gl);
    
    shadowFBO = new FBO(gl);
    shadowFBO.attachTexture(gl, shadowTbs);
    
    monoColorLabShader.adduniform(gl, "shadowTex", shadowTbs.texunit);
    monoColorLabShader.adduniform(gl, "shadowTexCoordSize", 2, shadowTexCoordSize);
  }
  
  public void setShadowTexCoordSize(int width, int height){
    shadowTexCoordSize[0] = (float) width / shadowTbs.tex.width;
    shadowTexCoordSize[1] = (float) height / shadowTbs.tex.height;
    monoColorLabShader.setuniform("shadowTexCoordSize", shadowTexCoordSize);
  }
  
  private void initObj(GL2GL3 gl){
    board = new PlyLoader("resources/Objdata/board.ply");
    board.init(gl);
    
    whiteRect = new Billboard(gl, "resources/Image/TextureImage/whiterectangle.png",
        new Vector2DDouble(0.5, 0.5), 1);
    whiteRect.setShader(billBoardShader);
    
    blackRect = new Billboard(gl, "resources/Image/TextureImage/blackrectangle.png",
        new Vector2DDouble(0.5, 0.5), 1);
    blackRect.setShader(billBoardShader);
    
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow4.png",
        new Vector2DDouble(0, 0.5), 1);
//    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow4.png");
//    shadow = new Billboard(gl, "resources/Image/TextureImage/hardshadow.png",
//        new Vector2DDouble(0, 0.5), 1);
//    shadow = new Billboard(gl, "resources/Image/TextureImage/circle.png",
//        new Vector2DDouble(0, 0.5), 1);
    shadow.setAlpha(alphaOfTexture);
    shadow.setShader(billBoardShader);
  }
  
  private void initObjPmt2(GL2GL3 gl){
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow6.png");
  }
  
  public void genColorMosaic(int width, boolean isShadowed){
    colorMosaic = new int[width][width];
    widthscale = 1 / (float) colorMosaic.length;
    heightscale = 1 / (float) colorMosaic[0].length;
    isChangedColor = new int[width][width];
    resetColorIndexList();
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        //colorMosaic[i][j] = (int) (Math.random() * COLOR_LIST.length);
        colorMosaic[i][j] = getColorIndexFromList();
        if (isShadowed) {
          isChangedColor[i][j] = (int) (Math.random() * 2);
        } else {
          isChangedColor[i][j] = 0;
        }
      }}
    
    
  }
  
  private int getColorIndexFromList(){
    if(colorIndexList.isEmpty()){
      resetColorIndexList();
    }
    int index = (int) (Math.random() * colorIndexList.size());
    int colorIndex = colorIndexList.get(index);
    colorIndexList.remove(index);
    usedColorIndexList.add(colorIndex);
    return colorIndex;
  }
  
  private void resetColorIndexList(){
    colorIndexList.addAll(usedColorIndexList);
    usedColorIndexList.clear();
  }
  
  public void genComparisonColorMosaic(int type){
    if(colorMosaic == null){
      System.out.println("Problem Generation error");
      return;
    }
    pmt2SMarkPosList.clear();
    comparisonColorMosaic = new int[colorMosaic.length][colorMosaic.length];
    comparisonIsChangedColor = new int[colorMosaic.length][colorMosaic.length];
    for(int i = 0; i < colorMosaic.length; i++){
      for(int j = 0; j < colorMosaic.length; j++){
        comparisonColorMosaic[i][j] = colorMosaic[i][j];
        comparisonIsChangedColor[i][j] = isChangedColor[i][j];
      }
    }
    int diffIndex = (int) (Math.random() * colorMosaic.length * colorMosaic.length);
    int indexw = diffIndex / comparisonColorMosaic.length;
    int indexh = diffIndex % comparisonColorMosaic.length;
    
    int diffIndex2;
    while((diffIndex2 = 
        (int) (Math.random() * colorMosaic.length * colorMosaic.length))
        == diffIndex){}
    int indexw2 = diffIndex2 / comparisonColorMosaic.length;
    int indexh2 = diffIndex2 % comparisonColorMosaic.length;
    //System.out.println(diffIndex + " " + diffIndex2);
    //indexw = indexh = 1;
    System.out.println(indexw + " " + indexh);
    
    if(type == 0){
      //same pattern
    }else if(type == 1){
      pmt2SMarkPosList.add(pmt2ShadowPos(indexw, indexh)); 
    }else if(type == 2){
      changeColor(comparisonColorMosaic, indexw, indexh);
    }else if(type == 3){
      pmt2SMarkPosList.add(pmt2ShadowPos(indexw, indexh));
      changeColor(comparisonColorMosaic, indexw2, indexh2);
    }else if(type == 4){
      pmt2SMarkPosList.add(pmt2ShadowPos(indexw, indexh));
      changeColor(comparisonColorMosaic, indexw, indexh);
    }else if(type == 5){
      inverseIsShadowedColor(comparisonIsChangedColor, indexw, indexh);
    }else if(type == 6){
      
    }else{
      System.out.println("Unknown Problem Type");
    }
  }
  
  private void changeColor(int[][] array, int x, int y){
    int newColor;
    int orignalColor = array[x][y];
    //while((newColor = (int) (Math.random() * COLOR_LIST.length)) == orignalColor){}
    while((newColor = getColorIndexFromList()) == orignalColor){}
    array[x][y] = newColor;
  }
  
  private void inverseIsShadowedColor(int[][] array, int x, int y){
    if(array == null){
      System.out.println("Inverse error");
      return;
    }
    if(array[x][y] == 1){
      array[x][y] = 0;
    }else{
      array[x][y] = 1;
    }
  }
  
  private Vector2DDouble pmt2ShadowPos(int x, int y){
    double grid = 1.0 / (colorMosaic.length);
     return new Vector2DDouble((x + 0.5) * grid  , (y + 0.5) * grid);
  }
  
  public void genFakeAnswer(int count, Vector2DInt ansPoint, boolean isShadowed){
    int[][] answerBackup = new int[patternSize][patternSize];
    int[][] isShadowedBackup = new int[patternSize][patternSize];
    
    //正解のバックアップ
    loadMosaicAndShadowed(ansPoint, answerBackup, isShadowedBackup);
    
    Vector2DInt rewritePoint = new Vector2DInt(0, 0);
    
    //ランダム領域を正解で書き換えたあと一つだけ変更
    for(int i = 0; i < count; i++){
      rewritePoint.x = (int) (Math.random() * (colorMosaic.length - patternSize));
      rewritePoint.y = (int) (Math.random() * (colorMosaic.length - patternSize));
      //System.out.println(rewritePoint);
      rewriteMosaicAndShadowed(rewritePoint, answerBackup, isShadowedBackup);
      Vector2DInt fakePoint = new Vector2DInt(
          rewritePoint.x + (int) (Math.random() * patternSize),
          rewritePoint.y + (int) (Math.random() * patternSize) );
      rewriteOneMosaic(fakePoint, 
          newColorIndex( colorMosaic[fakePoint.x][fakePoint.y]) );
      if (isShadowed){
        isChangedColor[fakePoint.x][fakePoint.y] = 
            (int) (Math.random() * 2);
      }
    }
    
    //正解地点を復元
    rewriteMosaicAndShadowed(ansPoint, answerBackup, isShadowedBackup);
  }
  
  private void loadMosaicAndShadowed(Vector2DInt pos, int[][] mosaic, int shadow[][]){
    for(int i = 0; i < mosaic.length; i++){
      for(int j = 0; j < mosaic[i].length; j++){
        mosaic[i][j] = colorMosaic[pos.x + i][pos.y + j];
        shadow[i][j] = isChangedColor[pos.x + i][pos.y + j];
      }}
  }
  
  private void rewriteMosaicAndShadowed(Vector2DInt pos, int[][] mosaic, int shadow[][]){
    for(int i = 0; i < mosaic.length; i++){
      for(int j = 0; j < mosaic[i].length; j++){
        colorMosaic[pos.x + i][pos.y + j] = mosaic[i][j];
        isChangedColor[pos.x + i][pos.y + j] = shadow[i][j];
      }}
  }
  
  private void rewriteOneMosaic(Vector2DInt pos, int index){
    colorMosaic[pos.x][pos.y] = index;
  }
  
  private int newColorIndex(int i){
    int j = (int) (Math.random() * COLOR_LIST.length);
    while(i == j){
      j = (int) (Math.random() * COLOR_LIST.length);
    }
    return j;
  }
  
  public void genPoissionDisk(){
    genPoissionDisk(0.05);
  }
  
  public void genPoissionDisk(double interval){
    pds = new UniformPoissonDiskSampler(-0.5, -0.5, 1.5, 1.5, interval);
    poissonList = pds.sample();
  }
  
  public void setPatternSize(int size){
    patternSize = size;
  }
  
  private void setObjColorRGB(GL2GL3 gl, int[][] colorMosaic, 
      int[][] isChangedColor, int i, int j, 
      Vector3DDouble combineColor, float ratioOfMark){
    Vector3DDouble originalRGB = ColorUtil.LabtoRGB(COLOR_LIST[colorMosaic[i][j]][0],
        COLOR_LIST[colorMosaic[i][j]][1],
        COLOR_LIST[colorMosaic[i][j]][2]);
    Vector3DDouble rgbVector;
    float[] rgb = new float[3];
    if(isChangedColor == null || isChangedColor[i][j] == 0){
      rgbVector = originalRGB;
    } else {
      rgbVector = 
          Vector3DDouble.add(Vector3DDouble.scale(originalRGB, 1 - ratioOfMark), 
              Vector3DDouble.scale(combineColor, ratioOfMark));
      //rgb = SHADOWED_COLOR_LIST[colorMosaic[i][j]];
      System.out.println("isChanged");
    }
    rgb[0] = (float) rgbVector.x;
    rgb[1] = (float) rgbVector.y;
    rgb[2] = (float) rgbVector.z;
    gl.glUniform3fv(monoColorRGBShaderUniform, 1, rgb, 0); 
  }
  
  private void setObjColorLab(GL2GL3 gl, int[][] colorMosaic, 
      int[][] isShadowedColor, int i, int j){
    float[] lab;
    if(isShadowedColor == null || isShadowedColor[i][j] == 0){
      lab = COLOR_LIST[colorMosaic[i][j]];
    } else {
      lab = new float[3];
      lab[0] = COLOR_LIST[colorMosaic[i][j]][0] - colorMosaicShadowSlider.getValue();
      lab[1] = COLOR_LIST[colorMosaic[i][j]][1];
      lab[2] = COLOR_LIST[colorMosaic[i][j]][2];
    }
    //System.out.println(lab[0]);
    gl.glUniform3fv(monoColorLabShaderUniform, 1, lab, 0); 
  }
  
  public void setMarkPos(Vector2DDouble pos){
    if(colorMosaic == null)return;
    rectPos.x = (int) (pos.x * colorMosaic.length);
    rectPos.y = (int) (pos.y * colorMosaic.length);
  }
  
  public void setMarkPos(Vector2DDouble pos, int width){
    rectPos.x = (int) (pos.x * width);
    rectPos.y = (int) (pos.y * width);
  }
  
  @Override
  public void rendering(GL2GL3 gl){
    
    pmt1RenderingShadowsFBO(gl);
    renderingMosaic(gl, new Vector2DInt(0, 0), colorMosaic.length);
    
    //renderingShadows(gl);
    
    //選択領域の表示
    renderingRect(gl);
  }
  
  public void rendeingAllColor(GL2GL3 gl){
    int width = (int) (Math.sqrt(COLOR_LIST.length) + 1);
    float widthScale = 1f / width;
    
    //モザイクの表示
    monoColorLabShader.use(gl);
    model.glLoadIdentity();
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        model.glPushMatrix();
        model.glTranslatef(i / (float) width, 
            j / (float) width, 0); 
        model.glScalef(widthScale, widthScale, 1);
        monoColorLabShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
        gl.glUniform3fv(monoColorLabShaderUniform, 1,
            COLOR_LIST[(i * width + j) % COLOR_LIST.length], 0); 
        board.rendering(gl);
        model.glPopMatrix();
      }}
  }
  
  public void pmt2Rendering1(GL2GL3 gl, Problem problem){
    fboClear(gl);
    if(problem.color == null){
      colorMosaicShadowSlider.setValue((int) problem.alphaOfBillBoard);
      monoColorLabShader.setuniform("shadowRange", (int)problem.alphaOfBillBoard);
      renderingMosaic(gl, new Vector2DInt(0, 0), colorMosaic.length);
    } else {
      renderingMosaicRGBCombine(gl, colorMosaic, isChangedColor, 
          new Vector2DInt(0, 0), colorMosaic.length, problem.color, problem.billBoard.alpha);
    }
  }
  
  public void pmt2Rendering1(GL2GL3 gl){
    fboClear(gl);
    renderingMosaic(gl, new Vector2DInt(0, 0), colorMosaic.length);
  }
  
  public void pmt2Rendering2(GL2GL3 gl, Problem problem){
    if(problem.billBoard == null){
      pmt2Rendering2Shadow(gl);
    } else {
      pmt2Rendering2MosaicAndMark(gl, problem.billBoard, problem.color);
    }
  }
  
  public void pmt2Rendering2Shadow(GL2GL3 gl){
    //pmt2Scale = 1f;// / colorMosaic.length;
    pmt2RenderingShadows(gl);
    renderingMosaicLab(gl, comparisonColorMosaic, comparisonIsChangedColor,  
        new Vector2DInt(0, 0), colorMosaic.length);
  }
  
  public void pmt2Rendering2MosaicAndMark(GL2GL3 gl, Billboard billBoard, 
      Vector3DDouble markColor){
    renderingMosaicRGBCombine(gl, comparisonColorMosaic, comparisonIsChangedColor,
        new Vector2DInt(0, 0), colorMosaic.length, markColor, billBoard.alpha);
    renderingBillBoardList(gl, billBoard, pmt2SMarkPosList, 0, pmt2Scale);
  }
  
  private void pmt2RenderingShadows(GL2GL3 gl){
    renderingShadowsFBOCore(gl, pmt2SMarkPosList, pmt2Rotate, pmt2Scale);
  }
  
  public void pmt3Rendering1(GL2GL3 gl, scene.usertest.PatternMatchTest3.ProblemSet problem){
    fboClear(gl);
    renderingMosaicLab(gl, problem.color);
  }
  
  public void pmt3Rendering2(GL2GL3 gl, scene.usertest.PatternMatchTest3.ProblemSet problem){
    renderingOneBillBoardFBO(gl, problem.billBoard, 
        new Vector2DDouble(0.5, 0.5), 0, 1);
    renderingMosaicLab(gl, problem.secondColor);
  }
  
  public void serachColorRendering(GL2GL3 gl, scene.usertest.SearchColor.Problem problem,
      Vector3DDouble clearColorLab, float scale){
    
    if(problem.mark != null){
      if(problem.blendType == 0){
        fboClear(gl);
        //
        renderingMosaicLab(gl, problem.colorArray, scale);
        renderingBillBoardList(gl, problem.mark, problem.poissonList, 
            pmt1Rotate, pmt1Scale);
      } else if(problem.blendType == 1){
        renderingBillBoardListFBO(gl, problem.mark, problem.poissonList, 
            pmt1Rotate, pmt1Scale);
        renderingMosaicLab(gl, problem.colorArray, scale);
        renderingMosaicLab(gl, clearColorLab, new Vector2DDouble(-0.5, -0.5), 10);
      } 
    } else {
      fboClear(gl);
      renderingMosaicLab(gl, problem.colorArray, scale);
    }
  }
  
  public void renderingMosaicLab(GL2GL3 gl, Vector3DDouble labColor){
    renderingMosaicLab(gl, labColor, new Vector2DDouble(0, 0), 1);
  }

  public void renderingMosaicLab(GL2GL3 gl, Vector3DDouble labColor, 
      Vector2DDouble pos, float scale){
    //fboClear(gl);
    monoColorLabShader.use(gl);
    model.glLoadIdentity();
    model.glScalef(scale, scale, 1);
    model.glTranslatef((float) pos.x, (float) pos.y, 0);
 
    monoColorLabShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
    setObjColorLab(gl, labColor);
    board.rendering(gl);   
  }
  
  public void renderingMosaicLab(GL2GL3 gl, Vector3DDouble labColor, float offsetz){
    //fboClear(gl);
    monoColorLabShader.use(gl);
    model.glLoadIdentity();
    model.glTranslatef(0, 0, offsetz);
    monoColorLabShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
    setObjColorLab(gl, labColor);
    board.rendering(gl);   
  }
  
  private void setObjColorLab(GL2GL3 gl, Vector3DDouble color){
    float[] lab = new float[3];
    lab[0] = (float) color.x;
    lab[1] = (float) color.y;
    lab[2] = (float) color.z;
    gl.glUniform3fv(monoColorLabShaderUniform, 1, lab, 0); 
  }
  
  public void renderingMosaicLab(GL2GL3 gl, int[][] colorMosaic,
      int[][] isChangedColor, Vector2DInt leftup, int width){
    float widthscale = 1 / (float) width;
    float heightscale = 1 / (float) width;
    
    //モザイクの表示
    monoColorLabShader.use(gl);
    model.glLoadIdentity();
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        model.glPushMatrix();
        model.glTranslatef(i / (float) width, 
            j / (float) width, 0); 
        model.glScalef(widthscale, heightscale, 1);
        monoColorLabShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
        setObjColorLab(gl, colorMosaic, isChangedColor, i + leftup.x, j + leftup.y);
        board.rendering(gl);
        model.glPopMatrix();
      }}
  }
  
  public void renderingMosaicRGBCombine(GL2GL3 gl, int[][] colorMosaic,
      int[][] isChangedColor, Vector2DInt leftup, int width,
      Vector3DDouble CombineColor, float alpha){
    float widthscale = 1 / (float) width;
    float heightscale = 1 / (float) width;
    
    //モザイクの表示
    monoColorRGBShader.use(gl);
    model.glLoadIdentity();
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        model.glPushMatrix();
        model.glTranslatef(i / (float) width, 
            j / (float) width, 0); 
        model.glScalef(widthscale, heightscale, 1);
        monoColorRGBShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
        setObjColorRGB(gl, colorMosaic, isChangedColor, 
            i + leftup.x, j + leftup.y, CombineColor, alpha);
        board.rendering(gl);
        model.glPopMatrix();
      }}
  }
  
  public void renderingMosaic(GL2GL3 gl, Vector2DInt leftup, 
      int width){
    renderingMosaicLab(gl, this.colorMosaic, this.isChangedColor, leftup, width);
  }
  
  public void renderingMosaicLab(GL2GL3 gl, Vector3DDouble[][] colorArray, float scale){
    int width = colorArray.length;
    int height = colorArray[0].length;
    float widthscale = 1 / (float) (width);
    float heightscale = 1 / (float) (width);
    float offset = widthscale * (1 - scale) * 0.5f;
    //モザイクの表示
    monoColorLabShader.use(gl);
    model.glLoadIdentity();
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        model.glPushMatrix();
        model.glTranslatef(i * widthscale + offset, 
            j * heightscale + offset, 0); 
        model.glScalef(widthscale * scale, heightscale * scale, 1);
        monoColorLabShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
        setObjColorLab(gl, colorArray[i][j]);
        board.rendering(gl);
        model.glPopMatrix();
      }}
  }
  
  public void pmt1RenderingShadows(GL2GL3 gl){
    renderingBillBoardList(gl, shadow, poissonList, pmt1Rotate, pmt1Scale);
  }
  
  public void pmt1RenderingShadowsFBO(GL2GL3 gl){
    renderingShadowsFBOCore(gl, poissonList, pmt1Rotate, pmt1Scale);
  }
  
  public void renderingOneBillBoardFBO(GL2GL3 gl, Billboard billboard,
      Vector2DDouble pos, float rotate, float scale){
    int[] viewport = new int[4];
    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
    gl.glViewport(0, 0, viewport[2], viewport[3]);
    
    shadowFBO.bind(gl);
    gl.glClearColor(1, 1, 1, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
    renderingOneBillBoard(gl, billboard, pos, rotate, scale);
    FBO.unbind(gl);
    
    gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
  }
  
  private void renderingOneBillBoard(GL2GL3 gl, Billboard billBoard,
      Vector2DDouble pos, float rotate, float scale){
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    billBoardShader.use(gl);
    model.glLoadIdentity();
    model.glPushMatrix();     
    model.glScalef(scale,  scale, 1);
    model.glTranslatef((float) pos.x, 
        (float) pos.y, 0);     
    model.glRotatef(rotate, 0, 0, 1);
    billBoardShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
    billBoard.renderingWithAlpha(gl, billBoardShader);
    model.glPopMatrix();

    gl.glDisable(GL2.GL_BLEND);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
  public void renderingBillBoardList(GL2GL3 gl, Billboard billboard, 
      List<Vector2DDouble> list,
      float rotate, float scale){
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    billBoardShader.use(gl);
    model.glLoadIdentity();
    for(Vector2DDouble pos : list){
      model.glPushMatrix();
      model.glTranslatef((float) pos.x, 
          (float) pos.y, 0); 
      model.glScalef(scale,  scale, 1);
      model.glRotatef(rotate, 0, 0, 1);
      billBoardShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
      billboard.renderingWithAlpha(gl, billBoardShader);
      model.glPopMatrix();
    }
    gl.glDisable(GL2.GL_BLEND);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
  public void renderingShadowsFBOCore(GL2GL3 gl, List<Vector2DDouble> list,
      float rotate, float scale){
    int[] viewport = new int[4];
    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
    gl.glViewport(0, 0, viewport[2], viewport[3]);
    
    shadowFBO.bind(gl);
    gl.glClearColor(1, 1, 1, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
    renderingBillBoardList(gl, shadow, list, rotate, scale);
    FBO.unbind(gl);
    
    gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
  }
  
  public void renderingBillBoardListFBO(GL2GL3 gl, Billboard billBoard,
      List<Vector2DDouble> list,
      float rotate, float scale){
    int[] viewport = new int[4];
    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
    gl.glViewport(0, 0, viewport[2], viewport[3]);
    
    shadowFBO.bind(gl);
    gl.glClearColor(1, 1, 1, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
    renderingBillBoardList(gl, billBoard, list, rotate, scale);
    FBO.unbind(gl);
    
    gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
  }
  
  public void fboClear(GL2GL3 gl){
    int[] viewport = new int[4];
    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
    gl.glViewport(0, 0, viewport[2], viewport[3]);
    
    shadowFBO.bind(gl);
    gl.glClearColor(1, 1, 1, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
    FBO.unbind(gl);
    
    gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
  }
  
  public void renderingRect(GL2GL3 gl){
    billBoardShader.use(gl); 
    model.glPushMatrix();
    model.glTranslatef((float) (rectPos.x / colorMosaic.length), 
        (float) (rectPos.y / colorMosaic[0].length), 0); 
    model.glScalef(patternSize * widthscale, 
        patternSize * heightscale, 1);
    billBoardShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
    whiteRect.renderingWithAlpha(gl);
    model.glPopMatrix();
  }
  
  public void renderingRect(GL2GL3 gl, int width){
    billBoardShader.use(gl); 
    model.glLoadIdentity();
    model.glPushMatrix();
    model.glTranslatef((float) (rectPos.x / width), 
        (float) (rectPos.y / width), 0); 
    model.glScalef((float) (1.0 / width), 
        (float) (1.0 / width), 1);
    billBoardShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
    blackRect.renderingWithAlpha(gl);
    model.glPopMatrix();
  }
  
  public void renderingRectList(GL2GL3 gl, List<Vector2DInt> list, double width){
    billBoardShader.use(gl); 
    for(Vector2DInt pos: list){
      model.glPushMatrix();
      model.glTranslatef((float) (pos.x / width), 
          (float) (pos.y / width), 0); 
      model.glScalef((float) (1.0 / width), 
          (float) (1.0 / width), 1);
      billBoardShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
      whiteRect.renderingWithAlpha(gl);
      model.glPopMatrix();
    }  
  }

  
  @Override
  public void iterate(){
    monoColorLabShader.setuniform("shadowRange", colorMosaicShadowSlider.getValue());
  }

}
