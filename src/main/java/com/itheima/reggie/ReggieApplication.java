package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan//扫描过滤器的注解
@EnableTransactionManagement
@EnableCaching//开启spring cache注解方式的缓存功能
//springbootapplication标注这个类是spring-boot的应用
public class ReggieApplication {
    public static void main (String[] args){
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功");
    }
}
