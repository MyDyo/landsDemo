package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
@TableName("filemeta")
public class fileMeta implements Serializable {
 private static final long serialVersionUID=1L;

    @TableId(value = "fileId", type = IdType.AUTO)
    private Integer fileId;
    @TableField("fileName")
    private String fileName;
    @TableField("fileStyle")
    private String fileStyle;
    @TableField("fileURL")
    private String fileURL;//内容
    @TableField("uploadTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    private Date uploadTime;//上传时间

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileStyle() {
        return fileStyle;
    }

    public void setFileStyle(String fileStyle) {
        this.fileStyle = fileStyle;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }
}
