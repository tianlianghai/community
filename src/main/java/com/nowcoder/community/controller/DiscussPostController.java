package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    //尝试使用过滤器，这里本是用hostholder直接获取
    //@LoginRequired
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        if (hostHolder.getUser()==null){
            return CommunityUtil.getJSONString(1,"您还没有登录哦");
        }

        DiscussPost post=new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setUserId(hostHolder.getUser().getId());

        discussPostService.addDiscussPost(post);

        //报错的情况，将来同一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }
}
