package com.example.bookingapp.API;

import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.*;
import com.example.bookingapp.Services.WalletService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class WalletApi {

    @Autowired
    WalletService walletService;

    // 1. LẤY DANH SÁCH VÍ CỦA USER ĐANG ĐĂNG NHẬP
    @GetMapping("/api/wallet/my-wallets")
    public ResponseEntity <WalletResponse> getMyWallets(
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getMyWallets(authentication)
        );
    }

    // 2. LẤY THÔNG TIN VÍ THEO ID
    @GetMapping("/api/wallet/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable String walletId,
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getWallet(
                        walletId,
                        authentication
                )
        );
    }

    // 3. THIẾT LẬP MÃ PIN LẦN ĐẦU
    @PostMapping("/api/wallet/{walletId}/pin")
    public ResponseEntity<Map<String, String>> createPin(
            @PathVariable String walletId,
            @Valid @RequestBody WalletPinRequest request,
            Authentication authentication) {

        String message = walletService.createPin(
                walletId,
                request,
                authentication
        );

        return ResponseEntity.ok(
                Map.of("message", message)
        );
    }

    // 4. ĐỔI MÃ PIN
    @PutMapping("/api/wallet/{walletId}/pin")
    public ResponseEntity<Map<String, String>> changePin(
            @PathVariable String walletId,
            @Valid @RequestBody ChangeWalletPinRequest request,
            Authentication authentication) {

        String message = walletService.changePin(
                walletId,
                request,
                authentication
        );

        return ResponseEntity.ok(
                Map.of("message", message)
        );
    }

    // 5. TRA CỨU TÀI KHOẢN NGÂN HÀNG THẬT
    @PostMapping("/api/wallet/{walletId}/bank/lookup")
    public ResponseEntity<BankAccountResponse> lookupBankAccount(
            @PathVariable String walletId,
            @Valid @RequestBody BankLinkRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.lookupBankAccount(
                        walletId,
                        request,
                        authentication
                )
        );
    }

    // 6. THÊM TÀI KHOẢN NGÂN HÀNG VÀO VÍ
    @PostMapping("/api/wallet/{walletId}/bank/link")
    public ResponseEntity<BankAccountResponse> addBank(
            @PathVariable String walletId,
            @Valid @RequestBody BankLinkRequest request,
            Authentication authentication) {

        BankAccountResponse response = walletService.addBank(
                walletId,
                request,
                authentication
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // 7. LẤY DANH SÁCH NGÂN HÀNG ĐÃ LIÊN KẾT
    @GetMapping("/api/wallet/{walletId}/banks")
    public ResponseEntity<List<BankAccountResponse>> getLinkedBanks(
            @PathVariable String walletId,
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getLinkedBanks(
                        walletId,
                        authentication
                )
        );
    }

    // 8. HỦY LIÊN KẾT NGÂN HÀNG
    @DeleteMapping("/api/wallet/{walletId}/banks/{linkedBankId}")
    public ResponseEntity<Map<String, String>> removeBank(
            @PathVariable String walletId,
            @PathVariable String linkedBankId,
            Authentication authentication) {

        String message = walletService.removeBank(
                walletId,
                linkedBankId,
                authentication
        );

        return ResponseEntity.ok(
                Map.of("message", message)
        );
    }

    // 9. TẠO YÊU CẦU NẠP TIỀN
    @PostMapping("/api/wallet/{walletId}/deposit")
    public ResponseEntity<DepositResponse> createDeposit(
            @PathVariable String walletId,
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        DepositResponse response = walletService.createDeposit(
                walletId,
                request,
                authentication
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }


    // 10. TẠO YÊU CẦU RÚT TIỀN
    @PostMapping("/api/wallet/{walletId}/withdraw")
    public ResponseEntity<WithdrawResponse> createWithdraw(
            @PathVariable String walletId,
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication) {

        WithdrawResponse response = walletService.createWithdraw(
                walletId,
                request,
                authentication
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }


    // 10. LẤY LỊCH SỬ GIAO DỊCH
    @GetMapping("/api/wallet/{walletId}/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(
            @PathVariable String walletId,
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getTransactions(
                        walletId,
                        authentication
                )
        );
    }

    @PostMapping("/api/wallet/payment/code")
    public ResponseEntity<Object> checkPaymentCode(@RequestParam String paymentCode, @RequestParam String walletId, Authentication authentication) {
        boolean result = walletService.verifyPin(walletId, paymentCode, authentication);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
