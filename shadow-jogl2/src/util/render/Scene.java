package util.render;

import java.nio.FloatBuffer;
import java.util.*;

import javax.media.opengl.*;

import oekaki.util.*;

import util.gl.*;
import util.render.obj.Light;

import com.jogamp.opengl.util.PMVMatrix;


public abstract class Scene implements RenderingPass {
  
  Shader shadowmapshader, shadowmapshadertess, 
  shader, shadertess;
  int smapwidth, smapheight;
  protected int realLightcount, virtualLightcount;
  FBO smapfbo;
  TexBindSet shadowmapB;
  
  String smapvsrc = "src/util/render/Shadowmap/vert.c",
      smapcsrc = "src/util/render/Shadowmap/ctrl.c",
      smapesrc = "src/util/render/Shadowmap/eval.c",
      smapgsrc = "src/util/render/Shadowmap/geom.c",
      smapfsrc = "src/util/render/Shadowmap/frag.c";
  
  final TexUnitManager tum = TexUnitManager.getInstance();
  ObjManager om;
  public List<Light> lights = new ArrayList<Light>();

  protected PMVMatrix pvmat, model;
  protected int uniformlightsview, uniformlightsproj,
  uniformlightsviewtess, uniformlightsprojtess,
  uniformview, uniformproj,
  uniformviewtess, uniformprojtess,
  uniformlightcount, uniformlightcounttess;
  
  public Scene(){
    pvmat = new PMVMatrix();
    model = new PMVMatrix();
  }
  
  public void setSmapfbo(FBO fbo){
    smapfbo = fbo;
  }
  
  private void addLight(int i){
    realLightcount += i;
    for(int j = 0; j < i; j++){
      lights.add(new Light());
    }
    virtualLightcount = realLightcount;
  }
  
  public int lightCount(){
    return realLightcount;
  }
  
  public void setShadowmapShaderTexture(GL2GL3 gl, 
      TexBindSet tbs, String texname){
    shadowmapshader.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadowmapshader.getID(), texname),
        tbs.texunit);
    shadowmapshadertess.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadowmapshadertess.getID(), texname),
        tbs.texunit);
    shadowmapshadertess.unuse(gl);
  }
  
  public void setShadowmapShader1i(GL2GL3 gl, int i, String name){
    shadowmapshader.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadowmapshader.getID(), name),
        i);
    shadowmapshadertess.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadowmapshadertess.getID(), name),
        i);
    shadowmapshadertess.unuse(gl);
  }
  
  public void initShadowmap(GL2GL3 gl, int lightcount, int width, int height){
    addLight(lightcount);
    smapwidth = width;
    smapheight = height;
    
    Tex2DArray shadowmap = new Tex2DArray(GL2.GL_RGB16F, GL2.GL_RGB,
        GL.GL_FLOAT, smapwidth, smapheight, realLightcount,
        GL.GL_LINEAR, "shadowmap");
    Tex2DArray shadowmapdepth = 
        new Tex2DArray(GL2.GL_DEPTH_COMPONENT, GL2.GL_DEPTH_COMPONENT,
            GL.GL_FLOAT, smapwidth, smapheight, realLightcount, 
            GL.GL_LINEAR, "shadowmapdepth");
    shadowmap.init(gl);
    shadowmapdepth.init(gl);
    shadowmapB = tum.bind(gl, shadowmap);
    smapfbo = new FBO(gl);  
    smapfbo.attachTexture(gl, shadowmapB);
    smapfbo.attachDepth(gl, shadowmapdepth);
    
    /*シャドウマップ*/
    shadowmapshader = new Shader(
        smapvsrc,
        null,
        null,
        smapgsrc,        
        smapfsrc);
    shadowmapshader.init(gl);
    shadowmapshader.use(gl);
 
    uniformlightsview = gl.glGetUniformLocation
        (shadowmapshader.getID(), "lightsview");
    uniformlightsproj = gl.glGetUniformLocation
        (shadowmapshader.getID(), "lightsproj");
    uniformlightcount = gl.glGetUniformLocation
        (shadowmapshader.getID(), "lightcount_real_virtual");
    
    /*シャドウマップテッセレーション*/
    shadowmapshadertess = new Shader(
        smapvsrc,
        smapcsrc,
        smapesrc,
        smapgsrc,
        smapfsrc);
    shadowmapshadertess.init(gl);
    shadowmapshadertess.use(gl);
    uniformlightsviewtess= gl.glGetUniformLocation
        (shadowmapshadertess.getID(), "lightsview");
    uniformlightsprojtess = gl.glGetUniformLocation
        (shadowmapshadertess.getID(), "lightsproj");
    uniformlightcounttess = gl.glGetUniformLocation
        (shadowmapshadertess.getID(), "lightcount_real_virtual");
  }
  
  public void replaceSmapShader(int shadertype, String path){
    switch (shadertype){
    case GL2.GL_VERTEX_SHADER:
      smapvsrc = path;
      break;
    case GL4.GL_TESS_CONTROL_SHADER:
      smapcsrc = path;
      break;
    case GL4.GL_TESS_EVALUATION_SHADER:
      smapesrc = path;
      break;
    case GL4.GL_GEOMETRY_SHADER:
      smapgsrc = path;
      break;
    case GL2.GL_FRAGMENT_SHADER:
      smapfsrc = path;
      break;
    }
  }
  
  public TexBindSet getShadowmapTexture(){
    return shadowmapB;
  }
  
  public void updatelights(GL2GL3 gl, 
      int uniformlightsview, int uniformlightsproj, 
      int uniformlightcount){
    FloatBuffer lightspos = FloatBuffer.allocate(16 * realLightcount);
    FloatBuffer lightsproj = FloatBuffer.allocate(16 * realLightcount);
    
    for(int i = 0; i < realLightcount; i++){
      FloatBuffer tmp = lights.get(i).getMatrixf(GL2.GL_MODELVIEW);
      //putするときにFloatBufferの位置が動いていけないのでmark()とreset()を行う。
      tmp.mark();
      lightspos.put(tmp);
      tmp.reset();
      tmp = lights.get(i).getMatrixf(GL2.GL_PROJECTION);
      tmp.mark();
      lightsproj.put(tmp);
      tmp.reset();
    }
    lightspos.rewind();
    lightsproj.rewind();  
    
    gl.glUniformMatrix4fv(uniformlightsview, realLightcount,
        false, lightspos);
    gl.glUniformMatrix4fv(uniformlightsproj, realLightcount,
        false, lightsproj);
    gl.glUniform2i(uniformlightcount, realLightcount, virtualLightcount);
  }
  
  public void updateligths(GL2GL3 gl, Shader shader){
    updatelights(gl, 
        gl.glGetUniformLocation(shader.getID(), "lightsview"), 
        gl.glGetUniformLocation(shader.getID(), "lightsproj"),
        gl.glGetUniformLocation(shader.getID(), "lightcount_real_virtual"));   
  }
  
  public void smapupdatelights(GL2GL3 gl){
    updatelights(gl, 
        uniformlightsview, uniformlightsproj, uniformlightcount); 
  }
  
  public void smapupdatelightstess(GL2GL3 gl){
    updatelights(gl, 
        uniformlightsviewtess, uniformlightsprojtess, uniformlightcounttess); 
  }
  
  public void ShadowMap(GL2GL3 gl){
    ShadowMap(gl, true);
  }
  
  public void ShadowMap(GL2GL3 gl, boolean show){
    smapfbo.bind(gl);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    gl.glViewport(0, 0, smapwidth, smapheight);
    
    shadowmapshader.use(gl);
    smapupdatelights(gl);
    scene(gl, shadowmapshader, show);
    
    shadowmapshadertess.use(gl);
    smapupdatelightstess(gl);
    scenetess((GL4)gl, shadowmapshadertess);
    
    gl.glFlush();
    smapfbo.unbind(gl);
    shadowmapshader.unuse(gl);
  }
  
  
  public void init(GL2GL3 gl, Shader shader, Shader shadertess){
    this.shader = shader;
    shader.use(gl);
    uniformview = gl.glGetUniformLocation
        (shader.getID(), "view");
    uniformproj = gl.glGetUniformLocation
        (shader.getID(), "proj");
    
    this.shadertess = shadertess;
    shadertess.use(gl);
    uniformviewtess = gl.glGetUniformLocation
        (shadertess.getID(), "view");
    uniformprojtess = gl.glGetUniformLocation
        (shadertess.getID(), "proj");
    shadertess.unuse(gl);
  }
  
  public void renderingToWindow(GL2GL3 gl, int offsetx, int offsety,
      int width, int height){
    renderingToWindow(gl, offsetx, offsety, width, height, true);
  }
  public void renderingToWindow(GL2GL3 gl, int offsetx, int offsety,
      int width, int height, boolean show){
    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    gl.glViewport(offsetx, offsety, width, height);
    
    shader.use(gl);
    updateligths(gl, shader);
    scene(gl, shader, show);
    
    shadertess.use(gl);
    updateligths(gl, shadertess);
    scenetess((GL3)gl, shadertess);
    shadertess.unuse(gl);
    
    gl.glFlush();
  }   
  
  public void renderingToFBO(GL2GL3 gl, FBO fbo, int offsetx, int offsety,
      int width, int height){
    shader.use(gl);
    fbo.bind(gl);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    gl.glViewport(offsetx, offsety, width, height);
    scene(gl, shader);
    shader.unuse(gl);
    fbo.unbind(gl);
    gl.glFlush();
  }   
  
  public void updatePVMatrix(GL2GL3 gl){
    shader.use(gl);
    gl.glUniformMatrix4fv(uniformview, 1, false, 
        pvmat.glGetMatrixf(GL2.GL_MODELVIEW));
    gl.glUniformMatrix4fv(uniformproj, 1, false, 
        pvmat.glGetMatrixf(GL2.GL_PROJECTION));
    shader.unuse(gl);
  } 
  public void updatePVMatrixtess(GL2GL3 gl){
    shadertess.use(gl);
    gl.glUniformMatrix4fv(uniformviewtess, 1, false, 
        pvmat.glGetMatrixf(GL2.GL_MODELVIEW));
    gl.glUniformMatrix4fv(uniformprojtess, 1, false, 
        pvmat.glGetMatrixf(GL2.GL_PROJECTION));
    shadertess.unuse(gl);
  }
  
  public void lookat(float eyeX, float eyeY, float eyeZ, 
      float centerX, float centerY, float centerZ,
      float upX, float upY, float upZ){
    Vec3 f = new Vec3(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
    f.Nor();
    Vec3 U = new Vec3(upX, upY, upZ);
    U.Nor();
    Vec3 s = f.Cro(U);
    s.Nor();
    Vec3 u = s.Cro(f);
    u.Nor();

    float[] mat = {
        s.x, u.x, -f.x, 0,
        s.y, u.y, -f.y, 0,
        s.z, u.z, -f.z, 0,
        -eyeX * s.x + -eyeY * s.y + -eyeZ * s.z,
        -eyeX * u.x + -eyeY * u.y + -eyeZ * u.z,
        -eyeX * -f.x + -eyeY * -f.y + -eyeZ * -f.z, 1};
    pvmat.glMatrixMode(GL2.GL_MODELVIEW);
    pvmat.glLoadMatrixf(mat, 0);
  }
  public void lookatd(double eyeX, double eyeY, double eyeZ, 
      double centerX, double centerY, double centerZ,
      double upX, double upY, double upZ){
    Vec3d f = new Vec3d(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
    f.Nor();
    Vec3d U = new Vec3d(upX, upY, upZ);
    U.Nor();
    Vec3d s = f.Cro(U);
    s.Nor();
    Vec3d u = s.Cro(f);
    u.Nor();

    float[] mat = {
        (float)s.x, (float)u.x, (float)-f.x, 0,
        (float)s.y, (float)u.y, (float)-f.y, 0,
        (float)s.z, (float)u.z, (float)-f.z, 0,
        (float)(-eyeX * s.x + -eyeY * s.y + -eyeZ * s.z),
        (float)(-eyeX * u.x + -eyeY * u.y + -eyeZ * u.z),
        (float)(-eyeX * -f.x + -eyeY * -f.y + -eyeZ * -f.z), 1};
    pvmat.glMatrixMode(GL2.GL_MODELVIEW);
    pvmat.glLoadMatrixf(mat, 0);
  }  
  public void perspectivef(float fovy,
      float aspect,
      float zNear,
      float zFar){
    pvmat.glMatrixMode(GL2.GL_PROJECTION);
    pvmat.glLoadIdentity();
    pvmat.gluPerspective(fovy, aspect, zNear, zFar);
  }  
  public void orthof(float left,
      float right,
      float bottom,
      float top,
      float zNear,
      float zFar){
    pvmat.glMatrixMode(GL2.GL_PROJECTION);
    pvmat.glOrthof(left, right, bottom, top, zNear, zFar);
    pvmat.update();
  }
  
  public void updateModelMatrix(GL2GL3 gl, Shader shader, PMVMatrix model){
    model.update();
//    if(gl.glGetUniformLocation(shader.getID(), "model") == -1)
//      System.out.println("uniform model error");
    gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.getID(), "model"),
        1, false, model.glGetMatrixf(GL2.GL_MODELVIEW));
  }
  
  public void setTessLevel(GL2GL3 gl, int level){
    shadowmapshadertess.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadowmapshadertess.getID(), "tesslevel"),
        level);
    shadowmapshadertess.unuse(gl);
    
    shadertess.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadertess.getID(), "tesslevel"),
        level);
    shadertess.unuse(gl);
  }
  public void shader1i(GL2GL3 gl, int i, String name){
    shader.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), name),i);
    shadertess.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shadertess.getID(), name),i);
    shadertess.unuse(gl);
  }
  @Override
  public abstract void scene(GL2GL3 gl, Shader shader);
    // TODO Auto-generated method stub
  public abstract void scene(GL2GL3 gl, Shader shader, boolean show);
  
  public abstract void scenetess(GL3 gl, Shader shader);
  
  public abstract void scenetess(GL3 gl, Shader shader, boolean show);

  public abstract void iterate();

}
