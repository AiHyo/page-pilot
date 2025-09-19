package com.aih.pagepilot.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * <p>
 * 多文件html+css+js 生成结果封装类
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/9/19
 */
@Data
@Description("多文件html+css+js的代码生成结果")
public class MultiFileCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("CSS代码")
    private String cssCode;

    @Description("JS代码")
    private String jsCode;

    @Description("生成的代码描述")
    private String description;
}
