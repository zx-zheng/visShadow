#version 400 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform mat4 view, proj;

		  in vec2 texcoord0[];
		  in vec2 Area[];
		  in int hswitch[];
out geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 Area;
  flat int hswitch;
}Geom;

void main(){
  vec3 A = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  vec3 B = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  Geom.normal = normalize( cross(A, B));
  Geom.hswitch = hswitch[0];
  for(int i = 0; i < gl_in.length(); i++){
    Geom.worldpos = gl_in[i].gl_Position;
    if(hswitch[0] == 0)Geom.worldpos.z = 0;
    //Geom.worldpos.z = 0;
    gl_Position = proj * view * Geom.worldpos;
    Geom.texcoord0 = texcoord0[i];
    Geom.Area = Area[i];
    EmitVertex();
  }
  EndPrimitive();
  
}
