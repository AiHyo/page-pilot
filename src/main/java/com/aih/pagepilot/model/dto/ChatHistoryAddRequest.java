package com.aih.pagepilot.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 对话历史创建请求
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 消息类型：user/ai
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 应用id
     */
    @NotNull(message = "应用id不能为空")
    private Long appId;

    private static final long serialVersionUID = 1L;
}
