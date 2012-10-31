package util.render.obj;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import oekaki.util.Tex2D;
import oekaki.util.TexImage;
import oekaki.util.TexImageUtil;
import oekaki.util.TextureBase;
import util.gl.Semantic;
import util.gl.Shader;
import util.gl.TexBindSet;
import util.render.Filter;

public class Billboard implements Semantic{
  
  protected int[] vao = new int[1];
  private int[] arraybuffer = new int[1];
  TexBindSet tbs;

  public Billboard(TextureBase tex){
    
  }
  
  public Billboard(TexBindSet tbs){
    
  }
  
  public Billboard(GL2GL3 gl, String img){
    initFrame(gl);
    TexImage image = TexImageUtil.loadImage(img, 4, 
        TexImage.TYPE_BYTE);
    
    Tex2D shadow = new Tex2D(GL2.GL_RGBA, GL2.GL_BGRA,
        GL.GL_UNSIGNED_BYTE, image.width, image.height,
        GL.GL_LINEAR, image.buffer, "shadow");
    shadow.init(gl);
    
    tbs = new TexBindSet(shadow);
    tbs.bind(gl);
  }
  
  private void initFrame(GL2GL3 gl){
    gl.glGenBuffers(1, arraybuffer, 0);
    gl.glGenVertexArrays(1, vao, 0);
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
  
  public void rendering(GL2GL3 gl, Shader shader){
    gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "shadowTex"), tbs.texunit);
    gl.glBindVertexArray(vao[0]);
    gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 4);
    gl.glBindVertexArray(0);
  }
  
}
