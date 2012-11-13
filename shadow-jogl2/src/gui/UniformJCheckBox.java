package gui;

import gl.Shader;

import java.awt.event.ItemEvent;
import java.util.ArrayList;

import javax.swing.JCheckBox;


public class UniformJCheckBox extends JCheckBox{
  
  /**
   * 
   */
  private static final long serialVersionUID= 1L;
  
  ArrayList<String> uniformlist = new ArrayList<String>();
  ArrayList<Shader> shaderlist = new ArrayList<Shader>();
  Shader shader;
  
  public UniformJCheckBox(String text, boolean selected, Shader shader){
    super(text, selected);
    shaderlist.add(shader);
  }
  
  public UniformJCheckBox(String text, boolean selected, String name, Shader shader){
    super(text, selected);
    uniformlist.add(name);
    shaderlist.add(shader);
  }

  public UniformJCheckBox(){
    super();
  }
  
  public void addShader(Shader shader){
    shaderlist.add(shader);
  }
  
  public void select(ItemEvent e){
    int val;
    if(e.getStateChange() == ItemEvent.SELECTED){
      val = 1;
    }else{
      val = 0;
    }
    for(Shader shader : shaderlist){
      for(String name : uniformlist){
        shader.setuniform(name, val);
      }
    }
  }

}
