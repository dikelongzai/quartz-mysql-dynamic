package com.quartz.bean;

import java.sql.Timestamp;

public class JobTaskBean{
  private long taskId = -1;
  private String taskCode = "";
  private String taskType = "";
  private String taskImplClass = "";
  private String taskExpress = "";
  private Timestamp stateDate = null;
  private String state = "";
  private String parms = "";
  private String remark = "";
  private Timestamp createDate = null;

  public String getJarPath() {
    return jarPath;
  }

  public void setJarPath(String jarPath) {
    this.jarPath = jarPath;
  }

  private String jarPath="";

  public Timestamp getCreateDate(){
    return createDate;
  }

  public String getParms(){
    return parms;
  }

  public String getRemark(){
    return remark;
  }

  public String getState(){
    return state;
  }

  public Timestamp getStateDate(){
    return stateDate;
  }

  public String getTaskCode(){
    return taskCode;
  }

  public String getTaskExpress(){
    return taskExpress;
  }

  public long getTaskId(){
    return taskId;
  }

  public String getTaskImplClass(){
    return taskImplClass;
  }

  public String getTaskType(){
    return taskType;
  }

  public void setCreateDate( Timestamp createDate ){
    this.createDate = createDate;
  }

  public void setParms( String parms ){
    this.parms = parms;
  }

  public void setRemark( String remark ){
    this.remark = remark;
  }

  public void setState( String state ){
    this.state = state;
  }

  public void setStateDate( Timestamp stateDate ){
    this.stateDate = stateDate;
  }

  public void setTaskCode( String taskCode ){
    this.taskCode = taskCode;
  }

  public void setTaskExpress( String taskExpress ){
    this.taskExpress = taskExpress;
  }

  public void setTaskId( long taskId ){
    this.taskId = taskId;
  }

  public void setTaskImplClass( String taskImplClass ){
    this.taskImplClass = taskImplClass;
  }

  public void setTaskType( String taskType ){
    this.taskType = taskType;
  }

}
