package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    List<Post> selectAllPosts();
    Post selectPostById(int postId);
    List<Post> selectPostsByTag(@Param("tag") String tag);
    void insertPost(Post post);
    void updatePost(Post post);
    void deletePostById(int postId);
}
