package scene.usertest;

import java.util.List;

import gl.Shader;
import gl.Shader.MatrixType;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

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
  
  Shader monoColorShader, billBoardShader;
  int monoColorShaderUniform;
  Billboard whiteRect, shadow;
  float alphaOfShadow = 0.5f;
  Vector2DDouble rectPos = new Vector2DDouble(0, 0);
  
  int[][] colorMosaic;
  int[][] isShadowedColor;
  float widthscale;
  float heightscale;
  
  int patternSize;
  
  PlyLoader board;
  
  @Override
  public void init(GL2GL3 gl){
    initCOLOR_LIST(20);
    initShadowedColorList();
    initShader(gl);
    initMatrix(gl);
    initObj(gl);
    genPoissionDisk();
  }
  
  private void initCOLOR_LIST(int length){
    COLOR_LIST = new float[length][3];
    for(int i = 0; i < length; i++){
      Vector3DDouble color =
          ColorUtil.LabtoRGB(L, 
              R * Math.cos(2 * Math.PI * i / length), 
              R * Math.sin(2 * Math.PI * i / length));
      COLOR_LIST[i][0] = (float) color.x;
      COLOR_LIST[i][1] = (float) color.y;
      COLOR_LIST[i][2] = (float) color.z;
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
        "resources/ShaderSource/MonoColor/frag.c",
        "monoColor");
    monoColorShader.init(gl);
    monoColorShaderUniform = 
        gl.glGetUniformLocation(monoColorShader.getID(), "inColor");
    
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
    monoColorShader.updateMatrix(gl, view, Shader.MatrixType.VIEW);
    
    billBoardShader.use(gl);
    billBoardShader.updateMatrix(gl, view, Shader.MatrixType.VIEW);
    Shader.unuse(gl);
  }
  
  private void initObj(GL2GL3 gl){
    board = new PlyLoader("resources/Objdata/board.ply");
    board.init(gl);
    
    whiteRect = new Billboard(gl, "resources/Image/TextureImage/whiterectangle.png",
        new Vector2DDouble(0.5, 0.5), 1);
    whiteRect.setShader(billBoardShader);
    
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow4.png",
        new Vector2DDouble(0, 0.5), 1);
//    shadow = new Billboard(gl, "resources/Image/TextureImage/hardshadow.png",
//        new Vector2DDouble(0, 0.5), 1);
//    shadow = new Billboard(gl, "resources/Image/TextureImage/circle.png",
//        new Vector2DDouble(0, 0.5), 1);
    shadow.setAlpha(alphaOfShadow);
    shadow.setShader(billBoardShader);
  }
  
  public void genColorMosaic(int width, boolean isShadowed){
    colorMosaic = new int[width][width];
    widthscale = 1 / (float) colorMosaic.length;
    heightscale = 1 / (float) colorMosaic[0].length;
    isShadowedColor = new int[width][width];
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        colorMosaic[i][j] = (int) (Math.random() * COLOR_LIST.length);
        if (isShadowed) {
          isShadowedColor[i][j] = (int) (Math.random() * 2);
        } else {
          isShadowedColor[i][j] = 0;
        }
      }}
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
  
  private void setObjColor(GL2GL3 gl, int i, int j){
    float[] rgb;
    if(isShadowedColor[i][j] == 0){
      rgb = COLOR_LIST[colorMosaic[i][j]];
    } else {
      rgb = SHADOWED_COLOR_LIST[colorMosaic[i][j]];
    }
    gl.glUniform3fv(monoColorShaderUniform, 1, rgb, 0); 
  }
  
  public void setMarkPos(Vector2DDouble pos){
    rectPos.x = (int) (pos.x * colorMosaic.length);
    rectPos.y = (int) (pos.y * colorMosaic.length);
  }
  
  @Override
  public void rendering(GL2GL3 gl){
    
    renderingMosaic(gl, new Vector2DInt(0, 0), colorMosaic.length);
    
    renderingShadows(gl);
    
    //選択領域の表示
    rendeingRect(gl);
  }
  
  public void renderingMosaic(GL2GL3 gl, Vector2DInt leftup, 
      int width){
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
        setObjColor(gl, i + leftup.x, j + leftup.y);
        board.rendering(gl);
        model.glPopMatrix();
      }}
  }
  
  public void renderingShadows(GL2GL3 gl){
    billBoardShader.use(gl);
    model.glLoadIdentity();
    for(Vector2DDouble pos : poissonList){
      model.glPushMatrix();
      model.glTranslatef((float) pos.x, 
          (float) pos.y, 0); 
      model.glScalef(0.05f * 2, 0.05f * 2, 1);
      model.glRotatef(45, 0, 0, 1);
      billBoardShader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
      shadow.renderingWithAlpha(gl, billBoardShader);
      model.glPopMatrix();
    }
  }
  
  public void rendeingRect(GL2GL3 gl){
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

}
