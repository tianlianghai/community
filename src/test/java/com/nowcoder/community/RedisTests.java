package com.nowcoder.community;

import com.nowcoder.community.service.LikeService;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    LikeService likeService;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));

    }

    @Test
    public void testList() {

        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));

        //空栈弹出返回null
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "汤家凤", "李永乐", "张宇");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    @Test
    public void testSortedSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 50);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 70);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 60);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "八戒").toString());
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2).toString());
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "八戒").toString());
    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    @Test
    public void testBoundOperation() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:ts";

                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey, "alen");
                redisOperations.opsForSet().add(redisKey, "bob");
                redisOperations.opsForSet().add(redisKey, "cat");

                System.out.println(redisOperations.opsForSet().members(redisKey));
                return redisOperations.exec();
            }
        });

        System.out.println(obj);
    }

    @Test
    public void testNullToInt() {
        likeService.findUserLikedCount(33);
    }


    @Test
    public void testHyperLoglog() {
        String redisKey = "test:hll:01";
        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }
        for (int i = 0; i < 10000; i++) {
            int r = (int) (Math.random() * 10000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    @Test
    public void testUnion() {
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }
        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);
        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);

    }

    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                System.out.println(redisKey);
                System.out.println(redisKey.getBytes());
                for (byte b:redisKey.getBytes()){
                    System.out.println(b);
                }
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);

        // 记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 5, true);
        redisTemplate.opsForValue().setBit(redisKey,6,true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));

        //统计
         obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                System.out.println(redisKey);
                System.out.println(redisKey.getBytes());
                for (byte b:redisKey.getBytes()){
                    System.out.println(b);
                }
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);

    }


    @Test
    public void testByteArr(){
        byte[] arr1={1,2,3};
        System.out.println(arr1);
    }


    @Test
    public void testBitmapOp(){
        String bmKey1="bm01";
        String bmKey2="bm02";
        String bmKey3="bm03";

        redisTemplate.opsForValue().setBit(bmKey1,0,true);
        redisTemplate.opsForValue().setBit(bmKey1,1,true);
        redisTemplate.opsForValue().setBit(bmKey1,2,true);

        redisTemplate.opsForValue().setBit(bmKey2,2,true);
        redisTemplate.opsForValue().setBit(bmKey2,3,true);
        redisTemplate.opsForValue().setBit(bmKey2,4,true);

        redisTemplate.opsForValue().setBit(bmKey3,4,true);
        redisTemplate.opsForValue().setBit(bmKey3,5,true);
        redisTemplate.opsForValue().setBit(bmKey3,6,true);

        String bmKey="bmor";
        Object obj=redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                 redisConnection.bitOp(
                        RedisStringCommands.BitOperation.OR,
                        bmKey.getBytes(),bmKey1.getBytes(),bmKey2.getBytes(),bmKey3.getBytes()
                );
                 return redisConnection.bitCount(bmKey.getBytes()   );
            }
        });
        System.out.println(obj);
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,3));
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,4));
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,5));
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,6));
        System.out.println(redisTemplate.opsForValue().getBit(bmKey,7));

    }
}
