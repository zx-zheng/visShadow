#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform mat4 viewproj[4];
uniform float alpha = 1;
uniform sampler2D billBoardTex;

in geom{
  vec3 normal;
  vec2 texcoord0;
  vec2 screentexcoord;
}Geom;

void main(){
  Color = texture(billBoardTex, Geom.texcoord0);
  Color.w *= alpha;
  //Color = vec4(0,0,0,1);
}
