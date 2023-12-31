package com.cms.job.utils;


import com.cms.common.tool.utils.SysCmsUtils;
import com.cms.job.entity.QuartzJobInfo;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @author  2021/12/10 14:21
 */
@Component
public class QuartzUtils implements ApplicationContextAware {

    /**
     * 调度任务对象key
     */
    public static final String SCHEDULE_TASK_OBJ_KEY = "scheduleTaskObjKey";

    /**
     * taskId参数key
     */
    public static final String TASK_ID = "taskId";

    private static Scheduler quartzScheduler;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try{
            quartzScheduler = applicationContext.getBean(Scheduler.class);
        }catch (Exception e){
            SysCmsUtils.log.warn("load quartz from spring fail");
        }
    }

    /**
     * 创建定时任务
     * @param taskInfo 任务实列对象
     * @return 返回布尔值
     */
    public Boolean addScheduleJob(QuartzJobInfo taskInfo) {
        boolean flag = true;
        JobDataMap newJobDataMap = new JobDataMap();
        newJobDataMap.put(TASK_ID, taskInfo.getTaskId());
        newJobDataMap.put(SCHEDULE_TASK_OBJ_KEY, taskInfo);
        JobDataMap exist = taskInfo.getDataMap();
        if(!ObjectUtils.isEmpty(exist)) {
            newJobDataMap.putAll(exist);
        }
        // 创建需要执行的任务
        JobDetail jobDetail = JobBuilder.newJob(taskInfo.getJobClass()).withIdentity(generateJobKey(taskInfo))
                        .withDescription(taskInfo.getTaskName()).usingJobData(newJobDataMap).build();
        if(taskInfo.getDataMap()!=null) {
            jobDetail.getJobDataMap().putAll(taskInfo.getDataMap());
        }
        // 创建触发器，指定任务执行时间
        ScheduleBuilder<CronTrigger> scheduleBuilder = CronScheduleBuilder.cronSchedule(taskInfo.getCronExpression());
        Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(generateTriggerKey(taskInfo))
                        .withSchedule(scheduleBuilder).build();
        // 把作业和触发器注册到任务调度中
        try {
            quartzScheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            SysCmsUtils.log.warn("创建任务异常：{}",e.toString());
            flag = false;
        }
        return flag;
    }

    /**
     * 生成TriggerKey对象
     * @param taskInfo 任务对象
     * @return 返回TriggerKey
     */
    private static TriggerKey generateTriggerKey(QuartzJobInfo taskInfo) {
        return TriggerKey.triggerKey(taskInfo.getTaskId());
    }

    /**
     * 生成JobKey对象
     * @param taskInfo 任务实列
     * @return 返回对象
     */
    private static JobKey generateJobKey(QuartzJobInfo taskInfo) {
        return JobKey.jobKey(taskInfo.getTaskId());
    }

    /**
     * 删除任务
     * @param taskId 任务ID
     */
    public void deleteScheduleJob(String taskId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(taskId);
        quartzScheduler.deleteJob(jobKey);
    }

    /**
     * 暂停某个任务
     * @param taskId 任务ID
     * @return 返回
     */
    public Boolean pauseScheduleJob(String taskId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(taskId);
        JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            return false;
        } else {
            quartzScheduler.pauseJob(jobKey);
            return true;
        }
    }

    /**
     * 恢复某个任务
     * @param taskId 任务ID
     * @return 返回
     */
    public Boolean resumeScheduleJob(String taskId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(taskId);
        JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            return false;
        } else {
            quartzScheduler.resumeJob(jobKey);
            return true;
        }
    }

    /**
     * 修改任务
     * @param taskId 任务ID
     * @param cronExpression 时间表达式 （如：0/5 * * * * ? ）
     * @return 返回
     */
    public Boolean updateScheduleJob(String taskId, String cronExpression) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(taskId);
        JobKey jobKey = new JobKey(taskId);
        if (quartzScheduler.checkExists(jobKey) && quartzScheduler.checkExists(triggerKey)) {
            CronTrigger trigger = (CronTrigger) quartzScheduler.getTrigger(triggerKey);
            // 不立即执行
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            // 重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            // 重新设置job
            quartzScheduler.rescheduleJob(triggerKey, trigger);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检验是否存在相同的计划任务
     * @param taskId 任务ID
     * @return 返回
     */
    public boolean checkExists(String taskId) {
        boolean flag = true;
        JobKey jobKey = JobKey.jobKey(taskId);
        try {
            quartzScheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            flag = false;
        }
        return flag;
    }

}
