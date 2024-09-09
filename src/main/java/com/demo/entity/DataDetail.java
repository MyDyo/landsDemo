package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataDetail {

    //项目名
    private String projectName;

    //重叠面积
    private Areas areas;

//    //重叠面积坐标
//    private Coordinate[] IntersectionCoordinates;

    //重叠图片
    private String img;

    //建养殖场/农村宅基地不通过还是办理手续后可以通过,0表示不通过,1表示办理手续后可通过，
    //2表示公共卫生厕所与080404没有保持10m以上距离，3表示公共卫生厕所与1301没有保持30m以上距离
    //4表示宅基地与国道(1202)没有保持20m以上距离，5表示宅基地与县道(1207)没有保持10m以上距离
    private int tag;
}
