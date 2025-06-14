//package com.sky.aspect;
//
//import com.sky.annotation.Log;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.lang.reflect.Method;
//import java.util.Date;
//
///**
// * @author yixin
// * @date 2025/6/14
// * @description 日志切面类
// */
//@Component
//@Aspect   //切面类
//public class LogAspect {
//    @Pointcut("@annotation(com.sky.annotation.Log)")
//    private void pointcut() {}
//
//    @Pointcut("execution(* com.sky.service.*.*(..))")
//    public void pointcut2(){}
//
//    @Around("pointcut2()")
//    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
//        //获取用户名
//        //需要通过解析seesion或token获取
//
//        //获取被增强类和方法的信息
//        Signature signature = joinPoint.getSignature();
//        MethodSignature methodSignature = (MethodSignature) signature;
//        //获取被增强的方法对象
//        Method method = methodSignature.getMethod();
//        //从方法中解析注解
//        if(method != null){
//            Log logAnnotation = method.getAnnotation(Log.class);
//            System.out.println(logAnnotation.name());
//        }
//        //方法名字
//        String name = method.getName();
//        System.out.println(name);
//
//        //通过工具类获取Request对象
//        RequestAttributes reqa = RequestContextHolder.getRequestAttributes();
//        ServletRequestAttributes sra = (ServletRequestAttributes)reqa;
//        HttpServletRequest request = sra.getRequest();
//        //访问的url
//        String url = request.getRequestURI().toString();
//        System.out.println(url);
//        //请求方式
//        String methodName = request.getMethod();
//        System.out.println(methodName);
//
//        //登录IP
//        String ipAddr = getIpAddr(request);
//        System.out.println(ipAddr);
//
//        //操作时间
//        System.out.println(new Date());
//
//        //保存到数据库（操作日志）
//        //....
//
//        return joinPoint.proceed();
//    }
//
//    /**
//     * 获取ip地址
//     * @param request
//     * @return
//     */
//    public String getIpAddr(HttpServletRequest request){
//        String ip = request.getHeader("x-forwarded-for");
//        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
//            ip = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
//            ip = request.getRemoteAddr();
//        }
//
//        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
//    }
//
//}
