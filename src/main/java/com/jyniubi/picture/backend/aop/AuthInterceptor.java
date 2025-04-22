package com.jyniubi.picture.backend.aop;

import com.jyniubi.picture.backend.annotation.AuthCheck;
import com.jyniubi.picture.backend.constant.UserConstant;
import com.jyniubi.picture.backend.exception.BusinessException;
import com.jyniubi.picture.backend.exception.ErrorCode;
import com.jyniubi.picture.backend.model.entity.User;
import com.jyniubi.picture.backend.model.enums.UserRoleEnum;
import com.jyniubi.picture.backend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 访问权限切面类
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(final ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 注解角色
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 没有登录注解，就不需要登录，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 当前登录用户
        User userLogin = userService.getUserLogin(request);
        // 以下就是需要登录才能访问的
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userLogin.getUserRole());
        // 没有权限，拒绝
        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 没有管理员权限，拒绝访问
        // 注解要求是管理员权限，但数据库的角色不是管理员
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 放行
        return joinPoint.proceed();
    }
}
