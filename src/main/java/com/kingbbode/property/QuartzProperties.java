package com.kingbbode.property;

/*
 * Created By Kingbbode
 * blog : http://kingbbode.github.io
 * github : http://github.com/kingbbode
 * 
 * Author                    Date                     Description
 * ------------------       --------------            ------------------
 * kingbbode                2017-08-02      
 */

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

/**
 * Quartz 설정를 위한 Properties Class.
 */
@ConfigurationProperties(prefix = "org.quartz")
@Setter
@Getter
public class QuartzProperties {
    private static final String PREFIX = "org.quartz";
    
    private Scheduler scheduler;

    private JobStore jobStore;

    private ThreadPool threadPool;
    
    @Setter
    public static class Scheduler {
        private String instanceId;
        private String instanceName;
        private String makeSchedulerThreadDaemon;
        private String interruptJobsOnShutdown;
    }
    
    @Setter
    public static class JobStore {
        private String clusterCheckinInterval;
        private String driverDelegateClass;
        private String isClustered;
        private String misfireThreshold;
        private String tablePrefix;
        private String useProperties;
    }
    
    @Setter
    public static class ThreadPool {
        private String threadCount;
        private String makeThreadsDaemons;
    }
    
    public Properties toProperties() throws IllegalAccessException {
        Properties properties = new Properties();
        findProperties(PREFIX, this, properties);
        return properties;
    }
    
    private void findProperties(String prefix, Object object, Properties properties) {
        Arrays.stream(object.getClass().getDeclaredFields()).filter(field -> !field.getName().equals("PREFIX"))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        putStringProperties(prefix, object, properties, field);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void putStringProperties(String prefix, Object object, Properties properties, Field field) throws IllegalAccessException {
        if(String.class == field.getType()){
            properties.put(prefix + "." + field.getName(), field.get(object));
            return;
        }
        findProperties(prefix + "." + field.getName(), field.get(object), properties);
    }
}

