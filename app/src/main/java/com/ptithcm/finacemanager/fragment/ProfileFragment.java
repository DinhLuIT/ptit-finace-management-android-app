package com.ptithcm.finacemanager.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.ptithcm.finacemanager.R;

public class ProfileFragment extends Fragment {

    private SwitchMaterial switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initListeners(view);
    }

    private void initViews(View view) {
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        // Đặt trạng thái ban đầu cho dark mode
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void initListeners(View view) {
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        view.findViewById(R.id.tv_statistics).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming in Phase 2", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.tv_exchange_rate).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming in Phase 3", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.tv_export).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming in Phase 3", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.tv_change_pin).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.tv_about).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Finance Manager v1.0\nPTIT HCM", Toast.LENGTH_LONG).show();
        });
    }
}
