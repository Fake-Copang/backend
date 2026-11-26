
package com.example.fake_coupang.wallet;

public record WalletResponse(
        Long balance,
        boolean checkedInToday
) {
}
