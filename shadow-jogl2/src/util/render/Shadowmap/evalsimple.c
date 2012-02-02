#version 400 core

layout(quads, equal_spacing, cw) in;
	
uniform mat4 model;

in ctrl{			  
  vec3 Normal;
  vec2 Area;
} Ctrl[];
		
out eval{
  vec4 Position;
} Eval;

vec4 interpolate4(in vec4 v0, in vec4 v1, in vec4 v2, in vec4 v3)
{
  vec4 a = mix(v0, v1, gl_TessCoord.x);
  vec4 b = mix(v3, v2, gl_TessCoord.x);
  return mix(a, b, gl_TessCoord.y);
}
		  
void main(){
  mat4 normalmat = transpose(inverse(model));
  gl_Position = interpolate4(gl_in[0].gl_Position,
			     gl_in[1].gl_Position, 
			     gl_in[2].gl_Position,
			     gl_in[3].gl_Position);
}
