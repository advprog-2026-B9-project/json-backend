package com.b9.json.jsonplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JsonPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testMain() {
        JsonPlatformApplication.main(new String[] {});
    }
}
