#version 400 core

#define ATTR_POSITION 0
#define ATTR_NORMAL 1
#define ATTR_COLOR 2
#define ATTR_TEXCOORD0_INDX 3
#define ATTR_AREA 6

uniform mat4 model;
uniform vec3 inColorLab; 

layout(location = ATTR_POSITION) in vec3 vertex;
layout(location = ATTR_COLOR) in vec4 Color;
layout(location = ATTR_TEXCOORD0_INDX) in vec2 texcoord0in;
layout(location = ATTR_NORMAL) in vec3 Normalin;
layout(location = ATTR_AREA) in vec2 Areain;


out vec3 Normal;
out vec2 texcoord0;
out vec3 color;

vec3 LabtoXYZ(float l,float a,float b){
  float xn = 0.9505;
  float yn = 1.0;
  float zn = 1.089;
  float p = (l+16.0)/116.0;
  float x = xn * pow(p+a/500.0,3.0);
  float y = yn * pow(p,3.0);
  float z = zn * pow((p-b/200.0),3.0);
  return vec3(x,y,z);
}

vec3 XYZtoRGB(vec3 xyz){
  mat3 xyz2rgb = mat3(3.240479, -0.969256, 0.055648,
		      -1.53715, 1.875991, -0.204043,
		      -0.498535, 0.041556, 1.057311);
  return xyz2rgb * xyz;
}

vec3 RGBnonlinearRGB(vec3 rgb){
  float rd,gd,bd;
  if(rgb.r<0.018) rd = rgb.r*4.5;
  else rd = 1.099*pow(rgb.r,0.45)-0.099;
  if(rgb.g<0.018) gd = rgb.g*4.5;
  else gd = 1.099*pow(rgb.g,0.45)-0.099;
  if(rgb.b<0.018) bd = rgb.b*4.5;
  else bd = 1.099*pow(rgb.b,0.45)-0.099;
  return vec3(rd,gd,bd);
}

void main(){
  gl_Position = model * vec4(vertex, 1.0);
  texcoord0 = texcoord0in;
  
}
