package scene;
import gui.Ctrlpanel;
import gui.SceneJCheckBox;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.JSlider;
import javax.swing.plaf.SliderUI;

import util.gl.Shader;
import util.loader.Load2Dfloat;
import util.loader.PlyLoader;
import util.render.Scene;
import util.render.obj.Light;
import util.render.obj.Tiledboard;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.math.function.ByteBufferFloat2Wrapper2D;
import za.co.luma.math.function.List2Wrapper2D;
import za.co.luma.math.function.RealFunction2DWrapper;
import za.co.luma.math.sampling.PoissonDiskSampler;
import za.co.luma.math.sampling.UniformPoissonDiskSampler;

public class Scene1 extends Scene {
  
  PlyLoader box = new PlyLoader("src/util/loader/ObjData/box.ply");
  PlyLoader board = new PlyLoader("src/util/loader/ObjData/board.ply");
  PlyLoader sphere = new PlyLoader("src/util/loader/ObjData/sphere3.ply");
  PlyLoader hex = new PlyLoader("src/util/loader/ObjData/square6.ply");
  PlyLoader plus = new PlyLoader("src/util/loader/ObjData/minus.ply");
  PlyLoader tube = new PlyLoader("src/util/loader/ObjData/tube6.ply");
  PlyLoader ring = new PlyLoader("src/util/loader/ObjData/ring.ply");

  //データの解像度
  private final int  GRID_SIZE_X = 201, GRID_SIZE_Y = 251;
  
  //データの間隔
  private final double GRID_INTERVAL_X = 0.25, GRID_INTERVAL_Y = 0.2,
      ASPECT_RATIO_Y_FOR_MAP = 0.7667;
  public double PROJ_SCALE;
  private float scaling;
  
  public Tiledboard tboard = 
      new Tiledboard(GRID_INTERVAL_X * GRID_SIZE_X, 
          GRID_INTERVAL_Y * GRID_SIZE_Y * ASPECT_RATIO_Y_FOR_MAP, 4);

  private int nochangeframe = 0;
  Load2Dfloat data;
      //new Load2Dfloat("test2.txt");
  ByteBufferFloat2Wrapper2D windspeed;
  RealFunction2DWrapper func, func0to1;
  
  Load2Dfloat windspeedu;
      
  Load2Dfloat windspeedv;
  
  FloatBuffer datafb;
  float windspeedmax;
  SceneJCheckBox textureshadow;
  JSlider possioninterval, viewdscale;
  private int currentinterval = 0, currentscale = 0;
  private Vector2DDouble viewcenter = new Vector2DDouble(0, 0),
      prevviewcenter = new Vector2DDouble(0, 0);
  private float cylinderscale = 0;
  private float lightdirectionrotate = 45;
  
  List<Vector2DDouble> possion2;
  
  public Scene1() {
    super();
  }
  
  public void setPROJ_SCALE(double x){
    PROJ_SCALE = x / (GRID_INTERVAL_X * GRID_SIZE_X);
    scaling =  (float) ((double) (viewdscale.getValue()) * PROJ_SCALE);
  }
  
  //中心の移動 x,yはピクセル単位
  public void moveCenter(int x, int y, int viewportx, int viewporty){
    double pixeltoworldcoordx = 
        (double) x / (double) viewportx 
        * Math.abs(orthoproj[1] - orthoproj[0]) / scaling;
    double pixeltoworldcoordy = 
        (double) y / (double) viewporty 
        * Math.abs(orthoproj[3] - orthoproj[2]) / scaling;
    //System.out.println(scaling);
    //System.out.println(pixeltoworldcoordx);
    viewcenter.x += pixeltoworldcoordx;
    viewcenter.y -= pixeltoworldcoordy;
    nochangeframe = 0;
  }
  
  public void initfile(String filepath){
    windspeedu = 
        new Load2Dfloat(filepath+"UofWind_10.0m_T0.txt");
    windspeedv = 
        new Load2Dfloat(filepath+"VofWind_10.0m_T0.txt");
    windspeedu.load();
    windspeedv.load();
    windspeedmax = windspeedmax();
    System.out.println("Max value : " + windspeedmax);
    data = 
        new Load2Dfloat(filepath + "RelativeHumidity_2.0m_T0.txt");
    data.load();
    datafb = data.getbuffer().asFloatBuffer();
    windspeed = 
        new ByteBufferFloat2Wrapper2D(tboard.leftdownx, tboard.leftdowny, 
            tboard.leftdownx + tboard.width, tboard.leftdowny + tboard.height,
            windspeedu.width, windspeedu.height, 
            windspeedu.getbuffer(), windspeedv.getbuffer());
    func = 
        new RealFunction2DWrapper(windspeed, 0, windspeed.max(), 0.2, 1);
    func0to1 = 
        new RealFunction2DWrapper(windspeed, 0, windspeed.max(), 0, 1);
  }
  
  @Override
  public void init(GL2GL3 gl, Shader shader, Shader shadertess){
    // TODO Auto-generated method stub
    super.init(gl, shader, shadertess);
    box.init(gl);
    board.init(gl);
    sphere.init(gl);
    hex.init(gl);
    tboard.init(gl);
    plus.init(gl);
    tube.init(gl);
    ring.init(gl);
    textureshadow = new SceneJCheckBox("texture shadow", false);
    Ctrlpanel.getInstance().addscenecheckbox(textureshadow);
    possioninterval = new JSlider(10, 60, 40);
    Ctrlpanel.getInstance().addSlider(possioninterval, "interval");
    viewdscale = new JSlider(1, 20, 1);
    Ctrlpanel.getInstance().addSlider(viewdscale, "Scale");
    possioninterval(intervaltopossion(currentinterval));
  }
  
  
  public void possioninterval(double interval) {
    //System.out.println("possion start");
    //intervalはsliderの0.01倍
    PoissonDiskSampler pds = 
        new PoissonDiskSampler(orthoproj[0] / scaling - viewcenter.x,
            orthoproj[2] / scaling + viewcenter.y, 
            orthoproj[1] / scaling - viewcenter.x, 
            orthoproj[3] / scaling  + viewcenter.y, 
            interval / scaling, func);

    possion2 = pds.sample();
    //System.out.println("possion end");
  }
  
  private float cylinderscale() {
    double tan = lights.get(0).posz 
        / (Math.sqrt(Math.pow(lights.get(0).posx, 2) 
        + Math.pow(lights.get(0).posy, 2)));
        
    return (float)(0.9*tan * intervaltopossion(currentinterval) / (windspeedmax));
  }
  
  public void setlightdirectionrotate(double rad){
    lightdirectionrotate = (float)(rad / (2 * Math.PI) * 360);
  }
  
  public void setlightpos(int lightidx, float x, float y, float z){
    Light light = lights.get(lightidx);
    light.posx = x; light.posy = y; light.posz = z;
    light.update();
  }
  
  public void setlightpos(int lightidx, float x, float y){
    Light light = lights.get(lightidx);
    light.posx = x; light.posy = y;
    light.update();
  }
  
  public void setlightpospolar(int lightidx, double r, double rad){
    Light light = lights.get(lightidx);
    light.posx = (float) (r * Math.cos(rad)); 
    light.posy = (float) (r * Math.sin(rad));
    light.update();
  }
  
  private float windspeedmax(){
    float max = 0;
    for(int i = 0; i < windspeedu.height * windspeedu.width-1; i++){
      float tmp = (float)Math.sqrt(Math.pow(windspeedu.getfloat(i),2)
          +Math.pow(windspeedv.getfloat(i),2));
      if(tmp > max)max = tmp;
    }
    return max;
  }
  
  private float pointtovalue(Vector2DDouble point, 
      Load2Dfloat data1, Load2Dfloat data2,
      double left, double right, double up, double down){
    int leftupindex;
    int datawidth = data1.width, dataheight = data1.height;
    double width = right - left, height = up - down;
    double leftdownx = (point.x - left) / width * datawidth;
    double leftdowny = (point.y - down) / height * dataheight;
    leftupindex = (int) leftdownx + (int) leftdowny * datawidth;
    double interx = leftdownx - (int) leftdownx;
    double intery = leftdowny - (int) leftdowny;
    double value1 = intery * (interx * data1.getfloat(leftupindex)
        + (1 - interx) * data1.getfloat(leftupindex + 1))
        + (1 - intery) * (interx * data1.getfloat(leftupindex + datawidth)
        + (1 - interx) * data1.getfloat(leftupindex + datawidth + 1));
    double value2 = intery * (interx * data2.getfloat(leftupindex)
        + (1 - interx) * data2.getfloat(leftupindex + 1))
        + (1 - intery) * (interx * data2.getfloat(leftupindex + datawidth)
        + (1 - interx) * data2.getfloat(leftupindex + datawidth + 1));
    return (float) Math.sqrt(value1 * value1 + value2 * value2);
  }
  
  private double intervaltopossion(int value) {
    return (double) value / 100.0;
  }
  
  @Override 
  public void scene(GL2GL3 gl, Shader shader) {
    scene(gl, shader, true);
  }
  
  public void scene(GL2GL3 gl, Shader shader, boolean show) {
    model.glLoadIdentity();
    for(int i=0; i < possion2.size(); i++){
      model.glPushMatrix();
      Vector2DDouble pos = possion2.get(i);
      float value = (float)func0to1.getDouble(pos.x, pos.y);
      //System.out.println(value);
      //System.out.println(pos.x + " " + pos.y);
      //拡大
      model.glScalef(scaling, scaling, 1);
      //移動
      //y成分に一時的にマイナスをつけてごまかす
      model.glTranslatef((float) (pos.x), -(float) (pos.y), 0);
      model.glTranslatef((float) viewcenter.x, (float) viewcenter.y, 0);
      cylinderscale = cylinderscale();
      //System.out.println(value);
      model.glScalef(0.3f * cylinderscale * value * 7, 
          0.3f * cylinderscale * value * 7, 
          cylinderscale * value * 0.9f * 70);
      //縮小
      model.glScalef(1f/scaling, 1f/scaling, 1);
      model.glRotatef(lightdirectionrotate - 90, 0, 0, 1);
      updateModelMatrix(gl, shader, model);
      tube.rendering(gl);
//      rping.rendering(gl);
      model.glPopMatrix();
    }
    
    //湿度
//    if(show & textureshadow.state){
//      model.glLoadIdentity();
//      model.glTranslatef(0, 0, 40);
//      //model.glPushMatrix();
//      for(float j = 0; j < 145; j+=2){
//        model.glPushMatrix();
//        model.glTranslatef(0, -(-height/2 + height*(j/145)), 0);
//        for(float i = 0; i < 201; i+=2){
//          model.glPushMatrix();
//          model.glTranslatef(-width/2 + width*(i/201), 0, 0);
//          float tmp = datafb.get((int)(j*201+i))/100;
//          //tmp *= 1.85;
//          //tmp = 1;
//          //System.out.println(tmp);
//          //model.glRotatef(45, 0, 0, 1);
//          //model.glScalef(width/201 * tmp, width/201*tmp, 1f);
//          
//          model.glScalef(width/201, width/201, 1f);
//          model.glPushMatrix();
//          //model.glRotatef(90f, 0, 0, 1);
//          model.glScalef(Math.min(tmp*2f+0.2f,1.1f), 
//              Math.max(0.2f,0.2f+2f*(tmp-0.5f)), 
//              1f);
//          updateModelMatrix(gl, shader, model);
//          sphere.rendering(gl);
//          model.glPopMatrix();
//          
//          //plus minus
////          model.glScalef(2*width/201, 2*width/201, 1f); 
////          model.glPushMatrix();
////          model.glScalef(2 * tmp, 0.1f, 1f);
////          updateModelMatrix(gl, shader, model);
////          box.rendering(gl);
////          model.glPopMatrix();
////          
////          model.glPushMatrix();
////          model.glScalef(0.1f, Math.max(0,2*1f*(tmp-0.5f)), 1f);
////          updateModelMatrix(gl, shader, model);
////          box.rendering(gl);
////          model.glPopMatrix();
//          //end
//          
//          model.glPopMatrix();
//        }
//        model.glPopMatrix();
//      }
//    }
    
    //風速
//    if(show & false){
//      model.glLoadIdentity();
//      model.glTranslatef(0, 0, 40);
//      //model.glPushMatrix();
//      for(float j = 0; j < 145; j+=2){
//        model.glPushMatrix();
//        model.glTranslatef(0, -(-height/2 + height*(j/145)), 0);
//        for(float i = 0; i < 201; i+=2){
//          model.glPushMatrix();
//          model.glTranslatef(-width/2 + width*(i/201), 0, 0);
//          float tmp = (float)Math.sqrt(Math.pow(windspeedu.getfloat((int)(j*201+i)),2)
//              +Math.pow(windspeedv.getfloat((int)(j*201+i)),2))/windspeedmax;
//          tmp *= 1.85;
//          //tmp = 1;
//          //System.out.println(tmp);
//          model.glScalef(width/201 * tmp, height/145*tmp, 0.02f);
//          updateModelMatrix(gl, shader, model);
//          hex.rendering(gl);
//          model.glPopMatrix();
//        }
//        model.glPopMatrix();
//      }
//    }

    if(show){
      model.glLoadIdentity();
      model.glPushMatrix();
      float scale = 10;
      //model.glScalef(scale, 0.1f, scale);
      updateModelMatrix(gl, shader, model);
      //box.rendering(gl);
      //sphere.rendering(gl);
      //hex.rendering(gl);
      model.glPopMatrix();
      model.glTranslatef(0, 0, 1f);
      //model.glRotatef(i, 1, 0, 0);
      updateModelMatrix(gl, shader, model);
      gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "colorset"),1);
      //box.rendering(gl);
      lights.get(0).move(gl, model);
      updateModelMatrix(gl, shader, model);
      //光源位置を表示
      //lights.get(0).rendering(gl);
    }       
    gl.glFlush();
  }
  
  public void scenetess(GL3 gl, Shader shader){
    scenetess(gl, shader, true);
  }
  @Override
  public void scenetess(GL3 gl, Shader shader, boolean show){
    // TODO Auto-generated method stub
    model.glLoadIdentity();
    model.glRotatef(-0, 1, 0, 0);
    model.glScalef(scaling, scaling, 1);
    model.glTranslatef((float) viewcenter.x, (float) viewcenter.y, 0);
    updateModelMatrix(gl, shader, model);
    //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
    //if(!show)
      //gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "offheight"), 0);
    tboard.rendering(gl);
    //ウラ面
//    model.glTranslatef(0, 0, 0.01f);
//    model.glRotatef(180, 1, 0, 0);
//    updateModelMatrix(gl, shader, model);
//    tboard.rendering(gl);
    gl.glFlush();
  }

  @Override
  public void iterate(){
    nochangeframe++;
    if(currentscale != viewdscale.getValue()){
      scaling = (float) ((double) (viewdscale.getValue()) * PROJ_SCALE);
      currentscale = viewdscale.getValue();
      nochangeframe = 0;
    }
    
    if(currentinterval != possioninterval.getValue()){
      currentinterval = possioninterval.getValue();
      possioninterval(intervaltopossion(currentinterval));
    }
    if(nochangeframe == 10){
      possioninterval(intervaltopossion(currentinterval));
    }
//    for(int j = 0; j < this.lightCount(); j++){
//      double angle = 2 * Math.PI * j / (this.lightCount()) + i/60;
//      //scene.lights.get(i).lookat(3, 3, i + 1, 0, 0, 0, 0, 1, 0);
//      this.lights.get(j).lookatd(
//          (3*Math.sin(angle)), 
//          (3*Math.cos(angle)), 
//          (5*Math.cos(angle / 3) + 10)*0 +4, 
//          0, 0, 0, 0, 1, 0);
//      //this.lights.get(j).perspectivef(90f, 1, 0.1f, 50f);
//      this.lights.get(j).orthof(-3, 3, -3, 3, 0.1f, 30);
//    }
//    setLightCircleLookOutside(2 * Math.sin(i/100), Math.cos(i/100), 
//        (2*Math.cos(i / 60) + 3),
//        Math.toRadians(0), i, 0.01);
//    setLightCircleLookOutside(0,0,2,0,i,1);
  }
  
  public void setLightCircleLookInside(double i, double z){
    for(int j = 0; j < this.lightCount(); j++){
      double angle = 2 * Math.PI * j / (this.lightCount()) + i;
      this.lights.get(j).lookatd(
          (3*Math.sin(angle)), 
          (3*Math.cos(angle)), 
          z, 
          0, 0, 0, 0, 1, 0);
      this.lights.get(j).orthof(-4, 4, -4, 4, 0.1f, 30);
    }
  }
  
  public void setLightCircleLookOutside(
      double eyeX, double eyeY, double eyeZ, double theta,
      double iterater, double speed, int uselightcount){
    float fovy = 120;
    if(uselightcount > 1){
      fovy = 360 / (uselightcount-1);
    }
    for(int i = 0; i < uselightcount-1; i++){
      double angle = 2 * Math.PI * i / (uselightcount-1) + iterater*speed;
      this.lights.get(i).lookatd(eyeX, eyeY, eyeZ, 
          eyeX + Math.sin(angle), eyeY + Math.cos(angle), eyeZ + Math.sin(Math.toRadians(theta)), 
          0, 1, 0);
      this.lights.get(i).perspectivef(fovy, 1, 0.01f, 30f);
    } 
    this.lights.get(uselightcount-1).lookatd(eyeX, eyeY, eyeZ, 
        eyeX, eyeY, eyeZ-1, 
        0, 1, 0);
    this.lights.get(uselightcount-1).perspectivef(120, 1, 0.1f, 30);
    virtualLightcount = this.lightCount()-uselightcount + 1;
//    this.lights.get(4).lookatd(
//      0, 
//    -3, 
//    3, 
//      0, 0, 0, 0, 1, 0);
//  this.lights.get(4).orthof(-3, 3, -3, 3, 0.1f, 30);
  }
  
  public void setLightCircleLookOutside(
      double eyeX, double eyeY, double eyeZ, double theta,
      double iterater, double speed){
    setLightCircleLookOutside(eyeX, eyeY, eyeZ, 
        theta, iterater, speed, this.lightCount());
  }



}