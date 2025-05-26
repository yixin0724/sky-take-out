package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //处理用户名重复后，抛出的sql键值对冲突异常
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")){
            //提取出报错中的用户名，响应给前端，用于提示
            String[] split = message.split(" ");
            String username = split[2];
            String msg =  username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else {
            //若不是这个，则响应未知错误
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
}
