package com.api.manage.feign;

import com.api.manage.factory.OauthFeignClientFallback;
import com.cms.common.tool.domain.SecurityClaimsUserEntity;
import com.cms.common.tool.result.ResultUtil;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.api.common.ApiConstants.APPLICATION_MANAGE_API_AFFIX;
import static com.api.common.ApiConstants.APPLICATION_NAME_MANAGE;

/**
 * @author  2022/1/7 16:23
 */
@Service
@FeignClient(value = APPLICATION_NAME_MANAGE, fallbackFactory = OauthFeignClientFallback.class)
public interface OauthFeignClientService {

    @GetMapping(value = APPLICATION_MANAGE_API_AFFIX + "/loadUserByUsername")
    ResultUtil<SecurityClaimsUserEntity> loadUserByUsername(@RequestParam String username);
}
