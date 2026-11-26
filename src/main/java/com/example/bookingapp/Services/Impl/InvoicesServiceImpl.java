package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Models.Request.InvoiceRequest;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.InvoicesService;
import com.example.bookingapp.Services.WebSocketService;
import com.example.bookingapp.Utils.RandomIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InvoicesServiceImpl implements InvoicesService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    PaymentMethodRepository paymentMethodRepository;
    @Autowired
    InvoicesRepository invoicesRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    WalletTransactionRepository walletTransactionRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    WebSocketService webSocketService;
    @Autowired
    NotificationTypeRepository notificationTypeRepository;
    @Autowired
    CustomerVouchersRepository customerVouchersRepository;
    @Autowired
    private VouchersRepository vouchersRepository;

    @Override
    public void addVoucher(String idInvoice, String idVoucher) {
        InvoicesEntity invoices = invoicesRepository.findById(idInvoice).get();
        CustomerVouchersEntity customerVouchersEntity = customerVouchersRepository.findById(idVoucher).get();
        invoices.setCustomerVouchersEntity(customerVouchersEntity);
        invoicesRepository.save(invoices);
    }

    @Override
    public Object createInvoice(InvoiceRequest invoiceRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        CustomerEntity customerEntity = null;
        PaymentMethodEntity paymentMethodEntity = null;
        Double total_amont = (double)0;
        try {
            //Tìm kiếm request
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(invoiceRequest.getRequest_id()).get();
            //Tìm kiếm khách hàng
            try {
                customerEntity = customerRepository.findById(invoiceRequest.getCustomer_id()).get();
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found customer");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //Tìm kiếm phuương thức thanh toán
            StatusEntity statusEntity = null;
            try {
                paymentMethodEntity = paymentMethodRepository.findById(invoiceRequest.getPayment_method_id()).get();
                //nếu trả tiền mặt thì tính vào công nợ của thợ
                if (paymentMethodEntity.getCode().equals("COD")){
                    statusEntity = statusRepository.findByNameStatus("PAID");
                    TechnicianEntity technicianEntity = repairRequestEntity.getTechnicianEntity();
                    Double debt = (double)0;
                    //lấy ra phần tiền mà công ty hưởng
                    for (DetailInvoiceDTO detailInvoiceDTO : invoiceRequest.getDetailInvoiceDTOS()){
                        if (detailInvoiceDTO.getName().equals("Công thợ")){
                            debt = (double) ((detailInvoiceDTO.getTotal_price() * 20) / 100);
                        }
                    }
                    //cập nhật công nợ của thợ
                    if (debt > 0 ){
                        technicianEntity.setTechnician_debt(debt);
                        technicianRepository.save(technicianEntity);
                    }
                }else {
                    statusEntity = statusRepository.findByNameStatus("UNPAID");
                }
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found payment method");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //Tạo một hóa đơn mới
            InvoicesEntity invoicesEntity = new InvoicesEntity();
            //Random id cho hóa đơn
            invoicesEntity.setId_invoices(RandomIdUtils.generateRandomId());
            invoicesEntity.setCreated_at(LocalDateTime.now());
            invoicesEntity.setUpdated_at(LocalDateTime.now());
            invoicesEntity.setCustomerEntity(customerEntity);
            invoicesEntity.setPaymentMethodEntity(paymentMethodEntity);
            invoicesEntity.setRepairRequestEntity(repairRequestEntity);
            invoicesEntity.setPaidAt(LocalDate.now());
            invoicesEntity.setStatusEntity(statusEntity);
            for (DetailInvoiceDTO detailInvoiceDTO : invoiceRequest.getDetailInvoiceDTOS()) {
                DetailInvoicesEntity detailInvoicesEntity = new DetailInvoicesEntity();
                //tính tổng giá của toàn bộ hóa đơn
                total_amont += detailInvoiceDTO.getQuantity() * detailInvoiceDTO.getPrice();
                modelMapper.map(detailInvoiceDTO, detailInvoicesEntity);
                detailInvoicesEntity.setId_detail_invoice(RandomIdUtils.generateRandomId());
                detailInvoicesEntity.setInvoicesEntity(invoicesEntity);
                detailInvoicesEntity.setInvoicesEntity(invoicesEntity);
                //thêm vào trong list detail invoice
                invoicesEntity.getDetailInvoicesEntities().add(detailInvoicesEntity);
            }
            invoicesEntity.setTotal_amount(total_amont);
            //Lưu dữ liệu
            invoicesRepository.save(invoicesEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object updateStatusInvoice(String id_invoice) {

        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();

        try {

            InvoicesEntity invoicesEntity = invoicesRepository
                    .findById(id_invoice)
                    .orElseThrow(NoSuchElementException::new);

            // Kiểm tra hóa đơn đã thanh toán
            if (invoicesEntity.getStatusEntity() != null
                    && "PAID".equalsIgnoreCase(
                    invoicesEntity.getStatusEntity().getNameStatus())) {

                messageResponse.setHttpStatus(HttpStatus.OK);
                messageResponse.setMessage(
                        "Hóa đơn đã được thanh toán"
                );

                return messageResponse;
            }

            StatusEntity statusEntity =
                    statusRepository.findByNameStatus("PAID");

            if (statusEntity == null) {
                throw new RuntimeException(
                        "Không tìm thấy trạng thái PAID"
                );
            }

            invoicesEntity.setStatusEntity(statusEntity);

            invoicesEntity.setPaidAt(LocalDate.now());

            invoicesEntity.setUpdated_at(LocalDateTime.now());

            invoicesRepository.save(invoicesEntity);

            messageResponse.setHttpStatus(HttpStatus.OK);

            messageResponse.setMessage(
                    "Thanh toán hóa đơn thành công"
            );

            return messageResponse;

        } catch (NoSuchElementException ex) {

            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);

            errorDTO.setMessage("Không tìm thấy hóa đơn");

            return errorDTO;
        }
    }

    @Override
    public Page<InvoicesDTO> getInvoiceByCustomer(String customer_id, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            //Tìm kiếm khách hàng
            CustomerEntity customerEntity = customerRepository.findById(customer_id).get();
            Page<InvoicesEntity> invoicesEntities = invoicesRepository.findByCustomerEntity(customerEntity, pageable);
            List<InvoicesDTO> invoicesDTOS = new ArrayList<>();
            for (InvoicesEntity invoicesEntity : invoicesEntities) {
                InvoicesDTO invoicesDTO = new InvoicesDTO();
                invoicesDTO.setId_invoices(invoicesEntity.getId_invoices());
                invoicesDTO.setTotal_amount(invoicesEntity.getTotal_amount());
                invoicesDTO.setName_tech(invoicesEntity.getRepairRequestEntity().getTechnicianEntity().getFull_name());
                invoicesDTO.setPaid_at(invoicesEntity.getPaidAt());
                invoicesDTO.setName_status(invoicesEntity.getStatusEntity().getNameStatus());
                invoicesDTO.setPayment_method(invoicesEntity.getPaymentMethodEntity().getName_method());
                invoicesDTO.setCode(invoicesEntity.getPaymentMethodEntity().getCode());
                //lấy ra danh sách chi tiết của hóa đơn
                List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                    DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                    detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                    detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                    detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                    detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                    detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                    detailInvoiceDTOS.add(detailInvoiceDTO);
                }
                invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
                invoicesDTOS.add(invoicesDTO);
            }
            return new PageImpl<>(invoicesDTOS, invoicesEntities.getPageable(), invoicesEntities.getTotalElements());
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    @Override
    public Object getDetailInvoices(String id_invoice) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            InvoicesDTO invoicesDTO = new InvoicesDTO();
            InvoicesEntity invoicesEntity = invoicesRepository.findById(id_invoice).get();
            invoicesDTO.setId_invoices(invoicesEntity.getId_invoices());
            invoicesDTO.setTotal_amount(invoicesEntity.getTotal_amount());
            invoicesDTO.setName_status(invoicesEntity.getStatusEntity().getNameStatus());
            invoicesDTO.setName_tech(invoicesEntity.getRepairRequestEntity().getTechnicianEntity().getFull_name());
            invoicesDTO.setPaid_at(invoicesEntity.getPaidAt());
            invoicesDTO.setPayment_method(invoicesEntity.getPaymentMethodEntity().getName_method());
            invoicesDTO.setCode(invoicesEntity.getPaymentMethodEntity().getCode());
            //lấy ra danh sách chi tiết của hóa đơn
            List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                detailInvoiceDTO.setId_detail_invoice(detailInvoicesEntity.getId_detail_invoice());
                detailInvoiceDTO.setName(detailInvoicesEntity.getName());
                detailInvoiceDTO.setQuantity(detailInvoicesEntity.getQuantity());
                detailInvoiceDTO.setPrice(detailInvoicesEntity.getPrice());
                detailInvoiceDTO.setTotal_price(detailInvoicesEntity.getTotal_price());
                detailInvoiceDTOS.add(detailInvoiceDTO);
            }
            invoicesDTO.setDetailInvoiceDTOS(detailInvoiceDTOS);
            return invoicesDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found invoice");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object PaymentInvoice(String id_invoice) {

        MessageResponse messageResponse = new MessageResponse();

        // Kiểm tra ID hóa đơn
        if (id_invoice == null || id_invoice.isBlank()) {
            messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
            messageResponse.setMessage("ID hóa đơn không hợp lệ");
            return messageResponse;
        }

        // Lấy hóa đơn
        InvoicesEntity invoicesEntity = invoicesRepository
                .findById(id_invoice)
                .orElse(null);

        if (invoicesEntity == null) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            errorDTO.setMessage("Không tìm thấy hóa đơn");
            return errorDTO;
        }

        // Kiểm tra trạng thái hóa đơn
        if (invoicesEntity.getStatusEntity() != null
                && "PAID".equalsIgnoreCase(
                invoicesEntity.getStatusEntity().getNameStatus())) {

            messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
            messageResponse.setMessage("Hóa đơn đã được thanh toán");
            return messageResponse;
        }

        // Lấy khách hàng
        CustomerEntity customerEntity =
                invoicesEntity.getCustomerEntity();

        if (customerEntity == null) {
            throw new IllegalStateException(
                    "Không tìm thấy khách hàng"
            );
        }

        // Lấy ví khách hàng
        WalletEntity walletCustomer =
                walletRepository.findByUserEntity(customerEntity);

        if (walletCustomer == null) {
            throw new IllegalStateException(
                    "Khách hàng chưa có ví điện tử"
            );
        }

        // Lấy thợ kỹ thuật
        if (invoicesEntity.getRepairRequestEntity() == null) {
            throw new IllegalStateException(
                    "Không tìm thấy yêu cầu sửa chữa"
            );
        }

        TechnicianEntity technicianEntity =
                invoicesEntity.getRepairRequestEntity()
                        .getTechnicianEntity();

        if (technicianEntity == null) {
            throw new IllegalStateException(
                    "Không tìm thấy thợ kỹ thuật"
            );
        }

        // Lấy ví thợ
        WalletEntity walletTech =
                technicianEntity.getWalletEntity();

        if (walletTech == null) {
            throw new IllegalStateException(
                    "Thợ kỹ thuật chưa có ví điện tử"
            );
        }

        // TÍNH TỔNG TIỀN HÓA ĐƠN

        double totalAmount =
                invoicesEntity.getTotal_amount() == null
                        ? 0.0
                        : invoicesEntity.getTotal_amount();

        if (!Double.isFinite(totalAmount) || totalAmount <= 0) {
            throw new IllegalStateException(
                    "Tổng tiền hóa đơn không hợp lệ"
            );
        }

        // KIỂM TRA VÀ TÍNH TIỀN VOUCHER
        CustomerVouchersEntity customerVoucher =
                invoicesEntity.getCustomerVouchersEntity();

        double totalDiscount = 0.0;

        if (customerVoucher != null) {

            // Kiểm tra trạng thái voucher
            if (customerVoucher.getStatus()
                    != VoucherStatus.AVAILABLE) {

                messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
                messageResponse.setMessage(
                        "Voucher không thể sử dụng"
                );
                return messageResponse;
            }

            // Kiểm tra số lượt sử dụng
            long usageCount =
                    customerVoucher.getUsageCount() == null
                            ? 0L
                            : customerVoucher.getUsageCount();

            if (customerVoucher.getUsageLimit() == null
                    || usageCount >= customerVoucher.getUsageLimit()) {

                messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
                messageResponse.setMessage(
                        "Voucher đã hết lượt sử dụng"
                );
                return messageResponse;
            }

            if (customerVoucher.getVouchersEntity() == null
                    || customerVoucher.getVouchersEntity()
                    .getPromotionsEntity() == null) {

                throw new IllegalStateException(
                        "Không tìm thấy chương trình khuyến mãi"
                );
            }

            var promotion = customerVoucher
                    .getVouchersEntity()
                    .getPromotionsEntity();

            // Phần trăm giảm giá
            double discountValue =
                    promotion.getDiscountValue().doubleValue();

            if (!Double.isFinite(discountValue)
                    || discountValue <= 0
                    || discountValue > 100) {

                throw new IllegalStateException(
                        "Phần trăm giảm giá không hợp lệ"
                );
            }

            // Kiểm tra giá trị hóa đơn tối thiểu
            if (promotion.getMinOrderValue() != null
                    && totalAmount <
                    promotion.getMinOrderValue().doubleValue()) {

                messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
                messageResponse.setMessage(
                        "Hóa đơn chưa đủ giá trị để áp dụng voucher"
                );
                return messageResponse;
            }

            // Tính tiền giảm theo phần trăm
            totalDiscount = totalAmount * discountValue / 100.0;

            // Giới hạn số tiền giảm tối đa
            if (promotion.getMaxDiscount() != null) {

                double maxDiscount =
                        promotion.getMaxDiscount().doubleValue();

                if (!Double.isFinite(maxDiscount)
                        || maxDiscount < 0) {
                    throw new IllegalStateException(
                            "Mức giảm tối đa không hợp lệ"
                    );
                }

                totalDiscount = Math.min(
                        totalDiscount,
                        maxDiscount
                );
            }

            // Không được giảm vượt quá giá trị hóa đơn
            totalDiscount = Math.min(
                    totalDiscount,
                    totalAmount
            );

            // Làm tròn theo đơn vị VNĐ
            totalDiscount = Math.round(totalDiscount);
        }

        // TÍNH SỐ TIỀN KHÁCH PHẢI THANH TOÁN
        double customerPayAmount =
                totalAmount - totalDiscount;

        double balanceCustomer =
                walletCustomer.getBalance() == null
                        ? 0.0
                        : walletCustomer.getBalance();

        if (!Double.isFinite(balanceCustomer)
                || balanceCustomer < 0) {

            throw new IllegalStateException(
                    "Số dư ví khách hàng không hợp lệ"
            );
        }

        if (balanceCustomer < customerPayAmount) {

            messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);

            messageResponse.setMessage(
                    "Số dư không đủ để thanh toán"
            );

            return messageResponse;
        }

        // TÍNH TỔNG TIỀN CÔNG THỢ

        double laborCost = 0.0;

        for (DetailInvoicesEntity detail :
                invoicesEntity.getDetailInvoicesEntities()) {

            String name = detail.getName();

            if (name != null
                    && ("Công thợ".equalsIgnoreCase(name.trim())
                    || "Chi phí nhân công".equalsIgnoreCase(name.trim()))) {

                if (detail.getTotal_price() != null) {
                    laborCost += detail.getTotal_price();
                }
            }
        }

        if (!Double.isFinite(laborCost)
                || laborCost < 0
                || laborCost > totalAmount) {

            throw new IllegalStateException(
                    "Chi phí nhân công không hợp lệ"
            );
        }

        // TÍNH HOA HỒNG VÀ TIỀN THỢ NHẬN
        // KingTech thu 20% tiền công thợ
        double companyFee = Math.round(laborCost * 0.20);

        // Thợ nhận tiền dựa trên hóa đơn gốc
        double technicianCredit =
                totalAmount - companyFee;

        // Chi phí tài trợ voucher
        double promotionExpense = totalDiscount;

        // Doanh thu ròng KingTech
        double companyRevenue =
                companyFee - promotionExpense;

        // KIỂM TRA TRẠNG THÁI PAID
        StatusEntity paidStatus =
                statusRepository.findByNameStatus("PAID");

        if (paidStatus == null) {
            throw new IllegalStateException(
                    "Không tìm thấy trạng thái PAID"
            );
        }

        // Kiểm tra giao dịch trùng lặp
        String customerKey = "INVOICE_CUSTOMER_" + id_invoice;
        String technicianKey = "INVOICE_TECH_" + id_invoice;

        if (walletTransactionRepository
                .existsByIdempotencyKey(customerKey)
                || walletTransactionRepository
                .existsByIdempotencyKey(technicianKey)) {

            messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
            messageResponse.setMessage(
                    "Giao dịch hóa đơn đã được xử lý"
            );
            return messageResponse;
        }

        //TRỪ TIỀN VÍ KHÁCH HÀNG

        walletCustomer.setBalance(
                balanceCustomer - customerPayAmount
        );

        walletRepository.save(walletCustomer);

        // Lưu lịch sử giao dịch khách hàng
        if (customerPayAmount > 0) {

            WalletTransactionEntity customerTransaction =
                    new WalletTransactionEntity();

            customerTransaction.setType(
                    WalletTransactionType.WITHDRAW
            );

            customerTransaction.setAmount(customerPayAmount);

            customerTransaction.setCreatedAt(
                    LocalDateTime.now()
            );

            customerTransaction.setWallet(walletCustomer);

            customerTransaction.setStatus(
                    WalletTransactionStatus.SUCCESS
            );

            customerTransaction.setIdempotencyKey(customerKey);

            walletTransactionRepository.save(customerTransaction);
        }

        // CỘNG TIỀN VÀO VÍ THỢ
        double currentBalanceTech =
                walletTech.getBalance() == null
                        ? 0.0
                        : walletTech.getBalance();

        if (!Double.isFinite(currentBalanceTech)) {
            throw new IllegalStateException(
                    "Số dư ví thợ không hợp lệ"
            );
        }

        walletTech.setBalance(
                currentBalanceTech + technicianCredit
        );

        walletRepository.save(walletTech);

        // Lưu lịch sử giao dịch ví thợ
        WalletTransactionEntity transactionTech =
                new WalletTransactionEntity();

        transactionTech.setType(
                WalletTransactionType.DEPOSIT
        );

        transactionTech.setAmount(technicianCredit);

        transactionTech.setCreatedAt(
                LocalDateTime.now()
        );

        transactionTech.setWallet(walletTech);

        transactionTech.setStatus(
                WalletTransactionStatus.SUCCESS
        );

        transactionTech.setIdempotencyKey(technicianKey);

        walletTransactionRepository.save(transactionTech);

        // Không cộng công nợ vì đã trừ hoa hồng

        // CẬP NHẬT VOUCHER KHÁCH HÀNG
        if (customerVoucher != null) {

            long currentUsageCount =
                    customerVoucher.getUsageCount() == null
                            ? 0L
                            : customerVoucher.getUsageCount();

            long newUsageCount = currentUsageCount + 1L;

            customerVoucher.setUsageCount(newUsageCount);

            customerVoucher.setUsedAt(
                    LocalDateTime.now()
            );

            // Nếu đã sử dụng hết lượt
            if (newUsageCount >= customerVoucher.getUsageLimit()) {

                customerVoucher.setStatus(
                        VoucherStatus.USED
                );
            }

            customerVouchersRepository.save(customerVoucher);

            VouchersEntity vouchersEntity = customerVoucher.getVouchersEntity();
            vouchersEntity.setUsedCount(vouchersEntity.getUsedCount() + 1L);
            vouchersRepository.save(vouchersEntity);
        }

        // CẬP NHẬT HÓA ĐƠN
        invoicesEntity.setStatusEntity(paidStatus);

        invoicesEntity.setPaidAt(LocalDate.now());

        invoicesEntity.setUpdated_at(LocalDateTime.now());

        // Lưu số tiền giảm thực tế, không lưu phần trăm
        invoicesEntity.setTotal_discount(totalDiscount);
        invoicesRepository.save(invoicesEntity);

        System.out.println("ID hóa đơn: " + id_invoice);
        System.out.println("Tổng hóa đơn: " + totalAmount);
        System.out.println("Tiền giảm giá: " + totalDiscount);
        System.out.println("Khách thanh toán: " + customerPayAmount);
        System.out.println("Tiền công thợ: " + laborCost);
        System.out.println("Hoa hồng KingTech: " + companyFee);
        System.out.println("Thợ thực nhận: " + technicianCredit);
        System.out.println("Chi phí khuyến mãi: " + promotionExpense);
        System.out.println("Doanh thu ròng: " + companyRevenue);

        // GỬI THÔNG BÁO CHO THỢ
        MessageNotifiDTO messageNotifiDTO =
                new MessageNotifiDTO();

        messageNotifiDTO.setType("PAYMENT_SUCCESS");

        messageNotifiDTO.setTitle(
                "Đơn hàng đã được thanh toán"
        );

        messageNotifiDTO.setBody(
                "Đơn hàng "
                        + invoicesEntity.getRepairRequestEntity()
                        .getId_request()
                        + " đã được thanh toán qua KingTech Pay"
        );

        messageNotifiDTO.setDateTime(
                LocalDateTime.now()
        );

        saveNotificationTechnician(
                messageNotifiDTO,
                technicianEntity
        );

        // Gửi socket sau khi lưu dữ liệu thành công
        String technicianEmail = technicianEntity.getEmail();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        try {
                            webSocketService.sendPrivateUser(
                                    technicianEmail,
                                    messageNotifiDTO
                            );
                        } catch (Exception e) {
                            System.err.println(
                                    "Lỗi gửi thông báo: " + e.getMessage()
                            );
                        }
                    }
                }
        );

        // TRẢ VỀ KẾT QUẢ
        messageResponse.setHttpStatus(HttpStatus.OK);

        messageResponse.setMessage(
                "Thanh toán hóa đơn thành công"
        );

        return messageResponse;
    }

    public void saveNotificationTechnician(MessageNotifiDTO messageNotifiDTO, UserEntity userEntity){
        //tạo thông báo mới để lưu vào cơ sở dữ liệu
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(messageNotifiDTO.getType());

        NotificationsEntity notificationsEntity = new NotificationsEntity();
        notificationsEntity.setId_notify(RandomIdUtils.generateRandomId());
        notificationsEntity.setTitle(messageNotifiDTO.getTitle());
        notificationsEntity.setMessage(messageNotifiDTO.getBody());
        notificationsEntity.setNotificationTypeEntity(notificationTypeEntity);
        notificationsEntity.setUpdated_at(LocalDateTime.now());
        notificationsEntity.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notificationsEntity);

        NotificationUserEntity userNotify = new NotificationUserEntity();
        StatusEntity statusNotify = statusRepository.findByNameStatus("UNREAD");
        userNotify.setId_user_notify(RandomIdUtils.generateRandomId());
        userNotify.setStatusEntity(statusNotify);
        userNotify.setDateTime(LocalDateTime.now());
        userNotify.setUserEntity(userEntity);
        userNotify.setNotificationsEntity(notificationsEntity);

        //thêm vào notify
        notificationsEntity.getNotificationUserEntities().add(userNotify);
        //lưu vào cơ sở dữ liệu
        notificationRepository.save(notificationsEntity);
    }
}