package com.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
List<Comment> getCommentsByPostId(int postId);
    Comment getCommentById(int commentId);
    Comment saveComment(Comment comment);
    Comment updateComment(Comment comment);
    void deleteComment(int commentId);
    void addReplyComment(Comment comment);
}
