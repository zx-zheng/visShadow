package util.render;

import javax.media.opengl.*;

import util.gl.TexBindSet;

public class TexViewer extends Filter{
  
  int uniformtex2darray,
  uniformviewmode, uniformdepth, currentdep = -1, 
  uniformchannel, currentch = -1;

  public TexViewer(){
    super("src/util/render/TextureViewer/vert.c",
        "src/util/render/TextureViewer/frag.c");
  }
  
  public TexViewer(String fsource){
    super("src/util/render/TextureViewer/vert.c",
        fsource);
  }
  
  @Override
  public void init(GL2GL3 gl){
    System.out.println("Init Texture viewer");
    super.init(gl);
    filter.use(gl);
    uniformtex2darray = gl.glGetUniformLocation(filter.getID(), "targetarray");
    uniformviewmode = gl.glGetUniformLocation(filter.getID(), "mode");
    uniformdepth = gl.glGetUniformLocation(filter.getID(), "depth");
    uniformchannel = gl.glGetUniformLocation(filter.getID(), "channel");
    filter.unuse(gl);
    
  }
  
  public void rendering(GL2GL3 gl, TexBindSet tbs, 
      int offsetx, int offsety, int width, int height){
    rendering(gl, tbs, 
        0, 0, 
        offsetx, offsety, width, height);
  }
  
  public void rendering(GL2GL3 gl, TexBindSet tbs, 
      int channel,
      int offsetx, int offsety, int width, int height){
    rendering(gl, tbs, channel, offsetx, offsety, width, height);
  }

  public void rendering(GL2GL3 gl, TexBindSet tbs, 
      int channel, int depth,
      int offsetx, int offsety, int width, int height){
    filter.use(gl);
    if(targetunit != tbs){
      targetunit = tbs;
      setTargetTexture(gl, tbs, channel, depth);      
      System.out.println("Show Texture Unit " + tbs.texunit);
    }
    if(currentch != channel){
      currentch = channel;
      gl.glUniform1i(uniformchannel, channel);
      System.out.println("Show Texture Channel " + channel);
    }
    if(currentdep != depth){
      currentdep = depth;
      gl.glUniform1i(uniformdepth, depth);  
      System.out.println("Show Texture Depth " + depth);
    }
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glViewport(offsetx, offsety, width, height);
    gl.glBindVertexArray(vao[0]);
    gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 4);
    gl.glBindVertexArray(0);
    filter.unuse(gl);
  }
  
  @Override
  public void setTargetTexture(GL2GL3 gl, TexBindSet tbs){
    setTargetTexture(gl, tbs, 0, 0);
  }
  
  public void setTargetTexture(GL2GL3 gl, TexBindSet tbs,
        int channel){
    setTargetTexture(gl, tbs, channel, 0);
  }
  
  public void setTargetTexture(GL2GL3 gl, TexBindSet tbs,
      int channel, int depth){
    switch(tbs.tex.target){
    case GL2.GL_TEXTURE_2D:
      gl.glUniform1i(uniformviewmode, 0);
      super.setTargetTexture(gl, tbs);
      break;
    case GL2.GL_TEXTURE_2D_ARRAY:
      gl.glUniform1i(uniformviewmode, 1);
      gl.glUniform1i(uniformtex2darray, tbs.texunit);
      break;
    }
  }
  
  public void setTexChannel(GL2GL3 gl, int ch){
    if(ch != currentch){
      filter.use(gl);
      gl.glUniform1i(uniformchannel, ch);
      currentch = ch;
      //filter.unuse(gl);
    }
  }
  
}
