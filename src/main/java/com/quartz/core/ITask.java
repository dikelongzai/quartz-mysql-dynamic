package com.quartz.core;

import java.util.HashMap;

/**
 *
 * 
 * @author keyboardsun
 */
public interface ITask{
  /**
   *
   * 
   * @param map
   * <item>
   * <key>sss</key>
   * <value>vvv</value>
   * </item>
   * <item>
   * <key>ss</key>
   * <value>vv</value>
   * </item>
   */
  public void execute(HashMap map);
}
