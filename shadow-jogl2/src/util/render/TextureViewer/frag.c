#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform sampler2D target;
uniform sampler2DArray targetarray;
uniform int channel, depth, mode;

in vert{
  vec2 texcoord;
}Vert;

void main(){
  if(mode == 0){
    Color = vec4(vec3(texture(target, Vert.texcoord)[channel]), 1);
  }else if(mode == 1){
    Color = 
      vec4(vec3(texture(targetarray, 
			vec3(Vert.texcoord, 4)).y), 1);
  }
}
