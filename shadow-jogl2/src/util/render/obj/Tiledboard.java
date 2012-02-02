package util.render.obj;

import java.nio.FloatBuffer;

import javax.media.opengl.*;

import util.gl.Semantic;

public class Tiledboard extends Obj implements Semantic{

  int wdivide, hdivide;
  float vertices[];
  int vao[] = new int[1];
  
  public Tiledboard(float leftdownx, float leftdowny,  float z,
      float width, float height, int wdivide, int hdivide){
    this.wdivide = wdivide; this.hdivide = hdivide;
    genVertices(leftdownx, leftdowny, z, width, height, wdivide, hdivide);
  }
  
  public Tiledboard(float leftdownx, float leftdowny,  float z,
      float width, float height, int divide){
    this.wdivide = divide;
    genVertices(leftdownx, leftdowny, z, width, height, divide, divide);
  }
  
  public void init(GL2GL3 gl){
    initArrayBuffer(gl);
  }
  
  public int getdivide(){
    return wdivide;
  }
  
  public void rendering(GL3 gl){
    gl.glBindVertexArray(vao[0]);
    gl.glPatchParameteri(GL4.GL_PATCH_VERTICES, 4);
    gl.glDrawArrays(GL4.GL_PATCHES, 0, vertices.length/5);
    gl.glBindVertexArray(0);
  }
  
  private void initArrayBuffer(GL2GL3 gl){
    //Generate a buffer object
    int arraybuffer[] = new int[1];
    gl.glGenBuffers(1, arraybuffer, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, arraybuffer[0]);
    gl.glBufferData(GL.GL_ARRAY_BUFFER,vertices.length * 4,
        FloatBuffer.wrap(vertices), GL.GL_STATIC_DRAW);

    gl.glGenVertexArrays(1, vao, 0);
    gl.glBindVertexArray(vao[0]);
    //vertex
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, arraybuffer[0]);
    gl.glVertexAttribPointer(VERT_POS_INDX, 
           VERT_POS_SIZE, 
           GL.GL_FLOAT, false,
           4 * (VERT_POS_SIZE + VERT_AREA_SIZE), 0);
    
    //Area index
    gl.glVertexAttribPointer(VERT_AREA_INDX,
           VERT_AREA_SIZE,
           GL.GL_FLOAT, false,
           4 * (VERT_POS_SIZE + VERT_AREA_SIZE), 4 * VERT_POS_SIZE);

    gl.glEnableVertexAttribArray(VERT_POS_INDX);
    gl.glEnableVertexAttribArray(VERT_AREA_INDX);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }
  private void genVertices( 
      float leftdownx, float leftdowny,  float z,
      float width, float height, int wdivide, int hdivide){
    vertices = new float[5 * 4 * wdivide * hdivide];
    float winterval = width/wdivide;
    float hinterval = height/hdivide;
    for(int i = 0; i < wdivide; ++i){
      for(int j = 0; j < hdivide; ++j){
        //leftdown
        //position(x, y, z)
        vertices[0 + i * hdivide * 20 + j * 20] = leftdownx + i * winterval;
        vertices[1 + i * hdivide * 20 + j * 20] = leftdowny + j * hinterval;
        vertices[2 + i * hdivide * 20 + j * 20] = z;
        //index(x, y)
        vertices[3 + i * hdivide * 20 + j * 20] = i;
        vertices[4 + i * hdivide * 20 + j * 20] = j;
        //leftup
        vertices[0 + i * hdivide * 20 + j * 20 + 15] = leftdownx + i * winterval;
        vertices[1 + i * hdivide * 20 + j * 20 + 15] = leftdowny + j * hinterval + hinterval;
        vertices[2 + i * hdivide * 20 + j * 20 + 15] = z;
        vertices[3 + i * hdivide * 20 + j * 20 + 15] = i;
        vertices[4 + i * hdivide * 20 + j * 20 + 15] = j;
        //rightup
        vertices[0 + i * hdivide * 20 + j * 20 + 10] = leftdownx + i * winterval + winterval;
        vertices[1 + i * hdivide * 20 + j * 20 + 10] = leftdowny + j * hinterval + hinterval;
        vertices[2 + i * hdivide * 20 + j * 20 + 10] = z;
        vertices[3 + i * hdivide * 20 + j * 20 + 10] = i;
        vertices[4 + i * hdivide * 20 + j * 20 + 10] = j;
        //rightdown
        vertices[0 + i * hdivide * 20 + j * 20 + 5] = leftdownx + i * winterval + winterval;
        vertices[1 + i * hdivide * 20 + j * 20 + 5] = leftdowny + j * hinterval;
        vertices[2 + i * hdivide * 20 + j * 20 + 5] = z;
        vertices[3 + i * hdivide * 20 + j * 20 + 5] = i;
        vertices[4 + i * hdivide * 20 + j * 20 + 5] = j;
      }
    }
  }
}
