package com.ptithcm.finacemanager.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.RecurringTransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.dialog.AddRecurringDialog;
import com.ptithcm.finacemanager.model.RecurringTransaction;
import com.ptithcm.finacemanager.utils.CustomToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách giao dịch định kỳ.
 *
 * <p>Được nhúng trong ViewPager2 của {@link TransactionsFragment} (Tab "Định kỳ").
 * Logic tương tự RecurringTransactionsActivity nhưng dưới dạng Fragment
 * để tái sử dụng trong cả ViewPager và navigation riêng.
 */
public class RecurringTransactionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyStateContainer;
    private TextView tvEmptyIcon, tvEmptyTitle, tvEmptySubtitle;

    private DBManager dbManager;
    private RecurringTransactionAdapter adapter;
    private List<RecurringTransaction> recurringList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recurring_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbManager = DBManager.getInstance(requireContext());
        initViews(view);
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_recurring);

        // Empty State
        emptyStateContainer = view.findViewById(R.id.include_empty_state);
        tvEmptyIcon = emptyStateContainer.findViewById(R.id.tv_empty_icon);
        tvEmptyTitle = emptyStateContainer.findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = emptyStateContainer.findViewById(R.id.tv_empty_subtitle);

        tvEmptyIcon.setText("🔄");
        tvEmptyTitle.setText(R.string.empty_recurring_title);
        tvEmptySubtitle.setText(R.string.empty_recurring_subtitle);

        // FAB thêm giao dịch định kỳ
        view.findViewById(R.id.fab_add_recurring).setOnClickListener(v -> showAddDialog());
    }

    private void setupRecyclerView() {
        adapter = new RecurringTransactionAdapter(recurringList, requireContext());
        adapter.setOnRecurringClickListener(new RecurringTransactionAdapter.OnRecurringClickListener() {
            @Override
            public void onItemClick(RecurringTransaction recurring) {
                showOptionsDialog(recurring);
            }

            @Override
            public void onItemLongClick(RecurringTransaction recurring) {
                showOptionsDialog(recurring);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        recurringList = dbManager.getAllActiveRecurringTransactions();
        adapter.updateData(recurringList);

        if (recurringList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddDialog() {
        AddRecurringDialog dialog = new AddRecurringDialog();
        dialog.setOnRecurringSavedListener(this::loadData);
        dialog.show(getChildFragmentManager(), "AddRecurringDialog");
    }

    private void showOptionsDialog(RecurringTransaction recurring) {
        String[] options = {
                getString(R.string.btn_deactivate_recurring),
                getString(R.string.btn_delete)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(recurring.getNote() != null && !recurring.getNote().isEmpty()
                        ? recurring.getNote()
                        : recurring.getLocalizedCategoryName(requireContext()))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        confirmDeactivate(recurring);
                    } else {
                        confirmDelete(recurring);
                    }
                })
                .show();
    }

    private void confirmDeactivate(RecurringTransaction recurring) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_deactivate_recurring)
                .setMessage(R.string.msg_confirm_deactivate_recurring)
                .setPositiveButton(R.string.btn_deactivate_recurring, (d, w) -> {
                    dbManager.deactivateRecurringTransaction(recurring.getId());
                    CustomToast.showSuccess(requireContext(), getString(R.string.msg_recurring_deactivated));
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void confirmDelete(RecurringTransaction recurring) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.msg_confirm_delete_recurring)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    dbManager.deleteRecurringTransaction(recurring.getId());
                    CustomToast.showSuccess(requireContext(), getString(R.string.msg_recurring_deleted));
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
