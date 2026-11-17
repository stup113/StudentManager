# Student Manager – Ứng dụng quản lý sinh viên (Android, Kotlin)

Công nghệ: Kotlin, Jetpack Compose (Material 3), Room (SQLite), Navigation Compose, Coil.

## Cách chạy
1. Android Studio (Koala trở lên) → **File > Open** → chọn thư mục `StudentManager`.
2. Chờ Gradle Sync xong (cần Internet lần đầu; nếu Studio hỏi tạo/nâng cấp Gradle wrapper thì đồng ý).
3. Chọn máy ảo / thiết bị thật (Android 7.0+, API 24+) → **Run ▶**.

## Chức năng
- **Trang 1 – Danh sách SV**: tìm kiếm, thêm (nút +), sửa, xóa, click vào SV để xem chi tiết.
- **Trang 2 – Chi tiết SV**: xem Họ tên, MSSV, Email, Avatar; nút Sửa / Xóa.
- **Trang 3 – Thêm / Sửa SV**: nhập thông tin, avatar bằng link ảnh hoặc chọn ảnh từ thiết bị.
- Dữ liệu lưu vào **Room/SQLite** (`student_db`, bảng `students`).
- Sửa / Xóa đều hiện hộp thoại xác nhận *"Bạn có muốn sửa / xóa thông tin SV không?"* (Có / Không).
- Đa ngôn ngữ tự động theo thiết bị: `values/` (English), `values-vi/` (Tiếng Việt), gồm cả tên app.
- Kiểm tra dữ liệu: không để trống, email hợp lệ, MSSV không trùng.

## Cấu trúc
```
app/src/main/java/com/example/studentmanager/
├── MainActivity.kt            # NavHost: list / detail / form
├── data/                      # Student (Entity), StudentDao, AppDatabase
└── ui/                        # ViewModel, 3 màn hình, components, theme
app/src/main/res/values/strings.xml      # English
app/src/main/res/values-vi/strings.xml   # Tiếng Việt
```
