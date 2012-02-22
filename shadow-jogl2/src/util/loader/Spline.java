package util.loader;

import java.nio.*;
import java.io.*;

public class Spline{
  int width, height, div;
  FloatBuffer data;
  ByteBuffer result;
  float[][] k;
  int count = 0;

  public Spline(ByteBuffer data, int width, int height, int div){
    this.width = width;
    this.height = height;
    this.div = div;
    this.data = data.asFloatBuffer();
    k = new float[width * (height - 1)][4];
  }

  public void setdiv(int div){
    this.div = div;
  }
  
  public ByteBuffer getSpline(){
    return getSpline(div);
  }
  
  public ByteBuffer getSpline(int div){
    this.div = div;
    result = ByteBuffer.allocate(((width - 1) * div + 1) * ((height - 1)* div + 1) * 4);
    if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)){
      result.order(ByteOrder.LITTLE_ENDIAN);
    }
    spline_row();
    interpolate(div);
    result.rewind();
    return result;
  }
  
  public int width(){
    return ((width - 1) * div + 1);
  }
  
  public int height(){
    return ((height - 1)* div + 1);
  }

  /*列の補間 曲線の方程式を求める*/
  private void spline_row(){
    float[] a = new float[4];
    for(int j = 0; j < width; j++){
      //最初
      a[0] = a[1] = data.get(width * 0 +  j);
      a[2] = data.get(width * 1 + j);
      a[3] = data.get(width * 2 + j);
      spline_f(a, j, 0);
      for(int i = 1; i < height - 2; i++){
        //ある点の一つ前からの４点を取り出し、補間する値とする
        a[0] = data.get(width * (i - 1) + j);
        a[1] = data.get(width * (i - 0) + j);
        a[2] = data.get(width * (i + 1) + j);
        a[3] = data.get(width * (i + 2) + j);
        spline_f(a, j , i);
      }
      //最後
      a[0] = data.get(width * (height - 3) + j);
      a[1] = data.get(width * (height - 2) + j);
      a[2] = a[3] = data.get(width * (height - 1) + j);
      spline_f(a, j, height - 2);
    }
  }

  /*列の補間で得られた曲線の方程式から列の補間値を求め
    行の補間をする。曲線の方程式を求めてから、値を求める。*/
  private void interpolate(int div){
    float tmp = 1;
    float[] a = new float[4];
    float[] c = new float[4];
    for(int i = 0; i < height - 1; i++){
      for(int t = 0; t < div; t++){
        //行の先頭の処理
        a[0] = a[1] =  get_interpolated(k[0 * (height - 1) + i], (float)t/(float)div);
        a[2] = get_interpolated(k[1 * (height - 1) + i], (float)t/(float)div);
        a[3] = get_interpolated(k[2 * (height - 1) + i], (float)t/(float)div);  
        //列の補間値から行の曲線を求める
        get_spline_f(a, c); 
        //求めた曲線で最終的な補間値を計算
        for (int s = 0; s < div; s++){
          result.putFloat(get_interpolated(c , (float)s/(float)div));
        } 
        //以降も同様に
        //行の最後の点より2個前の点まで計算する
        for(int j = 1; j < width - 2; j++){ 
          a[0] = a[1];
          a[1] = a[2];
          a[2] = a[3];
          a[3] = get_interpolated(k[(j + 2) * (height - 1) + i], (float)t/(float)div);
          get_spline_f(a, c);
          for (int s = 0; s < div; s++){
//            if(tmp == get_interpolated(c, (float)s/(float)div))
//              System.out.println("same");
            result.putFloat(get_interpolated(c, (float)s/(float)div));
            tmp = get_interpolated(c, (float)s/(float)div);
            //System.out.println(get_interpolated(c, (float)s/(float)div));
          }   
        }
        //行の最後
        a[0] = a[1];
        a[1] = a[2];
        a[2] = a[3];
        get_spline_f(a, c);
        for(int s = 0; s <= div; s++){
          result.putFloat(get_interpolated(c, (float)s/(float)div));
        }
      }
    }
    //最後の行
    //行の最初
    a[0] = a[1] = data.get(width * (height - 1) + 0);
    a[2] = data.get(width * (height - 1) + 1);
    a[3] = data.get(width * (height - 1) + 2);
    get_spline_f(a, c);
    for(int s = 0; s < div; s++){
      result.putFloat(get_interpolated(c, (float)s/(float)div));
      //count++;
    }
    for(int j = 1; j < width - 2; j++){
      a[0] = a[1];
      a[1] = a[2];
      a[2] = a[3];
      a[3] = data.get(width * (height - 1) + j + 2);
      get_spline_f(a, c);
      for(int s = 0; s < div; s++){
        result.putFloat(get_interpolated(c, (float)s/(float)div));
        //System.out.println(get_interpolated(c, (float)s/(float)div));
        //count++;
      }
    }
    //行の最後
    a[0] = a[1];
    a[1] = a[2];
    a[2] = a[3];
    for(int s = 0; s <= div; s++){
      result.putFloat(get_interpolated(c, (float)s/(float)div));
      //count++;
    }
    //System.out.println(count);
  }

  private float get_interpolated(float[] c, float x){
    return c[0] + c[1] * x + c[2] * x * x + c[3] * x * x * x;
  }

  /*3次スプライン補間の補間曲線の係数を求める
    a: 4点のy座標
    k: 3次関数の係数
    y=a+bx+cx^2+dx^3 のとき
    f[wh][0] == a
    f[wh][1] == b
    f[wh][2] == c
    f[wh][3] == d
   */
  private void spline_f(float[] a, int w, int h){
    float[] alpha = new float[4], l = new float[4],
        mu = new float[4], z = new float[4], b = new float[4],
        c = new float[4], d = new float[4];

    for(int i = 1; i < 3; i++){
      alpha[i] = 3f * (a[i + 1] - a[i]) - 3f * (a[i] - a[i - 1]);
    }
    l[0] = 1f; mu[0] = 0; z[0] = 0;
    for(int i = 1; i < 3; i++){
      l[i] = 4f - mu[i-1];
      mu[i] = 1f / l[i];
      z[i] = (alpha[i] - z[i-1]) / l[i];
    }
    l[3] = 1f;
    z[3] = 0;
    c[3] = 0;
    for(int j = 2; 0 <= j; j--){
      c[j] = z[j] - mu[j] * c[j+1];
      b[j] = a[j+1] - a[j] - (c[j+1] + 2f * c[j]) / 3f;
      d[j] = (c[j+1] - c[j]) / 3f;
    }
    k[(height - 1) * w + h][0] = a[1];
    k[(height - 1) * w + h][1] = b[1];
    k[(height - 1) * w + h][2] = c[1];
    k[(height - 1) * w + h][3] = d[1];
  }

  private void get_spline_f(float[] a, float[] dst){
    float[] alpha = new float[4], l = new float[4],
        mu = new float[4], z = new float[4], b = new float[4],
        c = new float[4], d = new float[4];

    for(int i = 1; i < 3; i++){
      alpha[i] = 3f * (a[i + 1] - a[i]) - 3f * (a[i] - a[i - 1]);
    }
    l[0] = 1f; mu[0] = 0; z[0] = 0;
    for(int i = 1; i < 3; i++){
      l[i] = 4f - mu[i-1];
      mu[i] = 1f / l[i];
      z[i] = (alpha[i] - z[i-1]) / l[i];
    }
    l[3] = 1f;
    z[3] = 0;
    c[3] = 0;
    for(int j = 2; 0 <= j; j--){
      c[j] = z[j] - mu[j] * c[j+1];
      b[j] = a[j+1] - a[j] - (c[j+1] + 2f * c[j]) / 3f;
      d[j] = (c[j+1] - c[j]) / 3f;
    }
    dst[0] = a[1];
    dst[1] = b[1];
    dst[2] = c[1];
    dst[3] = d[1];
  }


  public void saveFile(int width, int height, String name){
    result.rewind();
    try{
      File file = new File(name);

      PrintWriter pw = new PrintWriter(
          new BufferedWriter(new FileWriter(file)));
      for(int j = 0; j < height; j++){
        for(int i = 0; i < width; i++){
          pw.printf("%4d    %4d    %10f\n",
              i, j, result.getFloat((width * j + i) * 4));
        }
        pw.println();
      }
      pw.close();
    }catch(IOException e){
      System.out.println(e);
    }
  }

  private static boolean checkBeforeWritefile(File file){
    if (file.exists()){
      if (file.isFile() && file.canWrite()){
        return true;
      }
    }   
    return false;
  }
}
