package gui;

import gl.Shader;

import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;


public class UniformJSlider extends JSlider{

  /**
   * 
   */
  private static final long serialVersionUID= 4637402836240282568L;
  
  ArrayList<String> uniformlist = new ArrayList<String>();
  ArrayList<Shader> shaderlist = new ArrayList<Shader>();
  
  Function func;
  public JLabel label;
  boolean haveLabel = false;
  String labelText;
  
  public UniformJSlider(int min, int max, int value, String name, Shader shader){
    super(min, max, value);
    uniformlist.add(name);
    shaderlist.add(shader);
  }
  
  public UniformJSlider(){};
  
  public void addLabel(String text){
    label = new JLabel(text + Integer.toString(this.getValue()));
    labelText = text;
    haveLabel = true;
  }
  
  public void addShader(Shader shader){
    shaderlist.add(shader);
  }
  
  public void slide(ChangeEvent e){
      int val = this.getValue();
      if(haveLabel){
        label.setText(labelText + Integer.toString(this.getValue()));
      }
    for(Shader shader : shaderlist){
      for(String name : uniformlist){
        shader.setuniform(name, val);
      }
    }
  }

}
