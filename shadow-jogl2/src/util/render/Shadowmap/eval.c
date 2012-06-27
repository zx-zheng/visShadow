#version 400 core

layout(quads, equal_spacing, cw) in;
	
uniform mat4 model;
uniform sampler2D weatherTex;
				 uniform int divide;
				 uniform float border;
				 uniform int offheight = 1;
				 uniform float heightscale = 0.05;
				 uniform int stage = 0;

in ctrl{			  
  vec3 Normal;
  vec2 Area;
} Ctrl[];
		
out vec4 Position;
out vec2 texcoord0;
out vec2 Area;
out int hswitch;

vec2 possion64[64] = 
  vec2[](
	 vec2(-0.934812, 0.366741), vec2(-0.918943, -0.0941496), vec2(-0.873226, 0.62389),
	 vec2(-0.8352, 0.937803), vec2(-0.822138, -0.281655), vec2(-0.812983, 0.10416), 
	 vec2(-0.786126, -0.767632), vec2(-0.739494, -0.535813), vec2(-0.681692, 0.284707),
	 vec2(-0.61742, -0.234535), vec2(-0.601184, 0.562426), vec2(-0.607105, 0.847591),
	 vec2(-0.581835, -0.00485244), vec2(-0.554247, -0.771111), vec2(-0.483383, -0.976928),
	 vec2(-0.476669, -0.395672), vec2(-0.439802, 0.362407), vec2(-0.409772, -0.175695),
	 vec2(-0.367534, 0.102451), vec2(-0.35313, 0.58153), vec2(-0.341594, -0.737541),
	 vec2(-0.275979, 0.981567), vec2(-0.230811, 0.305094), vec2(-0.221656, 0.751152), 
	 vec2(-0.214393, -0.0592364), vec2(-0.204932, -0.483566), vec2(-0.183569, -0.266274),
	 vec2(-0.123936, -0.754448), vec2(-0.0859096, 0.118625), vec2(-0.0610675, 0.460555),
	 vec2(-0.0234687, -0.962523), vec2(-0.00485244, -0.373394), vec2(0.0213324, 0.760247), 
	 vec2(0.0359813, -0.0834071), vec2(0.0877407, -0.730766), vec2(0.14597, 0.281045),
	 vec2(0.18186, -0.529649), vec2(0.188208, -0.289529), vec2(0.212928, 0.063509),
	 vec2(0.23661, 0.566027), vec2(0.266579, 0.867061), vec2(0.320597, -0.883358),
	 vec2(0.353557, 0.322733), vec2(0.404157, -0.651479), vec2(0.410443, -0.413068), 
	 vec2(0.413556, 0.123325), vec2(0.46556, -0.176183), vec2(0.49266, 0.55388),
	 vec2(0.506333, 0.876888), vec2(0.535875, -0.885556), vec2(0.615894, 0.0703452),
	 vec2(0.637135, -0.637623), vec2(0.677236, -0.174291), vec2(0.67626, 0.7116),
	 vec2(0.686331, -0.389935), vec2(0.691031, 0.330729), vec2(0.715629, 0.999939),
	 vec2(0.8493, -0.0485549), vec2(0.863582, -0.85229), vec2(0.890622, 0.850581),
	 vec2(0.898068, 0.633778), vec2(0.92053, -0.355693), vec2(0.933348, -0.62981),
	 vec2(0.95294, 0.156896));

vec4 interpolate4(in vec4 v0, in vec4 v1, in vec4 v2, in vec4 v3)
{
  vec4 a = mix(v0, v1, gl_TessCoord.x);
  vec4 b = mix(v3, v2, gl_TessCoord.x);
  return mix(a, b, gl_TessCoord.y);
}

float heightswitch(float x, float y,int rsl, int r, int d, float ratio){
  int posx = int(x * rsl), posy = int(y * rsl);
  int rposx = posx % int((2*r+d)), rposy = posy % int((2*r+d)*ratio);
  int index =  (posx / int((2*r+d))*13 + posy / int((2*r+d)*ratio)*19) % 64;
  vec2 center = vec2(r+d/2, ratio*(r+d/2))+possion64[index]*0.5*d;
  //float dist = distance(center, vec2(rposx,rposy));
  float dist = pow(center.x-rposx,2)+pow(center.y-rposy,2)/pow(ratio,2);
  vec4 wdata = texture(weatherTex,
		       vec2((float(posx / int((2*r+d)))*int((2*r+d))+r+d/2)/float(rsl),
			    (float(posy / int((2*r+d)))*int((2*r+d))+r+d/2)/float(rsl)));
  float rlocal = min(r+d/2,sqrt(pow(wdata.z, 2) + pow(wdata.w, 2)) * heightscale*300);
  ///*
  if (offheight == 0){
    //return 0;
    if(dist>rlocal*rlocal){
    //turn off cylinder    
      hswitch = 0;
      return 5;
    }else{
    //turn on cylinder
      hswitch = 1;
      return 5.2* 1;//* 0+5;
    }
  }
  //*/
  return dist>rlocal*rlocal?0:0.5 * 1;
  //return rposx;
}
		  
void main(){
  mat4 normalmat = transpose(inverse(model));
  vec3 normal = (normalmat * vec4(0, 0, 1, 1)).xyz;
  gl_Position = interpolate4(gl_in[0].gl_Position,
			     gl_in[1].gl_Position, 
			     gl_in[2].gl_Position,
			     gl_in[3].gl_Position);

  float x=((gl_TessCoord.x + Ctrl[0].Area)/divide).x;
  float y=(1-(gl_TessCoord.y + Ctrl[0].Area)/divide).y;

  vec4 wdata = texture(weatherTex,vec2(x,y));
  float height = sqrt(pow(wdata.z, 2) + pow(wdata.w, 2)) * heightscale;

  float ratio = 201/145;
  ///*
  if(true){
    height *= heightswitch(x, y, 1024*64, int(200), 1200, 201.0/145.0);
  }else{
    height*=1;
    hswitch = 1;
  }
  //*/
  if(border > y)
    height = 0;
  gl_Position.xyz +=normal * height;
  texcoord0 = gl_TessCoord.xy;
  Area = Ctrl[0].Area;
}

