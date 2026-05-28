package com.ptithcm.finacemanager.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.activity.AddTransactionActivity;
import com.ptithcm.finacemanager.adapter.TransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionsFragment extends Fragment {

    private RecyclerView recyclerViewAllTransactions;
    private Chip chipFilterAll, chipFilterIncome, chipFilterExpense;
    private TextInputEditText editTextSearch;

    // Empty State views
    private View emptyStateContainer;
    private TextView tvEmptyIcon, tvEmptyTitle, tvEmptySubtitle;
    private MaterialButton btnEmptyAction;

    private DBManager databaseManager;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> allTransactions = new ArrayList<>();

    private String currentType = null;
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseManager = DBManager.getInstance(requireContext());
        initViews(view);
        initListeners();
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        recyclerViewAllTransactions = view.findViewById(R.id.rv_all_transactions);
        chipFilterAll = view.findViewById(R.id.chip_all);
        chipFilterIncome = view.findViewById(R.id.chip_income);
        chipFilterExpense = view.findViewById(R.id.chip_expense);
        editTextSearch = view.findViewById(R.id.et_search);

        // Empty State – sử dụng layout tái sử dụng
        emptyStateContainer = view.findViewById(R.id.include_empty_state);
        tvEmptyIcon = emptyStateContainer.findViewById(R.id.tv_empty_icon);
        tvEmptyTitle = emptyStateContainer.findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = emptyStateContainer.findViewById(R.id.tv_empty_subtitle);
        btnEmptyAction = emptyStateContainer.findViewById(R.id.btn_empty_action);
    }

    private void initListeners() {
        chipFilterAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentType = null;
                applyFilters();
            }
        });
        chipFilterIncome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentType = Constants.TYPE_INCOME;
                applyFilters();
            }
        });
        chipFilterExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentType = Constants.TYPE_EXPENSE;
                applyFilters();
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence textSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence textSequence, int start, int before, int count) {
                currentQuery = textSequence.toString().trim().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable editableText) {}
        });

        // Nút hành động trong Empty State – đặt lại bộ lọc
        btnEmptyAction.setOnClickListener(v -> resetFilters());
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(allTransactions, requireContext());
        transactionAdapter.setOnTransactionClickListener(this::showTransactionOptionsDialog);
        recyclerViewAllTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewAllTransactions.setAdapter(transactionAdapter);
    }

    private void showTransactionOptionsDialog(Transaction transaction) {
        String[] options = {getString(R.string.title_edit_transaction), getString(R.string.btn_delete)};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_transaction_options)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
                        intent.putExtra(Constants.EXTRA_TRANSACTION_ID, transaction.getId());
                        startActivity(intent);
                    } else if (which == 1) {
                        confirmDeleteTransaction(transaction);
                    }
                })
                .show();
    }

    private void confirmDeleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.msg_confirm_delete)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    databaseManager.deleteTransaction(transaction.getId());
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void loadData() {
        allTransactions = databaseManager.getAllTransactions();
        applyFilters();
    }

    private void applyFilters() {
        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(transaction -> {
                    if (currentType != null && !currentType.equals(transaction.getType())) {
                        return false;
                    }
                    if (!currentQuery.isEmpty()) {
                        String transactionNote = transaction.getNote() != null ? transaction.getNote().toLowerCase() : "";
                        String categoryName = transaction.getLocalizedCategoryName(requireContext()).toLowerCase();
                        return transactionNote.contains(currentQuery) || categoryName.contains(currentQuery);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        transactionAdapter.updateData(filteredTransactions);
        updateEmptyState(filteredTransactions);
    }

    /**
     * Cập nhật giao diện Empty State theo ngữ cảnh:
     * - Nếu danh sách gốc trống → hiện "Chưa có giao dịch nào"
     * - Nếu danh sách gốc có data nhưng lọc ra trống → hiện "Không tìm thấy kết quả"
     */
    private void updateEmptyState(List<Transaction> filteredList) {
        if (filteredList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerViewAllTransactions.setVisibility(View.GONE);

            if (allTransactions.isEmpty()) {
                // Chưa có giao dịch nào (danh sách gốc trống)
                tvEmptyIcon.setText("📋");
                tvEmptyTitle.setText(R.string.empty_transactions_title);
                tvEmptySubtitle.setText(R.string.empty_transactions_subtitle);
                btnEmptyAction.setVisibility(View.GONE);
            } else {
                // Có data nhưng bộ lọc/search không trả về kết quả
                tvEmptyIcon.setText("🔍");
                tvEmptyTitle.setText(R.string.empty_search_title);
                tvEmptySubtitle.setText(R.string.empty_search_subtitle);
                btnEmptyAction.setText(R.string.btn_reset_filter);
                btnEmptyAction.setVisibility(View.VISIBLE);
            }
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerViewAllTransactions.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Đặt lại tất cả bộ lọc và tìm kiếm về trạng thái mặc định.
     */
    private void resetFilters() {
        currentType = null;
        currentQuery = "";
        chipFilterAll.setChecked(true);
        editTextSearch.setText("");
        applyFilters();
    }
}
