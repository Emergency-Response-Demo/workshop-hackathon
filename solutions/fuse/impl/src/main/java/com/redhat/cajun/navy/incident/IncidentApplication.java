package com.redhat.cajun.navy.incident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by martins_rh on 05/04/2019.
 */

@SpringBootApplication
public class IncidentApplication {
    public static void main(String[] args){
        SpringApplication.run(IncidentApplication.class, args);
    }
/*
    @Bean
    BeerService beerService() {
    	return new BeerService();
    }
*/
}
