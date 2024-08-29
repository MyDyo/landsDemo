package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverLapArea {

    //不通过为红色
    private String redGeoJson;

    //办理手续为黄色
    private String yellowGeoJson;

    //通过为绿色
    private String greenGeoJson;

}
