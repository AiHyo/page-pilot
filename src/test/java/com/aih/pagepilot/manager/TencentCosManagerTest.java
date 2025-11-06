package com.aih.pagepilot.manager;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 腾讯云COS服务测试类
 * @author AiHyo
 */
@SpringBootTest
public class TencentCosManagerTest {

    @Resource
    private TencentCosManager tencentCosManager;

    /**
     * 测试1: 上传简单文本文件
     * 验证基本上传功能和配置是否正确
     */
    @Test
    public void testUploadSimpleFile() throws IOException {
        // 创建临时测试文件
        File testFile = File.createTempFile("test_", ".txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("这是一个测试文件，用于验证腾讯云COS配置");
        }

        try {
            // 上传文件到 test 目录
            String fileUrl = tencentCosManager.uploadFile(testFile, "test");
            
            System.out.println("✅ 上传成功！");
            System.out.println("文件URL: " + fileUrl);
            
            // 验证URL格式
            assert fileUrl != null && fileUrl.startsWith("http");
            
        } finally {
            // 清理临时文件
            testFile.delete();
        }
    }

}
