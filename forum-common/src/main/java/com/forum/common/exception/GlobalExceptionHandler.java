package com.forum.common.exception;


import com.forum.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xiaozhao
 */ //AOP 面向切面
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) //异常处理器
    @ResponseBody  //返回json数据
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail(null);
    }

    //自定义异常处理
    @ExceptionHandler(ForumException.class)
    @ResponseBody
    public Result error(ForumException exception) {
        return Result.build(null,exception.getCode(),exception.getMessage());
    }
}
