package net.xzh.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UaaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UaaServerApplication.class, args);
    }
    //new BCryptPasswordEncoder().encode("123321")
}

