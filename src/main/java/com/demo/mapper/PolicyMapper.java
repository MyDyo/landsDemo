package com.demo.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.entity.Policy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface PolicyMapper extends BaseMapper<Policy> {
//    List<Policy> findByTitleContaining(String keyword);
//    // 使用MyBatis Plus的分页功能
////    List<Policy> selectPage(Page<?> page, @Param(Constants.WRAPPER) QueryWrapper<Policy> queryWrapper);
//    List<Policy> findAll(RowBounds rowBounds);
//    List<Policy> findByTitleContaining(@Param("title") String title, RowBounds rowBounds);

}