
package com.example.fake_coupang.wallet;

import com.example.fake_coupang.user.User;
import com.example.fake_coupang.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WalletService {

    private static final long CHECK_IN_REWARD = 100_000L;

    private final UserRepository userRepository;

    public WalletService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long userId) {

        User user = findUser(userId);

        return toResponse(user);
    }

    @Transactional
    public WalletResponse checkIn(Long userId) {

        validateUserId(userId);

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        if (isCheckedInToday(user)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "오늘은 이미 출석 보상을 받았습니다."
            );
        }

        try {
            user.setVirtualMoney(
                    Math.addExact(
                            user.getVirtualMoney(),
                            CHECK_IN_REWARD
                    )
            );
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "잔액 범위를 초과했습니다."
            );
        }

        user.setLastCheckInAt(LocalDateTime.now());

        userRepository.save(user);

        return toResponse(user);
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

    private boolean isCheckedInToday(User user) {

        LocalDateTime lastCheckInAt =
                user.getLastCheckInAt();

        if (lastCheckInAt == null) {
            return false;
        }

        return lastCheckInAt.toLocalDate()
                .isEqual(LocalDate.now());
    }

    private WalletResponse toResponse(User user) {

        return new WalletResponse(
                user.getVirtualMoney(),
                isCheckedInToday(user)
        );
    }
}
