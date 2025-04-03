package com.commerce.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
	"com.commerce.cartservice",         	// cartservice 패키지
	"com.commerce.common"               	// common 모듈 패키지
})
@EnableDiscoveryClient // eureka
@EnableFeignClients // feign client
public class CartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartServiceApplication.class, args);
	}

}
