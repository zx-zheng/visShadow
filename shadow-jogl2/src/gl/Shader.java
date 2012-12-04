package gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Stack;

import javax.media.opengl.*;

import com.jogamp.opengl.util.PMVMatrix;

public class Shader{
  
  static int currentShaderID;
  private boolean isInitialized = false;
  private boolean modelMatAdded = false, viewMatAdded = false;
  int shaderID;
  String SHADER_NAME;
  String vsource, fsource, csource, esource, gsource;
  ShaderProgram ShaderProgram;
  Stack<GLUniformData> uniformstack = new Stack<GLUniformData>();
  HashMap<String, GLUniformData> uniformdatamap = 
      new HashMap<String, GLUniformData>();
  public enum MatrixType {MODEL, VIEWPROJ};
  final public static String SHADER_MODEL_MATRIX_NAME = "model";
  final public static String SHADER_VIEW_PROJ_MATRIX_NAME = "viewproj";
  int uniformModelMatrix, uniformViewProjMatrix;

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
    SHADER_NAME = "NoName";
  }
  
  public Shader(String vsource, String csource, String esource,
      String gsource, String fsource, String name){
    this(vsource, csource, esource, gsource, fsource);
    SHADER_NAME = name;
  }

  public void init(GL2GL3 gl){
    if(isInitialized){
      return;
    }else{
      isInitialized = true;
    }
    ShaderProgram = new ShaderProgram(gl);
    if(vsource != null) ShaderProgram.make_vert(vsource);
    if(csource != null) ShaderProgram.make_ctrl(csource);
    if(esource != null) ShaderProgram.make_eval(esource);
    if(gsource != null) ShaderProgram.make_geom(gsource);
    if(fsource != null) ShaderProgram.make_frag(fsource);
    ShaderProgram.link_valid();
    shaderID = ShaderProgram.getid();
    initUniformLocation(gl);
  }
  
  private void initUniformLocation(GL2GL3 gl){
    this.use(gl);
    uniformModelMatrix = gl.glGetUniformLocation(shaderID, SHADER_MODEL_MATRIX_NAME);
    uniformViewProjMatrix = gl.glGetUniformLocation(shaderID, SHADER_VIEW_PROJ_MATRIX_NAME);
    if(uniformModelMatrix == -1){ 
      System.out.println("Shader " + SHADER_NAME + " ID:" + shaderID  
          + ": Shader Matrix model Uniform location error");
    }
    if(uniformViewProjMatrix == -1){
      System.out.println("Shader " + SHADER_NAME + " ID:" + shaderID  
          + ": Shader Matrix view Uniform location error");
    }
    
    Shader.unuse(gl);
  }
  
  public void updateMatrix(GL2GL3 gl, PMVMatrix mat, MatrixType type){
    if(currentShaderID != shaderID){
      System.out.println("Shader Uniform error " + SHADER_NAME + " : shader is not in use");
      return;
    }
    
    mat.update();
    
    switch (type) {
    case MODEL:
      gl.glUniformMatrix4fv(uniformModelMatrix, 1, false, 
          mat.glGetMvMatrixf());
      break;
    case VIEWPROJ:
      gl.glUniformMatrix4fv(uniformViewProjMatrix, 4, false, 
          mat.glGetPMvMvitMatrixf());
      break;
    }
  }
  
  public void setMatrix(GL2GL3 gl, PMVMatrix mat, MatrixType type){
    
    mat.update();
    
    switch (type) {
    case MODEL:
      if(!modelMatAdded){
        adduniform(gl, "model", 4, 4, mat.glGetMatrixf());
        modelMatAdded = true;
        return;
      } else {
        setuniform("model", mat.glGetMatrixf());
      }   
      break;
    case VIEWPROJ:
      if(!viewMatAdded){
        adduniform(gl, "view", 4, 4, mat.glGetMatrixf());
        viewMatAdded = true;
        return;
      } else {
        setuniform("view", mat.glGetMatrixf());
      }   
      break;
    }
  }

  public void use(GL2GL3 gl){
    if(Shader.currentShaderID != shaderID){
      currentShaderID = shaderID;
      gl.glUseProgram(shaderID);
      updateuniforms(gl);
    }
  }
  
  public static void unuse(GL2GL3 gl){
    gl.glUseProgram(0);
    currentShaderID = 0;
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
  
  public void adduniform(GL2GL3 gl, String name, int components, float[] data){
    GLUniformData newuniform = new GLUniformData(name, components, FloatBuffer.wrap(data));
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
  
  public void setuniform(String name, float[] val){
    GLUniformData data = uniformdatamap.get(name);
    data.setData(FloatBuffer.wrap(val));
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
