package com.ptithcm.finacemanager.fragment;

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

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.TransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionsFragment extends Fragment {

    private RecyclerView recyclerViewAllTransactions;
    private TextView textViewEmptyState;
    private Chip chipFilterAll, chipFilterIncome, chipFilterExpense;
    private TextInputEditText editTextSearch;

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
        textViewEmptyState = view.findViewById(R.id.tv_empty);
        chipFilterAll = view.findViewById(R.id.chip_all);
        chipFilterIncome = view.findViewById(R.id.chip_income);
        chipFilterExpense = view.findViewById(R.id.chip_expense);
        editTextSearch = view.findViewById(R.id.et_search);
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
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(allTransactions, requireContext());
        recyclerViewAllTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewAllTransactions.setAdapter(transactionAdapter);
    }

    private void loadData() {
        allTransactions = databaseManager.getAllTransactions();
        applyFilters();
    }

    private void applyFilters() {
        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(transaction -> {
                    // lọc theo loại giao dịch
                    if (currentType != null && !currentType.equals(transaction.getType())) {
                        return false;
                    }

                    // lọc theo thanh tìm kiếm
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

    private void updateEmptyState(List<Transaction> transactionList) {
        if (transactionList.isEmpty()) {
            textViewEmptyState.setVisibility(View.VISIBLE);
            recyclerViewAllTransactions.setVisibility(View.GONE);
        } else {
            textViewEmptyState.setVisibility(View.GONE);
            recyclerViewAllTransactions.setVisibility(View.VISIBLE);
        }
    }
}
