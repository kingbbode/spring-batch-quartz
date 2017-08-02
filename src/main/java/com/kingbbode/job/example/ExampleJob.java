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

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class ExampleJob extends QuartzJobBean{
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Quartz Job Execute!");
    }
}
