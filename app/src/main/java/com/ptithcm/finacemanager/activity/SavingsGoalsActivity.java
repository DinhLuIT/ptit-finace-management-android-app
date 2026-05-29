package com.ptithcm.finacemanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.SavingsGoalAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.dialog.AddGoalDialog;
import com.ptithcm.finacemanager.model.SavingsGoal;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình danh sách mục tiêu tiết kiệm (Plan A – Độc lập).
 *
 * <p>Hiển thị tất cả mục tiêu tiết kiệm của người dùng dưới dạng danh sách card.
 * Mỗi card hiện tiến độ, số tiền, và đếm ngược ngày.
 * Nhấn vào card → GoalDetailActivity. Nhấn FAB → AddGoalDialog.
 *
 * <p>Truy cập từ: ProfileFragment → "Mục tiêu tiết kiệm"
 */
public class SavingsGoalsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGoals;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAddGoal;

    private DBManager databaseManager;
    private SavingsGoalAdapter adapter;
    private List<SavingsGoal> goals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goals);

        databaseManager = DBManager.getInstance(this);

        initViews();
        initListeners();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        recyclerViewGoals = findViewById(R.id.rv_goals);
        layoutEmpty = findViewById(R.id.layout_empty);
        fabAddGoal = findViewById(R.id.fab_add_goal);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void initListeners() {
        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    private void setupRecyclerView() {
        adapter = new SavingsGoalAdapter(goals, this);
        adapter.setOnGoalClickListener(goal -> {
            Intent intent = new Intent(this, GoalDetailActivity.class);
            intent.putExtra("extra_goal_id", goal.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGoals.setAdapter(adapter);
    }

    /**
     * Load danh sách mục tiêu từ DB và cập nhật UI.
     */
    private void loadData() {
        goals = databaseManager.getAllSavingsGoals();
        adapter.updateData(goals);

        if (goals.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewGoals.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewGoals.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hiển thị dialog tạo mục tiêu mới.
     */
    private void showAddGoalDialog() {
        AddGoalDialog dialog = new AddGoalDialog();
        dialog.setOnGoalSavedListener(this::loadData);
        dialog.show(getSupportFragmentManager(), "AddGoalDialog");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
