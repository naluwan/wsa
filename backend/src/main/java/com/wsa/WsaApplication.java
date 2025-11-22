package com.wsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WSA 應用程式主類別
 * Spring Boot 應用程式的進入點
 */
@SpringBootApplication
public class WsaApplication {

    /**
     * 應用程式主程式
     * 啟動 Spring Boot 應用程式
     *
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        SpringApplication.run(WsaApplication.class, args);
    }
}
