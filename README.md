# 💰 Finance Manager – Ứng dụng Quản Lý Chi Tiêu Cá Nhân

> Ứng dụng Android quản lý chi tiêu cá nhân lấy cảm hứng từ **Timo** với core concept **"Hủ Chi Tiêu" (Spending Pots/Jars)**.

## 📋 Mục Lục

- [Tổng Quan](#tổng-quan)
- [Kiến Trúc Dự Án](#kiến-trúc-dự-án)
- [Quy Tắc Code Chung](#quy-tắc-code-chung)
- [Use Cases](#use-cases)
- [Database Schema](#database-schema)
- [Thư Viện Sử Dụng](#thư-viện-sử-dụng)
- [Tiến Độ Phát Triển](#tiến-độ-phát-triển)

---

## Tổng Quan

| Thông tin | Chi tiết |
|---|---|
| **Tên App** | Finance Manager |
| **Ngôn ngữ** | Java |
| **Min SDK** | 27 (Android 8.1) |
| **Target SDK** | 36 |
| **Package** | `com.ptithcm.finacemanager` |
| **Kiến trúc UI** | Bottom Navigation + Fragments |
| **Database** | SQLite (SQLiteOpenHelper) |
| **Ngôn ngữ App** | Tiếng Việt + Tiếng Anh (I18N) |

---

## Kiến Trúc Dự Án

```
com.ptithcm.finacemanager/
├── activity/              ← Activity controllers (Splash, PIN, Main, Detail...)
├── fragment/              ← Fragments cho Bottom Navigation
│   ├── HomeFragment       ← Dashboard tổng quan
│   ├── PotsFragment       ← Danh sách hủ chi tiêu
│   ├── TransactionsFragment ← Lịch sử giao dịch
│   └── ProfileFragment    ← Hồ sơ & cài đặt
├── adapter/               ← RecyclerView Adapters
├── model/                 ← Data models (POJO)
├── database/              ← SQLite helper (DBManager)
├── api/                   ← API clients (Exchange Rate)
├── utils/                 ← Utility/Helper classes
│   ├── CustomToast         ← Toast tùy chỉnh (Success/Error/Warning)
│   ├── NotificationHelper  ← Quản lý Local Notification cảnh báo ngân sách
│   └── ...
└── dialog/                ← Custom DialogFragments
    ├── BudgetAlertDialog   ← Dialog cảnh báo ngân sách siêu đẹp
    └── ...
```

### Nguyên tắc kiến trúc
- **Activity** chỉ chứa logic điều hướng và quản lý Fragment
- **Fragment** chứa logic UI và tương tác người dùng
- **Adapter** chỉ chịu trách nhiệm bind data vào ViewHolder
- **DBManager** là singleton access point duy nhất tới SQLite
- **Model** là POJO thuần, không chứa business logic
- **Utils** chứa các helper method dùng chung (format tiền, ngày tháng, etc.)

---

## Quy Tắc Code Chung

### 1. Naming Convention

#### Java Classes
```
Activity:   XxxActivity.java         (VD: AddTransactionActivity.java)
Fragment:   XxxFragment.java         (VD: HomeFragment.java)
Adapter:    XxxAdapter.java          (VD: PotAdapter.java)
Model:      Xxx.java                 (VD: Transaction.java)
Dialog:     XxxDialog.java           (VD: AddPotDialog.java)
Utils:      XxxUtils.java / XxxHelper.java
```

#### Layout XML
```
Activity layout:    activity_xxx.xml         (VD: activity_main.xml)
Fragment layout:    fragment_xxx.xml         (VD: fragment_home.xml)
Item layout:        item_xxx.xml             (VD: item_pot.xml, item_transaction.xml)
Dialog layout:      dialog_xxx.xml           (VD: dialog_add_pot.xml)
Include layout:     layout_xxx.xml           (VD: layout_toolbar.xml)
```

#### View IDs (trong XML)
```
Format: loại_mô_tả    (snake_case, viết thường)

TextView:       tv_xxx          (VD: tv_pot_name, tv_balance)
EditText:       et_xxx          (VD: et_amount, et_note)
Button:         btn_xxx         (VD: btn_save, btn_cancel)
ImageView:      iv_xxx          (VD: iv_icon, iv_avatar)
RecyclerView:   rv_xxx          (VD: rv_pots, rv_transactions)
CardView:       card_xxx        (VD: card_pot, card_summary)
ProgressBar:    pb_xxx          (VD: pb_budget)
FloatingActionButton: fab_xxx   (VD: fab_add)
Spinner:        sp_xxx          (VD: sp_pot_selector)
TextInputLayout: til_xxx        (VD: til_amount)
BottomNavigationView: bnv_xxx   (VD: bnv_main)
ChipGroup:      cg_xxx          (VD: cg_categories)
```

#### Drawable Resources
```
Icon:           ic_xxx.xml               (VD: ic_food.xml, ic_home.xml)
Background:     bg_xxx.xml               (VD: bg_card_pot.xml, bg_rounded.xml)
Selector:       selector_xxx.xml         (VD: selector_tab.xml)
Shape:          shape_xxx.xml            (VD: shape_circle.xml)
```

#### String Resources (I18N)
```
strings.xml:        Tiếng Anh (mặc định)
strings.xml (vi):   Tiếng Việt (values-vi/strings.xml)

Key format:         snake_case
Prefix theo context:
  - Label:       label_xxx       (VD: label_total_balance)
  - Button:      btn_xxx         (VD: btn_save)
  - Title:       title_xxx       (VD: title_add_pot)
  - Message:     msg_xxx         (VD: msg_confirm_delete)
  - Error:       error_xxx       (VD: error_invalid_amount)
  - Hint:        hint_xxx        (VD: hint_enter_amount)
```

#### Color Resources
```
Format:  color_mô_tả           (VD: color_primary, color_income, color_expense)
```

### 2. Java Code Style

```java
// ✅ Biến private, đặt tên camelCase
private RecyclerView rvPots;
private PotAdapter potAdapter;
private DBManager dbManager;

// ✅ Hằng số dùng UPPER_SNAKE_CASE
private static final String TAG = "MainActivity";
private static final int REQUEST_CODE_ADD = 100;

// ✅ Method rõ ràng, verb-first
private void loadPots() { ... }
private void showAddPotDialog() { ... }
private void updateBalanceDisplay() { ... }

// ✅ Comment tiếng Việt cho logic nghiệp vụ phức tạp
// Tính tổng chi tiêu theo danh mục trong tháng hiện tại
private Map<String, Double> calculateCategoryExpenses() { ... }
```

### 3. Database Rules

```java
// ✅ Luôn đóng Cursor và Database sau khi dùng
Cursor cursor = db.rawQuery(...);
try {
    // xử lý data
} finally {
    cursor.close();
    db.close();
}

// ✅ Dùng parameterized queries để tránh SQL Injection
String selection = "POT_ID = ? AND TYPE = ?";
String[] selectionArgs = { String.valueOf(potId), type };
Cursor cursor = db.query("TRANSACTIONS", null, selection, selectionArgs, ...);

// ❌ KHÔNG dùng string concatenation cho SQL
// db.rawQuery("SELECT * FROM POTS WHERE ID = " + potId, null);  ← NGUY HIỂM

// ✅ Khi thay đổi schema → tăng DATABASE_VERSION và xử lý migration
// Dev: có thể DROP TABLE rồi tạo lại
// Production: dùng ALTER TABLE
```

### 4. Activity / Fragment Lifecycle

```java
// ✅ Khởi tạo View trong onCreate (Activity) hoặc onCreateView (Fragment)
// ✅ Load data trong onResume() để refresh khi quay lại
// ✅ Giải phóng resources trong onDestroy() hoặc onDestroyView()

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_xxx);
    initViews();       // Ánh xạ view
    initListeners();   // Gắn event listeners
}

@Override
protected void onResume() {
    super.onResume();
    loadData();        // Load/refresh data
}
```

### 5. RecyclerView Adapter Pattern

```java
// ✅ Dùng ViewHolder pattern chuẩn
// ✅ Adapter nhận List data qua constructor hoặc setter
// ✅ Cung cấp method updateData() để refresh

public class PotAdapter extends RecyclerView.Adapter<PotAdapter.PotViewHolder> {

    private List<Pot> potList;
    private final OnPotClickListener listener;

    // Interface callback cho click events
    public interface OnPotClickListener {
        void onPotClick(Pot pot);
        void onPotLongClick(Pot pot);
    }

    public void updateData(List<Pot> newList) {
        this.potList = newList;
        notifyDataSetChanged();
    }
}
```

### 6. Format Tiền Tệ

```java
// ✅ Luôn dùng CurrencyFormatter utility
// ✅ Hiển thị: 1,500,000 ₫ (có dấu phân cách hàng nghìn)
// ✅ Lưu DB: kiểu REAL (double), không format

CurrencyFormatter.format(1500000);  // → "1,500,000 ₫"
```

### 7. Error Handling

```java
// ✅ Validate input trước khi lưu DB
// ✅ Sử dụng CustomToast cho user feedback (thay thế Toast mặc định)
// ✅ Log error với TAG rõ ràng

if (amount <= 0) {
    tilAmount.setError(getString(R.string.error_invalid_amount));
    return;
}

try {
    dbManager.addTransaction(transaction);
    CustomToast.showSuccess(this, R.string.msg_transaction_saved);
} catch (Exception e) {
    Log.e(TAG, "Error saving transaction", e);
    CustomToast.showError(this, R.string.error_save_failed);
}
```

---

## Use Cases

### 🔐 UC-01: Xác Thực PIN
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Mở khóa ứng dụng bằng PIN 4-6 số |
| **Precondition** | App đã được cài đặt |
| **Flow chính** | 1. Mở app → Splash Screen<br>2. Kiểm tra PIN đã đặt chưa<br>3a. Chưa → Màn hình tạo PIN mới (nhập 2 lần)<br>3b. Rồi → Màn hình nhập PIN<br>4. Xác thực → Vào Dashboard |
| **Flow phụ** | Nhập sai PIN 3 lần → Hiển thị thông báo chờ 30s |
| **Postcondition** | Người dùng truy cập được Dashboard |

### 🏠 UC-02: Xem Dashboard
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Xem tổng quan tài chính cá nhân |
| **Flow chính** | 1. Sau khi xác thực PIN<br>2. Hiển thị: Tổng số dư, Tổng thu/chi tháng, Biểu đồ phân bổ, 5 giao dịch gần nhất |
| **Postcondition** | Hiển thị thông tin tài chính tổng quan |

### 🏺 UC-03: Quản Lý Hủ Chi Tiêu
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Tạo, sửa, xóa, xem danh sách hủ chi tiêu |

#### UC-03a: Tạo Hủ Mới
| | |
|---|---|
| **Flow chính** | 1. Tab "Hủ" → FAB (+)<br>2. Nhập: Tên hủ, Ngân sách, Chọn màu/icon<br>3. Validate: tên không trống, ngân sách > 0<br>4. Lưu → Hiển thị trong danh sách |
| **Postcondition** | Hủ mới được tạo với balance = 0 |

#### UC-03b: Xem Chi Tiết Hủ
| | |
|---|---|
| **Flow chính** | 1. Tap vào 1 hủ<br>2. Hiển thị: Tên, Số dư, Ngân sách, % đã dùng (progress bar), Danh sách giao dịch của hủ |
| **Postcondition** | Hiển thị chi tiết hủ và lịch sử giao dịch |

#### UC-03c: Sửa / Xóa Hủ
| | |
|---|---|
| **Flow chính** | Long press → Menu (Sửa / Xóa)<br>Xóa: Dialog xác nhận → Soft delete (IS_ACTIVE = 0) |
| **Business Rule** | Không thể xóa hủ có balance > 0 → Yêu cầu chuyển hết tiền trước |

#### UC-03d: Chuyển Tiền Giữa Các Hủ
| | |
|---|---|
| **Flow chính** | 1. Chọn hủ nguồn và hủ đích<br>2. Nhập số tiền<br>3. Validate: số tiền <= balance hủ nguồn<br>4. Tạo 2 transaction (EXPENSE ở hủ nguồn, INCOME ở hủ đích) |

### 💸 UC-04: Thêm Giao Dịch
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Ghi nhận một khoản thu nhập hoặc chi tiêu |
| **Flow chính** | 1. FAB (+) trên Dashboard hoặc trong Chi tiết hủ<br>2. Chọn loại: Thu nhập / Chi tiêu (toggle)<br>3. Chọn hủ (Spinner)<br>4. Nhập số tiền<br>5. Chọn danh mục<br>6. Chọn ngày (mặc định: hôm nay)<br>7. Nhập ghi chú (optional)<br>8. Validate → Lưu |
| **Validation** | Số tiền > 0, Đã chọn hủ, Đã chọn danh mục<br>Chi tiêu: số tiền <= balance hủ (optional warning) |
| **Postcondition** | Giao dịch được lưu, Balance hủ được cập nhật tự động |

### 📋 UC-05: Xem Lịch Sử Giao Dịch
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Xem, tìm kiếm, lọc danh sách giao dịch |
| **Flow chính** | 1. Tab "Giao dịch"<br>2. Hiển thị tất cả giao dịch (mới nhất trước)<br>3. Filter: Theo hủ, danh mục, loại, khoảng thời gian<br>4. Search: Theo ghi chú |
| **Flow phụ** | Sửa giao dịch: Tap → Màn hình sửa → Cập nhật balance<br>Xóa giao dịch: Swipe → Xác nhận → Hoàn lại balance |

### 📊 UC-06: Xem Thống Kê
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Xem biểu đồ và số liệu thống kê chi tiêu |
| **Flow chính** | 1. Profile → Thống kê<br>2. Biểu đồ tròn: Chi tiêu theo danh mục<br>3. Biểu đồ cột: Thu/Chi theo tháng<br>4. Biểu đồ đường: Xu hướng chi tiêu<br>5. Số liệu: Trung bình/ngày, Danh mục chi nhiều nhất |
| **Thư viện** | MPAndroidChart |

### 🔔 UC-07: Cảnh Báo Ngân Sách
| | |
|---|---|
| **Actor** | Hệ thống |
| **Mô tả** | Tự động cảnh báo khi chi tiêu gần đạt / vượt ngân sách hủ |
| **Trigger** | Sau mỗi lần thêm giao dịch chi tiêu |
| **Flow chính** | 1. Tính % chi tiêu so với budget_limit<br>2. >= 80%: Notification vàng (Cảnh báo)<br>3. >= 100%: Notification đỏ (Vượt ngân sách) |
| **Visual** | Progress bar đổi màu: Xanh (<60%) → Vàng (60-85%) → Đỏ (>85%) |

### 💱 UC-08: Xem Tỷ Giá Ngoại Tệ
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Xem tỷ giá và quy đổi ngoại tệ |
| **Flow chính** | 1. Profile → Tỷ giá<br>2. Gọi API Exchange Rate<br>3. Hiển thị tỷ giá USD, EUR, JPY... so với VND<br>4. Cho phép quy đổi nhanh |
| **API** | ExchangeRate-API (free tier) |

### 📤 UC-09: Xuất Báo Cáo
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Xuất dữ liệu chi tiêu ra file CSV |
| **Flow chính** | 1. Profile → Xuất báo cáo<br>2. Chọn khoảng thời gian<br>3. Tạo file CSV<br>4. Share intent → Email, Messaging |

### 🎯 UC-10: Mục Tiêu Tiết Kiệm
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Đặt mục tiêu tiết kiệm cho hủ |
| **Flow chính** | 1. Chi tiết hủ → Đặt mục tiêu<br>2. Nhập: Tên mục tiêu, Số tiền, Ngày hoàn thành dự kiến<br>3. Tracking tiến độ (progress bar + %) |

### 🔄 UC-11: Giao Dịch Định Kỳ
| | |
|---|---|
| **Actor** | Người dùng |
| **Mô tả** | Tạo giao dịch tự động lặp lại |
| **Flow chính** | 1. Thêm giao dịch → Toggle "Định kỳ"<br>2. Chọn chu kỳ: Hàng ngày / Hàng tuần / Hàng tháng<br>3. Hệ thống tự động tạo giao dịch theo chu kỳ (WorkManager) |

### 🌙 UC-12: Dark Mode
| | |
|---|---|
| **Flow chính** | Profile → Cài đặt → Toggle Dark Mode<br>App áp dụng theme tối ngay lập tức |

---

## Database Schema

### Bảng POTS
```sql
CREATE TABLE POTS (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    NAME TEXT NOT NULL,
    BALANCE REAL DEFAULT 0,
    BUDGET_LIMIT REAL NOT NULL,
    COLOR TEXT DEFAULT '#4CAF50',
    ICON TEXT DEFAULT 'ic_default',
    CREATED_AT TEXT NOT NULL,
    IS_ACTIVE INTEGER DEFAULT 1
);
```

### Bảng CATEGORIES
```sql
CREATE TABLE CATEGORIES (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    NAME TEXT NOT NULL,
    ICON TEXT NOT NULL,
    TYPE TEXT NOT NULL,          -- 'INCOME' / 'EXPENSE' / 'BOTH'
    IS_DEFAULT INTEGER DEFAULT 0
);
```

### Bảng TRANSACTIONS
```sql
CREATE TABLE TRANSACTIONS (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    POT_ID INTEGER NOT NULL,
    CATEGORY_ID INTEGER,
    AMOUNT REAL NOT NULL,
    TYPE TEXT NOT NULL,          -- 'INCOME' / 'EXPENSE'
    DATE TEXT NOT NULL,
    NOTE TEXT,
    CREATED_AT TEXT NOT NULL,
    FOREIGN KEY(POT_ID) REFERENCES POTS(ID),
    FOREIGN KEY(CATEGORY_ID) REFERENCES CATEGORIES(ID)
);
```

### Bảng USER_SETTINGS
```sql
CREATE TABLE USER_SETTINGS (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    PIN_HASH TEXT,
    CURRENCY TEXT DEFAULT 'VND',
    DARK_MODE INTEGER DEFAULT 0,
    NOTIFICATION_ENABLED INTEGER DEFAULT 1
);
```

### Bảng SAVINGS_GOALS (Phase 3 – Plan A: Độc lập)
```sql
CREATE TABLE SAVINGS_GOALS (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    NAME TEXT NOT NULL,
    TARGET_AMOUNT REAL NOT NULL,
    CURRENT_AMOUNT REAL DEFAULT 0,
    TARGET_DATE TEXT,
    ICON TEXT DEFAULT '🎯',
    COLOR TEXT DEFAULT '#4CAF50',
    CREATED_AT TEXT NOT NULL
);
```

### Bảng GOAL_CONTRIBUTIONS (Phase 3)
```sql
CREATE TABLE GOAL_CONTRIBUTIONS (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    GOAL_ID INTEGER NOT NULL,
    AMOUNT REAL NOT NULL,
    NOTE TEXT,
    DATE TEXT NOT NULL,
    CREATED_AT TEXT NOT NULL,
    FOREIGN KEY(GOAL_ID) REFERENCES SAVINGS_GOALS(ID)
);
```

### Bảng RECURRING_TRANSACTIONS (Phase 3)
```sql
CREATE TABLE RECURRING_TRANSACTIONS (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    POT_ID INTEGER NOT NULL,
    CATEGORY_ID INTEGER,
    AMOUNT REAL NOT NULL,
    TYPE TEXT NOT NULL,
    NOTE TEXT,
    FREQUENCY TEXT NOT NULL,     -- 'DAILY' / 'WEEKLY' / 'MONTHLY' / 'YEARLY'
    NEXT_DATE TEXT NOT NULL,
    IS_ACTIVE INTEGER DEFAULT 1,
    CREATED_AT TEXT NOT NULL,
    FOREIGN KEY(POT_ID) REFERENCES POTS(ID),
    FOREIGN KEY(CATEGORY_ID) REFERENCES CATEGORIES(ID)
);
```

---

## Thư Viện Sử Dụng

| Thư viện | Mục đích |
|---|---|
| `androidx.appcompat` | Backward compatibility |
| `com.google.android.material` | Material Design 3 components |
| `androidx.constraintlayout` | Flexible layouts |
| `MPAndroidChart` | Biểu đồ thống kê |
| `Lottie` | Animations |
| `androidx.biometric` | Fingerprint/Face unlock |
| `androidx.work` | WorkManager – Giao dịch định kỳ chạy nền |
| `Gson` | JSON parsing |
| `OkHttp` | HTTP client |

---

## Tiến Độ Phát Triển

### Phase 1: Core Foundation 🔴
- [x] SplashActivity + animation
- [x] PinLockActivity (tạo/xác thực PIN, SharedPreferences)
- [x] MainActivity + Bottom Navigation (4 tabs)
- [x] HomeFragment – Dashboard tổng quan
- [x] PotsFragment + PotAdapter – Danh sách hủ
- [x] AddPotActivity/Dialog – Tạo hủ mới
- [x] PotDetailActivity – Chi tiết hủ
- [x] AddTransactionActivity – Thêm giao dịch
- [x] TransactionAdapter – Hiển thị giao dịch
- [x] DBManager mở rộng – CRUD đầy đủ
- [x] UI/UX: layouts, colors, strings (I18N)

### Phase 2: Enhanced Features ✅
- [x] Model Category + bảng CATEGORIES + seed data
- [x] TransactionsFragment – Lịch sử, filter, search
- [x] StatisticsActivity – Biểu đồ PieChart (MPAndroidChart)
- [x] Cảnh báo ngân sách (BudgetAlertDialog siêu đẹp + System Notification)
- [x] Sửa/Xóa giao dịch + hoàn lại balance
- [x] Chuyển tiền giữa các hủ (Speed Dial FAB + TransferDialog + logic kế toán kép)
- [x] Empty states cao cấp (Reusable layout + Emoji icons + CTA buttons)
- [x] Animations & transitions (Slide, Fade, Bounce)
- [x] CustomToast (Thay thế Toast mặc định bằng toast bo tròn có icon trạng thái)
- [x] NotificationHelper (Quản lý Notification Channel + Budget alerts)
- [x] ProfileFragment – Toggle bật/tắt cảnh báo ngân sách

### Phase 3: Advanced Features 🟢
- [x] Mục tiêu tiết kiệm (Plan A – Độc lập, Carousel trên Home)
- [x] Giao dịch định kỳ (Recurring Transactions + WorkManager)
- [x] Dark Mode toggle (lưu SharedPreferences + FinanceManagerApp restore)
- [ ] ExchangeRateAPI – Tỷ giá ngoại tệ
- [ ] Export báo cáo CSV
- [ ] Biometric authentication (Fingerprint)
- [ ] Testing & Performance optimization
