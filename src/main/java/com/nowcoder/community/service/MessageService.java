package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit ){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int findUnreadLetterCount(int userId,String conversationId){
        return messageMapper.selectUnreadLetter(userId,conversationId);
    }

    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    public int sendLetter(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertLetter(message);
    }

    public int readLetters(List<Integer>  ids,int status){
        return messageMapper.updateLetterStatus(ids,status);
    }

    public Message findLatestNotice(int userId,String topic){
        return messageMapper.selectNewestNotice(userId,topic);
    }

    public int findNoticeCount(int userId,String topic){
        return messageMapper.selectNoticeCountByTopic(userId, topic);
    }

    public int findUnreadNoticeCount(int userId,String topic){
        return messageMapper.selectUnreadNoticeCount(userId, topic) ;
    }

    public List<Message> findNoticeList(int userId,String topic,int offset,int limit){
        return messageMapper.selectNoticeListByTopic(userId,topic,offset,limit);
    }

}
