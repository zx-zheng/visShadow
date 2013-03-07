package gui;

import gl.Shader;
import gl.TexUnitManager;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scene.SceneOrganizer;
import scene.SceneRender;
import scene.oldTypeScene.Scene;



public class Ctrlpanel implements ChangeListener, ItemListener, ActionListener{
  
  private static Ctrlpanel instance = new Ctrlpanel();
  public static Ctrlpanel getInstance(){
    return Ctrlpanel.instance;
  }
  
  public static SceneOrganizer currentSO;
  
  static JFrame settingFrame;// = new JFrame("Settings");
  JPanel ctrlPanel = new JPanel();
  JPanel userTestPanel = new JPanel();

  public Scene scene;
  public SceneRender sceneRender;
  
  public Ctrlpanel(){
    ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.Y_AXIS));
    //ctrlPanel.setVisible(false);
   
    userTestPanel.setLayout(new FlowLayout());
  }
  
  public void initSettingFrame(){
    settingFrame = new JFrame("Settings");
    settingFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    settingFrame.getContentPane().setLayout(new FlowLayout());
       
    settingFrame.setSize(300, 1000);
    settingFrame.getContentPane().add(ctrlPanel);
    SwingUtilities.invokeLater(new Runnable(){
      @Override
      public void run(){
        settingFrame.setVisible(true);
      }
    });
  }
  
  public void showCtrlPanel(){
    SwingUtilities.invokeLater(new Runnable(){
      @Override
      public void run(){
        ctrlPanel.setVisible(true);
      }});    
    settingFrame.validate();
  }
  
  public void hideCtrlPanel(){
    ctrlPanel.setVisible(false);
  }
  
  public JPanel getCtrlPanel(){
    return ctrlPanel;
  }
  
  public JPanel getUserTestPane(){
    return userTestPanel;
  }
  
  
  public void adduniformcheckbox(String text, boolean selected,
      String name, Shader shader){
    adduniformcheckbox(
        new UniformJCheckBox(text, selected, name, shader));
  }
  
  public void adduniformcheckbox(UniformJCheckBox checkbox){
    checkbox.addItemListener(this);
    ctrlPanel.add(checkbox);
  }
  
  public void addscenecheckbox(SceneJCheckBox cbox){
    cbox.addItemListener(this);
    ctrlPanel.add(cbox);
  }
  
  public void adduniformslider(int min, int max, int value, String text, String name,Shader shader){
    UniformJSlider slider= new UniformJSlider(min, max, value, name, shader);
    slider.addLabel(text);
    addJSlider(slider.label, slider);
  }
  
  public void addJSlider(JLabel label, JSlider slider){
    ctrlPanel.add(label);
    slider.addChangeListener(this);
    ctrlPanel.add(slider);
  }
  
  public void addJSlider(String text, JSlider slider){
    JLabel label = new JLabel(text);
    ctrlPanel.add(label);
    slider.addChangeListener(this);
    ctrlPanel.add(slider);
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
    ctrlPanel.add(label);
    JComboBox combobox = new JComboBox(index);
    combobox.setPreferredSize(new Dimension(100, 50));
    combobox.addActionListener(this);
    ctrlPanel.add(combobox);
  }
  
  public void addSlider(JSlider slider, String text) {
    addSlider(ctrlPanel, slider, text);
  }
  
  public void addSlider(JPanel panel, JSlider slider, String text) {
    JLabel label = new JLabel(text);
    panel.add(label);
    slider.addChangeListener(this);
    panel.add(slider);
  }
  
  public void addButton(JButton button){
    button.addActionListener(this);
    ctrlPanel.add(button);
  }
  
  public void addButton(JPanel panel, JButton button){
    button.addActionListener(this);
    panel.add(button);
  }
  
  public void addCheckBox(JPanel panel, JCheckBox checkBox){
    checkBox.addActionListener(this);
    panel.add(checkBox);
  }
  
  public void addComponent(JComponent comp){
    ctrlPanel.add(comp);
  }

  @Override
  public void stateChanged(ChangeEvent e){
    Object source = e.getSource();
    if(source.getClass() == new UniformJSlider().getClass()){
      ((UniformJSlider)source).slide(e);
    }
    if(currentSO != null){
      currentSO.stateChanged(e);
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
      scene.actionPerformed(e);
    
    if(sceneRender != null)
      sceneRender.actionPerformed(e);
  }

}
