package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Random;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public  AlphaService(){
       // System.out.println("实例化AlphaService");
    }
    @PostConstruct
    public  void init(){
        //System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy(){
        //System.out.println("销毁AlphaService");
    }

    public String find(){
       return  alphaDao.select();
    }


    /**
     * 测试事务
     * 注解式事务，这种更简单，但是对于复杂的数据库操作，如果仅仅其中几个操作需要事务，这样就会影响性能，最好用下方的编程式事务
     * 若A服务调用B服务，有以下情况
     * REQUIRED 若A有事务，则只使用A的事务，若A无事务，则新建并执行自己的事务
     * REQUIRES_NEW  ：创建一个新事物，并暂停当前事务
     * NESTED： 嵌套在当前事务（或者叫外部事务，这样更见名知意，Spring官方不这么叫）中执行
     */

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1(){
        User user=new User();
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setUsername("lisi11");
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("新人报道with事务");
        post.setContent("我是新人，哈哈");
        post.setCreateTime(new Date());

        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("hhh");
        return "yy";
    }


    /**
     * 编程式事务，适用于仅对其中几个操作声明事务
     * 在这之前需要注入TransactionTemplate，这是Spring自动创建并且装配到容器里的
     */

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                User user=new User();
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
                user.setType(0);
                user.setUsername("wangwu1");
                user.setStatus(0);
                user.setActivationCode(CommunityUtil.generateUUID());
                user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
                user.setCreateTime(new Date());

                userMapper.insertUser(user);

                DiscussPost post=new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("我是新人，哈哈");
                post.setCreateTime(new Date());

                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("hhh");

                return "OK";
            }
        });
    }
}
