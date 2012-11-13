package gl;

import javax.media.opengl.*;
import java.io.*;

public class ShaderProgram{

  GL2GL3 gl;
  
  int shader_id, vrx_id, frg_id, tessctrl_id, tesseval_id, geom_id;
  String[] src = {"", ""}, src_temp = {""};
  int[] compile_status = {GL.GL_TRUE}, link_status = {GL.GL_TRUE},
    validate_status = {GL.GL_TRUE};
  int[] log_length = {0},src_length = {0};
  private int log_max = 5000;
  byte[] log = new byte[log_max];
  String line;

  ShaderProgram(GL2GL3 gl){
    this.gl = gl;
    shader_id = gl.glCreateProgram();
    if(shader_id == 0)
      System.out.println("erroe at creating shader program");
  }


  //ソースの読み込み、コンパイル、attach
  //vertex shader
  public int make_vert(String file){
    vrx_id = gl.glCreateShader(GL2GL3.GL_VERTEX_SHADER);
    readfile(file, 0);

    src_length[0] = src[0].length();
      src_temp[0] = src[0];
    gl.glShaderSource(vrx_id, 1, src_temp, src_length, 0);

    gl.glCompileShader(vrx_id);

    if(check_compile_error(vrx_id, " vertex") == 1){
      //System.exit(1);
      return 1;
    }

    gl.glAttachShader(shader_id, vrx_id); 
    gl.glDeleteShader(vrx_id);
    return 0;
  }

  //fragment shader
  public int make_frag(String file){
    frg_id = gl.glCreateShader(GL2GL3.GL_FRAGMENT_SHADER);
    readfile(file, 1);

    src_length[0] = src[1].length();
      src_temp[0] = src[1];
    gl.glShaderSource(frg_id, 1, src_temp, src_length, 0);

    gl.glCompileShader(frg_id);

    if(check_compile_error(frg_id, " fragment") == 1){
      //System.exit(1);
      return 1;
    }

    gl.glAttachShader(shader_id, frg_id); 
    gl.glDeleteShader(frg_id);
    return 0;
  }

  //tessellation control shader
  public int make_ctrl(String file){
    tessctrl_id = gl.glCreateShader(GL4.GL_TESS_CONTROL_SHADER);
    readfile(file, 1);

    src_length[0] = src[1].length();
      src_temp[0] = src[1];
    gl.glShaderSource(tessctrl_id, 1, src_temp, src_length, 0);

    gl.glCompileShader(tessctrl_id);

    if(check_compile_error(tessctrl_id, " Tessellation Control") == 1){
      //System.exit(1);
      return 1;
    }

    gl.glAttachShader(shader_id, tessctrl_id); 
    gl.glDeleteShader(tessctrl_id);
    return 0;
  }

  //tessellation evaluation shader
  public int make_eval(String file){
    tesseval_id = gl.glCreateShader(GL4.GL_TESS_EVALUATION_SHADER);
    readfile(file, 1);
    
    src_length[0] = src[1].length();
    src_temp[0] = src[1];
    gl.glShaderSource(tesseval_id, 1, src_temp, src_length, 0);
    
    gl.glCompileShader(tesseval_id);
    
    if(check_compile_error(tesseval_id, " Tessellation Evaluation") == 1){
      //System.exit(1);
      return 1;
    }
    
    gl.glAttachShader(shader_id, tesseval_id); 
    gl.glDeleteShader(tesseval_id);
    return 0;
  }
  
  //geometry shader
  public int make_geom(String file){
    geom_id = gl.glCreateShader(GL4.GL_GEOMETRY_SHADER);
    readfile(file, 1);
    
    src_length[0] = src[1].length();
    src_temp[0] = src[1];
    gl.glShaderSource(geom_id, 1, src_temp, src_length, 0);
    
    gl.glCompileShader(geom_id);
    
    if(check_compile_error(geom_id, " Geometry") == 1){
      //System.exit(1);
      return 1;
    }
    gl.glAttachShader(shader_id, geom_id); 
    gl.glDeleteShader(geom_id);
    return 0;
  }
  
  ////////////////////////////////////////////////////////////////////

  public int getid(){
    return shader_id;
  }

  public void link_valid(){
    gl.glLinkProgram(shader_id);
    gl.glGetProgramiv(shader_id, GL2GL3.GL_LINK_STATUS, link_status, 0);
    if(link_status[0] == GL.GL_FALSE){
      System.out.println("link error");
      gl.glGetProgramInfoLog(shader_id, log_max, log_length, 0, log, 0);
      show_log(log, log_length[0]);
    }
    gl.glValidateProgram(shader_id);
    gl.glGetProgramiv(shader_id, GL2GL3.GL_VALIDATE_STATUS, validate_status, 0);
    if(validate_status[0] == GL.GL_FALSE){
      System.out.println("link error");
      gl.glGetProgramInfoLog(shader_id, log_max, log_length, 0, log, 0);
      show_log(log, log_length[0]);
    }
  }
  
  private int check_compile_error(int id, String s){
    gl.glGetShaderiv(id, GL2GL3.GL_COMPILE_STATUS, compile_status, 0);
    if(compile_status[0] == GL.GL_FALSE){
      System.out.println("compile error in" + s  + "shader");
      gl.glGetShaderInfoLog(id, log_max, log_length, 0, log, 0);
      show_log(log, log_length[0]);
      return 1;
    }else{
      return 0;
    }
  }

  private void show_log(byte[] log, int length){
    for(int i = 0; i < length; i++){
      System.out.print((char)log[i]);
    }
  }

  private void readfile(String file, int v_or_f){
    BufferedReader brv = null;
    try{
      brv = new BufferedReader(new FileReader(file));
    } catch(FileNotFoundException e){
      System.out.println(file + " not found");
      e.printStackTrace();
    }
    try{
      src[v_or_f] = "";
      while ((line=brv.readLine()) != null){
        src[v_or_f] += line + "\n";
      }
    }catch(IOException e){
      System.out.println(e);
    }   
  }

}
