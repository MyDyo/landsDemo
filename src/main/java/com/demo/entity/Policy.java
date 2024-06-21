package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class Policy implements Serializable {
 private static final long serialVersionUID=1L;

    @TableId(value = "policyId", type = IdType.AUTO)
    private Integer policyId;
    @TableField("title")
    private String title;
    @TableField("source")
    private String source;
    @TableField("content")
    private String content;//内容
    @TableField("uploadTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    private Date uploadTime;//上传时间

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public Date getUploadTime() {
      return uploadTime;
   }

   public void setUploadTime(Date uploadTime) {
      this.uploadTime = uploadTime;
   }

   public Integer getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Integer policyId) {
        this.policyId = policyId;
    }
}
