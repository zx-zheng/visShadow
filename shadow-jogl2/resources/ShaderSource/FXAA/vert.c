#version 400 core

#define ATTR_POSITION 0
#define ATTR_NORMAL 1
#define ATTR_COLOR 2
#define ATTR_TEXCOORD0_INDX 3
#define ATTR_AREA 6

layout(location = ATTR_POSITION) in vec3 vertex;
layout(location = ATTR_TEXCOORD0_INDX) in vec2 texcoord;

uniform float FXAA_SUBPIX_SHIFT = 0;//1.0 / 4.0;

out vert{
  vec4 texcoord;
}Vert;

void main(){
  mat4 mat = mat4(1, 0, 0, 0,
		  0, 1, 0, 0,
		  0, 0, 1, 0,
		  0, 0, 0, 1);
  gl_Position = mat * vec4(vertex, 1.0);
  Vert.texcoord.xy = texcoord;
  Vert.texcoord.zw = texcoord - vec2(0.5/1024.0 + FXAA_SUBPIX_SHIFT);
}
