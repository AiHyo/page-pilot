package com.aih.pagepilot.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aih.pagepilot.ai.model.enums.CodeGenTypeEnum;
import com.aih.pagepilot.annotation.AuthCheck;
import com.aih.pagepilot.common.BaseResponse;
import com.aih.pagepilot.common.ResultUtils;
import com.aih.pagepilot.constant.AppConstant;
import com.aih.pagepilot.constant.UserConstant;
import com.aih.pagepilot.exception.BusinessException;
import com.aih.pagepilot.exception.ErrorCode;
import com.aih.pagepilot.exception.ThrowUtils;
import com.aih.pagepilot.model.dto.AppAddRequest;
import com.aih.pagepilot.model.dto.AppAdminUpdateRequest;
import com.aih.pagepilot.model.dto.AppDeployRequest;
import com.aih.pagepilot.model.dto.AppQueryRequest;
import com.aih.pagepilot.model.dto.AppUpdateRequest;
import com.aih.pagepilot.model.entity.User;
import com.aih.pagepilot.model.vo.AppVO;
import com.aih.pagepilot.service.ChatHistoryService;
import com.aih.pagepilot.service.ProjectDownloadService;
import com.aih.pagepilot.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import com.aih.pagepilot.model.entity.App;
import com.aih.pagepilot.service.AppService;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author zeng.liqiang
 * @since 2025-09-22
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    /**
     * 创建应用
     *
     * @param appAddRequest 应用创建请求
     * @param request HTTP请求
     * @return 应用id
     */
    @PostMapping
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        
        // 设置默认优先级
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        
        // 参数校验
        appService.validApp(app, true);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(appAddRequest.getInitPrompt().substring(0, Math.min(appAddRequest.getInitPrompt().length(), 12)));
        // 暂时设置为VUE生成
        app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());
        
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    /**
     * 根据 id 删除应用（用户只能删除自己的应用）
     *
     * @param id 应用ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteApp(@PathVariable Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 仅本人或管理员可删除
        if (!appService.hasPermission(oldApp, request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 先删除应用的所有对话历史
        chatHistoryService.removeByAppId(id);
        
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新应用（用户只能更新自己的应用名称）
     *
     * @param id 应用ID
     * @param appUpdateRequest 应用更新请求
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @PatchMapping("/{id}")
    public BaseResponse<Boolean> updateApp(@PathVariable Long id, @RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (id == null || id <= 0 || appUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        App app = new App();
        BeanUtil.copyProperties(appUpdateRequest, app);
        app.setId(id);
        
        // 参数校验
        appService.validApp(app, false);
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 仅本人或管理员可修改
        if (!appService.hasPermission(oldApp, request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/{id}")
    public BaseResponse<AppVO> getAppVOById(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request HTTP请求
     * @return 应用列表
     */
    @GetMapping("/list/my")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        if (appQueryRequest == null) {
            appQueryRequest = new AppQueryRequest();
        }
        
        User loginUser = userService.getLoginUser(request);
        appQueryRequest.setUserId(loginUser.getId());
        
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = Math.min(appQueryRequest.getPageSize(), 20); // 限制每页最多20个
        
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));
        
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @GetMapping("/list/featured")
    public BaseResponse<Page<AppVO>> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            appQueryRequest = new AppQueryRequest();
        }
        
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = Math.min(appQueryRequest.getPageSize(), 20); // 限制每页最多20个
        
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getFeaturedQueryWrapper(appQueryRequest));
        
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    // region 管理员接口

    /**
     * 根据 id 删除应用（管理员）
     *
     * @param id 应用ID
     * @return 是否删除成功
     */
    @DeleteMapping("/admin/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        // 先删除应用的所有对话历史
        chatHistoryService.removeByAppId(id);
        
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新应用（管理员）
     *
     * @param id 应用ID
     * @param appAdminUpdateRequest 应用更新请求
     * @return 是否更新成功
     */
    @PutMapping("/admin/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@PathVariable Long id, @RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (id == null || id <= 0 || appAdminUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        app.setId(id);
        
        // 参数校验
        appService.validApp(app, false);
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取应用详情（管理员）
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/admin/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppById(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(app);
    }

    /**
     * 分页获取应用列表（管理员）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageAdmin(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            appQueryRequest = new AppQueryRequest();
        }
        
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));
        
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    // endregion

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        // 转换为 ServerSentEvent 格式
        return contentFlux
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("data", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder().data(jsonData).build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }


    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 权限校验：只有应用创建者可以下载代码
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }
        // 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        // 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }


}
