package com.forum.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.auth.AuthContextHolder;
import com.forum.common.result.Result;
import com.forum.platform.service.EventService;
import com.forum.xo.vo.EventVo;
import com.forum.xo.vo.UnreadCountVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/event")
@RestController
public class EventController {

    @Autowired
    EventService eventService;

    @RequestMapping("getEventPage/{currentPage}/{pageSize}")
    public Result<?> getEventPage(@PathVariable long currentPage, @PathVariable long pageSize, Integer eventType) {
        String uid = AuthContextHolder.getUserId();
        Page<EventVo> page = eventService.getEventPage(currentPage, pageSize, uid, eventType);
        return Result.ok(page);
    }

    @RequestMapping("getUnreadCount")
    public Result<?> getUnreadCount() {
        String uid = AuthContextHolder.getUserId();
        UnreadCountVo vo = eventService.getUnreadCount(uid);
        return Result.ok(vo);
    }

    @RequestMapping("readAll")
    public Result<?> readAll(Integer eventType) {
        String uid = AuthContextHolder.getUserId();
        eventService.readAll(uid, eventType);
        return Result.ok();
    }
}
