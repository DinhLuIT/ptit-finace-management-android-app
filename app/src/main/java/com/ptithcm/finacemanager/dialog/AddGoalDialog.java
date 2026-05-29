package com.ptithcm.finacemanager.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.SavingsGoal;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CustomToast;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Dialog tạo/sửa mục tiêu tiết kiệm (Plan A – Độc lập).
 *
 * <p>Form nhập: Tên, Số tiền mục tiêu, Ngày hoàn thành, Icon, Màu sắc.
 * Khi nhấn Lưu → validate → gọi callback {@link OnGoalSavedListener}.
 */
public class AddGoalDialog extends DialogFragment {

    private TextInputLayout tilGoalName, tilTargetAmount, tilTargetDate;
    private TextInputEditText etGoalName, etTargetAmount, etTargetDate;
    private MaterialButton btnSave, btnCancel;
    private TextView tvDialogTitle;
    private GridLayout gridIcons, gridColors;

    private SavingsGoal editGoal; // null nếu đang tạo mới
    private String selectedDate;
    private String selectedIcon = Constants.DEFAULT_GOAL_ICON;
    private String selectedColor = Constants.DEFAULT_GOAL_COLOR;
    private OnGoalSavedListener listener;

    /**
     * Callback khi mục tiêu được lưu thành công.
     */
    public interface OnGoalSavedListener {
        void onGoalSaved();
    }

    public void setOnGoalSavedListener(OnGoalSavedListener listener) {
        this.listener = listener;
    }

    /**
     * Đặt mục tiêu cần sửa (chế độ Edit). Gọi trước show().
     */
    public void setEditGoal(SavingsGoal goal) {
        this.editGoal = goal;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupIconPicker(view);
        setupColorPicker(view);
        populateEditData();
        initListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initViews(View view) {
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        tilGoalName = view.findViewById(R.id.til_goal_name);
        tilTargetAmount = view.findViewById(R.id.til_target_amount);
        tilTargetDate = view.findViewById(R.id.til_target_date);
        etGoalName = view.findViewById(R.id.et_goal_name);
        etTargetAmount = view.findViewById(R.id.et_target_amount);
        etTargetDate = view.findViewById(R.id.et_target_date);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        gridIcons = view.findViewById(R.id.grid_icons);
        gridColors = view.findViewById(R.id.grid_colors);
    }

    /**
     * Tạo grid chọn icon emoji.
     */
    private void setupIconPicker(View root) {
        gridIcons.removeAllViews();
        for (String icon : Constants.GOAL_ICONS) {
            TextView tv = new TextView(requireContext());
            tv.setText(icon);
            tv.setTextSize(24);
            tv.setPadding(16, 12, 16, 12);
            tv.setBackground(createIconBackground(icon.equals(selectedIcon)));
            tv.setOnClickListener(v -> {
                selectedIcon = icon;
                refreshIconSelection();
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            tv.setLayoutParams(params);
            tv.setGravity(android.view.Gravity.CENTER);

            gridIcons.addView(tv);
        }
    }

    /**
     * Tạo grid chọn màu sắc.
     */
    private void setupColorPicker(View root) {
        gridColors.removeAllViews();
        for (String color : Constants.GOAL_COLORS) {
            View circle = new View(requireContext());
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(color));
            if (color.equals(selectedColor)) {
                drawable.setStroke(4, Color.parseColor("#212121"));
            }
            circle.setBackground(drawable);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 48;
            params.height = 48;
            params.setMargins(12, 8, 12, 8);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            circle.setLayoutParams(params);

            circle.setOnClickListener(v -> {
                selectedColor = color;
                refreshColorSelection();
            });

            gridColors.addView(circle);
        }
    }

    private void refreshIconSelection() {
        for (int i = 0; i < gridIcons.getChildCount(); i++) {
            TextView tv = (TextView) gridIcons.getChildAt(i);
            tv.setBackground(createIconBackground(
                    Constants.GOAL_ICONS[i].equals(selectedIcon)));
        }
    }

    private void refreshColorSelection() {
        for (int i = 0; i < gridColors.getChildCount(); i++) {
            View v = gridColors.getChildAt(i);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(Constants.GOAL_COLORS[i]));
            if (Constants.GOAL_COLORS[i].equals(selectedColor)) {
                drawable.setStroke(4, Color.parseColor("#212121"));
            }
            v.setBackground(drawable);
        }
    }

    private GradientDrawable createIconBackground(boolean isSelected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(12);
        if (isSelected) {
            bg.setColor(Color.parseColor("#E8F5E9"));
            bg.setStroke(2, Color.parseColor("#4CAF50"));
        } else {
            bg.setColor(Color.TRANSPARENT);
        }
        return bg;
    }

    /**
     * Điền dữ liệu khi ở chế độ sửa.
     */
    private void populateEditData() {
        if (editGoal != null) {
            tvDialogTitle.setText(R.string.title_edit_goal);
            etGoalName.setText(editGoal.getName());
            etTargetAmount.setText(String.valueOf((long) editGoal.getTargetAmount()));

            if (editGoal.getTargetDate() != null && !editGoal.getTargetDate().isEmpty()) {
                selectedDate = editGoal.getTargetDate();
                etTargetDate.setText(DateUtils.formatForDisplay(selectedDate));
            }

            selectedIcon = editGoal.getIcon() != null ? editGoal.getIcon() : Constants.DEFAULT_GOAL_ICON;
            selectedColor = editGoal.getColor() != null ? editGoal.getColor() : Constants.DEFAULT_GOAL_COLOR;
            refreshIconSelection();
            refreshColorSelection();
        }
    }

    private void initListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveGoal());
        etTargetDate.setOnClickListener(v -> showDatePicker());
        tilTargetDate.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null && !selectedDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                calendar.setTime(sdf.parse(selectedDate));
            } catch (Exception ignored) { }
        }

        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    etTargetDate.setText(DateUtils.formatForDisplay(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000L);
        picker.show();
    }

    private void saveGoal() {
        String name = etGoalName.getText() != null ? etGoalName.getText().toString().trim() : "";
        String amountStr = etTargetAmount.getText() != null ? etTargetAmount.getText().toString().trim() : "";

        if (name.isEmpty()) {
            tilGoalName.setError(getString(R.string.error_goal_name_empty));
            return;
        }
        tilGoalName.setError(null);

        double targetAmount;
        try {
            targetAmount = Double.parseDouble(amountStr);
            if (targetAmount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilTargetAmount.setError(getString(R.string.error_target_amount_invalid));
            return;
        }
        tilTargetAmount.setError(null);

        // Ngày là tùy chọn (không bắt buộc)
        tilTargetDate.setError(null);

        DBManager db = DBManager.getInstance(requireContext());

        if (editGoal != null) {
            editGoal.setName(name);
            editGoal.setTargetAmount(targetAmount);
            editGoal.setTargetDate(selectedDate);
            editGoal.setIcon(selectedIcon);
            editGoal.setColor(selectedColor);
            db.updateSavingsGoal(editGoal);
            CustomToast.showSuccess(requireContext(), getString(R.string.msg_goal_updated));
        } else {
            SavingsGoal goal = new SavingsGoal(name, targetAmount, selectedDate,
                    selectedIcon, selectedColor);
            db.addSavingsGoal(goal);
            CustomToast.showSuccess(requireContext(), getString(R.string.msg_goal_created));
        }

        if (listener != null) {
            listener.onGoalSaved();
        }
        dismiss();
    }
}
