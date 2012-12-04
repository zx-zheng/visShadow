#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform vec3 inColor = vec3(1);

in geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 screentexcoord;
  vec3 shadeColor;
}Geom;

void main(){
  Color = vec4(inColor * Geom.shadeColor * 0.7 + 0.3, 1);
}
