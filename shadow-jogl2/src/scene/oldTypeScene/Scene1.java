package scene.oldTypeScene;

import gl.FBO;
import gl.Shader;
import gl.TexBindSet;
import gl.TexUnitManager;
import gui.Ctrlpanel;
import gui.SceneJCheckBox;
import gui.UniformJCheckBox;
import gui.UniformJSlider;

import java.awt.event.ActionEvent;
import java.awt.font.TextMeasurer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.media.opengl.*;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.SliderUI;
import javax.xml.datatype.DatatypeFactory;

import com.jogamp.opengl.util.PMVMatrix;

import oekaki.util.Tex2D;
import oekaki.util.TexImage;
import oekaki.util.TexImageUtil;

import scene.obj.Billboard;
import scene.obj.Light;
import scene.obj.Tiledboard;
import util.DataSet2D;
import util.loader.Load2Dfloat;
import util.loader.PlyLoader;
import util.loader.Spline;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.math.function.ByteBufferFloat2Wrapper2D;
import za.co.luma.math.function.ByteBufferFloatWrapper2D;
import za.co.luma.math.function.List2Wrapper2D;
import za.co.luma.math.function.RealFunction2DWrapper;
import za.co.luma.math.sampling.PoissonDiskSampler;
import za.co.luma.math.sampling.UniformPoissonDiskSampler;

public class Scene1 extends Scene {
  
  PlyLoader box = new PlyLoader("resources/Objdata/box.ply");
  PlyLoader board = new PlyLoader("resources/Objdata/board.ply");
  PlyLoader sphere = new PlyLoader("resources/Objdata/sphere3.ply");
  PlyLoader hex = new PlyLoader("resources/Objdata/square6.ply");
  PlyLoader plus = new PlyLoader("resources/Objdata/minus.ply");
  PlyLoader tube = new PlyLoader("resources/Objdata/tube6.ply");
  PlyLoader ring = new PlyLoader("resources/Objdata/ring.ply");
  PlyLoader cross = new PlyLoader("resources/Objdata/cross3.ply");
  PlyLoader cylinder = new PlyLoader("resources/Objdata/cylinder2.ply");
  
  PMVMatrix model, view;
  float[] PROJ = {-1, 1, -1, 1, -100, 100};
  float[] VIEW = {0, 0, 30, 0, 0, 0, 0, 1, 0};
  Shader billBoardShader, flatShading;
  Billboard shadow, whiteCircle;
  int uniformFlatShaingInColor;
 
  
  //データの範囲
  private final double LEFT_OF_LONGITUDE = 110;
  private final double RIGHT_OF_LONGITUDE = 160;
  
  //データの範囲の緯度が仕様とはすこしずれている？
  private final double UP_OF_LATITUDE = 51.5;
  private final double DOWN_OF_LATITUDE = 21.8;
  
  private final double CENTER_OF_LONGITUDE = 
      (LEFT_OF_LONGITUDE + RIGHT_OF_LONGITUDE) * 0.5;
  private final double CENTER_OF_LATITUDE = 
      (UP_OF_LATITUDE + DOWN_OF_LATITUDE) * 0.5;
  private final double WIDTH_OF_LONGTITUDE = 
      RIGHT_OF_LONGITUDE - LEFT_OF_LONGITUDE;
  private final double WIDTH_OF_LATITUDE = 
      UP_OF_LATITUDE - DOWN_OF_LATITUDE;
  
  //データの解像度
  private final int  GRID_SIZE_X = 201, GRID_SIZE_Y = 251;
  
  //データの間隔
  private final double GRID_INTERVAL_X = 0.25, GRID_INTERVAL_Y = 0.2,
      ASPECT_RATIO_Y_FOR_MAP = 0.7667;
  
  public double PROJ_SCALE;
  private float viewscaling;
  private final int INIT_MAP_SCALE_VALUE = 200;
  private float MAP_ASPECT;
  
  private String PATH_TO_MAP;
  private boolean MAP_CHANGED = false;
  
  private UniformJCheckBox mapAdjustmode;
  
  private List<Vector2DDouble> markPointList;
  
  public ArrayList<DataSet2D> dataList;
  private ArrayList<Integer> dataIndexList = new ArrayList<Integer>(),
      choicedDataList = new ArrayList<Integer>(),
      usedDataList = new ArrayList<Integer>();
  private DataSet2D currentData, currentData2;
  private int dataSetCount;
  private float scaleAnswerValue = 1;
  
  Shader viewmapshader;
  
  public Tiledboard tboard = 
      new Tiledboard(GRID_INTERVAL_X * (GRID_SIZE_X - 1), 
          GRID_INTERVAL_Y * (GRID_SIZE_Y - 1) * ASPECT_RATIO_Y_FOR_MAP, 4);

  private int nochangeframe = 0;
  
  //GUI
  SceneJCheckBox textureshadow;
  SceneJCheckBox softshadow;
  JSlider possioninterval, viewScale;
  JButton saveMapConf, loadMapConf, openMapImage;
  JFileChooser mapImageChooser;
  
  private int currentinterval = 0, currentscale = 0;
  
  private Vector2DDouble viewcenter = new Vector2DDouble(0, 0);
  private Vector2DDouble mapoffset = new Vector2DDouble(0, 0);
  UniformJSlider mapscaleslider;
  
  private float cylinderscale = 0;
  private float lightdirectionrotate = 45;
  
  public TexBindSet mapTex;
  
  public TexBindSet shadowTbs;
  private Tex2D shadowTex;
  private FBO shadowFBO;
  private float[] shadowTexCoordSize = {1, 1};
  
  private boolean isTest3Rendering1 = false;
  
  public Scene1() {
    super();
  }
  
  @Override
  public void init(GL2GL3 gl, Shader shader, Shader shadertess){
    super.init(gl, shader, shadertess);
    setCamera(gl);
    initShader(gl);
    initMatrix(gl);
    box.init(gl);
    board.init(gl);
    sphere.init(gl);
    hex.init(gl);
    tboard.init(gl);
    plus.init(gl);
    tube.init(gl);
    ring.init(gl);
    cross.init(gl);
    cylinder.init(gl);
    
    //ビルボードの中心は画像に応じて変える
    shadow = new Billboard(gl, "resources/Image/TextureImage/shadow4.png",
        new Vector2DDouble(0, 0.8), 2);

    whiteCircle = new Billboard(gl, "resources/Image/TextureImage/whiteCircle.png", 2);
    
    Ctrlpanel.getInstance().scene = this;
    textureshadow = new SceneJCheckBox("texture shadow", false);
    Ctrlpanel.getInstance().addscenecheckbox(textureshadow);
    softshadow = new SceneJCheckBox("Soft shadow", true);
    Ctrlpanel.getInstance().addscenecheckbox(softshadow);
    possioninterval = new JSlider(10, 60, 20);
    Ctrlpanel.getInstance().addSlider(possioninterval, "interval");
    viewScale = new JSlider(1, 200, 1);
    Ctrlpanel.getInstance().addSlider(viewScale, "Scale");
    
    viewmapshader = shadertess;
    
    setViewScaling();
    currentinterval = possioninterval.getValue();
    
    setMapGUI(gl, shadertess);
    setMapTex(gl, 
        "resources/Image/MapImage/whitemap.png",
        shadertess);
    
    initData(gl);
    initDataShaderUniform(gl, shader, shadertess);
    
    setTessLevel(gl, 1);
      
    initMarkPointList();
    initShadowFBO(gl);
  }
  
  private void initShader(GL2GL3 gl){
    billBoardShader = new Shader(
        "resources/ShaderSource/Billboard/vert.c",
        null, 
        null, 
        "resources/ShaderSource/Billboard/geom.c", 
        "resources/ShaderSource/Billboard/frag.c",
        "billBoard");
    billBoardShader.init(gl);
    
    flatShading = new Shader(
        "resources/ShaderSource/FlatShading/vert.c",
        null, 
        null, 
        "resources/ShaderSource/FlatShading/geom.c", 
        "resources/ShaderSource/FlatShading/frag.c",
        "Flat Shading");
    flatShading.init(gl);
    flatShading.adduniform(gl, "lightPos", 3, lightPosArray());
    flatShading.adduniform(gl, "lightNum", lightCount());
    uniformFlatShaingInColor =
        gl.glGetUniformLocation(flatShading.getID(), "inColor");
  }
  
  private void initMatrix(GL2GL3 gl){
    model = new PMVMatrix();
    model.glLoadIdentity();
    view = pvmat;
    /*
    view = new PMVMatrix();
    view.glLoadIdentity();
    view.gluLookAt(VIEW[0], VIEW[1], VIEW[2],
        VIEW[3], VIEW[4], VIEW[5],
        VIEW[6], VIEW[7], VIEW[8]);
    
    view.glOrthof(PROJ[0], PROJ[1], PROJ[2], PROJ[3], PROJ[4], PROJ[5]);
    */
 
    billBoardShader.use(gl);
    billBoardShader.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
    Shader.unuse(gl);
    
    flatShading.use(gl);
    flatShading.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
  }
  
  private void initShadowFBO(GL2GL3 gl){
    shadowTex = new Tex2D(GL2.GL_RGBA, GL2.GL_RGBA,
        GL2.GL_FLOAT, 1920, 1200,
        GL2.GL_LINEAR, GL2.GL_CLAMP, GL2.GL_CLAMP,
         null, "shadowTex");
    shadowTex.init(gl);
    
    shadowTbs = new TexBindSet(shadowTex);
    shadowTbs.bind(gl);
    
    shadowFBO = new FBO(gl);
    shadowFBO.attachTexture(gl, shadowTbs);
    
    shadertess.adduniform(gl, "shadowTex", shadowTbs.texunit);
    shadertess.adduniform(gl, "shadowTexCoordSize", 2, shadowTexCoordSize);
  }
  
  public void setShadowTexCoordSize(int width, int height){
    shadowTexCoordSize[0] = (float) width / shadowTbs.tex.width;
    shadowTexCoordSize[1] = (float) height / shadowTbs.tex.height;
    shadertess.setuniform("shadowTexCoordSize", shadowTexCoordSize);
  }
  
  public void setPROJ_SCALE(double x){
    PROJ_SCALE = x / (GRID_INTERVAL_X * (GRID_SIZE_X - 1));
  }
  
  //中心の移動 x,yはピクセル単位
  public void moveCenter(int x, int y, int viewportx, int viewporty){
    //画面座標から世界座標に変換
    double pixeltoworldcoordx = 
        (double) x / (double) viewportx 
        * Math.abs(orthoproj[1] - orthoproj[0]) / viewscaling;
    double pixeltoworldcoordy = 
        (double) y / (double) viewporty 
        * Math.abs(orthoproj[3] - orthoproj[2]) / viewscaling;

    viewcenter.x += pixeltoworldcoordx;
    viewcenter.y -= pixeltoworldcoordy;
    
    //シェーダーにviewのオフセットを転送
   applyViewCenterToShader();
  }
  
  public Vector2DDouble viewportToWorld(Vector2DInt posInWindow, 
      int viewportw, int viewporth){
    return new Vector2DDouble(
        (posInWindow.x - viewportw * 0.5) / (double) viewportw 
        * Math.abs(orthoproj[1] - orthoproj[0]) / viewscaling + viewcenter.x,
        (posInWindow.y - viewporth * 0.5) / (double) viewporth 
        * Math.abs(orthoproj[3] - orthoproj[2]) / viewscaling + viewcenter.y);
  }
  
  private void applyViewCenterToShader(){
    viewmapshader.setuniform("viewoffsetx", 
        (float) (-viewcenter.x 
            / (GRID_INTERVAL_X * (GRID_SIZE_X - 1))));
    viewmapshader.setuniform("viewoffsety", 
        (float) (viewcenter.y 
            / (GRID_INTERVAL_Y * (GRID_SIZE_Y - 1) / MAP_ASPECT)));
    
    nochangeframe = 0;
  }
  
  private void initData(GL2GL3 gl){
    dataList = new ArrayList<DataSet2D>();
    dataSetCount = 0;
    String filepath = "/home/michael/zheng/Programs/WeatherData/2009100818/";
    addData(gl, filepath);
    filepath = "/home/michael/zheng/Programs/WeatherData/2009100718/";
    addData(gl, filepath);
    filepath = "/home/michael/zheng/Programs/WeatherData/2008061418/";
    addData(gl, filepath);
//    filepath = "/home/michael/zheng/Programs/WeatherData/2009100800/";
//    addData(gl, filepath);
//    filepath = "/home/michael/zheng/Programs/WeatherData/2009100806/";
//    addData(gl, filepath);
  }
  
  public void addData(GL2GL3 gl, String filepath){
    DataSet2D data = new DataSet2D(filepath, tboard);
    data.init(gl);
    dataList.add(data);
    dataIndexList.add(dataSetCount);
    dataSetCount++;
  }
  
  //データを一つ選んでセット
  public int updateChoiceList(GL2GL3 gl){
    for(int i : choicedDataList){
      usedDataList.add(i);
      dataList.get(i).unuse(gl);
    }
    choicedDataList.clear();
    
    if(dataIndexList.size() < 1){
      System.out.println("No enough data set and reset index list.");
      resetChoiceList(gl);
    }
    
    int targetindex = (int)(Math.random() * dataIndexList.size());
    choicedDataList.add(dataIndexList.get(targetindex));
    dataIndexList.remove(targetindex);
    
    updatePoisson(intervalToPoission(currentinterval));
    setTargetData(gl, choicedDataList.get(0));
    return choicedDataList.get(0);
  }
  
  public ArrayList<Integer> resetAndGetChoiceList(GL2GL3 gl, int num){
    resetChoiceList(gl);
    if(dataIndexList.size() < num){
      System.out.println("No enough data set.");
      num = dataIndexList.size();
    }
    for(int i = 0; i < num; i++){
      int targetindex = (int)(Math.random() * dataIndexList.size());
      choicedDataList.add(dataIndexList.get(targetindex));
      dataIndexList.remove(targetindex);
    }
    updatePoisson(intervalToPoission(currentinterval));
    return choicedDataList;
  }
  
  public void resetChoiceList(GL2GL3 gl){
    for(int i : choicedDataList){
      dataIndexList.add(i);
      dataList.get(i).unuse(gl);
    }
    choicedDataList.clear();
    
    for(int i : usedDataList){
      dataIndexList.add(i);
    }
    usedDataList.clear();
  }
  
  public void setTargetData(GL2GL3 gl, int index){
    currentData = dataList.get(index);
    currentData.use(gl);
    //updatePoisson(intervalToPoission(currentinterval));
    
    shader.setuniform("weatherTex", currentData.tex.texunit);
    shader.setuniform("shadowmap", getShadowmapTexture().texunit);
    
    shadertess.setuniform("weatherTex", currentData.tex.texunit);
    shadertess.setuniform("shadowmap", getShadowmapTexture().texunit);
    shadertess.setuniform("divide", tboard.getdivide());
    
    setShadowmapShaderTexture(gl, currentData.tex, "weatherTex");
    setShadowmapShader1i(gl, tboard.getdivide(), "divide");
  }
  
  public void setTargetData(GL2GL3 gl, int index, int index2){
    currentData = dataList.get(index);
    currentData.use(gl);
    //updatePoisson(intervalToPoission(currentinterval));
    
    currentData2 = dataList.get(index2);
    
    shader.setuniform("weatherTex", currentData2.tex.texunit);
    shader.setuniform("shadowmap", getShadowmapTexture().texunit);
    
    shadertess.setuniform("weatherTex", currentData2.tex.texunit);
    shadertess.setuniform("shadowmap", getShadowmapTexture().texunit);
    shadertess.setuniform("divide", tboard.getdivide());
    
    setShadowmapShaderTexture(gl, currentData2.tex, "weatherTex");
    setShadowmapShader1i(gl, tboard.getdivide(), "divide");
  }
 
  
  public void initMarkPointList(){
    markPointList = new ArrayList<Vector2DDouble>();
    Vector2DDouble omaezaki = GeoCoordToWorldCoord(
        new Vector2DDouble(34.595, 138.225), tboard.width, tboard.height);
    Vector2DDouble souyamisaki= GeoCoordToWorldCoord(
        new Vector2DDouble(45.52, 141.937), tboard.width, tboard.height);
    Vector2DDouble oomazaki = GeoCoordToWorldCoord(
        new Vector2DDouble(41.5, 141.), tboard.width, tboard.height);
    Vector2DDouble miurashi = GeoCoordToWorldCoord(
        new Vector2DDouble(35.138, 139.6195), tboard.width, tboard.height);
    Vector2DDouble kengamine = GeoCoordToWorldCoord(
        new Vector2DDouble(35.36047, 138.72756), tboard.width, tboard.height);
    Vector2DDouble hakusangaku = GeoCoordToWorldCoord(
        new Vector2DDouble(35.36056, 138.72778), tboard.width, tboard.height);
    markPointList.add(omaezaki);
    markPointList.add(miurashi);
    markPointList.add(oomazaki);
    markPointList.add(souyamisaki);
    markPointList.add(kengamine);
    markPointList.add(hakusangaku);
  } 
  
  public void setMapTex(GL2GL3 gl, String pathToImage, Shader shader){
    //どのglかが重要
    if(mapTex != null){
      mapTex.unbind(gl);
      mapTex = null;
    }
    PATH_TO_MAP = pathToImage;
    TexImage mapimage = TexImageUtil.loadImage(pathToImage, 3, 
        TexImage.TYPE_BYTE);
    MAP_ASPECT = (float) mapimage.width / (float) mapimage.height;
    
    Tex2D maptexture = new Tex2D(GL2.GL_RGB, GL2.GL_BGR,
        GL.GL_UNSIGNED_BYTE, mapimage.width, mapimage.height,
        GL.GL_LINEAR, mapimage.buffer, "map");

    maptexture.init(gl);

    mapTex = new TexBindSet(maptexture);
    mapTex.bind(gl);

    Shader.unuse(gl);
    shader.use(gl);
    gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "mapTex"), 
        mapTex.texunit);
    gl.glUniform1f(gl.glGetUniformLocation(shader.getID(), "aspect_Y"), 
        MAP_ASPECT);
    Shader.unuse(gl);
    loadMapProperties(PATH_TO_MAP);
    MAP_CHANGED = false;
  }
  
  private float[] lightPosArray(){
    float[] lightPos = new float[3 * lightCount()];
    for(int i = 0; i < lightCount(); i++){
      lightPos[3 * i] = lights.get(i).posx;
      lightPos[3 * i + 1] = lights.get(i).posy;
      lightPos[3 * i + 2] = lights.get(i).posz;
    }
    return lightPos;
  }
  
  @Override
  public void clickButton(ActionEvent e){
    Object src = e.getSource();
    if (src == saveMapConf) {
      saveMapProperties(PATH_TO_MAP);
    } else if (src == loadMapConf) {
      loadMapProperties(PATH_TO_MAP);
    } else if (src == openMapImage) {
      int returnVal = 
          mapImageChooser.showOpenDialog(Ctrlpanel.getInstance().getPanel());
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = mapImageChooser.getSelectedFile();
        PATH_TO_MAP = "resources/Image/MapImage/" + file.getName();
        MAP_CHANGED = true;
      }
    }
  }
  
  public void loadMapProperties(String pathtomap){
    Properties conf = new Properties();
    try {
      InputStream inputStream = 
          new FileInputStream(new File(pathtomap + ".properties"));
      conf.load(inputStream);
      
      int scalevalue = (int) (INIT_MAP_SCALE_VALUE * 
          Double.parseDouble(conf.getProperty("SCALE", 
              String.valueOf(INIT_MAP_SCALE_VALUE))));
      int viewscalevalue = Integer.parseInt(conf.getProperty("VIEW_SCALE",
          "1"));
      mapscaleslider.setValue(scalevalue);
      viewScale.setValue(viewscalevalue);
      mapoffset.x = 
          Double.parseDouble(conf.getProperty("OFFSET_X", "0"));
      mapoffset.y = 
          Double.parseDouble(conf.getProperty("OFFSET_Y", "0"));
      
      viewcenter.x = 
          Double.parseDouble(conf.getProperty("VIEW_CENTER_X", "0"));
      viewcenter.y = 
          Double.parseDouble(conf.getProperty("VIEW_CENTER_Y", "0"));
      applyViewCenterToShader();
      
      applyMapCenter(viewmapshader);
    } catch (IOException e) {
      System.out.println("No properties file for " + pathtomap);
    }
  }
  
  public void saveMapProperties(String pathtomap){
    File conf = new File(pathtomap + ".properties");
    try{
      FileWriter fw = new FileWriter(conf);
      double scale = (double) mapscaleslider.getValue() / INIT_MAP_SCALE_VALUE;
      fw.write("SCALE = " + Double.toString(scale) + "\n");
      fw.write("OFFSET_X = " + Double.toString(mapoffset.x) + "\n");
      fw.write("OFFSET_Y = " + Double.toString(mapoffset.y) + "\n");
      fw.write("VIEW_SCALE = " + Integer.toString(viewScale.getValue()) + "\n");
      fw.write("VIEW_CENTER_X = " + Double.toString(viewcenter.x) + "\n");
      fw.write("VIEW_CENTER_Y = " + Double.toString(viewcenter.y) + "\n");
      fw.close();
    }catch(IOException e){
      e.printStackTrace();
      System.out.println("Fail to save file");
    }
  }
  
  public void setMapGUI(GL2GL3 gl, Shader shader){
    shader.adduniform(gl, "mapscaling", INIT_MAP_SCALE_VALUE);
    mapscaleslider = new UniformJSlider(0, 400, INIT_MAP_SCALE_VALUE, 
        "mapscaling", shader);
    Ctrlpanel.getInstance().addJSlider("MapScale", mapscaleslider);
    shader.adduniform(gl, "mapoffsetx", 0f);
    shader.adduniform(gl, "mapoffsety", 0f);
    
    shader.adduniform(gl, "viewoffsetx", 0f);
    shader.adduniform(gl, "viewoffsety", 0f);
    
    shader.adduniform(gl, "viewscaling", viewscaling);
    
    shader.adduniform(gl, "canvasAspect", 1);
    
    shader.adduniform(gl, "mapalpha", 50);
    UniformJSlider mapalpha = 
        new UniformJSlider(0, 100, 50, "mapalpha", shader);
    Ctrlpanel.getInstance().addJSlider("Map alpha", mapalpha);
    
    shader.adduniform(gl, "lightSize", 1);
    UniformJSlider lightSize = 
        new UniformJSlider(1, 100, 1, "lightSize", shader);
    Ctrlpanel.getInstance().addJSlider("Light size", lightSize);
    
    mapAdjustmode = new UniformJCheckBox("Map adjust", false, 
        "mapadjustmode", shader);
    shader.adduniform(gl, "mapadjustmode", 0);
    Ctrlpanel.getInstance().adduniformcheckbox(mapAdjustmode);
    
    saveMapConf = new JButton("Save");
    Ctrlpanel.getInstance().addButton(saveMapConf);
    
    loadMapConf = new JButton("Load");
    Ctrlpanel.getInstance().addButton(loadMapConf);
    
    openMapImage = new JButton("Open Map Image");
    Ctrlpanel.getInstance().addButton(openMapImage);
    
    mapImageChooser = new JFileChooser("resources/Image/MapImage");
    mapImageChooser.setFileFilter(
        new FileNameExtensionFilter("*.png", "png"));
  }
  
  public void adjustMapCenter(int x, int y, 
      int viewportx, int viewporty){
    double currentmapscale = 
        Math.pow(2, (mapscaleslider.getValue() - INIT_MAP_SCALE_VALUE) / 20.0);
       // (double) mapscaleslider.getValue() / (double) INIT_MAP_SCALE_VALUE;
    double viewscalefactor = viewscaling/ PROJ_SCALE;
    mapoffset.x += x * currentmapscale / (viewportx * viewscalefactor);
    mapoffset.y += y * currentmapscale / (viewporty * viewscalefactor)
        * MAP_ASPECT * (tboard.width/tboard.height);
    applyMapCenter(viewmapshader);
  }
  
  private void applyMapCenter(Shader shader){
    shader.setuniform("mapoffsetx", 
        (float) (-mapoffset.x/2));
    shader.setuniform("mapoffsety", 
        (float) (-mapoffset.y/2));
    System.out.println(mapoffset.y);
  }
  
  public void initDataShaderUniform(GL2GL3 gl, Shader shader, Shader shadertess){
    if(dataList.size() == 0){
      return;
    }
    currentData = dataList.get(0);
    currentData.use(gl);
    shader.adduniform(gl, "weatherTex", currentData.tex.texunit);
    shader.adduniform(gl, "shadowmap", getShadowmapTexture().texunit);
    
    shadertess.adduniform(gl, "weatherTex", currentData.tex.texunit);
    shadertess.adduniform(gl, "shadowmap", getShadowmapTexture().texunit);
    shadertess.adduniform(gl, "divide", tboard.getdivide());
    
    setShadowmapShaderTexture(gl, currentData.tex, "weatherTex");
    setShadowmapShader1i(gl, tboard.getdivide(), "divide");
    
    updatePoisson(intervalToPoission(currentinterval));
  }
  
  
  private void setCamera(GL2GL3 gl){
    lookat(VIEW[0], VIEW[1], VIEW[2],
        VIEW[3], VIEW[4], VIEW[5],
        VIEW[6], VIEW[7], VIEW[8]);
    orthof(PROJ[0], PROJ[1], PROJ[2], PROJ[3], PROJ[4], PROJ[5]);
    //perspectivef(80, 1, 1, 50);
    setPROJ_SCALE(1 * 2);
    updatePVMatrix(gl);
    updatePVMatrixtess(gl);
  }
  
  public void setCameraAspect(GL2GL3 gl, float aspect){
    orthof(-1, 1, -aspect, aspect, 0, 100);
    viewmapshader.setFloat(gl, "canvasAspect", aspect);
    updatePVMatrix(gl);
    updatePVMatrixtess(gl);
    updatePoisson(intervalToPoission(currentinterval));
  }
  
  
  public void updatePoisson(double interval) {
    //intervalはsliderの0.01倍
    float offset = (float) (Math.abs(orthoproj[0] - orthoproj[1]) / 20);
    for(int i : choicedDataList){
      DataSet2D data = dataList.get(i);
      PoissonDiskSampler pds = 
          new PoissonDiskSampler(orthoproj[0] / viewscaling - viewcenter.x + offset,
              orthoproj[2] / viewscaling + viewcenter.y + offset, 
              orthoproj[1] / viewscaling - viewcenter.x - offset, 
              orthoproj[3] / viewscaling  + viewcenter.y -offset,
              interval / viewscaling, data.funcWindP);
      data.updatePoisson(pds);
    }
  }
  
  private float cylinderscale() {
    double tan = lights.get(0).posz 
        / (Math.sqrt(Math.pow(lights.get(0).posx, 2) 
        + Math.pow(lights.get(0).posy, 2)));    
    return (float) (0.9 * tan 
        * intervalToPoission(currentinterval) / (currentData.max())) * 13;
  }
  
  public void setlightdirectionrotate(double rad){
    lightdirectionrotate = (float)(rad / (2 * Math.PI) * 360);
  }
  
  public void setlightpos(int lightidx, float x, float y, float z){
    Light light = lights.get(lightidx);
    light.posx = x; light.posy = y; light.posz = z;
    light.update();
  }
  
  public void setlightpos(int lightidx, float x, float y){
    Light light = lights.get(lightidx);
    light.posx = x; light.posy = y;
    light.update();
  }
  
  public void setlightpospolar(int lightidx, double r, double rad){
    Light light = lights.get(lightidx);
    light.posx = (float) (r * Math.cos(rad)); 
    light.posy = (float) (r * Math.sin(rad));
    light.update();
  }
  
  //経度緯度から原点中心の width x height の板への射影
  private Vector2DDouble GeoCoordToWorldCoord(Vector2DDouble geocoord,
      double width, double height){
    //System.out.println(CENTER_OF_LATITUDE + " " + CENTER_OF_LONGITUDE);
    return new Vector2DDouble(
        //緯度
        (geocoord.x - CENTER_OF_LATITUDE - 0) / WIDTH_OF_LATITUDE * height,
        //経度
        (geocoord.y - CENTER_OF_LONGITUDE) / WIDTH_OF_LONGTITUDE * width);
  }
  
  static private double intervalToPoission(int value) {
    return (double) value / 100.0;
  }
  
  public void test1Rendering(GL2GL3 gl){
   
    billBoardShadowFBO(gl, billBoardShader);
    shadertess.use(gl);
    updateligths(gl, shadertess);
    board((GL4) gl);
    Shader.unuse(gl);
    
    gl.glFlush();
  }
  
  public void settingTest3(){
    updatePoisson(intervalToPoission(currentinterval));
  }
  
  public double test3SetProblem(){
    //currentData.chooseOneRandomPoint();
    currentData.chooseOnePointPercentRange(85, 98);
    return currentData.getDouble(currentData.getChosenPoint());
  }
  
  public void test3SetAnswer(float value){
    scaleAnswerValue = value;
  }
  
  public void test3Rendering1(GL2GL3 gl){
    
    shader.use(gl);
    updateligths(gl, shader);
    
    isTest3Rendering1 = true;
    billBoardShadowFBO(gl, billBoardShader);
    isTest3Rendering1 = false;
    
    board((GL4) gl);
    
    if(currentData.isChosen){
      billBoardAlpha(gl, billBoardShader, whiteCircle,
          currentData.getChosenPoint(), 0.7f);
    }
    
    cylinders(gl, flatShading);
    
    gl.glFlush();
  }
  
  public void test3Rendering2(GL2GL3 gl){
    
    shader.use(gl);
    updateligths(gl, shader);
    billBoardShadowFBO(gl, billBoardShader);
    
    board((GL4) gl);
    
    if(currentData.isChosen){
      billBoardAlpha(gl, billBoardShader, whiteCircle,
          currentData.getChosenPoint(), 0.7f);
    }
    
    gl.glFlush();
  }
  
  @Override 
  public void scene(GL2GL3 gl, Shader shader) {
    scene(gl, shader, true);
  }
  
  public void scene(GL2GL3 gl, Shader shader, boolean show) {
    
    shader.use(gl);
    
    cylinders(gl, shader);
    
    if(softshadow.state){
      if(!show){
        //billBoardShadowAlpha(gl);
        
        billBoardShadowFBO(gl, billBoardShader);
      }
    }
    
    if (mapAdjustmode.isSelected()){
      renderMarkPoint(gl, model, shader, markPointList);
    }
    Shader.unuse(gl);
    
    gl.glFlush();
  }
 
  private void cylinders(GL2GL3 gl, Shader shader){
    shader.use(gl);
    shader.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
    model.glLoadIdentity();
    for(int i=0; i < currentData.dist.size(); i++){
      model.glPushMatrix();
      Vector2DDouble pos = currentData.dist.get(i);
      cylinderCoreRendering(gl, shader, pos,
          (float)currentData.funcWind.getDouble(pos.x, pos.y));
      model.glPopMatrix();
    }
    if(currentData.isChosen){
      gl.glUniform3f(uniformFlatShaingInColor, 0.2f, 0.4f, 1f);
      cylinderCoreRendering(gl, shader, 
          currentData.getChosenPoint(), scaleAnswerValue);
      //System.out.println(scaleAnswerValue);
      gl.glUniform3f(uniformFlatShaingInColor, 1f, 1f, 1f);
    }
    Shader.unuse(gl);
  }
  
  private void cylinderCoreRendering(GL2GL3 gl, Shader shader, 
      Vector2DDouble pos, float value){
    //拡大
    model.glScalef(viewscaling, viewscaling, 1);
    //移動
    //pos.yに一時的にマイナスをつけてごまかす
    model.glTranslatef((float) (pos.x + viewcenter.x), (float) (-pos.y + viewcenter.y), 0);
    
    float shadowCylinderScale = 31f/200 * 2;
    cylinderscale = cylinderscale() * value * shadowCylinderScale;
    model.glScalef(cylinderscale, 
        cylinderscale, 
        cylinderscale * 3);
    
    //縮小
    model.glScalef(1f/viewscaling, 1f/viewscaling, 1);
    model.glRotatef(90, 1, 0, 0);
    shader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
    cylinder.rendering(gl);
  }
   
  private void billBoardShadowFBO(GL2GL3 gl, Shader shader){
    int[] viewport = new int[4];
    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
    gl.glViewport(0, 0, viewport[2], viewport[3]);
    
    shadowFBO.bind(gl);
    gl.glClearColor(1, 1, 1, 1);
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
    billBoardShadowCore(gl, shader);
    FBO.unbind(gl);
    
    gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
  }
  
  private void billBoardShadowCore(GL2GL3 gl, Shader shader){
    if(!softshadow.state) {
      return;
    }
    
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    
    model.glLoadIdentity();
    for(int i=0; i < currentData.dist.size(); i++){
      model.glPushMatrix();
      Vector2DDouble pos = currentData.dist.get(i);
      billBoardCoreRendering(gl, shader, shadow, pos,
          (float)currentData.funcWind.getDouble(pos.x, pos.y));
      model.glPopMatrix();
    }
    if(currentData.isChosen){
      float value;
      if(isTest3Rendering1){
        value = scaleAnswerValue;
      } else {
        value = (float)currentData.funcWind.getDouble(
            currentData.getChosenPoint().x, 
            currentData.getChosenPoint().y);
      }
      billBoardCoreRendering(gl, shader, shadow, 
          currentData.getChosenPoint(), value);
    }
    
    gl.glDisable(GL2.GL_BLEND);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
  private void billBoardCoreRendering(GL2GL3 gl, Shader shader,
      Billboard billboard, Vector2DDouble pos, float value){
    shader.use(gl);
    shader.updateMatrix(gl, view, Shader.MatrixType.VIEWPROJ);
    //拡大
    model.glScalef(viewscaling, viewscaling, 1);
    //移動
    //y成分に一時的にマイナスをつけてごまかす
    model.glTranslatef((float) (pos.x + viewcenter.x), (float) (-pos.y + viewcenter.y), 0);
    cylinderscale = cylinderscale() * value;
    model.glScalef(1f * cylinderscale, 
        1f * cylinderscale, 
        1);
    //縮小
    model.glScalef(1f/viewscaling, 1f/viewscaling, 1);
    model.glRotatef(135, 0, 0, 1);
    shader.updateMatrix(gl, model, Shader.MatrixType.MODEL);
    billboard.rendering(gl, shader);
  }
  
  private void billBoardAlpha(GL2GL3 gl, Shader shader, Billboard billboard,
      Vector2DDouble pos, float value){
    shader.use(gl);
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    model.glLoadIdentity();
    billBoardCoreRendering(gl, shader, billboard, pos, value);
    gl.glDisable(GL2.GL_BLEND);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }

  
  private void textureShadow(GL2GL3 gl){
    if(textureshadow.state){
      model.glLoadIdentity();
      model.glTranslatef(0, 0, 40);
      //model.glPushMatrix();
      for(float j = 0; j < 145; j+=2){
        model.glPushMatrix();
        //model.glTranslatef(0, -(-height/2 + height*(j/145)), 0);
        for(float i = 0; i < 201; i+=2){
          model.glPushMatrix();
          //model.glTranslatef(-width/2 + width*(i/201), 0, 0);
          //float tmp = datafb.get((int)(j*201+i))/100;
          //tmp *= 1.85;
          //tmp = 1;
          //System.out.println(tmp);
          //model.glRotatef(45, 0, 0, 1);
          //model.glScalef(width/201 * tmp, width/201*tmp, 1f);
          
          //model.glScalef(width/201, width/201, 1f);
          model.glPushMatrix();
          //model.glRotatef(90f, 0, 0, 1);
//          model.glScalef(Math.min(tmp*2f+0.2f,1.1f), 
//              Math.max(0.2f,0.2f+2f*(tmp-0.5f)), 
//              1f);
          updateModelMatrix(gl, shader, model);
          sphere.rendering(gl);
          model.glPopMatrix();
          
          //plus minus
//          model.glScalef(2*width/201, 2*width/201, 1f); 
//          model.glPushMatrix();
//          model.glScalef(2 * tmp, 0.1f, 1f);
//          updateModelMatrix(gl, shader, model);
//          box.rendering(gl);
//          model.glPopMatrix();
//          
//          model.glPushMatrix();
//          model.glScalef(0.1f, Math.max(0,2*1f*(tmp-0.5f)), 1f);
//          updateModelMatrix(gl, shader, model);
//          box.rendering(gl);
//          model.glPopMatrix();
          //end
          
          model.glPopMatrix();
        }
        model.glPopMatrix();
      }
    }
    
//    if(show & false){
//      model.glLoadIdentity();
//      model.glTranslatef(0, 0, 40);
//      //model.glPushMatrix();
//      for(float j = 0; j < 145; j+=2){
//        model.glPushMatrix();
//        model.glTranslatef(0, -(-height/2 + height*(j/145)), 0);
//        for(float i = 0; i < 201; i+=2){
//          model.glPushMatrix();
//          model.glTranslatef(-width/2 + width*(i/201), 0, 0);
//          float tmp = (float)Math.sqrt(Math.pow(windspeedu.getfloat((int)(j*201+i)),2)
//              +Math.pow(windspeedv.getfloat((int)(j*201+i)),2))/windspeedmax;
//          tmp *= 1.85;
//          //tmp = 1;
//          //System.out.println(tmp);
//          model.glScalef(width/201 * tmp, height/145*tmp, 0.02f);
//          updateModelMatrix(gl, shader, model);
//          hex.rendering(gl);
//          model.glPopMatrix();
//        }
//        model.glPopMatrix();
//      }
//    }
  }
  
  public void renderMarkInScene(GL2GL3 gl, Billboard obj, Vector2DDouble pos,
      float scale){
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_BLEND);
    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    shader.use(gl);
    model.glLoadIdentity();
    model.glTranslatef((float) pos.x, (float) pos.y, 0);
    model.glScalef(1f / viewscaling  * scale, 1f / viewscaling * scale, 1);
    updateModelMatrix(gl, shader, model);
    obj.rendering(gl, shader);
    gl.glFlush();
    gl.glDisable(GL2.GL_BLEND);
    gl.glEnable(GL2.GL_DEPTH_TEST);
  }
  
  private void renderMarkPoint(GL2GL3 gl, PMVMatrix model, Shader shader,
      List<Vector2DDouble> pointList){
    model.glLoadIdentity();
    model.glScalef(viewscaling, viewscaling, 1);
    model.glTranslatef((float) viewcenter.x, (float) viewcenter.y, 0);
    for (Vector2DDouble markpoint : pointList){
      model.glPushMatrix();
      model.glTranslatef((float) markpoint.y, (float) markpoint.x, 1);
      model.glScalef(1f/viewscaling  * 0.05f, 1f/viewscaling * 0.05f, 1);
      updateModelMatrix(gl, shader, model);
      cross.rendering(gl);
      model.glPopMatrix();
    }
  }
  
  public void scenetess(GL3 gl, Shader shader){
    scenetess(gl, shader, true);
  }
  
  //shadowmapの修正をあとでする
  @Override
  public void scenetess(GL3 gl, Shader shader, boolean show){
    shadertess.use(gl);
    updateligths(gl, shadertess);
    board((GL4) gl);
    Shader.unuse(gl);
    gl.glFlush();
  }
  
  private void board(GL4 gl){
    shadertess.use(gl);
    updateligths(gl, shadertess);
    if (MAP_CHANGED){
      setMapTex(gl, PATH_TO_MAP, shader);
    }
    
    model.glLoadIdentity();
    model.glScalef(viewscaling, viewscaling, 1);
    model.glTranslatef((float) viewcenter.x, (float) viewcenter.y, 0);
    updateModelMatrix(gl, shadertess, model);
    //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
    tboard.rendering((GL4) gl);
    Shader.unuse(gl);
  }
  
  private void setViewScaling(){
    viewscaling =
        (float) (Math.pow(2, viewScale.getValue() / 10.0) * PROJ_SCALE);
        //(float) ((double) (viewScale.getValue()) * PROJ_SCALE);
  }

  @Override
  public void iterate(){
    nochangeframe++;
    if(currentscale != viewScale.getValue()){
      setViewScaling();
      viewmapshader.setuniform("viewscaling", 
          (float) (1.0 / (viewscaling / PROJ_SCALE)));
      currentscale = viewScale.getValue();
      nochangeframe = 0;
    }
    
    if(currentinterval != possioninterval.getValue()){
      currentinterval = possioninterval.getValue();
      updatePoisson(intervalToPoission(currentinterval));
    }
    if(nochangeframe == 10){
      //updatePoisson(intervalToPoission(currentinterval));
    }
//    for(int j = 0; j < this.lightCount(); j++){
//      double angle = 2 * Math.PI * j / (this.lightCount()) + i/60;
//      //scene.lights.get(i).lookat(3, 3, i + 1, 0, 0, 0, 0, 1, 0);
//      this.lights.get(j).lookatd(
//          (3*Math.sin(angle)), 
//          (3*Math.cos(angle)), 
//          (5*Math.cos(angle / 3) + 10)*0 +4, 
//          0, 0, 0, 0, 1, 0);
//      //this.lights.get(j).perspectivef(90f, 1, 0.1f, 50f);
//      this.lights.get(j).orthof(-3, 3, -3, 3, 0.1f, 30);
//    }
//    setLightCircleLookOutside(2 * Math.sin(i/100), Math.cos(i/100), 
//        (2*Math.cos(i / 60) + 3),
//        Math.toRadians(0), i, 0.01);
//    setLightCircleLookOutside(0,0,2,0,i,1);
  }
  
  public void setLightCircleLookInside(double i, double z){
    for(int j = 0; j < this.lightCount(); j++){
      double angle = 2 * Math.PI * j / (this.lightCount()) + i;
      this.lights.get(j).lookatd(
          (3*Math.sin(angle)), 
          (3*Math.cos(angle)), 
          z, 
          0, 0, 0, 0, 1, 0);
      this.lights.get(j).orthof(-4, 4, -4, 4, 0.1f, 30);
    }
  }
  
  public void setLightCircleLookOutside(
      double eyeX, double eyeY, double eyeZ, double theta,
      double iterater, double speed, int uselightcount){
    float fovy = 120;
    if(uselightcount > 1){
      fovy = 360 / (uselightcount-1);
    }
    for(int i = 0; i < uselightcount-1; i++){
      double angle = 2 * Math.PI * i / (uselightcount-1) + iterater*speed;
      this.lights.get(i).lookatd(eyeX, eyeY, eyeZ, 
          eyeX + Math.sin(angle), eyeY + Math.cos(angle), eyeZ + Math.sin(Math.toRadians(theta)), 
          0, 1, 0);
      this.lights.get(i).perspectivef(fovy, 1, 0.01f, 30f);
    } 
    this.lights.get(uselightcount-1).lookatd(eyeX, eyeY, eyeZ, 
        eyeX, eyeY, eyeZ-1, 
        0, 1, 0);
    this.lights.get(uselightcount-1).perspectivef(120, 1, 0.1f, 30);
    virtualLightcount = this.lightCount()-uselightcount + 1;
//    this.lights.get(4).lookatd(
//      0, 
//    -3, 
//    3, 
//      0, 0, 0, 0, 1, 0);
//  this.lights.get(4).orthof(-3, 3, -3, 3, 0.1f, 30);
  }
  
  public void setLightCircleLookOutside(
      double eyeX, double eyeY, double eyeZ, double theta,
      double iterater, double speed){
    setLightCircleLookOutside(eyeX, eyeY, eyeZ, 
        theta, iterater, speed, this.lightCount());
  }



}
