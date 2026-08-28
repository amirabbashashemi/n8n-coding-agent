package com.example.goldshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.goldshop.model.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // می‌توانید متدهای اضافی برای کوئری‌های خاص اضافه کنید
}