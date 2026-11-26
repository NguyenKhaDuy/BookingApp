package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.InvoicesDTO;
import com.example.bookingapp.Models.Request.InvoiceRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface InvoicesService {
    void addVoucher(String idInvoice, String idVoucher);
    Object createInvoice(InvoiceRequest invoiceRequest);
    Object updateStatusInvoice(String id_invoice);
    Page<InvoicesDTO> getInvoiceByCustomer(String customer_id, Integer pageNo);
    Object getDetailInvoices(String id_invoice);
    Object PaymentInvoice(String id_invoice);
}
