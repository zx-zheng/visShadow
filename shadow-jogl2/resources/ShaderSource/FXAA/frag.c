#version 400 core
uniform float FXAA_SPAN_MAX = 8.0;
uniform float FXAA_REDUCE_MUL = 0;//1.0/8.0;

#define FRAG_COLOR 0
#define FXAA_REDUCE_MIN   (1.0/128.0)

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform sampler2D target;

in vert{
  vec4 texcoord;
}Vert;

vec3 FxaaFragShader(vec4 posPos, sampler2D tex){
  vec3 rgbNW = texture(tex, posPos.zw).xyz;
  vec3 rgbNE = textureOffset(tex, posPos.zw, ivec2(1,0)).xyz;
  vec3 rgbSW = textureOffset(tex, posPos.zw, ivec2(0,1)).xyz;
  vec3 rgbSE = textureOffset(tex, posPos.zw, ivec2(1,1)).xyz;
  vec3 rgbM  = texture(tex, posPos.xy).xyz;

  vec3 luma = vec3(0.299, 0.587, 0.114);
  float lumaNW = dot(rgbNW, luma);
  float lumaNE = dot(rgbNE, luma);
  float lumaSW = dot(rgbSW, luma);
  float lumaSE = dot(rgbSE, luma);
  float lumaM  = dot(rgbM,  luma);

  float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
  float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

  vec2 dir;
  dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
  dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));

  float dirReduce = max(
			(lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL),
			FXAA_REDUCE_MIN);
  float rcpDirMin = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);
  dir = min(vec2( FXAA_SPAN_MAX,  FXAA_SPAN_MAX),
	    max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
		dir * rcpDirMin));

  vec3 rgbA = (1.0/2.0) * (
			   texture(tex, posPos.xy + dir * (1.0/3.0 - 0.5)).xyz +
			   texture(tex, posPos.xy + dir * (2.0/3.0 - 0.5)).xyz);
  vec3 rgbB = rgbA * (1.0/2.0) + (1.0/4.0) * (
					      texture(tex, posPos.xy + dir * (0.0/3.0 - 0.5)).xyz +
					      texture(tex, posPos.xy + dir * (3.0/3.0 - 0.5)).xyz);
  float lumaB = dot(rgbB, luma);
  if((lumaB < lumaMin) || (lumaB > lumaMax)) return rgbA;
  return rgbB; 
}

vec4 PostFX(sampler2D tex, vec4 posPos){
  vec4 c = vec4(0.0);
  c.rgb = FxaaFragShader(posPos, tex);
  //c.rgb = 1.0 - texture2D(tex, posPos.xy).rgb;
  c.a = 1.0;
  return c;
}


void main(){
  Color = PostFX(target, Vert.texcoord);
  //Color = texture(target, Vert.texcoord.xy);
}

