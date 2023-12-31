package com.cms.auth.config.handler;

import com.cms.auth.service.LogFeignService;
import com.cms.common.tool.domain.SecurityClaimsUserEntity;
import com.cms.common.tool.utils.SysCmsUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录成功处理器
 * @author 2022/1/7 14:07
 */
public class TokenAuthenticationSuccessHandler implements OAuth2AuthenticationSuccessHandler {

    @Resource
    private LogFeignService logFeignService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, OAuth2Authentication authentication) throws IOException, ServletException {
        SecurityClaimsUserEntity securityClaimsUser = (SecurityClaimsUserEntity) authentication.getPrincipal();
        logFeignService.sendLoginLog(request,securityClaimsUser,true);
        SysCmsUtils.log.info("登录成功推送日志信息->>>" + securityClaimsUser.getUsername());
    }
}
