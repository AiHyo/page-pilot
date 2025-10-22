package com.aih.pagepilot.ai.model.message;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 流式消息基类
 * </p>
 * @author zeng.liqiang
 * @date 2025/10/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor // 封装对象必须有无参构造对象，否则解释JSON会失败
public class StreamMessage {
    private String type;
}
