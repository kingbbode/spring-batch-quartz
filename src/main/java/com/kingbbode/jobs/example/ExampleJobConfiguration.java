package com.kingbbode.jobs.example;
/*
 * Created By Kingbbode
 * blog : http://kingbbode.github.io
 * github : http://github.com/kingbbode
 * 
 * Author                    Date                     Description
 * ------------------       --------------            ------------------
 * kingbbode                2017-08-02      
 */

import com.kingbbode.jobs.BatchHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Configuration
public class ExampleJobConfiguration {
    private static final String JOB_NAME = "exampleJob";
    private static final String STEP_NAME = "exampleJobStep";

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public ExampleJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public CronTriggerFactoryBean exampleJob1Trigger() {
        return BatchHelper.cronTriggerFactoryBeanBuilder()
                .cronExpression("0 0/1 * 1/1 * ? *")
                .jobDetailFactoryBean(exampleJob1Schedule())
                .build();
    }

    @Bean
    public JobDetailFactoryBean exampleJob1Schedule() {
        return BatchHelper.jobDetailFactoryBeanBuilder()
                .job(exampleJob1())
                .build();
    }
    
    @Bean
    public Job exampleJob1() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(exampleJob1Step())
                .build();
    }
    
    private Step exampleJob1Step() {
        return stepBuilderFactory.get(STEP_NAME)
                .<String, String>chunk(2)
                .reader(exampleJob1Reader())
                .processor(exampleJob1Processor())
                .writer(exampleJob1Writer())
                .build();
    }
    
    @Bean
    @StepScope
    public ItemReader<String> exampleJob1Reader() {
        return new ItemReader<String>() {
            private List<String> sampleData;
            private int count;

            @Override
            public String read() throws Exception {
                fetch();
                return next();
            }

            private String next() {
                if (this.count >= this.sampleData.size()) {
                    return null;
                }
                return this.sampleData.get(count++);
            }

            private void fetch() {
                if(isInitialized()){
                    return;
                }
                this.sampleData = IntStream.range(0, 20).boxed().map(String::valueOf).map(s -> s + "-read").collect(Collectors.toList());
            }

            private boolean isInitialized() {
                return this.sampleData != null;
            }
        };
    }
    
    private ItemProcessor<String, String> exampleJob1Processor() {
        return item -> item + "-processing";
    }

    private ItemWriter<String> exampleJob1Writer() {
        return items -> items.stream().map(o -> "[" + JOB_NAME + "] " + o + "-write").forEach(log::info);
    }
}
