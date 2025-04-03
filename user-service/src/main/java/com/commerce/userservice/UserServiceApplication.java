package com.commerce.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {
    "com.commerce.userservice",         // user-service 패키지
    "com.commerce.common"               // common 모듈 패키지
})
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {


        ConfigurableApplicationContext context = SpringApplication.run(UserServiceApplication.class, args);


        // Environment 객체를 통해 test.message 프로퍼티 값을 읽어옵니다.
        String testMessage = context.getEnvironment().getProperty("api.apigateway", "기본 메시지");
        System.out.println("Config Server에서 가져온 메시지: " + testMessage);

    }

}
