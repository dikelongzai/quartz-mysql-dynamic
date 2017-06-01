package com.quartz.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quartz.bean.JobTaskBean;
import com.quartz.util.JDBC;
import org.quartz.*;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * @author keyboardsun
 *
 */
public class JobEngine
    implements Job{

  public void execute( JobExecutionContext arg0 ) throws JobExecutionException{

    try{
      Scheduler inScheduler = arg0.getScheduler();
      JobDetail job = null;
      JobDataMap map = null;
      CronTrigger trigger = null;
      JobTaskBean[] bean = JDBC.getJobTaskBean("SELECT * FROM job_task WHERE STATE='U' AND STATE_DATE>now()", new HashMap());
      if( bean == null )
        return;
      for( int i = 0; i < bean.length; i++ ){
        String update = "UPDATE job_task SET STATE_DATE=now() WHERE TASK_ID=:TASK_ID";
        Connection con = JDBC.getConnection();
        HashMap m = new HashMap();
        m.put( "TASK_ID", new Long( bean[i].getTaskId() ) );
        PreparedStatement ps = JDBC.execStatementValues( con, update, m );
        ps.executeUpdate();
        con.close();
        map = new JobDataMap();
        map.put( "TASK_ID", new Long( bean[i].getTaskId() ) );
        if( bean[i].getParms() != null && !bean[i].getParms().equals( "" ) ){
          map = new JobDataMap();
          map.put("TASK_ID",new Long(bean[i].getTaskId()));
          if(bean[i].getParms()!=null&&!bean[i].getParms().equals("")){
            JSONObject jsonParam= JSON.parseObject(bean[i].getParms());
            Set<Map.Entry<String, Object>> mapTmp = jsonParam.entrySet();
            if(mapTmp!=null) {
              for(Map.Entry<String, Object> entry:jsonParam.entrySet()) {
                map.put(entry.getKey(),entry.getValue());
              }
            }
          }
        }
        try{
        inScheduler.unscheduleJob( bean[i].getTaskCode() + "trigger", bean[i].getTaskType() + "trigger" );
        inScheduler.deleteJob( bean[i].getTaskCode(), bean[i].getTaskType() );
        } catch(Exception e){
        }
        job = new JobDetail( bean[i].getTaskCode(), bean[i].getTaskType(), Class.forName( bean[i].getTaskImplClass() ) );
        job.setJobDataMap( map );
        trigger = new CronTrigger( bean[i].getTaskCode() + "trigger", bean[i].getTaskType() + "trigger", bean[i].getTaskCode(), bean[i]
          .getTaskType(), bean[i].getTaskExpress() );
        inScheduler.addJob( job, true );
        inScheduler.scheduleJob( trigger );
      }
    }catch( Exception e ){
      e.printStackTrace();
    }

  }

}
