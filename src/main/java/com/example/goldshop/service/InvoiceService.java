package com.example.goldshop.service;

import com.example.goldshop.model.Invoice;
import com.example.goldshop.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    // ایجاد یک فاکتور جدید
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    // دریافت لیست تمام فاکتورها
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    // دریافت فاکتور بر اساس ID
    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    // ویرایش فاکتور موجود
    public Invoice updateInvoice(Long id, Invoice invoiceDetails) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found for this id :: " + id));

        invoice.setAmount(invoiceDetails.getAmount());
        invoice.setDate(invoiceDetails.getDate());
        return invoiceRepository.save(invoice);
    }

    // حذف فاکتور بر اساس ID
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found for this id :: " + id));
        invoiceRepository.delete(invoice);
    }
}