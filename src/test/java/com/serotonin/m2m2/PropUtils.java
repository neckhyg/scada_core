package com.serotonin.m2m2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: walter
 * Date: 13-1-29
 * Time: 下午4:08
 * To change this template use File | Settings | File Templates.
 */
public class PropUtils {
    public static void main(String[] args){
        Properties prop = new Properties();
        Properties prop_cn = new Properties();
        try {

            FileInputStream fis = new FileInputStream("D:\\i18n.properties");
            prop.load(fis);
//            fis = new FileInputStream("D:\\messages_zh_CN.properties");
//            prop.load(fis);
//            prop.list(System.out);
            fis = new FileInputStream("D:\\messages_zh_CN.properties");
            prop_cn.load(fis);
            Set<String> pname = prop_cn.stringPropertyNames();
            Iterator<String> it = pname.iterator();
            while(it.hasNext()){
                String key = it.next();
                if(prop.containsKey(key)){
                    prop.setProperty(key,prop_cn.getProperty(key));
                }

//                System.out.println(it.next());
            }

            fis.close();
            FileOutputStream fos = new FileOutputStream("D:\\i18n_zh.properties");
            prop.store(fos, "");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
