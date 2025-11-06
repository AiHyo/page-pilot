package com.aih.pagepilot.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://github.com/AiHyo";
        String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }

}