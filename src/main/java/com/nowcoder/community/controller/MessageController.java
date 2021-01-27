package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping(path = "/letter")

public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();

        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findUnreadLetterCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
            model.addAttribute("conversations", conversations);

        }

        //查询总的未读数量
        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("unreadCount", letterUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(path = "/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {

        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setLimit(5);

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }

        List<Integer> ids = getUnreadLetterIds(letterList);
        if (!ids.isEmpty())
            messageService.readLetters(ids, 1);

        model.addAttribute("letters", letters);
        model.addAttribute("target", getTargetUser(conversationId));
        return "/site/letter-detail";
    }

    private List<Integer> getUnreadLetterIds(List<Message> messages) {
        List<Integer> ids = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0)
                    ids.add(message.getId());
            }
        }
        return ids;
    }

    private User getTargetUser(String conversationId) {
        String ids[] = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path = "/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "用户不存在");
        }
        Message message = new Message();
        message.setContent(content);
        message.setToId(target.getId());
        message.setFromId(hostHolder.getUser().getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setCreateTime(new Date());
        messageService.sendLetter(message);

        return CommunityUtil.getJSONString(0);
    }


}
