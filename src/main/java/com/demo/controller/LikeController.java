package com.demo.controller;

import com.demo.entity.Comment;
import com.demo.entity.Like;
import com.demo.entity.Post;
import com.demo.service.LikeService;
import com.demo.util.resultCode.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class LikeController {


   @Autowired
    private LikeService likeService;
////增加点赞帖子记录
//    @PostMapping("/posts/likes/{postId}")
//    public R likePost(@PathVariable int postId, @RequestBody int userId) {
//        likeService.likePost(userId, postId);
//        return R.ok();
//    }
//增加点赞评论记录
    @PostMapping("/comments/likes/{commentId}")
    public R likeComment(@PathVariable int commentId, @RequestBody int userId) {
        likeService.likeComment(userId, commentId);
        return R.ok();
    }

    // 取消点赞帖子
    @DeleteMapping("/posts/likes/{postId}/{likeId}")
    public R unlikePost(@PathVariable int likeId) {
        likeService.removeById(likeId);
        return R.ok();
    }

    // 取消点赞评论
    @DeleteMapping("/comments/{commentId}/likes/{likeId}")
    public R unlikeComment(@PathVariable int likeId) {
        likeService.removeById(likeId);
        return R.ok();
    }
}
