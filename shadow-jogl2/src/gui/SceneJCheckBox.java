package gui;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;

public class SceneJCheckBox extends JCheckBox{

  /**
   * 
   */
  private static final long serialVersionUID= -5356804796798943343L;
  
  public boolean state;
  
  public SceneJCheckBox(String text, boolean selected){
    super(text, selected);
    state = selected;
  }
  public SceneJCheckBox(){}
  
  public void select(ItemEvent e){
    if(e.getStateChange() == ItemEvent.SELECTED){
      state = true;
    }else{
      state = false;
    }
  }

}
