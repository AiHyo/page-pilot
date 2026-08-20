package com.aih.pagepilot.controller;

/**
 * <p>
 * 静态资源访问
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/9/22
 */

import com.aih.pagepilot.constant.AppConstant;
import com.aih.pagepilot.utils.ProjectPathGuard;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 静态资源访问
 */
@RestController
@RequestMapping("/static")
public class StaticResourceController {

    private static final Pattern DEPLOY_KEY = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
    private static final String HTML_CSP = "sandbox allow-scripts allow-forms allow-downloads";
    private static final String VISUAL_EDITOR_HOOK = loadVisualEditorHook();

    /**
     * 提供静态资源访问，支持目录重定向
     * 访问格式：http://localhost:8124/api/static/{deployKey}[/{fileName}]
     */
    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String deployKey,
            HttpServletRequest request) {
        try {
            if (!DEPLOY_KEY.matcher(deployKey).matches()) {
                return ResponseEntity.badRequest().build();
            }
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + deployKey).length());
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            Path file = resolveStaticFile(deployKey, resourcePath);
            if (file == null || !Files.isRegularFile(file)) {
                return ResponseEntity.notFound().build();
            }
            String filePath = file.toAbsolutePath().toString();
            if (isHtmlFile(filePath)) {
                return serveHtml(file);
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, getContentTypeWithCharset(filePath))
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Cross-Origin-Resource-Policy", "cross-origin")
                    .body(new FileSystemResource(file));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Look up generated preview first, then the deployed copy.
     */
    private Path resolveStaticFile(String deployKey, String resourcePath) {
        String relative = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        Path generated = resolveJailedFile(AppConstant.CODE_OUTPUT_ROOT_DIR, deployKey, relative);
        if (generated != null) {
            return generated;
        }
        return resolveJailedFile(AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey, relative);
    }

    private Path resolveJailedFile(String rootDir, String deployKey, String relativePath) {
        try {
            Path root = Paths.get(rootDir, deployKey);
            Path resolved = ProjectPathGuard.resolveInside(root, relativePath);
            if (Files.isRegularFile(resolved)) {
                return resolved;
            }
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return null;
    }

    private ResponseEntity<Resource> serveHtml(Path file) throws IOException {
        String html = Files.readString(file, StandardCharsets.UTF_8);
        html = injectVisualEditorHook(html);
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Content-Security-Policy", HTML_CSP)
                .header("X-Content-Type-Options", "nosniff")
                .header("Cross-Origin-Resource-Policy", "cross-origin")
                .contentLength(body.length)
                .body(new ByteArrayResource(body));
    }

    private String injectVisualEditorHook(String html) {
        if (html.contains("id=\"visual-editor-script\"") || html.contains("id='visual-editor-script'")) {
            return html;
        }
        String script = "<script id=\"visual-editor-script\">\n" + VISUAL_EDITOR_HOOK + "\n</script>\n";
        int bodyClose = html.toLowerCase(Locale.ROOT).lastIndexOf("</body>");
        if (bodyClose >= 0) {
            return html.substring(0, bodyClose) + script + html.substring(bodyClose);
        }
        return html + script;
    }

    private boolean isHtmlFile(String filePath) {
        int dot = filePath.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        return "html".equals(filePath.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "html" -> "text/html; charset=UTF-8";
            case "css" -> "text/css; charset=UTF-8";
            case "js" -> "application/javascript; charset=UTF-8";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private static String loadVisualEditorHook() {
        try (InputStream in = StaticResourceController.class.getResourceAsStream("/visual-editor-hook.js")) {
            if (in == null) {
                throw new IllegalStateException("missing visual-editor-hook.js");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
