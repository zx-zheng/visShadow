#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform vec4 lightscolor[10];
uniform ivec4 lightsattr[10];
uniform int divide,shadowswitch,colorset;
uniform ivec2 lightcount_real_virtual;
uniform sampler2DArray shadowmap;
uniform sampler2D weatherTex;
uniform float gamma;
uniform int L, lab_a, lab_b, shaderange, shadowrange;

in geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 Area;
  flat int hswitch;
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

vec3 valuetocolor(float value){
  float lab_a = 0.7, lab_b = 0.5, lab_c = 0.7, lab_d = 0.6;
  float labangle = 
    radians(lab_a*100.0+(270.0-lab_a*100.0+(lab_b-0.5)*100.0)*(1.0-value));
  vec3 xyz = LabtoXYZ(lab_d*100.0,
		      (value-0.5)*30 + 0*lab_c*100.0*cos(labangle),
		      0*lab_c*100.0*sin(labangle));
  vec3 rgb = XYZtoRGB(xyz);
  return RGBnonlinearRGB(rgb);
}

vec3 valuetocolorRG(float value){
  float l = 75, a = (value-0.5)*100*1.4, b = 0;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz))+vec3(0.1,0.1,0);
}

/*-----------------------------------------------------------------*
  Color Red-Green
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRG(float shadow, float value){
  float l = 75 - 40*(1-shadow), a = (value-0.5)*100*1.4, b = 0;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

/*-----------------------------------------------------------------*
  Color Red-Green  Shade Blue-Yellow b
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRGshadeBY(float value, float shadewide, float shadow){
  float l = 75 - 30*(1-shadow), 
    a = (value-0.5)*100*1.4 * 0, b = -shadewide * 0;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

/*-----------------------------------------------------------------*
  Color Red-Green  Shade Blue-Yellow+L
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRGshadeBYL(float value, float shadewide, float shadow){
  float l = L + shadewide *shaderange*1*1 - shadowrange*(1-shadow) * 1, 
    a = (value-0.5)* lab_a * 1, b = 1*lab_b*shadewide * 1;
  vec3 xyz = LabtoXYZ(l,a,b);
  //return vec3(l/100);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

/*-----------------------------------------------------------------*
  
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRGYG(float shadow, float value, float value2){
  float l = 75 - 20*(1-shadow), 
    a = (value-0.5)*100*1.4, b = (value2-0.5)*100*1.4;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
  //return vec3(1);
}

vec3 valuetocolorYB(float value){
  float l = 75, a = 0, b = (value-0.5)*100;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

vec3 valuetoshadowcolorYB(float shadow, float value){
  float l = 75 - 20*(1-shadow), a = 0, b = (value-0.5)*100;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

vec3 shadeYB(float shadewide, vec3 color){
  float blue = 0.15*2, yellow = 0.15*2;
  vec3 kcool = vec3(0,0,blue)+(1-blue)*color;
  vec3 kwarm = vec3(yellow,yellow,0)+(1-yellow)*color;
  vec3 shadecolor = (1-shadewide)*0.5*kcool+(1-(1-shadewide)*0.5)*kwarm;
  return shadecolor;
}

vec3 shadeshadowYB(float shadewide, float shadow, vec3 color){
  float blue = 0.3, yellow = 0.3;
  vec3 kcool = vec3(0,0,blue)+(1-blue)*color;
  vec3 kwarm = vec3(yellow,yellow,0)+(1-yellow)*color;
  vec3 shadecolor = (1-shadewide)*0.5*kcool+(1-(1-shadewide)*0.5)*kwarm;
  return shadecolor;
}

vec3 shadeRG(float shadewide, vec3 color){
  float red = 0.2, green = 0.2;
  vec3 kcool = vec3(red,0,0)+(1-red)*color;
  vec3 kwarm = vec3(0,green,0)+(1-green)*color;
  vec3 shadecolor = (1-shadewide)*0.5*kcool+(1-(1-shadewide)*0.5)*kwarm;
  return shadecolor;
}

/*-----------------------------------------------------------------*
  
 *-----------------------------------------------------------------*/
vec3 valRGshadeshadowYB(float val, float shadewide, float shadow){
  vec3 color = valuetoshadowcolorRG(shadow, val);
  return //shadow*shadeYB(min(shadewide, (shadow-0.5)*2), color)
    shadeshadowYB(shadewide/shadewide, shadow, color);
    //+(1-shadow)*0.5*shadeYB(shadewide, color);
}

/*-----------------------------------------------------------------*
  
 *-----------------------------------------------------------------*/
vec3 valRGYBshadeshadowYB(float val, float val2, float shadewide, float shadow){
  vec3 color = valuetoshadowcolorRGYG(shadow, val, val2);
  return //shadow*shadeYB(min(shadewide, (shadow-0.5)*2), color)
    shadeshadowYB(shadewide, shadow, color);
    //+(1-shadow)*0.5*shadeYB(shadewide, color);
}

vec3 shadecmb(vec3 shade, vec3 shadecw, float shadewide){
  float ratio = pow(1-abs(shadewide),2);
  vec3 color = ratio*0.5*shade+(1-ratio) * shadecw;
  return color;
}

void main(){
  mat4 bias = mat4(0.5, 0, 0, 0,
		   0, 0.5, 0, 0,
		   0, 0, 0.5, 0,
		   0.5, 0.5, 0.5, 1);
  ivec2 offset[9] = ivec2[](ivec2(-1,-1),ivec2(0,-1),ivec2(1,-1),
			    ivec2(-1,0),ivec2(0,0),ivec2(1,0),
			    ivec2(-1,1),ivec2(0,1),ivec2(1,1));
  float shadow, shade=0, octest, count = 0,shadewide=0;
  for (int i = 0; i < lightcount_real_virtual.x; ++i){
    vec4 posfromlightworld = lightsview[i] * Geom.worldpos;
    vec4 posfromlight = bias * lightsproj[i] * posfromlightworld;
    posfromlight /= posfromlight.w;
    if(posfromlight.x > 0 && posfromlight.x < 1 
       && posfromlight.y > 0 && posfromlight.y < 1 && posfromlightworld.z < 0){
      float occluder, shadowtmp=0;
      for(int j = 0; j < offset.length; j++){
	occluder = textureOffset(shadowmap, vec3(posfromlight.xy, i), offset[j]).x;
	shadowtmp += occluder < abs(posfromlightworld.z) - 0.04? 0:1.0 * lightscolor[i].w;
      }
      shadowtmp /= offset.length;
      if(lightsattr[i].x * Geom.hswitch==1 )shadowtmp = 1;
      vec3 lightvec = normalize((inverse(lightsview[i]) * vec4(0,0,0,1)).xyz 
				- Geom.worldpos.xyz/Geom.worldpos.w);
      float shadetmp = max(0, dot(Geom.normal, lightvec))*lightscolor[i].w;
      float shadewidetmp = dot(Geom.normal, lightvec)*lightscolor[i].w;
      shade += shadetmp;
      shadewide += shadewidetmp;
      shadow += shadowtmp;
      count+=lightscolor[i].w;
    }
  }   
  shade /= count;
  shadewide /= count;
  shadow /= count;
  //shadewide = 0;

  vec2 th = 
    texture(weatherTex, 
	    vec2(((Geom.texcoord0 + Geom.Area)/divide).x,
		 (1-(Geom.texcoord0 + Geom.Area)/divide).y)).xy;///10;
  float temperature = th.y;
  float humid = th.x/100;
  ///*
  if(colorset==1){
    Color=vec4(1,1,0,1);
    //Color=vec4(Geom.worldpos.z);
    Color=vec4(vec3(dot(Geom.normal,vec3(0,0,1))),1);
    return;
  }
  //*/
  temperature = (temperature - 273)/25.0;
  vec3 color = valuetocolorYB(temperature).xyz;

  float ratio = 0.5;
  float uplightshade = dot(Geom.normal, vec3(0,0,1));

  if(shadowswitch == 0)shadow = 1;
  //if (Geom.hswitch == 1)shadow = 1;
  
  //Color = vec4(color * (shade * (0.3 * shadow + 0.4) + 0.3) , 1);
  //Color = vec4(color * (shade * (0.6) + 0.4 - (1-shadow)*0.4 * sin(3.14/2*shade)) , 1);
  Color = vec4(color * (0.2 + shadow * 0.8 *shade + (1-shadow) * 0.2 * pow(shade, gamma)), 1);
  Color = (0.2+0.8*shadow)*vec4(shadeRG(shadewide, color), 1);
  Color = vec4(valRGshadeshadowYB(temperature, shadewide, shadow),1);
  Color = vec4(valuetoshadowcolorRGshadeBYL(temperature, shadewide, shadow),1);
  //Color=vec4(lightsattr[1].x);
  //Color = vec4(valRGshadeshadowYB(humid, shadewide, shadow),1);
  //Color = vec4(valRGYBshadeshadowYB(temperature, humid, shadewide, shadow),1);
  //Color = vec4((0.3+0.7*shade*shadow)* valuetocolorRG(temperature),1);
  //vec3 colorkw = shadeRG(shadewide, color);
  //vec3 colorstd = shade * color;
  //Color = (0.8+0.2*shadow)*vec4(shadecmb(colorstd, colorkw, shadewide),1);
  //Color = vec4(color * (0.3 + shadow * 0.7 *shade),1);
}
