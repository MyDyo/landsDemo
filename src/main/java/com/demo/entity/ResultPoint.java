package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对比公厕点后返回给前端的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultPoint {

    //圆区域内的代码：0703相应图层高亮
//    private Coordinate[] IntersectionCoordinates;
    private String geojson;

    //返回比对报告中的数据
    private List<DataDetail> details;
}
