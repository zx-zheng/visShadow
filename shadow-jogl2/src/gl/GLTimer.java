package gl;

import javax.media.opengl.*;

public class GLTimer{
  
  int query[] = new int[1];
  int available[] = new int[1];
  int time[] = new int[1];
  long sum;
  int count;
  
  public void init(GL2GL3 gl){
    gl.glGenQueries(1, query, 0);
    gl.glBeginQuery(GL2.GL_TIME_ELAPSED, query[0]);
    gl.glEndQuery(GL2.GL_TIME_ELAPSED);
  }
  
  public void start(GL2GL3 gl){
    gl.glGetQueryObjectiv(query[0], GL2.GL_QUERY_RESULT_AVAILABLE, available, 0);
    if(available[0]!=0){
      gl.glGetQueryObjectuiv(query[0], GL2.GL_QUERY_RESULT, time, 0);
      gl.glBeginQuery(GL2.GL_TIME_ELAPSED, query[0]);
    }
  }
  
  public int stop(GL2GL3 gl){
    if(available[0]!=0){
      gl.glEndQuery(GL2.GL_TIME_ELAPSED);
      count++;
      sum+=time[0];
      return time[0];
    }
    return -1;
  }
  
  public double averagemstime(){
    return sum/(count*1000000);
  }
}
