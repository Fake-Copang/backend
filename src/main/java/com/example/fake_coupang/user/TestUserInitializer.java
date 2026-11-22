
package com.example.fake_coupang.user;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class TestUserInitializer {

    @Bean
    public ApplicationRunner initializeTestUser(
            JdbcTemplate jdbcTemplate
    ) {
        return args -> {
            jdbcTemplate.update("""
                INSERT INTO users (
                    id,
                    virtual_money,
                    last_check_in_at
                )
                VALUES (1, 100000, NULL)
                ON CONFLICT (id) DO NOTHING
                """);

            jdbcTemplate.execute("""
                SELECT setval(
                    pg_get_serial_sequence('users', 'id'),
                    COALESCE((SELECT MAX(id) FROM users), 1),
                    true
                )
                """);
        };
    }
}
