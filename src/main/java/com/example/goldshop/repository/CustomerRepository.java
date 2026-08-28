package com.example.goldshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.goldshop.model.Customer;

/**
 * ریپازیتوری مدیریت مشتریان برای تعامل با دیتابیس.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // متدهای سفارشی برای جستجو، ذخیره و حذف مشتریان می‌تواند اینجا اضافه شود.
}
