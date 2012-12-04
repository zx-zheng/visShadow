#version 400 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

//4 matrices P, Mv, Mvi and Mvit.
uniform mat4 viewproj[4];
uniform vec3 lightPos[10];
		  uniform int lightNum;

		  in vec2 texcoord0[];
out geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 screentexcoord;
  vec3 shadeColor;
}Geom;

vec3 diffuse(){
  vec3 diffuse;
  for(int i = 0; i < lightNum; i++){
    diffuse += max(0, dot(Geom.normal, normalize(lightPos[i])));
  }
  diffuse /= lightNum;
  return diffuse;
}

void main(){
  vec3 A = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  vec3 B = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
  Geom.normal = normalize( cross(A, B));
  for(int i = 0; i < gl_in.length(); i++){
    Geom.worldpos = gl_in[i].gl_Position;
    gl_Position = viewproj[0] * viewproj[1] * Geom.worldpos;
    Geom.texcoord0 = texcoord0[i];
    Geom.screentexcoord 
      = vec2(gl_Position.x, -gl_Position.y) / gl_Position.w;
    Geom.shadeColor = diffuse();
    EmitVertex();
  }
  EndPrimitive();  
}
