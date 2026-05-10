package com.ptithcm.finacemanager.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.utils.Constants;

public class AddPotDialog extends DialogFragment {

    private TextInputEditText etPotName, etBudgetLimit;
    private TextInputLayout tilPotName, tilBudgetLimit;
    private LinearLayout llColorPicker;
    private GridLayout glIconPicker;
    private View viewPreviewBg;
    private TextView tvPreviewIcon, tvDialogTitle;

    private String selectedColor = Constants.POT_COLORS[0];
    private String selectedIcon = Constants.DEFAULT_POT_ICON;
    private OnPotSavedListener listener;

    // Edit mode
    private Pot editPot = null;
    private boolean isEditMode = false;

    public interface OnPotSavedListener {
        void onPotSaved();
    }

    public void setOnPotSavedListener(OnPotSavedListener listener) {
        this.listener = listener;
    }

    /**
     * Đặt pot để chỉnh sửa. Gọi trước show().
     */
    public void setEditPot(Pot pot) {
        this.editPot = pot;
        this.isEditMode = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_pot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etPotName = view.findViewById(R.id.et_pot_name);
        etBudgetLimit = view.findViewById(R.id.et_budget_limit);
        tilPotName = view.findViewById(R.id.til_pot_name);
        tilBudgetLimit = view.findViewById(R.id.til_budget_limit);
        llColorPicker = view.findViewById(R.id.ll_color_picker);
        glIconPicker = view.findViewById(R.id.gl_icon_picker);
        viewPreviewBg = view.findViewById(R.id.view_preview_bg);
        tvPreviewIcon = view.findViewById(R.id.tv_preview_icon);
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);

        // Nếu edit mode, pre-fill dữ liệu
        if (isEditMode && editPot != null) {
            tvDialogTitle.setText(R.string.title_edit_pot);
            etPotName.setText(editPot.getName());
            etBudgetLimit.setText(String.valueOf((long) editPot.getBudgetLimit()));
            selectedColor = editPot.getColor() != null ? editPot.getColor() : Constants.POT_COLORS[0];

            String icon = editPot.getIcon();
            if (icon != null && !icon.isEmpty() && !icon.equals("ic_default")) {
                selectedIcon = icon;
            }
        }

        setupIconPicker();
        setupColorPicker();
        updatePreview();

        view.findViewById(R.id.btn_save).setOnClickListener(v -> savePot());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card_rounded);
        }
    }

    // ===== ICON PICKER =====
    private void setupIconPicker() {
        glIconPicker.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (4 * density);

        for (String icon : Constants.POT_ICONS) {
            TextView iconView = new TextView(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            iconView.setLayoutParams(params);
            iconView.setText(icon);
            iconView.setTextSize(20);
            iconView.setGravity(android.view.Gravity.CENTER);

            if (icon.equals(selectedIcon)) {
                GradientDrawable selectedBg = new GradientDrawable();
                selectedBg.setShape(GradientDrawable.OVAL);
                selectedBg.setColor(Color.parseColor("#E8F5E9"));
                selectedBg.setStroke((int) (2 * density), Color.parseColor(selectedColor));
                iconView.setBackground(selectedBg);
            } else {
                GradientDrawable normalBg = new GradientDrawable();
                normalBg.setShape(GradientDrawable.OVAL);
                normalBg.setColor(Color.parseColor("#F5F5F5"));
                iconView.setBackground(normalBg);
            }

            iconView.setOnClickListener(v -> {
                selectedIcon = icon;
                setupIconPicker();
                updatePreview();
            });

            glIconPicker.addView(iconView);
        }
    }

    // ===== COLOR PICKER =====
    private void setupColorPicker() {
        llColorPicker.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (36 * density);
        int margin = (int) (5 * density);

        for (String color : Constants.POT_COLORS) {
            View colorView = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            colorView.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(color));

            if (color.equals(selectedColor)) {
                drawable.setStroke((int) (3 * density), Color.parseColor("#212121"));
            }

            colorView.setBackground(drawable);
            colorView.setOnClickListener(v -> {
                selectedColor = color;
                setupColorPicker();
                setupIconPicker();
                updatePreview();
            });

            llColorPicker.addView(colorView);
        }
    }

    // ===== PREVIEW =====
    private void updatePreview() {
        tvPreviewIcon.setText(selectedIcon);

        int potColor;
        try {
            potColor = Color.parseColor(selectedColor);
        } catch (Exception e) {
            potColor = Color.parseColor("#4CAF50");
        }

        GradientDrawable previewBg = new GradientDrawable();
        previewBg.setShape(GradientDrawable.OVAL);
        int alphaColor = Color.argb(40,
                Color.red(potColor), Color.green(potColor), Color.blue(potColor));
        previewBg.setColor(alphaColor);
        previewBg.setStroke(3, potColor);
        viewPreviewBg.setBackground(previewBg);
    }

    // ===== SAVE =====
    private void savePot() {
        String name = etPotName.getText() != null ? etPotName.getText().toString().trim() : "";
        String budgetStr = etBudgetLimit.getText() != null ? etBudgetLimit.getText().toString().trim() : "";

        // Validation
        if (name.isEmpty()) {
            tilPotName.setError(getString(R.string.error_pot_name_empty));
            return;
        }
        tilPotName.setError(null);

        double budget;
        try {
            budget = Double.parseDouble(budgetStr);
            if (budget <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilBudgetLimit.setError(getString(R.string.error_budget_invalid));
            return;
        }
        tilBudgetLimit.setError(null);

        DBManager db = DBManager.getInstance(requireContext());

        if (isEditMode && editPot != null) {
            // === EDIT MODE ===
            editPot.setName(name);
            editPot.setBudgetLimit(budget);
            editPot.setColor(selectedColor);
            editPot.setIcon(selectedIcon);
            db.updatePot(editPot);
            Toast.makeText(requireContext(), R.string.msg_pot_updated, Toast.LENGTH_SHORT).show();
        } else {
            // === CREATE MODE ===
            Pot pot = new Pot(name, budget, selectedColor);
            pot.setIcon(selectedIcon);
            db.addPot(pot);
            Toast.makeText(requireContext(), R.string.msg_pot_created, Toast.LENGTH_SHORT).show();
        }

        if (listener != null) listener.onPotSaved();
        dismiss();
    }
}
