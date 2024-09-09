package com.demo.service.Impl;

import com.demo.entity.*;
import com.demo.result.Result;
import com.demo.service.PolygonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.proj4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

@Service
public class PolygonServiceImpl implements PolygonService {

    @Value("${geoserver.url}")
    private String geoserverUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public ResultData comparePolygonForFarm(Map<String, List<List<Double>>> requestBody){
        List<List<Double>> polygonPoints = requestBody.get("polygon");

        // 将前端传入的经纬度坐标转换为JTS Polygon对象
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[polygonPoints.size() + 1];

        for (int i = 0; i < polygonPoints.size(); i++) {
            List<Double> point = polygonPoints.get(i);
            if (point.size() < 2) {
                System.err.println("Point size is less than 2 at index: " + i);
            }
            coordinates[i] = new Coordinate(point.get(0), point.get(1));
        }
        // 闭合多边形
        coordinates[polygonPoints.size()] = coordinates[0];

        // 打印转换前的坐标
        System.out.println("Original coordinates:");
        for (Coordinate coord : coordinates) {
            System.out.println(coord);
        }
        System.out.println("--------------------------------------------------------------------------");

        // 将坐标转换为CGCS2000_3_Degree_GK_Zone_37坐标系
        String sourceCRS = "EPSG:4326"; // WGS84坐标系
        String targetCRS = "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"; // CGCS2000_3_Degree_GK_Zone_37坐标系

        Coordinate[] transformedCoordinates = transformCoordinates1(coordinates, sourceCRS, targetCRS);

        // 打印经纬度转换成CGCS2000_3_Degree_GK_Zone_37后的坐标
        System.out.println("Transformed coordinates:");
        for (Coordinate coord : transformedCoordinates) {
            System.out.println(coord);
        }
        System.out.println("--------------------------------------------------------------------------");

        Polygon polygon = geometryFactory.createPolygon(transformedCoordinates);

        // 养殖场比对逻辑
        // 与GeoServer中的图层数据根据比对逻辑，计算得到交集面积
        ResultData resultData = getGeoServerFeaturesAndCalculateIntersectionForFarm(polygon);

        return resultData;
    }

    @Override
    public ResultData comparePolygonForRuralLands(Map<String, List<List<Double>>> requestBody) throws IOException {
        List<List<Double>> polygonPoints = requestBody.get("polygon");

        // 将前端传入的经纬度坐标转换为JTS Polygon对象
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[polygonPoints.size() + 1];

        for (int i = 0; i < polygonPoints.size(); i++) {
            List<Double> point = polygonPoints.get(i);
            if (point.size() < 2) {
                System.err.println("Point size is less than 2 at index: " + i);
            }
            coordinates[i] = new Coordinate(point.get(0), point.get(1));
        }
        // 闭合多边形
        coordinates[polygonPoints.size()] = coordinates[0];

        // 打印转换前的坐标
        System.out.println("Original coordinates:");
        for (Coordinate coord : coordinates) {
            System.out.println(coord);
        }
        System.out.println("--------------------------------------------------------------------------");

        // 将坐标转换为CGCS2000_3_Degree_GK_Zone_37坐标系
        String sourceCRS = "EPSG:4326"; // WGS84坐标系
        String targetCRS = "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"; // CGCS2000_3_Degree_GK_Zone_37坐标系

        Coordinate[] transformedCoordinates = transformCoordinates1(coordinates, sourceCRS, targetCRS);

        // 打印经纬度转换成CGCS2000_3_Degree_GK_Zone_37后的坐标
        System.out.println("Transformed coordinates:");
        for (Coordinate coord : transformedCoordinates) {
            System.out.println(coord);
        }
        System.out.println("--------------------------------------------------------------------------");

        Polygon polygon = geometryFactory.createPolygon(transformedCoordinates);

        // 农村宅基地比对逻辑
        // 与GeoServer中的图层数据根据比对逻辑，计算得到交集面积
        ResultData resultData = getGeoServerFeaturesAndCalculateIntersectionForRuralLands(polygon);

        return resultData;
    }

    /**
     * 养殖场比对逻辑
     * @param polygon
     * @return
     */
    private ResultData getGeoServerFeaturesAndCalculateIntersectionForFarm(Geometry polygon) {

        //返回结果类型
        ResultData resultData = new ResultData();
        //存储各个图层的交集面积和项目名称
        List<DataDetail> details = new ArrayList<>();
        //存储红色，黄色，绿色三种重叠区域的geojson信息
        OverLapArea overLapArea = new OverLapArea();
        //存储变量polygon,相当于全局变量,类型换成基类Geometry
        Map<String,Geometry> map = new HashMap();
        //Map<String,Geometry> map = new HashMap();
        map.put("new_polygon",polygon);
        //存储红色区域图层
        List<Geometry> red = new ArrayList<>();
        //存储黄色区域图层
        List<Geometry> yellow = new ArrayList<>();
        //存储绿色区域图层
        List<Geometry> green = new ArrayList<>();

        // 构建GeoServer查询生态保护红线URL
        String url_bhhx = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_bhhx");

        // 构建GeoServer查询永久基本农田URL
        String url_yjjbnt = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_yjjbnt");

        // 构建GeoServer查询高标农田URL
        String url_gbnt = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_22gb");

        // 构建GeoServer查询转化为国空URL
        String url_zhwgk = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_zhwgk");

        //System.out.println("GeoServer URL: " + url);

        // 发起请求获取生态保护红线图层数据
        ResponseEntity<String> response_bhhx = restTemplate.getForEntity(url_bhhx, String.class);

        System.out.println("GeoServer response_bhhx status: " + response_bhhx.getStatusCode());

        if (response_bhhx.getStatusCode().is2xxSuccessful()) {
            String responseBody_bhhx = response_bhhx.getBody();
            //System.out.println("GeoServer response body: " + responseBody);
            System.out.println("--------------------------------------------------------------------------");
            try {
                // 解析GeoServer返回的JSON数据
                JsonNode rootNode_bhhx = objectMapper.readTree(responseBody_bhhx);
                JsonNode features_bhhx = rootNode_bhhx.path("features");

                //TODO:创建一个存储绘制区域与生态保护红线交集的区域集合
                List<Geometry> bhhx = new ArrayList<>();
                DataDetail dataDetail_bhhx = new DataDetail();
                Areas areas_bhhx = new Areas();

                for (JsonNode feature_bhhx : features_bhhx) {
//                    DataDetail dataDetail = new DataDetail();
//                    Areas areas = new Areas();
                    JsonNode geometryNode_bhhx = feature_bhhx.path("geometry");

                    //保存项目名信息到DataDetail实体类
                    dataDetail_bhhx.setProjectName(feature_bhhx.path("properties").path("HXMC").asText());

                    // 检查几何数据是否存在
                    if (geometryNode_bhhx.isMissingNode() || geometryNode_bhhx.isNull()) {
                        System.err.println("Geometry node is missing or null for feature ID: " + feature_bhhx.path("id").asText());
                        continue;
                    }

                    // 将GeoJSON几何数据解析为JTS Geometry对象
                    Geometry layerGeometry_bhhx = parseGeoJsonToGeometry(geometryNode_bhhx);
                    if (!layerGeometry_bhhx.isEmpty()) {
                        // 打印图层几何信息
                        //System.out.println("Layer geometry:");
                        //System.out.println(layerGeometry);

                        IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                        IsValidOp isValidOp2 = new IsValidOp(layerGeometry_bhhx);

                        if(isValidOp1.isValid() && isValidOp2.isValid()){
                            Geometry intersection_bhhx = map.get("new_polygon").intersection(layerGeometry_bhhx);

                            if(!intersection_bhhx.isEmpty()){
                                IsValidOp isValidOp_bhhx = new IsValidOp(intersection_bhhx);
                                if(isValidOp_bhhx.isValid()){
                                    bhhx.add(intersection_bhhx);
                                }
                            }
                        }

                    } else {
                        System.err.println("Layer geometry is null for feature ID: " + feature_bhhx.path("id").asText());
                    }
                }

                if (!bhhx.isEmpty()) {

                    Geometry bhhxAll = UnaryUnionOp.union(bhhx);
                    double bhhxAllintersectionArea = bhhxAll.getArea();

                    //保存重叠面积信息到DataDetail实体类
                    areas_bhhx.setAreaId("");
                    areas_bhhx.setArea(String.format("%.2f",bhhxAllintersectionArea));
                    dataDetail_bhhx.setAreas(areas_bhhx);

                    // 生成包含重合区域和原先绘制区域的图片
                    BufferedImage image = createImageWithPolygons1(polygon, bhhxAll);

                    // 将图像转换为 Base64 编码的字符串
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", baos);
                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                    //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                    //保存生成的重叠面积图片到DataDetail实体类
                    dataDetail_bhhx.setImg(base64Image);

                    dataDetail_bhhx.setTag(0);

                    details.add(dataDetail_bhhx);

                    IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                    IsValidOp isValidOp2 = new IsValidOp(bhhxAll);
                    if(isValidOp1.isValid() && isValidOp2.isValid()){
                        Geometry new_polygon = map.get("new_polygon").difference(bhhxAll);
                        map.put("new_polygon",new_polygon);

                        //传入红色区域
                        red.add(bhhxAll);

                        System.out.println("绘制区域与：【"+dataDetail_bhhx.getProjectName()+"】存在"+dataDetail_bhhx.getAreas().getArea()+"平方米的重合面积，不予通过");

                    }
                }

                //TODO:开始比对永久基本农田逻辑

                // 发起请求获取永久基本农田图层数据
                ResponseEntity<String> response_yjjbnt = restTemplate.getForEntity(url_yjjbnt, String.class);

                System.out.println("GeoServer response_yjjbnt status: " + response_yjjbnt.getStatusCode());

                if (response_yjjbnt.getStatusCode().is2xxSuccessful()) {

                    String response_yjjbntBody = response_yjjbnt.getBody();
                    System.out.println("--------------------------------------------------------------------------");

                    // 解析GeoServer返回的JSON数据
                    JsonNode yjjbnt_rootNode = objectMapper.readTree(response_yjjbntBody);
                    JsonNode features_yjjbnt = yjjbnt_rootNode.path("features");

                    //TODO:创建一个存储绘制区域与永久基本农田的交集区域
                    List<Geometry> yjjbnt = new ArrayList<>();
                    DataDetail dataDetail_yjjbnt = new DataDetail();
                    Areas areas_yjjbnt = new Areas();

                    for (JsonNode feature_yjjbnt : features_yjjbnt) {
                        JsonNode geometryNode_yjjbnt = feature_yjjbnt.path("geometry");

                        //保存项目名信息到DataDetail实体类
                        dataDetail_yjjbnt.setProjectName("永久基本农田:"+feature_yjjbnt.path("properties").path("DLMC").asText());

                        // 检查几何数据是否存在
                        if (geometryNode_yjjbnt.isMissingNode() || geometryNode_yjjbnt.isNull()) {
                            System.err.println("Geometry node is missing or null for feature ID: " + feature_yjjbnt.path("id").asText());
                            continue;
                        }

                        // 将GeoJSON几何数据解析为JTS Geometry对象
                        Geometry layerGeometry_yjjbnt = parseGeoJsonToGeometry(geometryNode_yjjbnt);
                        if (!layerGeometry_yjjbnt.isEmpty()) {

                            IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                            IsValidOp isValidOp2 = new IsValidOp(layerGeometry_yjjbnt);
                            if(isValidOp1.isValid() && isValidOp2.isValid()){
                                Geometry intersection_yjjbnt = map.get("new_polygon").intersection(layerGeometry_yjjbnt);

                                if(!intersection_yjjbnt.isEmpty()){
                                    IsValidOp isValidOp_yjjbnt = new IsValidOp(intersection_yjjbnt);
                                    if(isValidOp_yjjbnt.isValid()){
                                        yjjbnt.add(intersection_yjjbnt);
                                    }
                                }
                            }



                        } else {
                            System.err.println("Layer geometry is null for feature ID: " + feature_yjjbnt.path("id").asText());
                        }

                    }

                    if (!yjjbnt.isEmpty()) {

                        Geometry yjjbntAll = UnaryUnionOp.union(yjjbnt);
                        double yjjbntAllintersectionArea = yjjbntAll.getArea();
                        //Coordinate[] yjjbntAllintersectionCoordinates = yjjbntAll.getCoordinates();
                        //Coordinate[] yjjbntAllcoordinates = transformCoordinates2(yjjbntAllintersectionCoordinates, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                        //dataDetail_yjjbnt.setIntersectionCoordinates(yjjbntAllcoordinates);
                        //保存重叠面积信息到DataDetail实体类
                        areas_yjjbnt.setAreaId("");
                        areas_yjjbnt.setArea(String.format("%.2f",yjjbntAllintersectionArea));
                        dataDetail_yjjbnt.setAreas(areas_yjjbnt);

                        // 生成包含重合区域和原先绘制区域的图片
                        BufferedImage image = createImageWithPolygons1(polygon, yjjbntAll);

                        // 将图像转换为 Base64 编码的字符串
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", baos);
                        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                        //保存生成的重叠面积图片到DataDetail实体类
                        dataDetail_yjjbnt.setImg(base64Image);
                        dataDetail_yjjbnt.setTag(0);

                        details.add(dataDetail_yjjbnt);

                        IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                        IsValidOp isValidOp2 = new IsValidOp(yjjbntAll);

                        if(isValidOp1.isValid() && isValidOp2.isValid()){
                            Geometry new_polygon = map.get("new_polygon").difference(yjjbntAll);
                            map.put("new_polygon",new_polygon);

                            //传入红色区域
                            red.add(yjjbntAll);

                            System.out.println("绘制区域与：【"+dataDetail_yjjbnt.getProjectName()+"】存在"+dataDetail_yjjbnt.getAreas().getArea()+"平方米的重合面积，不予通过");

                        }
                    }

                    //TODO:开始比对高标农田逻辑
                    //开始比对高标农田逻辑

                    // 发起请求获取高标农田图层数据
                    ResponseEntity<String> response_gbnt = restTemplate.getForEntity(url_gbnt, String.class);

                    System.out.println("GeoServer response_gbnt status: " + response_gbnt.getStatusCode());

                    if (response_gbnt.getStatusCode().is2xxSuccessful()) {

                        String response_gbntBody = response_gbnt.getBody();
                        System.out.println("--------------------------------------------------------------------------");

                        // 解析GeoServer返回的JSON数据
                        JsonNode gbnt_rootNode = objectMapper.readTree(response_gbntBody);
                        JsonNode features_gbnt = gbnt_rootNode.path("features");

                        //TODO:创建一个存储绘制区域与高标农田的交集区域
                        List<Geometry> gbnt = new ArrayList<>();
                        DataDetail dataDetail_gbnt = new DataDetail();
                        Areas areas_gbnt = new Areas();

                        for (JsonNode feature_gbnt : features_gbnt) {
                            JsonNode geometryNode_gbnt = feature_gbnt.path("geometry");

                            //保存项目名信息到DataDetail实体类
                            dataDetail_gbnt.setProjectName(feature_gbnt.path("properties").path("项目名").asText());

                            // 检查几何数据是否存在
                            if (geometryNode_gbnt.isMissingNode() || geometryNode_gbnt.isNull()) {
                                System.err.println("Geometry node is missing or null for feature ID: " + feature_gbnt.path("id").asText());
                                continue;
                            }

                            // 将GeoJSON几何数据解析为JTS Geometry对象
                            Geometry layerGeometry_gbnt = parseGeoJsonToGeometry(geometryNode_gbnt);
                            if (!layerGeometry_gbnt.isEmpty()) {

                                IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                                IsValidOp isValidOp2 = new IsValidOp(layerGeometry_gbnt);
                                if(isValidOp1.isValid() && isValidOp2.isValid()){
                                    Geometry intersection_gbnt = map.get("new_polygon").intersection(layerGeometry_gbnt);

                                    if(!intersection_gbnt.isEmpty()){
                                        IsValidOp isValidOp_gbnt = new IsValidOp(intersection_gbnt);
                                        if(isValidOp_gbnt.isValid()){
                                            gbnt.add(intersection_gbnt);
                                        }
                                    }
                                }

                            } else {
                                System.err.println("Layer geometry is null for feature ID: " + feature_gbnt.path("id").asText());
                            }
                        }

                        if (!gbnt.isEmpty()) {

                            Geometry gbntAll = UnaryUnionOp.union(gbnt);
                            double gbntAllintersectionArea = gbntAll.getArea();
                            //Coordinate[] gbntAllintersectionCoordinates = gbntAll.getCoordinates();
                            //Coordinate[] gbntAllcoordinates = transformCoordinates2(gbntAllintersectionCoordinates, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                            //dataDetail_gbnt.setIntersectionCoordinates(gbntAllcoordinates);
                            //保存重叠面积信息到DataDetail实体类
                            areas_gbnt.setAreaId("");
                            areas_gbnt.setArea(String.format("%.2f",gbntAllintersectionArea));
                            dataDetail_gbnt.setAreas(areas_gbnt);

                            // 生成包含重合区域和原先绘制区域的图片
                            BufferedImage image = createImageWithPolygons1(polygon, gbntAll);

                            // 将图像转换为 Base64 编码的字符串
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "png", baos);
                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                            //保存生成的重叠面积图片到DataDetail实体类
                            dataDetail_gbnt.setImg(base64Image);
                            dataDetail_gbnt.setTag(0);

                            details.add(dataDetail_gbnt);

                            IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                            IsValidOp isValidOp2 = new IsValidOp(gbntAll);
                            if(isValidOp1.isValid() && isValidOp2.isValid()){
                                Geometry new_polygon = map.get("new_polygon").difference(gbntAll);
                                map.put("new_polygon",new_polygon);

                                //传入红色区域
                                red.add(gbntAll);

                                System.out.println("绘制区域与：【"+dataDetail_gbnt.getProjectName()+"】存在"+dataDetail_gbnt.getAreas().getArea()+"平方米的重合面积，不予通过");

                            }
                        }

                        //TODO:开始比对转化为国空的数据

                        // 发起请求获取转化为国空图层数据
                        ResponseEntity<String> response_zhwkg = restTemplate.getForEntity(url_zhwgk, String.class);

                        System.out.println("GeoServer response_zhwkg status: " + response_zhwkg.getStatusCode());

                        if (response_zhwkg.getStatusCode().is2xxSuccessful()) {
                            String response_zhwkgBody = response_zhwkg.getBody();
                            System.out.println("--------------------------------------------------------------------------");

                            // 解析GeoServer返回的JSON数据
                            JsonNode rootNode_zhwgk = objectMapper.readTree(response_zhwkgBody);
                            JsonNode features_zhwgk = rootNode_zhwgk.path("features");

                            //TODO:创建一个存储绘制区域与转化为国空的交集区域
                            List<Geometry> zhwgk_red = new ArrayList<>();
                            List<Geometry> zhwgk_yellow = new ArrayList<>();

                            //创建一个集合存放用地用海分类和对应的重合面积，以此过滤重复类型
                            Map<String,Geometry> classMap = new HashMap<>();

                            for (JsonNode feature_zhwgk : features_zhwgk) {
                                DataDetail dataDetail_zhwgk = new DataDetail();
                                Areas areas_zhwgk = new Areas();
                                JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");

                                //保存项目名信息到DataDetail_zhwgk实体类
                                dataDetail_zhwgk.setProjectName("用地用海分类");

                                // 检查几何数据是否存在
                                if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                                    System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                                    continue;
                                }

                                // 将GeoJSON几何数据解析为JTS Geometry对象
                                Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                                if (!layerGeometry_zhwgk.isEmpty()) {

                                    IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                                    IsValidOp isValidOp2 = new IsValidOp(layerGeometry_zhwgk);
                                    if(isValidOp1.isValid() && isValidOp2.isValid()){
                                        Geometry intersection_zhwgk = map.get("new_polygon").intersection(layerGeometry_zhwgk);

                                        if (!intersection_zhwgk.isEmpty()) {

                                            double intersectionArea_zhwgk = intersection_zhwgk.getArea();
                                            String code = feature_zhwgk.path("properties").path("转换为国空").asText();

                                            //如果当前遍历的区域其转化为国空字段在classMap中不存在，那么视为新用地用海分类
                                            if(!classMap.containsKey(code)){

                                                if(isNotPass(code)){
                                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();
                                                    //Coordinate[] intersectionCoordinates_zhwgk = intersection_zhwgk.getCoordinates();
                                                    //Coordinate[] coordinates_zhwgk = transformCoordinates2(intersectionCoordinates_zhwgk, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                                                    //dataDetail_zhwgk.setIntersectionCoordinates(coordinates_zhwgk);
                                                    //保存重叠面积信息到DataDetail实体类
                                                    areas_zhwgk.setAreaId(featureId_zhwgk);
                                                    areas_zhwgk.setArea(String.format("%.2f", intersectionArea_zhwgk));
                                                    dataDetail_zhwgk.setAreas(areas_zhwgk);

                                                    // 生成包含重合区域和原先绘制区域的图片
                                                    BufferedImage image = createImageWithPolygons1(polygon, intersection_zhwgk);

                                                    // 将图像转换为 Base64 编码的字符串
                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    ImageIO.write(image, "png", baos);
                                                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                                                    //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                                                    //保存生成的重叠面积图片到DataDetail实体类
                                                    dataDetail_zhwgk.setImg(base64Image);
                                                    dataDetail_zhwgk.setTag(0);

                                                    details.add(dataDetail_zhwgk);

                                                    IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                                    if(isValidOp_zhwgk.isValid()){
                                                        zhwgk_red.add(intersection_zhwgk);
                                                        classMap.put(code,intersection_zhwgk);
                                                        System.out.println("绘制区域与：【"+dataDetail_zhwgk.getProjectName()+":"+dataDetail_zhwgk.getAreas().getAreaId()+"】存在"+dataDetail_zhwgk.getAreas().getArea()+"平方米的重合面积,不予通过");
                                                    }

                                                }else if(isWaitPass(code)){

                                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();
                                                    //Coordinate[] intersectionCoordinates_zhwgk = intersection_zhwgk.getCoordinates();
                                                    //Coordinate[] coordinates_zhwgk = transformCoordinates2(intersectionCoordinates_zhwgk, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                                                    //dataDetail_zhwgk.setIntersectionCoordinates(coordinates_zhwgk);
                                                    //保存重叠面积信息到DataDetail实体类
                                                    areas_zhwgk.setAreaId(featureId_zhwgk);
                                                    areas_zhwgk.setArea(String.format("%.2f", intersectionArea_zhwgk));
                                                    dataDetail_zhwgk.setAreas(areas_zhwgk);

                                                    // 生成包含重合区域和原先绘制区域的图片
                                                    BufferedImage image = createImageWithPolygons2(polygon, intersection_zhwgk);

                                                    // 将图像转换为 Base64 编码的字符串
                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    ImageIO.write(image, "png", baos);
                                                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                                                    //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                                                    //保存生成的重叠面积图片到DataDetail实体类
                                                    dataDetail_zhwgk.setImg(base64Image);
                                                    dataDetail_zhwgk.setTag(1);

                                                    details.add(dataDetail_zhwgk);

                                                    IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                                    if(isValidOp_zhwgk.isValid()){
                                                        zhwgk_yellow.add(intersection_zhwgk);

                                                        classMap.put(code,intersection_zhwgk);

                                                        System.out.println("绘制区域与：【"+dataDetail_zhwgk.getProjectName()+":"+dataDetail_zhwgk.getAreas().getAreaId()+"】存在"+dataDetail_zhwgk.getAreas().getArea()+"平方米的重合面积,办理手续后通过");
                                                    }

                                                }
                                            }else{
                                                //当前遍历的区域其转化为国空字段在classMap中存在，重复的用地用海分类，进行更新details中的数据
                                                if(isNotPass(code)){

                                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();

                                                    //在details中找到对应的分类数据,更新detail数据
                                                    for (DataDetail detail : details) {
                                                        if(detail.getAreas().getAreaId().equals(featureId_zhwgk)){
                                                            detail.getAreas().setArea(String.format("%.2f", Double.parseDouble(detail.getAreas().getArea()) + intersectionArea_zhwgk));

                                                            // 生成包含重合区域和原先绘制区域的图片

//                                                        Geometry geometry = classMap.get(code);
//                                                        if (!geometry.isValid()) {
//                                                            geometry = GeometryFixer.fix(geometry);
//                                                        }
//
//                                                        if (!intersection_zhwgk.isValid()) {
//                                                            intersection_zhwgk = GeometryFixer.fix(intersection_zhwgk);
//                                                        }
//
//                                                        // 添加一个非常小的缓冲区以修复几何对象
//                                                        geometry = geometry.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                                        intersection_zhwgk = intersection_zhwgk.buffer(0.0).buffer(-0.0001).buffer(0.0);


                                                            BufferedImage image = createImageWithPolygons1(polygon, intersection_zhwgk.union(classMap.get(code)));

                                                            // 将图像转换为 Base64 编码的字符串
                                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                            ImageIO.write(image, "png", baos);
                                                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                                                            detail.setImg(base64Image);

                                                            IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                                            if(isValidOp_zhwgk.isValid()){
                                                                zhwgk_red.add(intersection_zhwgk);

                                                                classMap.put(code,intersection_zhwgk.union(classMap.get(code)));

                                                                System.out.println("绘制区域与：更新后的【"+detail.getProjectName()+":"+detail.getAreas().getAreaId()+"】存在"+detail.getAreas().getArea()+"平方米的重合面积,不予通过");

                                                            }
                                                        }
                                                    }


                                                }else if(isWaitPass(code)){

                                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();

                                                    //在details中找到对应的分类数据,更新detail数据
                                                    for (DataDetail detail : details) {
                                                        if(detail.getAreas().getAreaId().equals(featureId_zhwgk)){
                                                            detail.getAreas().setArea(String.format("%.2f", Double.parseDouble(detail.getAreas().getArea()) + intersectionArea_zhwgk));

                                                            // 生成包含重合区域和原先绘制区域的图片

//                                                        Geometry geometry = classMap.get(code);
//                                                        if (!geometry.isValid()) {
//                                                            geometry = GeometryFixer.fix(geometry);
//                                                        }
//
//                                                        if (!intersection_zhwgk.isValid()) {
//                                                            intersection_zhwgk = GeometryFixer.fix(intersection_zhwgk);
//                                                        }
//
//                                                        // 添加一个非常小的缓冲区以修复几何对象
//                                                        geometry = geometry.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                                        intersection_zhwgk = intersection_zhwgk.buffer(0.0).buffer(-0.0001).buffer(0.0);


                                                            BufferedImage image = createImageWithPolygons2(polygon, intersection_zhwgk.union(classMap.get(code)));

                                                            // 将图像转换为 Base64 编码的字符串
                                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                            ImageIO.write(image, "png", baos);
                                                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                                                            detail.setImg(base64Image);

                                                            IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                                            if(isValidOp_zhwgk.isValid()){
                                                                zhwgk_yellow.add(intersection_zhwgk);

                                                                classMap.put(code,intersection_zhwgk.union(classMap.get(code)));

                                                                System.out.println("绘制区域与：更新后的【"+detail.getProjectName()+":"+detail.getAreas().getAreaId()+"】存在"+detail.getAreas().getArea()+"平方米的重合面积,不予通过");

                                                            }
                                                        }
                                                    }

                                                }
                                            }

                                        } else {
                                            //System.out.println("No intersection with feature ID: " + feature_zhwgk.path("id").asText());
                                        }
                                    }

                                } else {
                                    System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                                }
                            }

                            if (!zhwgk_red.isEmpty()) {

                                Geometry zhwgkRedAll = UnaryUnionOp.union(zhwgk_red);

                                // 在进行相减运算的时候，先检查并修复几何对象的有效性
                                Geometry new_polygon = map.get("new_polygon");
//                                if (!new_polygon.isValid()) {
//                                    new_polygon = GeometryFixer.fix(new_polygon);
//                                }
//
//                                if (!zhwgkRedAll.isValid()) {
//                                    zhwgkRedAll = GeometryFixer.fix(zhwgkRedAll);
//                                }
//
//                                // 添加一个非常小的缓冲区以修复几何对象
//                                new_polygon = new_polygon.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                zhwgkRedAll = zhwgkRedAll.buffer(0.0).buffer(-0.0001).buffer(0.0);

                                // 计算相减结果
                                IsValidOp isValidOp1 = new IsValidOp(new_polygon);
                                IsValidOp isValidOp2 = new IsValidOp(zhwgkRedAll);
                                if(isValidOp1.isValid() && isValidOp2.isValid()){
                                    new_polygon = new_polygon.difference(zhwgkRedAll);

                                    map.put("new_polygon",new_polygon);

                                    //传入红色区域
                                    red.add(zhwgkRedAll);
                                }

                            }

                            if (!zhwgk_yellow.isEmpty()) {

                                Geometry zhwgkYellowAll = UnaryUnionOp.union(zhwgk_yellow);

                                // 在进行相减运算的时候，先检查并修复几何对象的有效性
                                Geometry new_polygon = map.get("new_polygon");
//                                if (!new_polygon.isValid()) {
//                                    new_polygon = GeometryFixer.fix(new_polygon);
//                                }
//
//                                if (!zhwgkYellowAll.isValid()) {
//                                    zhwgkYellowAll = GeometryFixer.fix(zhwgkYellowAll);
//                                }
//
//                                // 添加一个非常小的缓冲区以修复几何对象
//                                new_polygon = new_polygon.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                zhwgkYellowAll = zhwgkYellowAll.buffer(0.0).buffer(-0.0001).buffer(0.0);

                                //计算相减结果
                                IsValidOp isValidOp1 = new IsValidOp(new_polygon);
                                IsValidOp isValidOp2 = new IsValidOp(zhwgkYellowAll);
                                if(isValidOp1.isValid() && isValidOp2.isValid()){

                                    // 使用 GeometryPrecisionReducer 减少精度
                                    PrecisionModel precisionModel = new PrecisionModel(1000);
                                    GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(precisionModel);
                                    Geometry reducedPrecisionRedUnion = precisionReducer.reduce(new_polygon);

                                    new_polygon = reducedPrecisionRedUnion.difference(zhwgkYellowAll);
                                    map.put("new_polygon",new_polygon);

                                    //传入黄色区域
                                    yellow.add(zhwgkYellowAll);
                                }

                            }

//                            if((red.get("red") == null) && (yellow.get("yellow") == null)){
//                                green.put("green",polygon);
//                            }
//
//                            if((red.get("red") != null) && (yellow.get("yellow") == null)){
//                                green.put("green",polygon.difference(red.get("red")));
//                            }
//
//                            if((red.get("red") == null) && (yellow.get("yellow") != null)){
//                                green.put("green",polygon.difference(yellow.get("yellow")));
//                            }
//
//                            if((red.get("red") != null) && (yellow.get("yellow") != null)){
//                                green.put("green",polygon.difference(red.get("red")).difference(yellow.get("yellow")));
//                            }

                            //TODO:比对逻辑结束
                            //更新map集合中的值，换成原始传入图形数据
                            map.put("new_polygon",polygon);
                            //System.out.println(polygon);

                            if(!red.isEmpty()){
                                // 转换几何对象
                                Geometry redUnion = UnaryUnionOp.union(red);
                                createImageWithPolygons1(polygon, redUnion);
                                IsValidOp isValidOp_redUnion = new IsValidOp(redUnion);
                                if(isValidOp_redUnion.isValid()){

                                    // 使用 GeometryPrecisionReducer 减少精度
                                    PrecisionModel precisionModel = new PrecisionModel(1000);
                                    GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(precisionModel);
                                    Geometry reducedPrecisionRedUnion = precisionReducer.reduce(map.get("new_polygon"));

                                    Geometry new_polygon = reducedPrecisionRedUnion.difference(redUnion);
                                    map.put("new_polygon",new_polygon);

                                    // 定义源和目标坐标系
                                    CRSFactory crsFactory = new CRSFactory();
                                    CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                                    CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                                    // 创建转换器
                                    CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                                    CoordinateTransform transform = ctFactory.createTransform(sourceCRS1, targetCRS1);
                                    Geometry transformedGeometry = transformGeometry(redUnion, transform);

                                    // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                                    GeometryJSON gjson = new GeometryJSON(14);
                                    StringWriter writer = new StringWriter();
                                    gjson.write(transformedGeometry, writer);

                                    // 设置结果
                                    overLapArea.setRedGeoJson(writer.toString());
                                }

                            }
                            if(!yellow.isEmpty()){
                                // 转换几何对象
                                Geometry yellowUnion = UnaryUnionOp.union(yellow);
                                createImageWithPolygons2(polygon, yellowUnion);
                                //System.out.println(yellowUnion);
                                // 检查并简化几何图形（如果需要）
                                Geometry new_polygon = map.get("new_polygon");
                                //new_polygon = DouglasPeuckerSimplifier.simplify(new_polygon, 0.001);
                                //yellowUnion = DouglasPeuckerSimplifier.simplify(yellowUnion, 0.001);
                                IsValidOp isValidOp1 = new IsValidOp(new_polygon);
                                IsValidOp isValidOp2 = new IsValidOp(yellowUnion);
                                if(isValidOp1.isValid() && isValidOp2.isValid()){

                                    // 使用 GeometryPrecisionReducer 减少精度
                                    PrecisionModel precisionModel = new PrecisionModel(1000);
                                    GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(precisionModel);
                                    Geometry reducedPrecisionRedUnion = precisionReducer.reduce(new_polygon);

                                    new_polygon = reducedPrecisionRedUnion.difference(yellowUnion);
                                    map.put("new_polygon",new_polygon);

                                    // 定义源和目标坐标系
                                    CRSFactory crsFactory = new CRSFactory();
                                    CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                                    CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                                    // 创建转换器
                                    CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                                    CoordinateTransform transform = ctFactory.createTransform(sourceCRS1, targetCRS1);

                                    Geometry transformedGeometry = transformGeometry(yellowUnion, transform);
                                    // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                                    GeometryJSON gjson = new GeometryJSON(14);
                                    StringWriter writer = new StringWriter();
                                    gjson.write(transformedGeometry, writer);

                                    // 设置结果
                                    overLapArea.setYellowGeoJson(writer.toString());
                                }

                            }

                            //设置绿色区域数据
                            // 转换几何对象
                            Geometry greenUnion = map.get("new_polygon");
                            createImageWithPolygons3(polygon, greenUnion);
                            //System.out.println(greenUnion);
                            //Geometry greenUnion = polygon.difference((UnaryUnionOp.union(yellow)));
                            IsValidOp isValidOp = new IsValidOp(greenUnion);
                            if(isValidOp.isValid()){

                                // 定义源和目标坐标系
                                CRSFactory crsFactory = new CRSFactory();
                                CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                                CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                                // 创建转换器
                                CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                                CoordinateTransform transform = ctFactory.createTransform(sourceCRS1, targetCRS1);

                                Geometry transformedGeometry = transformGeometry(greenUnion, transform);
                                // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                                GeometryJSON gjson = new GeometryJSON(14);
                                StringWriter writer = new StringWriter();
                                gjson.write(transformedGeometry, writer);

                                // 设置结果
                                overLapArea.setGreenGeoJson(writer.toString());

                                resultData.setOverLapArea(overLapArea);
                                resultData.setDataDetails(details);

                                return resultData;
                            }

                        }

                    }


                }


            } catch (IOException e) {
                System.err.println("Failed to parse response JSON with error: " + e.getMessage());
            }
        } else {
            System.err.println("Failed to fetch data from GeoServer. Status Code: " + response_bhhx.getStatusCode());
            return null;
        }

        return resultData;
    }

    /**
     * 农村宅基地比对逻辑
     * @param polygon
     * @return
     */
    private ResultData getGeoServerFeaturesAndCalculateIntersectionForRuralLands(Geometry polygon) throws IOException {

        //返回结果类型
        ResultData resultData = new ResultData();
        //存储各个图层的交集面积和项目名称
        List<DataDetail> details = new ArrayList<>();
        //存储红色，黄色，绿色三种重叠区域的geojson信息
        OverLapArea overLapArea = new OverLapArea();
        //存储变量polygon,相当于全局变量,类型换成基类Geometry
        Map<String,Geometry> map = new HashMap();
        //Map<String,Geometry> map = new HashMap();
        //map.put("new_polygon",polygon);
        //存储红色区域图层
        List<Geometry> red = new ArrayList<>();
        //存储黄色区域图层
        List<Geometry> yellow = new ArrayList<>();
        //存储绿色区域图层
        List<Geometry> green = new ArrayList<>();

        //先进行绘制区域与生态保护红线，永久基本农田，高标农田的比对逻辑
        FormerRedData formerRedDataForBhYjGb = getFormerRedDataFor_BH_YJ_GB(polygon);

        //进行数据的填充
        if(!formerRedDataForBhYjGb.getDetails().isEmpty()){
            for (int i = 0; i < formerRedDataForBhYjGb.getDetails().size(); i++) {
                DataDetail dataDetail = formerRedDataForBhYjGb.getDetails().get(i);
                details.add(dataDetail);
            }
        }

        if (!formerRedDataForBhYjGb.getMap().isEmpty()){
            map = formerRedDataForBhYjGb.getMap();
        }else{
            map.put("new_polygon",polygon);
        }

        if(!formerRedDataForBhYjGb.getRed().isEmpty()){
            for (int i = 0; i < formerRedDataForBhYjGb.getRed().size(); i++) {
                red.add(formerRedDataForBhYjGb.getRed().get(i));
            }
        }

        // 构建GeoServer查询转化为国空URL
        String url_zhwgk = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_zhwgk");

        //TODO:开始比对转化为国空的数据

        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_zhwkg = restTemplate.getForEntity(url_zhwgk, String.class);

        System.out.println("GeoServer response_zhwkg status: " + response_zhwkg.getStatusCode());

        if (response_zhwkg.getStatusCode().is2xxSuccessful()) {
            String response_zhwkgBody = response_zhwkg.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_zhwgk = objectMapper.readTree(response_zhwkgBody);
            JsonNode features_zhwgk = rootNode_zhwgk.path("features");

            //TODO:创建一个存储绘制区域与转化为国空的交集区域
            List<Geometry> zhwgk_red = new ArrayList<>();
            List<Geometry> zhwgk_yellow = new ArrayList<>();

            //创建一个集合存放用地用海分类和对应的重合面积，以此过滤重复类型
            Map<String,Geometry> classMap = new HashMap<>();

            //TODO:转化为国空布置宅基地要考虑距离因素，暂没有实现

            for (JsonNode feature_zhwgk : features_zhwgk) {
                DataDetail dataDetail_zhwgk = new DataDetail();
                Areas areas_zhwgk = new Areas();
                JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");

                //保存项目名信息到DataDetail_zhwgk实体类
                dataDetail_zhwgk.setProjectName("用地用海分类");

                // 检查几何数据是否存在
                if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                    System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                    continue;
                }

                // 将GeoJSON几何数据解析为JTS Geometry对象
                Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                if (!layerGeometry_zhwgk.isEmpty()) {

                    String code = feature_zhwgk.path("properties").path("转换为国空").asText();
                    //判断宅基地选址是否满足距离要求，与国道(1202)保持20m以上范围，与县道(1207)保持10m以上范围
                    if(code.equals("1202")){
                        Geometry buffer20 = layerGeometry_zhwgk.buffer(20); //国道创建20m缓冲区
                        if(polygon.intersects(buffer20)){

                            DataDetail dataDetail_zjd = new DataDetail();
                            dataDetail_zjd.setProjectName("农村宅基地");
                            dataDetail_zjd.setTag(4);

                            // 生成包含重合区域和原先绘制区域的图片
                            BufferedImage image = createImageWithPolygons1(polygon, layerGeometry_zhwgk);

                            // 将图像转换为 Base64 编码的字符串
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "png", baos);
                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                            //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                            //保存生成的重叠面积图片到DataDetail实体类
                            dataDetail_zjd.setImg(base64Image);

                            details.add(dataDetail_zjd);

                            System.out.println("农村宅基地的规划选址应与国道保持20米以上安全距离");
                        }

                    }else if(code.equals("1207")){
                        Geometry buffer10 = layerGeometry_zhwgk.buffer(10); //县道创建10m缓冲区
                        if(polygon.intersects(buffer10)){

                            DataDetail dataDetail_zjd = new DataDetail();
                            dataDetail_zjd.setProjectName("农村宅基地");
                            dataDetail_zjd.setTag(5);

                            // 生成包含重合区域和原先绘制区域的图片
                            BufferedImage image = createImageWithPolygons1(polygon, layerGeometry_zhwgk);

                            // 将图像转换为 Base64 编码的字符串
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "png", baos);
                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                            //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                            //保存生成的重叠面积图片到DataDetail实体类
                            dataDetail_zjd.setImg(base64Image);

                            details.add(dataDetail_zjd);

                            System.out.println("农村宅基地的规划选址应与县道保持10米以上安全距离");
                        }
                    }

                    IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                    IsValidOp isValidOp2 = new IsValidOp(layerGeometry_zhwgk);
                    if(isValidOp1.isValid() && isValidOp2.isValid()){
                        Geometry intersection_zhwgk = map.get("new_polygon").intersection(layerGeometry_zhwgk);

                        if (!intersection_zhwgk.isEmpty()) {

                            double intersectionArea_zhwgk = intersection_zhwgk.getArea();
                            code = feature_zhwgk.path("properties").path("转换为国空").asText();

                            //如果当前遍历的区域其转化为国空字段在classMap中不存在，那么视为新用地用海分类
                            if(!classMap.containsKey(code)){

                                if(isNotPassForRuralLands(code)){
                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();
                                    //Coordinate[] intersectionCoordinates_zhwgk = intersection_zhwgk.getCoordinates();
                                    //Coordinate[] coordinates_zhwgk = transformCoordinates2(intersectionCoordinates_zhwgk, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                                    //dataDetail_zhwgk.setIntersectionCoordinates(coordinates_zhwgk);
                                    //保存重叠面积信息到DataDetail实体类
                                    areas_zhwgk.setAreaId(featureId_zhwgk);
                                    areas_zhwgk.setArea(String.format("%.2f", intersectionArea_zhwgk));
                                    dataDetail_zhwgk.setAreas(areas_zhwgk);

                                    // 生成包含重合区域和原先绘制区域的图片
                                    BufferedImage image = createImageWithPolygons1(polygon, intersection_zhwgk);

                                    // 将图像转换为 Base64 编码的字符串
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ImageIO.write(image, "png", baos);
                                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                                    //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                                    //保存生成的重叠面积图片到DataDetail实体类
                                    dataDetail_zhwgk.setImg(base64Image);
                                    dataDetail_zhwgk.setTag(0);

                                    details.add(dataDetail_zhwgk);

                                    IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                    if(isValidOp_zhwgk.isValid()){
                                        zhwgk_red.add(intersection_zhwgk);
                                        classMap.put(code,intersection_zhwgk);
                                        System.out.println("绘制区域与：【"+dataDetail_zhwgk.getProjectName()+":"+dataDetail_zhwgk.getAreas().getAreaId()+"】存在"+dataDetail_zhwgk.getAreas().getArea()+"平方米的重合面积,不予通过");
                                    }

                                }else if(isWaitPassForRuralLands(code)){

                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();
                                    //Coordinate[] intersectionCoordinates_zhwgk = intersection_zhwgk.getCoordinates();
                                    //Coordinate[] coordinates_zhwgk = transformCoordinates2(intersectionCoordinates_zhwgk, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                                    //dataDetail_zhwgk.setIntersectionCoordinates(coordinates_zhwgk);
                                    //保存重叠面积信息到DataDetail实体类
                                    areas_zhwgk.setAreaId(featureId_zhwgk);
                                    areas_zhwgk.setArea(String.format("%.2f", intersectionArea_zhwgk));
                                    dataDetail_zhwgk.setAreas(areas_zhwgk);

                                    // 生成包含重合区域和原先绘制区域的图片
                                    BufferedImage image = createImageWithPolygons2(polygon, intersection_zhwgk);

                                    // 将图像转换为 Base64 编码的字符串
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ImageIO.write(image, "png", baos);
                                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                                    //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                                    //保存生成的重叠面积图片到DataDetail实体类
                                    dataDetail_zhwgk.setImg(base64Image);
                                    dataDetail_zhwgk.setTag(1);

                                    details.add(dataDetail_zhwgk);

                                    IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                    if(isValidOp_zhwgk.isValid()){
                                        zhwgk_yellow.add(intersection_zhwgk);

                                        classMap.put(code,intersection_zhwgk);

                                        System.out.println("绘制区域与：【"+dataDetail_zhwgk.getProjectName()+":"+dataDetail_zhwgk.getAreas().getAreaId()+"】存在"+dataDetail_zhwgk.getAreas().getArea()+"平方米的重合面积,办理手续后通过");
                                    }

                                }
                            }else{
                                //当前遍历的区域其转化为国空字段在classMap中存在，重复的用地用海分类，进行更新details中的数据
                                if(isNotPassForRuralLands(code)){

                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();

                                    //在details中找到对应的分类数据,更新detail数据
                                    for (DataDetail detail : details) {
                                        if(detail.getAreas().getAreaId().equals(featureId_zhwgk)){
                                            detail.getAreas().setArea(String.format("%.2f", Double.parseDouble(detail.getAreas().getArea()) + intersectionArea_zhwgk));

                                            // 生成包含重合区域和原先绘制区域的图片

//                                                        Geometry geometry = classMap.get(code);
//                                                        if (!geometry.isValid()) {
//                                                            geometry = GeometryFixer.fix(geometry);
//                                                        }
//
//                                                        if (!intersection_zhwgk.isValid()) {
//                                                            intersection_zhwgk = GeometryFixer.fix(intersection_zhwgk);
//                                                        }
//
//                                                        // 添加一个非常小的缓冲区以修复几何对象
//                                                        geometry = geometry.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                                        intersection_zhwgk = intersection_zhwgk.buffer(0.0).buffer(-0.0001).buffer(0.0);


                                            BufferedImage image = createImageWithPolygons1(polygon, intersection_zhwgk.union(classMap.get(code)));

                                            // 将图像转换为 Base64 编码的字符串
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            ImageIO.write(image, "png", baos);
                                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                                            detail.setImg(base64Image);

                                            IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                            if(isValidOp_zhwgk.isValid()){
                                                zhwgk_red.add(intersection_zhwgk);

                                                classMap.put(code,intersection_zhwgk.union(classMap.get(code)));

                                                System.out.println("绘制区域与：更新后的【"+detail.getProjectName()+":"+detail.getAreas().getAreaId()+"】存在"+detail.getAreas().getArea()+"平方米的重合面积,不予通过");

                                            }
                                        }
                                    }


                                }else if(isWaitPassForRuralLands(code)){

                                    String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();

                                    //在details中找到对应的分类数据,更新detail数据
                                    for (DataDetail detail : details) {
                                        if(detail.getAreas().getAreaId().equals(featureId_zhwgk)){
                                            detail.getAreas().setArea(String.format("%.2f", Double.parseDouble(detail.getAreas().getArea()) + intersectionArea_zhwgk));

                                            // 生成包含重合区域和原先绘制区域的图片

//                                                        Geometry geometry = classMap.get(code);
//                                                        if (!geometry.isValid()) {
//                                                            geometry = GeometryFixer.fix(geometry);
//                                                        }
//
//                                                        if (!intersection_zhwgk.isValid()) {
//                                                            intersection_zhwgk = GeometryFixer.fix(intersection_zhwgk);
//                                                        }
//
//                                                        // 添加一个非常小的缓冲区以修复几何对象
//                                                        geometry = geometry.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                                        intersection_zhwgk = intersection_zhwgk.buffer(0.0).buffer(-0.0001).buffer(0.0);


                                            BufferedImage image = createImageWithPolygons2(polygon, intersection_zhwgk.union(classMap.get(code)));

                                            // 将图像转换为 Base64 编码的字符串
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            ImageIO.write(image, "png", baos);
                                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                                            detail.setImg(base64Image);

                                            IsValidOp isValidOp_zhwgk = new IsValidOp(intersection_zhwgk);
                                            if(isValidOp_zhwgk.isValid()){
                                                zhwgk_yellow.add(intersection_zhwgk);

                                                classMap.put(code,intersection_zhwgk.union(classMap.get(code)));

                                                System.out.println("绘制区域与：更新后的【"+detail.getProjectName()+":"+detail.getAreas().getAreaId()+"】存在"+detail.getAreas().getArea()+"平方米的重合面积,不予通过");

                                            }
                                        }
                                    }

                                }
                            }

                        } else {
                            //System.out.println("No intersection with feature ID: " + feature_zhwgk.path("id").asText());
                        }
                    }

                } else {
                    System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                }
            }

            if (!zhwgk_red.isEmpty()) {

                Geometry zhwgkRedAll = UnaryUnionOp.union(zhwgk_red);

                // 在进行相减运算的时候，先检查并修复几何对象的有效性
                Geometry new_polygon = map.get("new_polygon");
//                                if (!new_polygon.isValid()) {
//                                    new_polygon = GeometryFixer.fix(new_polygon);
//                                }
//
//                                if (!zhwgkRedAll.isValid()) {
//                                    zhwgkRedAll = GeometryFixer.fix(zhwgkRedAll);
//                                }
//
//                                // 添加一个非常小的缓冲区以修复几何对象
//                                new_polygon = new_polygon.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                zhwgkRedAll = zhwgkRedAll.buffer(0.0).buffer(-0.0001).buffer(0.0);

                // 计算相减结果
                IsValidOp isValidOp1 = new IsValidOp(new_polygon);
                IsValidOp isValidOp2 = new IsValidOp(zhwgkRedAll);
                if(isValidOp1.isValid() && isValidOp2.isValid()){
                    new_polygon = new_polygon.difference(zhwgkRedAll);

                    map.put("new_polygon",new_polygon);

                    //传入红色区域
                    red.add(zhwgkRedAll);
                }

            }

            if (!zhwgk_yellow.isEmpty()) {

                Geometry zhwgkYellowAll = UnaryUnionOp.union(zhwgk_yellow);

                // 在进行相减运算的时候，先检查并修复几何对象的有效性
                Geometry new_polygon = map.get("new_polygon");
//                                if (!new_polygon.isValid()) {
//                                    new_polygon = GeometryFixer.fix(new_polygon);
//                                }
//
//                                if (!zhwgkYellowAll.isValid()) {
//                                    zhwgkYellowAll = GeometryFixer.fix(zhwgkYellowAll);
//                                }
//
//                                // 添加一个非常小的缓冲区以修复几何对象
//                                new_polygon = new_polygon.buffer(0.0).buffer(-0.0001).buffer(0.0);
//                                zhwgkYellowAll = zhwgkYellowAll.buffer(0.0).buffer(-0.0001).buffer(0.0);

                //计算相减结果
                IsValidOp isValidOp1 = new IsValidOp(new_polygon);
                IsValidOp isValidOp2 = new IsValidOp(zhwgkYellowAll);
                if(isValidOp1.isValid() && isValidOp2.isValid()){

                    // 使用 GeometryPrecisionReducer 减少精度
                    PrecisionModel precisionModel = new PrecisionModel(1000);
                    GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(precisionModel);
                    Geometry reducedPrecisionRedUnion = precisionReducer.reduce(new_polygon);

                    new_polygon = reducedPrecisionRedUnion.difference(zhwgkYellowAll);
                    map.put("new_polygon",new_polygon);

                    //传入黄色区域
                    yellow.add(zhwgkYellowAll);
                }

            }

            //TODO:比对逻辑结束
            //更新map集合中的值，换成原始传入图形数据
            map.put("new_polygon",polygon);
            //System.out.println(polygon);

            if(!red.isEmpty()){
                // 转换几何对象
                Geometry redUnion = UnaryUnionOp.union(red);
                createImageWithPolygons1(polygon, redUnion);
                IsValidOp isValidOp_redUnion = new IsValidOp(redUnion);
                if(isValidOp_redUnion.isValid()){

                    // 使用 GeometryPrecisionReducer 减少精度
                    PrecisionModel precisionModel = new PrecisionModel(1000);
                    GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(precisionModel);
                    Geometry reducedPrecisionRedUnion = precisionReducer.reduce(map.get("new_polygon"));

                    Geometry new_polygon = reducedPrecisionRedUnion.difference(redUnion);
                    map.put("new_polygon",new_polygon);

                    // 定义源和目标坐标系
                    CRSFactory crsFactory = new CRSFactory();
                    CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                    CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                    // 创建转换器
                    CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                    CoordinateTransform transform = ctFactory.createTransform(sourceCRS1, targetCRS1);
                    Geometry transformedGeometry = transformGeometry(redUnion, transform);

                    // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                    GeometryJSON gjson = new GeometryJSON(14);
                    StringWriter writer = new StringWriter();
                    gjson.write(transformedGeometry, writer);

                    // 设置结果
                    overLapArea.setRedGeoJson(writer.toString());
                }

            }
            if(!yellow.isEmpty()){
                // 转换几何对象
                Geometry yellowUnion = UnaryUnionOp.union(yellow);
                createImageWithPolygons2(polygon, yellowUnion);
                //System.out.println(yellowUnion);
                // 检查并简化几何图形（如果需要）
                Geometry new_polygon = map.get("new_polygon");
                //new_polygon = DouglasPeuckerSimplifier.simplify(new_polygon, 0.001);
                //yellowUnion = DouglasPeuckerSimplifier.simplify(yellowUnion, 0.001);
                IsValidOp isValidOp1 = new IsValidOp(new_polygon);
                IsValidOp isValidOp2 = new IsValidOp(yellowUnion);
                if(isValidOp1.isValid() && isValidOp2.isValid()){

                    // 使用 GeometryPrecisionReducer 减少精度
                    PrecisionModel precisionModel = new PrecisionModel(1000);
                    GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(precisionModel);
                    Geometry reducedPrecisionRedUnion = precisionReducer.reduce(new_polygon);

                    new_polygon = reducedPrecisionRedUnion.difference(yellowUnion);
                    map.put("new_polygon",new_polygon);

                    // 定义源和目标坐标系
                    CRSFactory crsFactory = new CRSFactory();
                    CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                    CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                    // 创建转换器
                    CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                    CoordinateTransform transform = ctFactory.createTransform(sourceCRS1, targetCRS1);

                    Geometry transformedGeometry = transformGeometry(yellowUnion, transform);
                    // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                    GeometryJSON gjson = new GeometryJSON(14);
                    StringWriter writer = new StringWriter();
                    gjson.write(transformedGeometry, writer);

                    // 设置结果
                    overLapArea.setYellowGeoJson(writer.toString());
                }

            }

            //设置绿色区域数据
            // 转换几何对象
            Geometry greenUnion = map.get("new_polygon");
            createImageWithPolygons3(polygon, greenUnion);
            //System.out.println(greenUnion);
            //Geometry greenUnion = polygon.difference((UnaryUnionOp.union(yellow)));
            IsValidOp isValidOp = new IsValidOp(greenUnion);
            if(isValidOp.isValid()){

                // 定义源和目标坐标系
                CRSFactory crsFactory = new CRSFactory();
                CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                // 创建转换器
                CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                CoordinateTransform transform = ctFactory.createTransform(sourceCRS1, targetCRS1);

                Geometry transformedGeometry = transformGeometry(greenUnion, transform);
                // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                GeometryJSON gjson = new GeometryJSON(14);
                StringWriter writer = new StringWriter();
                gjson.write(transformedGeometry, writer);

                // 设置结果
                overLapArea.setGreenGeoJson(writer.toString());

                resultData.setOverLapArea(overLapArea);
                resultData.setDataDetails(details);

                return resultData;
            }

        }

        return resultData;
    }

    @Override
    public ResultPoint comparePointForWC(Map<String, PointInfo> requestBody) throws IOException {
        PointInfo pointInfo = requestBody.get("pointInfo");
        // 将前端传入的经纬度坐标转换为JTS Polygon对象
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(pointInfo.getX(), pointInfo.getY());

        // 打印转换前的坐标
        System.out.println("Original coordinates:");
        System.out.println(coordinate);
        System.out.println("--------------------------------------------------------------------------");

        // 将坐标转换为CGCS2000_3_Degree_GK_Zone_37坐标系
        String sourceCRS = "EPSG:4326"; // WGS84坐标系
        String targetCRS = "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"; // CGCS2000_3_Degree_GK_Zone_37坐标系

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem source = crsFactory.createFromName(sourceCRS);
        CoordinateReferenceSystem target = crsFactory.createFromParameters(null, targetCRS);
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(source, target);

        ProjCoordinate sourceCoord = new ProjCoordinate(pointInfo.getX(), pointInfo.getY());
        ProjCoordinate targetCoord = new ProjCoordinate();
        transform.transform(sourceCoord, targetCoord);
        Coordinate coord = new Coordinate(targetCoord.x, targetCoord.y);

        // 打印经纬度转换成CGCS2000_3_Degree_GK_Zone_37后的坐标
        System.out.println("Transformed coordinates:");
        System.out.println(coord);

        System.out.println("--------------------------------------------------------------------------");

        //创建一个以传入的坐标点为圆心，指定半径的圆
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setCentre(new Coordinate(coord.getX(), coord.getY()));
        shapeFactory.setSize(2 * 10); // 设置圆的半径为10m
        //创建以10m为半径的圆
        Geometry circle_10 = shapeFactory.createCircle();

        shapeFactory.setCentre(new Coordinate(coord.getX(), coord.getY()));
        shapeFactory.setSize(2 * 30); // 设置圆的半径为30m
        //创建以30m为半径的圆
        Geometry circle_30 = shapeFactory.createCircle();

        shapeFactory.setCentre(new Coordinate(coord.getX(), coord.getY()));
        shapeFactory.setSize(2 * 1000); // 设置圆的半径为1000m
        //创建以1000m为半径的圆
        Geometry circle_1000 = shapeFactory.createCircle();

        //TODO:与转化为国空字段图层对比,要跟080404保持10m以上距离，要跟1301保持30m以上距离,
        //构建GeoServer查询转化为国空URL
        String url_zhwgk = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_zhwgk");

        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_zhwkg = restTemplate.getForEntity(url_zhwgk, String.class);

        System.out.println("GeoServer response_zhwkg status: " + response_zhwkg.getStatusCode());

        List<DataDetail> details = new ArrayList<>();
        List<Geometry> list_geometry = new ArrayList<>();
        List<Geometry> list_notMeetDistance1 = new ArrayList<>();
        List<Geometry> list_notMeetDistance2 = new ArrayList<>();
        ResultPoint resultPoint = new ResultPoint();

        if (response_zhwkg.getStatusCode().is2xxSuccessful()) {
            String response_zhwkgBody = response_zhwkg.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_zhwgk = objectMapper.readTree(response_zhwkgBody);
            JsonNode features_zhwgk = rootNode_zhwgk.path("features");

            for (JsonNode feature_zhwgk : features_zhwgk) {
                //获取到转化为国空的代码
                String code = feature_zhwgk.path("properties").path("转换为国空").asText();
                //保存大圆circle_1000与0703的交集面积
                if(code.equals("0703")){
                    JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");
                    // 检查几何数据是否存在
                    if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                        System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                        continue;
                    }
                    // 将GeoJSON几何数据解析为JTS Geometry对象
                    Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                    if (!layerGeometry_zhwgk.isEmpty()) {

                        //计算所画区域与0703代码图层的交集部分
                        Geometry intersection_zhwgk = circle_1000.intersection(layerGeometry_zhwgk);
                        //将交集部分存到存储0703部分交集的list集合中
                        if(!intersection_zhwgk.isEmpty()){
                            list_geometry.add(intersection_zhwgk);
                        }

                    } else {
                        System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                    }

                }else if(code.equals("080404")){

                    DataDetail dataDetail_zhwgk = new DataDetail();
                    Areas areas_zhwgk = new Areas();
                    JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");

                    //保存项目名信息到DataDetail_zhwgk实体类
                    dataDetail_zhwgk.setProjectName("公共厕所");

                    // 检查几何数据是否存在
                    if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                        System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                        continue;
                    }

                    // 将GeoJSON几何数据解析为JTS Geometry对象
                    Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                    if (!layerGeometry_zhwgk.isEmpty()) {

                        String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();
                        //保存距离小于10m的交集部分到DataDetail实体类
                        areas_zhwgk.setAreaId(featureId_zhwgk);
                        dataDetail_zhwgk.setAreas(areas_zhwgk);

                        //计算所画区域与080404代码图层的交集部分
                        Geometry intersection_zhwgk = circle_10.intersection(layerGeometry_zhwgk);
                        if(!intersection_zhwgk.isEmpty()){
                            //存放公共卫生厕所不满足距离要求的交集区域
                            list_notMeetDistance1.add(intersection_zhwgk);
                        }

                    } else {
                        System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                    }

                }else if(code.equals("1301")){

                    DataDetail dataDetail_zhwgk = new DataDetail();
                    Areas areas_zhwgk = new Areas();
                    JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");

                    //保存项目名信息到DataDetail_zhwgk实体类
                    dataDetail_zhwgk.setProjectName("公共厕所");

                    // 检查几何数据是否存在
                    if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                        System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                        continue;
                    }

                    // 将GeoJSON几何数据解析为JTS Geometry对象
                    Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                    if (!layerGeometry_zhwgk.isEmpty()) {

                        String featureId_zhwgk = feature_zhwgk.path("properties").path("DLMC").asText();
                        //保存距离小于30m的交集部分到DataDetail实体类
                        areas_zhwgk.setAreaId(featureId_zhwgk);
                        dataDetail_zhwgk.setAreas(areas_zhwgk);

                        //计算所画区域与1301代码图层的交集部分
                        Geometry intersection_zhwgk = circle_30.intersection(layerGeometry_zhwgk);
                        if(!intersection_zhwgk.isEmpty()){
                            //存放公共卫生厕所不满足距离要求的交集区域
                            list_notMeetDistance2.add(intersection_zhwgk);
                        }

                    } else {
                        System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                    }
                }

            }
            //如果设立的公共厕所不满足距离要求，记录结果给比对报告
            if(!list_notMeetDistance1.isEmpty()){
                DataDetail dataDetail_zhwgk = new DataDetail();
                dataDetail_zhwgk.setProjectName("公共卫生厕所");
                dataDetail_zhwgk.setTag(2);

                // 生成包含重合区域和原先绘制区域的图片
                Geometry intersection_union = UnaryUnionOp.union(list_notMeetDistance1);
                BufferedImage image = createImageWithPolygons1(circle_10, intersection_union);

                // 将图像转换为 Base64 编码的字符串
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                //保存生成的重叠面积图片到DataDetail实体类
                dataDetail_zhwgk.setImg(base64Image);

                details.add(dataDetail_zhwgk);

                System.out.println("公共卫生厕所的规划选址应与托幼机构保持10米以上安全距离");
            }
            if(!list_notMeetDistance2.isEmpty()){
                DataDetail dataDetail_zhwgk = new DataDetail();
                dataDetail_zhwgk.setProjectName("公共卫生厕所");
                dataDetail_zhwgk.setTag(3);

                // 生成包含重合区域和原先绘制区域的图片
                Geometry intersection_union = UnaryUnionOp.union(list_notMeetDistance2);
                BufferedImage image = createImageWithPolygons1(circle_30, intersection_union);

                // 将图像转换为 Base64 编码的字符串
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                //保存生成的重叠面积图片到DataDetail实体类
                dataDetail_zhwgk.setImg(base64Image);

                details.add(dataDetail_zhwgk);

                System.out.println("公共卫生厕所的规划选址应与集中式给水点和地下取水构筑物保持30米以上的安全距离");
            }
            //计算拿到与0703交集部分的坐标信息
            if(!list_geometry.isEmpty()){
                Geometry intersection_union = UnaryUnionOp.union(list_geometry);
                if (!intersection_union.isEmpty()) {
                    BufferedImage image = createImageWithPolygons1(circle_1000, intersection_union);

                    // 定义源和目标坐标系
                    crsFactory = new CRSFactory();
                    CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                    CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                    // 创建转换器
                    transform = new BasicCoordinateTransform(sourceCRS1, targetCRS1);

                    // 转换几何对象
                    Geometry transformedGeometry = transformGeometry(intersection_union, transform);

                    // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                    GeometryJSON gjson = new GeometryJSON();
                    StringWriter writer = new StringWriter();
                    gjson.write(transformedGeometry, writer);

                    // 设置结果
                    resultPoint.setGeojson(writer.toString());
                }
            }
            resultPoint.setDetails(details);
        }
        return resultPoint;
    }

    @Override
    public List<Coordinate[]> getValidShowLayerForWC() throws JsonProcessingException {
        // 构建GeoServer查询转化为国空URL
        String url_zhwgk = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_zhwgk");
        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_zhwgk = restTemplate.getForEntity(url_zhwgk, String.class);
        System.out.println("GeoServer response_zhwgk status: " + response_zhwgk.getStatusCode());

        if(response_zhwgk.getStatusCode().is2xxSuccessful()){
            String responseBody_zhwgk = response_zhwgk.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_zhwgk = objectMapper.readTree(responseBody_zhwgk);
            JsonNode features_zhwgk = rootNode_zhwgk.path("features");

            //TODO:创建一个存储可选区域坐标的列表
            List<Coordinate[]> validcoords = new ArrayList<>();

            for (JsonNode feature_zhwgk : features_zhwgk) {

                JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");

                // 检查几何数据是否存在
                if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                    System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                    continue;
                }

                // 将GeoJSON几何数据解析为JTS Geometry对象
                Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                if (!layerGeometry_zhwgk.isEmpty()) {

                    String code = feature_zhwgk.path("properties").path("转换为国空").asText();
                    if(isValidCode(code)){
                        //System.out.println(layerGeometry_zhwgk);
                        Coordinate[] coordinates = layerGeometry_zhwgk.getCoordinates();
                        validcoords.add(coordinates);
                    }
                } else {
                    System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                }

            }
            //System.out.println(validGeometry);
            return validcoords;
        }
        return null;
    }

    @Override
    public List<Coordinate[]> getValidShowLayerForBHHX() throws JsonProcessingException {
        // 构建GeoServer查询转化为国空URL
        String url_bhhx = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_bhhx");
        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_bhhx = restTemplate.getForEntity(url_bhhx, String.class);
        System.out.println("GeoServer response_bhhx status: " + response_bhhx.getStatusCode());

        if(response_bhhx.getStatusCode().is2xxSuccessful()){
            String responseBody_bhhx = response_bhhx.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_bhhx = objectMapper.readTree(responseBody_bhhx);
            JsonNode features_bhhx = rootNode_bhhx.path("features");

            //TODO:创建一个存储可选区域坐标的列表
            List<Coordinate[]> validcoords = new ArrayList<>();

            for (JsonNode feature_bhhx : features_bhhx) {

                JsonNode geometryNode_bhhx = feature_bhhx.path("geometry");

                // 检查几何数据是否存在
                if (geometryNode_bhhx.isMissingNode() || geometryNode_bhhx.isNull()) {
                    System.err.println("Geometry node is missing or null for feature ID: " + feature_bhhx.path("id").asText());
                    continue;
                }

                // 将GeoJSON几何数据解析为JTS Geometry对象
                Geometry layerGeometry_bhhx = parseGeoJsonToGeometry(geometryNode_bhhx);
                if (!layerGeometry_bhhx.isEmpty()) {

                    Coordinate[] coordinates = layerGeometry_bhhx.getCoordinates();
                    validcoords.add(coordinates);

                } else {
                    System.err.println("Layer geometry is null for feature ID: " + feature_bhhx.path("id").asText());
                }

            }
            //System.out.println(validGeometry);
            return validcoords;
        }

        return null;
    }

    @Override
    public List<Coordinate[]> getValidShowLayerForYJJBNT() throws JsonProcessingException {
        // 构建GeoServer查询转化为国空URL
        String url_yjjbnt = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_yjjbnt");
        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_yjjbnt = restTemplate.getForEntity(url_yjjbnt, String.class);
        System.out.println("GeoServer response_yjjbnt status: " + response_yjjbnt.getStatusCode());

        if(response_yjjbnt.getStatusCode().is2xxSuccessful()){
            String responseBody_yjjbnt = response_yjjbnt.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_yjjbnt = objectMapper.readTree(responseBody_yjjbnt);
            JsonNode features_yjjbnt = rootNode_yjjbnt.path("features");

            //TODO:创建一个存储可选区域坐标的列表
            List<Coordinate[]> validcoords = new ArrayList<>();

            for (JsonNode feature_yjjbnt : features_yjjbnt) {

                JsonNode geometryNode_yjjbnt = feature_yjjbnt.path("geometry");

                // 检查几何数据是否存在
                if (geometryNode_yjjbnt.isMissingNode() || geometryNode_yjjbnt.isNull()) {
                    System.err.println("Geometry node is missing or null for feature ID: " + feature_yjjbnt.path("id").asText());
                    continue;
                }

                // 将GeoJSON几何数据解析为JTS Geometry对象
                Geometry layerGeometry_yjjbnt = parseGeoJsonToGeometry(geometryNode_yjjbnt);
                if (!layerGeometry_yjjbnt.isEmpty()) {

                    Coordinate[] coordinates = layerGeometry_yjjbnt.getCoordinates();
                    validcoords.add(coordinates);

                } else {
                    System.err.println("Layer geometry is null for feature ID: " + feature_yjjbnt.path("id").asText());
                }

            }
            //System.out.println(validGeometry);
            return validcoords;
        }

        return null;
    }

    @Override
    public List<Coordinate[]> getValidShowLayerForGBNT() throws JsonProcessingException {
        // 构建GeoServer查询转化为国空URL
        String url_gbnt = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_22gb");
        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_gbnt = restTemplate.getForEntity(url_gbnt, String.class);
        System.out.println("GeoServer response_gbnt status: " + response_gbnt.getStatusCode());

        if(response_gbnt.getStatusCode().is2xxSuccessful()){
            String responseBody_gbnt = response_gbnt.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_gbnt = objectMapper.readTree(responseBody_gbnt);
            JsonNode features_gbnt = rootNode_gbnt.path("features");

            //TODO:创建一个存储可选区域坐标的列表
            List<Coordinate[]> validcoords = new ArrayList<>();

            for (JsonNode feature_gbnt : features_gbnt) {

                JsonNode geometryNode_gbnt = feature_gbnt.path("geometry");

                // 检查几何数据是否存在
                if (geometryNode_gbnt.isMissingNode() || geometryNode_gbnt.isNull()) {
                    System.err.println("Geometry node is missing or null for feature ID: " + feature_gbnt.path("id").asText());
                    continue;
                }

                // 将GeoJSON几何数据解析为JTS Geometry对象
                Geometry layerGeometry_gbnt = parseGeoJsonToGeometry(geometryNode_gbnt);
                if (!layerGeometry_gbnt.isEmpty()) {

                    Coordinate[] coordinates = layerGeometry_gbnt.getCoordinates();
                    validcoords.add(coordinates);

                } else {
                    System.err.println("Layer geometry is null for feature ID: " + feature_gbnt.path("id").asText());
                }

            }
            //System.out.println(validGeometry);
            return validcoords;
        }

        return null;
    }

    @Override
    public List<Coordinate[]> getValidShowLayerForGarbage() throws JsonProcessingException{
        // 构建GeoServer查询转化为国空URL
        String url_zhwgk = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_zhwgk");
        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_zhwgk = restTemplate.getForEntity(url_zhwgk, String.class);
        System.out.println("GeoServer response_zhwgk status: " + response_zhwgk.getStatusCode());

        if(response_zhwgk.getStatusCode().is2xxSuccessful()){
            String responseBody_zhwgk = response_zhwgk.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_zhwgk = objectMapper.readTree(responseBody_zhwgk);
            JsonNode features_zhwgk = rootNode_zhwgk.path("features");

            //TODO:创建一个存储可选区域坐标的列表
            List<Coordinate[]> validcoords = new ArrayList<>();

            for (JsonNode feature_zhwgk : features_zhwgk) {

                JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");

                // 检查几何数据是否存在
                if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                    System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                    continue;
                }

                // 将GeoJSON几何数据解析为JTS Geometry对象
                Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                if (!layerGeometry_zhwgk.isEmpty()) {

                    String code = feature_zhwgk.path("properties").path("转换为国空").asText();
                    if(isGarbageCode(code)){
                        //System.out.println(layerGeometry_zhwgk);
                        Coordinate[] coordinates = layerGeometry_zhwgk.getCoordinates();
                        validcoords.add(coordinates);
                    }
                } else {
                    System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                }

            }
            //System.out.println(validGeometry);
            return validcoords;
        }
        return null;
    }

    @Override
    public ResultPoint comparePointForGarbage(Map<String, PointInfo> requestBody) throws IOException {
        PointInfo pointInfo = requestBody.get("pointInfo");
        // 将前端传入的经纬度坐标转换为JTS Polygon对象
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(pointInfo.getX(), pointInfo.getY());

        // 打印转换前的坐标
        System.out.println("Original coordinates:");
        System.out.println(coordinate);
        System.out.println("--------------------------------------------------------------------------");

        // 将坐标转换为CGCS2000_3_Degree_GK_Zone_37坐标系
        String sourceCRS = "EPSG:4326"; // WGS84坐标系
        String targetCRS = "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"; // CGCS2000_3_Degree_GK_Zone_37坐标系

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem source = crsFactory.createFromName(sourceCRS);
        CoordinateReferenceSystem target = crsFactory.createFromParameters(null, targetCRS);
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(source, target);

        ProjCoordinate sourceCoord = new ProjCoordinate(pointInfo.getX(), pointInfo.getY());
        ProjCoordinate targetCoord = new ProjCoordinate();
        transform.transform(sourceCoord, targetCoord);
        Coordinate coord = new Coordinate(targetCoord.x, targetCoord.y);

        // 打印经纬度转换成CGCS2000_3_Degree_GK_Zone_37后的坐标
        System.out.println("Transformed coordinates:");
        System.out.println(coord);

        System.out.println("--------------------------------------------------------------------------");

        //创建一个以传入的坐标点为圆心，指定半径的圆
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setCentre(new Coordinate(coord.getX(), coord.getY()));
        shapeFactory.setSize(2 * 200); // 设置圆的半径为200m
        //创建以200m为半径的圆
        Geometry circle_200 = shapeFactory.createCircle();

        //TODO:与转化为国空字段图层对比
        //构建GeoServer查询转化为国空URL
        String url_zhwgk = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_zhwgk");

        // 发起请求获取转化为国空图层数据
        ResponseEntity<String> response_zhwkg = restTemplate.getForEntity(url_zhwgk, String.class);

        System.out.println("GeoServer response_zhwkg status: " + response_zhwkg.getStatusCode());

        List<DataDetail> details = new ArrayList<>();
        List<Geometry> list_geometry = new ArrayList<>();
        ResultPoint resultPoint = new ResultPoint();

        if (response_zhwkg.getStatusCode().is2xxSuccessful()) {
            String response_zhwkgBody = response_zhwkg.getBody();
            System.out.println("--------------------------------------------------------------------------");

            // 解析GeoServer返回的JSON数据
            JsonNode rootNode_zhwgk = objectMapper.readTree(response_zhwkgBody);
            JsonNode features_zhwgk = rootNode_zhwgk.path("features");

            for (JsonNode feature_zhwgk : features_zhwgk) {
                //获取到转化为国空的代码
                String code = feature_zhwgk.path("properties").path("转换为国空").asText();
                //保存圆circle_200与0703的交集面积
                if(code.equals("0703")){
                    JsonNode geometryNode_zhwgk = feature_zhwgk.path("geometry");
                    // 检查几何数据是否存在
                    if (geometryNode_zhwgk.isMissingNode() || geometryNode_zhwgk.isNull()) {
                        System.err.println("Geometry node is missing or null for feature ID: " + feature_zhwgk.path("id").asText());
                        continue;
                    }
                    // 将GeoJSON几何数据解析为JTS Geometry对象
                    Geometry layerGeometry_zhwgk = parseGeoJsonToGeometry(geometryNode_zhwgk);
                    if (!layerGeometry_zhwgk.isEmpty()) {

                        //计算所画区域与0703代码图层的交集部分
                        Geometry intersection_zhwgk = circle_200.intersection(layerGeometry_zhwgk);
                        //将交集部分存到存储0703部分交集的list集合中
                        if(!intersection_zhwgk.isEmpty()){
                            list_geometry.add(intersection_zhwgk);
                        }

                    } else {
                        System.err.println("Layer geometry is null for feature ID: " + feature_zhwgk.path("id").asText());
                    }

                }

            }
            //计算拿到与0703交集部分的坐标信息
            if(!list_geometry.isEmpty()){
                Geometry intersection_union = UnaryUnionOp.union(list_geometry);
                if (!intersection_union.isEmpty()) {
                    BufferedImage image = createImageWithPolygons1(circle_200, intersection_union);

                    // 定义源和目标坐标系
                    crsFactory = new CRSFactory();
                    CoordinateReferenceSystem sourceCRS1 = crsFactory.createFromName("EPSG:4525");
                    CoordinateReferenceSystem targetCRS1 = crsFactory.createFromName("EPSG:4326");

                    // 创建转换器
                    transform = new BasicCoordinateTransform(sourceCRS1, targetCRS1);

                    // 转换几何对象
                    Geometry transformedGeometry = transformGeometry(intersection_union, transform);

                    // 使用 GeoTools 的 GeometryJSON 生成 GeoJSON
                    GeometryJSON gjson = new GeometryJSON();
                    StringWriter writer = new StringWriter();
                    gjson.write(transformedGeometry, writer);

                    // 设置结果
                    resultPoint.setGeojson(writer.toString());
                }
            }
            resultPoint.setDetails(details);
        }
        return resultPoint;
    }

    @Override
    public Double getPolygonArea(Map<String, Object> requestBody) {
        // 获取前端传来的几何类型
        String type = (String) requestBody.get("type");

        GeometryFactory geometryFactory = new GeometryFactory();
        double totalArea = 0.0;

        if (type.equals("Polygon")) {
            // 如果是 Polygon，获取的坐标类型为 List<List<Double>>
            List<List<Double>> polygonPoints = (List<List<Double>>) requestBody.get("polygon");

            // 将坐标转换为 JTS Polygon 对象
            Coordinate[] coordinates = new Coordinate[polygonPoints.size() + 1];

            for (int i = 0; i < polygonPoints.size(); i++) {
                List<Double> point = polygonPoints.get(i);
                if (point.size() < 2) {
                    System.err.println("Point size is less than 2 at index: " + i);
                    return null; // 坐标不完整，返回空值或抛出异常
                }
                coordinates[i] = new Coordinate(point.get(0), point.get(1));
            }

            // 闭合多边形
            coordinates[polygonPoints.size()] = coordinates[0];

            // 坐标转换
            String sourceCRS = "EPSG:4326"; // WGS84坐标系
            String targetCRS = "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"; // CGCS2000_3_Degree_GK_Zone_37坐标系

            Coordinate[] transformedCoordinates;
            try {
                transformedCoordinates = transformCoordinates1(coordinates, sourceCRS, targetCRS);
            } catch (Exception e) {
                System.err.println("Coordinate transformation failed: " + e.getMessage());
                return 0.0; // 如果坐标转换失败，返回0面积
            }

            // 创建多边形并计算面积
            Polygon polygon = geometryFactory.createPolygon(transformedCoordinates);
            totalArea = polygon.getArea();

        } else if (type.equals("MultiPolygon")) {
            // 如果是 MultiPolygon，获取的坐标类型为 List<List<List<Double>>>
            List<List<List<Double>>> polygonPoints = (List<List<List<Double>>>) requestBody.get("polygon");

            for (List<List<Double>> singlePolygon : polygonPoints) {
                // 将坐标转换为 JTS Polygon 对象
                Coordinate[] coordinates = new Coordinate[singlePolygon.size() + 1];

                for (int i = 0; i < singlePolygon.size(); i++) {
                    List<Double> point = singlePolygon.get(i);
                    if (point.size() < 2) {
                        System.err.println("Point size is less than 2 at index: " + i);
                        return null; // 坐标不完整，返回空值或抛出异常
                    }
                    coordinates[i] = new Coordinate(point.get(0), point.get(1));
                }

                // 闭合多边形
                coordinates[singlePolygon.size()] = coordinates[0];

                // 坐标转换
                String sourceCRS = "EPSG:4326"; // WGS84坐标系
                String targetCRS = "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"; // CGCS2000_3_Degree_GK_Zone_37坐标系

                Coordinate[] transformedCoordinates;
                try {
                    transformedCoordinates = transformCoordinates1(coordinates, sourceCRS, targetCRS);
                } catch (Exception e) {
                    System.err.println("Coordinate transformation failed: " + e.getMessage());
                    return 0.0; // 如果坐标转换失败，返回0面积
                }

                // 创建多边形并计算面积
                Polygon polygon = geometryFactory.createPolygon(transformedCoordinates);
                totalArea += polygon.getArea();
            }
        } else {
            System.err.println("Unsupported geometry type: " + type);
            return null; // 返回空值或者抛出异常
        }

        // 对总面积进行精度处理并返回
        BigDecimal bd = new BigDecimal(Double.toString(totalArea));
        BigDecimal rounded = bd.setScale(2, RoundingMode.HALF_UP);
        return rounded.doubleValue();
    }

    @Override
    public Double getGreenArea(Map<String, Object> requestBody) {
        // 获取前端传来的几何类型
        String type = (String) requestBody.get("type");

        GeometryFactory geometryFactory = new GeometryFactory();
        double totalArea = 0.0;

        // 坐标系定义
        String sourceCRS = "EPSG:4326"; // WGS84坐标系
        String targetCRS = "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"; // CGCS2000_3_Degree_GK_Zone_37坐标系

        // 获取 Polygon 或 MultiPolygon 数据
        List<?> polygonData = (List<?>) requestBody.get("polygon");

        if (polygonData != null && !polygonData.isEmpty()) {
            // 处理 Polygon 类型
            if (type.equals("Polygon")) {
                // 确保结构正确
                if (polygonData.get(0) instanceof List) {
                    List<List<List<Double>>> coordinatesList = (List<List<List<Double>>>) polygonData;

                    // 创建坐标数组，处理 Polygon 的每个点
                    Coordinate[] coordinates = new Coordinate[coordinatesList.get(0).size() + 1];
                    for (int i = 0; i < coordinatesList.get(0).size(); i++) {
                        List<Double> point = coordinatesList.get(0).get(i);
                        if (point.size() < 2) {
                            System.err.println("Point size is less than 2 at index: " + i);
                            return 0.0;
                        }
                        coordinates[i] = new Coordinate(point.get(0), point.get(1));
                    }

                    // 闭合多边形
                    coordinates[coordinatesList.get(0).size()] = coordinates[0];

                    // 坐标转换
                    Coordinate[] transformedCoordinates;
                    try {
                        transformedCoordinates = transformCoordinates1(coordinates, sourceCRS, targetCRS);
                    } catch (Exception e) {
                        System.err.println("Coordinate transformation failed: " + e.getMessage());
                        return 0.0;
                    }

                    // 创建多边形并计算面积
                    Polygon polygon = geometryFactory.createPolygon(transformedCoordinates);
                    totalArea = polygon.getArea();

                } else {
                    System.err.println("Invalid data structure for Polygon.");
                    return 0.0;
                }

            } else if (type.equals("MultiPolygon")) {
                // 处理 MultiPolygon 类型
                for (Object polygonObject : polygonData) {
                    if (polygonObject instanceof List) {
                        List<List<List<Double>>> coordinatesList = (List<List<List<Double>>>) polygonObject;

                        // 创建坐标数组，处理 MultiPolygon 中的每个 Polygon
                        Coordinate[] coordinates = new Coordinate[coordinatesList.get(0).size() + 1];
                        for (int i = 0; i < coordinatesList.get(0).size(); i++) {
                            List<Double> point = coordinatesList.get(0).get(i);
                            if (point.size() < 2) {
                                System.err.println("Point size is less than 2 at index: " + i);
                                return 0.0;
                            }
                            coordinates[i] = new Coordinate(point.get(0), point.get(1));
                        }

                        // 闭合多边形
                        coordinates[coordinatesList.get(0).size()] = coordinates[0];

                        // 坐标转换
                        Coordinate[] transformedCoordinates;
                        try {
                            transformedCoordinates = transformCoordinates1(coordinates, sourceCRS, targetCRS);
                        } catch (Exception e) {
                            System.err.println("Coordinate transformation failed: " + e.getMessage());
                            return 0.0;
                        }

                        // 创建多边形并计算面积
                        Polygon polygon = geometryFactory.createPolygon(transformedCoordinates);
                        totalArea += polygon.getArea();

                    } else {
                        System.err.println("Invalid data structure for MultiPolygon.");
                        return 0.0;
                    }
                }

            } else {
                System.err.println("Unsupported geometry type: " + type);
                return 0.0;
            }

        } else {
            System.err.println("Invalid data structure.");
            return 0.0;
        }

        // 对总面积进行精度处理并返回
        BigDecimal bd = new BigDecimal(Double.toString(totalArea));
        BigDecimal rounded = bd.setScale(2, RoundingMode.HALF_UP);
        return rounded.doubleValue();
    }


    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<以下为辅助方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private Geometry parseGeoJsonToGeometry(JsonNode geometryNode) {
        try {
            String type = geometryNode.get("type").asText();
            JsonNode coordinates = geometryNode.get("coordinates");

            GeometryFactory geometryFactory = new GeometryFactory();

            if ("Polygon".equals(type)) {
                return parsePolygon(coordinates, geometryFactory);
            } else if ("MultiPolygon".equals(type)) {
                return parseMultiPolygon(coordinates, geometryFactory);
            } else {
                throw new IllegalArgumentException("Unsupported geometry type: " + type);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse GeoJSON geometry: " + e.getMessage());
            return null;
        }
    }

    private Polygon parsePolygon(JsonNode coordinates, GeometryFactory geometryFactory) {
        Coordinate[] exteriorRing = parseCoordinates(coordinates.get(0));
        return geometryFactory.createPolygon(exteriorRing);
    }

    private Geometry parseMultiPolygon(JsonNode coordinates, GeometryFactory geometryFactory) {
        int numPolygons = coordinates.size();
        Polygon[] polygons = new Polygon[numPolygons];
        for (int i = 0; i < numPolygons; i++) {
            polygons[i] = parsePolygon(coordinates.get(i), geometryFactory);
        }
        return geometryFactory.createMultiPolygon(polygons);
    }

    //经纬度坐标转换CGCS2000_3_Degree_GK_Zone_37坐标转换
    private Coordinate[] transformCoordinates1(Coordinate[] coordinates, String sourceCRS, String targetCRS) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem source = crsFactory.createFromName(sourceCRS);
        CoordinateReferenceSystem target = crsFactory.createFromParameters(null, targetCRS);
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(source, target);

        Coordinate[] transformedCoordinates = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            ProjCoordinate sourceCoord = new ProjCoordinate(coordinates[i].x, coordinates[i].y);
            ProjCoordinate targetCoord = new ProjCoordinate();
            transform.transform(sourceCoord, targetCoord);
            transformedCoordinates[i] = new Coordinate(targetCoord.x, targetCoord.y);
        }
        return transformedCoordinates;
    }

    // 创建绘制红色重合区域后的图片
    private BufferedImage createImageWithPolygons1(Geometry polygon, Geometry intersection) {
        int width = 150;
        int height = 150;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置缩放比例和偏移
        Envelope envelope = polygon.getEnvelopeInternal();
        double scaleX = width / envelope.getWidth();
        double scaleY = height / envelope.getHeight();
        double scale = Math.min(scaleX, scaleY); // 保持比例
        double offsetX = -envelope.getMinX() * scale;
        double offsetY = -envelope.getMinY() * scale;

        // 绘制原始多边形
        g2d.setColor(new Color(0, 0, 255,50 )); // 设置蓝色填充并带有透明度
        fillPolygon(g2d, polygon, scale, offsetX, offsetY, height);

        // 检查和打印交集区域的坐标
        if (!intersection.isEmpty()) {
            //System.out.println("Intersection coordinates:");
            //for (Coordinate coord : intersection.getCoordinates()) {
            //    System.out.println(coord);
            //}

            // 绘制相交区域
            g2d.setColor(new Color(255, 0, 0)); // 设置红色填充
            fillPolygon(g2d, intersection, scale, offsetX, offsetY, height);
        } else {
            System.out.println("No intersection found.");
        }

        g2d.dispose();

        // 将图像保存为文件进行调试
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputfile = new File(outputDir, "red.png");
            ImageIO.write(image, "png", outputfile);
            System.out.println("Image saved to: " + outputfile.getAbsolutePath()); // 调试输出保存路径
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    private Coordinate[] parseCoordinates(JsonNode coordinatesArray) {
        int numPoints = coordinatesArray.size();
        Coordinate[] coordinates = new Coordinate[numPoints];
        for (int i = 0; i < numPoints; i++) {
            JsonNode point = coordinatesArray.get(i);
            coordinates[i] = new Coordinate(point.get(0).asDouble(), point.get(1).asDouble());
        }
        return coordinates;
    }

    //CGCS2000_3_Degree_GK_Zone_37坐标转换成经纬度坐标
    private Coordinate[] transformCoordinates2(Coordinate[] coordinates, String sourceCRS, String targetCRS) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem source = crsFactory.createFromParameters(null, sourceCRS);
        CoordinateReferenceSystem target = crsFactory.createFromName(targetCRS);
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(source, target);

        Coordinate[] transformedCoordinates = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            ProjCoordinate sourceCoord = new ProjCoordinate(coordinates[i].x, coordinates[i].y);
            ProjCoordinate targetCoord = new ProjCoordinate();
            transform.transform(sourceCoord, targetCoord);
            transformedCoordinates[i] = new Coordinate(targetCoord.x, targetCoord.y);
        }
        return transformedCoordinates;
    }

    // 创建绘制黄色重合区域后的图片
    private BufferedImage createImageWithPolygons2(Geometry polygon, Geometry intersection) {
        int width = 150;
        int height = 150;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置缩放比例和偏移
        Envelope envelope = polygon.getEnvelopeInternal();
        double scaleX = width / envelope.getWidth();
        double scaleY = height / envelope.getHeight();
        double scale = Math.min(scaleX, scaleY); // 保持比例
        double offsetX = -envelope.getMinX() * scale;
        double offsetY = -envelope.getMinY() * scale;

        // 绘制原始多边形
        g2d.setColor(new Color(0, 0, 255, 50)); // 设置蓝色填充并带有透明度
        fillPolygon(g2d, polygon, scale, offsetX, offsetY, height);

        // 检查和打印交集区域的坐标
        if (!intersection.isEmpty()) {
            //System.out.println("Intersection coordinates:");
            //for (Coordinate coord : intersection.getCoordinates()) {
            //    System.out.println(coord);
            //}

            // 绘制相交区域
            g2d.setColor(new Color(255, 255, 0)); // 设置黄色填充
            fillPolygon(g2d, intersection, scale, offsetX, offsetY, height);
        } else {
            System.out.println("No intersection found.");
        }

        g2d.dispose();

        // 将图像保存为文件进行调试
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputfile = new File(outputDir, "yellow.png");
            ImageIO.write(image, "png", outputfile);
            //System.out.println("Image saved to: " + outputfile.getAbsolutePath()); // 调试输出保存路径
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    //创建绘制绿色重合区域后的图片
    private BufferedImage createImageWithPolygons3(Geometry polygon, Geometry intersection) {
        int width = 150;
        int height = 150;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置缩放比例和偏移
        Envelope envelope = polygon.getEnvelopeInternal();
        double scaleX = width / envelope.getWidth();
        double scaleY = height / envelope.getHeight();
        double scale = Math.min(scaleX, scaleY); // 保持比例
        double offsetX = -envelope.getMinX() * scale;
        double offsetY = -envelope.getMinY() * scale;

        // 绘制原始多边形
        g2d.setColor(new Color(0, 0, 255, 50)); // 设置蓝色填充并带有透明度
        fillPolygon(g2d, polygon, scale, offsetX, offsetY, height);

        // 检查和打印交集区域的坐标
        if (!intersection.isEmpty()) {
            //System.out.println("Intersection coordinates:");
            //for (Coordinate coord : intersection.getCoordinates()) {
            //    System.out.println(coord);
            //}

            // 绘制相交区域
            g2d.setColor(new Color(0, 255, 0)); // 设置绿色
            fillPolygon(g2d, intersection, scale, offsetX, offsetY, height);
        } else {
            System.out.println("No intersection found.");
        }

        g2d.dispose();

        // 将图像保存为文件进行调试
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputfile = new File(outputDir, "green.png");
            ImageIO.write(image, "png", outputfile);
            //System.out.println("Image saved to: " + outputfile.getAbsolutePath()); // 调试输出保存路径
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    // 填充原区域和重合区域
    private void fillPolygon(Graphics2D g2d, Geometry geometry, double scale, double offsetX, double offsetY, int height) {
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] coordinates = polygon.getCoordinates();
            int[] xPoints = new int[coordinates.length];
            int[] yPoints = new int[coordinates.length];
            for (int i = 0; i < coordinates.length; i++) {
                xPoints[i] = (int) (coordinates[i].x * scale + offsetX); // 根据实际情况进行缩放和平移
                yPoints[i] = height - (int) (coordinates[i].y * scale + offsetY); // 根据实际情况进行缩放和平移，并翻转Y轴
            }
            g2d.fillPolygon(xPoints, yPoints, coordinates.length);
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                fillPolygon(g2d, multiPolygon.getGeometryN(i), scale, offsetX, offsetY, height);
            }
        }
    }

    public boolean checkprefix(String code1,String code2,int len){
        if(code1.length() < len || code2.length() < len){
            return false;
        }

        String prefix1 = code1.substring(0, len);
        String prefix2 = code2.substring(0, len);

        return prefix1.startsWith(prefix2);
    }

    //转化为国空布置养殖场不通过
    public boolean isNotPass(String code){
        if(code.equals("0201")){
            return true;
        }

        if(code.equals("0202")){
            return true;
        }

        if(code.equals("0203")){
            return true;
        }

        if(code.equals("0301")){
            return true;
        }

        if(code.equals("0302")){
            return true;
        }

        if(code.equals("0303")){
            return true;
        }

        if(checkprefix(code,"05",2)){
            return true;
        }

        if(code.equals("060101")){
            return true;
        }

        if(code.equals("060102")){
            return true;
        }

        if(code.equals("060201")){
            return true;
        }

        if(checkprefix(code,"07",2)){
            return true;
        }

        if(checkprefix(code,"08",2)){
            return true;
        }

        if(checkprefix(code,"09",2)){
            return true;
        }

        if(checkprefix(code,"10",2)){
            return true;
        }

        if(checkprefix(code,"11",2)){
            return true;
        }

        if(checkprefix(code,"12",2)){
            return true;
        }

        if(checkprefix(code,"13",2)){
            return true;
        }

        if(checkprefix(code,"14",2)){
            return true;
        }

        if(checkprefix(code,"15",2)){
            return true;
        }

        if(checkprefix(code,"17",2)){
            return true;
        }

        if(checkprefix(code,"18",2)){
            return true;
        }

        if(checkprefix(code,"19",2)){
            return true;
        }

        if(checkprefix(code,"20",2)){
            return true;
        }

        if(checkprefix(code,"21",2)){
            return true;
        }

        if(checkprefix(code,"22",2)){
            return true;
        }

        if(code.equals("2301")){
            return true;
        }

        if(code.equals("2307")){
            return true;
        }

        if(checkprefix(code,"24",2)){
            return true;
        }

        return false;

    }

    //转化为国空布置养殖场办手续通过
    public boolean isWaitPass(String code){
        if(checkprefix(code,"0204",4)){
            return true;
        }

        if(checkprefix(code,"0304",4)){
            return true;
        }

        if(checkprefix(code,"04",2)){
            return true;
        }

        return false;
    }

    //转化为国空布置公厕有效代码
    public boolean isValidCode(String code){
        if(checkprefix(code,"0704",4)){
            return true;
        }
        if(checkprefix(code,"08",2)){
            return true;
        }
        if(checkprefix(code,"14",2)){
            return true;
        }

        return false;
    }

    //将Geometry几何对象转化坐标系
    private static Geometry transformGeometry(Geometry geom, CoordinateTransform transform) {
        GeometryTransformer transformer = new GeometryTransformer() {
            @Override
            protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
                ProjCoordinate srcCoord = new ProjCoordinate();
                ProjCoordinate destCoord = new ProjCoordinate();
                CoordinateSequence newCoords = geom.getFactory().getCoordinateSequenceFactory().create(coords.size(), 2);
                for (int i = 0; i < coords.size(); i++) {
                    srcCoord.x = coords.getX(i);
                    srcCoord.y = coords.getY(i);
                    transform.transform(srcCoord, destCoord);
                    if (Double.isInfinite(destCoord.x) || Double.isInfinite(destCoord.y)) {
                        System.out.printf("Warning: Infinite value detected for input (%f, %f). Transformed to (%f, %f)\n",
                                srcCoord.x, srcCoord.y, destCoord.x, destCoord.y);
                    }
                    newCoords.setOrdinate(i, 0, destCoord.x);
                    newCoords.setOrdinate(i, 1, destCoord.y);
                }
                return newCoords;
            }
        };

        if (geom instanceof Polygon) {
            return transformer.transform(geom);
        } else if (geom instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geom;
            Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                polygons[i] = (Polygon) transformer.transform(multiPolygon.getGeometryN(i));
            }
            return geom.getFactory().createMultiPolygon(polygons);
        } else if (geom instanceof GeometryCollection) {
            GeometryCollection geometryCollection = (GeometryCollection) geom;
            Geometry[] geometries = new Geometry[geometryCollection.getNumGeometries()];
            for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                geometries[i] = transformGeometry(geometryCollection.getGeometryN(i), transform);
            }
            return geom.getFactory().createGeometryCollection(geometries);
        } else {
            throw new IllegalArgumentException("Unsupported Geometry type: " + geom.getGeometryType());
        }
    }

    //转化为国空布置垃圾收集点有效代码
    public boolean isGarbageCode(String code){
        if(checkprefix(code,"08",2)){
            if(code.equals("080401") || code.equals("080402") || code.equals("080403") || code.equals("080404")){
                return false;
            }else {
                return true;
            }
        }
        if(code.equals("1309")){
            return true;
        }
        if(checkprefix(code,"14",2)){
            return true;
        }
        return false;
    }

    //转化为国空布置农村宅基地不通过
    public boolean isNotPassForRuralLands(String code){
        if(code.equals("0504")){
            return true;
        }
        if(code.equals("0505")){
            return true;
        }
        if(code.equals("0506")){
            return true;
        }
        if(checkprefix(code,"07",2)){
            if(code.equals("0703")){
                return false;
            }else {
                return true;
            }
        }
        if(checkprefix(code,"08",2)){
            return true;
        }
        if(checkprefix(code,"09",2)){
            return true;
        }
        if(checkprefix(code,"10",2)){
            return true;
        }
        if(checkprefix(code,"11",2)){
            return true;
        }
        if(checkprefix(code,"12",2)){
            return true;
        }
        if(checkprefix(code,"13",2)){
            return true;
        }
        if(checkprefix(code,"14",2)){
            return true;
        }
        if(checkprefix(code,"15",2)){
            return true;
        }
        if(checkprefix(code,"16",2)){
            return true;
        }
        if(code.equals("1701")){
            return true;
        }
        if(code.equals("1702")){
            return true;
        }
        if(checkprefix(code,"18",2)){
            return true;
        }
        if(checkprefix(code,"19",2)){
            return true;
        }
        if(checkprefix(code,"20",2)){
            return true;
        }
        if(checkprefix(code,"21",2)){
            return true;
        }
        if(checkprefix(code,"22",2)){
            return true;
        }
        if(checkprefix(code,"23",2)){
            if(code.equals("2301") || code.equals("2302")){
                return false;
            }else {
                return true;
            }
        }
        if(checkprefix(code,"24",2)){
            return true;
        }
        return false;
    }

    //转化为国空布置农村宅基地办理手续后通过
    public boolean isWaitPassForRuralLands(String code){
        if(checkprefix(code,"01",2)){
            return true;
        }
        if(checkprefix(code,"02",2)){
            return true;
        }
        if(checkprefix(code,"03",2)){
            return true;
        }
        if(checkprefix(code,"04",2)){
            return true;
        }
        if(code.equals("0501")){
            return true;
        }
        if(code.equals("0502")){
            return true;
        }
        if(code.equals("0503")){
            return true;
        }
        if(code.equals("0507")){
            return true;
        }
        if(checkprefix(code,"06",2)){
            return true;
        }
        if(code.equals("1703")){
            return true;
        }
        if(code.equals("1704")){
            return true;
        }
        if(code.equals("1705")){
            return true;
        }
        if(code.equals("2302")){
            return true;
        }
        return false;
    }

    //选址比对生态保护红线，永久基本农田，高标农田占用情况
    public FormerRedData getFormerRedDataFor_BH_YJ_GB(Geometry polygon){
        //返回结果类型
        FormerRedData formerRedData = new FormerRedData();

        //存储各个图层的交集面积和项目名称
        List<DataDetail> details = new ArrayList<>();
        //存储变量polygon,相当于全局变量,类型换成基类Geometry
        Map<String,Geometry> map = new HashMap();
        //Map<String,Geometry> map = new HashMap();
        map.put("new_polygon",polygon);
        //存储红色区域图层
        List<Geometry> red = new ArrayList<>();

        // 构建GeoServer查询生态保护红线URL
        String url_bhhx = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_bhhx");

        // 构建GeoServer查询永久基本农田URL
        String url_yjjbnt = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_yjjbnt");

        // 构建GeoServer查询高标农田URL
        String url_gbnt = String.format("%s/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=%s&outputFormat=application/json",
                geoserverUrl, "tongguan:dongxiao_22gb");


        // 发起请求获取生态保护红线图层数据
        ResponseEntity<String> response_bhhx = restTemplate.getForEntity(url_bhhx, String.class);

        System.out.println("GeoServer response_bhhx status: " + response_bhhx.getStatusCode());

        if (response_bhhx.getStatusCode().is2xxSuccessful()) {
            String responseBody_bhhx = response_bhhx.getBody();
            //System.out.println("GeoServer response body: " + responseBody);
            System.out.println("--------------------------------------------------------------------------");
            try {
                // 解析GeoServer返回的JSON数据
                JsonNode rootNode_bhhx = objectMapper.readTree(responseBody_bhhx);
                JsonNode features_bhhx = rootNode_bhhx.path("features");

                //TODO:创建一个存储绘制区域与生态保护红线交集的区域集合
                List<Geometry> bhhx = new ArrayList<>();
                DataDetail dataDetail_bhhx = new DataDetail();
                Areas areas_bhhx = new Areas();

                for (JsonNode feature_bhhx : features_bhhx) {
//                    DataDetail dataDetail = new DataDetail();
//                    Areas areas = new Areas();
                    JsonNode geometryNode_bhhx = feature_bhhx.path("geometry");

                    //保存项目名信息到DataDetail实体类
                    dataDetail_bhhx.setProjectName(feature_bhhx.path("properties").path("HXMC").asText());

                    // 检查几何数据是否存在
                    if (geometryNode_bhhx.isMissingNode() || geometryNode_bhhx.isNull()) {
                        System.err.println("Geometry node is missing or null for feature ID: " + feature_bhhx.path("id").asText());
                        continue;
                    }

                    // 将GeoJSON几何数据解析为JTS Geometry对象
                    Geometry layerGeometry_bhhx = parseGeoJsonToGeometry(geometryNode_bhhx);
                    if (!layerGeometry_bhhx.isEmpty()) {
                        // 打印图层几何信息
                        //System.out.println("Layer geometry:");
                        //System.out.println(layerGeometry);

                        IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                        IsValidOp isValidOp2 = new IsValidOp(layerGeometry_bhhx);

                        if(isValidOp1.isValid() && isValidOp2.isValid()){
                            Geometry intersection_bhhx = map.get("new_polygon").intersection(layerGeometry_bhhx);

                            if(!intersection_bhhx.isEmpty()){
                                IsValidOp isValidOp_bhhx = new IsValidOp(intersection_bhhx);
                                if(isValidOp_bhhx.isValid()){
                                    bhhx.add(intersection_bhhx);
                                }
                            }
                        }

                    } else {
                        System.err.println("Layer geometry is null for feature ID: " + feature_bhhx.path("id").asText());
                    }
                }

                if (!bhhx.isEmpty()) {

                    Geometry bhhxAll = UnaryUnionOp.union(bhhx);
                    double bhhxAllintersectionArea = bhhxAll.getArea();

                    //保存重叠面积信息到DataDetail实体类
                    areas_bhhx.setAreaId("");
                    areas_bhhx.setArea(String.format("%.2f",bhhxAllintersectionArea));
                    dataDetail_bhhx.setAreas(areas_bhhx);

                    // 生成包含重合区域和原先绘制区域的图片
                    BufferedImage image = createImageWithPolygons1(polygon, bhhxAll);

                    // 将图像转换为 Base64 编码的字符串
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", baos);
                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                    //System.out.println("Base64 Image: " + base64Image); // 调试输出 Base64 字符串

                    //保存生成的重叠面积图片到DataDetail实体类
                    dataDetail_bhhx.setImg(base64Image);

                    dataDetail_bhhx.setTag(0);

                    details.add(dataDetail_bhhx);

                    IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                    IsValidOp isValidOp2 = new IsValidOp(bhhxAll);
                    if(isValidOp1.isValid() && isValidOp2.isValid()){
                        Geometry new_polygon = map.get("new_polygon").difference(bhhxAll);
                        map.put("new_polygon",new_polygon);

                        //传入红色区域
                        red.add(bhhxAll);

                        System.out.println("绘制区域与：【"+dataDetail_bhhx.getProjectName()+"】存在"+dataDetail_bhhx.getAreas().getArea()+"平方米的重合面积，不予通过");

                    }
                }

                //TODO:开始比对永久基本农田逻辑

                // 发起请求获取永久基本农田图层数据
                ResponseEntity<String> response_yjjbnt = restTemplate.getForEntity(url_yjjbnt, String.class);

                System.out.println("GeoServer response_yjjbnt status: " + response_yjjbnt.getStatusCode());

                if (response_yjjbnt.getStatusCode().is2xxSuccessful()) {

                    String response_yjjbntBody = response_yjjbnt.getBody();
                    System.out.println("--------------------------------------------------------------------------");

                    // 解析GeoServer返回的JSON数据
                    JsonNode yjjbnt_rootNode = objectMapper.readTree(response_yjjbntBody);
                    JsonNode features_yjjbnt = yjjbnt_rootNode.path("features");

                    //TODO:创建一个存储绘制区域与永久基本农田的交集区域
                    List<Geometry> yjjbnt = new ArrayList<>();
                    DataDetail dataDetail_yjjbnt = new DataDetail();
                    Areas areas_yjjbnt = new Areas();

                    for (JsonNode feature_yjjbnt : features_yjjbnt) {
                        JsonNode geometryNode_yjjbnt = feature_yjjbnt.path("geometry");

                        //保存项目名信息到DataDetail实体类
                        dataDetail_yjjbnt.setProjectName("永久基本农田:"+feature_yjjbnt.path("properties").path("DLMC").asText());

                        // 检查几何数据是否存在
                        if (geometryNode_yjjbnt.isMissingNode() || geometryNode_yjjbnt.isNull()) {
                            System.err.println("Geometry node is missing or null for feature ID: " + feature_yjjbnt.path("id").asText());
                            continue;
                        }

                        // 将GeoJSON几何数据解析为JTS Geometry对象
                        Geometry layerGeometry_yjjbnt = parseGeoJsonToGeometry(geometryNode_yjjbnt);
                        if (!layerGeometry_yjjbnt.isEmpty()) {

                            IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                            IsValidOp isValidOp2 = new IsValidOp(layerGeometry_yjjbnt);
                            if(isValidOp1.isValid() && isValidOp2.isValid()){
                                Geometry intersection_yjjbnt = map.get("new_polygon").intersection(layerGeometry_yjjbnt);

                                if(!intersection_yjjbnt.isEmpty()){
                                    IsValidOp isValidOp_yjjbnt = new IsValidOp(intersection_yjjbnt);
                                    if(isValidOp_yjjbnt.isValid()){
                                        yjjbnt.add(intersection_yjjbnt);
                                    }
                                }
                            }



                        } else {
                            System.err.println("Layer geometry is null for feature ID: " + feature_yjjbnt.path("id").asText());
                        }

                    }

                    if (!yjjbnt.isEmpty()) {

                        Geometry yjjbntAll = UnaryUnionOp.union(yjjbnt);
                        double yjjbntAllintersectionArea = yjjbntAll.getArea();
                        //Coordinate[] yjjbntAllintersectionCoordinates = yjjbntAll.getCoordinates();
                        //Coordinate[] yjjbntAllcoordinates = transformCoordinates2(yjjbntAllintersectionCoordinates, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                        //dataDetail_yjjbnt.setIntersectionCoordinates(yjjbntAllcoordinates);
                        //保存重叠面积信息到DataDetail实体类
                        areas_yjjbnt.setAreaId("");
                        areas_yjjbnt.setArea(String.format("%.2f",yjjbntAllintersectionArea));
                        dataDetail_yjjbnt.setAreas(areas_yjjbnt);

                        // 生成包含重合区域和原先绘制区域的图片
                        BufferedImage image = createImageWithPolygons1(polygon, yjjbntAll);

                        // 将图像转换为 Base64 编码的字符串
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", baos);
                        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                        //保存生成的重叠面积图片到DataDetail实体类
                        dataDetail_yjjbnt.setImg(base64Image);
                        dataDetail_yjjbnt.setTag(0);

                        details.add(dataDetail_yjjbnt);

                        IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                        IsValidOp isValidOp2 = new IsValidOp(yjjbntAll);

                        if(isValidOp1.isValid() && isValidOp2.isValid()){
                            Geometry new_polygon = map.get("new_polygon").difference(yjjbntAll);
                            map.put("new_polygon",new_polygon);

                            //传入红色区域
                            red.add(yjjbntAll);

                            System.out.println("绘制区域与：【"+dataDetail_yjjbnt.getProjectName()+"】存在"+dataDetail_yjjbnt.getAreas().getArea()+"平方米的重合面积，不予通过");

                        }
                    }

                    //TODO:开始比对高标农田逻辑
                    //开始比对高标农田逻辑

                    // 发起请求获取高标农田图层数据
                    ResponseEntity<String> response_gbnt = restTemplate.getForEntity(url_gbnt, String.class);

                    System.out.println("GeoServer response_gbnt status: " + response_gbnt.getStatusCode());

                    if (response_gbnt.getStatusCode().is2xxSuccessful()) {

                        String response_gbntBody = response_gbnt.getBody();
                        System.out.println("--------------------------------------------------------------------------");

                        // 解析GeoServer返回的JSON数据
                        JsonNode gbnt_rootNode = objectMapper.readTree(response_gbntBody);
                        JsonNode features_gbnt = gbnt_rootNode.path("features");

                        //TODO:创建一个存储绘制区域与高标农田的交集区域
                        List<Geometry> gbnt = new ArrayList<>();
                        DataDetail dataDetail_gbnt = new DataDetail();
                        Areas areas_gbnt = new Areas();

                        for (JsonNode feature_gbnt : features_gbnt) {
                            JsonNode geometryNode_gbnt = feature_gbnt.path("geometry");

                            //保存项目名信息到DataDetail实体类
                            dataDetail_gbnt.setProjectName(feature_gbnt.path("properties").path("项目名").asText());

                            // 检查几何数据是否存在
                            if (geometryNode_gbnt.isMissingNode() || geometryNode_gbnt.isNull()) {
                                System.err.println("Geometry node is missing or null for feature ID: " + feature_gbnt.path("id").asText());
                                continue;
                            }

                            // 将GeoJSON几何数据解析为JTS Geometry对象
                            Geometry layerGeometry_gbnt = parseGeoJsonToGeometry(geometryNode_gbnt);
                            if (!layerGeometry_gbnt.isEmpty()) {

                                IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                                IsValidOp isValidOp2 = new IsValidOp(layerGeometry_gbnt);
                                if(isValidOp1.isValid() && isValidOp2.isValid()){
                                    Geometry intersection_gbnt = map.get("new_polygon").intersection(layerGeometry_gbnt);

                                    if(!intersection_gbnt.isEmpty()){
                                        IsValidOp isValidOp_gbnt = new IsValidOp(intersection_gbnt);
                                        if(isValidOp_gbnt.isValid()){
                                            gbnt.add(intersection_gbnt);
                                        }
                                    }
                                }

                            } else {
                                System.err.println("Layer geometry is null for feature ID: " + feature_gbnt.path("id").asText());
                            }
                        }

                        if (!gbnt.isEmpty()) {

                            Geometry gbntAll = UnaryUnionOp.union(gbnt);
                            double gbntAllintersectionArea = gbntAll.getArea();
                            //Coordinate[] gbntAllintersectionCoordinates = gbntAll.getCoordinates();
                            //Coordinate[] gbntAllcoordinates = transformCoordinates2(gbntAllintersectionCoordinates, "+proj=tmerc +lat_0=0 +lon_0=111 +k=1.0 +x_0=37500000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs", "EPSG:4326");
                            //dataDetail_gbnt.setIntersectionCoordinates(gbntAllcoordinates);
                            //保存重叠面积信息到DataDetail实体类
                            areas_gbnt.setAreaId("");
                            areas_gbnt.setArea(String.format("%.2f",gbntAllintersectionArea));
                            dataDetail_gbnt.setAreas(areas_gbnt);

                            // 生成包含重合区域和原先绘制区域的图片
                            BufferedImage image = createImageWithPolygons1(polygon, gbntAll);

                            // 将图像转换为 Base64 编码的字符串
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "png", baos);
                            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                            //保存生成的重叠面积图片到DataDetail实体类
                            dataDetail_gbnt.setImg(base64Image);
                            dataDetail_gbnt.setTag(0);

                            details.add(dataDetail_gbnt);

                            IsValidOp isValidOp1 = new IsValidOp(map.get("new_polygon"));
                            IsValidOp isValidOp2 = new IsValidOp(gbntAll);
                            if(isValidOp1.isValid() && isValidOp2.isValid()){
                                Geometry new_polygon = map.get("new_polygon").difference(gbntAll);
                                map.put("new_polygon",new_polygon);

                                //传入红色区域
                                red.add(gbntAll);

                                System.out.println("绘制区域与：【"+dataDetail_gbnt.getProjectName()+"】存在"+dataDetail_gbnt.getAreas().getArea()+"平方米的重合面积，不予通过");

                                //封装数据到FormerRedData中
                                formerRedData.setDetails(details);
                                formerRedData.setRed(red);
                                formerRedData.setMap(map);
                            }
                        }

                    }

                }


            } catch (IOException e) {
                System.err.println("Failed to parse response JSON with error: " + e.getMessage());
            }
        } else {
            System.err.println("Failed to fetch data from GeoServer. Status Code: " + response_bhhx.getStatusCode());
            return null;
        }
        return formerRedData;
    }
}
