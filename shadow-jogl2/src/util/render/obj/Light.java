package util.render.obj;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import render.Vec3;
import render.Vec3d;

import util.loader.PlyLoader;

import com.jogamp.opengl.util.PMVMatrix;

public class Light extends Obj {
  public PMVMatrix pmmat;
  public double centerx,centery,centerz, upx, upy, upz;
  static PlyLoader obj = new PlyLoader("src/util/loader/ObjData/box.ply");
  Vec3d color;
  public double intensity;
  public int[] attribute = new int[4]; 
  
  public Light(){
    pmmat = new PMVMatrix();
    color = new Vec3d(1,1,1);
    intensity = 1;
  }
  
  public Light(Vec3d color, double intensity, int[] attr){
    pmmat = new PMVMatrix();
    this.color = color;
    this.intensity = intensity;
    this.attribute = attr;
  }
  
  public void init(GL2GL3 gl){
    obj.init(gl);
  }
  
  public void rendering(GL2GL3 gl){
    obj.rendering(gl);
  }
  
  public void setcolor(Vec3d color){
    this.color = color;
  }
  
  public void setintensity(double intensity){
    this.intensity = intensity;
  }
  
  public void setattr(int[] attr){
    for(int i = 0;i < 4; i++){
      this.attribute[i] = attr[i];
    }
  }
  
  public void setattr(int index, int attr){
    attribute[index] = attr;
  }
  
  public FloatBuffer color(){
    float[] color = {(float)this.color.x, 
        (float)this.color.y, (float)this.color.z};
    return FloatBuffer.wrap(color);
  }
  
  public IntBuffer attr(){
    return IntBuffer.wrap(attribute);
  }
  
  public void move(GL2GL3 gl, PMVMatrix model){
    model.glTranslatef(posx, posy, 6);
    model.glRotatef(45, 0, 0, 1);
    model.glScalef(0.1f, 0.1f, 0.1f);
  }
  
  public void lookat(float eyeX, float eyeY, float eyeZ, 
      float centerX, float centerY, float centerZ,
      float upX, float upY, float upZ){
    posx = eyeX; posy = eyeY; posz = eyeZ;
    centerx = centerX; centery = centerY; centerz = centerZ;
    upx = upX; upy = upY; upz = upZ;
    Vec3 f = new Vec3(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
    f.Nor();
    Vec3 U = new Vec3(upX, upY, upZ);
    U.Nor();
    Vec3 s = f.Cro(U);
    s.Nor();
    Vec3 u = s.Cro(f);
    u.Nor();

    float[] mat = {
        s.x, u.x, -f.x, 0,
        s.y, u.y, -f.y, 0,
        s.z, u.z, -f.z, 0,
        -eyeX * s.x + -eyeY * s.y + -eyeZ * s.z,
        -eyeX * u.x + -eyeY * u.y + -eyeZ * u.z,
        -eyeX * -f.x + -eyeY * -f.y + -eyeZ * -f.z, 1};
    pmmat.glMatrixMode(GL2.GL_MODELVIEW);
    pmmat.glLoadMatrixf(mat, 0);
  }
  public void lookatd(double eyeX, double eyeY, double eyeZ, 
      double centerX, double centerY, double centerZ,
      double upX, double upY, double upZ){
    posx = (float)eyeX; posy = (float)eyeY; posz = (float)eyeZ;
    centerx = centerX; centery = centerY; centerz = centerZ;
    upx = upX; upy = upY; upz = upZ;
    Vec3d f = new Vec3d(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
    f.Nor();
    Vec3d U = new Vec3d(upX, upY, upZ);
    U.Nor();
    Vec3d s = f.Cro(U);
    s.Nor();
    Vec3d u = s.Cro(f);
    u.Nor();

    float[] mat = {
        (float)s.x, (float)u.x, (float)-f.x, 0,
        (float)s.y, (float)u.y, (float)-f.y, 0,
        (float)s.z, (float)u.z, (float)-f.z, 0,
        (float)(-eyeX * s.x + -eyeY * s.y + -eyeZ * s.z),
        (float)(-eyeX * u.x + -eyeY * u.y + -eyeZ * u.z),
        (float)(-eyeX * -f.x + -eyeY * -f.y + -eyeZ * -f.z), 1};
    pmmat.glMatrixMode(GL2.GL_MODELVIEW);
    pmmat.glLoadMatrixf(mat, 0);
  }  
  
  public void perspectivef(float fovy,
      float aspect,
      float zNear,
      float zFar){
    pmmat.glMatrixMode(GL2.GL_PROJECTION);
    pmmat.glLoadIdentity();
    pmmat.gluPerspective(fovy, aspect, zNear, zFar);
  }
  
  public void orthof(float left,
      float right,
      float bottom,
      float top,
      float zNear,
      float zFar){
    pmmat.glMatrixMode(GL2.GL_PROJECTION);
    pmmat.glLoadIdentity();
    pmmat.glOrthof(left, right, bottom, top, zNear, zFar);
    pmmat.update();
  }
  
  public FloatBuffer getMatrixf(){
    pmmat.update();
    return pmmat.glGetPMvMatrixf();
  }
  
  public FloatBuffer getMatrixf(int matrixName){
    pmmat.update();
    return pmmat.glGetMatrixf(matrixName);
  }
  
  public void update(){
    lookatd(posx, posy, posz, centerx, centery, centerz, upx, upy, upz);
  }

}
