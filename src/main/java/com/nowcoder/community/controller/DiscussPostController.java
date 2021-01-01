package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstance;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstance {

    @Autowired
    UserService userService;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    //尝试使用过滤器，这里本是用hostholder直接获取
    //@LoginRequired
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        if (hostHolder.getUser() == null) {
            return CommunityUtil.getJSONString(1, "您还没有登录哦");
        }

        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setUserId(hostHolder.getUser().getId());

        discussPostService.addDiscussPost(post);

        //报错的情况，将来同一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(Model model, Page page,@PathVariable("discussPostId") int discussPostId) {
        DiscussPost post=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        User user=userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //评论分页信息
        page.setLimit(5);
        //复用路径，用于拼凑分页链接，后面将来会接一个currentPage，并传入Page对象
        page.setPath("/discuss/detail/"+discussPostId);
        //本应通过postId，和commentMapper查询，但是为了效率，在数据表中冗余存储了count字段
        page.setRows(post.getCommentCount());

        /**
         * 评论：给帖子的评论
         * 回复：给评论的评论
         * commentList：评论列表
         */
        List<Comment> commentList=commentService.findCommentsByEntity(
                ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit()
        );

        //评论VO列表
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String,Object> commentVo=new HashMap<>();
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //回复列表，对评论的评论，也是属于一条评论的对象
                List<Comment> replyList=commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if (replyVoList != null) {
                    for (Comment reply : replyList) {
                        Map<String,Object> replyVo=new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target=reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys",replyVoList);

                //回复数量
                int replyCount=commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }


}
