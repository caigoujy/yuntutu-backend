package com.jyniubi.picture.backend.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthCheck {
    /**
     * 必须有一个角色
     */
    String mustRole() default "";
}
