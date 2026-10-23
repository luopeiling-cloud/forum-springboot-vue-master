package com.forum.util.msm.controller;

import cn.hutool.core.util.RandomUtil;
import com.forum.common.result.Result;
import com.forum.common.utils.RedisUtils;
import com.forum.common.validator.ValidatorUtils;
import com.forum.common.validator.group.DefaultGroup;
import com.forum.util.constant.UserConstant;
import com.forum.util.dto.UserDTO;
import com.forum.util.msm.service.DmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 *发送邮件的功能
 */
@RestController
@RequestMapping("/util/dm")
public class DmController {

    @Autowired
    DmService dmService;

    @Autowired
    RedisUtils redisUtils;

    /**
     * 发生邮件
     *
     * @param
     * @return
     */
    @PostMapping("sendDm")
    public Result<?> sendDm(@RequestBody UserDTO userDTO) {
        try {
            String content = RandomUtil.randomNumbers(4);
            dmService.sendDm(userDTO.getEmail(), content);
            redisUtils.set(UserConstant.CODE+userDTO.getEmail(), content, 60 * 5L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok(null);
    }
}
