package com.ptithcm.finacemanager.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Category;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.model.RecurringTransaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CustomToast;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Dialog thêm giao dịch định kỳ mới.
 *
 * <p>Cho phép người dùng cấu hình:
 * <ul>
 *     <li>Loại giao dịch (Thu/Chi)</li>
 *     <li>Số tiền</li>
 *     <li>Hủ (Pot)</li>
 *     <li>Danh mục</li>
 *     <li>Tần suất (Hàng ngày/tuần/tháng/năm)</li>
 *     <li>Ngày bắt đầu lặp</li>
 *     <li>Ghi chú</li>
 * </ul>
 */
public class AddRecurringDialog extends DialogFragment {

    private DBManager dbManager;
    private String currentType = Constants.TYPE_EXPENSE;
    private int selectedPotId = -1;
    private int selectedCategoryId = -1;
    private String selectedFrequency = null;
    private String selectedStartDate;

    private List<Pot> potList;
    private List<Category> categoryList;

    // Frequency mapping
    private String[] frequencyKeys;
    private String[] frequencyLabels;

    private OnRecurringSavedListener onSavedListener;

    public interface OnRecurringSavedListener {
        void onRecurringSaved();
    }

    public void setOnRecurringSavedListener(OnRecurringSavedListener listener) {
        this.onSavedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_recurring, null);

        dbManager = DBManager.getInstance(requireContext());

        // Init frequency arrays
        frequencyKeys = new String[]{
                Constants.FREQ_DAILY, Constants.FREQ_WEEKLY,
                Constants.FREQ_MONTHLY, Constants.FREQ_YEARLY
        };
        frequencyLabels = new String[]{
                getString(R.string.freq_daily), getString(R.string.freq_weekly),
                getString(R.string.freq_monthly), getString(R.string.freq_yearly)
        };

        // Find views
        MaterialButtonToggleGroup toggleType = view.findViewById(R.id.toggle_type);
        TextInputLayout tilAmount = view.findViewById(R.id.til_amount);
        TextInputEditText etAmount = view.findViewById(R.id.et_amount);
        TextInputLayout tilPot = view.findViewById(R.id.til_pot);
        AutoCompleteTextView spPot = view.findViewById(R.id.sp_pot);
        TextInputLayout tilCategory = view.findViewById(R.id.til_category);
        AutoCompleteTextView spCategory = view.findViewById(R.id.sp_category);
        TextInputLayout tilFrequency = view.findViewById(R.id.til_frequency);
        AutoCompleteTextView spFrequency = view.findViewById(R.id.sp_frequency);
        TextInputEditText etStartDate = view.findViewById(R.id.et_start_date);
        TextInputEditText etNote = view.findViewById(R.id.et_note);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = view.findViewById(R.id.btn_save);

        // Default start date = today
        selectedStartDate = DateUtils.getTodayDB();
        etStartDate.setText(DateUtils.formatForDisplay(selectedStartDate));

        // Load pots
        potList = dbManager.getAllActivePots();
        String[] potNames = new String[potList.size()];
        for (int i = 0; i < potList.size(); i++) {
            potNames[i] = potList.get(i).getName();
        }
        spPot.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, potNames));

        // Load categories
        loadCategories(spCategory);

        // Frequency dropdown
        spFrequency.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, frequencyLabels));

        // === Listeners ===

        // Type toggle
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                currentType = (checkedId == R.id.btn_income)
                        ? Constants.TYPE_INCOME : Constants.TYPE_EXPENSE;
                loadCategories(spCategory);
            }
        });

        // Pot selection
        spPot.setOnItemClickListener((parent, v, position, id) -> {
            if (position < potList.size()) {
                selectedPotId = potList.get(position).getId();
            }
        });

        // Category selection
        spCategory.setOnItemClickListener((parent, v, position, id) -> {
            if (position < categoryList.size()) {
                selectedCategoryId = categoryList.get(position).getId();
            }
        });

        // Frequency selection
        spFrequency.setOnItemClickListener((parent, v, position, id) -> {
            if (position < frequencyKeys.length) {
                selectedFrequency = frequencyKeys[position];
            }
        });

        // Date picker
        etStartDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (dp, year, month, day) -> {
                        selectedStartDate = String.format("%04d-%02d-%02d", year, month + 1, day);
                        etStartDate.setText(DateUtils.formatForDisplay(selectedStartDate));
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // Cancel
        btnCancel.setOnClickListener(v -> dismiss());

        // Save
        btnSave.setOnClickListener(v -> {
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

            // Validate frequency
            if (selectedFrequency == null) {
                tilFrequency.setError(getString(R.string.error_frequency_not_selected));
                return;
            }
            tilFrequency.setError(null);

            // Build and save
            String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
            RecurringTransaction recurring = new RecurringTransaction(
                    selectedPotId, selectedCategoryId, amount,
                    currentType, note, selectedFrequency, selectedStartDate
            );
            recurring.setCreatedAt(DateUtils.getNowDB());
            dbManager.addRecurringTransaction(recurring);

            CustomToast.showSuccess(requireContext(), getString(R.string.msg_recurring_created));
            if (onSavedListener != null) onSavedListener.onRecurringSaved();
            dismiss();
        });

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .create();
    }

    private void loadCategories(AutoCompleteTextView spCategory) {
        categoryList = dbManager.getCategoriesByType(currentType);
        String[] catNames = new String[categoryList.size()];
        for (int i = 0; i < categoryList.size(); i++) {
            catNames[i] = categoryList.get(i).getLocalizedName(requireContext());
        }
        spCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, catNames));
        spCategory.setText("", false);
        selectedCategoryId = -1;
    }
}
