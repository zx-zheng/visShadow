import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.*;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.opengl.util.awt.Screenshot;

import oekaki.util.*;

import util.gl.*;
import util.loader.Load2Dfloat;
import util.loader.Spline;
import util.render.*;
import util.render.obj.Tiledboard;

public class SceneRender{
  
  FBO smapfbo;
  int SHADOWMAPWIDTH = 2048, SHADOWMAPHEIGHT = 2048, shadowswitch; 
  boolean switched = false, rightclicking = false, heightctrl = false;
  public Scene1 scene;
  public TexViewer texviewer;
  Shader shader, shadowmapping,shadowmappingtess;
  TexBindSet tbs, weather;
  Tex2D tex;
  FBO fbo;
  int[] viewport = {0, 0, 1024, 1024};
  float projsize = 3.f;
  float[] proj = {-projsize, projsize, -projsize, projsize};
  double posx = 0, posy= 0, height = 2.0, angle = 0;
  private int filecount;
  Load2Dfloat loader;
  
  public void init(GL2GL3 gl){
    smapfbo = new FBO(gl);
    
    texviewer = new TexViewer();
    texviewer.init(gl);
    
    initWeatherData(gl);
    //initTestData(gl);
    
    scene = new Scene1();
    scene.initShadowmap(gl, 4, SHADOWMAPWIDTH, SHADOWMAPHEIGHT);
//    for(int i = 0; i < scene.lightCount(); i++){
//      float angle = 2 * (float)Math.PI * i / (scene.lightCount() - 1);
//      //scene.lights.get(i).lookat(3, 3, i + 1, 0, 0, 0, 0, 1, 0);
//      scene.lights.get(i).lookat(6*(float)Math.sin(angle), 6*(float)Math.cos(angle), 8, 
//          0, 0, 0, 0, 1, 0);
//      scene.lights.get(i).perspectivef(120f, 1, 0.1f, 50f);
//    }
    scene.lights.get(scene.lightCount()-1).lookatd(0, 0, 20, 0, 0, 0, 0, 1, 0);
    scene.lights.get(scene.lightCount()-1).orthof(-5, 5, -5, 5, 0, 30);

    
    shader = new Shader("src/SimpleShader/vert.c",
        null, null, null, "src/SimpleShader/frag.c");
    shader.init(gl);
    
    shadowmapping = new Shader("src/ShadowMapping/vert.c",
        null, null, "src/ShadowMapping/geom.c",
        "src/ShadowMapping/frag.c");
    shadowmapping.init(gl);
    shadowmapping.use(gl);
    
    //tess evalシェーダーはシャドウマップのものを使う
    shadowmappingtess = new Shader("src/ShadowMapping/vert.c",
        "src/ShadowMapping/ctrl.c", "src/util/render/Shadowmap/eval.c", "src/ShadowMapping/geom.c",
        "src/ShadowMapping/frag.c");
    shadowmappingtess.init(gl);
    
    TexImage img = TexImageUtil.loadImage("original1.png", 3, TexImage.TYPE_BYTE);
    tex = new Tex2D(GL2.GL_RGB, GL2. GL_BGR,
        GL2.GL_UNSIGNED_BYTE, img.width, img.height,
        GL2.GL_LINEAR, GL2.GL_REPEAT, GL2.GL_REPEAT,
         img.buffer, "testimage");
    gl.glActiveTexture(GL2.GL_TEXTURE0);
    tex.init(gl);
    tbs = new TexBindSet(tex);
    tbs.bind(gl);
    
    initshadowmapping(gl, shadowmapping, shadowmappingtess);
    
    //scene.updateligths(gl, shadowmapping);
    scene.init(gl, shadowmapping, shadowmappingtess);
    scene.lookat(0, 0, 7, 0, 0, 0, 0, 1, 0);
    //scene.perspectivef(50, 1, 1f, 30f);
    scene.orthof(proj[0], proj[1], proj[2], proj[3], 0, 30);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.setTessLevel(gl, 64);
    
    
   
    
    fbo = new FBO(gl);
    fbo.attachTexture(gl, tbs);
    fbo.attachDepthRBO(gl, 1024, 1024);
    //*/
  }
  private void initWeatherData(GL2GL3 gl){
    String filepath = "/home/michael/zheng/Programs/WeatherData/2009100812/";
    loader = new Load2Dfloat(filepath + "RelativeHumidity_2.0m_T0.txt",
        filepath + "Temperature_2.0m_T0.txt",
        filepath + "UofWind_10.0m_T0.txt",
        filepath + "VofWind_10.0m_T0.txt");
    loader.loadOffsetLine(0);
//    Tex2D weatherTex = new Tex2D(GL2.GL_RGBA16F, GL2.GL_RGBA, 
//        GL.GL_FLOAT, loader.width, loader.height, 
//        GL.GL_LINEAR, loader.getbuffer(), "wheather data");
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    weatherTex.init(gl);
//    weather = TexUnitManager.getInstance().bind(gl, weatherTex);
    
    int div = 10;
    Load2Dfloat l0 = new Load2Dfloat(filepath + "RelativeHumidity_2.0m_T0.txt");
    l0.loadOffsetLine(0);
    Spline spl0 = new Spline(l0.getbuffer(), l0.width, l0.height, div);
    Load2Dfloat l1 = new Load2Dfloat(filepath + "Temperature_2.0m_T0.txt");
    l1.loadOffsetLine(0);
    Spline spl1 = new Spline(l1.getbuffer(), l1.width, l1.height, div);
    Load2Dfloat l2 = new Load2Dfloat(filepath + "UofWind_10.0m_T0.txt");
    l2.loadOffsetLine(0);
    Spline spl2 = new Spline(l2.getbuffer(), l2.width, l2.height, div);
    Load2Dfloat l3 = new Load2Dfloat(filepath + "VofWind_10.0m_T0.txt");
    l3.loadOffsetLine(0);
    Spline spl3 = new Spline(l3.getbuffer(), l3.width, l3.height, div);
    ByteBuffer[] splarray = {spl0.getSpline(),spl1.getSpline(),
        spl2.getSpline(),spl3.getSpline()};
    ByteBuffer buffer = Load2Dfloat.constructByteBuffer(splarray);
    Tex2D weatherTex = new Tex2D(GL2.GL_RGBA16F, GL2.GL_RGBA, 
        GL.GL_FLOAT, spl0.width(), spl0.height(), 
        GL.GL_LINEAR, buffer, "wheather data");
    gl.glActiveTexture(GL2.GL_TEXTURE0);
    weatherTex.init(gl);
    weather = TexUnitManager.getInstance().bind(gl, weatherTex);
  }
  
  private void initTestData(GL2GL3 gl){
    String filepath = "/home/michael/zheng/Programs/Testdata/";    
    int div = 1;
    Load2Dfloat l0 = new Load2Dfloat(filepath + "test.txt");
    l0.loadOffsetLine(0);
    Spline spl0 = new Spline(l0.getbuffer(), l0.width, l0.height, div);
    Load2Dfloat l1 = new Load2Dfloat(filepath + "test.txt");
    l1.loadOffsetLine(0);
    Spline spl1 = new Spline(l1.getbuffer(), l1.width, l1.height, div);
    Load2Dfloat l2 = new Load2Dfloat(filepath + "test.txt");
    l2.loadOffsetLine(0);
    Spline spl2 = new Spline(l2.getbuffer(), l2.width, l2.height, div);
    Load2Dfloat l3 = new Load2Dfloat(filepath + "test.txt");
    l3.loadOffsetLine(0);
    Spline spl3 = new Spline(l3.getbuffer(), l3.width, l3.height, div);
    ByteBuffer[] splarray = {spl0.getSpline(),spl1.getSpline(),
        spl2.getSpline(),spl3.getSpline()};
    ByteBuffer buffer = Load2Dfloat.constructByteBuffer(splarray);
    Tex2D weatherTex = new Tex2D(GL2.GL_RGBA16F, GL2.GL_RGBA, 
        GL.GL_FLOAT, spl0.width(), spl0.height(), 
        GL.GL_LINEAR, buffer, "wheather data");
    gl.glActiveTexture(GL2.GL_TEXTURE0);
    weatherTex.init(gl);
    weather = TexUnitManager.getInstance().bind(gl, weatherTex);
  }
  
  private void initshadowmapping(GL2GL3 gl, Shader shader, 
        Shader shadertess){
    shader.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "shadowmap"), 
        scene.getShadowmapTexture().texunit);
    
    shadertess.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadertess.getID(), "shadowmap"), 
        scene.getShadowmapTexture().texunit);
    gl.glUniform1i(gl.glGetUniformLocation(shadertess.getID(), "weatherTex"),
        weather.texunit);
    gl.glUniform1i(gl.glGetUniformLocation(shadertess.getID(), "divide"),
        scene.tboard.getdivide());
    shadertess.unuse(gl);
    
    scene.setShadowmapShaderTexture(gl, weather, "weatherTex");
    scene.setShadowmapShader1i(gl, scene.tboard.getdivide(), "divide");
  }
  
  public void rendering(GL2GL3 gl){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    scene.ShadowMap(gl);
    texviewer.rendering(gl, scene.getShadowmapTexture(), 1024, 0, 640, 640);
    //texviewer.rendering(gl, weather, 640, 0, 640, 640);
    gl.glFlush();
    scene.renderingToWindow(gl, viewport[0], viewport[1], 
        viewport[2], viewport[3]);
    scene.iterate();
    //scene.shader1i(gl, shadowswitch, "shadowswitch");
    if(switched){
      scene.shader1i(gl, shadowswitch, "shadowswitch");
      switched=false;
    }
    //screenshot("SS/", 1024, 1024);
  }
  
  public void mouseDragged(MouseEvent e){
    if(javax.swing.SwingUtilities.isLeftMouseButton(e)){
      int x = e.getX();
      int y = e.getY();
      //java.awt.Dimension size = e.getComponent().getSize();
      //System.out.println("mouse" + x + " " + y);
      mouseToWorldCoord(x, y, viewport, proj, loader, scene.tboard, heightctrl);
    }
  }
  
  private void mouseToWorldCoord(int x, int y, int[] viewport, float[] proj,
      Load2Dfloat data, Tiledboard board, boolean heightctrl){
    posx = (proj[1] - proj[0])/viewport[2] * (x-viewport[0])+ proj[0];
    posy = (-proj[3] + proj[2])/viewport[3] * (y-viewport[1]) + proj[3];
    //高さ自動調整
    float heighttmp=0;
    if(heightctrl)heighttmp = mouseToHeight(posx, posy, data, board);
    scene.setLightCircleLookOutside(posx,posy,height+heighttmp,angle,1,1,scene.lightCount());
  }
  
  private float mouseToHeight(double wx, double wy, 
        Load2Dfloat data, Tiledboard board){
    double localx = (wx - board.leftdownx)/ board.width * data.width,
          localy = (wy - board.leftdowny)/ board.height * data.height;
    localx = clamp(localx, 0, data.width);
    localy = clamp(localy, 0, data.height);
    int localxi = (int)localx, localyi = (int)localy;
    double percentx = localx - localxi, percenty = localy- localyi;
    int bufferindex = data.width * (data.height - localyi -1) + localxi;
    FloatBuffer fb = data.getbuffer().asFloatBuffer();
    //System.out.println(percentx+" "+percenty);
    double heightofpoint = 
        (1-percenty)*(getdatafrombuffer(fb, bufferindex)*(1-percentx)
        +getdatafrombuffer(fb, bufferindex+1)*percentx)
        +percenty*(getdatafrombuffer(fb,bufferindex+data.width)*(1-percentx)
            +getdatafrombuffer(fb,bufferindex+data.width+1)*percentx);
    return (float)heightofpoint;
  }
  
  private double getdatafrombuffer(FloatBuffer fb, int index){
    return Math.sqrt(Math.pow(fb.get(4*index+2), 2)
        +Math.pow(fb.get(4*index+3),2)) * 0.05;
  }
  
  public static double clamp (double i, double low, double high) {
    return java.lang.Math.max (java.lang.Math.min (i, high), low);
  }
  
  public void mouseWheelMoved(MouseWheelEvent e){
    if(rightclicking){
      angle += e.getWheelRotation() * 0.5;
      System.out.println(angle);
    }else{
      height += e.getWheelRotation() * 0.1;
    }
    scene.setLightCircleLookOutside(posx,posy,height,angle,1,1,scene.lightCount());
  }
  
  public void keyPressed(KeyEvent arg0){
    switch (arg0.getKeyChar()){
    case 's':
      if(shadowswitch==0){shadowswitch=1;}
      else{shadowswitch=0;}
      switched = true;
      break;
    case 'h':
      if(heightctrl){heightctrl=false;}
      else{heightctrl=true;}
      break;
    }
  }
  
  public void mousePressed(MouseEvent e){
    // TODO Auto-generated method stub
    if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)rightclicking = true;
  }

  public void mouseReleased(MouseEvent e){
    // TODO Auto-generated method stub
    if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) rightclicking = false;
  }
  
  private void screenshot(String path, int width, int height){
    File file = new File(path + "image" + String.format("%04d", filecount) + ".png");
    try {
      //        context.makeCurrent();
      Screenshot.writeToFile(file,width,height);
    } // end of try
    catch(IOException ex){
      System.out.println(ex);
    } // end of try-catch
    filecount++;
  }
}
