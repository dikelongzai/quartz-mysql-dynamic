package com.quartz.core;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;

import com.quartz.bean.TaskLogBean;
import com.quartz.util.JDBC;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author keyboardsun
 *
 */
public abstract class Task
    implements ITask,Job,Serializable{

  /**
   * 
   * public void jobToBeExecuted(JobExecutionContext inContext)
   * public void jobWasExecuted(JobExecutionContext inContext,
            JobExecutionException inException
   * @param arg0
   * @throws JobExecutionException
   */
  public void execute( JobExecutionContext arg0 ) throws JobExecutionException{
    TaskLogBean b = null;
    try{
      HashMap map = new HashMap();
      JobDataMap qzMap = arg0.getJobDetail().getJobDataMap();
      Iterator i = qzMap.keySet().iterator();
      while (i.hasNext()){
        Object key = i.next();
        map.put( key, qzMap.get( key ) );
      }
      String insert = "INSERT INTO task_log(TASK_LOG_ID,TASK_ID,REMARKS,START_DATE,STATE) VALUES(:TASK_LOG_ID , :TASK_ID , 'running' , now() , 'R' )";
      HashMap clumMap = new HashMap();
      clumMap.put( "TASK_ID", map.get( "TASK_ID" ) );
      b = JDBC.insert(insert, clumMap);
      execute( map );
      Connection con = JDBC.getConnection();
      String sql = "UPDATE task_log SET STATE='O',REMARKS='complete',FINISH_DATE=now() WHERE TASK_LOG_ID=:TASK_LOG_ID ";
      HashMap m = new HashMap();
      m.put( "TASK_LOG_ID", new Long( b.getTaskLogId() ) );
      PreparedStatement ps = JDBC.execStatementValues( con, sql, m );
      ps.executeUpdate();
      con.close();
    }catch( Exception e ){
      e.printStackTrace();
      Connection con;
      try{
        con = JDBC.getConnection();
        String sql = "UPDATE task_log SET STATE='E',FINISH_DATE=now(),REMARKS=:REMARKS WHERE TASK_LOG_ID=:TASK_LOG_ID ";
        HashMap m = new HashMap();
        m.put( "TASK_LOG_ID", new Long( b.getTaskLogId() ) );
        m.put( "REMARKS", e.getMessage() );
        PreparedStatement ps = JDBC.execStatementValues( con, sql, m );
        ps.executeUpdate();
        con.close();
      }catch( Exception e1 ){
        e1.printStackTrace();
      }

    }

  }

}
