package scene.usertest;

import java.util.ArrayList;
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
  double L = 70;
  double R = 90;
  int defalutShadow = 10;
  
  float pmt1Rotate = 45f, pmt1Scale = 0.1f;
  float pmt2Rotate = 0f, pmt2Scale;
  
  static boolean guiInit = false;
  static JSlider colorMosaicShadowSlider;
  
  //左上が(0,0)になるような射影
  float[] PROJ = {0f, 1f, 1f, 0f, -10, 10};
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
  
  Shader monoColorShader, billBoardShader;
  int monoColorShaderUniform;
  Billboard whiteRect, shadow;
  float alphaOfShadow = 1f;
  Vector2DDouble rectPos = new Vector2DDouble(0, 0);
  
  int[][] colorMosaic;
  int[][] comparisonColorMosaic;
  int[][] isShadowedColor;
  int[][] comparisonIsShadowedColor;
  float widthscale;
  float heightscale;
  
  int patternSize;
  
  public TexBindSet shadowTbs;
  private Tex2D shadowTex;
  private FBO shadowFBO;
  private float[] shadowTexCoordSize = {1, 1};
 
  ArrayList<Vector2DDouble> pmt2ShadowList;
  
  PlyLoader board;
  
  @Override
  public void init(GL2GL3 gl){
    initGUI();
    initCOLOR_LIST(20);
    initShadowedColorList();
    initShader(gl);
    initMatrix(gl);
    initShadowFBO(gl);
    initObj(gl);
    genPoissionDisk();
  }
  
  public void initForPmt2(GL2GL3 gl){
    pmt2ShadowList = new ArrayList<Vector2DDouble>();
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
      COLOR_LIST[i][0] = (float) L;
      COLOR_LIST[i][1] = (float) (R * Math.cos(2 * Math.PI * i / length));
      COLOR_LIST[i][2] = (float) (R * Math.sin(2 * Math.PI * i / length));
      colorIndexList.add(i);
    }
  }
  
  private void initShadowedColorList(){
    SHADOWED_COLOR_LIST = new float[COLOR_LIST.length][COLOR_LIST[0].length];
    for (int i = 0; i < COLOR_LIST.length; i++) {
      for (int j = 0; j < COLOR_LIST[0].length; j++) {
        SHADOWED_COLOR_LIST[i][j] = alphaOfShadow * COLOR_LIST[i][j];
      }}
  }
  
  private void initShader(GL2GL3 gl) {
    monoColorShader = new Shader(
        "resources/ShaderSource/MonoColor/vert.c",
        null, 
        null, 
        "resources/ShaderSource/MonoColor/geom.c", 
        "resources/ShaderSource/MonoColor/fragshadow.c",
        "monoColor");
    monoColorShader.init(gl);
    monoColorShader.adduniform(gl, "shadowRange", colorMosaicShadowSlider.getValue());
    monoColorShaderUniform = 
        gl.glGetUniformLocation(monoColorShader.getID(), "inColorLab");
    
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
    monoColorShader.use(gl);
    monoColorShader.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
    
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
    
    monoColorShader.adduniform(gl, "shadowTex", shadowTbs.texunit);
    monoColorShader.adduniform(gl, "shadowTexCoordSize", 2, shadowTexCoordSize);
  }
  
  public void setShadowTexCoordSize(int width, int height){
    shadowTexCoordSize[0] = (float) width / shadowTbs.tex.width;
    shadowTexCoordSize[1] = (float) height / shadowTbs.tex.height;
    monoColorShader.setuniform("shadowTexCoordSize", shadowTexCoordSize);
  }
  
  private void initObj(GL2GL3 gl){
    board = new PlyLoader("resources/Objdata/board.ply");
    board.init(gl);
    
    whiteRect = new Billboard(gl, "resources/Image/TextureImage/whiterectangle.png",
        new Vector2DDouble(0.5, 0.5), 1);
    whiteRect.setShader(billBoardShader);
    
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow4.png",
        new Vector2DDouble(0, 0.5), 1);
//    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow4.png");
//    shadow = new Billboard(gl, "resources/Image/TextureImage/hardshadow.png",
//        new Vector2DDouble(0, 0.5), 1);
//    shadow = new Billboard(gl, "resources/Image/TextureImage/circle.png",
//        new Vector2DDouble(0, 0.5), 1);
    shadow.setAlpha(alphaOfShadow);
    shadow.setShader(billBoardShader);
  }
  
  private void initObjPmt2(GL2GL3 gl){
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow6.png");
  }
  
  public void genColorMosaic(int width, boolean isShadowed){
    colorMosaic = new int[width][width];
    widthscale = 1 / (float) colorMosaic.length;
    heightscale = 1 / (float) colorMosaic[0].length;
    isShadowedColor = new int[width][width];
    resetColorIndexList();
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        //colorMosaic[i][j] = (int) (Math.random() * COLOR_LIST.length);
        colorMosaic[i][j] = getColorIndexFromList();
        if (isShadowed) {
          isShadowedColor[i][j] = (int) (Math.random() * 2);
        } else {
          isShadowedColor[i][j] = 0;
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
    pmt2ShadowList.clear();
    comparisonColorMosaic = new int[colorMosaic.length][colorMosaic.length];
    comparisonIsShadowedColor = new int[colorMosaic.length][colorMosaic.length];
    for(int i = 0; i < colorMosaic.length; i++){
      for(int j = 0; j < colorMosaic.length; j++){
        comparisonColorMosaic[i][j] = colorMosaic[i][j];
        comparisonIsShadowedColor[i][j] = isShadowedColor[i][j];
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
      pmt2ShadowList.add(pmt2ShadowPos(indexw, indexh)); 
    }else if(type == 2){
      changeColor(comparisonColorMosaic, indexw, indexh);
    }else if(type == 3){
      pmt2ShadowList.add(pmt2ShadowPos(indexw, indexh));
      changeColor(comparisonColorMosaic, indexw2, indexh2);
    }else if(type == 4){
      pmt2ShadowList.add(pmt2ShadowPos(indexw, indexh));
      changeColor(comparisonColorMosaic, indexw, indexh);
    }else if(type == 5){
      inverseIsShadowedColor(comparisonIsShadowedColor, indexw, indexh);
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
        isShadowedColor[fakePoint.x][fakePoint.y] = 
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
        shadow[i][j] = isShadowedColor[pos.x + i][pos.y + j];
      }}
  }
  
  private void rewriteMosaicAndShadowed(Vector2DInt pos, int[][] mosaic, int shadow[][]){
    for(int i = 0; i < mosaic.length; i++){
      for(int j = 0; j < mosaic[i].length; j++){
        colorMosaic[pos.x + i][pos.y + j] = mosaic[i][j];
        isShadowedColor[pos.x + i][pos.y + j] = shadow[i][j];
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
    pds = new UniformPoissonDiskSampler(0, 0, 1, 1, 0.05);
    poissonList = pds.sample();
  }
  
  public void setPatternSize(int size){
    patternSize = size;
  }
  
  private void setObjColor(GL2GL3 gl, int[][] colorMosaic, 
      int[][] isShadowedColor, int i, int j){
    float[] rgb;
    if(isShadowedColor == null || isShadowedColor[i][j] == 0){
      rgb = COLOR_LIST[colorMosaic[i][j]];
    } else {
      rgb = SHADOWED_COLOR_LIST[colorMosaic[i][j]];
    }
    gl.glUniform3fv(monoColorShaderUniform, 1, rgb, 0); 
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
    gl.glUniform3fv(monoColorShaderUniform, 1, lab, 0); 
  }
  
  public void setMarkPos(Vector2DDouble pos){
    if(colorMosaic == null)return;
    rectPos.x = (int) (pos.x * colorMosaic.length);
    rectPos.y = (int) (pos.y * colorMosaic.length);
  }
  
  @Override
  public void rendering(GL2GL3 gl){
    
    renderingShadowsFBO(gl);
    renderingMosaic(gl, new Vector2DInt(0, 0), colorMosaic.length);
    
    //renderingShadows(gl);
    
    //選択領域の表示
    renderingRect(gl);
  }
  
  public void pmt2Rendering1(GL2GL3 gl){
    renderingShadowsFBOClear(gl);
    renderingMosaic(gl, new Vector2DInt(0, 0), colorMosaic.length);
  }
  
  public void pmt2Rendering2(GL2GL3 gl){
    pmt2Scale = 1f;// / colorMosaic.length;
    pmt2RenderingShadows(gl);
    renderingMosaic(gl, comparisonColorMosaic, comparisonIsShadowedColor,  
        new Vector2DInt(0, 0), colorMosaic.length);
    //pmt2
  }
  
  public void renderingMosaic(GL2GL3 gl, int[][] colorMosaic,
      int[][] isShadowedColor, Vector2DInt leftup, int width){
    float widthscale = 1 / (float) width;
    float heightscale = 1 / (float) width;
    
    //モザイクの表示
    monoColorShader.use(gl);
    model.glLoadIdentity();
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        model.glPushMatrix();
        model.glTranslatef(i / (float) width, 
            j / (float) width, 0); 
        model.glScalef(widthscale, heightscale, 1);
        monoColorShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
        setObjColorLab(gl, colorMosaic, isShadowedColor, i + leftup.x, j + leftup.y);
        board.rendering(gl);
        model.glPopMatrix();
      }}
  }
  
  public void renderingMosaic(GL2GL3 gl, Vector2DInt leftup, 
      int width){
    renderingMosaic(gl, this.colorMosaic, this.isShadowedColor, leftup, width);
  }
  
  private void pmt2RenderingShadows(GL2GL3 gl){
    renderingShadowsFBOCore(gl, pmt2ShadowList, pmt2Rotate, pmt2Scale);
  }
  
  public void renderingShadows(GL2GL3 gl){
    renderingShadowsCore(gl, poissonList, pmt1Rotate, pmt1Scale);
  }
  
  public void renderingShadowsCore(GL2GL3 gl, List<Vector2DDouble> list,
      float rotate, float scale){
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    billBoardShader.use(gl);
//    shadow.setAlpha(0.3f);
    model.glLoadIdentity();
    for(Vector2DDouble pos : list){
      model.glPushMatrix();
      model.glTranslatef((float) pos.x, 
          (float) pos.y, 0); 
      model.glScalef(scale,  scale, 1);
      model.glRotatef(rotate, 0, 0, 1);
      billBoardShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
      shadow.renderingWithAlpha(gl, billBoardShader);
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
    renderingShadowsCore(gl, list, rotate, scale);
    FBO.unbind(gl);
    
    gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
  }
  
  public void renderingShadowsFBO(GL2GL3 gl){
    renderingShadowsFBOCore(gl, poissonList, pmt1Rotate, pmt1Scale);
  }
  
  public void renderingShadowsFBOClear(GL2GL3 gl){
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
  
  @Override
  public void iterate(){
    monoColorShader.setuniform("shadowRange", colorMosaicShadowSlider.getValue());
  }

}
