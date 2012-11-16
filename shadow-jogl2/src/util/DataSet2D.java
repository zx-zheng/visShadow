package util;

import gl.TexBindSet;
import gl.TexUnitManager;

import java.nio.ByteBuffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import oekaki.util.Tex2D;
import scene.obj.Tiledboard;
import util.loader.Load2Dfloat;
import util.loader.Spline;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.math.function.ByteBufferFloat2Wrapper2D;
import za.co.luma.math.function.ByteBufferFloatWrapper2D;
import za.co.luma.math.function.RealFunction2DWrapper;
import za.co.luma.math.sampling.PoissonDiskSampler;

public class DataSet2D{

  private boolean IN_USE = false;
  private String filepath;
  private Tiledboard tboard;
  public TexBindSet tex;
  public RealFunction2DWrapper funcHumid, funcHumidP, 
  funcWind, funcWindP;
  public List<Vector2DDouble> dist;
  public PoissonDiskSampler poisson;
  private double MAX;
  
  public DataSet2D(String filepath, Tiledboard tboard){
    this.filepath = filepath;
    this.tboard = tboard;
  }
  
  public void init(GL2GL3 gl){
    initDataTex(gl, filepath);
    initDataFunc(filepath, tboard);
  }
  
  private void initDataTex(GL2GL3 gl, String filepath){

    Load2Dfloat loader = new Load2Dfloat(filepath + "RelativeHumidity_2.0m_T0.txt",
        filepath + "Temperature_2.0m_T0.txt",
        filepath + "UofWind_10.0m_T0.txt",
        filepath + "VofWind_10.0m_T0.txt");
    loader.loadOffsetLine(0);

    int div = 1;
    Load2Dfloat l0 = new Load2Dfloat(filepath + "RelativeHumidity_2.0m_T0.txt");
    l0.loadOffsetLine(0);
    Spline spl0 = new Spline(l0.getbuffer(), l0.width, l0.height, div);
    Load2Dfloat l1 = new Load2Dfloat(filepath + "Temperature_2.0m_T0.txt");
    l1.loadOffsetLine(0);
    Spline spl1 = new Spline(l1.getbuffer(), l1.width, l1.height, div);
    Load2Dfloat l2 = new Load2Dfloat(filepath + "UofWind_10.0m_T0.txt");
    l2.loadOffsetLine(0);
    Spline spl2 = new Spline(l2.getbuffer(), l2.width, l2.height, div);
    Load2Dfloat l3 = new Load2Dfloat(filepath + "VofWind_10.0m_T0.txt");
    l3.loadOffsetLine(0);
    Spline spl3 = new Spline(l3.getbuffer(), l3.width, l3.height, div);
    ByteBuffer[] splarray = {spl0.getSpline(),spl1.getSpline(),
        spl2.getSpline(),spl3.getSpline()};
    ByteBuffer buffer = Load2Dfloat.constructByteBuffer(splarray);

    Tex2D weatherTex = new Tex2D(GL2.GL_RGBA16F, GL2.GL_RGBA, 
        GL.GL_FLOAT, spl0.width(), spl0.height(), 
        GL.GL_LINEAR, buffer, "wheather data");
    weatherTex.init(gl);
    tex = new TexBindSet(weatherTex);
  }
  
  private void initDataFunc(String filepath, Tiledboard tboard){
    Load2Dfloat windspeedu = 
        new Load2Dfloat(filepath + "UofWind_10.0m_T0.txt");
    Load2Dfloat windspeedv = 
        new Load2Dfloat(filepath + "VofWind_10.0m_T0.txt");
    windspeedu.load();
    windspeedv.load();
    
    Load2Dfloat humid = 
        new Load2Dfloat(filepath + "RelativeHumidity_2.0m_T0.txt");
    humid.load();
   
    ByteBufferFloat2Wrapper2D windspeed = 
        new ByteBufferFloat2Wrapper2D(tboard.leftdownx, tboard.leftdowny, 
            tboard.leftdownx + tboard.width, tboard.leftdowny + tboard.height,
            windspeedu.width, windspeedu.height, 
            windspeedu.getbuffer(), windspeedv.getbuffer());
    ByteBufferFloatWrapper2D humidity = 
        new ByteBufferFloatWrapper2D(tboard.leftdownx, tboard.leftdowny, 
        tboard.leftdownx + tboard.width, tboard.leftdowny + tboard.height,
        humid.width, humid.height, 
        humid.getbuffer());
    funcWindP = 
        new RealFunction2DWrapper(windspeed, 0, windspeed.max(), 0.2, 1);
    funcWind = 
        new RealFunction2DWrapper(windspeed, 0, windspeed.max(), 0, 1);
    MAX = windspeed.max();
    funcHumidP = 
        new RealFunction2DWrapper(humidity, 0, humidity.max(), 0.2, 1);
    funcHumid = 
        new RealFunction2DWrapper(humidity, 0, humidity.max(), 0, 1);
  }
  
  public void updatePoisson(PoissonDiskSampler pds){
    poisson = pds;
    dist = poisson.sample();
  }
  
  public double max(){
    return MAX;
  }
  
  public void use(GL2GL3 gl){
    if(IN_USE){
      return;
    }
    tex.bind(gl);
    IN_USE = true;
  }
  
  public void unuse(GL2GL3 gl){
    tex.unbind(gl);
    IN_USE = false;
  }
  
  public boolean isUse(){
    return IN_USE;
  }
}
