package com.aih.pagepilot.common;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.Set;

/**
 * Allow-list for client {@code sortField} values. Never pass a raw client string to {@code orderBy}.
 */
public final class SortFields {

    private SortFields() {
    }

    public static final Set<String> APP = Set.of(
            "id", "createTime", "updateTime", "priority", "appName", "userId"
    );

    public static final Set<String> USER = Set.of(
            "id", "createTime", "updateTime", "userAccount", "userName", "userRole"
    );

    public static final Set<String> CHAT_HISTORY = Set.of(
            "id", "createTime", "updateTime"
    );

    /**
     * @return the column name if it is allow-listed, otherwise {@code null}
     */
    public static String resolve(String sortField, Set<String> allowed) {
        if (StrUtil.isBlank(sortField) || allowed == null) {
            return null;
        }
        String field = sortField.trim();
        return allowed.contains(field) ? field : null;
    }

    /**
     * Apply {@code orderBy} only when {@code sortField} is allow-listed.
     */
    public static void apply(QueryWrapper queryWrapper, String sortField, String sortOrder, Set<String> allowed) {
        String column = resolve(sortField, allowed);
        if (column != null) {
            queryWrapper.orderBy(column, "ascend".equals(sortOrder));
        }
    }
}
