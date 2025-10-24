package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailInvoiceDTO {
    private Long id_detail_invoice;
    private String name;
    private Integer quantity;
    private Float price;
    private Float total_price;
}
