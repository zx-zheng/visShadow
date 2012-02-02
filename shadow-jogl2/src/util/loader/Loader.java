package util.loader;

import java.io.*;
import java.nio.*;
import java.util.Scanner;

public abstract class Loader {
  File[] file;
  ByteBuffer buffer;
  Scanner[] scanner;
  
  public Loader(String filepath){
    try{
      this.file = new File[1]; 
      file[0] = new File(filepath);
      scanner = new Scanner[1];
      scanner[0] = new Scanner(file[0]);
    }catch(FileNotFoundException e){
      System.out.println(e);
    }
  }
  
  public Loader(String filepath0,
      String filepath1,String filepath2,String filepath3){
    this.file = new File[4]; 
    scanner = new Scanner[4];
    try{
      file[0] = new File(filepath0);
      file[1] = new File(filepath1);
      file[2] = new File(filepath2);
      file[3] = new File(filepath3);
      for(int i = 0; i < 4; i++){
        scanner[i] = new Scanner(file[i]);
      }
    }catch(FileNotFoundException e){
      System.out.println(e);
    }
  }

  abstract public void loadOffsetLineValue
  (int lineoffset, float value);

  public void loadOffsetLine(int offset){
    loadOffsetLineValue(offset, 0);
  }
  
  public void loadOffsetValue(float offset){
    loadOffsetLineValue(0, offset);
  }
  
  public void load(){
    loadOffsetLineValue(0, 0);
  }
  
  abstract public void readheader();
  public ByteBuffer getbuffer(){
    buffer.rewind();
    return buffer;
  }
}
