package com.aih.pagepilot.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebScreenshotUtilsTest {

    @Test
    void blankUrlReturnsNullWithoutStartingDriver() {
        WebScreenshotUtils utils = new WebScreenshotUtils();
        Assertions.assertNull(utils.saveWebPageScreenshot(""));
        Assertions.assertNull(utils.saveWebPageScreenshot(null));
    }
}
