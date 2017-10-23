package com.kingbbode.provider;

import com.kingbbode.properties.QuartzProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by YG-MAC on 2017. 10. 23..
 */
@Component
@EnableConfigurationProperties(QuartzProperties.class)
public class BatchNameProvider {

    @Autowired
    private QuartzProperties quartzProperties;

    
}
