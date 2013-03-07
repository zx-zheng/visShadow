#version 400 core

#define FRAG_COLOR 0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

uniform mat4 lightsview[10];
uniform mat4 lightsproj[10];
uniform vec4 lightscolor[10];
uniform ivec4 lightsattr[10];
uniform int divide,shadowswitch,colorset;
uniform ivec2 lightcount_real_virtual;
uniform sampler2DArray shadowmap;
uniform sampler2D weatherTex, mapTex, shadowTex;
uniform float gamma;
uniform int L, lab_a, lab_b, shaderange, shadowrange;
uniform float aspect_Y, canvasAspect = 1;
uniform int mapscaling = 200;
uniform float mapoffsetx, mapoffsety, viewoffsetx, viewoffsety;
uniform float viewscaling = 1;
uniform int mapadjustmode = 0, mapalpha = 50;
uniform int lightSize;
uniform vec2 shadowTexCoordSize;

in geom{
  vec4 worldpos;
  vec3 normal;
  vec2 texcoord0;
  vec2 Area;
  vec2 screentexcoord;
  flat int hswitch;
}Geom;

vec2 possion32[32] = 
  vec2[](
	 vec2(-0.975402, -0.0711386), vec2(-0.920347, -0.41142), vec2(-0.883908, 0.217872),
	 vec2(-0.884518, 0.568041), vec2(-0.811945, 0.90521), vec2(-0.792474, -0.779962), 
	 vec2(-0.614856, 0.386578), vec2(-0.580859, -0.208777), vec2(-0.53795, 0.716666),
	 vec2(-0.515427, 0.0899991), vec2(-0.454634, -0.707938), vec2(-0.420942, 0.991272),
	 vec2(-0.261147, 0.588488), vec2(-0.211219, 0.114841), vec2(-0.146336, -0.259194),
	 vec2(-0.139439, -0.888668), vec2(0.0116886, 0.326395), vec2(0.0380566, 0.625477),
	 vec2(0.0625935, -0.50853), vec2(0.125584, 0.0469069), vec2(0.169469, -0.997253),
	 vec2(0.320597, 0.291055), vec2(0.359172, -0.633717), vec2(0.435713, -0.250832),
	 vec2(0.507797, -0.916562), vec2(0.545763, 0.730216), vec2(0.56859, 0.11655),
	 vec2(0.743156, -0.505173), vec2(0.736442, -0.189734), vec2(0.843562, 0.357036),
	 vec2(0.865413, 0.763726), vec2(0.872005, -0.927));

vec3 LabtoXYZ(float l,float a,float b){
  float xn = 0.9505;
  float yn = 1.0;
  float zn = 1.0888;
  float p = (l+16.0)/116.0;
  float x = xn * pow(p+a/500.0,3.0);
  float y = yn * pow(p,3.0);
  float z = zn * pow((p-b/200.0),3.0);
  return vec3(x,y,z);
}

vec3 XYZtoRGB(vec3 xyz){
  mat3 xyz2rgb = mat3(3.2406, -0.9689, 0.0557,
		      -1.5372, 1.8758, -0.2040,
		      -0.4986, 0.0415, 1.0570);
  return xyz2rgb * xyz;
}

vec3 RGBnonlinearRGB(vec3 rgb){
  float rd,gd,bd;
  if(rgb.r<0.0031308) rd = rgb.r*12.92;
  else rd = 1.055*pow(rgb.r,0.45)-0.055;
  if(rgb.g<0.0031308) gd = rgb.g*12.92;
  else gd = 1.055*pow(rgb.g,0.45)-0.055;
  if(rgb.b<0.0031308) bd = rgb.b*12.92;
  else bd = 1.055*pow(rgb.b,0.45)-0.055;
  return vec3(rd,gd,bd);
}

vec3 valuetocolor(float value){
  float lab_a = 0.7, lab_b = 0.5, lab_c = 0.7, lab_d = 0.6;
  float labangle = 
    radians(lab_a*100.0+(270.0-lab_a*100.0+(lab_b-0.5)*100.0)*(1.0-value));
  vec3 xyz = LabtoXYZ(lab_d*100.0,
		      (value-0.5)*30 + 0*lab_c*100.0*cos(labangle),
		      0*lab_c*100.0*sin(labangle));
  vec3 rgb = XYZtoRGB(xyz);
  return RGBnonlinearRGB(rgb);
}

vec3 valuetocolorRG(float value){
  float l = 75, a = (value-0.5)*100*1.4, b = 0;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz))+vec3(0.1,0.1,0);
}

/*-----------------------------------------------------------------*
  Color Red-Green
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRG(float shadow, float value){
  float l = 75 - 40*(1-shadow), a = (value-0.5)*100*1.4, b = 0;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

/*-----------------------------------------------------------------*
  Color Red-Green  Shade Blue-Yellow b
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRGshadeBY(float value, float shadewide, float shadow){
  float l = 75 - 30*(1-shadow), 
    a = (value-0.5)*100*1.4 * 0, b = -shadewide * 0;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

/*-----------------------------------------------------------------*
  Color Red-Green  Shade Blue-Yellow+L
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRGshadeBYL(float value, float shadewide, float shadow){
  float l = L + shadewide *shaderange*1*1 - shadowrange*(1-shadow) * 1;
  float a = (value-0.5)* lab_a * 1, b = 1*lab_b* shadewide * 1;
  vec3 xyz = LabtoXYZ(l,a,b);
  //return vec3(l/100);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

/*-----------------------------------------------------------------*
  
 *-----------------------------------------------------------------*/
vec3 valuetoshadowcolorRGYG(float shadow, float value, float value2){
  float l = 75 - 20*(1-shadow), 
    a = (value-0.5)*100*1.4, b = (value2-0.5)*100*1.4;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

vec3 valuetocolorYB(float value){
  float l = 75, a = 0, b = (value-0.5)*100;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

vec3 valuetoshadowcolorYB(float shadow, float value){
  float l = 75 - 20*(1-shadow), a = 0, b = (value-0.5)*100;
  vec3 xyz = LabtoXYZ(l,a,b);
  return RGBnonlinearRGB(XYZtoRGB(xyz));
}

vec3 shadeYB(float shadewide, vec3 color){
  float blue = 0.15*2, yellow = 0.15*2;
  vec3 kcool = vec3(0,0,blue)+(1-blue)*color;
  vec3 kwarm = vec3(yellow,yellow,0)+(1-yellow)*color;
  vec3 shadecolor = (1-shadewide)*0.5*kcool+(1-(1-shadewide)*0.5)*kwarm;
  return shadecolor;
}

vec3 shadeshadowYB(float shadewide, float shadow, vec3 color){
  float blue = 0.3, yellow = 0.3;
  vec3 kcool = vec3(0,0,blue)+(1-blue)*color;
  vec3 kwarm = vec3(yellow,yellow,0)+(1-yellow)*color;
  vec3 shadecolor = (1-shadewide)*0.5*kcool+(1-(1-shadewide)*0.5)*kwarm;
  return shadecolor;
}

vec3 shadeRG(float shadewide, vec3 color){
  float red = 0.2, green = 0.2;
  vec3 kcool = vec3(red,0,0)+(1-red)*color;
  vec3 kwarm = vec3(0,green,0)+(1-green)*color;
  vec3 shadecolor = (1-shadewide)*0.5*kcool+(1-(1-shadewide)*0.5)*kwarm;
  return shadecolor;
}

/*-----------------------------------------------------------------*
  
 *-----------------------------------------------------------------*/
vec3 valRGshadeshadowYB(float val, float shadewide, float shadow){
  vec3 color = valuetoshadowcolorRG(shadow, val);
  return //shadow*shadeYB(min(shadewide, (shadow-0.5)*2), color)
    shadeshadowYB(shadewide/shadewide, shadow, color);
    //+(1-shadow)*0.5*shadeYB(shadewide, color);
}

/*-----------------------------------------------------------------*
  
 *-----------------------------------------------------------------*/
vec3 valRGYBshadeshadowYB(float val, float val2, float shadewide, float shadow){
  vec3 color = valuetoshadowcolorRGYG(shadow, val, val2);
  return //shadow*shadeYB(min(shadewide, (shadow-0.5)*2), color)
    shadeshadowYB(shadewide, shadow, color);
    //+(1-shadow)*0.5*shadeYB(shadewide, color);
}

vec3 shadecmb(vec3 shade, vec3 shadecw, float shadewide){
  float ratio = pow(1-abs(shadewide),2);
  vec3 color = ratio*0.5*shade+(1-ratio) * shadecw;
  return color;
}

vec2 maptexcoordtransform(vec2 texcoord, float scale, 
			  vec2 offset, vec2 viewoffset){
  vec2 aspect = vec2(1, aspect_Y * canvasAspect);
  //float scaling = mapscaling * 0.02;
  float scaling = pow(2, (mapscaling - 200) * 0.05);
  //scaling = 1;
  return texcoord  * aspect * 0.5 * scaling //* viewscaling 
  + offset;// + viewoffset * scaling ;//+ vec2(0.5);
}

void main(){
  mat4 bias = mat4(0.5, 0, 0, 0,
		   0, 0.5, 0, 0,
		   0, 0, 0.5, 0,
		   0.5, 0.5, 0.5, 1);
  ivec2 offset[9] = ivec2[](ivec2(-1,-1),ivec2(0,-1),ivec2(1,-1),
			    ivec2(-1,0),ivec2(0,0),ivec2(1,0),
			    ivec2(-1,1),ivec2(0,1),ivec2(1,1));
  float shadow, shade=0, octest, count = 0,shadewide=0;
  for (int i = 0; i < lightcount_real_virtual.x; ++i){
    vec4 posfromlightworld = lightsview[i] * Geom.worldpos;
    vec4 posfromlight = bias * lightsproj[i] * posfromlightworld;
    posfromlight /= posfromlight.w;
    if(posfromlight.x > 0 && posfromlight.x < 1 
       && posfromlight.y > 0 && posfromlight.y < 1 && posfromlightworld.z < 0){
      float occluder, shadowtmp=0;

      ///*PCF
      for(int j = 0; j < offset.length; j++){
	occluder = textureOffset(shadowmap, vec3(posfromlight.xy, i), offset[j]).x;
	shadowtmp += occluder < abs(posfromlightworld.z) - 0.04? 0:1.0 * lightscolor[i].w;
      }
      shadowtmp /= offset.length;
      //*/

      /*PCSS*/
      /*
      float occluderFromLight = texture(shadowmap, vec3(posfromlight.xy, i)).z;
      float penumbraSize 
	= min(1, lightSize/20240000.0 * pow((abs(posfromlightworld.z) - occluderFromLight),3) );
      for(int j = 0; j < possion32.length; j++){
	occluder = texture(shadowmap, vec3(posfromlight.xy + penumbraSize * possion32[j], i)).x;
	shadowtmp += occluder < abs(posfromlightworld.z) - 0.04? 0:1.0 * lightscolor[i].w;
      }
      shadowtmp /= possion32.length;
      */

      if(lightsattr[i].x * Geom.hswitch==1 )shadowtmp = 1;
      vec3 lightvec = normalize((inverse(lightsview[i]) * vec4(0,0,0,1)).xyz);
				//- Geom.worldpos.xyz/Geom.worldpos.w);
      float shadetmp = max(0, dot(Geom.normal, lightvec))*lightscolor[i].w;
      float shadewidetmp = dot(Geom.normal, lightvec)*lightscolor[i].w;
      shade += shadetmp;
      shadewide += shadewidetmp;
      shadow += shadowtmp;
      count+=lightscolor[i].w;
    }
  } 

  //count = 1;  
  shade /= count;
  shadewide /= count;
  shadow /= count;
  //shadewide = 0;

  vec2 th = 
    texture(weatherTex, 
	    vec2(((Geom.texcoord0 + Geom.Area)/divide).x,
		 (1-(Geom.texcoord0 + Geom.Area)/divide).y)).xy;///10;
  float temperature = th.y;
  float humid = th.x/100;
  //temperature = 0;
  ///*
  if(colorset==1){
    Color=vec4(1,1,0,1);
    Color=vec4(vec3(dot(Geom.normal,vec3(0,0,1))),1);
    return;
  }
  //*/
  //temperature = (temperature - 273)/25.0;
  //vec3 color = valuetocolorYB(temperature).xyz;

  //float ratio = 0.5;
  //float uplightshade = dot(Geom.normal, vec3(0,0,1));

  vec2 shadowTexCoord = Geom.screentexcoord * 0.5 + 0.5;
  shadowTexCoord.y = 1 - shadowTexCoord.y;
  shadowTexCoord *= shadowTexCoordSize;

  shadow =  texture(shadowTex, shadowTexCoord).x;

  //if(shadowswitch == 0)shadow = 1;

  vec4 mapcolor = texture(mapTex, 
		   maptexcoordtransform(
					vec2(((Geom.texcoord0 + Geom.Area)/divide).x,
					     (1-(Geom.texcoord0 + Geom.Area)/divide).y), 1, 
					vec2(mapoffsetx, mapoffsety),
					vec2(viewoffsetx, viewoffsety)));
  vec4 visualizedcolor = 
    vec4(valuetoshadowcolorRGshadeBYL(temperature, shadewide, shadow), 1);
  //visualizedcolor = vec4(1);

  if(mapadjustmode == 1){
    Color = mapalpha * 0.01 * mapcolor 
      + (100 - mapalpha) * 0.01 * visualizedcolor;
  } else {
    if(mapcolor.x > 0.9){
      Color = (0.39 * 1 + 0.61) * visualizedcolor;
    } else {
      Color = (0.39 * shadow + 0.61) * mapcolor;
    }
  }

  /*
  if(visualizedcolor.x > 1 || visualizedcolor.y > 1){
    visualizedcolor = vec4(0);
  }
  */
  Color = visualizedcolor;
  //Color = vec4(temperature);

  //Color = vec4(255/255 , 0/255, 0/255,1);
  //Color = vec4(shadewide);
  //Color = vec4(shadow);
  
  //Color = texture(shadowTex, shadowTexCoord);

  //Color = vec4(Geom.screentexcoord, 0,1);
  //Color = vec4(vec2(((Geom.texcoord0 + Geom.Area)/divide).x,
  //		    (1-(Geom.texcoord0 + Geom.Area)/divide).y), 0, 1);

  //Color = vec4(color * (shade * (0.3 * shadow + 0.4) + 0.3) , 1);
  //Color = vec4(color * (shade * (0.6) + 0.4 - (1-shadow)*0.4 * sin(3.14/2*shade)) , 1);
  //Color = vec4(color * (0.2 + shadow * 0.8 *shade + (1-shadow) * 0.2 * pow(shade, gamma)), 1);
  //Color = (0.2+0.8*shadow)*vec4(shadeRG(shadewide, color), 1);
  //Color = vec4(valRGshadeshadowYB(temperature, shadewide, shadow),1);
  //Color = vec4(valRGshadeshadowYB(humid, shadewide, shadow),1);
  //Color = vec4(valRGYBshadeshadowYB(temperature, humid, shadewide, shadow),1);
  //Color = vec4((0.3+0.7*shade*shadow)* valuetocolorRG(temperature),1);
  //vec3 colorkw = shadeRG(shadewide, color);
  //vec3 colorstd = shade * color;
  //Color = (0.8+0.2*shadow)*vec4(shadecmb(colorstd, colorkw, shadewide),1);

  //Color = 0.5 * vec4(valuetoshadowcolorRGshadeBYL(temperature, shadewide, shadow),1);

  //Color = vec4( maptexcoordtransform(Geom.screentexcoord, 1, vec2(1)),0,1);
}
