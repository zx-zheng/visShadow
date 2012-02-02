package util.loader;

import java.nio.*;

public class Load2Dfloat extends Loader{

  public int width, height;
  
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

  @Override
  public void readheader(){
    width = scanner[0].nextInt();
    height = scanner[0].nextInt();
  }
  
  public void readheader(int i) {
    width = scanner[i].nextInt();
    height = scanner[i].nextInt();
  }

}
