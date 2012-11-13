package scene.usertest;

import gl.Shader;
import gl.Shader.MatrixType;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import oekaki.util.Tex2D;

import com.jogamp.opengl.util.PMVMatrix;

import scene.SceneBase;
import util.loader.PlyLoader;
import util.render.obj.Billboard;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.geom.Vector3DDouble;

public class ColorMosaic extends SceneBase{

  PMVMatrix model, view;
  
  //左上が(0,0)になるような射影
  float[] PROJ = {0f, 1f, 1f, 0f, -10, 10};
  float[][] COLOR_LIST = {
      {0, 0, 0},
      {1, 0, 0},
      {0, 1, 0},
      {0, 0, 1},
      {1, 1, 0},
      {1, 0, 1},
      {0, 1, 1},
      {1, 1, 1}
  };
  
  Tex2D colorMosaicTex;
  Shader monoColorShader, billBoardShader;
  int monoColorShaderUniform;
  Billboard whiteRect;
  Vector2DDouble rectPos = new Vector2DDouble(0, 0);
  int[][] colorMosaic;
  int patternSize;
  
  PlyLoader board;
  
  @Override
  public void init(GL2GL3 gl){
    initShader(gl);
    initMatrix(gl);
    initObj(gl);
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
    
    whiteRect = new Billboard(gl, "resources/whiterectangle.png",
        new Vector2DDouble(0.5, 0.5), 1);
    whiteRect.setShader(billBoardShader);
  }
  
  public void genColorMosaic(GL2GL3 gl, int width){
    colorMosaic = new int[width][width];
    for(int i = 0; i < width; i++){
      for(int j = 0; j < width; j++){
        colorMosaic[i][j] = (int) (Math.random() * COLOR_LIST.length);
      }
    }
  }
  
  public void setPatternSize(int size){
    patternSize = size;
  }
  
  private void setObjColor(GL2GL3 gl, float[]  rgb){
    gl.glUniform3fv(monoColorShaderUniform, 1, rgb, 0); 
  }
  
  public void setMarkPos(Vector2DDouble pos){
    rectPos.x = (int) (pos.x * colorMosaic.length);
    rectPos.y = (int) (pos.y * colorMosaic.length);
  }
  
  @Override
  public void rendering(GL2GL3 gl){
    float widthscale = 1 / (float) colorMosaic.length;
    float heightscale = 1 / (float) colorMosaic[0].length;
    
    renderingSubMosaic(gl, new Vector2DInt(0, 0), colorMosaic.length);
    
    //選択領域の表示
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
  
  public void renderingSubMosaic(GL2GL3 gl, Vector2DInt leftup, 
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
        setObjColor(gl, COLOR_LIST[colorMosaic[i + leftup.x][j + leftup.y]]);
        board.rendering(gl);
        model.glPopMatrix();
      }
    }
  }

}
