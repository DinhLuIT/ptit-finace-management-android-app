package com.ptithcm.finacemanager.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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
 * Màn hình quản lý giao dịch định kỳ.
 *
 * <p>Hiển thị danh sách tất cả giao dịch định kỳ đang hoạt động.
 * Người dùng có thể:
 * <ul>
 *     <li>Xem danh sách với icon, tần suất, số tiền, ngày lặp tiếp theo</li>
 *     <li>Thêm mới qua FAB → AddRecurringDialog</li>
 *     <li>Long-press → Tạm dừng hoặc Xóa giao dịch định kỳ</li>
 * </ul>
 */
public class RecurringTransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyStateContainer;
    private TextView tvEmptyIcon, tvEmptyTitle, tvEmptySubtitle;

    private DBManager dbManager;
    private RecurringTransactionAdapter adapter;
    private List<RecurringTransaction> recurringList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_transactions);

        dbManager = DBManager.getInstance(this);
        initViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        // Back button
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_recurring);

        // Empty State
        emptyStateContainer = findViewById(R.id.include_empty_state);
        tvEmptyIcon = emptyStateContainer.findViewById(R.id.tv_empty_icon);
        tvEmptyTitle = emptyStateContainer.findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = emptyStateContainer.findViewById(R.id.tv_empty_subtitle);

        tvEmptyIcon.setText("🔄");
        tvEmptyTitle.setText(R.string.empty_recurring_title);
        tvEmptySubtitle.setText(R.string.empty_recurring_subtitle);

        // FAB thêm giao dịch định kỳ
        findViewById(R.id.fab_add_recurring).setOnClickListener(v -> showAddDialog());
    }

    private void setupRecyclerView() {
        adapter = new RecurringTransactionAdapter(recurringList, this);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    /**
     * Hiện dialog thêm giao dịch định kỳ mới.
     */
    private void showAddDialog() {
        AddRecurringDialog dialog = new AddRecurringDialog();
        dialog.setOnRecurringSavedListener(this::loadData);
        dialog.show(getSupportFragmentManager(), "AddRecurringDialog");
    }

    /**
     * Hiện dialog tùy chọn: Tạm dừng hoặc Xóa.
     */
    private void showOptionsDialog(RecurringTransaction recurring) {
        String[] options = {
                getString(R.string.btn_deactivate_recurring),
                getString(R.string.btn_delete)
        };

        new AlertDialog.Builder(this)
                .setTitle(recurring.getNote() != null && !recurring.getNote().isEmpty()
                        ? recurring.getNote()
                        : recurring.getLocalizedCategoryName(this))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Tạm dừng
                        confirmDeactivate(recurring);
                    } else {
                        // Xóa
                        confirmDelete(recurring);
                    }
                })
                .show();
    }

    private void confirmDeactivate(RecurringTransaction recurring) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_deactivate_recurring)
                .setMessage(R.string.msg_confirm_deactivate_recurring)
                .setPositiveButton(R.string.btn_deactivate_recurring, (d, w) -> {
                    dbManager.deactivateRecurringTransaction(recurring.getId());
                    CustomToast.showSuccess(this, getString(R.string.msg_recurring_deactivated));
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void confirmDelete(RecurringTransaction recurring) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.msg_confirm_delete_recurring)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    dbManager.deleteRecurringTransaction(recurring.getId());
                    CustomToast.showSuccess(this, getString(R.string.msg_recurring_deleted));
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
