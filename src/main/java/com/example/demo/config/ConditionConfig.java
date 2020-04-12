package com.example.demo.config;

import com.example.demo.compent.LinuxCondition;
import com.example.demo.compent.WindowsCondition;
import com.example.demo.service.ListService;
import com.example.demo.service.impl.LinuxListService;
import com.example.demo.service.impl.WindowsListService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionConfig {

    @Bean
    @Conditional(WindowsCondition.class)
    public ListService windowsListService(){
        return new WindowsListService();
    }

    @Bean
    @Conditional(LinuxCondition.class)
    public  ListService linuxListService(){
        return new LinuxListService();
    }

}
