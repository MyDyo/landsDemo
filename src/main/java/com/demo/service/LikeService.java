package com.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.Like;

public interface LikeService extends IService<Like> {
    void likePost(int userId, int postId);
    void likeComment(int userId, int commentId);
    int getLikesCountForPost(int postId);
    int getLikesCountForComment(int commentId);
}
