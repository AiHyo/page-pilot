package com.aih.pagepilot.ai;

import com.aih.pagepilot.ai.model.HtmlCodeResult;
import com.aih.pagepilot.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceWrapperTest {

    @Resource
    private AiCodeGeneratorServiceWrapper aiCodeGeneratorServiceWrapper;

    @Test
    void generateMultiFileCode() {
        String userMessage = "生成一个番茄时钟任务管理的页面";
        MultiFileCodeResult result = aiCodeGeneratorServiceWrapper.generateMultiFileCode(userMessage);
        System.out.println(result);
        assertTrue(result.getHtmlCode().contains("<html"));
    }
}