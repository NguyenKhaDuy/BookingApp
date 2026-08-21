package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticTechnicianDTO {
    private Long month;
    private Long requestIncompleted;
    private Long requestReceiving;
    private Long requestCompleted;
    private Long requestReceived;
    private Long invoiceUnpaid;
    private Long invoicePaid;
}
