#version 400 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 4) out;

uniform mat4 lightsview[10], lightsproj[10];
uniform int maxlight;

out geom{
  vec4 worldpos;
  float z;
}Geom;

void main(){
  for(int layer = 0; layer < maxlight; layer++){
    gl_Layer = layer;
    for(int i = 0; i < gl_in.length(); i++){
      Geom.worldpos = lightsview[i] * gl_in[i].gl_Position;
      gl_Position = lightsproj[i] * Geom.worldpos;
      Geom.z = gl_Position.z/gl_Position.w;
      EmitVertex();
    }
    EndPrimitive();
  }
}
