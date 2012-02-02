#version 400 core

#define ATTR_POSITION 0
#define ATTR_NORMAL 1
#define ATTR_COLOR 2
#define ATTR_TEXCOORD0_INDX 3
#define ATTR_AREA 6

uniform mat4 model; 

layout(location = ATTR_POSITION) in vec3 vertex;
layout(location = ATTR_COLOR) in vec4 Color;
layout(location = ATTR_NORMAL) in vec3 Normal;
layout(location = ATTR_AREA) in vec2 Area;

out vert{
  vec3 Normal;
  vec2 Area;
}Vert;

void main(){
  gl_Position = model * vec4(vertex, 1.0);
  Vert.Area = Area;
}
