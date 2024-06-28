package com.demo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.demo.entity.fileMeta;
import com.demo.mapper.FileMetaMapper;
import com.demo.service.fileMetaService;
import org.springframework.stereotype.Service;

@Service
public class fileMetaServiceImpl extends ServiceImpl<FileMetaMapper, fileMeta> implements fileMetaService {

}
