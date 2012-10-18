package util.render;

import java.nio.FloatBuffer;
import javax.media.opengl.*;

import util.gl.FBO;
import util.gl.FilterShader;
import util.gl.Semantic;
import util.gl.Shader;
import util.gl.TexBindSet;

public class Filter implements RenderingPass, Semantic{
  
  protected int[] vao = new int[1];
  private int[] arraybuffer = new int[1];
  String vsource, fsource;
  int width = 0, height = 0, targetUniform;
  TexBindSet targetunit;
  FilterShader filter;
  
  public Filter(){}
  
  public Filter(String vsource, String fsource){
    this.vsource = vsource;
    this.fsource = fsource;
  }
  
  public void init(GL2GL3 gl){
    gl.glGenBuffers(1, arraybuffer, 0);
    gl.glGenVertexArrays(1, vao, 0);
    initFrame(gl);
    filter = new FilterShader(gl, vsource, fsource);
    filter.init(gl);
    targetUniform = gl.glGetUniformLocation(filter.getID(), "target");
  }

  @Override
  public void scene(GL2GL3 gl, Shader shader){
    // TODO Auto-generated method stub

  }
  
  public void setTargetTexture(GL2GL3 gl, TexBindSet tbs){
    filter.setTargetTexture(gl, tbs);
  }
  
  public void filtering(GL2GL3 gl, TexBindSet tbs, FBO outfbo){
    filter.use(gl);
    if(targetunit != tbs){
      targetunit = tbs;
      filter.setTargetTextureCore(gl, tbs);
    }
    if(outfbo != null){
      outfbo.bind(gl);
      gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    }else{
      gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    }
    gl.glViewport(0, 0, tbs.tex.width, tbs.tex.height);
    gl.glBindVertexArray(vao[0]);
    gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 6);
    gl.glBindVertexArray(0);
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    filter.unuse(gl);
  }
  
  private void initFrame(GL2GL3 gl){
    float[] frame = {
      -1f, 1f, 0,   0, 1f,
      1f, 1f, 0,    1f, 1f,
      -1f, -1f, 0,    0, 0,
      1f, -1f, 0,    1f, 0
      };

    
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, arraybuffer[0]);
    FloatBuffer frameBuffer = FloatBuffer.wrap(frame);
    gl.glBufferData(GL.GL_ARRAY_BUFFER,frame.length * 4,
        frameBuffer, GL.GL_STATIC_DRAW);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

    gl.glBindVertexArray(vao[0]);

    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, arraybuffer[0]);

    gl.glVertexAttribPointer(VERT_POS_INDX, 
           VERT_POS_SIZE, 
           GL.GL_FLOAT, false,
           4 * 5, 0);

    gl.glVertexAttribPointer(VERT_TEXCOORD0_INDX, 
           VERT_TEXCOORD_SIZE, 
           GL.GL_FLOAT, false,
           4 * 5, 4 * 3);
    gl.glEnableVertexAttribArray(VERT_POS_INDX);
    gl.glEnableVertexAttribArray(VERT_TEXCOORD0_INDX);

    gl.glBindVertexArray(0);
  }
  
  public void setSizeRect(GL2GL3 gl, int width, int height){
    this.width = width;
    this.height = height;
    float[] frame = {
      -1f, 1f, 0,   0, (float)width,
      1f, 1f, 0,    (float)width, (float)height,
      -1f, -1f, 0,    0, 0,
      1f, -1f, 0,    (float)height, 0
    };

    
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, arraybuffer[0]);
    FloatBuffer frameBuffer = FloatBuffer.wrap(frame);
    gl.glBufferData(GL.GL_ARRAY_BUFFER,frame.length * 4,
        frameBuffer, GL.GL_STATIC_DRAW);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

    gl.glBindVertexArray(vao[0]);

    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, arraybuffer[0]);

    gl.glVertexAttribPointer(VERT_POS_INDX, 
           VERT_POS_SIZE, 
           GL.GL_FLOAT, false,
           4 * 5, 0);

    gl.glVertexAttribPointer(VERT_TEXCOORD0_INDX, 
           VERT_TEXCOORD_SIZE, 
           GL.GL_FLOAT, false,
           4 * 5, 4 * 3);
    gl.glEnableVertexAttribArray(VERT_POS_INDX);
    gl.glEnableVertexAttribArray(VERT_TEXCOORD0_INDX);

    gl.glBindVertexArray(0);
  }

}
