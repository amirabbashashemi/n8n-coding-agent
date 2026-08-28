package com.example.goldshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // اینجا می‌توانید متدهای خاص‌تری را برای کار با فاکتورها اضافه کنید.
}