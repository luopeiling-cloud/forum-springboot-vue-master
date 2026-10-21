package com.forum.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.forum.common.auth.AuthContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FieldMetaObjectHandler implements MetaObjectHandler {
    private final static String CREATE_DATE = "createDate";
    private final static String CREATOR = "creator";
    private final static String UPDATE_DATE = "updateDate";
    private final static String UPDATER = "updater";

    private String getUserId() {
        String uid = AuthContextHolder.getUserId();
        // 过滤掉前端可能传来的无效值
        if (uid == null || "undefined".equals(uid) || uid.isEmpty()) {
            return null;
        }
        return uid;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        Date date = new Date();
        String userId = getUserId();
        strictInsertFill(metaObject, CREATOR, String.class, userId);
        strictInsertFill(metaObject, CREATE_DATE, Date.class, date);
        strictInsertFill(metaObject, UPDATER, String.class, userId);
        strictInsertFill(metaObject, UPDATE_DATE, Date.class, date);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = getUserId();
        this.setFieldValByName(UPDATER, userId, metaObject);
        this.setFieldValByName(UPDATE_DATE, new Date(), metaObject);
    }
}