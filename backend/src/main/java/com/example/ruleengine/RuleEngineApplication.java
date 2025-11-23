package com.example.ruleengine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 规则引擎应用启动类
 *
 * @author system
 * @date 2025-01-01
 */
@SpringBootApplication
@MapperScan("com.example.ruleengine.mapper")
@EnableCaching
@EnableAsync
public class RuleEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuleEngineApplication.class, args);
        System.out.println("\n----------------------------------------------------------");
        System.out.println("规则引擎系统启动成功!");
        System.out.println("API文档地址: http://localhost:8080/api/swagger-ui.html");
        System.out.println("----------------------------------------------------------\n");
    }

}
