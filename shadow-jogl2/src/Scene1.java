import javax.media.opengl.*;

import util.gl.Shader;
import util.loader.PlyLoader;
import util.render.Scene;
import util.render.obj.Tiledboard;


public class Scene1 extends Scene {
  
  PlyLoader box = new PlyLoader("src/util/loader/ObjData/box.ply");
  PlyLoader board = new PlyLoader("src/util/loader/ObjData/board.ply");
  float width = 8,height = width * 145 /210;
  public Tiledboard tboard = 
      new Tiledboard(-width/2, -height/2, 0, width, height, 8);
  float i;
  
  public Scene1(){
    super();
  }
  
  @Override
  public void init(GL2GL3 gl, Shader shader, Shader shadertess){
    // TODO Auto-generated method stub
    super.init(gl, shader, shadertess);
    box.init(gl);
    board.init(gl);
    tboard.init(gl);
  }
  @Override
  
  public void scene(GL2GL3 gl, Shader shader) {
    scene(gl, shader, true);
  }
  
  public void scene(GL2GL3 gl, Shader shader, boolean show) {
    if(show){
      model.glLoadIdentity();
      model.glPushMatrix();
      float scale = 10;
      model.glScalef(scale, 0.1f, scale);
      updateModelMatrix(gl, shader, model);
      //box.rendering(gl);
      model.glPopMatrix();

      model.glTranslatef(0, 0, 1f);
      model.glRotatef(i, 1, 0, 0);
      model.glScalef(0.4f, 0.4f, 0.4f);
      updateModelMatrix(gl, shader, model);
      //box.rendering(gl);
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
    model.glRotatef(0, 1, 0, 0);
    updateModelMatrix(gl, shader, model);
    //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
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
//      double angle = 2 * Math.PI * j / (this.lightCount() - 1) + i/30;
//      //scene.lights.get(i).lookat(3, 3, i + 1, 0, 0, 0, 0, 1, 0);
//      this.lights.get(j).lookatd(
//          (3*Math.sin(angle)), 
//          (3*Math.cos(angle)), 
//          (5*Math.cos(angle / 3) + 10), 
//          0, 0, 0, 0, 1, 0);
//      //this.lights.get(j).perspectivef(120f, 1, 0.1f, 50f);
//    }
//    setLightCircleLookOutside(2 * Math.sin(i/100), Math.cos(i/100), 
//        (2*Math.cos(i / 60) + 3),
//        Math.toRadians(0), i, 0.01);
//    setLightCircleLookOutside(0,0,2,0,i,1);
  }
  
  public void setLightCircleLookOutside(
      double eyeX, double eyeY, double eyeZ, double theta,
      double iterater, double speed, int uselightcount){
    float fovy = 360 / (uselightcount-1);
    for(int i = 0; i < uselightcount-1; i++){
      double angle = 2 * Math.PI * i / (uselightcount-1) + iterater*speed;
      this.lights.get(i).lookatd(eyeX, eyeY, eyeZ, 
          eyeX + Math.sin(angle), eyeY + Math.cos(angle), eyeZ + Math.sin(Math.toRadians(theta)), 
          0, 1, 0);
      this.lights.get(i).perspectivef(fovy, 1, 0.1f, 30f);
    } 
    this.lights.get(uselightcount-1).lookatd(eyeX, eyeY, eyeZ, 
        eyeX, eyeY, eyeZ-1, 
        0, 1, 0);
    this.lights.get(uselightcount-1).perspectivef(120, 1, 0.1f, 30);
    virtualLightcount = this.lightCount()-uselightcount + 1;
  }
  
  public void setLightCircleLookOutside(
      double eyeX, double eyeY, double eyeZ, double theta,
      double iterater, double speed){
//    float fovy = 360 / (this.lightCount()- 1);
//    for(int i = 0; i < this.lightCount()-1; i++){
//      double angle = 2 * Math.PI * i / (this.lightCount()-1) + iterater*speed;
//      this.lights.get(i).lookatd(eyeX, eyeY, eyeZ, 
//          eyeX + Math.sin(angle), eyeY + Math.cos(angle), eyeZ + Math.sin(theta), 
//          0, 1, 0);
//      this.lights.get(i).perspectivef(fovy, 1, 0.1f, 30f);
//    } 
//    this.lights.get(lightCount()-1).lookatd(eyeX, eyeY, eyeZ, 
//        eyeX, eyeY, eyeZ-1, 
//        0, 1, 0);
//    this.lights.get(lightCount()-1).perspectivef(120, 1, 0.1f, 30);
//    virtualLightcount = 1;
    setLightCircleLookOutside(eyeX, eyeY, eyeZ, 
        theta, iterater, speed, this.lightCount());
  }



}
