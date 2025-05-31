package com.sky.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author yixin
 * @date 2025/5/31
 * @description 套餐已存在异常类
 */
public class SetmealAlreadyExistsException extends BaseException{
    public SetmealAlreadyExistsException() {}

    public SetmealAlreadyExistsException(String msg){
        super(msg);
    }
}
