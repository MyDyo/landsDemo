package com.demo.service;

import com.demo.entity.PointInfo;
import com.demo.entity.ResultData;
import com.demo.entity.ResultPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.locationtech.jts.geom.Coordinate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PolygonService {
    ResultPoint comparePoint(Map<String, PointInfo> requestBody) throws IOException;

    ResultData comparePolygon(Map<String, List<List<Double>>> requestBody);

    List<Coordinate[]> getValidShowLayerForWC() throws JsonProcessingException;

    List<Coordinate[]> getValidShowLayerForBHHX() throws JsonProcessingException;

    List<Coordinate[]> getValidShowLayerForYJJBNT() throws JsonProcessingException;

    List<Coordinate[]> getValidShowLayerForGBNT() throws JsonProcessingException;
}
