package com.nowcoder.community;


import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    UserMapper userMapper;


    @Autowired
    SensitiveFilter sensitiveFilter;
    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    MessageMapper messageMapper;
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
        System.out.println(new Date());
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

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();

        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        System.out.println(new Date());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));

        loginTicketMapper.insertLoginTicker(loginTicket);

    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc",1);
        loginTicket=loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters(){

        int userId=111;
        String conversationId="111_131";
        //查询某个用户所有会话列表的第一条消息
        List<Message> messages=messageMapper.selectConversations(userId,0,20);
        System.out.println("用户"+userId+"的消息列表为");
        for (Message message:
             messages) {
            System.out.println(message);
        }

        printRiver();
        //查询某用户会话数量
        int count=messageMapper.selectConversationCount(111);
        System.out.println("用户 "+"的会话数量为"+count);
        printRiver();

        messages=messageMapper.selectLetters("111_112",0,10);
        for (Message message:
                messages) {
            System.out.println(message);
        }
        printRiver();

        count= messageMapper.selectLetterCount("111_112");
        System.out.println(count);
        printRiver();

        count= messageMapper.selectUnreadLetter(131,"111_131");
        System.out.println(count);
    }


    void printRiver(){
        System.out.println("********************************************");
    }
}
