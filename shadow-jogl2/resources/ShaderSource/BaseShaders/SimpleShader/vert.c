#version 400 core

#define ATTR_POSITION 0
#define ATTR_NORMAL 1
#define ATTR_COLOR 2
#define ATTR_TEXCOORD0_INDX 3
#define ATTR_AREA 6

uniform mat4 model, view, proj; 

layout(location = ATTR_POSITION) in vec3 vertex;

void main(){
  gl_Position = proj * view * model * vec4(vertex, 1.0);
}
