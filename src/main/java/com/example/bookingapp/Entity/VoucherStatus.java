package com.example.bookingapp.Entity;

public enum VoucherStatus {
    AVAILABLE,  // Có thể sử dụng
    RESERVED,   // Đang được giữ để thanh toán
    USED,       // Đã sử dụng
    EXPIRED,    // Đã hết hạn
    REVOKED,     // Đã thu hồi
    APPLIED,    // Thanh toán thành công, đã áp dụng
    CANCELED    // Thanh toán thất bại hoặc bị hủy
}
