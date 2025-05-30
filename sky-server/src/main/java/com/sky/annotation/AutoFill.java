package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yixin
 * @date 2025/5/29
 * @description 自定义注解，用于标识某个方法需要进行功能自动填充字段的处理
 */
@Target(ElementType.METHOD) //指定注解只能加在方法上面
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //通过自定义的枚举类，设置数据库操作类型，因为这些公共字段只有在insert和update的时候才需要填充，所以只设置了这俩个操作
    OperationType  value();
}
