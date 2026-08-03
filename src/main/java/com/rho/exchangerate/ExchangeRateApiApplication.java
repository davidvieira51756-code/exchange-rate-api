package com.rho.exchangerate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ExchangeRateApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExchangeRateApiApplication.class, args);
	}

}
