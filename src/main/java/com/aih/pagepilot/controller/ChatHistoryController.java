package com.aih.pagepilot.controller;

import com.aih.pagepilot.annotation.AuthCheck;
import com.aih.pagepilot.common.BaseResponse;
import com.aih.pagepilot.common.ResultUtils;
import com.aih.pagepilot.constant.UserConstant;
import com.aih.pagepilot.exception.BusinessException;
import com.aih.pagepilot.exception.ErrorCode;
import com.aih.pagepilot.exception.ThrowUtils;
import com.aih.pagepilot.model.dto.ChatHistoryAddRequest;
import com.aih.pagepilot.model.dto.ChatHistoryQueryRequest;
import com.aih.pagepilot.model.entity.ChatHistory;
import com.aih.pagepilot.model.entity.User;
import com.aih.pagepilot.model.vo.ChatHistoryVO;
import com.aih.pagepilot.service.ChatHistoryService;
import com.aih.pagepilot.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author zeng.liqiang
 * @since 2025-09-25
 */
@RestController
@RequestMapping("/api/chat-history")
@Slf4j
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 创建对话历史
     *
     * @param chatHistoryAddRequest 创建请求
     * @param request HTTP请求
     * @return 创建的对话历史ID
     */
    @PostMapping
    public BaseResponse<Long> addChatHistory(@RequestBody @Valid ChatHistoryAddRequest chatHistoryAddRequest,
                                             HttpServletRequest request) {
        if (chatHistoryAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        
        // 校验用户是否有权限操作该应用的对话历史
        if (!chatHistoryService.hasPermission(chatHistoryAddRequest.getAppId(), request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setMessage(chatHistoryAddRequest.getMessage());
        chatHistory.setMessageType(chatHistoryAddRequest.getMessageType());
        chatHistory.setAppId(chatHistoryAddRequest.getAppId());
        chatHistory.setUserId(loginUser.getId());
        chatHistory.setCreateTime(LocalDateTime.now());
        chatHistory.setUpdateTime(LocalDateTime.now());

        chatHistoryService.validChatHistory(chatHistory, true);
        boolean result = chatHistoryService.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(chatHistory.getId());
    }

    /**
     * 删除对话历史
     *
     * @param id 对话历史ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteChatHistory(@PathVariable Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ChatHistory oldChatHistory = chatHistoryService.getById(id);
        ThrowUtils.throwIf(oldChatHistory == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chatHistoryService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) java.time.LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取应用的最新对话历史（用于页面初始化）
     *
     * @param appId 应用ID
     * @param request HTTP请求
     * @return 最新的对话历史列表
     */
    @GetMapping("/app/{appId}/latest")
    public BaseResponse<List<ChatHistoryVO>> getLatestChatHistory(@PathVariable Long appId,
                                                                  @RequestParam(defaultValue = "10") Integer limit,
                                                                  HttpServletRequest request) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 校验用户是否有权限查看该应用的对话历史
        if (!chatHistoryService.hasPermission(appId, request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 限制每次最多获取50条记录
        if (limit > 50) {
            limit = 50;
        }

        List<ChatHistory> chatHistoryList = chatHistoryService.getLatestChatHistory(appId, limit);
        List<ChatHistoryVO> chatHistoryVOList = chatHistoryService.getChatHistoryVOList(chatHistoryList);
        return ResultUtils.success(chatHistoryVOList);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 根据应用ID删除所有对话历史（应用删除时调用）
     *
     * @param appId 应用ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @DeleteMapping("/app/{appId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteChatHistoryByAppId(@PathVariable Long appId, HttpServletRequest request) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = chatHistoryService.removeByAppId(appId);
        return ResultUtils.success(result);
    }
}
