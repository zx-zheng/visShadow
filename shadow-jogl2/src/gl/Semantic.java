package gl;

public interface Semantic{
  
  //vertex attribute index
  int VERT_POS_INDX = 0;
  int VERT_NORM_INDX = 1;
  int VERT_COLOR_INDX = 2;
  int VERT_TEXCOORD0_INDX = 3;
  int VERT_TEXCOORD1_INDX = 4;
  int VERT_TEXCOORD2_INDX = 5;
  int VERT_AREA_INDX = 6;
  
  //vertex attribute size
  int VERT_POS_SIZE = 3;
  int VERT_NORM_SIZE = 3;
  int VERT_COLOR_SIZE = 3;
  int VERT_TEXCOORD_SIZE = 2;
  int VERT_AREA_SIZE = 2;
  
  //type size
  int BYTE_SIZE = 1;
  int SHORT_SIZE = 2;
  int INT_SIZE = 4;
  int LONG_SIZE = 8;
  
  int FLOAT_SIZE = 4;
  int DOUBLE_SIZE = 8;
  
  int CHAR_SIZE = 2;
  
}
