package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterCustomerRequest {
    //id được random
    //updatedat createdat
    //avatar không cần set sau này giao diện sẽ cho mặc định
    //giao diện kiểm tra người dùng xem nếu avatar = null thì cho ảnh mặc định
    //set role mặc định là customer
    private String full_name;
    private String address;
    private String phone_number;
    private String email;
    private String password; // mã hóa
    private LocalDate dob;
    private String gender;
}
