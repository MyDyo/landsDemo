package com.demo.controller;

import com.demo.entity.*;
import com.demo.result.Result;
import com.demo.service.PolygonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Api(tags = "规划选址相关接口")
public class PolygonController {

    @Autowired
    private PolygonService polygonService;

    /**
     * 接收前端绘制的封闭区域，并返回养殖场比对结果
     * @param requestBody
     * @return
     */
    @CrossOrigin
    @PostMapping("/compare")
    @ApiOperation("养殖场选址逻辑")
    public Result<ResultData> comparePolygon(@RequestBody Map<String, List<List<Double>>> requestBody) {

        List<List<Double>> polygonPoints = requestBody.get("polygon");

        if (polygonPoints == null || polygonPoints.isEmpty()) {
            System.err.println("Polygon points are null or empty");
            return Result.error("Polygon points are null or empty",null);
        }
        ResultData resultData = polygonService.comparePolygon(requestBody);

        // 返回每个属性块的交集面积和图片
        if (resultData == null) {
            System.err.println("Intersection areas are null");
            return Result.error("Intersection areas are null",null);
        }
        return Result.success("success",resultData);
    }

    /**
     * 接收前端传回的布置公厕的坐标点信息，并返回公厕服务半径下农村宅基地数据
     * @param requestBody
     * @return
     */
    @CrossOrigin
    @PostMapping("/pointForWC")
    @ApiOperation("公厕服务半径下农村宅基地数据")
    public Result<ResultPoint> comparePoint(@RequestBody Map<String,PointInfo> requestBody) throws IOException {

        PointInfo pointInfo = requestBody.get("pointInfo");

        if (pointInfo == null) {
            System.err.println("pointInfo are null");
            return Result.error("pointInfo are null",null);
        }

        ResultPoint resultPoint = polygonService.comparePoint(requestBody);

        // 返回每个属性块的交集面积和图片
        return Result.success("success",resultPoint);
    }

    /**
     * 返回给前端可以布置公共卫生厕所区域的图层信息
     * @return
     * @throws JsonProcessingException
     */
    @CrossOrigin
    @GetMapping("/getValidShowLayerForWC")
    @ApiOperation("布置公共卫生厕所区域的图层信息")
    public Result<Object> getValidShowLayerForWC() throws JsonProcessingException {

        List<Coordinate[]> validcoords = polygonService.getValidShowLayerForWC();

        if(validcoords == null){
            return Result.error("获取展示图层失败",null);
        }
        return Result.success("success",validcoords);
    }

    /**
     * 返回给前端所要展示的生态保护红线图层
     * @return
     * @throws JsonProcessingException
     */
    @CrossOrigin
    @GetMapping("/getValidShowLayerForBHHX")
    @ApiOperation("展示生态保护红线图层")
    public Result<Object> getValidShowLayerForBHHX() throws JsonProcessingException {

        List<Coordinate[]> validcoords = polygonService.getValidShowLayerForBHHX();
        if(validcoords == null){
            return Result.error("获取展示图层失败",null);
        }
        return Result.success("success",validcoords);

    }

    /**
     * 返回给前端所要展示的永久基本农田图层
     * @return
     * @throws JsonProcessingException
     */
    @CrossOrigin
    @GetMapping("/getValidShowLayerForYJJBNT")
    @ApiOperation("展示永久基本农田图层")
    public Result<Object> getValidShowLayerForYJJBNT() throws JsonProcessingException {

        List<Coordinate[]> validcoords = polygonService.getValidShowLayerForYJJBNT();
        if(validcoords == null){
            return Result.error("获取展示图层失败",null);
        }
        return Result.success("success",validcoords);

    }

    /**
     * 返回给前端所要展示的高标农田图层
     * @return
     * @throws JsonProcessingException
     */
    @CrossOrigin
    @GetMapping("/getValidShowLayerForGBNT")
    @ApiOperation("展示高标农田图层")
    public Result<Object> getValidShowLayerForGBNT() throws JsonProcessingException {

        List<Coordinate[]> validcoords = polygonService.getValidShowLayerForGBNT();
        if(validcoords == null){
            return Result.error("获取展示图层失败",null);
        }
        return Result.success("success",validcoords);

    }

}
