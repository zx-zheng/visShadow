package util;

import za.co.luma.geom.Vector3DDouble;

public class ColorUtil{

  public static Vector3DDouble LabtoXYZ(double l,double a,double b){
    double xn = 0.9505;
    double yn = 1.0;
    double zn = 1.089;
    double p = (l+16.0)/116.0;
    double x = xn * Math.pow(p+a/500.0,3.0);
    double y = yn * Math.pow(p,3.0);
    double z = zn * Math.pow((p-b/200.0),3.0);
    return new Vector3DDouble(x,y,z);
  }

  public static Vector3DDouble XYZtoRGB(Vector3DDouble xyz){
//    mat3 xyz2rgb = mat3(3.240479, -0.969256, 0.055648,
//            -1.53715, 1.875991, -0.204043,
//            -0.498535, 0.041556, 1.057311);
    //return xyz2rgb * xyz;
    return new Vector3DDouble(3.240479 * xyz.x -1.53715 * xyz.y + -0.498535 * xyz.z ,
        -0.969256 * xyz.x + 1.875991 * xyz.y -0.041556 * xyz.z,
        0.055648 * xyz.x  -0.204043 * xyz.y + 1.057311 * xyz.z);
  }
  
  public static Vector3DDouble RGBnonlinearRGB(Vector3DDouble rgb){
    double rd, gd, bd;
    if (rgb.x < 0.018) rd = rgb.x * 4.5;
    else rd = 1.099 * Math.pow(rgb.x, 0.45) - 0.099;
    if (rgb.y < 0.018) gd = rgb.y * 4.5;
    else gd = 1.099 * Math.pow(rgb.y, 0.45) - 0.099;
    if (rgb.z < 0.018) bd = rgb.z * 4.5;
    else bd = 1.099 * Math.pow(rgb.z, 0.45) - 0.099;
    return new Vector3DDouble(rd, gd, bd);
  }
  
  public static Vector3DDouble LabtoRGB(double l,double a,double b){
    return RGBnonlinearRGB(XYZtoRGB(LabtoXYZ(l,a,b)));
  }
}
