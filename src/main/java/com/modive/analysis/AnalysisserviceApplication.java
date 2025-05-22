package com.modive.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.modive.analysis")
@SpringBootApplication
public class AnalysisserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalysisserviceApplication.class, args);
	}

}
