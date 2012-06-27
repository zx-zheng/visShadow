import java.nio.FloatBuffer;

import javax.media.opengl.*;

import util.gl.Shader;
import util.loader.Load2Dfloat;
import util.loader.PlyLoader;
import util.render.Scene;
import util.render.obj.Tiledboard;


public class Scene1 extends Scene {
  
  PlyLoader box = new PlyLoader("src/util/loader/ObjData/box.ply");
  PlyLoader board = new PlyLoader("src/util/loader/ObjData/board.ply");
  PlyLoader sphere = new PlyLoader("src/util/loader/ObjData/sphere3.ply");
  PlyLoader hex = new PlyLoader("src/util/loader/ObjData/square6.ply");
  PlyLoader plus = new PlyLoader("src/util/loader/ObjData/minus.ply");
  float width = 8,height = width * 145 /201;
  public Tiledboard tboard = 
      new Tiledboard(-width/2, -height/2, 0, width, height, 10);
  float i;
  String filepath = 
      "/home/michael/zheng/Programs/WeatherData/2009100812/";
  Load2Dfloat data = 
      new Load2Dfloat(filepath + "RelativeHumidity_2.0m_T0.txt");
      //new Load2Dfloat("test2.txt");
  
  Load2Dfloat windspeedu = 
      new Load2Dfloat(filepath+"UofWind_10.0m_T0.txt");
  Load2Dfloat windspeedv = 
      new Load2Dfloat(filepath+"VofWind_10.0m_T0.txt");
  FloatBuffer datafb;
  float windspeedmax;
  
  public Scene1(){
    super();
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
    data.load();
    plus.init(gl);
    windspeedu.load();
    windspeedv.load();
    windspeedmax = windspeedmax();
    datafb = data.getbuffer().asFloatBuffer();
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
  
  @Override 
  public void scene(GL2GL3 gl, Shader shader) {
    scene(gl, shader, true);
  }
  
  public void scene(GL2GL3 gl, Shader shader, boolean show) {
    //湿度
    if(show & true){
      model.glLoadIdentity();
      model.glTranslatef(0, 0, 40);
      //model.glPushMatrix();
      for(float j = 0; j < 145; j+=2){
        model.glPushMatrix();
        model.glTranslatef(0, -(-height/2 + height*(j/145)), 0);
        for(float i = 0; i < 201; i+=2){
          model.glPushMatrix();
          model.glTranslatef(-width/2 + width*(i/201), 0, 0);
          float tmp = datafb.get((int)(j*201+i))/100;
          //tmp *= 1.85;
          //tmp = 1;
          //System.out.println(tmp);
          //model.glRotatef(45, 0, 0, 1);
          //model.glScalef(width/201 * tmp, width/201*tmp, 1f);
          
          model.glScalef(width/201, width/201, 1f);
          model.glPushMatrix();
          //model.glRotatef(90f, 0, 0, 1);
          model.glScalef(Math.min(tmp*2f+0.2f,1.1f), 
              Math.max(0.2f,0.2f+2f*(tmp-0.5f)), 
              1f);
          updateModelMatrix(gl, shader, model);
          sphere.rendering(gl);
          model.glPopMatrix();
          
          //plus minus
//          model.glScalef(2*width/201, 2*width/201, 1f); 
//          model.glPushMatrix();
//          model.glScalef(2 * tmp, 0.1f, 1f);
//          updateModelMatrix(gl, shader, model);
//          box.rendering(gl);
//          model.glPopMatrix();
//          
//          model.glPushMatrix();
//          model.glScalef(0.1f, Math.max(0,2*1f*(tmp-0.5f)), 1f);
//          updateModelMatrix(gl, shader, model);
//          box.rendering(gl);
//          model.glPopMatrix();
          //end
          
          model.glPopMatrix();
        }
        model.glPopMatrix();
      }
    }
    
    //風速
    if(show & false){
      model.glLoadIdentity();
      model.glTranslatef(0, 0, 40);
      //model.glPushMatrix();
      for(float j = 0; j < 145; j+=2){
        model.glPushMatrix();
        model.glTranslatef(0, -(-height/2 + height*(j/145)), 0);
        for(float i = 0; i < 201; i+=2){
          model.glPushMatrix();
          model.glTranslatef(-width/2 + width*(i/201), 0, 0);
          float tmp = (float)Math.sqrt(Math.pow(windspeedu.getfloat((int)(j*201+i)),2)
              +Math.pow(windspeedv.getfloat((int)(j*201+i)),2))/windspeedmax;
          tmp *= 1.85;
          //tmp = 1;
          //System.out.println(tmp);
          model.glScalef(width/201 * tmp, height/145*tmp, 0.02f);
          updateModelMatrix(gl, shader, model);
          hex.rendering(gl);
          model.glPopMatrix();
        }
        model.glPopMatrix();
      }
    }

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
    // TODO Auto-generated method stub
    i++;
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
