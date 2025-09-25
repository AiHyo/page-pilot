package com.aih.pagepilot.service;

import com.aih.pagepilot.model.dto.ChatHistoryQueryRequest;
import com.aih.pagepilot.model.entity.ChatHistory;
import com.aih.pagepilot.model.entity.User;
import com.aih.pagepilot.model.vo.ChatHistoryVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author zeng.liqiang
 * @since 2025-09-25
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 校验对话历史
     *
     * @param chatHistory 对话历史信息
     * @param add 是否为创建校验
     */
    void validChatHistory(ChatHistory chatHistory, boolean add);

    /**
     * 获取脱敏后的对话历史信息
     *
     * @param chatHistory 对话历史信息
     * @return 脱敏后的对话历史信息
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 获取脱敏后的对话历史信息列表
     *
     * @param chatHistoryList 对话历史列表
     * @return 脱敏后的对话历史信息列表
     */
    List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList);

    /**
     * 根据查询条件构造数据查询参数
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 校验用户是否有权限查看对话历史
     *
     * @param appId 应用ID
     * @param request HTTP请求
     * @return 是否有权限
     */
    boolean hasPermission(Long appId, HttpServletRequest request);

    /**
     * 保存用户消息
     *
     * @param appId 应用ID
     * @param message 消息内容
     * @param loginUser 当前用户
     * @return 保存的对话历史
     */
    ChatHistory saveUserMessage(Long appId, String message, User loginUser);

    /**
     * 保存AI消息
     *
     * @param appId 应用ID
     * @param message 消息内容
     * @param loginUser 当前用户
     * @return 保存的对话历史
     */
    ChatHistory saveAiMessage(Long appId, String message, User loginUser);

    /**
     * 根据应用ID删除所有对话历史（应用删除时调用）
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean removeByAppId(Long appId);

    /**
     * 获取应用的最新对话历史
     *
     * @param appId 应用ID
     * @param limit 限制条数
     * @return 对话历史列表
     */
    List<ChatHistory> getLatestChatHistory(Long appId, int limit);

    /**
     * 获取管理员查询条件（管理员可以查看所有对话历史）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getAdminQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId 应用ID
     * @param pageSize 页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param loginUser 当前用户
     * @return 对话历史分页
     */
    com.mybatisflex.core.paginate.Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                                              java.time.LocalDateTime lastCreateTime,
                                                                              User loginUser);
}
