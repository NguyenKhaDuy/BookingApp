package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InvoicesDTO {
    private String id_invoices;
    private Float total_amount;
    private List<DetailInvoiceDTO> detailInvoiceDTOS = new ArrayList<>();
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate paid_at;
    private String payment_method;
    private String name_status;
}
