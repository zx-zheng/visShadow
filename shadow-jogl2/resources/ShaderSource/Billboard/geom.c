#version 400 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

//4 matrices P, Mv, Mvi and Mvit.
uniform mat4 viewproj[4];

in vec3 Normal[];
in vec2 texcoord0[];

out geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 screentexcoord; //(-1,-1):leftdown of screen (1,1):upright
}Geom;

void main(){
  vec3 A = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  vec3 B = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  Geom.normal = normalize( cross(A, B));
  for(int i = 0; i < gl_in.length(); i++){
    Geom.worldpos = gl_in[i].gl_Position;
    gl_Position = viewproj[0] * viewproj[1] * gl_in[i].gl_Position;
    Geom.texcoord0 = texcoord0[i];
    Geom.screentexcoord 
      = vec2(gl_Position.x, -gl_Position.y) / gl_Position.w;
    EmitVertex();
  }
  EndPrimitive();  
}
