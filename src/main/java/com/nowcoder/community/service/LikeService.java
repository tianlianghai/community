package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    RedisTemplate redisTemplate;

    //点赞
    //开发个人主页时统计某个人所有被点赞数,需要重构点赞方法,以实现通过userId查询所有点赞数
    //由于需要多次数据库操作,所以这里采用事务
    //而Redis数据库多采用编程式事务
    public void like(int userId,int entityType,int entityId,int targetUserId){

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId );
                String userLikeKey=RedisKeyUtil.getUserLikeKey(targetUserId );
                //查询在事务之前完成,否则没有作用,因为Redis事务是用队列完成的.在事务提交后同时执行队列所有语句
                boolean isMember=operations.opsForSet().isMember(entityLikeKey,userId);

                //声明事务开始
                operations.multi();

                //Redis对于未声明的key使用incr命令会自动生成该key并加1,也就是新建一个值为1的key
                //所以这里不必担心key不存在
                if (isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                operations.exec();

                return null;
            }
        });


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

    //查询某人获得赞总数
    //因为需要判断空值所以使用Integer接收而不是int
    public int findUserLikedCount(int userId){
        String userLikedKey=RedisKeyUtil.getUserLikeKey(userId);

//        int count= (int)  redisTemplate.opsForValue().get(userLikedKey);
        Integer count=(Integer)redisTemplate.opsForValue().get(userLikedKey);
        return count==null?0:count.intValue();
    }
}
