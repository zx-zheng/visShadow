package util;

import za.co.luma.geom.Vector3DDouble;

public class ColorUtil{

  public static Vector3DDouble LabtoXYZ(double l,double a,double b){
    double xn = 0.9505;
    double yn = 1.0;
    double zn = 1.0888;
    double p = (l+16.0)/116.0;
    double x = xn * Math.pow(p+a/500.0,3.0);
    double y = yn * Math.pow(p,3.0);
    double z = zn * Math.pow((p-b/200.0),3.0);
    return new Vector3DDouble(x,y,z);
  }
  
  public static Vector3DDouble XYZtoLab(Vector3DDouble xyz){
    double l, a, b;
    double xn = 0.9505;
    double yn = 1.0;
    double zn = 1.0888;
    l = 116 * Math.pow(xyz.y,  1.0 / 3) - 16;
    a = 500 * (Math.pow((xyz.x / xn), 1.0/3) - Math.pow((xyz.y / yn), 1.0/3));
    b = 200 * (Math.pow((xyz.y / yn), 1.0/3) - Math.pow((xyz.z / zn), 1.0/3));
    return new Vector3DDouble(l, a, b);
  }

  public static Vector3DDouble XYZtosRGB(Vector3DDouble xyz){
    return new Vector3DDouble(3.2406 * xyz.x -1.5372 * xyz.y + -0.4986 * xyz.z ,
        -0.9689 * xyz.x + 1.8758 * xyz.y +0.0415 * xyz.z,
        0.0557 * xyz.x  -0.2040 * xyz.y + 1.0570 * xyz.z);
  }
  
  public static Vector3DDouble RGBtoNonlinearRGB(Vector3DDouble rgb){
    double rd, gd, bd;
    if (rgb.x < 0.0031308) rd = rgb.x * 12.92;
    else rd = 1.055 * Math.pow(rgb.x, 1.0 / 2.4) - 0.055;
    if (rgb.y < 0.0031308) gd = rgb.y * 12.92;
    else gd = 1.055 * Math.pow(rgb.y, 1.0 / 2.4) - 0.055;
    if (rgb.z < 0.0031308) bd = rgb.z * 12.92;
    else bd = 1.055 * Math.pow(rgb.z, 1.0 / 2.4) - 0.055;
    return new Vector3DDouble(rd, gd, bd);
  }
  
  public static Vector3DDouble LabtoRGB(double l,double a,double b){
    return RGBtoNonlinearRGB(XYZtosRGB(LabtoXYZ(l,a,b)));
  }
  
  public static Vector3DDouble RGBtoLab(double r, double g, double b){
    return XYZtoLab(
        RGBtoXYZ(
            nonlinearRGBtoLinearRGB(
                new Vector3DDouble(r, g, b))));
  }
  
  public static Vector3DDouble nonlinearRGBtoLinearRGB(Vector3DDouble rgb){
    double r, g, b;
    if(rgb.x < 0.040449936) r = rgb.x / 12.92;
    else r = Math.pow(((rgb.x + 0.055) / 1.055), 2.4);
    if(rgb.y < 0.040449936) g = rgb.y / 12.92;
    else g = Math.pow(((rgb.y + 0.055) / 1.055), 2.4);
    if(rgb.z < 0.040449936) b = rgb.z / 12.92;
    else b = Math.pow(((rgb.z + 0.055) / 1.055), 2.4);
    return new Vector3DDouble(r, g, b);
  }
  
  public static Vector3DDouble RGBtoXYZ(Vector3DDouble rgb){
    double x, y, z;
    x = 0.412391*rgb.x+ 0.357584 * rgb.y + 0.180481 * rgb.z;
    y = 0.212639 * rgb.x+ 0.715169 * rgb.y + 0.072192 * rgb.z;
    z = 0.019331 * rgb.x + 0.119195 * rgb.y + 0.950532 * rgb.z;
    //System.out.println(new Vector3DDouble(x, y, z));
    return new Vector3DDouble(x, y, z);
  }
  
  
}
