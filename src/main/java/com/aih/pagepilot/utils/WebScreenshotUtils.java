package com.aih.pagepilot.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.pagepilot.exception.BusinessException;
import com.aih.pagepilot.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class WebScreenshotUtils {

    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;
    private static final long READY_STATE_FALLBACK_MS = 500L;

    private WebDriver webDriver;

    @PreDestroy
    public void destroy() {
        synchronized (this) {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.warn("Failed to quit Chrome driver", e);
                } finally {
                    webDriver = null;
                }
            }
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            options.addArguments("--disable-extensions");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存图片到文件
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     */
    private static void compressImage(String sourceImagePath, String targetImagePath) {
        final float COMPRESS_RATIO = 0.3f;
        try {
            ImgUtil.compress(FileUtil.file(sourceImagePath), FileUtil.file(targetImagePath), COMPRESS_RATIO);
        } catch (Exception e) {
            log.error("压缩图片失败: {}", sourceImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * Wait for document.readyState === complete. If that never happens, pause at most 500ms.
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
            try {
                Thread.sleep(READY_STATE_FALLBACK_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 生成网页截图
     *
     * @param webUrl 网页URL
     * @return 压缩后的截图文件路径，失败返回null
     */
    public String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }
        synchronized (this) {
            try {
                WebDriver driver = getDriver();
                String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                        + File.separator + UUID.randomUUID().toString().substring(0, 8);
                FileUtil.mkdir(rootPath);
                final String IMAGE_SUFFIX = ".png";
                String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
                driver.get(webUrl);
                waitForPageLoad(driver);
                byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                saveImage(screenshotBytes, imageSavePath);
                log.info("原始截图保存成功: {}", imageSavePath);
                final String COMPRESSION_SUFFIX = "_compressed.jpg";
                String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
                compressImage(imageSavePath, compressedImagePath);
                log.info("压缩图片保存成功: {}", compressedImagePath);
                FileUtil.del(imageSavePath);
                return compressedImagePath;
            } catch (Exception e) {
                log.error("网页截图失败: {}", webUrl, e);
                resetDriver();
                return null;
            }
        }
    }

    private WebDriver getDriver() {
        if (webDriver == null) {
            webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        return webDriver;
    }

    private void resetDriver() {
        if (webDriver == null) {
            return;
        }
        try {
            webDriver.quit();
        } catch (Exception e) {
            log.warn("Failed to quit Chrome driver after error", e);
        } finally {
            webDriver = null;
        }
    }
}
