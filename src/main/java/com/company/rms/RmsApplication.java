package com.company.rms;

import jakarta.annotation.PostConstruct; // [MỚI] Thêm import này
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone; // [MỚI] Thêm import này

@SpringBootApplication
@EnableJpaAuditing
public class RmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RmsApplication.class, args);
    }

    // [MỚI] Thiết lập múi giờ mặc định là Việt Nam (UTC+7)
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
}