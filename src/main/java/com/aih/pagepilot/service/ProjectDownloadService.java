package com.aih.pagepilot.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 *
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/11/6
 */
public interface ProjectDownloadService {
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
