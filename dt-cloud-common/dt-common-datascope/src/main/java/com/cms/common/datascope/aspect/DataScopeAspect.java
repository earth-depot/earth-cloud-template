package com.cms.common.datascope.aspect;

import com.cms.common.core.domain.SysSearchPage;
import com.cms.common.core.utils.ApiCallUtils;
import com.cms.common.core.utils.ServletUtils;
import com.cms.common.datascope.annotation.DataScope;
import com.cms.common.tool.domain.SecurityClaimsUserEntity;
import com.cms.common.tool.domain.SysDataScopeVoEntity;
import com.cms.common.tool.result.ResultException;
import com.cms.common.tool.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static com.cms.common.tool.constant.ConstantCode.DATA_SCOPE;
import static com.cms.common.tool.constant.ConstantCode.DATA_SCOPE_ALL;
import static com.cms.common.tool.constant.ConstantCode.DATA_SCOPE_CUSTOM;
import static com.cms.common.tool.constant.ConstantCode.DATA_SCOPE_DEPT;
import static com.cms.common.tool.constant.ConstantCode.DATA_SCOPE_DEPT_AND_CHILD;
import static com.cms.common.tool.constant.ConstantCode.DATA_SCOPE_SELF;

/**
 * 角色数据权限过滤
 * @author  2022/3/18 17:27
 */
@Slf4j
@Aspect
@Component
public class DataScopeAspect {

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) throws Throwable {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }

    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope) {
        // 获取当前登录的用户
        SecurityClaimsUserEntity currentUser = null;
        try {
            currentUser = ApiCallUtils.securityClaimsUser(ServletUtils.getRequest());
        } catch (ResultException ex) {
            ex.printStackTrace();
        }
        if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin()) {
            // 如果是超级管理员，则不过滤数据
            dataScopeFilter(joinPoint, currentUser, controllerDataScope.deptAlias(), controllerDataScope.userAlias());
        }
    }

    /**
     * 数据范围过滤
     * @param joinPoint 切点
     * @param user 用户
     * @param deptAlias 部门表别名
     * @param userAlias 用户表别名
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SecurityClaimsUserEntity user, String deptAlias, String userAlias) {
        StringBuilder sqlString = new StringBuilder();
        for (SysDataScopeVoEntity role : user.getRoles()) {
            Long dataScope = role.getDataScope();
            if (DATA_SCOPE_ALL.equals(dataScope)) {
                sqlString = new StringBuilder();
                break;
            }
            else if (DATA_SCOPE_CUSTOM.equals(dataScope)) {
                log.info("自定义数据权限->>>");
                sqlString.append(StringUtils.format(
                        " OR {}.id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias,
                        role.getRoleId()));
            }
            else if (DATA_SCOPE_DEPT.equals(dataScope)) {
                log.info("部门数据权限->>>");
                sqlString.append(StringUtils.format(" OR {}.id = {} ", deptAlias, user.getDeptId()));
            }
            else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
                log.info("部门及以下数据权限->>>");
                sqlString.append(StringUtils.format(
                        " OR {}.id IN ( SELECT id FROM sys_department WHERE id = {} or find_in_set( {} , parent_id ) )",
                        deptAlias, user.getDeptId(), user.getDeptId()));
            }
            else if (DATA_SCOPE_SELF.equals(dataScope)) {
                if (StringUtils.isNotBlank(userAlias)) {
                    log.info("仅本人数据权限->>>");
                    sqlString.append(StringUtils.format(" OR {}.id = {} ", userAlias, user.getUserid()));
                }
                else {
                    // 数据权限为仅本人且没有userAlias别名不查询任何数据
                    sqlString.append(" OR 1=0 ");
                }
            }
        }
        if (StringUtils.isNotBlank(sqlString.toString())) {
            // 获取查询的参数
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof SysSearchPage) {
                SysSearchPage baseEntity = (SysSearchPage) params;
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + sqlString.substring(4) + ")");
                log.info("sql拼接查询为："+baseEntity);
            }
        }
    }

    private void clearDataScope(final JoinPoint joinPoint) {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof SysSearchPage) {
            SysSearchPage baseEntity = (SysSearchPage) params;
            baseEntity.getParams().put(DATA_SCOPE, "");
        }
    }
}
