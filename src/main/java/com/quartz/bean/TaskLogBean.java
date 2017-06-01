package com.quartz.bean;

import java.sql.Timestamp;
/**
 *
 * @author keyboardsun
 *
 */
public class TaskLogBean{
  private long taskLogId=-1;
  private long taskId=-1;
  private String state="";
  private Timestamp startDate=null;
  private Timestamp finishDate=null;
  private String remarks="";
  public Timestamp getFinishDate(){
    return finishDate;
  }
  public String getRemarks(){
    return remarks;
  }
  public Timestamp getStartDate(){
    return startDate;
  }
  public String getState(){
    return state;
  }
  public long getTaskId(){
    return taskId;
  }
  public long getTaskLogId(){
    return taskLogId;
  }
  public void setFinishDate( Timestamp finishDate ){
    this.finishDate = finishDate;
  }
  public void setRemarks( String remarks ){
    this.remarks = remarks;
  }
  public void setStartDate( Timestamp startDate ){
    this.startDate = startDate;
  }
  public void setState( String state ){
    this.state = state;
  }
  public void setTaskId( long taskId ){
    this.taskId = taskId;
  }
  public void setTaskLogId( long taskLogId ){
    this.taskLogId = taskLogId;
  }

}
