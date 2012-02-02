#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform int divide,shadowswitch;
uniform ivec2 lightcount_real_virtual;
uniform sampler2DArray shadowmap;
uniform sampler2D weatherTex;

in geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 Area;
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

vec4 XYZtoRGB(float x,float y,float z){
  float r = 3.240479*x-1.53715*y-0.498535*z;
  float g = -0.969256*x+1.875991*y+0.041556*z;
  float b = 0.055648*x-0.204043*y+1.057311*z;
  return vec4(r,g,b,1.0);
}

vec4 RGBnonlinearRGB(float r,float g,float b){
  float rd,gd,bd;
  if(r<0.018) rd = r*4.5;
  else rd = 1.099*pow(r,0.45)-0.099;
  if(g<0.018) gd = g*4.5;
  else gd = 1.099*pow(g,0.45)-0.099;
  if(b<0.018) bd = b*4.5;
  else bd = 1.099*pow(b,0.45)-0.099;
  return vec4(rd,gd,bd,1.0);
}

vec4 valuetocolor(float value){
  float lab_a = 0.60, lab_b = 0.6, lab_c = 0.6, lab_d = 0.6;
  float labangle = radians(lab_a*100.0+(270.0-lab_a*100.0+(lab_b-0.5)*100.0)*(1.0-value));
  vec3 xyz = LabtoXYZ(lab_d*100.0,lab_c*100.0*cos(labangle),lab_c*100.0*sin(labangle));
  vec4 rgb = XYZtoRGB(xyz.r,xyz.g,xyz.b);
  return RGBnonlinearRGB(rgb.r,rgb.g,rgb.b);
}

void main(){
  mat4 bias = mat4(0.5, 0, 0, 0,
		   0, 0.5, 0, 0,
		   0, 0, 0.5, 0,
		   0.5, 0.5, 0.5, 1);
  float shadow, shade, octest, count;
  for (int i = 0; i < lightcount_real_virtual.x; ++i){
    vec4 posfromlightworld = lightsview[i] * Geom.worldpos;
    vec4 posfromlight = bias * lightsproj[i] * posfromlightworld;
    posfromlight /= posfromlight.w;
    if(posfromlight.x > 0 && posfromlight.x < 1 
       && posfromlight.y > 0 && posfromlight.y < 1 && posfromlightworld.z < 0){
      float occluder = texture(shadowmap, vec3(posfromlight.xy, i)).x;
      float shadowtmp = occluder < abs(posfromlightworld.z) - 0.02? 0:1.0;
      vec3 lightvec = normalize((inverse(lightsview[i]) * vec4(0,0,0,1)).xyz 
				- Geom.worldpos.xyz/Geom.worldpos.w);
      float shadetmp = max(0, dot(Geom.normal, lightvec));
      shade += shadetmp;
      shadow += shadowtmp;
      count += 1;
    }
  }   
  shade /= ( count);
  shadow /= ( count);

  float temperature = 
    texture(weatherTex, 
	    vec2(((Geom.texcoord0 + Geom.Area)/divide).x,
		 (1-(Geom.texcoord0 + Geom.Area)/divide).y)).y;
  temperature = (temperature - 273)/25.0;
  vec3 color = valuetocolor(temperature).xyz;

  if(shadowswitch == 1)shadow = 1;

  //Color = vec4(color * (shade * (0.3 * shadow + 0.4) + 0.3) , 1);
  //Color = vec4(color * (shade * (0.6) + 0.4 - (1-shadow)*0.4 * sin(3.14/2*shade)) , 1);
  Color = vec4(color * (0.3 + shadow * 0.7 *shade + (1-shadow) * 0.2 * pow(shade, 1)), 1);
  //Color = vec4(count/10);
  //Color = vec4(shade);
}
