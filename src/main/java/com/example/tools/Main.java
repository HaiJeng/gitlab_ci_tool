package com.example.tools;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author d4127
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static void changeValues(String yamlName) {
        Properties props1 = PropertiesUtil.loadYaml(yamlName);
        Set<Map.Entry<Object, Object>> entries = props1.entrySet();
        for (Map.Entry<Object, Object> it : entries) {
            Object key = it.getKey();
            String string = key.toString();
            String replace = string.replaceAll("[.-]", "_");
            String upperCase = replace.toUpperCase();
            it.setValue("__" + upperCase + "__");
        }
        props1.forEach((key, value) -> System.out.println(key + "=" + value));
        PropertiesUtil.write2Yaml(props1, "changed-values.yaml");
    }

    private static void values(String yamlName) {
        Properties props1 = PropertiesUtil.loadYaml(yamlName);
        Set<Map.Entry<Object, Object>> entries = props1.entrySet();
        for (Map.Entry<Object, Object> it : entries) {
            it.setValue("");
        }
        PropertiesUtil.write2Yaml(props1, "values.yaml");
    }

    private static void configMap(String yamlName) {
        Properties props1 = PropertiesUtil.loadYaml(yamlName);
        Set<Map.Entry<Object, Object>> entries = props1.entrySet();
        Map<Object, Object> secret = new HashMap<>();
        Map<Object, Object> config = new HashMap<>();


        for (Map.Entry<Object, Object> it : entries) {
            if (StrUtil.endWithIgnoreCase(it.getKey().toString(), "password")) {
                String s = "{{ b64enc .Values." + it.getKey() + " | quote}}";
                secret.put(it.getKey(), s);
            } else {
                String s = "{{ .Values." + it.getKey() + " | quote}}";
                config.put(it.getKey(), s);
            }
        }
        props1.clear();
        props1.put("data", secret);
        PropertiesUtil.write2Yaml(props1, "secret.yaml");
        props1.clear();
        props1.put("data", config);
        PropertiesUtil.write2Yaml(props1, "configmap.yaml");
    }

    private static void deployment(String yamlName) {
        Properties props1 = PropertiesUtil.loadYaml(yamlName);
        Properties properties = new Properties();
        Set<Map.Entry<Object, Object>> entries = props1.entrySet();
        int i = 0;
        for (Map.Entry<Object, Object> it : entries) {
            Object key = it.getKey();
            String string = key.toString();
            String replace = string.replaceAll("[.-]", "_");
            String upperCase = replace.toUpperCase();
            properties.setProperty("env[" + i + "].name", upperCase);
            if (StrUtil.endWithIgnoreCase(it.getKey().toString(), "password")) {
                properties.setProperty("env[" + i + "].valueFrom.secretKeyRef.key", string);
                properties.setProperty("env[" + i + "].valueFrom.secretKeyRef.name", "{{ .Release.Name }}-secret");
            } else {
                properties.setProperty("env[" + i + "].valueFrom.configMapKeyRef.key", string);
                properties.setProperty("env[" + i + "].valueFrom.configMapKeyRef.name", "{{ .Release.Name }}-configmap");
            }

            i++;
        }
//        properties.forEach((key, val) -> System.out.println(key + "=" + val));
        PropertiesUtil.write2Yaml(properties, "deployment.yaml");
    }

    private static void gitlabCi(String yamlName) {
        Properties props1 = PropertiesUtil.loadYaml(yamlName);
        Properties properties = new Properties();
        Set<Map.Entry<Object, Object>> entries = props1.entrySet();
        StringBuilder deployScript = new StringBuilder("\n");
        StringBuilder sedd = new StringBuilder();
        for (Map.Entry<Object, Object> it : entries) {
            Object key = it.getKey();
            String string = key.toString();
            String replace = string.replaceAll("[.-]", "_");
            String upperCase = replace.toUpperCase();
            deployScript.append("    export ").append(upperCase).append("=").append(it.getValue()).append("\n");
            sedd.append("    sed -i ").append("\"s|__").append(upperCase).append("__|$").append(upperCase).append("|g\" changed-values.yaml").append("\n");
        }
        properties.setProperty("deploy to k8s.script", deployScript.append(sedd).toString());
        PropertiesUtil.write2Yaml(properties, ".gitlab-ci.yml");
    }

    private static void applicationDocker (String yamlName)     {
        Properties props1 = PropertiesUtil.loadYaml(yamlName);
        Set<Map.Entry<Object, Object>> entries = props1.entrySet();
        for (Map.Entry<Object, Object> it : entries) {
            Object key = it.getKey();
            String string = key.toString();
            String replace = string.replaceAll("[.-]", "_");
            String upperCase = replace.toUpperCase();
            it.setValue("${" + upperCase + "}");
        }
        PropertiesUtil.write2Yaml(props1, "application-docker.yml");
    }
    private static void applicationProd (String yamlName)     {
        Properties props1 = PropertiesUtil.loadYaml(yamlName);
        Set<Map.Entry<Object, Object>> entries = props1.entrySet();
        for (Map.Entry<Object, Object> it : entries) {
            Object key = it.getKey();
            String string = key.toString();
            String replace = string.replaceAll("[.-]", "_");
            String upperCase = replace.toUpperCase();
            it.setValue("${" + upperCase + "}");
        }
        PropertiesUtil.write2Yaml(props1, "application-prod.yml");
    }

    public static void main(String[] args) {
        changeValues("application.yml");
        values("application.yml");
        configMap("application.yml");
        deployment("application.yml");
        gitlabCi("application.yml");
        applicationProd("application.yml");
        applicationDocker("application.yml");
    }
}