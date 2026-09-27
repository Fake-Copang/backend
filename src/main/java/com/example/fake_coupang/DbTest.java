package com.example.fake_coupang;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbTest implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT 1",
                Integer.class
        );

        System.out.println("DB 연결 테스트 결과: " + result);
    }
}