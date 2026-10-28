package com.brandguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BrandGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrandGuardApplication.class, args);
    }
}
