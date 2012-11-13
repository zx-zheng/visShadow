package gui;

import gl.Shader;
import gl.TexUnitManager;

import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import render.Scene;



public class Ctrlpanel implements ChangeListener, ItemListener, ActionListener{
  
  private static Ctrlpanel instance = new Ctrlpanel();
  public static Ctrlpanel getInstance(){
    return Ctrlpanel.instance;
  }
  JPanel panel = new JPanel();
  //ArrayList<JComponent> list = new ArrayList<JComponent>();
  public Scene scene;
  
  public Ctrlpanel(){
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
  }
  
  public JPanel getPanel(){
    return panel;
  }
  
  
  public void adduniformcheckbox(String text, boolean selected,
      String name, Shader shader){
    adduniformcheckbox(
        new UniformJCheckBox(text, selected, name, shader));
  }
  
  public void adduniformcheckbox(UniformJCheckBox checkbox){
    checkbox.addItemListener(this);
    panel.add(checkbox);
  }
  
  public void addscenecheckbox(SceneJCheckBox cbox){
    cbox.addItemListener(this);
    panel.add(cbox);
  }
  
  public void adduniformslider(int min, int max, int value, String text, String name,Shader shader){
    UniformJSlider slider= new UniformJSlider(min, max, value, name, shader);
    addJSlider(text, slider);
  }
  
  public void addJSlider(String text, JSlider slider){
    JLabel label = new JLabel(text);
    panel.add(label);
    slider.addChangeListener(this);
    panel.add(slider);
  }
  
  public void addLightCtrl(int count, Scene scene){
//    addComboBox("light", count);
//    JPanel lightpanel = new JPanel();
//    lightpanel.add(new JLabel("pos"));
//    lightpanel.add(new TextField(String.valueOf(scene.getLight(0).posx)));
//    lightpanel.add(new TextField(String.valueOf(scene.getLight(0).posy)));
//    lightpanel.add(new TextField(String.valueOf(scene.getLight(0).posz)));
//    panel.add(lightpanel);
  }
  
  public void addComboBox(String text, int count){
    String[] index = new String[count];
    for(int i=0;i<count;i++){
      index[i]=String.valueOf(i);
    }
    JLabel label = new JLabel(text);
    panel.add(label);
    JComboBox combobox = new JComboBox(index);
    combobox.setPreferredSize(new Dimension(100, 50));
    combobox.addActionListener(this);
    panel.add(combobox);
  }
  
  public void addSlider(JSlider slider, String text) {
    JLabel label = new JLabel(text);
    panel.add(label);
    slider.addChangeListener(this);
    panel.add(slider);
  }
  
  public void addButton(JButton button){
    button.addActionListener(this);
    panel.add(button);
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

  @Override
  public void actionPerformed(ActionEvent e){
    if(scene != null)
      scene.clickButton(e);
  }
  
}
