package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRquest {
    private Long id_request;
    private Long id_status;
}
