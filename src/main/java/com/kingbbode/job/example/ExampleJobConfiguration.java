package com.kingbbode.job.example; 
/*
 * Created By Kingbbode
 * blog : http://kingbbode.github.io
 * github : http://github.com/kingbbode
 * 
 * Author                    Date                     Description
 * ------------------       --------------            ------------------
 * kingbbode                2017-08-02      
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
public class ExampleJobConfiguration {
    @Bean
    public CronTriggerFactoryBean exampleJob1Trigger() {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(exampleJob1Schedule().getObject());
        cronTriggerFactoryBean.setCronExpression("0 0/1 * 1/1 * ? *");
        return cronTriggerFactoryBean;
    }

    @Bean
    public JobDetailFactoryBean exampleJob1Schedule() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(ExampleJob.class);
        jobDetailFactory.setDurability(true);
        jobDetailFactory.setRequestsRecovery(true);
        return jobDetailFactory;
    }
}
