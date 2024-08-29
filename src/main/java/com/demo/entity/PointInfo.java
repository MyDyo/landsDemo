package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointInfo {

    //传入坐标点的经度值
    private Double x;

    //传入坐标点的纬度值
    private Double y;

}
