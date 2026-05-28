package com.ptithcm.finacemanager.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Category;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CustomToast;
import com.ptithcm.finacemanager.utils.DateUtils;
import com.ptithcm.finacemanager.utils.NotificationHelper;
import com.ptithcm.finacemanager.dialog.BudgetAlertDialog;

import java.util.Calendar;
import java.util.List;

public class AddTransactionActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleType;
    private TextInputEditText etAmount, etDate, etNote;
    private TextInputLayout tilAmount, tilPot, tilCategory, tilDate;
    private AutoCompleteTextView spPot, spCategory;
    private MaterialButton btnSave;

    private DBManager dbManager;
    private List<Pot> potList;
    private List<Category> categoryList;

    private int selectedPotId = -1;
    private int selectedCategoryId = -1;
    private String selectedDate;
    private String currentType = Constants.TYPE_EXPENSE;

    // Nếu mở từ PotDetail, nhận potId để tự chọn
    private int preselectedPotId = -1;

    // Nếu mở để sửa giao dịch
    private int editTransactionId = -1;
    private Transaction editTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbManager = DBManager.getInstance(this);
        preselectedPotId = getIntent().getIntExtra(Constants.EXTRA_POT_ID, -1);
        editTransactionId = getIntent().getIntExtra(Constants.EXTRA_TRANSACTION_ID, -1);

        initViews();
        initListeners();
        loadData();

        if (editTransactionId != -1) {
            setupEditMode();
        } else {
            // Mặc định ngày hôm nay cho chế độ tạo mới
            selectedDate = DateUtils.getTodayDB();
            etDate.setText(DateUtils.formatForDisplay(selectedDate));
        }
    }

    private void initViews() {
        toggleType = findViewById(R.id.toggle_type);
        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        etNote = findViewById(R.id.et_note);
        tilAmount = findViewById(R.id.til_amount);
        tilPot = findViewById(R.id.til_pot);
        tilCategory = findViewById(R.id.til_category);
        tilDate = findViewById(R.id.til_date);
        spPot = findViewById(R.id.sp_pot);
        spCategory = findViewById(R.id.sp_category);
        btnSave = findViewById(R.id.btn_save);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void initListeners() {
        // Toggle INCOME / EXPENSE
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_income) {
                    currentType = Constants.TYPE_INCOME;
                } else {
                    currentType = Constants.TYPE_EXPENSE;
                }
                loadCategories();
            }
        });

        // Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Pot selection
        spPot.setOnItemClickListener((parent, view, position, id) -> {
            if (position < potList.size()) {
                selectedPotId = potList.get(position).getId();
            }
        });

        // Category selection
        spCategory.setOnItemClickListener((parent, view, position, id) -> {
            if (position < categoryList.size()) {
                selectedCategoryId = categoryList.get(position).getId();
            }
        });

        // Save
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void loadData() {
        // Load pots
        potList = dbManager.getAllActivePots();
        String[] potNames = new String[potList.size()];
        for (int i = 0; i < potList.size(); i++) {
            potNames[i] = potList.get(i).getName();
        }
        ArrayAdapter<String> potAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, potNames);
        spPot.setAdapter(potAdapter);

        // Tự chọn pot nếu mở từ PotDetail
        if (preselectedPotId != -1) {
            for (int i = 0; i < potList.size(); i++) {
                if (potList.get(i).getId() == preselectedPotId) {
                    spPot.setText(potList.get(i).getName(), false);
                    selectedPotId = preselectedPotId;
                    break;
                }
            }
        }

        // Load categories
        loadCategories();
    }

    private void loadCategories() {
        categoryList = dbManager.getCategoriesByType(currentType);
        String[] catNames = new String[categoryList.size()];
        for (int i = 0; i < categoryList.size(); i++) {
            catNames[i] = categoryList.get(i).getLocalizedName(this);
        }
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, catNames);
        spCategory.setAdapter(catAdapter);

        // Reset selection nếu không phải đang set up Edit Mode lần đầu
        if (editTransaction == null || !currentType.equals(editTransaction.getType()) || spCategory.getText().toString().isEmpty()) {
            spCategory.setText("", false);
            selectedCategoryId = -1;
        }
    }

    private void setupEditMode() {
        editTransaction = dbManager.getTransactionById(editTransactionId);
        if (editTransaction == null) {
            CustomToast.showError(this, "Không tìm thấy giao dịch");
            finish();
            return;
        }

        // Đổi tiêu đề và nút
        TextView tvTitle = findViewById(R.id.tv_toolbar_title);
        if (tvTitle != null) tvTitle.setText(R.string.title_edit_transaction);
        btnSave.setText(R.string.btn_save);

        // Nạp dữ liệu cơ bản
        currentType = editTransaction.getType();
        if (Constants.TYPE_INCOME.equals(currentType)) {
            toggleType.check(R.id.btn_income);
        } else {
            toggleType.check(R.id.btn_expense);
        }
        
        // Vô hiệu hóa việc đổi loại giao dịch (Thu/Chi) khi đang sửa để đảm bảo logic hoàn tiền không bị rối
        findViewById(R.id.btn_income).setEnabled(false);
        findViewById(R.id.btn_expense).setEnabled(false);

        etAmount.setText(String.valueOf(editTransaction.getAmount()));
        if (editTransaction.getNote() != null) {
            etNote.setText(editTransaction.getNote());
        }

        selectedDate = editTransaction.getDate();
        etDate.setText(DateUtils.formatForDisplay(selectedDate));

        // Nạp Hủ
        selectedPotId = editTransaction.getPotId();
        for (Pot pot : potList) {
            if (pot.getId() == selectedPotId) {
                spPot.setText(pot.getName(), false);
                break;
            }
        }

        // Load categories dựa trên type mới và nạp danh mục
        loadCategories();
        selectedCategoryId = editTransaction.getCategoryId();
        for (Category cat : categoryList) {
            if (cat.getId() == selectedCategoryId) {
                spCategory.setText(cat.getLocalizedName(this), false);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etDate.setText(DateUtils.formatForDisplay(selectedDate));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveTransaction() {
        // Validate amount
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
            tilAmount.setError(null);
        } catch (NumberFormatException e) {
            tilAmount.setError(getString(R.string.error_amount_invalid));
            return;
        }

        // Validate pot
        if (selectedPotId == -1) {
            tilPot.setError(getString(R.string.error_pot_not_selected));
            return;
        }
        tilPot.setError(null);

        // Validate category
        if (selectedCategoryId == -1) {
            tilCategory.setError(getString(R.string.error_category_not_selected));
            return;
        }
        tilCategory.setError(null);

        // Tạo transaction
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        if (editTransactionId != -1) {
            Transaction updatedTransaction = new Transaction(
                    selectedPotId, selectedCategoryId, amount, currentType, selectedDate, note);
            updatedTransaction.setId(editTransactionId);
            dbManager.updateTransaction(updatedTransaction);
            CustomToast.showSuccess(this, getString(R.string.msg_transaction_saved));
        } else {
            Transaction transaction = new Transaction(
                    selectedPotId, selectedCategoryId, amount, currentType, selectedDate, note);
            dbManager.addTransaction(transaction);
            CustomToast.showSuccess(this, getString(R.string.msg_transaction_saved));
        }

        // Kiểm tra ngân sách và hiện cảnh báo nếu cần (chỉ cho EXPENSE)
        if (Constants.TYPE_EXPENSE.equals(currentType)) {
            checkBudgetAndAlert();
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Kiểm tra ngân sách hũ sau khi thêm/sửa giao dịch chi tiêu.
     * Nếu vượt ngưỡng 80% hoặc 100%, hiển thị BudgetAlertDialog siêu đẹp
     * và gửi System Notification.
     */
    private void checkBudgetAndAlert() {
        Pot pot = dbManager.getPotById(selectedPotId);
        if (pot == null || pot.getBudgetLimit() <= 0) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        double budgetLimit = pot.getBudgetLimit();
        double currentBalance = pot.getBalance();
        double spent = budgetLimit - currentBalance;
        if (spent < 0) spent = 0;
        double spentPercentage = spent / budgetLimit;

        if (spentPercentage >= NotificationHelper.NOTIFICATION_DANGER_THRESHOLD) {
            // >= 100%: Hiện dialog cảnh báo MỨC NGUY HIỂM
            showBudgetAlert(pot.getName(), budgetLimit, currentBalance, spentPercentage, true);
            NotificationHelper.checkAndNotifyBudget(this, pot.getId(), pot.getName(), budgetLimit, currentBalance);
        } else if (spentPercentage >= NotificationHelper.NOTIFICATION_WARNING_THRESHOLD) {
            // >= 80%: Hiện dialog cảnh báo MỨC CẢNH BÁO
            showBudgetAlert(pot.getName(), budgetLimit, currentBalance, spentPercentage, false);
            NotificationHelper.checkAndNotifyBudget(this, pot.getId(), pot.getName(), budgetLimit, currentBalance);
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Hiển thị BudgetAlertDialog siêu đẹp. Khi người dùng bấm "Tôi đã hiểu",
     * dialog tự đóng và Activity sẽ finish().
     */
    private void showBudgetAlert(String potName, double budgetLimit,
                                  double currentBalance, double spentPercentage, boolean isDanger) {
        BudgetAlertDialog dialog = BudgetAlertDialog.newInstance(
                potName, budgetLimit, currentBalance, spentPercentage, isDanger);

        // Lắng nghe khi dialog bị dismiss (bấm "Tôi đã hiểu" hoặc bấm ngoài) → finish Activity
        dialog.setOnDismissListener(d -> {
            setResult(RESULT_OK);
            finish();
        });

        dialog.show(getSupportFragmentManager(), "BudgetAlertDialog");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
