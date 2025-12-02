package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PaymentmethodRequest {
    private Long id_method;
    private String name_method;
    private String provider;
    private MultipartFile iconBase64;
}
