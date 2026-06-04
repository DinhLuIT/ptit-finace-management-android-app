package com.ptithcm.finacemanager.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ptithcm.finacemanager.R;

/**
 * Fragment container cho tab "Giao dịch" trong Bottom Navigation.
 *
 * <p>Chứa ViewPager2 với 2 tab:
 * <ol>
 *     <li><b>Lịch sử</b> – {@link TransactionHistoryFragment}: danh sách giao dịch đã xảy ra</li>
 *     <li><b>Định kỳ</b> – {@link RecurringTransactionsFragment}: quản lý giao dịch tự động lặp lại</li>
 * </ol>
 *
 * <p>Sử dụng {@link TabLayoutMediator} để liên kết TabLayout với ViewPager2.
 */
public class TransactionsFragment extends Fragment {

    private static final int TAB_HISTORY = 0;
    private static final int TAB_RECURRING = 1;
    private static final int TAB_COUNT = 2;

    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        // Adapter cho ViewPager2
        viewPager.setAdapter(new TransactionsPagerAdapter(this));

        // Liên kết TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case TAB_HISTORY:
                    tab.setText(R.string.tab_history);
                    break;
                case TAB_RECURRING:
                    tab.setText(R.string.tab_recurring);
                    break;
            }
        }).attach();
    }

    /**
     * Chuyển sang sub-tab Định kỳ (Recurring).
     * Được gọi từ ProfileFragment khi user nhấn "Giao dịch định kỳ".
     */
    public void selectRecurringTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(TAB_RECURRING, true);
        }
    }

    /**
     * PagerAdapter quản lý 2 child fragments: History và Recurring.
     */
    private static class TransactionsPagerAdapter extends FragmentStateAdapter {

        TransactionsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case TAB_RECURRING:
                    return new RecurringTransactionsFragment();
                case TAB_HISTORY:
                default:
                    return new TransactionHistoryFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TAB_COUNT;
        }
    }
}
