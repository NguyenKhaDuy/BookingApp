package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Request.InvoiceRequest;
import org.springframework.stereotype.Service;

@Service
public interface InvoicesService {
    Object createInvoice(InvoiceRequest invoiceRequest);
}
