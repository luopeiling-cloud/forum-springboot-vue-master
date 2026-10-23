package com.forum.common.exception;

import com.forum.common.result.ResultCodeEnum;
import com.forum.common.utils.MessageUtils;
import lombok.Data;

/**
 * @author xiaozhao
 */
@Data
public class ForumException extends RuntimeException{

    //异常状态码
    private Integer code;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param message
     * @param code
     */
    public ForumException(String message, Integer code) {
        super(message);
        this.code = code;
    }


    public ForumException(int code, String... params) {
        super(MessageUtils.getMessage(code, params));
        this.code = code;
    }

    public ForumException(String msg) {
        super(msg);
        this.code = ResultCodeEnum.FAIL.getCode();
    }


    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public ForumException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

}
