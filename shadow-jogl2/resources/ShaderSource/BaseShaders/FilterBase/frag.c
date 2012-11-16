#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform sampler2D target;

in vert{
  vec2 texcoord;
}Vert;

void main(){
  Color = texture(target, Vert.texcoord);
}
