# CRM - Quản Lý Khách Hàng

## 📅 Nhật ký cập nhật (13/06/2026)

### 🔹 Cải tiến hệ thống xử lý ngoại lệ

* Đức đã bổ sung cơ chế bắt lỗi 400 Runtime Exception tập trung trong `GlobalExceptionHandler`.
* Hệ thống tự động trả về mã lỗi và thông báo theo cấu trúc JSON thống nhất trong RestResponse

### 🔹 Cập nhật bảo mật

Đã cấu hình cho phép truy cập không cần đăng nhập đối với các API CRM trong file SecurityConfiguration

```java
"/api/v1/crm/customers/**"
```

Cấu hình được bổ sung trong `SecurityConfiguration`.

### 🔹 Chuẩn hóa dữ liệu khách hàng

* Thêm `CustomerStatusEnum` trong package `constant`.
* Chuyển đổi trường `gender` từ `String` ➜ `GenderEnum`. ==> entity Customer
* Chuyển đổi trường `status` từ `String` ➜ `CustomerStatusEnum` ==> entity Customer
* Tăng tính an toàn dữ liệu (Type Safety) và hạn chế lỗi nhập liệu.

---

# 📦 Chuẩn Response API

Toàn bộ API CRM sử dụng chung cấu trúc phản hồi:

```json
{
    "statusCode": 200,
    "error": null,
    "message": "Thực hiện thành công",
    "data": {}
}
```

| Trường     | Kiểu dữ liệu  | Ý nghĩa             |
| ---------- | ------------- | ------------------- |
| statusCode | Integer       | Mã trạng thái HTTP  |
| error      | String / null | Thông tin lỗi       |
| message    | String        | Thông báo nghiệp vụ |
| data       | Object / null | Dữ liệu trả về      |

---

# 👥 API Quản Lý Khách Hàng

## 1. Tra cứu danh sách khách hàng

### Endpoint

```http
GET /api/v1/crm/customers/search
```

### Query Parameters

| Tham số | Mô tả                               |
| ------- | ----------------------------------- |
| keyword | Tên hoặc số điện thoại              |
| page    | Trang hiện tại (mặc định: 0)        |
| size    | Số bản ghi mỗi trang (mặc định: 10) |

### Đặc điểm kỹ thuật

* Hỗ trợ phân trang.
* Hỗ trợ tìm kiếm theo tên hoặc số điện thoại.
* Chỉ hiển thị khách hàng có trạng thái `ACTIVE`.
* Tự động sắp xếp khách hàng mới nhất lên đầu.

```java
Sort.by("createdAt").descending()
```

---

## 2. Xem chi tiết khách hàng

### Endpoint

```http
GET /api/v1/crm/customers/{id}
```

### Đặc điểm kỹ thuật

* Tìm kiếm khách hàng theo ID.
* Nếu không tồn tại:

    * Tự động ném ngoại lệ.
    * `GlobalExceptionHandler` xử lý tập trung.
    * Trả về mã lỗi phù hợp.

---

## 3. Thêm mới khách hàng

### Endpoint

```http
POST /api/v1/crm/customers
```

### Đặc điểm kỹ thuật

* Kiểm tra trùng số điện thoại:

```java
existsByPhone(...)
```

* Không cho phép tạo khách hàng có SĐT đã tồn tại.
* Tự động sinh:

```java
createdAt
updatedAt
```

thông qua:

```java
@PrePersist
```

* Trạng thái mặc định:

```java
CustomerStatusEnum.ACTIVE
```

---

## 4. Cập nhật thông tin khách hàng

### Endpoint

```http
PUT /api/v1/crm/customers/{id}
```

### Đặc điểm kỹ thuật

* Cho phép chỉnh sửa thông tin cá nhân.
* Kiểm tra trùng số điện thoại nâng cao:

```java
existsByPhoneAndIdNot(...)
```

### Logic xử lý

✅ Cho phép:

* Giữ nguyên số điện thoại hiện tại.

❌ Không cho phép:

* Sử dụng số điện thoại đã thuộc về khách hàng khác.

### Tự động cập nhật

```java
@PreUpdate
```

để ghi nhận thời gian chỉnh sửa mới nhất.

---

## 5. Khóa khách hàng (Soft Delete)

### Endpoint

```http
PATCH /api/v1/crm/customers/{id}/deactivate
```

### Đặc điểm kỹ thuật

#### Không sử dụng DELETE cứng

Để đảm bảo toàn vẹn dữ liệu và tránh làm mất liên kết với:

* Hóa đơn
* Đơn hàng
* Lịch sử chăm sóc khách hàng

#### Phương án thực hiện

Chỉ cập nhật:

```java
status = CustomerStatusEnum.INACTIVE
```

### Ưu điểm

* Không mất dữ liệu lịch sử.
* Không làm vỡ khóa ngoại.
* Dễ dàng khôi phục tài khoản khi cần.
* Tự động bị loại khỏi danh sách tìm kiếm khách hàng tại quầy.

---

# ✅ Trạng thái khách hàng

```java
public enum CustomerStatusEnum {
    ACTIVE,
    INACTIVE
}
```

| Trạng thái | Ý nghĩa           |
| ---------- | ----------------- |
| ACTIVE     | Đang hoạt động    |
| INACTIVE   | Đã khóa (xóa mềm) |
|            |                   |
