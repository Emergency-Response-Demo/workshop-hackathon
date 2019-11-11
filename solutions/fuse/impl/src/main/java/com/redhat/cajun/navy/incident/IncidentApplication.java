package com.redhat.cajun.navy.incident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.camel.component.properties.PropertiesComponent;
import org.springframework.context.annotation.Bean;

/**
 * Created by martins_rh on 05/04/2019.
 */

@SpringBootApplication
public class IncidentApplication {
    public static void main(String[] args){
        SpringApplication.run(IncidentApplication.class, args);
    }

    @Bean
    public PropertiesComponent beerService() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("application.properties");
        return pc;
    }
}
