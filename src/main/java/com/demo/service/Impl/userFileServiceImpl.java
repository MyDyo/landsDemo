package com.demo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.userFile;
import com.demo.mapper.userFileMapper;
import com.demo.service.userFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class userFileServiceImpl extends ServiceImpl<userFileMapper, userFile> implements userFileService {
    @Autowired
    private userFileMapper userFileMapper;

    @Override
    public void createUserFile(String username, String fileName, String fileUrl) {
        String userFileURL = fileUrl + "?user=" + username; // 构造新的 URL
        userFile userFile = new userFile();
        userFile.setUserName(username);
        userFile.setUserFileName(fileName);
        userFile.setUserFileURL(userFileURL);
        userFileMapper.insertUserFile(userFile);
    }

    @Override
    public userFile getUserFileById(Integer userFileId) {
        return userFileMapper.selectUserFileById(userFileId);
    }

    public userFile getUserFileByuserFileURL(String fileUrl){
        return userFileMapper.selectUserFileByuserFileURL(fileUrl);
    }
}
