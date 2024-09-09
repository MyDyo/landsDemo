package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormerRedData {

    //存储绘制区域与生态保护红线，永久基本农田，高标农田的交集面积和项目名称
    private List<DataDetail> details = new ArrayList<>();

    //存储绘制区域与生态保护红线，永久基本农田，高标农田的交集红色区域
    private List<Geometry> red = new ArrayList<>();

    //存储绘制区域减去交集区域后剩余的几何图形
    private Map<String,Geometry> map = new HashMap();

}
