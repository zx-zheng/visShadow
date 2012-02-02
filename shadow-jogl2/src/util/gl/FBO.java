package util.gl;

import javax.media.opengl.*;

import oekaki.util.TextureBase;

public class FBO{
  final static private int[] COLOR_ATTACHMENTS={
    GL2.GL_COLOR_ATTACHMENT0,GL2.GL_COLOR_ATTACHMENT1,
    GL2.GL_COLOR_ATTACHMENT2,GL2.GL_COLOR_ATTACHMENT3,
    GL2.GL_COLOR_ATTACHMENT4,GL2.GL_COLOR_ATTACHMENT5,
    GL2.GL_COLOR_ATTACHMENT6,GL2.GL_COLOR_ATTACHMENT7,
    GL2.GL_COLOR_ATTACHMENT8,GL2.GL_COLOR_ATTACHMENT9,
    GL2.GL_COLOR_ATTACHMENT10,GL2.GL_COLOR_ATTACHMENT11,
    GL2.GL_COLOR_ATTACHMENT12,GL2.GL_COLOR_ATTACHMENT13,
    GL2.GL_COLOR_ATTACHMENT14,GL2.GL_COLOR_ATTACHMENT15};
  
  boolean isInitialized;

  private int fboid, rboid, attachcount = 0;
  
  public FBO(GL2GL3 gl){
    int[] id = new int[1];
    gl.glGenFramebuffers(1, id, 0);
    fboid = id[0];
  }
  
  public void attachTexture(GL2GL3 gl, TexBindSet tbs){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fboid);
    gl.glFramebufferTextureARB(GL2.GL_FRAMEBUFFER,
        COLOR_ATTACHMENTS[attachcount],
        tbs.tex.getTexID(),
        0);
    attachcount++;
  }
  
  public void attachDepth(GL2GL3 gl, TexBindSet tbs){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fboid);
    gl.glFramebufferTextureARB(GL2.GL_FRAMEBUFFER,
        GL2.GL_DEPTH_ATTACHMENT,
        tbs.tex.getTexID(),
        0);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
  public void attachDepth(GL2GL3 gl, TextureBase tex){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fboid);
    gl.glFramebufferTextureARB(GL2.GL_FRAMEBUFFER,
        GL2.GL_DEPTH_ATTACHMENT,
        tex.getTexID(),
        0);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
  public void attachDepthRBO(GL2GL3 gl, int width, int height){
    int[] id = new int[1];
    gl.glGenRenderbuffers(1, id, 0);
    rboid = id[0];
    gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, rboid);
    gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER,
        GL2.GL_DEPTH_COMPONENT,
        width, height);
    gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
    
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fboid);
    gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER,
        GL2.GL_DEPTH_ATTACHMENT,
        GL2.GL_RENDERBUFFER,
        rboid);
    gl.glEnable(GL2.GL_DEPTH_TEST);
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
  }
  
  public void bind(GL2GL3 gl){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fboid);
    gl.glDrawBuffers(attachcount, COLOR_ATTACHMENTS, 0);
  }
  
  public void unbind(GL2GL3 gl){
    gl.glDrawBuffers(1, COLOR_ATTACHMENTS, 0);
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
  }
  
  public int getID(){
    return fboid;
  }
}
