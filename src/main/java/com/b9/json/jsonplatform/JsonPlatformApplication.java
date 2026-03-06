package com.b9.json.jsonplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class JsonPlatformApplication {

    public static void main(String[] args) {
        String envDir = new java.io.File("backend/.env").exists() ? "./backend" : ".";
        Dotenv dotenv = Dotenv.configure()
                .directory(envDir)
                .ignoreIfMissing()
                .load();

        setIfPresent("spring.datasource.url", dotenv.get("DB_URL", null));
        setIfPresent("spring.datasource.username", dotenv.get("DB_USERNAME", null));
        setIfPresent("spring.datasource.password", dotenv.get("DB_PASSWORD", null));

        SpringApplication.run(JsonPlatformApplication.class, args);
    }

    private static void setIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }
}
