#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;


uniform sampler2D billBoardTex;

in geom{
  vec3 normal;
  vec2 texcoord0;
  vec2 screentexcoord;
}Geom;

void main(){
Color = texture(billBoardTex, Geom.texcoord0);
}
