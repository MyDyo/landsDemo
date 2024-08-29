package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对比封闭区域后返回给前端的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultData {

    //返回包括红，黄，绿所示区域的重叠面积
    private OverLapArea overLapArea;

    //返回比对报告中的数据
    private List<DataDetail> dataDetails;

}
