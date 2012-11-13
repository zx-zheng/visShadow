package gl;

import javax.media.opengl.*;
//import com.jogamp.opengl.util.*;
//import java.nio.*;

public class FilterShader extends Shader{

  //static private PMVMatrix filterPMV;
  //static private FloatBuffer filterPMVbuffer;
  
  int targetUniform;
  
  /*
  static
  {
    filterPMV = new PMVMatrix();
    filterPMV.glMatrixMode(GL2.GL_MODELVIEW);
    filterPMV.glLoadIdentity();
    filterPMV.glMatrixMode(GL2.GL_PROJECTION);
    filterPMV.glLoadIdentity();
    filterPMV.glOrthof(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
    filterPMV.update();
    filterPMVbuffer = filterPMV.glGetPMvMatrixf();
  }
*/
  public FilterShader(GL2GL3 gl,
      String vsource, 
      String fsource){
    this.vsource = vsource;
    this.fsource = fsource;
  }
  
  @Override
  public void init(GL2GL3 gl){
    ShaderProgram = new ShaderProgram(gl);
    ShaderProgram.make_vert(vsource);
    ShaderProgram.make_frag(fsource);
    ShaderProgram.link_valid();
    shaderID = ShaderProgram.getid();
    gl.glUseProgram(shaderID);
    //gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderID, "PMV")
    //   , 1, false, filterPMVbuffer);
    targetUniform = gl.glGetUniformLocation(shaderID, "target");
    gl.glUseProgram(0);
  }

  public void setTargetTexture(GL2GL3 gl, TexBindSet tbs){
    this.use(gl);
    setTargetTextureCore(gl, tbs);
    //this.unuse(gl);
  }
  
  public void setTargetTextureCore(GL2GL3 gl, TexBindSet tbs){
    gl.glUniform1i(targetUniform, tbs.texunit);
  }
  
}
