package com.cms.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.common.core.domain.SysSearchPage;
import com.cms.common.tool.result.ResultUtil;
import com.cms.job.entity.JobInformationEntity;

/**
 * @date 2022/3/28 19:27
 */
public interface JobService extends IService<JobInformationEntity> {

    ResultUtil<IPage<JobInformationEntity>> pageSearch(SysSearchPage request);

    ResultUtil<String> addScheduleJob(JobInformationEntity jobInformationEntity);

    ResultUtil<String> updateScheduleJob(JobInformationEntity jobInformationEntity);

    ResultUtil<String> deleteScheduleJob(String taskId);

    ResultUtil<String> pauseScheduleJob(String taskId);

    ResultUtil<String> resumeScheduleJob(String taskId);
}
