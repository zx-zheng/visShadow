#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

in geom{
  vec4 worldpos;
  float z;
}Geom;

void main(){
  Color = vec4(abs(Geom.worldpos.z),
	       Geom.z * 0.5 + 0.5, 1, 0);
}
