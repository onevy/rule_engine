package com.example.ruleengine.common.constant;

/**
 * 通用常量
 *
 * @author system
 * @date 2025-01-01
 */
public interface CommonConstant {

    /**
     * 成功响应码
     */
    int SUCCESS_CODE = 200;

    /**
     * 失败响应码
     */
    int FAIL_CODE = 500;

    /**
     * 未授权响应码
     */
    int UNAUTHORIZED_CODE = 401;

    /**
     * 无权限响应码
     */
    int FORBIDDEN_CODE = 403;

    /**
     * 资源不存在响应码
     */
    int NOT_FOUND_CODE = 404;

    /**
     * 默认分页大小
     */
    long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 最大分页大小
     */
    long MAX_PAGE_SIZE = 100L;

    /**
     * 逻辑删除标记 - 已删除
     */
    int DELETED = 1;

    /**
     * 逻辑删除标记 - 未删除
     */
    int NOT_DELETED = 0;

    /**
     * 启用状态
     */
    int STATUS_ENABLED = 1;

    /**
     * 禁用状态
     */
    int STATUS_DISABLED = 0;

}
