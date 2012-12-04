#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform vec4 lightscolor[10];
uniform vec3 lightPos[10];
uniform ivec4 lightsattr[10];
uniform int divide,shadowswitch,colorset;
uniform ivec2 lightcount_real_virtual;
uniform sampler2DArray shadowmap;
uniform sampler2D weatherTex, mapTex, billBoardTex;
uniform float aspect_Y;
uniform int mapscaling = 50;
uniform float mapoffsetx, mapoffsety, viewoffsetx, viewoffsety;
uniform float viewscaling = 1;

in geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 Area;
  vec2 screentexcoord;
  flat int hswitch;
}Geom;



vec2 maptexcoordtransform(vec2 texcoord, float scale, 
			  vec2 offset, vec2 viewoffset){
  vec2 aspect = vec2(1, aspect_Y);
  float scaling = mapscaling * 0.02;
  //scaling = 1;
  return texcoord  * aspect * 0.5 * scaling * viewscaling 
    + offset + viewoffset * scaling + vec2(0.5);
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

  if(colorset==1){
    Color=vec4(1,1,0,1);
    //Color=vec4(Geom.worldpos.z);
    Color=vec4(vec3(dot(Geom.normal,vec3(0,0,1))),1);
    return;
  }

  float uplightshade = dot(Geom.normal, vec3(0,0,1));

  if(shadowswitch == 0)shadow = 1;

  Color = vec4(Geom.normal, 1);
  //Color = vec4(0.7) + vec4(0.3) * shadow;
  //Color = vec4(0);
  //Color = texture(billBoardTex, Geom.texcoord0);
  //Color = vec4(1,1,0,1);
}
