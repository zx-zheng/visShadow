package util.gl;

import java.util.*;
import javax.media.opengl.*;
import oekaki.util.*;

public class TexUnitManager{
  
  int[] max_tex_unit = new int[1];
  int MAX_TEX_UNIT;

  //List<TexBindSet> Texlist = new ArrayList<TexBindSet>();
  Stack<Integer> emptyTexUnit = new Stack<Integer>();
  
  //Singleton
  private static final TexUnitManager instance = new TexUnitManager();
  private TexUnitManager(){};
  public static TexUnitManager getInstance(){
    return TexUnitManager.instance;
  }
  
  public void init(GL2GL3 gl){
    gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_IMAGE_UNITS, max_tex_unit, 0);
    MAX_TEX_UNIT = max_tex_unit[0];
    System.out.println("Max Texture Unit is " + MAX_TEX_UNIT);
    int texture0 = GL2.GL_TEXTURE0;
    for(int i = 0; i < MAX_TEX_UNIT; i++){
      emptyTexUnit.push(texture0);
      texture0++;
    }
  }
  
  public void ActiveTexture(GL2GL3 gl, TexBindSet tbs){
    if(tbs.texunit != -1){
      gl.glActiveTexture(GL2.GL_TEXTURE0 + tbs.texunit);
    }
  }
  
  public TexBindSet bind(GL2GL3 gl, TextureBase tex){
    if(emptyTexUnit.empty()){
      System.out.println("Fail to bind texture. No empty texture unit.");
      return null;
    }
    int emptyunit = emptyTexUnit.pop();
    gl.glActiveTexture(emptyunit);
    tex.bind(gl);
    gl.glActiveTexture(GL2.GL_TEXTURE0);
    TexBindSet tbs = new TexBindSet(tex, emptyunit - GL.GL_TEXTURE0);
    System.out.println(tex.name + " is binded to TEXTURE" + tbs.texunit);
    return tbs;
  }
  
  public void bind(GL2GL3 gl, TexBindSet tbs){
    if(emptyTexUnit.empty()){
      System.out.println("Fail to bind texture. No empty texture unit.");
      return;
    }
    int emptyunit = emptyTexUnit.pop();
    gl.glActiveTexture(emptyunit);
    tbs.tex.bind(gl);
    gl.glActiveTexture(GL2.GL_TEXTURE0);
    tbs.texunit = emptyunit - GL.GL_TEXTURE0;
    System.out.println(tbs.tex.name + " is binded to TEXTURE" + tbs.texunit);
  }
  
  public void unbind(GL2GL3 gl, TexBindSet tbs){
    gl.glActiveTexture(tbs.texunit + GL.GL_TEXTURE0);
    tbs.tex.unbind(gl);
    emptyTexUnit.push(tbs.texunit + GL.GL_TEXTURE0);
    System.out.println("Texture " + tbs.tex.name + " is unbinded from TEXTURE" + tbs.texunit);
    tbs.texunit = -1;
  }
}
