package com.example.tools;

import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.logging.Logger;

/**
 * 把properties 转化为 Map
 */
public class PropertiesToMapUtil {
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(PropertiesToMapUtil.class.getName());

    /**
     * properties 配置文件转Map 集合
     */
    public static Map<String, Object> prop2Map(Properties properties) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String, Object> propMap = new HashMap<>();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                propMap.put((String) entry.getKey(), entry.getValue());
            }
            resultMap = parseToMap(propMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 将propMap 转化为 HashTable结构
     *
     * @param propMap
     * @return
     */
    private static Map<String, Object> parseToMap(Map<String, Object> propMap) {
        Map<String, Object> resultMap = new Hashtable<>();
        try {
            if (CollectionUtils.isEmpty(propMap)) {
                return resultMap;
            }
            Iterator<Map.Entry<String, Object>> it = propMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String key = entry.getKey();
                Object propValue = entry.getValue();
                Object resultObj = null;
                if (!key.contains(".")) {
                    if (!key.contains("[") && !key.contains("]")) {
                        resultMap.put(key, propValue);
                    } else if (key.contains("[") && key.contains("]")) {
                        key = key.substring(0, key.lastIndexOf("["));
                        resultObj = resultMap.get(key);
                        List<String> list = null;
                        if (resultObj != null) {
                            list = (List<String>) resultObj;
                        } else {
                            list = new ArrayList<>();
                            resultMap.put(key, list);
                        }
                        list.add((String) propValue);
                    }
                    it.remove();
                } else {
                    String currentKey = key.substring(0, key.indexOf("."));
                    Map<String, Object> childMap = getChildMap(propMap, currentKey);
                    Map<String, Object> map = parseToMap(childMap);
                    resultMap.put(currentKey, map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 获取拥有相同父级节点的子节点
     */
    private static Map<String, Object> getChildMap(Map<String, Object> propMap, String currentKey) {
        Map<String, Object> childMap = new Hashtable<>();
        try {
            Iterator<Map.Entry<String, Object>> iterator = propMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                if (key.contains(currentKey + ".")) {
                    key = key.substring(key.indexOf(".") + 1);
                    childMap.put(key, entry.getValue());
//                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return childMap;
    }

    /**
     * map集合转化为yaml格式字符串
     */
    public static StringBuffer map2YamlString(Map<String, Object> propMap) {
        //默认deep 为零，表示不空格，deep 每加一层，缩进两个空格
        return map2YamlString(propMap, 0);
    }

    /**
     * 把Map集合转化为yaml格式 String字符串
     *
     * @param propMap map格式配置文件
     * @param deep    树的层级，默认deep 为零，表示不空格，deep 每加一层，缩进两个空格
     */
    private static StringBuffer map2YamlString(Map<String, Object> propMap, int deep) {
        StringBuffer yamlBuffer = new StringBuffer();
        try {
            if (CollectionUtils.isEmpty(propMap)) {
                return yamlBuffer;
            }
            String space = getSpace(deep);
            for (Map.Entry<String, Object> entry : propMap.entrySet()) {
                String key = entry.getKey();
                Object valObj = entry.getValue();
                if (key.contains("[") && key.contains("]")) { //list下是一个对象
                    Map<String, Object> valMap = (Map<String, Object>) valObj;
                    if (CollectionUtils.isEmpty(valMap)) {
                        return yamlBuffer;
                    }
                    yamlBuffer.append(getSpace(deep + 1)).append("- ");
                    //在map集合中任意取一个key作为'-'后追加的key
                    String removeKey = "";
                    for (Map.Entry<String, Object> valEntry : valMap.entrySet()) {
                        removeKey = valEntry.getKey();
                        LOGGER.info(valEntry.getKey() + "======" + valEntry.getValue());
                        Object value = valEntry.getValue();
                        StringBuffer valStr;
                        if (value instanceof String) {
                            valStr = new StringBuffer(String.valueOf(value));
                            yamlBuffer.append(removeKey).append(": ").append(valStr).append("\n");
                        } else {
                            Map<String, Object> val = (Map<String, Object>) valEntry.getValue();
                            yamlBuffer.append(removeKey).append(":\n");
                            valStr = map2YamlString(val, deep + 3);
                            yamlBuffer.append(valStr.toString());
                        }
                        break;
                    }
                    //移除已使用过的
                    valMap.remove(removeKey);
                    StringBuffer valStr = map2YamlString(valMap, deep + 2);
                    yamlBuffer.append(valStr.toString());
                } else {
                    key = space + key + ":";
                    if (valObj instanceof String) { //值为value 类型，不用再继续遍历
                        yamlBuffer.append(key).append(" ").append((String) valObj).append("\n");
                    } else if (valObj instanceof List) { //yaml List 集合格式
                        yamlBuffer.append(key).append("\n");
                        List<String> list = (List<String>) entry.getValue();
                        String lSpace = getSpace(deep + 1);
                        for (String str : list) {
                            yamlBuffer.append(lSpace).append("- ").append(str).append("\n");
                        }
                    } else if (valObj instanceof Map) { //继续递归遍历
                        Map<String, Object> valMap = (Map<String, Object>) valObj;
                        yamlBuffer.append(key).append("\n");
                        StringBuffer valStr = map2YamlString(valMap, deep + 1);
                        yamlBuffer.append(valStr.toString());
                    } else {
                        yamlBuffer.append(key).append(" ").append(valObj).append("\n");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return yamlBuffer;
    }

    /**
     * 获取缩进空格
     */
    private static String getSpace(int deep) {
        StringBuilder buffer = new StringBuilder();
        if (deep == 0) {
            return "";
        }
        for (int i = 0; i < deep; i++) {
            buffer.append("  ");
        }
        return buffer.toString();
    }

    /**
     * properties 格式转化为 yaml 格式字符串
     */
    public static StringBuffer prop2YmlString(Properties properties) {
        if (properties == null || properties.isEmpty()) {
            return new StringBuffer();
        }
        Map<String, Object> map = prop2Map(properties);
        map.forEach((key, val) -> System.out.println(key + "===" + val));
        return map2YamlString(map);
    }

}

