package com.example.ruleengine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则状态枚举
 *
 * @author system
 * @date 2025-01-01
 */
@Getter
@AllArgsConstructor
public enum RuleStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 草稿
     */
    DRAFT(2, "草稿");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static RuleStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (RuleStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的规则状态: " + code);
    }

}
