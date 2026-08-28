// InvoiceRepository.java

package com.example.goldshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.model.Invoice;

/**
 * ریپازیتوری برای مدیریت داده‌های فاکتور.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {}
