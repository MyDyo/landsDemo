package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class userFile implements Serializable {
 private static final long serialVersionUID=1L;

    @TableId(value = "userFileId", type = IdType.AUTO)
    private Integer userFileId;
    @TableField("userName")
    private String userName;
    @TableField("userFileName")
    private String userFileName;
    @TableField("userFileURL")
    private String userFileURL;//内容

    public Integer getUserFileId() {
        return userFileId;
    }

    public void setUserFileId(Integer userFileId) {
        this.userFileId = userFileId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFileName() {
        return userFileName;
    }

    public void setUserFileName(String userFileName) {
        this.userFileName = userFileName;
    }

    public String getUserFileURL() {
        return userFileURL;
    }

    public void setUserFileURL(String userFileURL) {
        this.userFileURL = userFileURL;
    }
}
