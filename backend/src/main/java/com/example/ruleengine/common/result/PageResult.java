package com.example.ruleengine.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果类
 *
 * @author system
 * @date 2025-01-01
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页记录数
     */
    private Long pageSize;

    /**
     * 总页数
     */
    private Long totalPages;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long page, Long pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }

    /**
     * 从MyBatis Plus的Page对象转换
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(
            page.getRecords(),
            page.getTotal(),
            page.getCurrent(),
            page.getSize()
        );
    }

}
