package com.forum.platform.client;

import com.forum.common.im.Message;
import com.forum.common.result.Result;
import com.forum.platform.config.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 解决远程调用设置token
 * @author xiaozhao
 */
@FeignClient(value = "forum-im", configuration = {FeignRequestInterceptor.class})
@Component
public interface ChatClient {
    /**
     * 发送消息
     * @param message 消息实体
     * @return success
     */
    @RequestMapping("/im/chat/sendMsg")
    Result<?> sendMsg(@RequestBody Message message);
}
