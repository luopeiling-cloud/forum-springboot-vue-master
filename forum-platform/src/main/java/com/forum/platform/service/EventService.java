package com.forum.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forum.xo.entity.Event;
import com.forum.xo.vo.EventVo;
import com.forum.xo.vo.UnreadCountVo;

public interface EventService extends IService<Event> {
    void saveEvent(String fromUid, String toUid, int eventType, int targetType, String targetId, String url);
    Page<EventVo> getEventPage(long currentPage, long pageSize, String toUid, Integer eventType);
    UnreadCountVo getUnreadCount(String toUid);
    void readAll(String toUid, Integer eventType);
}
