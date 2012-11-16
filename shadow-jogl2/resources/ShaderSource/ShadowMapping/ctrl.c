#version 400 core

layout(vertices = 4) out;

uniform int tesslevel;

in vec3 Normal[];
in vec2 Area[];

out ctrl{
  vec3 Normal;
  vec2 Area;
  //vec2 texcoord0;
}Ctrl[];

void main(){
  int id = gl_InvocationID;
  gl_TessLevelInner[0] = tesslevel;
  gl_TessLevelInner[1] = tesslevel;
  gl_TessLevelOuter[0] = tesslevel;
  gl_TessLevelOuter[1] = tesslevel;
  gl_TessLevelOuter[2] = tesslevel;
  gl_TessLevelOuter[3] = tesslevel;
  gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
 
  Ctrl[gl_InvocationID].Normal = Normal[gl_InvocationID];
  Ctrl[gl_InvocationID].Area = Area[gl_InvocationID];
  //Ctrl[gl_InvocationID].texcoord0 = Vert[gl_InvocationID].texcoord0;
}
