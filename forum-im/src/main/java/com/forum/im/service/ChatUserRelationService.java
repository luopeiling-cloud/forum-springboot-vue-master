package com.forum.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forum.common.im.Message;
import com.forum.xo.entity.ChatUserRelation;

/**
 * @author xiaozhao
 */
public interface ChatUserRelationService extends IService<ChatUserRelation> {
     /**
      * 发送消息
      *
      * @param message 消息实体
      */
     void sendMessage(Message message);
}
