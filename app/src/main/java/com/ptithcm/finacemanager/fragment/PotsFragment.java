package com.ptithcm.finacemanager.fragment;

import android.content.Intent;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.activity.PotDetailActivity;
import com.ptithcm.finacemanager.adapter.PotAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.dialog.AddPotDialog;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PotsFragment extends Fragment implements PotAdapter.OnPotClickListener {

    private RecyclerView rvPots;
    private FloatingActionButton fabAddPot;

    // Empty State views
    private View emptyStateContainer;
    private TextView tvEmptyIcon, tvEmptyTitle, tvEmptySubtitle;
    private MaterialButton btnEmptyAction;

    private DBManager dbManager;
    private PotAdapter adapter;
    private List<Pot> potList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pots, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbManager = DBManager.getInstance(requireContext());
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
        rvPots = view.findViewById(R.id.rv_pots);
        fabAddPot = view.findViewById(R.id.fab_add_pot);

        // Empty State – sử dụng layout tái sử dụng
        emptyStateContainer = view.findViewById(R.id.include_empty_state);
        tvEmptyIcon = emptyStateContainer.findViewById(R.id.tv_empty_icon);
        tvEmptyTitle = emptyStateContainer.findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = emptyStateContainer.findViewById(R.id.tv_empty_subtitle);
        btnEmptyAction = emptyStateContainer.findViewById(R.id.btn_empty_action);

        // Cấu hình nội dung Empty State cho màn hình Hủ
        tvEmptyIcon.setText("🏺");
        tvEmptyTitle.setText(R.string.empty_pots_title);
        tvEmptySubtitle.setText(R.string.empty_pots_subtitle);
        btnEmptyAction.setText(R.string.empty_pots_action);
        btnEmptyAction.setVisibility(View.VISIBLE);
    }

    private void initListeners() {
        fabAddPot.setOnClickListener(v -> showAddPotDialog());

        // Nút hành động trong Empty State cũng mở dialog tạo hủ mới
        btnEmptyAction.setOnClickListener(v -> showAddPotDialog());
    }

    private void setupRecyclerView() {
        adapter = new PotAdapter(potList, this);
        rvPots.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPots.setAdapter(adapter);
    }

    private void loadData() {
        potList = dbManager.getAllActivePots();
        adapter.updateData(potList);

        if (potList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            rvPots.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            rvPots.setVisibility(View.VISIBLE);
        }
    }

    private void showAddPotDialog() {
        AddPotDialog dialog = new AddPotDialog();
        dialog.setOnPotSavedListener(() -> loadData());
        dialog.show(getChildFragmentManager(), "AddPotDialog");
    }

    @Override
    public void onPotClick(Pot pot) {
        Intent intent = new Intent(requireContext(), PotDetailActivity.class);
        intent.putExtra(Constants.EXTRA_POT_ID, pot.getId());
        startActivity(intent);
    }

    @Override
    public void onPotLongClick(Pot pot) {
        showPotOptionsMenu(pot);
    }

    private void showPotOptionsMenu(Pot pot) {
        String[] options = {
                getString(R.string.title_edit_pot),
                getString(R.string.btn_delete)
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(pot.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Sửa hủ
                        showEditPotDialog(pot);
                    } else if (which == 1) {
                        // Xóa hủ
                        confirmDeletePot(pot);
                    }
                })
                .show();
    }

    private void showEditPotDialog(Pot pot) {
        AddPotDialog dialog = new AddPotDialog();
        dialog.setEditPot(pot);
        dialog.setOnPotSavedListener(() -> loadData());
        dialog.show(getChildFragmentManager(), "EditPotDialog");
    }

    private void confirmDeletePot(Pot pot) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setMessage(R.string.msg_confirm_delete_pot)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    dbManager.deletePot(pot.getId());
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
