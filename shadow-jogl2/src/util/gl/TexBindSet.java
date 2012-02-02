package util.gl;

import javax.media.opengl.GL2GL3;

import oekaki.util.*;

public class TexBindSet{
  public TextureBase tex;
  public int texunit;
  
  public TexBindSet(TextureBase tex, int texunit){
    this.tex = tex;
    this.texunit = texunit;
  }
  
  public TexBindSet(TextureBase tex){
    this.tex = tex;
    this.texunit = -1;
  }
  
  public void bind(GL2GL3 gl){
    if(texunit == -1){
      TexUnitManager.getInstance().bind(gl, this);
    }else{
      System.out.println(tex.name + " is already binded to TEXTURE" + this.texunit);
    }
  }
  
  public void unbind(GL2GL3 gl){
    if(texunit != -1){
      TexUnitManager.getInstance().unbind(gl, this);
    }else{
      System.out.println(tex.name + "is not binded");
    }
  }
  
  public void delete(GL2GL3 gl){
    unbind(gl);
    System.out.println(tex.name + "is deleted");
    tex.delete(gl);
    this.tex = null;
  }
    
}
