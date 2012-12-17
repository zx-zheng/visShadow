#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform vec3 inColorLab;
uniform sampler2D shadowTex;
uniform vec2 shadowTexCoordSize;
uniform int shadowRange = 10;

in geom{
  vec3 normal;
  vec2 texcoord0;
  vec2 screentexcoord;
  vec3 color;
}Geom;

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
  vec2 shadowTexCoord = Geom.screentexcoord * 0.5 + 0.5;
  shadowTexCoord.y = 1 - shadowTexCoord.y;
  shadowTexCoord *= shadowTexCoordSize;
  float shadow =  texture(shadowTex, shadowTexCoord).x;
  Color =
    vec4
    (RGBnonlinearRGB
	 (XYZtoRGB
	  (LabtoXYZ
	   (inColorLab.x - shadowRange * (1 - shadow), 
	    inColorLab.y, 
	    inColorLab.z))), 1);
}
