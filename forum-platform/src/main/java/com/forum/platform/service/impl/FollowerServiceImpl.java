package com.forum.platform.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.common.auth.AuthContextHolder;
import com.forum.platform.im.ChatUtils;
import com.forum.platform.service.EventService;
import com.forum.platform.service.FollowerService;
import com.forum.platform.service.LikeOrCollectionService;
import com.forum.platform.service.NoteService;
import com.forum.platform.service.UserService;
import com.forum.platform.vo.TrendVo;
import com.forum.xo.dao.FollowerDao;
import com.forum.xo.entity.Follower;
import com.forum.xo.entity.LikeOrCollection;
import com.forum.xo.entity.Note;
import com.forum.xo.entity.User;
import com.forum.xo.vo.FollowerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowerServiceImpl extends ServiceImpl<FollowerDao, Follower> implements FollowerService {

    @Autowired
    NoteService noteService;

    @Autowired
    UserService userService;

    @Autowired
    LikeOrCollectionService likeOrCollectionService;

    @Autowired
    ChatUtils chatUtils;

    @Autowired
    EventService eventService;

    @Override
    public Page<TrendVo> getFollowTrendPage(long currentPage, long pageSize) {
        Page<TrendVo> page = new Page<>();
        // 得到当前用户所有关注的用户
        String currentUid = AuthContextHolder.getUserId();
        List<Follower> followers = this.list(new QueryWrapper<Follower>().eq("uid", currentUid));
        List<String> fids = followers.stream().map(Follower::getFid).collect(Collectors.toList());
        fids.add(currentUid);
        Page<Note> notePage =  noteService.page(new Page<>((int)currentPage,(int)pageSize),new QueryWrapper<Note>().in("uid", fids).orderByDesc("update_date"));
        List<Note> notes = notePage.getRecords();
        List<TrendVo> trendVos = new ArrayList<>();
        if (!notes.isEmpty()){
            //得到所有用户的图片
            List<String> ids = notes.stream().map(Note::getUid).collect(Collectors.toList());
            List<User> users = userService.listByIds(ids);
            HashMap<String,User> userMap = new HashMap<>();
            users.forEach(item-> userMap.put(item.getId(),item));
            // 是否点赞
            List<LikeOrCollection> likeOrCollections = likeOrCollectionService.list(new QueryWrapper<LikeOrCollection>().eq("uid", currentUid).eq("type",1));
            List<String> likeOrCollectionIds = likeOrCollections.stream().map(LikeOrCollection::getLikeOrCollectionId).collect(Collectors.toList());

            for (Note note : notes) {
                TrendVo trendVo = new TrendVo();
                User user = userMap.get(note.getUid());
                trendVo.setUid(user.getId())
                        .setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setNid(note.getId())
                        .setTime(note.getUpdateDate().getTime())
                        .setContent(note.getContent())
                        .setCommentCount(note.getCommentCount())
                        .setLikeCount(note.getLikeCount())
                        .setIsLike(likeOrCollectionIds.contains(note.getId()))
                        .setIsLoading(false);
                String urls = note.getUrls();
                List<String> imgList = JSONUtil.toList(urls, String.class);
                if(imgList.size()>4){
                    List<String> subList = imgList.subList(0, 4);
                    trendVo.setImgUrls(subList);
                }else {
                    trendVo.setImgUrls(imgList);
                }
                trendVos.add(trendVo);
            }
        }
        long total = notePage.getTotal();
        page.setTotal(total);
        page.setRecords(trendVos);
        return page;
    }

    @Override
    public Page<FollowerVo> getFriendPage(long currentPage, long pageSize, Integer type) {
        Page<FollowerVo> result = new Page<>();
        String currentUid = AuthContextHolder.getUserId();

        // type=1: 我关注的人（uid=我, 取fid）
        // type=2: 关注我的人/粉丝（fid=我, 取uid）
        QueryWrapper<Follower> wrapper = new QueryWrapper<>();
        if (type != null && type == 2) {
            wrapper.eq("fid", currentUid);
        } else {
            wrapper.eq("uid", currentUid);
        }
        wrapper.orderByDesc("create_date");

        Page<Follower> followerPage = this.page(new Page<>((int) currentPage, (int) pageSize), wrapper);
        List<Follower> records = followerPage.getRecords();
        long total = followerPage.getTotal();

        // 获取目标用户 ID 集合
        Set<String> targetUids = records.stream()
                .map(f -> (type != null && type == 2) ? f.getUid() : f.getFid())
                .collect(Collectors.toSet());

        // 查询用户信息
        Map<String, User> userMap = userService.listByIds(targetUids).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 当前用户的所有关注（用于 isFollow 标记）
        List<Follower> myFollows = this.list(new QueryWrapper<Follower>().eq("uid", currentUid));
        Set<String> myFollowIds = myFollows.stream().map(Follower::getFid).collect(Collectors.toSet());

        List<FollowerVo> voList = new ArrayList<>();
        for (Follower f : records) {
            String targetUid = (type != null && type == 2) ? f.getUid() : f.getFid();
            User user = userMap.get(targetUid);
            if (user == null) continue;
            FollowerVo vo = new FollowerVo();
            vo.setUid(user.getId())
              .setUsername(user.getUsername())
              .setAvatar(user.getAvatar())
              .setTime(f.getCreateDate().getTime())
              .setIsFollow(myFollowIds.contains(targetUid));
            voList.add(vo);
        }

        result.setRecords(voList);
        result.setTotal(total);
        return result;
    }

    // TODO 需要优化
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void followById(String followerId) {
        Follower follower = new Follower();
        String userId = AuthContextHolder.getUserId();
        follower.setUid(userId);
        follower.setFid(followerId);
        // 得到当前用户
        User currentUser = userService.getById(userId);
        User followerUser = userService.getById(followerId);
        long curFollowerCount = currentUser.getFollowerCount() == null ? 0L : currentUser.getFollowerCount();
        long curFanCount = followerUser.getFanCount() == null ? 0L : followerUser.getFanCount();
        if(isFollow(followerId)){
            currentUser.setFollowerCount(curFollowerCount - 1);
            followerUser.setFanCount(curFanCount - 1);
            this.remove(new QueryWrapper<Follower>().eq("uid", userId).eq("fid", followerId));
        }else{
            currentUser.setFollowerCount(curFollowerCount + 1);
            followerUser.setFanCount(curFanCount + 1);
            this.save(follower);
            eventService.saveEvent(userId, followerId, 4, 1, userId,
                    "/pages/profile/index?userId=" + userId);
        }
        userService.updateById(currentUser);
        userService.updateById(followerUser);
    }

    @Override
    public boolean isFollow(String followerId) {
        String userId = AuthContextHolder.getUserId();
        long count = this.count(new QueryWrapper<Follower>().eq("uid", userId).eq("fid", followerId));
        return count>0;
    }

    @Override
    public Page<FollowerVo> getNoticeFollower(long currentPage, long pageSize) {
        Page<FollowerVo> result = new Page<>();
        String userId = AuthContextHolder.getUserId();

        Page<Follower> followerPage = this.page(new Page<>((int) currentPage, (int) pageSize), new QueryWrapper<Follower>().eq("fid", userId).ne("uid",userId).orderByDesc("create_date"));
        List<Follower> followerList = followerPage.getRecords();
        long total = followerPage.getTotal();

        Set<String> uids = followerList.stream().map(Follower::getUid).collect(Collectors.toSet());
        Map<String, User> userMap = userService.listByIds(uids).stream().collect(Collectors.toMap(User::getId, user -> user));

        // 得到当前用户的所有关注
        List<Follower> followers = this.list(new QueryWrapper<Follower>().eq("uid", userId));
        Set<String> followerSet = followers.stream().map(Follower::getFid).collect(Collectors.toSet());

        List<FollowerVo> followerVoList = new ArrayList<>();
        followerList.forEach(item->{
            FollowerVo followerVo = new FollowerVo();
            User user = userMap.get(item.getUid());
            followerVo.setUid(user.getId())
                  .setUsername(user.getUsername())
                  .setAvatar(user.getAvatar())
                  .setTime(item.getCreateDate().getTime())
                  .setIsFollow(followerSet.contains(item.getUid()));
            followerVoList.add(followerVo);
        });
        result.setRecords(followerVoList);
        result.setTotal(total);
        return result;
    }

}
