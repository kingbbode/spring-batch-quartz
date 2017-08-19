package com.kingbbode.config;

/*
 * Created By Kingbbode
 * blog : http://kingbbode.github.io
 * github : http://github.com/kingbbode
 * 
 * Author                    Date                     Description
 * ------------------       --------------            ------------------
 * kingbbode                2017-08-02      
 */

import com.kingbbode.properties.QuartzProperties;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableConfigurationProperties(QuartzProperties.class)
@EnableBatchProcessing
public class BatchConfiguration {
    /**
     * JobRegistry 에 Job 을 자동으로 등록하기 위한 설정.
     *
     * @param jobRegistry ths Spring Batch Job Registry
     * @return JobRegistry BeanPostProcessor
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
    
    /**
     * Quartz Schedule Job 에 의존성 주입
     * 
     * @param beanFactory application context beanFactory
     * @return the job factory
     */
    @Bean
    public JobFactory jobFactory(AutowireCapableBeanFactory beanFactory) {
        return new SpringBeanJobFactory(){
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                beanFactory.autowireBean(job);
                return job;
            }
        };
    }
    
    /**
     * Scheduler 전체를 관리하는 Manager.
     *
     * @param datasource Spring datasource
     * @param quartzProperties quartz config    
     * @return the scheduler factory bean
     * @throws Exception the exception
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource datasource, QuartzProperties quartzProperties, JobFactory jobFactory, Trigger[] registryTrigger) throws Exception {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setSchedulerName("SampleProject-0.0.1");
        //Register JobFactory
        factory.setJobFactory(jobFactory);
        //Graceful Shutdown 을 위한 설정으로 Job 이 완료될 때까지 Shutdown 을 대기하는 설정
        factory.setWaitForJobsToCompleteOnShutdown(true);
        //Job Detail 데이터 Overwrite 유무
        factory.setOverwriteExistingJobs(true);
        //Register QuartzProperties
        factory.setQuartzProperties(quartzProperties.toProperties());
        //Schedule 관리를 Spring Datasource 에 위임
        factory.setDataSource(datasource);
        //Register Triggers
        factory.setTriggers(registryTrigger);

        return factory;
    }

    /**
     * Scheduler 에 Trigger 를 자동으로 등록하기 위한 설정.
     *
     * @return the trigger [ ]
     */
    @Bean
    public Trigger[] registryTrigger(List<CronTriggerFactoryBean> cronTriggerFactoryBeanList) {
        return cronTriggerFactoryBeanList.stream().map(CronTriggerFactoryBean::getObject).toArray(Trigger[]::new);
    }

    /**
     * Spring Framework 의 Shutdown Hook 설정.
     * Quartz 의 Shutdown 동작을 위임받아 Graceful Shutdown 을 보장.
     * Quartz 의 자체 Shutdown Plugin 을 사용하면 Spring 의 Datasource 가 먼저 Close 되므로,
     * Spring 에게 Shutdown 동작을 위임하여, 상위에서 컨트롤.
     *
     * @param schedulerFactoryBean quartz schedulerFactoryBean.
     * @return SmartLifecycle
     */
    
    @Bean
    public SmartLifecycle gracefulShutdownHookForQuartz(SchedulerFactoryBean schedulerFactoryBean) {
        return new SmartLifecycle() {
            private boolean isRunning = false;
            private final Logger logger = LoggerFactory.getLogger(this.getClass());
            @Override
            public boolean isAutoStartup() {
                return true;
            }

            @Override
            public void stop(Runnable callback) {
                stop();
                logger.info("Spring container is shutting down.");
                callback.run();
            }

            @Override
            public void start() {
                logger.info("Quartz Graceful Shutdown Hook started.");
                isRunning = true;
            }

            @Override
            public void stop() {
                isRunning = false;
                try {
                    logger.info("Quartz Graceful Shutdown... ");
                    schedulerFactoryBean.destroy();
                } catch (SchedulerException e) {
                    try {
                        logger.info(
                                "Error shutting down Quartz: " + e.getMessage(), e);
                        schedulerFactoryBean.getScheduler().shutdown(false);
                    } catch (SchedulerException ex) {
                        logger.error("Unable to shutdown the Quartz scheduler.", ex);
                    }
                }
            }

            @Override
            public boolean isRunning() {
                return isRunning;
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE;
            }
        };
    }
}
