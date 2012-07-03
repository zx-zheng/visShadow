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


public class Ctrlpanel implements ChangeListener, ItemListener{
  
  private static Ctrlpanel instance = new Ctrlpanel();
  public static Ctrlpanel getInstance(){
    return Ctrlpanel.instance;
  }
  JPanel panel = new JPanel();
  ArrayList<JComponent> list = new ArrayList<JComponent>();
  public Ctrlpanel(){
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//    JSlider slider = new JSlider();
//    slider.addChangeListener(this);
//    UniformJCheckBox cbox = new UniformJCheckBox("test", true); 
//    cbox.addItemListener(this);
//    panel.add(slider);
//    panel.add(cbox);
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
  
  public void adduniformslider(int min, int max, int value, String name,Shader shader){
    JLabel label = new JLabel(name);
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
    }
  }
  
}
