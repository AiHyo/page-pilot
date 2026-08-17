package com.aih.pagepilot.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用对话生成请求
 */
@Data
public class AppChatGenRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 用户消息
     */
    private String message;

    private static final long serialVersionUID = 1L;
}
