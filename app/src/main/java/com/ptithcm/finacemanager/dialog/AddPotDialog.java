package com.ptithcm.finacemanager.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.utils.Constants;

public class AddPotDialog extends DialogFragment {

    private TextInputEditText etPotName, etBudgetLimit;
    private TextInputLayout tilPotName, tilBudgetLimit;
    private TextView tvDialogTitle;
    private View viewPreviewBg;
    private TextView tvPreviewLetter;
    private ImageView ivPreviewIcon;
    private GridLayout glIconPicker;
    private LinearLayout llColorPicker;

    private String selectedColor = Constants.POT_COLORS[0];
    private String selectedIcon = "";
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
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        viewPreviewBg = view.findViewById(R.id.view_preview_bg);
        tvPreviewLetter = view.findViewById(R.id.tv_preview_letter);
        ivPreviewIcon = view.findViewById(R.id.iv_preview_icon);
        glIconPicker = view.findViewById(R.id.gl_icon_picker);
        llColorPicker = view.findViewById(R.id.ll_color_picker);

        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = view.findViewById(R.id.btn_save);

        etPotName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (isEditMode && editPot != null) {
            tvDialogTitle.setText(R.string.title_edit_pot);
            etPotName.setText(editPot.getName());
            etBudgetLimit.setText(String.valueOf((long) editPot.getBudgetLimit()));
            selectedColor = editPot.getColor() != null ? editPot.getColor() : Constants.POT_COLORS[0];
            selectedIcon = editPot.getIcon() != null ? editPot.getIcon() : "";
        }

        setupIconPicker();
        setupColorPicker();
        updatePreview();

        btnSave.setOnClickListener(v -> savePot());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int) (16 * getResources().getDisplayMetrics().density);
            android.graphics.drawable.Drawable bg = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_rounded);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.InsetDrawable(bg, margin));
        }
    }

    // ===== ICON PICKER (VECTOR) =====
    private void setupIconPicker() {
        glIconPicker.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (44 * density);
        int padding = (int) (10 * density);

        int potColor;
        try {
            potColor = Color.parseColor(selectedColor);
        } catch (Exception e) {
            potColor = Color.parseColor("#4CAF50");
        }

        for (String iconKey : Constants.POT_ICONS) {
            View iconContainer;
            
            if (iconKey.equals("EMPTY")) {
                TextView tv = new TextView(requireContext());
                tv.setText("Aa");
                tv.setTextSize(18f);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
                tv.setGravity(android.view.Gravity.CENTER);
                if (iconKey.equals(selectedIcon) || selectedIcon.isEmpty()) {
                    tv.setTextColor(potColor);
                } else {
                    tv.setTextColor(Color.parseColor("#9E9E9E"));
                }
                iconContainer = tv;
            } else {
                ImageView iv = new ImageView(requireContext());
                iv.setImageResource(com.ptithcm.finacemanager.utils.IconMapper.getIconResource(requireContext(), iconKey));
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                iv.setPadding(padding, padding, padding, padding);
                if (iconKey.equals(selectedIcon)) {
                    iv.setColorFilter(potColor);
                } else {
                    iv.setColorFilter(Color.parseColor("#9E9E9E"));
                }
                iconContainer = iv;
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = size;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(6, 6, 6, 6);
            iconContainer.setLayoutParams(params);

            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setShape(GradientDrawable.RECTANGLE);
            bgDrawable.setCornerRadius(14 * density); // Bo góc 14dp tạo hình vuông bo góc mềm mại

            if (iconKey.equals(selectedIcon) || (iconKey.equals("EMPTY") && selectedIcon.isEmpty())) {
                bgDrawable.setColor(Color.argb(38, Color.red(potColor), Color.green(potColor), Color.blue(potColor)));
            } else {
                bgDrawable.setColor(Color.parseColor("#F5F5F5"));
            }
            iconContainer.setBackground(bgDrawable);

            iconContainer.setOnClickListener(v -> {
                if (iconKey.equals("EMPTY")) {
                    selectedIcon = "";
                } else {
                    selectedIcon = iconKey;
                }
                setupIconPicker();
                updatePreview();
            });

            glIconPicker.addView(iconContainer);
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
                drawable.setStroke((int) (1.5f * density), Color.parseColor("#212121"));
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
        int potColor;
        try {
            potColor = Color.parseColor(selectedColor);
        } catch (Exception e) {
            potColor = Color.parseColor("#4CAF50");
        }

        boolean isVector = !selectedIcon.isEmpty();

        if (isVector) {
            tvPreviewLetter.setVisibility(View.GONE);
            ivPreviewIcon.setVisibility(View.VISIBLE);
            ivPreviewIcon.setImageResource(com.ptithcm.finacemanager.utils.IconMapper.getIconResource(requireContext(), selectedIcon));
            ivPreviewIcon.setColorFilter(potColor);
        } else {
            ivPreviewIcon.setVisibility(View.GONE);
            tvPreviewLetter.setVisibility(View.VISIBLE);
            String name = etPotName.getText() != null ? etPotName.getText().toString().trim() : "";
            String letter = "P";
            if (!name.isEmpty()) {
                letter = name.substring(0, 1).toUpperCase();
            }
            tvPreviewLetter.setText(letter);
            tvPreviewLetter.setTextColor(potColor);
        }

        GradientDrawable previewBg = new GradientDrawable();
        previewBg.setShape(GradientDrawable.OVAL);
        int alphaColor = Color.argb(38, // 15% opacity
                Color.red(potColor), Color.green(potColor), Color.blue(potColor));
        previewBg.setColor(alphaColor);
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
