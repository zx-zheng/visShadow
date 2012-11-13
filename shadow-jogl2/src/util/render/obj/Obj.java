package util.render.obj;

import javax.media.opengl.GL2GL3;

import gl.Shader;

public abstract class Obj{
  public float posx, posy, posz;
  public int mode;
  Shader shader;
  
  public void setShader(Shader shader){
    this.shader = shader;
  }
  
  abstract void rendering(GL2GL3 gl);
}
