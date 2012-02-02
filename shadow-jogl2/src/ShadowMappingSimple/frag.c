#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform int maxlight;
uniform sampler2DArray shadowmap;

in geom{
  vec4 worldpos;
  vec3 normal;
}Geom;

void main(){
  mat4 bias = mat4(0.5, 0, 0, 0,
		   0, 0.5, 0, 0,
		   0, 0, 0.5, 0,
		   0.5, 0.5, 0.5, 1);
  float shadow, shade, octest;
  vec3 color;
  vec2 crdtmp;
  vec4 tmp;
  for (int i = 0; i < maxlight; ++i){
    vec4 posfromlightworld = lightsview[i] * Geom.worldpos;
    vec4 posfromlight = bias * lightsproj[i] * posfromlightworld;
    posfromlight /= posfromlight.w;
    float occluder = texture(shadowmap, vec3(posfromlight.xy, i)).x;
    float shadowtmp = occluder < abs(posfromlightworld.z) - 0.02? 0:1.0;
    vec3 lightvec = normalize((inverse(lightsview[i]) * vec4(0,0,0,1)).xyz 
			      - Geom.worldpos.xyz/Geom.worldpos.w);
    float colortmp = max(0, dot(Geom.normal, lightvec));
    shade += colortmp;
    shadow += shadowtmp;
  }   
  shade /= maxlight;
  shadow /= maxlight;

  Color = vec4(vec3(shade * shadow) , 1);
  //Color = vec4(1);
}
