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
    ResultPoint comparePointForWC(Map<String, PointInfo> requestBody) throws IOException;

    ResultData comparePolygonForFarm(Map<String, List<List<Double>>> requestBody);

    ResultData comparePolygonForRuralLands(Map<String, List<List<Double>>> requestBody) throws IOException;

    List<Coordinate[]> getValidShowLayerForWC() throws JsonProcessingException;

    List<Coordinate[]> getValidShowLayerForBHHX() throws JsonProcessingException;

    List<Coordinate[]> getValidShowLayerForYJJBNT() throws JsonProcessingException;

    List<Coordinate[]> getValidShowLayerForGBNT() throws JsonProcessingException;

    List<Coordinate[]> getValidShowLayerForGarbage() throws JsonProcessingException;

    ResultPoint comparePointForGarbage(Map<String, PointInfo> requestBody) throws IOException;

    Double getPolygonArea(Map<String, Object> requestBody);

    Double getGreenArea(Map<String, Object> requestBody);
}
