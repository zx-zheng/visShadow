package util.loader;

import java.nio.*;

public class Load2Dfloat extends Loader{

  public int width, height;
  
  final static float TEMP_MAX = 313, TEMP_MIN = 235;
  
  public Load2Dfloat(String filepath){
    super(filepath);
    scanner[0].useDelimiter(",\\s+|\\s+|E\\+|#\\s");
    readheader();
    buffer = ByteBuffer.allocate(4 * width * height);
    if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)){
      buffer.order(ByteOrder.LITTLE_ENDIAN);
    }
  } 
  
  public Load2Dfloat(String filepath0,
      String filepath1,String filepath2,String filepath3){
    super(filepath0, filepath1, filepath2, filepath3);
    for(int i = 0; i < 4; i++){
      scanner[i].useDelimiter(",\\s+|\\s+|E\\+|#\\s");
      readheader(i);
      }
    buffer = ByteBuffer.allocate(file.length * 4 * width * height);
    if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)){
      buffer.order(ByteOrder.LITTLE_ENDIAN);
    }
  }
  
  
  @Override
  public void loadOffsetLineValue
  (int lineoffset, float valueoffset) {
    // TODO Auto-generated method stub
    while(scanner[0].hasNext()){
      for(int i = 0; i < file.length; i++){   
        for(int j = 0; j < lineoffset; j++){
          scanner[i].next();
        }
        buffer.putFloat(scanner[i].nextFloat() - valueoffset);
        scanner[i].nextLine();
      }
    }
  }
  
  public void normalize(int index){
    float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
    for(int i= 0; i < width * height; i++){
      
      float value = buffer.getFloat(4 * i * file.length + (4 * index));
      if(value > max){
        max = value;
      }
      if(value < min){
        min = value;
      }
    }
    
    //System.out.println(max + " " + min);
    
    double a = 1 / (max - min);
    double b = -min / (max - min);
    for(int i= 0; i<buffer.capacity() / file.length / 4; i++){
      int index2 = 4 * i * file.length + (4 * index);
      float value = buffer.getFloat(index2);
      value = (float) (a * value + b);
      buffer.putFloat(index2, value);
      
    }
  }
  
  public void normalizeTemp(){
    float max = TEMP_MAX; 
    float min = TEMP_MIN;
 
    int index = 0;
    
    double a = 1 / (max - min);
    double b = -min / (max - min);
    for(int i= 0; i<buffer.capacity() / file.length / 4; i++){
      int index2 = 4 * i * file.length + (4 * index);
      float value = buffer.getFloat(index2);
      value = (float) (a * value + b);
      buffer.putFloat(index2, value);
      
    }
  }

  @Override
  public void readheader(){
    width = scanner[0].nextInt();
    height = scanner[0].nextInt();
  }
  
  public void readheader(int i) {
    width = scanner[i].nextInt();
    height = scanner[i].nextInt();
  }

  
  public static ByteBuffer constructByteBuffer(ByteBuffer[] buffer){
    ByteBuffer result = ByteBuffer.allocate(buffer[0].capacity()*4);
    if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)){
      result.order(ByteOrder.LITTLE_ENDIAN);
    }
    for(int i = 0; i < buffer.length; i++){
      buffer[i].rewind();
    }
    for(int i = 0; i < buffer[0].capacity()/4; i++){
      for(int j = 0; j < buffer.length; j++){
        result.putFloat(buffer[j].getFloat());
      }
    }
    result.rewind();
    return result;
  }

  public float getfloat(int index){
    return buffer.getFloat(index*4);
  }
  
}
