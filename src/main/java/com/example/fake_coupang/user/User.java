package com.example.fake_coupang.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public static final long INITIAL_VIRTUAL_MONEY = 100_000L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "virtual_money", nullable = false)
    private Long virtualMoney;

    @Column(name = "last_check_in_at")
    private LocalDateTime lastCheckInAt;

    @Builder
    private User(
            String email,
            String password,
            String nickname,
            Long virtualMoney,
            LocalDateTime lastCheckInAt
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.virtualMoney = (virtualMoney != null) ? virtualMoney : INITIAL_VIRTUAL_MONEY;
        this.lastCheckInAt = lastCheckInAt;
    }

    public static User create(String email, String encodedPassword, String nickname) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .virtualMoney(INITIAL_VIRTUAL_MONEY)
                .lastCheckInAt(null)
                .build();
    }

    public void claimDailyCheckInReward(LocalDateTime now, long rewardAmount) {
        if (rewardAmount <= 0) {
            throw new IllegalArgumentException("출석 보상은 0보다 커야 합니다.");
        }
        if (hasCheckedInToday(now)) {
            throw new IllegalStateException("오늘은 이미 출석체크 보상을 받았습니다.");
        }
        long currentMoney = (this.virtualMoney != null) ? this.virtualMoney : 0L;
        this.virtualMoney = Math.addExact(currentMoney, rewardAmount);
        this.lastCheckInAt = now;
    }

    public boolean hasCheckedInToday(LocalDateTime now) {
        if (this.lastCheckInAt == null) {
            return false;
        }
        return this.lastCheckInAt.toLocalDate().isEqual(now.toLocalDate());
    }

    public void deductVirtualMoney(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }
        long currentMoney = (this.virtualMoney != null) ? this.virtualMoney : 0L;
        if (currentMoney < amount) {
            throw new IllegalStateException("가상 화폐 잔액이 부족합니다.");
        }
        this.virtualMoney = Math.subtractExact(currentMoney, amount);
    }
}