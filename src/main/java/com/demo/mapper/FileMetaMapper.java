package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.fileMeta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface FileMetaMapper extends BaseMapper<fileMeta> {
    void insertFileMeta( String fileName,  String fileStyle, Date uploadTime,  String fileURL);
    fileMeta selectFileMetaById(@Param("fileId") int fileId);
    List<fileMeta> findAll();
}
