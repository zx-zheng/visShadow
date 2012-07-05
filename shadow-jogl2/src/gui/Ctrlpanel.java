package gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.gl.Shader;
import util.gl.TexUnitManager;
import util.render.Scene;


public class Ctrlpanel implements ChangeListener, ItemListener{
  
  private static Ctrlpanel instance = new Ctrlpanel();
  public static Ctrlpanel getInstance(){
    return Ctrlpanel.instance;
  }
  JPanel panel = new JPanel();
  //ArrayList<JComponent> list = new ArrayList<JComponent>();
  public Ctrlpanel(){
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
  }
  
  public JPanel getPanel(){
    return panel;
  }
  
  public void adduniformcheckbox(String text, boolean selected,
      String name, Shader shader){
    UniformJCheckBox cbox = new UniformJCheckBox(text, selected, name, shader);
    cbox.addItemListener(this);
    panel.add(cbox);
  }
  
  public void addscenecheckbox(SceneJCheckBox cbox){
    cbox.addItemListener(this);
    panel.add(cbox);
  }
  
  public void adduniformslider(int min, int max, int value, String text, String name,Shader shader){
    JLabel label = new JLabel(text);
    panel.add(label);
    UniformJSlider slider= new UniformJSlider(min, max, value, name, shader);
    slider.addChangeListener(this);
    panel.add(slider);
  }

  @Override
  public void stateChanged(ChangeEvent e){
    Object source = e.getSource();
    if(source.getClass() == new UniformJSlider().getClass()){
      ((UniformJSlider)source).slide(e);
    }
  }

  @Override
  public void itemStateChanged(ItemEvent e){
    Object source = e.getItemSelectable();
    if(source.getClass() == new UniformJCheckBox().getClass()){
      ((UniformJCheckBox)source).select(e);
    }else if(source.getClass() == new SceneJCheckBox().getClass()){
      ((SceneJCheckBox)source).select(e);
    }
  }
  
}
