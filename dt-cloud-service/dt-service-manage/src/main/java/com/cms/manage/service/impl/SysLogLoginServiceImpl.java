package com.cms.manage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.core.domain.SysSearchPage;
import com.cms.common.tool.result.ResultUtil;
import com.cms.common.tool.utils.SysCmsUtils;
import com.cms.manage.entity.SysLogLoginEntity;
import com.cms.manage.mapper.SysLogLoginMapper;
import com.cms.manage.service.SysLogLoginService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统登录日志数据实现类
 * @date 2021/11/20 21:28
 */
@CommonsLog
@Service
public class SysLogLoginServiceImpl extends ServiceImpl<SysLogLoginMapper, SysLogLoginEntity> implements SysLogLoginService {

    @Async("sysTaskExecutor")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLoginLog(SysLogLoginEntity loginLogEntity) {
        this.baseMapper.insert(loginLogEntity);
        SysCmsUtils.log.warn("插入数据到MySQL中，当前线程[{}]",Thread.currentThread().getName());
    }

    @Override
    @Transactional(readOnly = true)
    public ResultUtil<IPage<SysLogLoginEntity>> pageSearch(SysSearchPage request) {
        Page<SysLogLoginEntity> page = new Page<>(request.getCurrent(),request.getSize());
        IPage<SysLogLoginEntity> list = this.baseMapper.pageSearch(page,request);
        return ResultUtil.success(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<?> deleteBathLogin(long[] ids) {
        this.baseMapper.deleteBathLogin(ids);
        return ResultUtil.success();
    }
}
