package com.example.ruleengine.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作符枚举
 *
 * @author system
 * @date 2025-01-01
 */
@Getter
@AllArgsConstructor
public enum OperatorEnum {

    /**
     * 等于
     */
    EQ("EQ", "等于", "=="),

    /**
     * 不等于
     */
    NE("NE", "不等于", "!="),

    /**
     * 大于
     */
    GT("GT", "大于", ">"),

    /**
     * 大于等于
     */
    GTE("GTE", "大于等于", ">="),

    /**
     * 小于
     */
    LT("LT", "小于", "<"),

    /**
     * 小于等于
     */
    LTE("LTE", "小于等于", "<="),

    /**
     * 包含于(IN)
     */
    IN("IN", "包含于", "memberOf"),

    /**
     * 不包含于
     */
    NOT_IN("NOT_IN", "不包含于", "not memberOf"),

    /**
     * 包含(CONTAINS)
     */
    CONTAINS("CONTAINS", "包含", "contains"),

    /**
     * 不包含
     */
    NOT_CONTAINS("NOT_CONTAINS", "不包含", "not contains"),

    /**
     * 左匹配
     */
    LEFT_MATCH("LEFT_MATCH", "左匹配", "matches"),

    /**
     * 模糊匹配
     */
    FUZZY_MATCH("FUZZY_MATCH", "模糊匹配", "matches"),

    /**
     * 区间(BETWEEN)
     */
    BETWEEN("BETWEEN", "区间", ">=&&<="),

    /**
     * 为空
     */
    IS_NULL("IS_NULL", "为空", "== null"),

    /**
     * 不为空
     */
    IS_NOT_NULL("IS_NOT_NULL", "不为空", "!= null");

    /**
     * 操作符编码
     */
    private final String code;

    /**
     * 操作符名称
     */
    private final String name;

    /**
     * DRL转换符号
     */
    private final String drlSymbol;

    /**
     * 根据code获取枚举
     */
    public static OperatorEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (OperatorEnum operator : values()) {
            if (operator.getCode().equals(code)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("未知的操作符: " + code);
    }

}
