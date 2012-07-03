package util.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Stack;

import javax.media.opengl.*;

public class Shader{
  int shaderID;
  String vsource, fsource, csource, esource, gsource;
  ShaderProgram ShaderProgram;
  Stack<GLUniformData> uniformstack = new Stack<GLUniformData>();
  HashMap<String, GLUniformData> uniformdatamap = 
      new HashMap<String, GLUniformData>();

  public Shader(){
   
  }

  public Shader(String vsource, String csource, String esource,
        String gsource, String fsource){
    this();
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
    updateuniforms(gl);
  }
  
  public void unuse(GL2GL3 gl){
    gl.glUseProgram(0);
  }
  
  public void adduniform(GL2GL3 gl, String name, int val){
    GLUniformData newuniform = new GLUniformData(name, val);
    newuniform.setLocation(gl.glGetUniformLocation(shaderID, name));
    uniformdatamap.put(name, newuniform);
    uniformstack.push(newuniform);
  }
  
  public void adduniform(GL2GL3 gl, String name, float val){
    GLUniformData newuniform = new GLUniformData(name, val);
    newuniform.setLocation(gl.glGetUniformLocation(shaderID, name));
    uniformdatamap.put(name, newuniform);
    uniformstack.push(newuniform);
  }
  
  public void adduniform(GL2GL3 gl, String name, int components, IntBuffer data){
    GLUniformData newuniform = new GLUniformData(name, components, data);
    newuniform.setLocation(gl.glGetUniformLocation(shaderID, name));
    uniformdatamap.put(name, newuniform);
    uniformstack.push(newuniform);
  }
  
  public void adduniform(GL2GL3 gl, String name, int components, FloatBuffer data){
    GLUniformData newuniform = new GLUniformData(name, components, data);
    newuniform.setLocation(gl.glGetUniformLocation(shaderID, name));
    uniformdatamap.put(name, newuniform);
    uniformstack.push(newuniform);
  }
  
  public void adduniform(GL2GL3 gl, String name, int rows, int columns, FloatBuffer data){
    GLUniformData newuniform = new GLUniformData(name, rows, columns, data);
    newuniform.setLocation(gl.glGetUniformLocation(shaderID, name));
    uniformdatamap.put(name, newuniform);
    uniformstack.push(newuniform);
  }
  
  public void setuniform(String name, int val){
    GLUniformData data = uniformdatamap.get(name);
    data.setData(val);
    uniformstack.push(data);
  }
  
  public void setuniform(String name, float val){
    GLUniformData data = uniformdatamap.get(name);
    data.setData(val);
    uniformstack.push(data);
  }
  
  public void setuniform(String name, IntBuffer val){
    GLUniformData data = uniformdatamap.get(name);
    data.setData(val);
    uniformstack.push(data);
  }
  
  public void setuniform(String name, FloatBuffer val){
    GLUniformData data = uniformdatamap.get(name);
    data.setData(val);
    uniformstack.push(data);
  }
  
  public void updateuniforms(GL2GL3 gl){
    while(!uniformstack.empty()){
      gl.glUniform(uniformstack.pop());
    }
  }
  
  public GLUniformData getuniform(String name){
    return uniformdatamap.get(name);
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
