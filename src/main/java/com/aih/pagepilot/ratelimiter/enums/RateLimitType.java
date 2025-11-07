package com.aih.pagepilot.ratelimiter.enums;


/**
 * 限流类型枚举
 *
 * @author zeng.liqiang
 * @date 2025/11/7
 */
public enum RateLimitType {
    
    /**
     * 接口级别限流
     */
    API,
    
    /**
     * 用户级别限流
     */
    USER,
    
    /**
     * IP级别限流
     */
    IP
}
