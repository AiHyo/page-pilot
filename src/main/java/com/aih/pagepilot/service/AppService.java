package com.aih.pagepilot.service;

import com.aih.pagepilot.model.entity.User;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.aih.pagepilot.model.dto.AppQueryRequest;
import com.aih.pagepilot.model.entity.App;
import com.aih.pagepilot.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 网页 服务层。
 *
 * @author zeng.liqiang
 * @since 2025-09-22
 */
public interface AppService extends IService<App> {

    /**
     * 校验网页
     *
     * @param app 网页信息
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取脱敏后的网页信息
     *
     * @param app 网页信息
     * @return 脱敏后的网页信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏后的网页信息（分页）
     *
     * @param appList 网页列表
     * @return 脱敏后的网页信息列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据查询条件构造数据查询参数
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 校验用户是否有权限操作网页
     *
     * @param app 网页信息
     * @param request HTTP请求
     * @return 是否有权限
     */
    boolean hasPermission(App app, HttpServletRequest request);

    /**
     * 获取精选网页的查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest);


    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);
}
