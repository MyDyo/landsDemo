package com.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.userFile;
import org.springframework.stereotype.Service;

@Service
public interface userFileService extends IService<userFile> {
     void createUserFile(String username, String fileName, String fileUrl);
    userFile getUserFileById(Integer userFileId);
    userFile getUserFileByuserFileURL(String fileUrl);
}
