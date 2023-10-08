package com.example.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.io.*;
import java.util.Properties;

/**
 * 配置文件操作工具类
 * @author d4127
 */
public class PropertiesUtil {

    private static final String ENCODING = "utf-8";

    /**
     * 加载获取yaml配置文件
     *
     */
    public static Properties loadYaml(String filePath) {
        Properties properties = null;
        try {
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            FileSystemResource fileSystemResource = new FileSystemResource(FileUtil.file(ResourceUtil.getResource(filePath)));
            yaml.setResources(fileSystemResource);
            properties = yaml.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 加载获取properties配置文件
     *
     */
    public static Properties loadProperties(String filePath) {
        FileInputStream fis = null;
        Properties properties = new Properties();
        try {
            fis = new FileInputStream(new File(filePath, ENCODING));
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    /**
     * 把properties配置文件写到指定位置
     *
     */
    public static void write2Properties(Properties properties, String filePath) {
        FileOutputStream fos = null;
        try {
            if (properties == null) {
                return;
            }
            fos = new FileOutputStream(new File(filePath));
            properties.store(fos, "hello");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 把 properties 转化为yaml 格式，输出到指定位置
     *
     */
    public static void write2Yaml(Properties properties, String filePath) {
        try {
            if (properties == null) {
                return;
            }
            //properties 转化为yaml 格式字符串
            StringBuffer ymlString = PropertiesToMapUtil.prop2YmlString(properties);
            writeStr2File(ymlString, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把字符串写到指定的文件
     *
     */
    public static void writeStr2File(StringBuffer msg, String filePath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("src\\main\\resources\\target\\" + filePath);
            //将字符串写到指定文件
            fos.write(msg.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

