#version 400 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 40) out;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform ivec2 lightcount_real_virtual;

out geom{
  vec4 worldpos;
  float z;
}Geom;

void main(){
  for(int layer = 0; layer < lightcount_real_virtual.x; layer++){
    gl_Layer = layer;
    for(int i = 0; i < gl_in.length(); i++){
      vec4 worldpostmp = lightsview[layer] * gl_in[i].gl_Position;
      Geom.worldpos = worldpostmp;
      gl_Position = lightsproj[layer] * worldpostmp;
      Geom.z = gl_Position.z/gl_Position.w;
      EmitVertex();
    }
    EndPrimitive();
  }
}
