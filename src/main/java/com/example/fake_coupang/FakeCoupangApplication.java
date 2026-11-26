
package com.example.fake_coupang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FakeCoupangApplication {

	public static void main(String[] args) {
		SpringApplication.run(
				FakeCoupangApplication.class,
				args
		);
	}
}
