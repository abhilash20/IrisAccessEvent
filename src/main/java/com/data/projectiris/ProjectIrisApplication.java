package com.data.projectiris;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRetry

public class ProjectIrisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectIrisApplication.class, args);
    }

}