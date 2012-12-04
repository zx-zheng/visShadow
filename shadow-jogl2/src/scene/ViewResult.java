package scene;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.*;

import scene.oldTypeScene.Scene;
import scene.oldTypeScene.Scene1;
import scene.templateScene.TexViewer;


public class ViewResult extends SceneOrganizer {
  
  Scene1 scene;
  TexViewer texviewer;
  int[] SCENE_VIEWPORT = {0, 0, 1024, 1024};
  
  public ViewResult(GL2GL3 gl, Scene1 scene){
    this.scene = scene;
    scene.updateChoiceList(gl);
  }
  
  @Override
  public void switchSO(GL2GL3 gl){
  
  }
  
  public void addTexViewer(TexViewer texviewer){
    this.texviewer = texviewer;
  }

  @Override
  public void rendering(GL2GL3 gl){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    
    scene.ShadowMap(gl, true); 
    
    if(texviewer != null) {
      texviewer.rendering(gl, scene.shadowTbs, 1024, 0, 640, 640);
    }
 
    scene.setShadowTexCoordSize(SCENE_VIEWPORT[2], SCENE_VIEWPORT[3]);
    scene.renderingToWindow(gl, SCENE_VIEWPORT[0], SCENE_VIEWPORT[1], 
        SCENE_VIEWPORT[2], SCENE_VIEWPORT[3], false);
    
    gl.glFlush();
  }

  @Override
  public void iterate(GL2GL3 gl){
    scene.iterate();
  }

  @Override
  public void keyPressed(KeyEvent e){
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseDragged(MouseEvent e){
    int x = e.getX(), y = e.getY();
    int movex = x - prevMouseX, movey = y - prevMouseY;
    prevMouseX = x; prevMouseY = y;
    if(javax.swing.SwingUtilities.isRightMouseButton(e)){
      scene.adjustMapCenter(movex, movey,
          SCENE_VIEWPORT[2], SCENE_VIEWPORT[3]);
    }else if(javax.swing.SwingUtilities.isLeftMouseButton(e)){
      scene.moveCenter(movex, movey, SCENE_VIEWPORT[2], SCENE_VIEWPORT[3]);
    }
    
  }

  @Override
  public void mousePressed(MouseEvent e){
    prevMouseX = e.getX();
    prevMouseY = e.getY();
  }

  @Override
  public void mouseReleased(MouseEvent e){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseMoved(MouseEvent e){
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clickButton(ActionEvent e){
    // TODO Auto-generated method stub
    
  }

}
