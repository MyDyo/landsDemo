package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.Like;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper extends BaseMapper<Like> {
    void insertLike(Like like);
    int countLikesForPost(int postId);
    int countLikesForComment(int commentId);

}
