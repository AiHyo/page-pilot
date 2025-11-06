package com.aih.pagepilot.service;

import java.io.IOException;

/**
 * 截图服务
 *
 * @author zeng.liqiang
 * @date 2025/11/6
 */
public interface ScreenshotService {
    String generateAndUploadScreenshot(String webUrl) throws IOException;
}
