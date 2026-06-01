package com.ptithcm.finacemanager.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.CustomToast;
import com.ptithcm.finacemanager.utils.NotificationHelper;
import com.ptithcm.finacemanager.adapter.PotDropdownAdapter;
import android.os.SystemClock;

import java.util.List;

/**
 * Dialog cho tính năng Chuyển tiền giữa các hủ.
 * Áp dụng logic kế toán kép (double-entry):
 * - 1 giao dịch Chi (EXPENSE) ở Hủ Nguồn
 * - 1 giao dịch Thu (INCOME) ở Hủ Đích
 */
public class TransferDialog extends DialogFragment {

    private TextInputLayout textInputLayoutSourcePot;
    private TextInputLayout textInputLayoutDestinationPot;
    private TextInputLayout textInputLayoutAmount;
    private TextInputLayout textInputLayoutNote;
    private AutoCompleteTextView autoCompleteSourcePot;
    private AutoCompleteTextView autoCompleteDestinationPot;
    private TextInputEditText editTextAmount;
    private TextInputEditText editTextNote;

    private DBManager databaseManager;
    private List<Pot> activePotList;

    private int selectedSourcePotId = -1;
    private int selectedDestinationPotId = -1;
    private long lastClickTime = 0;

    private OnTransferCompleteListener transferCompleteListener;

    /**
     * Interface callback khi chuyển tiền thành công.
     * Fragment/Activity gọi loadData() để refresh UI.
     */
    public interface OnTransferCompleteListener {
        void onTransferComplete();
    }

    public void setOnTransferCompleteListener(OnTransferCompleteListener listener) {
        this.transferCompleteListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_transfer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseManager = DBManager.getInstance(requireContext());

        initViews(view);
        loadPotData();
        initListeners();
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

    private void initViews(View view) {
        textInputLayoutSourcePot = view.findViewById(R.id.til_source_pot);
        textInputLayoutDestinationPot = view.findViewById(R.id.til_dest_pot);
        textInputLayoutAmount = view.findViewById(R.id.til_amount);
        textInputLayoutNote = view.findViewById(R.id.til_note);
        autoCompleteSourcePot = view.findViewById(R.id.sp_source_pot);
        autoCompleteDestinationPot = view.findViewById(R.id.sp_dest_pot);
        editTextAmount = view.findViewById(R.id.et_amount);
        editTextNote = view.findViewById(R.id.et_note);
    }

    /**
     * Nạp danh sách hủ đang hoạt động vào cả 2 dropdown bằng custom adapter.
     */
    private void loadPotData() {
        activePotList = databaseManager.getAllActivePots();

        PotDropdownAdapter sourcePotAdapter = new PotDropdownAdapter(requireContext(), activePotList, true);
        autoCompleteSourcePot.setAdapter(sourcePotAdapter);

        PotDropdownAdapter destinationPotAdapter = new PotDropdownAdapter(requireContext(), activePotList, true);
        autoCompleteDestinationPot.setAdapter(destinationPotAdapter);
    }

    private void initListeners() {
        // Chọn hủ nguồn
        autoCompleteSourcePot.setOnItemClickListener((parent, view, position, id) -> {
            if (position < activePotList.size()) {
                selectedSourcePotId = activePotList.get(position).getId();
                textInputLayoutSourcePot.setError(null);
            }
        });

        // Chọn hủ đích
        autoCompleteDestinationPot.setOnItemClickListener((parent, view, position, id) -> {
            if (position < activePotList.size()) {
                selectedDestinationPotId = activePotList.get(position).getId();
                textInputLayoutDestinationPot.setError(null);
            }
        });

        // Nút Chuyển tiền
        requireView().findViewById(R.id.btn_transfer).setOnClickListener(view -> executeTransfer());

        // Nút Hủy
        requireView().findViewById(R.id.btn_cancel).setOnClickListener(view -> dismiss());
    }

    /**
     * Xác thực đầu vào và thực hiện chuyển tiền.
     */
    private void executeTransfer() {
        // Chống double click
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
        lastClickTime = SystemClock.elapsedRealtime();

        // Reset lỗi
        textInputLayoutSourcePot.setError(null);
        textInputLayoutDestinationPot.setError(null);
        textInputLayoutAmount.setError(null);

        // Validate hủ nguồn
        if (selectedSourcePotId == -1) {
            textInputLayoutSourcePot.setError(getString(R.string.error_source_pot_not_selected));
            return;
        }

        // Validate hủ đích
        if (selectedDestinationPotId == -1) {
            textInputLayoutDestinationPot.setError(getString(R.string.error_dest_pot_not_selected));
            return;
        }

        // Validate hủ nguồn khác hủ đích
        if (selectedSourcePotId == selectedDestinationPotId) {
            textInputLayoutDestinationPot.setError(getString(R.string.error_same_pot));
            return;
        }

        // Validate số tiền
        String amountText = editTextAmount.getText() != null
                ? editTextAmount.getText().toString().trim() : "";
        double transferAmount;
        try {
            transferAmount = Double.parseDouble(amountText);
            if (transferAmount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            textInputLayoutAmount.setError(getString(R.string.error_amount_invalid));
            return;
        }

        // Kiểm tra số dư hủ nguồn
        Pot sourcePot = databaseManager.getPotById(selectedSourcePotId);
        if (sourcePot != null && sourcePot.getBalance() < transferAmount) {
            textInputLayoutAmount.setError(getString(R.string.error_insufficient_balance));
            return;
        }

        // Lấy ghi chú
        String note = editTextNote.getText() != null
                ? editTextNote.getText().toString().trim() : "";

        // Thực hiện chuyển tiền (atomic trong DBManager)
        boolean transferSuccess = databaseManager.transferMoney(
                selectedSourcePotId, selectedDestinationPotId, transferAmount, note);

        if (transferSuccess) {
            CustomToast.showSuccess(requireContext(), getString(R.string.msg_transfer_success));

            // Kiểm tra ngân sách hủ nguồn sau khi chuyển tiền (vì tiền bị trừ đi)
            checkBudgetAfterTransfer();

            if (transferCompleteListener != null) {
                transferCompleteListener.onTransferComplete();
            }
            dismiss();
        } else {
            CustomToast.showError(requireContext(), getString(R.string.error_insufficient_balance));
        }
    }

    /**
     * Kiểm tra ngân sách hủ nguồn sau khi chuyển tiền.
     * Chuyển tiền làm giảm số dư hủ nguồn, nên cần kiểm tra xem
     * hủ nguồn có vượt ngưỡng ngân sách không và gửi System Notification.
     *
     * <p>Lưu ý: Không hiện BudgetAlertDialog ở đây vì TransferDialog là DialogFragment
     * (không phải Activity). Chỉ gửi System Notification.
     */
    private void checkBudgetAfterTransfer() {
        Pot sourcePot = databaseManager.getPotById(selectedSourcePotId);
        if (sourcePot != null && sourcePot.getBudgetLimit() > 0) {
            NotificationHelper.checkAndNotifyBudget(
                    requireContext(),
                    sourcePot.getId(),
                    sourcePot.getName(),
                    sourcePot.getBudgetLimit(),
                    sourcePot.getBalance()
            );
        }
    }
}
