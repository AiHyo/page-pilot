package com.aih.pagepilot.core.parser;


import com.aih.pagepilot.ai.model.enums.CodeGenTypeEnum;
import com.aih.pagepilot.exception.BusinessException;
import com.aih.pagepilot.exception.ErrorCode;

/**
 * <p>
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/9/19
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     *
     * @param codeContent     代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        switch (codeGenTypeEnum) {
            case HTML:
                return htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE:
                return multiFileCodeParser.parseCode(codeContent);
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
    }
}
