package org.example.qyuanmanage.config;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.qyuancommon.Result;
import org.example.qyuanmanage.Feign.UserAuthFeign;
import org.example.qyuanmanage.entity.VIPState;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Aspect
@Component
@Slf4j
public class AuthAspect {

    @Resource
    UserAuthFeign userAuthFeign;

    @Pointcut("execution(* org.example.qyuanmanage.controller..*(..)) && !execution(* org.example.qyuanmanage.controller.ReportController.createReport(..))")
    public void RootCut(){}

    @Before("RootCut()")
    public void rootAuth(JoinPoint joinPoint) {

        // 1️⃣ 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 2️⃣ 获取参数名 & 参数值
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // 3️⃣ 查找名为 user_id 的参数
        for (int i = 0; i < paramNames.length; i++) {
            if ("user_id".equals(paramNames[i]) || "userId".equals(paramNames[i])) {
                Integer userId =(Integer) args[i];
                log.info("user_id = "+ userId);

                Result<Object> result=userAuthFeign.authRoot(userId);
                LinkedHashMap<String,Object> jsonObject= (LinkedHashMap<String, Object>) result.getData();
                Integer permission_level= (Integer) jsonObject.get("permission_level");
                if(permission_level!=3){
                    throw new RuntimeException();
                }
                break;
            }
        }
    }
}
