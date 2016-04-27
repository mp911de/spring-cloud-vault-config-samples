package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class SpringBootVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootVaultApplication.class, args);
    }

    @Configuration
    static class Config {

        @Value("${key1}")
        String key1;

        @Bean
        CommandLineRunner commandLineRunner() {
            return new CommandLineRunner() {
                @Override
                public void run(String... strings) throws Exception {
                    System.out.println("##########################");
                    System.out.println(key1);
					System.out.println("##########################");
                }
            };
        }
    }
}
