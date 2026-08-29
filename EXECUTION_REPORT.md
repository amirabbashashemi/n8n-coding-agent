# 📋 گزارش جامع اجرای پروژه (Automated Execution Report)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## ۱. درخواست کاربر و برنامه معماری (Planner)
* **درخواست اولیه:** `به زبان جاوا و با استفاده از maven یه برنامه ساده بساز که فقط یه خروجی داره که توش نوشته شده HELLO WORLD!`
* **تحلیل پلنر:** این پروژه یک برنامه ساده جاوا است که با استفاده از Maven ساخته شده و تنها یک خروجی را در کنسول چاپ می‌کند.

### 🎯 مراحل اجرایی مصوب:
* **گام 1:** `pom.xml` ➔ ایجاد فایل pom.xml با محتویات زیر:
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>HelloWorld</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
</project>
* **گام 2:** `src/main/java/com/example/HelloWorld.java` ➔ ایجاد کلاس HelloWorld با متد main:
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hi My Code!");
    }
}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## ۲. گزارش فایل‌ها و کدهای پروژه
هیچ فایلی تغییر داده نشد.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## ۳. نتیجه اجرای واقعی در ساندباکس (Sandbox Test Result)
* **وضعیت کلی:** ❌ بیلد یا تست با خطا مواجه شد (FAILED)
* **محیط اجرا:** Java 21 (Maven)

### 📜 لاگ خروجی تست:
```text

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━