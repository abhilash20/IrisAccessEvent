package com.data.projectiris;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties({AmadeusProperties.class,IrisProperties.class})
public class ProjectIrisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectIrisApplication.class, args);
    }

}