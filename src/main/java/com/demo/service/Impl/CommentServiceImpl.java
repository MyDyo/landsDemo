package com.demo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.Comment;
import com.demo.mapper.CommentMapper;
import com.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public List<Comment> getCommentsByPostId(int postId) {
        return commentMapper.selectCommentsByPostId(postId);
    }

    @Override
    public Comment getCommentById(int commentId) {
        return commentMapper.selectCommentById(commentId);
    }

    @Override
    @Transactional
    public Comment saveComment(Comment comment) {
        commentMapper.insertComment(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment updateComment(Comment comment) {
        commentMapper.updateComment(comment);
        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(int commentId) {
        commentMapper.deleteCommentById(commentId);
    }

    @Transactional
    public void addReplyComment(Comment comment) {
        commentMapper.insertComment(comment);
    }
}
