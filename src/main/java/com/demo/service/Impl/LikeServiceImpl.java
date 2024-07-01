package com.demo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.Like;
import com.demo.entity.Post;
import com.demo.mapper.LikeMapper;
import com.demo.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {
    @Autowired
    private LikeMapper likeMapper;
//    @Autowired
//    @Transactional
//    public void likePost(int userId, int postId) {
//        Like like = new Like();
//        like.setUserId(userId);
//        like.setPostId(postId);
//        like.setCreatedAt(LocalDateTime.now());
//        likeMapper.insertLike(like);
//    }

    @Override
    @Transactional
    public void likeComment(int userId, int commentId) {
        Like like = new Like();
        like.setUserId(userId);
        like.setCommentId(commentId);
        like.setCreatedAt(LocalDateTime.now());
        likeMapper.insertLike(like);
    }

    @Override
    public int getLikesCountForPost(int postId) {
        return likeMapper.countLikesForPost(postId);
    }

    @Override
    public int getLikesCountForComment(int commentId) {
        return likeMapper.countLikesForComment(commentId);
    }
}
