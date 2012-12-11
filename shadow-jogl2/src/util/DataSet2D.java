package util;

import gl.TexBindSet;
import gl.TexUnitManager;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import oekaki.util.Tex2D;
import scene.obj.Tiledboard;
import util.loader.Load2Dfloat;
import util.loader.Spline;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector3DDouble;
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
  funcWind, funcWindP, funcTemp;
  private RealFunction2DWrapper currentFunc;
  public List<Vector2DDouble> dist;
  public PoissonDiskSampler poisson;
  private double MAX;
  public boolean isChosen = false;
  private boolean isSort = false;
  
  private Vector2DDouble onePoint;
  
  public DataSet2D(String filepath, Tiledboard tboard){
    this.filepath = filepath;
    this.tboard = tboard;
  }
  
  public void init(GL2GL3 gl){
    initDataTex(gl, filepath);
    initDataFunc(filepath, tboard);
    currentFunc = funcWind;
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
        GL.GL_NEAREST, buffer, "wheather data");
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
    
    Load2Dfloat temp = 
        new Load2Dfloat(filepath + "Temperature_2.0m_T0.txt");
    temp.load();
   
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
    ByteBufferFloatWrapper2D temperature = 
        new ByteBufferFloatWrapper2D(tboard.leftdownx, tboard.leftdowny, 
        tboard.leftdownx + tboard.width, tboard.leftdowny + tboard.height,
        temp.width, temp.height, 
        temp.getbuffer());
    
    funcWindP = 
        new RealFunction2DWrapper(windspeed, 0, windspeed.max(), 0.2, 1);
    funcWind = 
        new RealFunction2DWrapper(windspeed, 0, windspeed.max(), 0, 1);
    MAX = windspeed.max();
    funcHumidP = 
        new RealFunction2DWrapper(humidity, 0, humidity.max(), 0.2, 1);
    funcHumid = 
        new RealFunction2DWrapper(humidity, 0, humidity.max(), 0, 1);
    funcTemp = 
        new RealFunction2DWrapper(temperature, 0, temperature.max(), 0, 1);
  }
  
  public double getDouble(double x, double y){
    return currentFunc.getDouble(x, y);
  }
  
  public double getDouble(Vector2DDouble pos){
    return currentFunc.getDouble(pos.x, pos.y);
  }
  
  public void updatePoisson(PoissonDiskSampler pds){
    poisson = pds;
    dist = poisson.sample();
    isChosen = false;
    isSort = false;
  }
  
  public void chooseOneRandomPoint(){
    if(dist == null || isChosen) return;
    int index = (int) (dist.size() * Math.random());
    onePoint = dist.get(index);
    dist.remove(index);
    isChosen = true;
  }
  
  public void chooseOnePointPercentRange(int from, int to){
    int index = 
        (int) ((from + Math.random() * (to - from)) * 0.01 * dist.size());
    chooseKthLargestPoint(index);
  }
  
  public void chooseKthLargestPoint(int k){
    if(dist == null) return;
    sortWind();
    onePoint = dist.get(k);
    dist.remove(k);
    isChosen = true;
  }
  
  public void sortWind(){
    if(dist == null) return;
    
    Collections.sort(dist, new PosWindComparator());
    isSort = true;
    
//    for(int i = 0; i < dist.size(); i++){
//      Vector2DDouble pos = dist.get(i);
//      System.out.println(funcWind.getDouble(pos.x, pos.y));
//    }
  }
  
  class PosWindComparator implements java.util.Comparator<Vector2DDouble> {
    @Override
    public int compare(Vector2DDouble pos1, Vector2DDouble pos2){     
      double diff = funcWind.getDouble(pos1.x, pos1.y) 
          - funcWind.getDouble(pos2.x, pos2.y);
      if(diff > 0){
        return 1;
      }else if(diff < 0){
        return -1;
      }
      return 0;
    }
    
  }
  
  public Vector2DDouble getChosenPoint(){
    return onePoint;
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
