package com.forum.platform.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forum.platform.service.*;
import com.forum.xo.dao.CommentSyncDao;
import com.forum.xo.entity.*;
import org.springframework.stereotype.Service;

@Service
public class CommentSyncServiceImpl extends ServiceImpl<CommentSyncDao, CommentSync> implements CommentSyncService {
}
