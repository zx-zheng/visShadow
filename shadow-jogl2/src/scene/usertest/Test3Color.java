package scene.usertest;

import gui.Ctrlpanel;

import javax.media.opengl.GL2GL3;
import javax.swing.JButton;
import javax.swing.JSlider;

public class Test3Color extends Test3Shadow{

  @Override
  protected void initView(){
    viewpos1 = new float[3];
    viewpos2 = new float[3];
    
    double r = 30;
    
    viewpos1[0] = 0; 
    viewpos1[1] = 0;
    viewpos1[2] = (float) r;
    viewpos2[0] = 0; 
    viewpos2[1] = 0; 
    viewpos2[2] = (float) r;
  }
  
  @Override
  protected void setSceneViewport(){
    viewport1 = new int[4];
    viewport2 = new int[4];
    int offset = 20;
    int width = Math.min(CANVAS_HEIGHT, CANVAS_WIDTH / 2);
    viewport1[0] = offset;
    viewport1[1] = offset;
    viewport1[2] = width - 2 * offset;
    viewport1[3] = width - 2 * offset;
    
    int subWindowWidth = 400;
    viewport2[0] = viewport1[0] + viewport1[2] + offset 
        + viewport2[0] / 2 - subWindowWidth / 2;
    viewport2[1] = offset + viewport2[3] / 2;// - subWindowWidth / 2;
    viewport2[2] = subWindowWidth - 2 * offset;
    viewport2[3] = subWindowWidth - 2 * offset;
  }
  
  protected void setGUI(){
    answerSlider = new JSlider(-100, 100, 100);
    Ctrlpanel.getInstance().addSlider(answerSlider, "Test3ScaleSlider");
    
    answerButton = new JButton("Test3Color Answer");
    Ctrlpanel.getInstance().addButton(answerButton);
  }
  
  @Override
  protected void showQuestion(GL2GL3 gl){
    gl.glViewport(viewport1[0], viewport1[1], 
        viewport1[2], viewport1[3]);
    scene.setShadowTexCoordSize(viewport1[2], viewport1[3]);
    scene.lookat(viewpos1[0], viewpos1[1], viewpos1[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.test3ColorRendering1(gl);
    

    gl.glViewport(viewport2[0], viewport2[1], 
        viewport2[2], viewport2[3]);
    scene.setShadowTexCoordSize(viewport2[2], viewport2[3]);
    scene.lookat(viewpos2[0], viewpos2[1], viewpos2[2],
        0, 0, 0, 0, 1, 0);
    scene.updatePVMatrix(gl);
    scene.updatePVMatrixtess(gl);
    scene.test3ColorRendering2(gl, sliderValueConvert(answerSlider.getValue()));
  }
  
  private double sliderValueConvert(int value){
    return value / 100d;
  }
}
