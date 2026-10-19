package com.taiwei.reggie.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

/**
 * Base class of the integration tests. Runs the whole application (real MySQL with the Flyway schema, real Redis)
 * in Testcontainers; both containers are started once and shared by every test class.
 *
 * <p>Needs a running Docker daemon. These tests are named {@code *IT} and run in {@code mvn verify}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // root user: Flyway's MySQL support reads performance_schema.user_variables_by_thread, which unprivileged
    // users may not select from (docker-compose grants that explicitly, see docker/mysql-init).
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("reggie").withUsername("root").withPassword("test");

    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static {
        Startables.deepStart(MYSQL, REDIS).join();
    }

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected JdbcTemplate jdbc;
    @Autowired
    protected StringRedisTemplate redis;

    /** Every test starts from an empty catalogue / no customers; the bootstrap admin is kept. */
    @BeforeEach
    void cleanDatabase() {
        for (String table : new String[]{"order_detail", "orders", "shopping_cart", "address_book", "`user`",
                "setmeal_dish", "setmeal", "dish_flavor", "dish", "category"}) {
            jdbc.update("DELETE FROM " + table);
        }
        jdbc.update("DELETE FROM employee WHERE username <> 'admin'");
        Set<String> keys = redis.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    protected ApiClient anonymous() {
        return new ApiClient(mockMvc, objectMapper);
    }

    protected ApiClient adminClient() {
        ApiClient client = anonymous();
        client.post("/employee/login", java.util.Map.of("username", "admin", "password", "Adm1n-for-tests"))
                .assertSuccess();
        return client;
    }

    /** A customer that has completed the phone + code login. */
    protected ApiClient customerClient(String phone) {
        ApiClient client = anonymous();
        String code = client.post("/user/sendMsg", java.util.Map.of("phone", phone))
                .json().path("map").path("devCode").asText();
        client.post("/user/login", java.util.Map.of("phone", phone, "code", code)).assertSuccess();
        return client;
    }
}
