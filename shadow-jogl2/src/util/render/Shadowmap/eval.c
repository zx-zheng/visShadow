#version 400 core

layout(quads, equal_spacing, cw) in;
	
uniform mat4 model;
uniform sampler2D weatherTex;
				 uniform int divide;

in ctrl{			  
  vec3 Normal;
  vec2 Area;
} Ctrl[];
		
out vec4 Position;
out vec2 texcoord0;
out vec2 Area;

vec4 interpolate4(in vec4 v0, in vec4 v1, in vec4 v2, in vec4 v3)
{
  vec4 a = mix(v0, v1, gl_TessCoord.x);
  vec4 b = mix(v3, v2, gl_TessCoord.x);
  return mix(a, b, gl_TessCoord.y);
}
		  
void main(){
  mat4 normalmat = transpose(inverse(model));
  vec3 normal = (normalmat * vec4(0, 0, 1, 1)).xyz;
  gl_Position = interpolate4(gl_in[0].gl_Position,
			     gl_in[1].gl_Position, 
			     gl_in[2].gl_Position,
			     gl_in[3].gl_Position);
  vec4 wdata = texture(weatherTex, 
		       vec2(((gl_TessCoord.x + Ctrl[0].Area)/divide).x,
			    (1-(gl_TessCoord.y + Ctrl[0].Area)/divide).y));
  float height = sqrt(pow(wdata.z, 2) + pow(wdata.w, 2)) * 0.05;
  gl_Position.xyz +=normal * height;
  texcoord0 = gl_TessCoord.xy;
  Area = Ctrl[0].Area;
}

