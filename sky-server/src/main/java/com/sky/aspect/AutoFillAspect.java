package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @author yixin
 * @date 2025/5/30
 * @description 自定义的切面类，实现自动填充公共字段机制
 */
@Aspect
@Component  // 表明该类是一个组件，交给spring来管理
@Slf4j
public class AutoFillAspect {
    //切面就是通知加上切入点，通知就是做增强的那一部分
    /**
     * 切入点
     * 切面表达式拦截的是：返回值为*，com.sky.mapper包下的所有类的所有方法的匹配所有参数类型，并且必须加上了@AutoFill注解的方法
     * 根据要加强的内容可知，需要选择的通知类型为前置通知
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..))  && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }

    /**
     * 设置前置通知，在通知中进行公共字段的赋值操作
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的自动填充...");
        //写代码前，先看看update和insert所要处理的公共字段是否完全一致，如果不一样，那就必须要分开处理
        //使用反射获取当前被拦截的方法的数据库操作类型，
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//先获取方法签名对象获取
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取到当前被拦截的方法的注解对象
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //获取到当前被拦截的方法的参数-即实体对象，根据参数为相应的公共字段属性赋值
        Object[] args = joinPoint.getArgs();//获取方法的所有参数
        if (args == null || args.length == 0){  //如果没有参数，直接返回
            return;
        }
        Object entity = args[0];//把实体对象放到第一个，直接取第一个，因为返回值是不确定的，用object接收

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的操作类型，为对应的属性使用反射来赋值
        if (operationType == OperationType.INSERT){
            //如果是插入操作，为插入时间、更新时间、创建人、更新人使用反射获取实体类对应的set方法进行赋值
            //有直接要写的字符串尽量换成常量类
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射来为对应的属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE){
            //如果是更新操作，为更新时间、更新人进行赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射来为对应的属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
