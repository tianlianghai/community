package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstance {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        org.springframework.data.domain.Page<DiscussPost> resultPage=
                elasticsearchService.searchDiscussPost(keyword,page.getCurrent()-1,page.getLimit());

        List<Map<String,Object>> posts=new ArrayList<>();
        if (resultPage!=null){
            for (DiscussPost post:resultPage){
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                map.put("user",userService.findUserById(post.getUserId()));
                map.put("like",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                posts.add(map);
            }
        }

        model.addAttribute("posts",posts);
        model.addAttribute("keyword",keyword);

        page.setPath("/search?keyword="+keyword);
        page.setRows(resultPage==null?0:(int)resultPage.getTotalElements());
        return "/site/search";
    }
}
