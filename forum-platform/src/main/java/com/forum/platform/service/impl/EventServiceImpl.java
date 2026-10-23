package com.forum.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.common.utils.ConvertUtils;
import com.forum.platform.service.*;
import com.forum.xo.dao.EventDao;
import com.forum.xo.entity.Comment;
import com.forum.xo.entity.CommentSync;
import com.forum.xo.entity.Event;
import com.forum.xo.entity.Note;
import com.forum.xo.entity.User;
import com.forum.xo.vo.EventVo;
import com.forum.xo.vo.UnreadCountVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl extends ServiceImpl<EventDao, Event> implements EventService {

    @Autowired
    UserService userService;
    @Autowired
    NoteService noteService;
    @Autowired
    CommentService commentService;
    @Autowired
    CommentSyncService commentSyncService;

    @Override
    public void saveEvent(String fromUid, String toUid, int eventType, int targetType, String targetId, String url) {
        if (fromUid.equals(toUid)) return; // 不通知自己
        Event event = new Event();
        event.setFromUid(fromUid);
        event.setToUid(toUid);
        event.setEventType(eventType);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setReadStatus(0);
        event.setUrl(url);
        this.save(event);
    }

    @Override
    public Page<EventVo> getEventPage(long currentPage, long pageSize, String toUid, Integer eventType) {
        Page<EventVo> result = new Page<>();

        QueryWrapper<Event> wrapper = new QueryWrapper<Event>().eq("to_uid", toUid);
        if (eventType != null && eventType > 0) {
            if (eventType == 1 || eventType == 3) {
                wrapper.in("event_type", 1, 3); // 赞+收藏合并查
            } else {
                wrapper.eq("event_type", eventType);
            }
        }
        wrapper.orderByDesc("create_date");

        Page<Event> eventPage = this.page(new Page<>((int) currentPage, (int) pageSize), wrapper);
        List<Event> events = eventPage.getRecords();

        // 查询所有相关用户
        Set<String> uids = new HashSet<>();
        for (Event e : events) uids.add(e.getFromUid());
        Map<String, User> userMap = userService.listByIds(uids).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 查询笔记（targetType=1）
        Set<String> nids = events.stream().filter(e -> e.getTargetType() == 1)
                .map(Event::getTargetId).collect(Collectors.toSet());
        Map<String, Note> noteMap = nids.isEmpty() ? new HashMap<>()
                : noteService.listByIds(nids).stream().collect(Collectors.toMap(Note::getId, n -> n));

        // 查询评论（targetType=2），同时查 t_comment 和 t_comment_sync
        Set<String> cids = events.stream().filter(e -> e.getTargetType() == 2)
                .map(Event::getTargetId).collect(Collectors.toSet());
        Map<String, Comment> commentMap = new HashMap<>();
        if (!cids.isEmpty()) {
            commentMap = commentService.listByIds(cids).stream()
                    .collect(Collectors.toMap(Comment::getId, c -> c));
            // 补充查 comment_sync 表
            List<CommentSync> syncList = commentSyncService.listByIds(cids);
            for (CommentSync cs : syncList) {
                if (!commentMap.containsKey(cs.getId())) {
                    Comment c = ConvertUtils.sourceToTarget(cs, Comment.class);
                    commentMap.put(c.getId(), c);
                }
            }
        }

        List<EventVo> voList = new ArrayList<>();
        for (Event e : events) {
            EventVo vo = new EventVo();
            User fromUser = userMap.get(e.getFromUid());
            if (fromUser == null) continue;
            vo.setId(e.getId())
               .setFromUid(e.getFromUid())
               .setFromUsername(fromUser.getUsername())
               .setFromAvatar(fromUser.getAvatar())
               .setEventType(e.getEventType())
               .setTargetType(e.getTargetType())
               .setTargetId(e.getTargetId())
               .setReadStatus(e.getReadStatus())
               .setTime(e.getCreateDate().getTime())
               .setUrl(e.getUrl());

            if (e.getTargetType() == 1) {
                Note note = noteMap.get(e.getTargetId());
                if (note != null) {
                    vo.setNoteCover(note.getNoteCover());
                    vo.setNoteTitle(note.getTitle());
                    vo.setNid(note.getId());
                }
            } else if (e.getTargetType() == 2) {
                Comment comment = commentMap.get(e.getTargetId());
                if (comment != null) {
                    vo.setContent(comment.getContent());
                    vo.setNid(comment.getNid());
                }
            }
            voList.add(vo);
        }

        result.setRecords(voList);
        result.setTotal(eventPage.getTotal());
        return result;
    }

    @Override
    public UnreadCountVo getUnreadCount(String toUid) {
        UnreadCountVo vo = new UnreadCountVo();
        vo.setLikeCount(countUnread(toUid, 1));
        vo.setCommentCount(countUnread(toUid, 2));
        vo.setCollectionCount(countUnread(toUid, 3));
        vo.setFollowCount(countUnread(toUid, 4));
        return vo;
    }

    private Long countUnread(String toUid, int eventType) {
        return this.count(new QueryWrapper<Event>()
                .eq("to_uid", toUid).eq("event_type", eventType).eq("read_status", 0));
    }

    @Override
    public void readAll(String toUid, Integer eventType) {
        QueryWrapper<Event> wrapper = new QueryWrapper<Event>().eq("to_uid", toUid);
        if (eventType != null && eventType > 0) {
            wrapper.eq("event_type", eventType);
        }
        Event update = new Event();
        update.setReadStatus(1);
        this.update(update, wrapper);
    }
}
