package util.render;

import javax.media.opengl.*;

import util.gl.Shader;

public interface RenderingPass {
  
  public void scene(GL2GL3 gl, Shader shader);
  
}
