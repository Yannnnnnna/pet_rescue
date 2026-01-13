package com.wei.pet.pet_rescue;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.wei.pet.pet_rescue.mapper")
@EnableTransactionManagement // 建议加上：开启事务支持
@EnableAsync // 建议加上：开启异步支持
public class PetRescueApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetRescueApplication.class, args);
    }

}
