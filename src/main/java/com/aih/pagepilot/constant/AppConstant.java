package com.aih.pagepilot.constant;

/**
 * <p>
 * 网页优先级常量
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/9/22
 */
public interface AppConstant {

    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("app.code.output.root",
            System.getProperty("user.dir") + "/tmp/code_output");

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("app.code.deploy.root",
            System.getProperty("user.dir") + "/tmp/code_deploy");

    /**
     * 应用部署域名（本地开发可覆盖为 http://localhost:8124/api/static）
     */
    String CODE_DEPLOY_HOST = System.getProperty("app.code.deploy.host", "http://localhost");

}
