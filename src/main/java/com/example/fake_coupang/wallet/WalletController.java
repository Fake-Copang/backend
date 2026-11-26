
package com.example.fake_coupang.wallet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                walletService.getWallet(userId)
        );
    }

    @PostMapping("/check-in")
    public ResponseEntity<WalletResponse> checkIn(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                walletService.checkIn(userId)
        );
    }
}
