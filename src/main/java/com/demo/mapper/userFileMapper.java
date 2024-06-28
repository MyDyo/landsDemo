package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.userFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface userFileMapper extends BaseMapper<userFile> {
    void insertUserFile(userFile userFile);
    userFile selectUserFileById(Integer userFileId);
    userFile selectUserFileByuserFileURL(String userFileUrl);
}
