package com.aih.pagepilot.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.aih.pagepilot.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 * 支持 AI 通过工具调用的方式写入文件
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool{

    @Tool("写入文件到指定路径")
    public String writeFile(@P("文件的相对路径") String relativeFilePath, @P("要写入文件的内容") String content, @ToolMemoryId Long appId) {
        try {
            // 解析目标路径
            Path targetPath = resolveTargetPath(relativeFilePath, appId);
            // 确保目录存在
            ensureDirectoryExists(targetPath.getParent());
            // 写入内容到文件
            writeContentToFile(targetPath, content);
            log.info("成功写入文件: {}", targetPath.toAbsolutePath());
            return "文件写入成功: " + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = String.format("文件写入失败: %s, 错误: %s", relativeFilePath, e.getMessage());
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 解析目标路径
     * @param relativeFilePath
     * @param appId
     * @return
     */
    private Path resolveTargetPath(String relativeFilePath, Long appId) {
        Path path = Paths.get(relativeFilePath);
        if (path.isAbsolute()) {
            return path;
        }
        String projectDirName = "vue_project_" + appId;
        Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
        return projectRoot.resolve(relativeFilePath);
    }

    /**
     * 确保目录存在
     * @param directory
     * @throws IOException
     */
    private void ensureDirectoryExists(Path directory) throws IOException {
        if (directory != null) {
            Files.createDirectories(directory);
        }
    }

    /**
     * 写入内容到文件
     * @param path
     * @param content
     * @throws IOException
     */
    private void writeContentToFile(Path path, String content) throws IOException {
        Files.write(path,
                content.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getDisplayName() {
        return "写入文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String content = arguments.getStr("content");
        return String.format("""
                        [工具调用] %s %s
                        ```%s
                        %s
                        ```
                        """, getDisplayName(), relativeFilePath, suffix, content);
    }
}
