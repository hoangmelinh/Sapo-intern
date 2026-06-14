# Sapo Intern - Clothing Shop POS

Hệ thống quản lý cửa hàng bán lẻ quần áo (Point of Sale) được xây dựng trên nền tảng **Spring Boot**. Dự án cung cấp các tính năng cốt lõi để quản lý kho hàng, khách hàng, và hệ thống phân quyền nhân viên chuẩn mực.

---

## Công Nghệ Sử Dụng (Tech Stack)

*   **Backend Framework:** Spring Boot 3.3.6
*   **Ngôn ngữ:** Java 17
*   **Cơ Sở Dữ Liệu:** MySQL 8.x
*   **ORM:** Spring Data JPA / Hibernate
*   **Bảo Mật:** Spring Security + JWT (JSON Web Token)
*   **Tài liệu API (Swagger):** SpringDoc OpenAPI 2.5.0
*   **Build Tool:** Maven

---

## Cấu Trúc Thư Mục (Architecture)

Dự án được tổ chức theo kiến trúc **Domain-Driven** kết hợp với **Layered Architecture** để dễ dàng phân chia công việc cho team và mở rộng tính năng:

*   **`auth/`**: Xử lý logic đăng nhập, xác thực và cấp phát token (JWT).
*   **`user/`**: Quản lý tài khoản nhân viên, phân quyền.
*   **`warehouse/`**: Quản lý các nghiệp vụ nhập, xuất, điều chuyển kho.
*   **`entity/`**: Nơi tập trung toàn bộ các class thực thể (JPA Entities) được ánh xạ với Database.
*   **`common/`** & **`util/`**: Các class tiện ích dùng chung (Format Response, DTO...).
*   **`config/`**: Cấu hình hệ thống cốt lõi (Spring Security, CORS, Swagger, Date Format...).
*   **`exception/`**: Nơi xử lý bắt lỗi tập trung (Global Exception Handler) để trả về format chuẩn.

---

## Hệ Thống Phân Quyền (RBAC)

Ứng dụng hỗ trợ phân quyền rõ ràng theo các vai trò (Role):
*   `ROLE_ADMIN`: Quản trị toàn quyền hệ thống.
*   `ROLE_SALE`: Nhân viên bán hàng.
*   `ROLE_CS`: Nhân viên chăm sóc khách hàng.
*   `ROLE_WH`: Nhân viên kho hàng.

Hệ thống bảo mật sử dụng **JWT Token**, bao gồm Access Token (sử dụng Header Authorization) và Refresh Token (lưu trữ an toàn qua cơ chế HttpOnly Cookie để tránh tấn công XSS).

---

## Hướng Dẫn Cài Đặt Và Khởi Chạy

### 1. Clone dự án

```bash
git clone https://github.com/hoangmelinh/Sapo-intern.git
cd Sapo-intern/clothing-shop-pos
```

### 2. Thiết lập Cơ sở dữ liệu (MySQL)
Tạo một database mới trong MySQL để ứng dụng kết nối tới:
```sql
CREATE DATABASE clothing_shop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Cấu hình các biến môi trường bảo mật
Bạn cần tạo file `src/main/resources/application-secret.properties` (file này đã được bỏ qua bởi `.gitignore` nên sẽ không bị push lên Git) và thêm mật khẩu Database cùng khóa bí mật JWT của riêng bạn:
```properties
spring.datasource.password=mat_khau_mysql_cua_ban
hoangmelinh.jwt.base64-secret=khoa_jwt_base64_dai_it_nhat_64_ky_tu_cua_ban_de_ky_token_o_day
```

### 4. Khởi chạy dự án
Bạn không cần phải cài đặt sẵn Maven trên máy, chỉ cần chạy các lệnh sau bằng Maven Wrapper:
```bash
# Trên Windows
mvnw.cmd spring-boot:run

# Trên Linux / MacOS
./mvnw spring-boot:run
```

### 5. Truy cập API Documentation (Swagger UI)
Sau khi ứng dụng khởi chạy thành công ở cổng `8080`, bạn có thể truy cập giao diện tương tác API tại:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

*Dự án thực tập Sapo (Mock Project) - Backend Team*
