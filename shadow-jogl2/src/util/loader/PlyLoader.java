package util.loader;

import gl.Semantic;

import java.nio.*;

import javax.media.opengl.*;


public class PlyLoader extends Loader implements Semantic{

  String line;
  String[] linetmp;
  int totalvertex, totalproperty = 0, totalface, realtotalface;
  boolean inited;

  FloatBuffer vertex;
  IntBuffer index;
  int[] vboid = new int[2], vaoid = new int[1];

  public PlyLoader(String filepath){
    super(filepath);
    scanner[0].useDelimiter("\\n");
  }

  @Override
  public void loadOffsetLineValue(int lineoffset, float value){
    // TODO Auto-generated method stub

  }

  @Override
  public void readheader(){
    // TODO Auto-generated method stub
    while((line = scanner[0].next()) != null){
      if(line.indexOf("element vertex") == 0){
        break;
      }
    }
    //System.out.println(line);
    linetmp = line.substring("element vertex".length()).split(" ");
    //System.out.println(linetmp[1]);
    totalvertex = Integer.parseInt(linetmp[1]);


    //find property of vertex
    while((line = scanner[0].next()) != null){
      //System.out.println(line);
      if(line.indexOf("property") == 0){
        totalproperty++;
      }else{
        break;
      }
    }

    //find number of face
    while(line != null){
      if(line.indexOf("element face") == 0){
        break;
      }
      line = scanner[0].next();
    }
    //System.out.println(line);
    linetmp = line.substring("element face".length()).split(" ");
    totalface = Integer.parseInt(linetmp[1]);

    //go to end of header
    while((line = scanner[0].next()) != null){
      if(line.indexOf("end_header") == 0)
        break;
    }

//    System.out.println("vertex:" + totalvertex + 
//        "\nproperty:" + totalproperty + 
//        "\nface:" + totalface); 
//    System.out.println(line); 
  }

  private void readvertex(){
    String[] splited;
    vertex = FloatBuffer.allocate(totalvertex * totalproperty * 4);
    //System.out.println(vertex.capacity());

    for(int i = 0; i < totalvertex; i++){
      line = scanner[0].next();
      splited = line.split(" ");
      for(int j = 0; j < totalproperty; j++){
        //System.out.println(splited[j]);
        vertex.put(new Float(splited[j]));
      }
    }

    vertex.rewind();
  }

  public FloatBuffer getvertex(){
    //readvertex();
    return vertex;
  }

  private void readface(){
    String[] splited;
    //一つの面を構成する頂点が4以上だと別に処理する必要がある
    index = IntBuffer.allocate(totalface * 3 * 2); 
    int faceform;
    realtotalface = totalface;
    for(int i = 0; i < totalface; i++){
      line = scanner[0].next();
      splited = line.split(" ");
      faceform = new Integer(splited[0]);
      //transform to triangles
      for(int j = 2; j < faceform; j++){
        //ポリゴンの順番のためにj+1がさき
        index.put(new Integer(splited[1]));
        index.put(new Integer(splited[j + 1]));
        index.put(new Integer(splited[j]));
        realtotalface++;
      }
      realtotalface--;
    }

    index.rewind();
  }

  public IntBuffer getface(){
    //readface();
    return index;
  }

  public void init(GL2GL3 gl){
    if(!inited){
      //System.out.println("init Ply loader");
      readheader();
      readvertex();
      readface();

      gl.glGenBuffers(2, vboid, 0);
      gl.glGenVertexArrays(1, vaoid, 0);

      //build vbo
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboid[0]);
      gl.glBufferData(GL.GL_ARRAY_BUFFER, totalproperty * totalvertex * 4,
          vertex, GL.GL_STATIC_DRAW);
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);


      gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboid[1]);
      gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, realtotalface * 3 * 4,
          index, GL.GL_STATIC_DRAW);

      gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

      //build vao
      gl.glBindVertexArray(vaoid[0]);
      //vertex
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboid[0]);
      gl.glVertexAttribPointer(VERT_POS_INDX,
          VERT_POS_SIZE,
          GL.GL_FLOAT, false,
          totalproperty * 4, 0);
      gl.glEnableVertexAttribArray(VERT_POS_INDX);

      gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboid[1]);

      gl.glBindVertexArray(0);
      inited = true;
    }
  }

  public void rendering(GL2GL3 gl){
    gl.glBindVertexArray(vaoid[0]); 
    gl.glDrawElementsInstanced(GL4.GL_TRIANGLES, realtotalface * 3,
        GL2.GL_INT, null, 1);
    gl.glBindVertexArray(0);
  }

}
