package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
     List<Comment> selectCommentsByPostId(int postId);
    Comment selectCommentById(int commentId);
    void insertComment(Comment comment);
    void updateComment(Comment comment);
    void deleteCommentById(int commentId);
    void insertReplyComment(Comment comment);

}

