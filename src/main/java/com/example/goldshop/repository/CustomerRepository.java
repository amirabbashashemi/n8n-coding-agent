// ریپازیتوری برای مدیریت داده‌های مشتری

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.goldshop.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
