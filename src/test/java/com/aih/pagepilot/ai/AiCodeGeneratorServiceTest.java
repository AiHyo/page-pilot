package com.aih.pagepilot.ai;

import com.aih.pagepilot.ai.model.HtmlCodeResult;
import com.aih.pagepilot.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        String userMessage = "生成todo任务清单页面";
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        System.out.println(result);
        assertTrue(result.getHtmlCode().contains("<html"));
    }

    @Test
    void generateMultiFileCode() {
        String userMessage = "生成一个番茄时钟任务管理的页面，代码尽量简短";
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        System.out.println(result);
        assertTrue(result.getHtmlCode().contains("<html"));
    }
}