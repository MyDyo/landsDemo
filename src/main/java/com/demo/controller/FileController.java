package com.demo.controller;

import com.demo.entity.fileMeta;
import com.demo.mapper.FileMetaMapper;
import com.demo.entity.userFile;
import com.demo.service.userFileService;
import com.demo.util.resultCode.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private userFileService userFileService;
    @Autowired
    private FileMetaMapper fileMetaMapper;
//    管理员在前端表单窗口中填入文件名、文件类型、文件上传时间、文件的本地URL地址，后端根据这些信息，将数据存入数据库
    @PostMapping("uploadFileMeta")
    public R uploadFileMeta(@RequestParam String fileName, @RequestParam String fileStyle, @RequestParam Date uploadTime, @RequestParam String fileURL) {
       fileMetaMapper.insertFileMeta(fileName,fileStyle,uploadTime,fileURL);
       return R.ok();
    }
//    前端传递fileMetaId,后端从数据库调取原始的file文件信息
    @GetMapping("getFileMetaMessage/{fileId}")
    public R getFileMetaMessage(@PathVariable Integer fileId){
        System.out.println(fileId);
        fileMeta fileMeta = fileMetaMapper.selectFileMetaById(fileId);
        String fileName= fileMeta.getFileName();
        String fileStyle = fileMeta.getFileStyle();
        String fileURL =fileMeta.getFileURL();
        Date uploadTime = fileMeta.getUploadTime();
        return R.ok().data("fileName",fileName).data("fileStyle",fileStyle).data("fileURL",fileURL).data("uploadTime",uploadTime);
    }

//查询所有政策列表
    @GetMapping("viewFile")
    public R viewFile(){
        List<fileMeta> fileMetaList = fileMetaMapper.findAll();
        int total = fileMetaList.size();
        return R.ok().data("fileMetaList", fileMetaList).data("total", total);
    }

    @GetMapping("getRelatedFiles/{fileId}")
    public R getRelatedFiles(@PathVariable Integer fileId) {
        Integer prevId;
        prevId=fileId-1;
        System.out.println(prevId);
        Integer nextId;
        nextId=fileId+1;
        System.out.println(nextId);
        fileMeta prevFile = fileMetaMapper.selectFileMetaById(prevId);
        fileMeta nextFile = fileMetaMapper.selectFileMetaById(nextId);
        return R.ok().data("prevFile",prevFile).data("nextFile",nextFile);
    }

//根据从前端获取到的用户名，文件名、url信息将经过用户修改后的文件存入用户文件表
    @PostMapping("createUserFile")
    public R createUserFile(@RequestParam String username, @RequestParam String fileName, @RequestParam String fileUrl) {
        userFileService.createUserFile(username, fileName, fileUrl);
        return R.ok();
    }
//根据从前端获取到的用户文件ｕｒｌ　链接为用户提供下载已经编辑过的用户文件功能
    @GetMapping("/getUserFile")
    public R getUserFileById(@PathVariable @RequestParam String userFileURL) {
        userFile userFile = userFileService.getUserFileByuserFileURL(userFileURL);
        return R.ok().data("userfile",userFile);
    }
}
