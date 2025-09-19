package com.aih.pagepilot.core.parser;

import com.aih.pagepilot.ai.model.HtmlCodeResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * HTML 单文件代码解析器
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/9/19
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        
        // 首先尝试解析JSON格式
        if (tryParseAsJson(codeContent, result)) {
            return result;
        }
        
        // 如果不是JSON格式，则使用原来的Markdown代码块解析
        parseAsMarkdown(codeContent, result);
        return result;
    }
    
    /**
     * 尝试将内容解析为JSON格式
     */
    private boolean tryParseAsJson(String codeContent, HtmlCodeResult result) {
        try {
            // 去除可能的前后空白字符
            String trimmedContent = codeContent.trim();
            
            // 检查是否以 { 开始和 } 结束，初步判断是否为JSON
            if (!trimmedContent.startsWith("{") || !trimmedContent.endsWith("}")) {
                return false;
            }
            
            JsonNode jsonNode = objectMapper.readTree(trimmedContent);
            
            // 提取各个字段
            if (jsonNode.has("htmlCode")) {
                result.setHtmlCode(jsonNode.get("htmlCode").asText());
            }
            if (jsonNode.has("description")) {
                result.setDescription(jsonNode.get("description").asText());
            }
            
            return true;
        } catch (Exception e) {
            // 如果JSON解析失败，返回false，继续尝试Markdown解析
            return false;
        }
    }
    
    /**
     * 使用Markdown代码块格式解析
     */
    private void parseAsMarkdown(String codeContent, HtmlCodeResult result) {
        // 提取 HTML 代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
    }

    /**
     * 提取 HTML 代码内容
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}