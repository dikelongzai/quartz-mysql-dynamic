package com.quartz.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quartz.bean.JobTaskBean;
import com.quartz.util.JDBC;
import com.quartz.util.PropertiesUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author hlb
 */
public class Mouse {
    private static final Log log = LogFactory.getLog(Mouse.class);
    private static SchedulerFactory sf = new StdSchedulerFactory();
    public static String type;
    public static void run(String  taskType) throws Exception {
        if(type!=null&&!"".equals(taskType)){
            type=taskType;
        }
        loadAll();
        Timer timer = new Timer();
        timer.schedule(new reloadTimer(), new Date(), 5000L);
    }

    /**
     * 清除任务
     *
     * @throws Exception
     */
    public static void loadAll() throws Exception {
        Scheduler sched = sf.getScheduler();
        JobDetail job = null;
        JobDataMap map = null;
        CronTrigger trigger = null;
        JobTaskBean[] bean=null;
        String baseSql="SELECT * FROM job_task WHERE STATE='U'";
        if(type!=null&&!"".equals(type.trim())){
            baseSql+=" ADD TASK_TYPE='"+type+"'";
        }
         bean = JDBC.getJobTaskBean("SELECT * FROM job_task WHERE STATE='U'", new HashMap());
        if (bean == null) return;
        for (int i = 0; i < bean.length; i++) {
            map = new JobDataMap();
            map.put("TASK_ID", new Long(bean[i].getTaskId()));
            if (bean[i].getParms() != null && !bean[i].getParms().equals("")) {
                JSONObject jsonParam = JSON.parseObject(bean[i].getParms());
                //数据库json格式的参数初始化遍历到map
                Set<Map.Entry<String, Object>> mapTmp = jsonParam.entrySet();
                if (mapTmp != null) {
                    for (Map.Entry<String, Object> entry : jsonParam.entrySet()) {
                        map.put(entry.getKey(), entry.getValue());
                        log.info("--->Mouse load TASK_ID=" + map.get("TASK_ID") + ";param=" + entry.getKey() + ";value=" + entry.getValue().toString());
                    }
                }
            }

            Class jobClass = getJobClass(bean[i].getJarPath(), bean[i].getTaskImplClass());
            if (jobClass != null) {
                job = new JobDetail(bean[i].getTaskCode(), bean[i].getTaskType(), jobClass);
                job.setJobDataMap(map);
                trigger = new CronTrigger(bean[i].getTaskCode() + "trigger", bean[i].getTaskType() + "trigger", bean[i].getTaskCode(),
                        bean[i].getTaskType(), bean[i].getTaskExpress());
                sched.addJob(job, true);
                sched.scheduleJob(trigger);
            }

        }
        sched.start();
    }

    /**
     * <p>
     * 重新加载任务
     * </p>
     *
     * @throws Exception
     */
    static void reload() throws Exception {
        if (clearScheduler()) {
            loadAll();
            log.info("success reload");
        }

    }

    /**
     * <p>
     * 清除任务
     * </p>
     *
     * @throws Exception
     */
    static boolean clearScheduler() throws Exception {
        boolean isSuccess = true;
        Scheduler sched = sf.getScheduler();
        synchronized (Mouse.class){
            if (sched.getCurrentlyExecutingJobs().isEmpty()) {
                String triggerGroups[]=sched.getTriggerGroupNames();
                for(String triggerGroup:triggerGroups){
                    String[] triggerNames=sched.getTriggerNames(triggerGroup);
                    for(String trigger:triggerNames){
                        log.info("delete job trigger=" + trigger + ";triggerGroup=" + triggerGroup);
                        sched.pauseTrigger(trigger, triggerGroup);// 停止触发器
                        sched.unscheduleJob(trigger, triggerGroup);// 移除触发器
                    }


                }
                String[] groupNames = sched.getJobGroupNames();

                for (String groupName : groupNames) {
                    String[] jobNames = sched.getJobNames(groupName);
                    for (String jobName : jobNames) {
                        isSuccess = sched.deleteJob(jobName, groupName);
                        log.info("delete job jobName=" + jobName + ";groupName=" + groupName);
                        if (!isSuccess) {
                            return isSuccess;
                        }
                    }
                }

            }
        }

        return isSuccess;

    }

    static Class getJobClass(String jarPath, String className) throws Exception {
        Class<?> classInstance = null;
        try {
            classInstance = Class.forName(className);
        } catch (Exception e) {
            URL url1 = new URL(jarPath);
            URLClassLoader myClassLoader1 = new URLClassLoader(new URL[]{url1}, Thread.currentThread()
                    .getContextClassLoader());
            classInstance = myClassLoader1.loadClass(className);

        }
        if (!classInstance.getSuperclass().getName().endsWith("Task")) {
            classInstance = null;
            throw new RuntimeException(" job super class must be Task class is =" + classInstance.getName());
        }
        return classInstance;
    }

    static  class reloadTimer extends TimerTask {
        reloadTimer() {
        }

        public void run() {
            log.info(" check is need reload");
            if (Boolean.valueOf(PropertiesUtils.readData("quartzcore.properties", "quartz.job.isneedreload").toLowerCase().equals("true"))) {
               try {
                   reload();
               }catch (Exception e){
                   log.error("reload exception"+e.getMessage());
                   e.printStackTrace();
               }
                PropertiesUtils.writeData("quartzcore.properties", "quartz.job.isneedreload", "false");
                log.info("reload success");
            }
        }
    }


}

