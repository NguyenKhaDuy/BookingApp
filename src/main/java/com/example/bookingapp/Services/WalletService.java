package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WalletService {
    // Lấy danh sách ví của người dùng đang đăng nhập
    WalletResponse getMyWallets(
            Authentication authentication
    );

    // Lấy thông tin một ví
    WalletResponse getWallet(
            String walletId,
            Authentication authentication
    );

    // Thiết lập PIN lần đầu
    String createPin(
            String walletId,
            WalletPinRequest request,
            Authentication authentication
    );

    // Đổi PIN
    String changePin(
            String walletId,
            ChangeWalletPinRequest request,
            Authentication authentication
    );

    // Kiểm tra PIN
    boolean verifyPin(
            String walletId,
            String pin,
            Authentication authentication
    );

    // Tra cứu tài khoản ngân hàng thật
    BankAccountResponse lookupBankAccount(
            String walletId,
            BankLinkRequest request,
            Authentication authentication
    );

    // Lưu tài khoản ngân hàng
    BankAccountResponse addBank(
            String walletId,
            BankLinkRequest request,
            Authentication authentication
    );

    // Danh sách tài khoản ngân hàng đã lưu
    List<BankAccountResponse> getLinkedBanks(
            String walletId,
            Authentication authentication
    );

    // Hủy liên kết
    String removeBank(
            String walletId,
            String linkedBankId,
            Authentication authentication
    );

    // Tạo yêu cầu nạp tiền
    DepositResponse createDeposit(
            String walletId,
            DepositRequest request,
            Authentication authentication
    );

    // Tạo yêu cầu rút tiền
    WithdrawResponse createWithdraw(
            String walletId,
            WithdrawRequest request,
            Authentication authentication
    );

    // Lịch sử giao dịch
    List<WalletTransactionResponse> getTransactions(
            String walletId,
            Authentication authentication
    );
}
