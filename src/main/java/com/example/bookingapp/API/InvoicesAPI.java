package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.InvoicesDTO;
import com.example.bookingapp.Models.Request.InvoiceRequest;
import com.example.bookingapp.Services.InvoicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class InvoicesAPI {
    @Autowired
    InvoicesService invoicesService;

    @PostMapping(value = "/api/invoices/")
    public ResponseEntity<Object> createInvoices(@RequestBody InvoiceRequest invoiceRequest){
        Object result = invoicesService.createInvoice(invoiceRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/invoices/id={id_invoice}")
    public ResponseEntity<Object> updateStatusInvoices(@PathVariable String id_invoice){
        Object result = invoicesService.updateStatusInvoice(id_invoice);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/customer/invoices/id={id_customer}")
    public ResponseEntity<Object> getInvoicesByCustomer(@PathVariable String id_customer, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<InvoicesDTO> invoicesDTOS = invoicesService.getInvoiceByCustomer(id_customer, pageNo);
        if (invoicesDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found customer");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(invoicesDTOS.getTotalPages());
        dataDTO.setData(invoicesDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/customer/invoices/id-invoice={id_invoice}")
    public ResponseEntity<Object> getDetailInvoice(@PathVariable String id_invoice){
        Object result = invoicesService.getDetailInvoices(id_invoice);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }
}
