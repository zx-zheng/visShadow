package scene;
import gl.*;
import gui.Ctrlpanel;
import gui.GuiVariables;

import java.awt.event.ActionEvent;
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

import render.*;
import scene.obj.Tiledboard;
import scene.oldTypeScene.Scene1;
import scene.templateScene.Filter;
import scene.templateScene.TexViewer;
import scene.usertest.ColorMosaic;
import scene.usertest.PatternMatchTest;
import scene.usertest.Test1;
import scene.usertest.Test2;
import scene.usertest.Test3Color;
import scene.usertest.Test3Shadow;
import util.loader.Load2Dfloat;
import util.loader.Spline;

public class SceneRender extends GuiVariables{
  
  FBO smapfbo;
  int SHADOWMAPWIDTH = 2048, SHADOWMAPHEIGHT = 2048, shadowswitch; 
  public int CANVAS_WIDTH, CANVAS_HEIGHT, 
  CANVAS_WIDTH_prev, CANVAS_HEIGHT_prev;
  boolean switched = true, rightclicking = false, heightctrl = false;
  public Scene1 scene;
  public TexViewer texviewer;
  public Filter fxaa;
  Shader shader, shadowmapping,shadowmappingtess;
  TexBindSet tbs;
  Tex2D tex;
  FBO fbo;
  int[] viewport = {0, 0, 1024, 1024};
  float projsize = 3.f;
  float[] proj = {-projsize, projsize, -projsize, projsize};
  double posx = 0, posy= 0, height = 5.0, angle = 0;
  private int filecount;
  Load2Dfloat loader;
  float gamma = 1, border = 0, heightscale = 0.05f;
  
  GLTimer timer = new GLTimer();
  
  SceneOrganizer currentSO;
  ViewResult viewResult;
  Test1 test1;
  Test2 test2;
  Test3Shadow test3Shadow;
  Test3Color test3Color;
  
  public void init(GL2GL3 gl){
    timer.init(gl);
    
    smapfbo = new FBO(gl);
    
    texviewer = new TexViewer();
    texviewer.init(gl);
    
    fxaa = new Filter("resources/ShaderSource/FXAA/vert.c", "resources/ShaderSource/FXAA/frag.c");
    fxaa.init(gl);
    
    //String filepath = "/home/michael/zheng/Programs/WeatherData/2009100718/";
    String filepath = "/home/michael/zheng/Programs/WeatherData/2009100818/";
    //String filepath = "/home/michael/zheng/Programs/WeatherData/2008061418/";
    
    //initTestData(gl);
    
    scene = new Scene1();
    scene.initShadowmap(gl, 2, SHADOWMAPWIDTH, SHADOWMAPHEIGHT);
    for(int i = 0; i < scene.lightCount(); i++){
      scene.lights.get(i).init(gl);
//      float angle = 2 * (float)Math.PI * i / (scene.lightCount() - 1);
//      //scene.lights.get(i).lookat(3, 3, i + 1, 0, 0, 0, 0, 1, 0);
//      scene.lights.get(i).lookat(6*(float)Math.sin(angle), 6*(float)Math.cos(angle), 8, 
//          0, 0, 0, 0, 1, 0);
//      scene.lights.get(i).perspectivef(120f, 1, 0.1f, 50f);
    }
    scene.lights.get(0).lookatd(4, 4, 10, 0, 0, 0, 0, 1, 0);
    //scene.lights.get(0).lookatd(0, 0, 50, 0, 0, 0, 0, 1, 0);
    scene.lights.get(0).orthof(-4, 4, -4, 4, 0, 60);
    scene.lights.get(0).setattr(0, 1);
    scene.lights.get(0).setintensity(1.25);
    
//    scene.lights.get(2).lookatd(4, 4, 10, 0, 0, 0, 0, 1, 0);
//    scene.lights.get(2).orthof(-5, 5, -5, 5, 0, 30);
    
    scene.lights.get(1).lookatd(4, -4, 10, 0, 0, 0, 0, 1, 0);
    scene.lights.get(1).orthof(-4, 4, -4, 4, 0, 100);
    
//    scene.lights.get(2).lookatd(0, 0, 50, 0, 0, 0, 0, 1, 0);
//    scene.lights.get(2).orthof(-5, 5, -5, 5, 0, 100);

    
    shader = new Shader("resources/ShaderSource/BaseShaders/SimpleShader/vert.c",
        null, null, null, "resources/ShaderSource/BaseShaders/SimpleShader/frag.c");
    shader.init(gl);
    
    shadowmapping = new Shader("resources/ShaderSource/ShadowMapping/vert.c",
        null, null, "resources/ShaderSource/ShadowMapping/geom.c",
        "resources/ShaderSource/ShadowMapping/fragsimple.c");
    shadowmapping.init(gl);
    shadowmapping.use(gl);
    
    //tess evalシェーダーはシャドウマップのものを使う
    shadowmappingtess = new Shader("resources/ShaderSource/ShadowMapping/vert.c",
        "resources/ShaderSource/ShadowMapping/ctrl.c", 
        "resources/ShaderSource/BaseShaders/Shadowmap/eval.c", 
        "resources/ShaderSource/ShadowMapping/geom.c",
        "resources/ShaderSource/ShadowMapping/frag.c");
    shadowmappingtess.init(gl);
    
    tex = new Tex2D(GL2.GL_RGBA, GL2.GL_RGBA,
        GL2.GL_FLOAT, 1024, 1024,
        GL2.GL_LINEAR, GL2.GL_REPEAT, GL2.GL_REPEAT,
         null, "testimage");

    tex.init(gl);
    tbs = new TexBindSet(tex);
    tbs.bind(gl);
    
//    scene.updateligths(gl, shadowmapping);
//    scene.lookat(0, 0, 30, 0, 0, 0, 0, 1, 0);
//    scene.orthof(proj[0], proj[1], proj[2], proj[3], 0, 100);
    
  //initの場所はあとで調整
    scene.init(gl, shadowmapping, shadowmappingtess);
    //円柱オフ
    scene.shader1i(gl, 0, "offheight");
       
    fbo = new FBO(gl);
    fbo.attachTexture(gl, tbs);
    fbo.attachDepthRBO(gl, 1024, 1024);
    //*/
    initSceneOrganizer(gl);
  }
  
  private void initSceneOrganizer(GL2GL3 gl){
//    viewResult = new ViewResult(gl, scene);
//    viewResult.addTexViewer(texviewer);
//    currentSO = viewResult;
    
//    test1 = new Test1();
//    test1.init(gl, scene, CANVAS_WIDTH, CANVAS_HEIGHT);
//    currentSO = test1;
    
//    test2 = new Test2(gl, scene);
//    currentSO = test2;
    
    test3Shadow = new Test3Shadow(8);
    test3Shadow.init(gl, scene);
    currentSO = test3Shadow;
    
//    test3Color = new Test3Color();
//    test3Color.init(gl, scene);
//    currentSO = test3Color;
    
//    PatternMatchTest pmt = new PatternMatchTest(8);
//    pmt.init(gl);
    //currentSO = pmt;
  }
  
  public void rendering(GL2GL3 gl){
    updateCanvasSize(gl);test3Shadow = new Test3Shadow(10);
//  
    currentSO.rendering(gl);
    currentSO.iterate(gl);
    if(switched){
      scene.shader1f(gl, gamma, "gamma");
      scene.shader1f(gl, heightscale, "heightscale");
      scene.shadowmapshader1f(gl, heightscale, "heightscale");

      switched=false;
    }
    //screenshot("SS/ipsj2/", 1024, 1024);
  }
  
  private void updateCanvasSize(GL2GL3 gl){
    if(CANVAS_WIDTH_prev != CANVAS_WIDTH
        || CANVAS_HEIGHT_prev != CANVAS_HEIGHT){
      currentSO.setCanvasSize(gl, CANVAS_WIDTH, CANVAS_HEIGHT);
      CANVAS_WIDTH_prev = CANVAS_WIDTH;
      CANVAS_HEIGHT_prev = CANVAS_HEIGHT;
    }
  }
  
  public void mouseDragged(MouseEvent e){
    currentSO.mouseDragged(e);

////      mouseToWorldCoord(x, y, viewport, proj, loader, scene.tboard, heightctrl);
////      border = (float)y/(float)viewport[3]-0.1f;
////      switched = true;

  }
  
  private void mouseToWorldCoord(int x, int y, int[] viewport, float[] proj,
      Load2Dfloat data, Tiledboard board, boolean heightctrl){
    posx = (proj[1] - proj[0])/viewport[2] * (x-viewport[0])+ proj[0];
    posy = (-proj[3] + proj[2])/viewport[3] * (y-viewport[1]) + proj[3];
    System.out.println("light " + posx + " " + posy);
    //scene.setlightpos(0, (float)posx, (float)posy);
    double rad = Math.acos(posx/Math.sqrt(posx * posx + posy * posy));
    if(posy < 0) rad = 2 * Math.PI - rad;
    double r = Math.sqrt(32);
    scene.setlightpospolar(0, r, rad);
    scene.setlightdirectionrotate(rad);
    //高さ自動調整
    //float heighttmp=0;
    //if(heightctrl)heighttmp = mouseToHeight(posx, posy, data, board);
    //scene.setLightCircleLookOutside(posx,posy,height+heighttmp,angle,1,1,scene.lightCount());
    posx = (double)x/viewport[2]*2*Math.PI;
    posx = 2*Math.PI*0.8;
    //scene.setLightCircleLookInside(posx, height);
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
      //angle += e.getWheelRotation() * 0.5;
      heightscale += e.getWheelRotation() * 0.01;
      switched = true;
      System.out.println(heightscale);
    }else{
      height += e.getWheelRotation() * 0.1;
      //scene.setLightCircleLookInside(posx, height);
    }
    //scene.setLightCircleLookOutside(posx,posy,height,angle,1,1,scene.lightCount());
  }
  
  public void keyPressed(KeyEvent e){
    currentSO.keyPressed(e);
    switch (e.getKeyChar()){
    case 's':
      if(shadowswitch==0){shadowswitch=1;}
      else{shadowswitch=0;}
      switched = true;
      break;
    case 'h':
      if(heightctrl){heightctrl=false;}
      else{heightctrl=true;}
      break;
    case 'p':
      gamma+=0.1;
      System.out.println(gamma);
      switched = true;
      break;
    case 'o':
      gamma-=0.1;
      System.out.println(gamma);
      switched = true;
      break;
    }
  }
  
  public void mousePressed(MouseEvent e){
    if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)rightclicking = true;
    currentSO.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e){
    if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) rightclicking = false;
    currentSO.mouseReleased(e);
  }
  
  public void mouseMoved(MouseEvent e){
    currentSO.mouseMoved(e);
  }
  
  public void clickButton(ActionEvent e){
    currentSO.clickButton(e);
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
