package render;

import gl.Shader;

import javax.media.opengl.*;


public interface RenderingPass {
  
  public void scene(GL2GL3 gl, Shader shader);
  
}
