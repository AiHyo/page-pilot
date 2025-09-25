package com.aih.pagepilot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.pagepilot.constant.UserConstant;
import com.aih.pagepilot.exception.BusinessException;
import com.aih.pagepilot.exception.ErrorCode;
import com.aih.pagepilot.exception.ThrowUtils;
import com.aih.pagepilot.model.dto.ChatHistoryQueryRequest;
import com.aih.pagepilot.model.entity.App;
import com.aih.pagepilot.model.entity.User;
import com.aih.pagepilot.model.enums.MessageTypeEnum;
import com.aih.pagepilot.model.vo.AppVO;
import com.aih.pagepilot.model.vo.ChatHistoryVO;
import com.aih.pagepilot.model.vo.UserVO;
import com.aih.pagepilot.service.AppService;
import com.aih.pagepilot.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.aih.pagepilot.model.entity.ChatHistory;
import com.aih.pagepilot.mapper.ChatHistoryMapper;
import com.aih.pagepilot.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author zeng.liqiang
 * @since 2025-09-25
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public void validChatHistory(ChatHistory chatHistory, boolean add) {
        if (chatHistory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String message = chatHistory.getMessage();
        String messageType = chatHistory.getMessageType();
        Long appId = chatHistory.getAppId();

        // 创建时，参数不能为空
        if (add) {
            if (StrUtil.isBlank(message) || StrUtil.isBlank(messageType) || appId == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息内容、消息类型和应用ID不能为空");
            }
        }

        // 校验消息类型
        if (StrUtil.isNotBlank(messageType)) {
            MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
            if (messageTypeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息类型不正确");
            }
        }

        // 校验应用是否存在
        if (appId != null) {
            App app = appService.getById(appId);
            if (app == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
            }
        }
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);

        // 填充用户信息
        if (chatHistory.getUserId() != null) {
            User user = userService.getById(chatHistory.getUserId());
            if (user != null) {
                chatHistoryVO.setUser(userService.getUserVO(user));
            }
        }

        // 填充应用信息
        if (chatHistory.getAppId() != null) {
            App app = appService.getById(chatHistory.getAppId());
            if (app != null) {
                chatHistoryVO.setApp(appService.getAppVO(app));
            }
        }

        return chatHistoryVO;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }

        // 批量获取用户信息，避免N+1查询问题
        List<Long> userIds = chatHistoryList.stream()
                .map(ChatHistory::getUserId)
                .filter(userId -> userId != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取应用信息
        List<Long> appIds = chatHistoryList.stream()
                .map(ChatHistory::getAppId)
                .filter(appId -> appId != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息并转换为Map
        Map<Long, UserVO> userVOMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            userVOMap = users.stream()
                    .collect(Collectors.toMap(User::getId, userService::getUserVO));
        }

        // 批量查询应用信息并转换为Map
        Map<Long, AppVO> appVOMap = new HashMap<>();
        if (!appIds.isEmpty()) {
            List<App> apps = appService.listByIds(appIds);
            appVOMap = apps.stream()
                    .collect(Collectors.toMap(App::getId, appService::getAppVO));
        }

        // 构建ChatHistoryVO列表
        final Map<Long, UserVO> finalUserVOMap = userVOMap;
        final Map<Long, AppVO> finalAppVOMap = appVOMap;
        return chatHistoryList.stream()
                .map(chatHistory -> {
                    ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
                    BeanUtil.copyProperties(chatHistory, chatHistoryVO);
                    // 设置用户信息
                    if (chatHistory.getUserId() != null) {
                        chatHistoryVO.setUser(finalUserVOMap.get(chatHistory.getUserId()));
                    }
                    // 设置应用信息
                    if (chatHistory.getAppId() != null) {
                        chatHistoryVO.setApp(finalAppVOMap.get(chatHistory.getAppId()));
                    }
                    return chatHistoryVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        java.time.LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getId, id)
                .like(ChatHistory::getMessage, message)
                .eq(ChatHistory::getMessageType, messageType)
                .eq(ChatHistory::getAppId, appId)
                .eq(ChatHistory::getUserId, userId);

        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt(ChatHistory::getCreateTime, lastCreateTime);
        }

        // 默认按创建时间降序排序
        if (StrUtil.isBlank(sortField)) {
            queryWrapper.orderBy(ChatHistory::getCreateTime, false);
        } else {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }

        return queryWrapper;
    }

    @Override
    public boolean hasPermission(Long appId, HttpServletRequest request) {
        if (appId == null || request == null) {
            return false;
        }
        try {
            User loginUser = userService.getLoginUser(request);
            // 管理员有所有权限
            if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
                return true;
            }
            // 普通用户只能查看自己创建的应用的对话历史
            App app = appService.getById(appId);
            return app != null && app.getUserId() != null && app.getUserId().equals(loginUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ChatHistory saveUserMessage(Long appId, String message, User loginUser) {
        if (appId == null || StrUtil.isBlank(message) || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        ChatHistory chatHistory = ChatHistory.builder()
                .message(message)
                .messageType(MessageTypeEnum.USER.getValue())
                .appId(appId)
                .userId(loginUser.getId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        validChatHistory(chatHistory, true);
        boolean success = this.save(chatHistory);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存用户消息失败");
        }
        return chatHistory;
    }

    @Override
    public ChatHistory saveAiMessage(Long appId, String message, User loginUser) {
        if (appId == null || StrUtil.isBlank(message) || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        ChatHistory chatHistory = ChatHistory.builder()
                .message(message)
                .messageType(MessageTypeEnum.AI.getValue())
                .appId(appId)
                .userId(loginUser.getId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        validChatHistory(chatHistory, true);
        boolean success = this.save(chatHistory);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存AI消息失败");
        }
        return chatHistory;
    }

    @Override
    public boolean removeByAppId(Long appId) {
        if (appId == null) {
            return false;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<ChatHistory> getLatestChatHistory(Long appId, int limit) {
        if (appId == null || limit <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .orderBy(ChatHistory::getCreateTime, false)
                .limit(limit);
        return this.list(queryWrapper);
    }

    @Override
    public QueryWrapper getAdminQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        // 管理员查询条件与普通用户相同，但不限制用户权限
        return getQueryWrapper(chatHistoryQueryRequest);
    }


    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public com.mybatisflex.core.paginate.Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                                                     java.time.LocalDateTime lastCreateTime,
                                                                                     User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        
        // 查询数据
        return this.page(com.mybatisflex.core.paginate.Page.of(1, pageSize), queryWrapper);
    }


    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

}
