package com.nowcoder.community;


import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.AlphaService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    UserMapper userMapper;

    @Test
    public  void  testSelectUser(){
        User user=userMapper.selectById(101);
        System.out.println(user);
        user=userMapper.selectByName("liubei");
        System.out.println(user);
        user=userMapper.selectByEmail("nowcoder117@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user=new User();
//        user.setId(0);
        user.setUsername("mark");
        user.setPassword("123456");
        user.setSalt("31");
        user.setEmail("tian@qq.com");
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode("0");
        user.setHeaderUrl("aaa.jpg");
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

    }

    @Test
    public void testUpdateUser(){
        userMapper.updateHeader(151,"new header");
        userMapper.updatePassword(151,"newPass");
        userMapper.updateStatus(151,1);
    }

    @Autowired
    DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectPost(){
        List<DiscussPost> postList= discussPostMapper.selectDiscussPosts(149,0,10);
        for (DiscussPost post:
                postList
             ) {
            System.out.println(post);
        }

        System.out.println(discussPostMapper.selectDiscussPostRows(0));
        System.out.println(discussPostMapper.selectDiscussPostRows(149) );
    }
}
