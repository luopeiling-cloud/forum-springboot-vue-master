package com.forum.im.service.impl;

import cn.hutool.json.JSONUtil;
import com.forum.common.constant.ImConstant;
import com.forum.common.utils.RedisUtils;
import com.forum.im.entity.CountMessage;
import com.forum.common.im.Message;
import com.forum.im.factory.MessageFactory;


public class CountMessageServiceImpl implements MessageFactory {

    RedisUtils redisUtils;
    CountMessageServiceImpl(RedisUtils redisUtils){
        this.redisUtils = redisUtils;
    }

    @Override
    public void sendMessage(Message message) {
        String messageCountKey = ImConstant.MESSAGE_COUNT_KEY + message.getAcceptUid();
        CountMessage countMessage = JSONUtil.toBean(JSONUtil.toJsonStr(message.getContent()),CountMessage.class);
        countMessage.setUid(message.getAcceptUid());
        redisUtils.set(messageCountKey,JSONUtil.toJsonStr(countMessage));
    }
}
