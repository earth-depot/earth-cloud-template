package com.cms.auth.config.custom;


import org.apache.commons.collections.MapUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.concurrent.TimeUnit;

import static com.cms.common.tool.constant.ConstantCode.CACHE_LOGIN_TOKEN;

/**
 * @author 2022/1/7 14:27
 */
public class CmsTokenStore extends JwtTokenStore {

    private final StringRedisTemplate stringRedisTemplate;

    public CmsTokenStore(JwtAccessTokenConverter jwtTokenEnhancer, StringRedisTemplate stringRedisTemplate) {
        super(jwtTokenEnhancer);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        String jti = MapUtils.getString(token.getAdditionalInformation(),"jti");
        String userid = MapUtils.getString(token.getAdditionalInformation(),"userid");
        System.out.println("jti->>>"+jti);
        System.out.println("userid->>>"+userid);
        stringRedisTemplate.opsForValue().set(CACHE_LOGIN_TOKEN + jti,userid,token.getExpiresIn(), TimeUnit.SECONDS);
        SecurityOAuth2AuthenticationHolder.setAuthentication(authentication);
    }

    // 这里用DefaultOAuth2AccessToken的value来构造jti
    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        stringRedisTemplate.delete(CACHE_LOGIN_TOKEN + token.getValue());
    }
}
