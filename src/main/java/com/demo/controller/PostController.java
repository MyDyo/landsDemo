package com.demo.controller;

import com.demo.entity.Post;
import com.demo.mapper.PostMapper;
import com.demo.util.resultCode.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostMapper postMapper;
    @GetMapping("getAllPosts")
    public R getAllPosts() {
        List<Post> posts=postMapper.selectAllPosts();
        return R.ok().data("posts",posts);
    }

    @GetMapping("/{postId}")
    public R getPostById(@PathVariable int postId) {
        Post post=postMapper.selectPostById(postId);
        return R.ok().data("post",post);
    }

    @GetMapping("/by-tag")
    public R getPostsByTag(@RequestParam("tag") String tags) {
        List<Post> posts=postMapper.selectPostsByTag(tags);
        return R.ok().data("posts",posts);
    }


    @PostMapping("createPost")
    public R createPost(@RequestBody Post post) {
        postMapper.insertPost(post);
        return R.ok();
    }

    @PutMapping("updatePost/{postId}")
    public R updatePost(@PathVariable int postId, @RequestBody Post post) {
        post.setPostId(postId);
        postMapper.updatePost(post);
        return R.ok();
    }

    @DeleteMapping("/{postId}")
    public R deletePost(@PathVariable int postId) {
        postMapper.deletePostById(postId);
        return R.ok();
    }
}
