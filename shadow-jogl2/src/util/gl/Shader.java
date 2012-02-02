package util.gl;

import javax.media.opengl.*;

public class Shader{
  int shaderID;
  String vsource, fsource, csource, esource, gsource;
  ShaderProgram ShaderProgram;

  public Shader(){

  }

  public Shader(String vsource, String csource, String esource,
        String gsource, String fsource){
    this.vsource = vsource;
    this.csource = csource;
    this.esource = esource;
    this.gsource = gsource;
    this.fsource = fsource;
  }

  public void init(GL2GL3 gl){
    ShaderProgram = new ShaderProgram(gl);
    if(vsource != null) ShaderProgram.make_vert(vsource);
    if(csource != null) ShaderProgram.make_ctrl(csource);
    if(esource != null) ShaderProgram.make_eval(esource);
    if(gsource != null) ShaderProgram.make_geom(gsource);
    if(fsource != null) ShaderProgram.make_frag(fsource);
    ShaderProgram.link_valid();
    shaderID = ShaderProgram.getid();
  }

  public void use(GL2GL3 gl){
    gl.glUseProgram(shaderID);
  }
  
  public void unuse(GL2GL3 gl){
    gl.glUseProgram(0);
  }
  public void setInteger(GL2GL3 gl, String target, int num){
    gl.glUseProgram(shaderID);
    gl.glUniform1i(gl.glGetUniformLocation(shaderID, target),
       num);
    gl.glUseProgram(0);
  }

  public void setFloat(GL2GL3 gl, String target, float num){
    gl.glUseProgram(shaderID);
    gl.glUniform1f(gl.glGetUniformLocation(shaderID, target),
       num);
    gl.glUseProgram(0);
  }

  public int getID(){
    return shaderID;
  }

}
