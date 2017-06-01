package com.quartz.test;

import com.quartz.core.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author keyboardsun
 *
 */
public class Test
    extends Task {

  public void execute( HashMap map ){
      System.out.println("****************************************");
      System.out.println("run id:"+map.get("TASK_ID"));
    Iterator i = map.keySet().iterator();
    while (i.hasNext()){
      Object key = i.next();
      System.out.println("init key:"+key+" value:"+map.get( key ));
    }
      System.out.println(System.currentTimeMillis());
      System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

  }

}
