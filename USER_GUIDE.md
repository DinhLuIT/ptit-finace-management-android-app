# 📖 Hướng Dẫn Sử Dụng – Quản Lý Chi Tiêu

> Tài liệu hướng dẫn chi tiết cách sử dụng ứng dụng **Quản Lý Chi Tiêu** (Finance Manager).

---

## Mục Lục

1. [Bắt đầu sử dụng](#1-bắt-đầu-sử-dụng)
2. [Màn hình chính (Dashboard)](#2-màn-hình-chính-dashboard)
3. [Quản lý Hủ Chi Tiêu](#3-quản-lý-hủ-chi-tiêu)
4. [Thêm giao dịch](#4-thêm-giao-dịch)
5. [Xem chi tiết hủ](#5-xem-chi-tiết-hủ)
6. [Lịch sử giao dịch](#6-lịch-sử-giao-dịch)
7. [Hồ sơ & Cài đặt](#7-hồ-sơ--cài-đặt)
8. [Luồng sử dụng mẫu](#8-luồng-sử-dụng-mẫu)
9. [Câu hỏi thường gặp (FAQ)](#9-câu-hỏi-thường-gặp-faq)

---

## 1. Bắt đầu sử dụng

### 1.1 Cài đặt
- Tải file APK hoặc build từ Android Studio
- Yêu cầu: Android 8.1 (API 27) trở lên

### 1.2 Mở app lần đầu

Khi mở app lần đầu tiên, bạn sẽ thấy:

1. **Màn hình Splash** – Logo và tên app hiện lên trong 2 giây
2. **Tạo mã PIN** – App yêu cầu bạn tạo mã PIN 4 chữ số

#### Tạo mã PIN:
```
Bước 1: Nhập 4 chữ số bạn muốn dùng làm mã PIN
Bước 2: Nhập lại 4 chữ số đó để xác nhận
→ Nếu 2 lần khớp nhau → Tạo thành công, vào app
→ Nếu 2 lần không khớp → Hiển thị lỗi, yêu cầu nhập lại từ đầu
```

### 1.3 Mở app những lần sau

```
Bước 1: Màn hình Splash (2 giây)
Bước 2: Nhập mã PIN 4 chữ số đã tạo
→ Đúng → Vào Dashboard
→ Sai → Hiển thị lỗi "Mã PIN không đúng", nhập lại
```

> ⚠️ **Lưu ý**: Nếu quên mã PIN, hiện tại cần xóa dữ liệu app trong Cài đặt > Ứng dụng > Quản Lý Chi Tiêu > Xóa dữ liệu. Tính năng đặt lại PIN sẽ có ở phiên bản sau.

---

## 2. Màn hình chính (Dashboard)

Sau khi mở khóa, bạn sẽ thấy **Dashboard** với các thông tin:

### Thẻ tổng quan (màu xanh lá phía trên)
| Thông tin | Mô tả |
|---|---|
| **Tổng số dư** | Tổng số tiền còn lại trong tất cả các hủ |
| **Thu nhập tháng này** | Tổng tiền đã thu trong tháng hiện tại (màu xanh, dấu +) |
| **Chi tiêu tháng này** | Tổng tiền đã chi trong tháng hiện tại (màu đỏ, dấu -) |

### Giao dịch gần đây
- Hiển thị **5 giao dịch mới nhất**
- Mỗi giao dịch hiện: icon danh mục, tên danh mục, ghi chú, số tiền, ngày
- Nhấn **"Xem tất cả"** → Chuyển sang tab Giao dịch

### Nút (+) xanh góc dưới phải
- Nhấn để **thêm giao dịch mới** nhanh

### Thanh điều hướng dưới cùng (4 tab)
| Tab | Icon | Chức năng |
|---|---|---|
| **Trang chủ** | 🏠 | Dashboard tổng quan |
| **Hủ** | 💰 | Danh sách hủ chi tiêu |
| **Giao dịch** | 🏛 | Lịch sử tất cả giao dịch |
| **Hồ sơ** | 👤 | Cài đặt & thông tin |

---

## 3. Quản lý Hủ Chi Tiêu

### 3.1 Hủ chi tiêu là gì?

Hủ chi tiêu (Spending Pot) là cách phân chia tiền theo mục đích sử dụng. Ví dụ:
- 🍜 Hủ "Ăn uống" – Ngân sách 3,000,000 ₫/tháng
- 🏠 Hủ "Nhà ở" – Ngân sách 5,000,000 ₫/tháng
- 🎮 Hủ "Giải trí" – Ngân sách 1,000,000 ₫/tháng
- 💰 Hủ "Tiết kiệm" – Ngân sách 2,000,000 ₫/tháng

### 3.2 Xem danh sách hủ

Nhấn tab **"Hủ"** trên thanh điều hướng. Mỗi hủ hiển thị:

```
┌─────────────────────────────────────┐
│ ■ Ăn uống                          │
│   Ngân sách: 3,000,000 ₫           │
│                         1,800,000 ₫ │ ← Số dư hiện tại
│   ████████████░░░░░  40%            │ ← Thanh tiến độ
│   40%                 Còn lại:      │
│                       1,800,000 ₫   │
└─────────────────────────────────────┘
```

#### Màu thanh tiến độ:
| Màu | Điều kiện | Ý nghĩa |
|---|---|---|
| 🟢 Xanh lá | Đã chi < 60% ngân sách | An toàn |
| 🟡 Vàng | Đã chi 60–85% ngân sách | Cảnh báo |
| 🔴 Đỏ | Đã chi > 85% ngân sách | Nguy hiểm |

### 3.3 Tạo hủ mới

```
Bước 1: Vào tab "Hủ"
Bước 2: Nhấn nút (+) xanh góc dưới phải
Bước 3: Trong dialog hiện ra, nhập:
   • Tên hủ (bắt buộc): VD "Ăn uống"
   • Hạn mức ngân sách (bắt buộc): VD "3000000"
   • Chọn màu sắc: Nhấn vào 1 trong 8 vòng tròn màu
Bước 4: Nhấn "Lưu"
→ Hủ mới xuất hiện trong danh sách với số dư = 0
```

### 3.4 Xóa hủ

```
Bước 1: Nhấn vào hủ muốn xóa → Vào màn Chi tiết hủ
Bước 2: Nhấn icon thùng rác (🗑) góc trên phải
Bước 3: Xác nhận "Xóa" trong dialog
→ Hủ và tất cả giao dịch bên trong sẽ bị xóa
```

> ⚠️ **Cảnh báo**: Xóa hủ sẽ xóa toàn bộ giao dịch trong hủ đó. Thao tác không thể hoàn tác!

---

## 4. Thêm giao dịch

### 4.1 Cách thêm giao dịch

Có 2 cách mở màn hình thêm giao dịch:
- **Cách 1**: Nhấn nút (+) trên Dashboard (tab Trang chủ)
- **Cách 2**: Vào Chi tiết hủ → Nhấn nút (+)

### 4.2 Điền thông tin giao dịch

```
Bước 1: Chọn loại giao dịch
   • [Thu nhập] ← Tiền vào (lương, thưởng, quà...)
   • [Chi tiêu] ← Tiền ra (ăn uống, mua sắm, hóa đơn...)
   → Mặc định là "Chi tiêu"

Bước 2: Nhập số tiền
   • Nhập số (VD: 500000)
   • Đơn vị: ₫ (hiển thị bên phải)

Bước 3: Chọn hủ
   • Nhấn vào dropdown "Chọn hủ"
   • Chọn hủ muốn ghi nhận giao dịch
   → Nếu mở từ Chi tiết hủ, hủ đã được tự chọn sẵn

Bước 4: Chọn danh mục
   • Nhấn vào dropdown "Chọn danh mục"
   • Danh sách danh mục thay đổi theo loại (Thu nhập/Chi tiêu):
     - Chi tiêu: Ăn uống, Nhà ở, Di chuyển, Giải trí, Học tập, 
                  Sức khỏe, Mua sắm, Tiết kiệm, Quà tặng, Khác
     - Thu nhập: Lương, Tiết kiệm, Quà tặng, Khác

Bước 5: Chọn ngày
   • Mặc định là ngày hôm nay
   • Nhấn vào ô ngày để chọn ngày khác (DatePicker)

Bước 6: Ghi chú (tùy chọn)
   • VD: "Tiền cơm trưa", "Lương tháng 5"

Bước 7: Nhấn "Lưu"
```

### 4.3 Sau khi lưu giao dịch

- **Thu nhập** → Số dư hủ **tăng** lên
- **Chi tiêu** → Số dư hủ **giảm** xuống
- Giao dịch xuất hiện trong:
  - Dashboard (nếu nằm trong 5 giao dịch gần nhất)
  - Tab Giao dịch (tất cả)
  - Chi tiết hủ (giao dịch của hủ đó)

### 4.4 Validation (Kiểm tra đầu vào)

| Trường | Quy tắc | Lỗi hiển thị |
|---|---|---|
| Số tiền | Phải > 0 | "Vui lòng nhập số tiền hợp lệ" |
| Hủ | Phải chọn | "Vui lòng chọn hủ" |
| Danh mục | Phải chọn | "Vui lòng chọn danh mục" |
| Ngày | Tự có mặc định | — |
| Ghi chú | Tùy chọn | — |

---

## 5. Xem chi tiết hủ

### Cách vào: Nhấn vào 1 hủ bất kỳ trong tab "Hủ"

### Thông tin hiển thị:

```
← [Tên hủ]                        🗑

┌─ Thẻ xanh ────────────────────────┐
│ Số dư                              │
│ 1,800,000 ₫                       │
│ ████████████░░░░░                  │
│ 40% Đã chi · Ngân sách: 3,000,000 ₫│
└────────────────────────────────────┘

Giao dịch
├── 🍜 Ăn uống      -50,000 ₫   10/05/2026
│      Tiền cơm trưa
├── 💵 Lương        +3,000,000 ₫  01/05/2026
│      Lương tháng 5
└── ...

                                  (+)
```

### Các thao tác:
| Thao tác | Cách thực hiện |
|---|---|
| Quay lại | Nhấn nút ← góc trên trái |
| Xóa hủ | Nhấn icon 🗑 góc trên phải |
| Thêm giao dịch cho hủ này | Nhấn nút (+) → Hủ sẽ được tự chọn sẵn |

---

## 6. Lịch sử giao dịch

### Cách vào: Nhấn tab **"Giao dịch"** trên thanh điều hướng

### Bộ lọc (Filter Chips)

Phía trên danh sách có 3 chip lọc:

| Chip | Hiển thị |
|---|---|
| **Tất cả** (mặc định) | Tất cả giao dịch |
| **Thu nhập** | Chỉ giao dịch thu nhập |
| **Chi tiêu** | Chỉ giao dịch chi tiêu |

### Thông tin mỗi giao dịch

```
🍜  Ăn uống                    -50,000 ₫
    Tiền cơm trưa               10/05/2026
```

- **Icon tròn bên trái**: Emoji danh mục
  - Nền xanh = Thu nhập
  - Nền đỏ = Chi tiêu
- **Dòng 1**: Tên danh mục + Số tiền (xanh nếu +, đỏ nếu -)
- **Dòng 2**: Ghi chú (hoặc tên hủ nếu không có ghi chú) + Ngày

### Sắp xếp
- Giao dịch **mới nhất hiển thị trước** (theo ngày, giảm dần)

---

## 7. Hồ sơ & Cài đặt

### Cách vào: Nhấn tab **"Hồ sơ"** trên thanh điều hướng

### Các mục hiện có:

| Mục | Trạng thái | Mô tả |
|---|---|---|
| **Thống kê** | 🔜 Phase 2 | Biểu đồ thống kê chi tiêu |
| **Tỷ giá** | 🔜 Phase 3 | Xem tỷ giá ngoại tệ |
| **Xuất báo cáo** | 🔜 Phase 3 | Xuất dữ liệu ra CSV |
| **Chế độ tối** | ✅ Hoạt động | Bật/tắt dark mode |
| **Đổi mã PIN** | 🔜 Sắp có | Đổi mã PIN mở app |
| **Giới thiệu** | ✅ Hoạt động | Hiển thị thông tin app |

### Chế độ tối (Dark Mode)

```
Bật: Gạt công tắc sang phải → Toàn bộ app chuyển sang nền tối
Tắt: Gạt công tắc sang trái → Quay lại nền sáng
→ Áp dụng ngay lập tức, không cần restart app
```

---

## 8. Luồng sử dụng mẫu

### Kịch bản: Bắt đầu quản lý chi tiêu tháng mới

```
1. Mở app → Nhập PIN

2. Tạo các hủ chi tiêu:
   Tab "Hủ" → (+) → Tạo lần lượt:
   • "Ăn uống"     – 3,000,000 ₫  – Màu xanh lá
   • "Nhà ở"       – 5,000,000 ₫  – Màu xanh dương
   • "Di chuyển"   – 1,000,000 ₫  – Màu cam
   • "Giải trí"    – 1,000,000 ₫  – Màu tím
   • "Tiết kiệm"   – 2,000,000 ₫  – Màu đỏ

3. Ghi nhận lương đầu tháng:
   Nút (+) → Thu nhập → 12,000,000 ₫
   → Hủ: Ăn uống → Danh mục: Lương → Lưu
   (Lặp lại cho mỗi hủ với số tiền tương ứng)

4. Hàng ngày, ghi nhận chi tiêu:
   Nút (+) → Chi tiêu → 50,000 ₫
   → Hủ: Ăn uống → Danh mục: Ăn uống
   → Ghi chú: "Cơm trưa" → Lưu

5. Kiểm tra tình hình:
   • Dashboard: Xem tổng thu/chi tháng
   • Tab "Hủ": Xem % ngân sách đã dùng
   • Nhấn vào hủ: Xem chi tiết giao dịch
```

### Kịch bản: Kiểm tra cuối tháng

```
1. Mở Dashboard → Xem tổng chi tiêu tháng
2. Tab "Hủ" → Xem hủ nào còn tiền, hủ nào hết
   • Xanh (<60%): Tốt, còn dư nhiều
   • Vàng (60-85%): Cần chú ý
   • Đỏ (>85%): Sắp/đã vượt ngân sách!
3. Tab "Giao dịch" → Lọc "Chi tiêu" → Xem đã chi gì nhiều nhất
```

---

## 9. Câu hỏi thường gặp (FAQ)

### Q: Tôi quên mã PIN thì sao?
**A:** Hiện tại chưa có tính năng quên PIN. Bạn cần vào **Cài đặt điện thoại > Ứng dụng > Quản Lý Chi Tiêu > Xóa dữ liệu** để reset. ⚠️ Lưu ý: Thao tác này sẽ **xóa toàn bộ dữ liệu** (hủ, giao dịch). Tính năng khôi phục PIN sẽ được thêm ở phiên bản sau.

### Q: Tôi có thể xóa 1 giao dịch đã nhập không?
**A:** Tính năng xóa giao dịch riêng lẻ sẽ có ở Phase 2 (swipe để xóa). Hiện tại có thể xóa cả hủ (sẽ xóa hết giao dịch trong hủ).

### Q: Danh mục hiển thị khác nhau khi chọn Thu nhập và Chi tiêu?
**A:** Đúng. Mỗi loại giao dịch có danh mục riêng:
- **Chi tiêu**: Ăn uống, Nhà ở, Di chuyển, Giải trí, Học tập, Sức khỏe, Mua sắm
- **Thu nhập**: Lương
- **Cả hai**: Tiết kiệm, Quà tặng, Khác

### Q: Số dư hủ có thể âm không?
**A:** Có. Nếu chi tiêu nhiều hơn số dư, hủ sẽ có số dư âm. Thanh tiến độ sẽ hiện đỏ (>85%).

### Q: App có hỗ trợ tiếng Anh không?
**A:** Có. Ngôn ngữ mặc định là tiếng Việt. Nếu bạn đổi ngôn ngữ điện thoại sang English, app sẽ tự động hiển thị tiếng Anh.

### Q: Dữ liệu được lưu ở đâu?
**A:** Dữ liệu được lưu **cục bộ** trên điện thoại bằng SQLite. Không có đồng bộ đám mây. Nếu gỡ app, dữ liệu sẽ bị mất.

### Q: Dark Mode hoạt động như thế nào?
**A:** Vào tab **Hồ sơ** → Bật/tắt **Chế độ tối**. App sẽ đổi giao diện ngay lập tức.

---

## Biểu tượng danh mục

| Biểu tượng | Danh mục | Loại |
|---|---|---|
| 🍜 | Ăn uống | Chi tiêu |
| 🏠 | Nhà ở | Chi tiêu |
| 🚗 | Di chuyển | Chi tiêu |
| 🎮 | Giải trí | Chi tiêu |
| 📚 | Học tập | Chi tiêu |
| 💊 | Sức khỏe | Chi tiêu |
| 🛍 | Mua sắm | Chi tiêu |
| 💰 | Tiết kiệm | Cả hai |
| 💵 | Lương | Thu nhập |
| 🎁 | Quà tặng | Cả hai |
| 📦 | Khác | Cả hai |

---

## Phím tắt & Mẹo

| Mẹo | Chi tiết |
|---|---|
| ⚡ Thêm giao dịch nhanh | Nút (+) trên Dashboard → không cần vào tab Hủ trước |
| 📊 Xem nhanh tình hình | Dashboard hiện tổng thu/chi tháng ngay trên đầu |
| 🎯 Quản lý theo hủ | Nhấn vào hủ → Xem toàn bộ giao dịch của hủ đó |
| 🔍 Lọc giao dịch | Tab Giao dịch → Chip "Thu nhập" hoặc "Chi tiêu" |
| 🌙 Dùng ban đêm | Bật Dark Mode trong Hồ sơ để dễ nhìn |

---

> 📌 **Phiên bản**: 1.0 (Phase 1)  
> 📅 **Cập nhật lần cuối**: 10/05/2026  
> 🏫 **Phát triển bởi**: PTIT HCM
