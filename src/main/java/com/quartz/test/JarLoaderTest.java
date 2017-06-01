package com.quartz.test;

import com.framework.jar.JarLoader;

/**
 * Created by dikelongzai 15399073387@163.com on 2016-09-23
 * .
 */
public class JarLoaderTest {
    public static void main(String[] args) {
        //将jar包转为byte[]
        byte[] resource = JarLoader.getDataSource("D:/fps_test.jar");
        try {
            //通过byte[]获得主函数所在类
            Class clz = JarLoader.load(resource);
            //调用main函数
            JarLoader.callVoidMethod(clz, "main", new String[] {""});
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
