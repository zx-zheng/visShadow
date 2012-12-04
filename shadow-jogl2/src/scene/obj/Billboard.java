package scene.obj;

import gl.Semantic;
import gl.Shader;
import gl.TexBindSet;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import oekaki.util.Tex2D;
import oekaki.util.TexImage;
import oekaki.util.TexImageUtil;
import oekaki.util.TextureBase;
import scene.templateScene.Filter;
import za.co.luma.geom.Vector2DDouble;

public class Billboard extends Obj implements Semantic{
  
  public static final String TEX_UNIFORM_NAME = "billBoardTex";
  static final String UNIFORM_ALPHA = "alpha";
  private float alpha = 1;
  protected int[] vao = new int[1];
  private int[] arraybuffer = new int[1];
  TexBindSet tbs;

  public Billboard(TextureBase tex){
    
  }
  
  public Billboard(TexBindSet tbs){
    
  }
  
  public Billboard(GL2GL3 gl, String img, 
      Vector2DDouble center, double width){
    initTex(gl, img);
    initFrame(gl, center, width, width * tbs.tex.height / tbs.tex.width);
  }
  
  public Billboard(GL2GL3 gl, String img, 
      Vector2DDouble center, double width, Shader shader){
    initTex(gl, img);
    initFrame(gl, center, width, width * tbs.tex.height / tbs.tex.width);
    shader.init(gl);
    setShader(shader);
  }
  
  public Billboard(GL2GL3 gl, String img){
    this(gl, img, new Vector2DDouble(0, 0), 1.0);
  }
  
  public Billboard(GL2GL3 gl, String img, float width){
    this(gl, img, new Vector2DDouble(0, 0), width);
  }
  
  private void initTex(GL2GL3 gl, String img){
    TexImage image = TexImageUtil.loadImage(img, 4, 
        TexImage.TYPE_BYTE);
    
    Tex2D tex = new Tex2D(GL2.GL_RGBA, GL2.GL_BGRA,
        GL.GL_UNSIGNED_BYTE, image.width, image.height,
        GL.GL_LINEAR, image.buffer, "BillboardTex");
    tex.init(gl);
    
    tbs = new TexBindSet(tex);
    tbs.bind(gl);
  }
  
  private void initFrame(GL2GL3 gl, Vector2DDouble center, 
      double width, double height){
    gl.glGenBuffers(1, arraybuffer, 0);
    gl.glGenVertexArrays(1, vao, 0);
    float[] frame = {
        (float) (-width / 2 + center.x), (float) (height / 2 + center.y), 0,  
        0, 1f,
        (float) (width / 2 + center.x), (float) (height / 2 + center.y), 0,  
        1f, 1f,
        (float) (-width / 2 + center.x), (float) (-height / 2 + center.y), 0,  
        0, 0,
        (float) (width / 2 + center.x), (float) (-height / 2 + center.y), 0,  
        1f, 0
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
  
  public void setAlpha(float alpha){
    this.alpha = alpha;
  }
  
  public void rendering(GL2GL3 gl, Shader shader){
    gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), TEX_UNIFORM_NAME), 
        tbs.texunit);
    gl.glBindVertexArray(vao[0]);
    gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 4);
    gl.glBindVertexArray(0);
  }

  @Override
  public void rendering(GL2GL3 gl){
    if(shader != null){
      this.shader.use(gl);
    }
    gl.glUniform1i(gl.glGetUniformLocation(this.shader.getID(), TEX_UNIFORM_NAME), 
        tbs.texunit);
    gl.glBindVertexArray(vao[0]);
    gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 4);
    gl.glBindVertexArray(0);
  }
  
  public void renderingWithAlpha(GL2GL3 gl){
    gl.glUniform1f(gl.glGetUniformLocation(shader.getID(), UNIFORM_ALPHA), alpha);
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    this.rendering(gl);
    gl.glDisable(GL2.GL_BLEND);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
  public void renderingWithAlpha(GL2GL3 gl, Shader shader){
    gl.glUniform1f(gl.glGetUniformLocation(shader.getID(), UNIFORM_ALPHA), alpha);
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    this.rendering(gl, shader);
    gl.glDisable(GL2.GL_BLEND);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
}
