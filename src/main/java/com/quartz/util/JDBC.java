package com.quartz.util;

import com.quartz.bean.JobTaskBean;
import com.quartz.bean.TaskLogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by Administrator on 2016-09-22.
 */
public class JDBC {
    private static final Log log = LogFactory.getLog(JDBC.class);
    static String DRIVE="";
    static String JDBC_URL="";
    static String USER="";
    static String PASS="";
    static {
       Properties properties=new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("quartzcore.properties"));
        }catch (Exception e){
            log.error("quartzcore.properties must contains in classpath");
        }
        DRIVE=properties.getProperty("quartz.jdbc.driverClass");
        log.info(" JDBC init DRIVE="+DRIVE);
        JDBC_URL=properties.getProperty("quartz.jdbc.url");
        log.info(" JDBC init JDBC_URL="+JDBC_URL);
        USER=properties.getProperty("quartz.jdbc.username");
        log.info(" JDBC init USER="+USER);
        PASS=properties.getProperty("quartz.jdbc.password");
        log.info(" JDBC init PASS="+PASS);
    }
    /**
     * 获取数据库连接，这里的例子是用mysql的
     * @return
     * @throws Exception
     */
    public static Connection getConnection()throws Exception {
        try {
            Class.forName(DRIVE).newInstance();
            Connection conn = DriverManager.getConnection(JDBC_URL,USER, PASS );
            conn.setAutoCommit(true);
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("connect quartz mysql database err "+e.getMessage());
        }
    }

    /**
     * 插入操作日志，返回操作日志的主键的值。
     * 这里用了同步，可能执行过程中会影响效率，当然这里可以选择用static变量都是可以了，反正这是一个解决办法而已，随便你怎么搞，
     * 这个只是一个超级简单的例子，能看明白怎么回事就好了。
     * @param sql
     * @param map
     * @return
     * @throws Exception
     */
    public synchronized static TaskLogBean insert(String sql,HashMap map) throws Exception{
        Connection con = getConnection();
        String sql_ = "SELECT MAX(TASK_LOG_ID)+1 FROM task_log"; //获取最大的值+1 作为主键，这里方法比较土。
        PreparedStatement ps = con.prepareStatement(sql_);
        ResultSet rs = ps.executeQuery();
        rs.next();
        map.put("TASK_LOG_ID",new Long(rs.getLong(1)));
        rs.close();
        ps = execStatementValues(con,sql,map);
        ps.executeUpdate();
        sql_= "SELECT * FROM task_log WHERE TASK_LOG_ID=:TASK_LOG_ID";
        TaskLogBean[] bean = getTaskLogBean(con,sql_, map);
        return bean[0];
    }

    /**
     * 获取JOB的任务列表。
     * @param sql
     * @param map
     * @return
     * @throws Exception
     */
    public static JobTaskBean[] getJobTaskBean(String sql,HashMap map) throws Exception{
        Connection con = getConnection();
        PreparedStatement ps = execStatementValues(con,sql,map);
        ResultSet rs = ps.executeQuery();
        List list = new ArrayList();
        while(rs.next()){
            JobTaskBean bean = new JobTaskBean();
            bean.setCreateDate(rs.getTimestamp("CREATE_DATE"));
            bean.setParms(rs.getString("PARMS"));
            bean.setRemark(rs.getString("REMARK"));
            bean.setState(rs.getString("STATE"));
            bean.setStateDate(rs.getTimestamp("STATE_DATE"));
            bean.setTaskCode(rs.getString("TASK_CODE"));
            bean.setTaskExpress(rs.getString("TASK_EXPRESS"));
            bean.setTaskId(rs.getLong("TASK_ID"));
            bean.setTaskImplClass(rs.getString("TASK_IMPL_CLASS"));
            bean.setTaskType(rs.getString("TASK_TYPE"));
            bean.setJarPath(rs.getString("TASK_JAR_PATH"));
            list.add(bean);
        }
        rs.close();
        con.close();
        return list.size()==0?null:(JobTaskBean[])list.toArray(new JobTaskBean[0]);
    }

    /**
     * 获取任务日志记录
     * @param con
     * @param sql
     * @param map
     * @return
     * @throws Exception
     */
    public static TaskLogBean[] getTaskLogBean(Connection con,String sql,HashMap map) throws Exception{
        if(con==null)
            con = getConnection(); //这里连接可以传空，无所谓的。
        PreparedStatement ps = execStatementValues(con,sql,map);
        ResultSet rs = ps.executeQuery();
        List list = new ArrayList();
        while(rs.next()){
            TaskLogBean bean = new TaskLogBean();
            bean.setFinishDate(rs.getTimestamp("FINISH_DATE"));
            bean.setRemarks(rs.getString("REMARKS"));
            bean.setStartDate(rs.getTimestamp("START_DATE"));
            bean.setState(rs.getString("STATE"));
            bean.setTaskId(rs.getLong("TASK_ID"));
            bean.setTaskLogId(rs.getLong("TASK_LOG_ID"));
            list.add(bean);
        }
        rs.close();
        con.close();
        return list.size()==0?null:(TaskLogBean[])list.toArray(new TaskLogBean[0]);
    }

    /**
     * 以下方法用于绑定变量的，没啥技术含量。
     * 可以参看我的另外的数据库绑定变量的blog。
     * 更多代码可以去sourceforge下载jdf项目看看。
     * @param con 数据库连接
     * @param sql 绑定变量类型的sql
     * @param map 绑定值和绑定名字
     * @return 赋值初始化好的
     * @throws SQLException
     */
    public static PreparedStatement execStatementValues(Connection con,String sql,HashMap map) throws Exception{

        PreparedStatement stmt = null;
        ArrayList clumBandNameList = new ArrayList(); //存放绑定变量的名字
        sql = sql + " ";
        String [] temp = sql.split(":"); //这里把变量的名字给取出来
        for (int i = 1; i < temp.length; i++) {
            try{
                clumBandNameList.add(temp[i].substring(0,temp[i].indexOf(" ")));
            } catch(StringIndexOutOfBoundsException exception){
                clumBandNameList.add(temp[i]);
            }
        }
        sql = sql.replaceAll(":(.*?)\\s","?");//把绑定变量的名字:XXX 取出为 ?,生成标准SQL

        stmt = con.prepareStatement(sql);

        int index = 0;

        for (int i = 0; i < clumBandNameList.size(); i++) {
            if(map==null||map.size()<1){
                throw new SQLException("有的变量的值没有赋，请确定每个绑定的都有值了："+sql+map.toString());
            }
            Object value = null;
            try{
                value = map.get(clumBandNameList.get(i));
//      log.debug("邦定变量的值:"+clumBandNameList.get(i)+"="+value.toString());
            }catch(Exception e){
                throw new Exception("变量："+clumBandNameList.get(i)+"没有对应绑定的值");
            }

            String type = value.getClass().getName().substring(value.getClass().getName().lastIndexOf(".")); //获取绑定的类型
            index = i+1; //绑定的索引
            if (type.equalsIgnoreCase("String")) {
                String content = value.toString();
                if (content.length() > 2000) {
                    stmt.setCharacterStream(index, new StringReader(content), content.length());
                } else  {
                    stmt.setString(index, content);
                }
            }  else if (type.equalsIgnoreCase("Short")) {
                stmt.setShort(index, Short.parseShort(value.toString()));
            } else if (type.equalsIgnoreCase("Integer")) {
                stmt.setInt(index, Integer.parseInt(value.toString()));
            }  else if (type.equalsIgnoreCase("Float")) {
                stmt.setFloat(index, Float.parseFloat(value.toString()));
            } else if (type.equalsIgnoreCase("Byte")) {
                stmt.setByte(index, Byte.parseByte(value.toString()));
            } else if (type.equalsIgnoreCase("Char")) {
                stmt.setString(index, value.toString());
            }   else if (type.equalsIgnoreCase("Long")) {
                stmt.setLong(index, Long.parseLong(value.toString()));
            } else if (type.equalsIgnoreCase("Double")) {
                stmt.setDouble(index, Double.parseDouble(value.toString()));
            }else if (type.equalsIgnoreCase("Boolean")) {
                stmt.setBoolean(index, Boolean.getBoolean(value.toString()));
            } else if (type.equalsIgnoreCase("Date")) {
                if (value instanceof java.sql.Date)
                    stmt.setDate(index, (java.sql.Date)value);
                else
                    stmt.setDate(index, java.sql.Date.valueOf(value.toString()));
            } else if (type.equalsIgnoreCase("Time")) {
                if (value instanceof Time)
                    stmt.setTime(index, (Time)value);
                else
                    stmt.setTime(index, Time.valueOf(value.toString()));
            } else if (type.equalsIgnoreCase("DateTime")) {
                if (value instanceof Timestamp)
                    stmt.setTimestamp(index, (Timestamp)value);
                else if (value instanceof java.sql.Date)
                    stmt.setTimestamp(index, new Timestamp(((java.sql.Date)value).getTime()));
                else
                    stmt.setTimestamp(index, Timestamp.valueOf(value.toString()));
            }
            else if(type.equalsIgnoreCase("Timestamp")){
                stmt.setTimestamp(index, (Timestamp)value);
            }
            else if (value instanceof Character) {
                stmt.setString(index, value.toString());
            } else {
                stmt.setObject(index, value);
            }
        }

        return stmt;

    }
}
