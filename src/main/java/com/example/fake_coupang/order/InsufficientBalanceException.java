
package com.example.fake_coupang.order;

public class InsufficientBalanceException
        extends RuntimeException {

    public InsufficientBalanceException() {
        super("잔액이 부족합니다.");
    }
}
