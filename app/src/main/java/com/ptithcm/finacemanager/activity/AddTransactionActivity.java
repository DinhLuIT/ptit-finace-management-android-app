package com.ptithcm.finacemanager.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.ptithcm.finacemanager.utils.DateUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbManager = DBManager.getInstance(this);
        preselectedPotId = getIntent().getIntExtra(Constants.EXTRA_POT_ID, -1);

        initViews();
        initListeners();
        loadData();

        // Mặc định ngày hôm nay
        selectedDate = DateUtils.getTodayDB();
        etDate.setText(DateUtils.formatForDisplay(selectedDate));
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

        // Reset selection
        spCategory.setText("", false);
        selectedCategoryId = -1;
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

        Transaction transaction = new Transaction(
                selectedPotId, selectedCategoryId, amount, currentType, selectedDate, note);

        dbManager.addTransaction(transaction);

        Toast.makeText(this, R.string.msg_transaction_saved, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
