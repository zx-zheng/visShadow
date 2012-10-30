package za.co.luma.math.function;

import java.nio.ByteBuffer;

import za.co.luma.geom.Vector2DDouble;

public class ByteBufferFloat2Wrapper2D extends RealFunction2DDouble{

  //データは1次元で左上から右下の順番で数値が並んでいるとする
  private ByteBuffer buffer1, buffer2;
  //p1:leftdown p2:rightup
  Vector2DDouble p1, p2;
  int datawidth, dataheight;
  double width, height;
  double max;
  boolean getmax = false;
  
  public ByteBufferFloat2Wrapper2D(double x1, double y1, double x2, double y2, 
      int datawidth, int dataheight, ByteBuffer buffer1, ByteBuffer buffer2){
    this.buffer1 = buffer1;
    this.buffer2 = buffer2;
    p1 = new Vector2DDouble(x1, y1);
    p2 = new Vector2DDouble(x2, y2);
    this.datawidth = datawidth;
    this.dataheight = dataheight;
    width = x2 - x1; height = y2 - y1;
  }
  
  @Override
  public double getDouble(double x, double y){
    if(!inArea(new Vector2DDouble(x, y)) || Double.isNaN(x) || Double.isNaN(y))
      return 0;
    //System.out.println(x +" " + y);
    //System.out.println(x + " "+ p1.x +  " "+ width+ " "+  datawidth);
    double leftdownx = (x - p1.x) / width * datawidth;
    double leftdowny = (y - p1.y) / height * dataheight;
    //System.out.println(leftdownx);
    int leftupindex = (int) leftdownx + (int) leftdowny * datawidth;
    double interx = leftdownx - (int) leftdownx;
    double intery = leftdowny - (int) leftdowny;
    int rightupindex, leftdownindex, rightdownindex;
 
    if(leftdownx > datawidth -1){
      rightupindex = leftupindex;
      rightdownindex = leftupindex;
    }else{
      rightupindex = leftupindex + 1;
      rightdownindex = leftupindex + 1;
    }
    
    if(leftdowny > dataheight - 1){
    //if(leftdowny < 2){
      leftdownindex = rightdownindex;
      //System.out.println(leftdownindex);
    }else{
      //System.out.println(leftdowny);
      leftdownindex = rightdownindex + datawidth;
      rightdownindex += datawidth;
      //System.out.println(leftdownindex);
    }
    interx = 1- interx;
    intery = 1-intery;
    double value1 = (intery) * (interx * buffer1.getFloat(4 * leftupindex)
        + (1 - interx) * buffer1.getFloat(4 * rightupindex))
        + (1 - intery) * (interx * buffer1.getFloat(4 * leftdownindex)
        + (1 - interx) * buffer1.getFloat(4 * rightdownindex));
    double value2 = (intery) * (interx * buffer2.getFloat(4 * leftupindex)
        + (1 - interx) * buffer2.getFloat(4 * rightupindex))
        + (1 - intery) * (interx * buffer2.getFloat(4 * leftdownindex)
        + (1 - interx) * buffer2.getFloat(4 * rightdownindex));
    return Math.sqrt(value1 * value1 + value2 * value2);
  }
  
  private boolean inArea(Vector2DDouble p){
    if(p.x < p1.x || p2.x < p.x || p.y < p1.y || p2.y < p.y){
      //System.out.println("out of area");
      return false;
    }
    return true;
  }

  public double max(){
    if(!getmax){
      for(int i = 0; i <buffer1.limit() ; i+=4){
        double tmp = (float)Math.sqrt(Math.pow(buffer1.getFloat(i),2)
            +Math.pow(buffer2.getFloat(i),2));
        if(tmp > max)max = tmp;
      }
      getmax = true;
    }
    return max;
  }

}
