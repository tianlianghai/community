package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
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
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    //之所以不直接用静态页面是为了：动态显示帖子列表
    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        //方法调用前，springMVC会自动实例化Model和Page，并且会把Page对象放入Model
        //所以在Thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list=discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        if (list!=null){
            for (DiscussPost post :
                    list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                User user=userService.findUserById(post.getUserId());
                map.put("user",user );

                discussPosts.add( map);

            }
        }
        model.addAttribute("page",page);
        model.addAttribute("discussPosts",discussPosts);
        return  "/index";
    }

    //不写index自动跳到index
    @RequestMapping(path = " ")
    public String noPathToIndex(){
        return "redirect:/index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String error(){
        return "/error/500";
    }

}
