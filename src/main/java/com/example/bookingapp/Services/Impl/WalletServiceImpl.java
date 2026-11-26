package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.*;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.BankAccountLookupService;
import com.example.bookingapp.Services.WalletService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    LinkBankAccountRepository linkBankAccountRepository;
    @Autowired
    WalletTransactionRepository walletTransactionRepository;
    @Autowired
    BankAccountLookupService bankAccountLookupService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    private UserRepository userRepository;

    //lấy user hiện tại đang đăng nhập
    private String getCurrentUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Vui lòng đăng nhập"
            );
        }

        if (!(authentication.getPrincipal() instanceof UserEntity user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Thông tin xác thực không hợp lệ"
            );
        }

        return user.getId_user();
    }

    //Kiểm tra quyền sở hữu ví
    private WalletEntity getOwnedWallet(
            String walletId,
            Authentication authentication) {

        String userId = getCurrentUserId(authentication);

        WalletEntity wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy ví"
                ));

        boolean userOwner =
                wallet.getUserEntity() != null
                        && userId.equals(
                        wallet.getUserEntity().getId_user()
                );

        if (!userOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền sử dụng ví này"
            );
        }

        return wallet;
    }

    private WalletResponse toWalletResponse(WalletEntity wallet) {
        boolean hasPin = wallet.getCode() != null
                && !wallet.getCode().isBlank();

        return new WalletResponse(
                wallet.getIdWallet(),
                wallet.getBalance(),
                hasPin
        );
    }


    //lấy tất cả ví của user
    @Override
    public WalletResponse getMyWallets(Authentication authentication) {
        String userId = getCurrentUserId(authentication);
        WalletEntity wallets = null;

        UserEntity userEntity = userRepository.findById(userId).get();

        if (userEntity == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy khách hàng hoặc kỹ thuật viên với ID: " + userId
            );
        }

        wallets = walletRepository.findByUserEntity(userEntity);


        return toWalletResponse(wallets);
    }

    @Override
    public WalletResponse getWallet(String walletId, Authentication authentication) {
        WalletEntity wallet =
                getOwnedWallet(walletId, authentication);

        return toWalletResponse(wallet);
    }

    private void validatePin(String pin) {

        if (pin == null || !pin.matches("\\d{6}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã PIN phải gồm đúng 6 chữ số"
            );
        }
    }

    @Override
    public String createPin(String walletId, WalletPinRequest request, Authentication authentication) {
        WalletEntity wallet =
                getOwnedWallet(walletId, authentication);

        if (wallet.getCode() != null
                && !wallet.getCode().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ví đã thiết lập mã PIN"
            );
        }

        validatePin(request.getPin());

        wallet.setCode(
                passwordEncoder.encode(request.getPin())
        );

        walletRepository.save(wallet);

        return "Thiết lập mã PIN thành công";
    }

    @Override
    public String changePin(String walletId, ChangeWalletPinRequest request, Authentication authentication) {
        WalletEntity wallet =
                getOwnedWallet(walletId, authentication);

        if (wallet.getCode() == null
                || wallet.getCode().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ví chưa thiết lập mã PIN"
            );
        }

        if (request.getOldPin() == null
                || !passwordEncoder.matches(
                request.getOldPin(),
                wallet.getCode())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã PIN cũ không chính xác"
            );
        }

        validatePin(request.getNewPin());

        if (passwordEncoder.matches(
                request.getNewPin(),
                wallet.getCode())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã PIN mới phải khác mã PIN cũ"
            );
        }

        wallet.setCode(
                passwordEncoder.encode(request.getNewPin())
        );

        walletRepository.save(wallet);

        return "Đổi mã PIN thành công";
    }

    @Override
    public boolean verifyPin(String walletId, String pin, Authentication authentication) {
        WalletEntity wallet =
                getOwnedWallet(walletId, authentication);

        return wallet.getCode() != null
                && !wallet.getCode().isBlank()
                && pin != null
                && passwordEncoder.matches(pin, wallet.getCode());
    }

    private void validateBankRequest(BankLinkRequest request) {

        if (request == null
                || request.getBankBin() == null
                || !request.getBankBin().matches("\\d{6}")
                || request.getBankCode() == null
                || request.getBankCode().isBlank()
                || request.getAccountNumber() == null
                || !request.getAccountNumber().matches("\\d{4,19}")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thông tin ngân hàng không hợp lệ"
            );
        }
    }


    @Override
    public BankAccountResponse lookupBankAccount(
            String walletId,
            BankLinkRequest request,
            Authentication authentication) {

        getOwnedWallet(walletId, authentication);
        validateBankRequest(request);

        String bankCode = request.getBankCode().trim();
        String accountNumber = request.getAccountNumber().trim();

        System.out.println(
                "[WALLET] Calling BankGateway lookup"
        );

        String accountName =
                bankAccountLookupService.lookupAccount(
                        bankCode,
                        accountNumber
                );

        return new BankAccountResponse(
                null,
                request.getBankBin(),
                bankCode,
                accountNumber,
                accountName,
                true,
                false
        );
    }


    private BankAccountResponse toBankResponse(
            LinkBankAccountEntity link) {

        return new BankAccountResponse(
                link.getId(),
                link.getBankBin(),
                link.getBankCode(),
                link.getAccountNumber(),
                link.getAccountName(),
                link.isLookupVerified(),
                link.isOwnershipVerified()
        );
    }

    @Override
    public BankAccountResponse addBank(String walletId, BankLinkRequest request, Authentication authentication) {
        WalletEntity wallet =
                getOwnedWallet(walletId, authentication);

        validateBankRequest(request);

        String bankBin = request.getBankBin();
        String accountNumber = request.getAccountNumber();

        // Tra cứu thông tin thật từ VietQR
        String accountName =
                bankAccountLookupService.lookupAccount(
                        request.getBankCode().trim(),
                        accountNumber
                );

        Optional<LinkBankAccountEntity> existing =
                linkBankAccountRepository
                        .findByWalletEntity_IdWalletAndBankBinAndAccountNumber(
                                walletId,
                                bankBin,
                                accountNumber
                        );

        LinkBankAccountEntity link;

        if (existing.isPresent()) {

            link = existing.get();

            if (link.getStatus() == BankLinkStatus.LINKED) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Tài khoản ngân hàng đã tồn tại trong ví"
                );
            }

        } else {
            link = new LinkBankAccountEntity();
        }

        link.setWalletEntity(wallet);
        link.setBankBin(bankBin);
        link.setBankCode(request.getBankCode());
        link.setAccountNumber(accountNumber);
        link.setAccountName(accountName);
        link.setLookupVerified(true);
        link.setOwnershipVerified(true);
        link.setStatus(BankLinkStatus.LINKED);
        link.setLinkedAt(LocalDateTime.now());

        LinkBankAccountEntity saved =
                linkBankAccountRepository.save(link);

        return toBankResponse(saved);
    }

    @Override
    public List<BankAccountResponse> getLinkedBanks(String walletId, Authentication authentication) {
        getOwnedWallet(walletId, authentication);

        return linkBankAccountRepository
                .findByWalletEntity_IdWalletAndStatus(
                        walletId,
                        BankLinkStatus.LINKED
                )
                .stream()
                .map(this::toBankResponse)
                .toList();
    }

    @Override
    public String removeBank(String walletId, String linkedBankId, Authentication authentication) {
        getOwnedWallet(walletId, authentication);

        LinkBankAccountEntity link =
                linkBankAccountRepository
                        .findByIdAndWalletEntity_IdWalletAndStatus(
                                linkedBankId,
                                walletId,
                                BankLinkStatus.LINKED
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy tài khoản ngân hàng"
                        ));

        link.setStatus(BankLinkStatus.UNLINKED);

        linkBankAccountRepository.save(link);

        return "Hủy lưu tài khoản ngân hàng thành công";
    }

    private DepositResponse toDepositResponse(
            WalletTransactionEntity transaction) {

        return new DepositResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                "Đã tạo yêu cầu nạp tiền, đã thanh toán"
        );
    }


    @Override
    @Transactional
    public DepositResponse createDeposit(
            String walletId,
            DepositRequest request,
            Authentication authentication
    ) {
        WalletEntity wallet = getOwnedWallet(walletId, authentication);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thiếu thông tin nạp tiền"
            );
        }

        Double amount = request.getAmount();

        if (amount == null
                || !Double.isFinite(amount)
                || amount < 10000
                || amount % 1 != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền phải từ 10.000 VNĐ trở lên và không có phần thập phân"
            );
        }

        String idempotencyKey = request.getIdempotencyKey();
        String bankAccountId = request.getBankAccountId();

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã giao dịch không được để trống"
            );
        }

        idempotencyKey = idempotencyKey.trim();

        if (bankAccountId == null || bankAccountId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn tài khoản ngân hàng nạp tiền"
            );
        }

        bankAccountId = bankAccountId.trim();

        // Kiểm tra giao dịch đã tồn tại
        Optional<WalletTransactionEntity> existing =
                walletTransactionRepository.findByIdempotencyKey(
                        idempotencyKey
                );

        if (existing.isPresent()) {
            WalletTransactionEntity old = existing.get();

            if (!old.getWallet().getIdWallet().equals(walletId)
                    || old.getType() != WalletTransactionType.DEPOSIT
                    || Double.compare(old.getAmount(), amount) != 0
                    || old.getBankAccount() == null
                    || !old.getBankAccount().getId().equals(bankAccountId)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Mã giao dịch đã được sử dụng cho yêu cầu khác"
                );
            }

            return toDepositResponse(old);
        }

        // Lấy ngân hàng liên kết
        LinkBankAccountEntity bankAccount =
                linkBankAccountRepository.findById(bankAccountId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy tài khoản ngân hàng"
                        ));

        // Kiểm tra ngân hàng thuộc ví hiện tại
        if (!bankAccount.getWalletEntity()
                .getIdWallet()
                .equals(walletId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản ngân hàng không thuộc ví của bạn"
            );
        }

        // Kiểm tra tài khoản đã xác minh
        if (!bankAccount.isOwnershipVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản ngân hàng chưa xác minh quyền sở hữu"
            );
        }

        // Tạo yêu cầu nạp tiền
        WalletTransactionEntity transaction =
                new WalletTransactionEntity();

        transaction.setWallet(wallet);
        transaction.setBankAccount(bankAccount);
        transaction.setType(WalletTransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setStatus(WalletTransactionStatus.SUCCESS);
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setCreatedAt(LocalDateTime.now());

        WalletTransactionEntity saved =
                walletTransactionRepository.save(transaction);

        WalletEntity walletEntity = walletRepository.findById(walletId).get();
        walletEntity.setBalance(walletEntity.getBalance() + amount);
        walletRepository.save(walletEntity);

        return toDepositResponse(saved);
    }




    @Override
    @Transactional
    public WithdrawResponse createWithdraw(
            String walletId,
            WithdrawRequest request,
            Authentication authentication
    ) {
        WalletEntity wallet = getOwnedWallet(walletId, authentication);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thiếu thông tin rút tiền"
            );
        }

        Double amount = request.getAmount();

        if (amount == null
                || !Double.isFinite(amount)
                || amount < 10000
                || amount % 1 != 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền rút phải từ 10.000 VNĐ trở lên và không có phần thập phân"
            );
        }

        String idempotencyKey = request.getIdempotencyKey();
        String bankAccountId = request.getBankAccountId();

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã giao dịch không được để trống"
            );
        }

        idempotencyKey = idempotencyKey.trim();

        if (bankAccountId == null || bankAccountId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn tài khoản ngân hàng nhận tiền"
            );
        }

        bankAccountId = bankAccountId.trim();

        // Kiểm tra mã giao dịch đã tồn tại
        Optional<WalletTransactionEntity> existing =
                walletTransactionRepository.findByIdempotencyKey(
                        idempotencyKey
                );

        if (existing.isPresent()) {
            WalletTransactionEntity old = existing.get();

            if (!old.getWallet().getIdWallet().equals(walletId)
                    || old.getType() != WalletTransactionType.WITHDRAW
                    || Double.compare(old.getAmount(), amount) != 0
                    || old.getBankAccount() == null
                    || !old.getBankAccount().getId().equals(bankAccountId)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Mã giao dịch đã được sử dụng cho yêu cầu khác"
                );
            }

            return toWithdrawResponse(old);
        }

        // Kiểm tra ngân hàng nhận tiền
        LinkBankAccountEntity bankAccount =
                linkBankAccountRepository.findById(bankAccountId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy tài khoản ngân hàng"
                        ));

        // Kiểm tra ngân hàng thuộc ví
        if (!bankAccount.getWalletEntity()
                .getIdWallet()
                .equals(walletId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản ngân hàng không thuộc ví của bạn"
            );
        }

        if (!bankAccount.isOwnershipVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản ngân hàng chưa xác minh quyền sở hữu"
            );
        }

        // Kiểm tra PIN đầu vào
        String pin = request.getPin();

        if (pin == null || !pin.matches("\\d{6}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã PIN phải gồm 6 chữ số"
            );
        }

        if (wallet.getBalance() == null
                || !Double.isFinite(wallet.getBalance())
                || wallet.getBalance() < amount) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số dư ví không đủ để thực hiện rút tiền"
            );
        }

        // Tạo yêu cầu rút tiền
        WalletTransactionEntity transaction =
                new WalletTransactionEntity();

        transaction.setWallet(wallet);
        transaction.setBankAccount(bankAccount);
        transaction.setType(WalletTransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setStatus(WalletTransactionStatus.SUCCESS);
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setCreatedAt(LocalDateTime.now());

        WalletTransactionEntity saved =
                walletTransactionRepository.save(transaction);

        WalletEntity walletEntity = walletRepository.findById(walletId).get();
        walletEntity.setBalance(walletEntity.getBalance() - amount);
        walletRepository.save(walletEntity);

        return toWithdrawResponse(saved);
    }



    private WithdrawResponse toWithdrawResponse(
            WalletTransactionEntity transaction
    ) {
        WithdrawResponse response = new WithdrawResponse();

        response.setTransactionId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setStatus(transaction.getStatus().name());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }



    @Override
    public List<WalletTransactionResponse> getTransactions(String walletId, Authentication authentication) {
        getOwnedWallet(walletId, authentication);

        return walletTransactionRepository
                .findByWallet_IdWalletOrderByCreatedAtDesc(walletId)
                .stream()
                .map(transaction ->
                        new WalletTransactionResponse(
                                transaction.getId(),
                                transaction.getType(),
                                transaction.getStatus(),
                                transaction.getAmount(),
                                transaction.getCreatedAt()
                        )
                )
                .toList();
    }
}
