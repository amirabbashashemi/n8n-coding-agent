package com.example.goldshop.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.invoice.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // اینجا می‌توانید متدهایی برای عملیات خاص فاکتور اضافه کنید
}