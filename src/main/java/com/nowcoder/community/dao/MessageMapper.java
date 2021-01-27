package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户会话列表，针对每个会话只显示一条最新消息
    List<Message> selectConversations(int userId,int offset,int limit);

    //查询当前用户会话数量
    int selectConversationCount(int userId);

    //查询某个会话消息列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话消息数量
    int selectLetterCount(String conversationId);

    //查询未读消息数量，若不带conversationId参数，则为所有未读，若带上，则为该消息未读
    int selectUnreadLetter(int userId,String conversationId);

    //插入消息
    int insertLetter(Message message);

    //更新消息状态
    int updateLetterStatus(List<Integer> ids,int status);

}
