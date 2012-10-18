#version 400 core

layout(vertices = 4) out;

uniform int tesslevel = 1;

in vert{
  vec3 Normal;
  vec2 Area;
}Vert[];

out ctrl{
  vec3 Normal;
  vec2 Area;
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
 
  Ctrl[gl_InvocationID].Normal = Vert[gl_InvocationID].Normal;
  Ctrl[gl_InvocationID].Area = Vert[gl_InvocationID].Area;
}
