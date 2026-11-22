
package com.example.fake_coupang.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "virtual_money", nullable = false)
    private Long virtualMoney = 0L;

    @Column(name = "last_check_in_at")
    private LocalDateTime lastCheckInAt;
}
