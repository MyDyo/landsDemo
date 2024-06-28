package com.demo.controller;

import com.demo.entity.Comment;
import com.demo.service.CommentService;
import com.demo.util.resultCode.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/getCommentsByPostIds/{postId}")
    public R getCommentsByPostId(@PathVariable int postId) {
        List<Comment> comments=commentService.getCommentsByPostId(postId);
        return R.ok().data("comments",comments);
    }

//    @GetMapping("/{commentId}")
//    public R getCommentById(@PathVariable int postId,@PathVariable int commentId) {
//        Comment comment=commentService.getCommentById(commentId);
//        return R.ok().data("comment",comment);
//    }

    @PostMapping("addComment")
    public R addComment(@RequestBody Comment comment) {
        commentService.saveComment(comment);
        return R.ok();
    }

    @PutMapping("/{commentId}")
    public R updateComment( @RequestBody Comment comment) {
//        comment.setCommentId(commentId);
        commentService.updateComment(comment);
        return R.ok();
    }
    @PostMapping("/addReplyComment/{commentId}")
    public R addReplyComment(@PathVariable int commentId, @RequestBody Comment comment) {
        comment.setParentId(commentId);
        commentService.addReplyComment(comment);
        return R.ok();
    }

    @DeleteMapping("/{commentId}")
    public R deleteComment(@PathVariable int commentId) {
        commentService.deleteComment(commentId);
        return R.ok();
    }
}
