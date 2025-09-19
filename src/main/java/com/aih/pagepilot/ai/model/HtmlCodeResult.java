package com.aih.pagepilot.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * <p>
 * 单文件HTML 生成结果封装类
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/9/19
 */
@Data
@Description("单文件HTML的代码生成结果")
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成的代码描述")
    private String description;
}

