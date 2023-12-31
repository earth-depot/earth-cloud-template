package com.cms.common.tool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 2022/1/7 16:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityClaimsUserEntity implements Serializable {

    private static final long serialVersionUID = -7487458381816891683L;


    /**
     * 登录用户账号
     */
    private String username;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 身份证号码
     */
    private String idno;

    /**
     * 微信号ID
     */
    private String openid;

    /**
     * 登录客户端身份标识
     */
    private String authenticationIdentity;

    /**
     * 登录用户密码
     */
    private String password;

    /**
     * 登录用户ID
     */
    private Long userid;

    /**
     * 登录用户所在部门
     */
    private Long deptId;

    /**
     * 登录用户是否超级管理员
     */
    private boolean isAdmin;

    /**
     * 登录用户角色数据权限
     */
    private List<SysDataScopeVoEntity> roles;

    /**
     * 登录用户头像
     */
    private String avatar;

    /**
     * 帐户是否过期(1 未过期，0已过期)
     */
    private boolean isAccountNonExpired;

    /**
     * 帐户是否被锁定(1 未锁定，0已锁定)
     */
    private boolean isAccountNonLocked;

    /**
     * 密码是否过期(1 未过期，0已过期)
     */
    private boolean isCredentialsNonExpired;

    /**
     * 帐户是否可用(1 可用，0 删除用户)
     */
    private boolean isEnabled;

    /**
     * 登录token
     */
    String jti;

    public Map<String,Object> jwtClaims() {
        Map<String,Object> claims_json = new HashMap<>();
        claims_json.put("username",getUsername());
        claims_json.put("userid",getUserid());
        claims_json.put("avatar",getAvatar());
        claims_json.put("jti",getJti());
        return claims_json;
    }

    public Map<String,Object> jwtClaims(String jti) {
        Map<String,Object> claims_json = jwtClaims();
        claims_json.put("jti",jti);
        return claims_json;
    }
}
