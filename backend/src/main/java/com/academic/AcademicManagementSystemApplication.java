package com.academic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 学术活动与科研成果管理系统启动类
 *
 * @author Academic Team
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.academic.mapper")
@EnableScheduling
public class AcademicManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademicManagementSystemApplication.class, args);
        System.out.println("启动成功！");
    }
}
