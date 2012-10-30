package za.co.luma.math.function;

import java.util.List;

import za.co.luma.geom.Vector2DDouble;

public class List2Wrapper2D extends RealFunction2DDouble{

  private List<Double> list1, list2;
  //p1:leftdown p2:rightup
  Vector2DDouble p1, p2;
  int datawidth, dataheight;
  double width, height;
  double max;
  boolean getmax = false;
  
  public List2Wrapper2D(double x1, double y1, double x2, double y2, 
      int datawidth, int dataheight, List<Double> list1, List<Double> list2){
    this.list1 = list1;
    this.list2 = list2;
    p1 = new Vector2DDouble(x1, y1);
    p2 = new Vector2DDouble(x2, y2);
    this.datawidth = datawidth;
    this.dataheight = dataheight;
    width = x2 - x1; height = y2 - y1;
  }
  
  @Override
  public double getDouble(double x, double y){
    // TODO Auto-generated method stub
    double leftdownx = (x - p1.x) / width * datawidth;
    double leftdowny = (y - p1.y) / height * dataheight;
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
      leftdownindex = rightdownindex;
    }else{
      leftdownindex = rightdownindex + datawidth;
      rightdownindex += datawidth;
    }
    double value1 = intery * (interx * list1.get(leftupindex)
        + (1 - interx) * list1.get(rightupindex))
        + (1 - intery) * (interx * list1.get(leftdownindex)
        + (1 - interx) * list1.get(rightdownindex));
    double value2 = intery * (interx * list2.get(leftupindex)
        + (1 - interx) * list2.get(rightupindex))
        + (1 - intery) * (interx * list2.get(leftdownindex)
        + (1 - interx) * list2.get(rightdownindex));
    return Math.sqrt(value1 * value1 + value2 * value2);
  }

  public double max(){
    if(!getmax){
      for(int i = 0; i <list1.size() ; i++){
        double tmp = (float)Math.sqrt(Math.pow(list1.get(i),2)
            +Math.pow(list2.get(i),2));
        if(tmp > max)max = tmp;
      }
      getmax = true;
    }
    return max;
  }
}
