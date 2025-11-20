package com.example.bookingapp.Models.Request;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.DetailInvoicesEntity;
import com.example.bookingapp.Entity.PaymentMethodEntity;
import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Models.DTO.DetailInvoiceDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InvoiceRequest {
    private Long payment_method_id;
    private String customer_id;
    private Long request_id;
    private List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
}
