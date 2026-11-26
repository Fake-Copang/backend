package com.example.fake_coupang.wallet;

import com.example.fake_coupang.user.User;
import com.example.fake_coupang.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class WalletService {

    private static final long CHECK_IN_REWARD = 100_000L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;

    public WalletService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long userId) {
        User user = findUser(userId);
        return toResponse(user, LocalDate.now(KST));
    }

    @Transactional
    public WalletResponse checkIn(Long userId) {
        validateUserId(userId);

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        LocalDateTime now = LocalDateTime.now(KST);

        if (user.hasCheckedInToday(now)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "오늘은 이미 출석 보상을 받았습니다."
            );
        }

        try {
            user.claimDailyCheckInReward(now, CHECK_IN_REWARD);
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "잔액 범위를 초과했습니다."
            );
        }

        return toResponse(user, now.toLocalDate());
    }

    private User findUser(Long userId) {
        validateUserId(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "잘못된 사용자 ID입니다."
            );
        }
    }

    private WalletResponse toResponse(User user, LocalDate today) {
        LocalDateTime lastCheckInAt = user.getLastCheckInAt();
        boolean checkedInToday = lastCheckInAt != null && lastCheckInAt.toLocalDate().isEqual(today);
        return new WalletResponse(
                user.getVirtualMoney(),
                checkedInToday
        );
    }
}