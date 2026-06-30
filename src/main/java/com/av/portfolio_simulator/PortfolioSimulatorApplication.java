package com.av.portfolio_simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PortfolioSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioSimulatorApplication.class, args);
	}

}
