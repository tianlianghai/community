package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    RedisTemplate redisTemplate;

    //点赞
    public void like(int userId,int entityType,int entityId){
        String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId );
        boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if (isMember){
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        }else {
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }
    }

    //查询某实体点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey );
    }

    //查询某人对某实体点赞状态
    //0表示未点赞,1表示点赞,将来可用-1表示踩
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        boolean isLiked=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if (isLiked){
            return 1;
        }else {
            return 0;
        }
    }
}
