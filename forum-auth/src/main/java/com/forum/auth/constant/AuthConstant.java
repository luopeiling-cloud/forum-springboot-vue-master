package com.forum.auth.constant;

/**
 * @author xiaozhao
 */
public interface AuthConstant {

   long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24;

   long REFRESH_TOKEN_EXPIRATION_TIME = ACCESS_TOKEN_EXPIRATION_TIME * 2;

   String REFRESH_TOKEN_START_TIME = "refreshTokenStartTime:";

   String USER_KEY = "userKey:";

   String USER_INFO = "userInfo";

   String CODE = "code:";

   String DEFAULT_AVATAR = "/oss/default.jpg";

   String DEFAULT_COVER = "/oss/default.jpg";

   String DEFAULT_PASSWORD = "qwer1234";

   String LOGIN_FAIL = "登录失败";
}
