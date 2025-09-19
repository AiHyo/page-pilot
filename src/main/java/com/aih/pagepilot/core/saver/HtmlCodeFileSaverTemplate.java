package com.aih.pagepilot.core.saver;


import cn.hutool.core.util.StrUtil;
import com.aih.pagepilot.ai.model.HtmlCodeResult;
import com.aih.pagepilot.ai.model.enums.CodeGenTypeEnum;
import com.aih.pagepilot.exception.BusinessException;
import com.aih.pagepilot.exception.ErrorCode;

/**
 * <p>
 * HTML代码文件保存器
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/9/19
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // HTML 代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML 代码不能为空");
        }
    }
}
