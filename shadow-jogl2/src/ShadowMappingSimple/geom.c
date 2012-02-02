#version 400 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform mat4 view, proj;
uniform int maxlight;

out geom{
  vec4 worldpos;
  vec3 normal;
}Geom;

void main(){
  vec3 A = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  vec3 B = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  Geom.normal = normalize( cross(A, B));

  for(int i = 0; i < gl_in.length(); i++){
    Geom.worldpos = gl_in[i].gl_Position;
    gl_Position = proj * view * Geom.worldpos;
    EmitVertex();
  }
  EndPrimitive();
  
}
