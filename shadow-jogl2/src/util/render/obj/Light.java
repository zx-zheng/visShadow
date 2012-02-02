package util.render.obj;

import java.nio.FloatBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL4bc;

import util.loader.PlyLoader;
import util.render.Vec3;
import util.render.Vec3d;

import com.jogamp.opengl.util.PMVMatrix;

public class Light extends Obj {
  public PMVMatrix pmmat;
  static PlyLoader obj = new PlyLoader("src/util/loader/ObjData/box.ply");
  
  public Light(){
    pmmat = new PMVMatrix();
  }
  
  public void init(GL4bc gl){
    obj.init(gl);
  }
  
  public void rendering(GL4bc gl){
    
  }
  
  public void lookat(float eyeX, float eyeY, float eyeZ, 
      float centerX, float centerY, float centerZ,
      float upX, float upY, float upZ){
    posx = eyeX; posy = eyeY; posz = eyeZ;
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

}
