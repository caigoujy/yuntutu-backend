package com.jyniubi.picture.backend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 用户角色
 */
@Getter
public enum UserRoleEnum {
    USER("用户","user"),
    ADMIN("管理员","admin")
    ;
    private final String text;

    private final String value;

    UserRoleEnum(String text, String value){
        this.text = text;
        this.value = value;
    }

    /**
     *  根据value获取枚举
     * @param value
     * @return
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.hasEmpty(value)){
            return null;
        }

        for (UserRoleEnum e : UserRoleEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
