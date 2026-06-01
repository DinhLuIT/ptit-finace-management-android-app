package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.IconMapper;

import java.util.List;

public class PotDropdownAdapter extends ArrayAdapter<Pot> {

    private final LayoutInflater inflater;
    private final boolean showBalance;

    public PotDropdownAdapter(@NonNull Context context, @NonNull List<Pot> pots, boolean showBalance) {
        super(context, 0, pots);
        this.inflater = LayoutInflater.from(context);
        this.showBalance = showBalance;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_dropdown_pot, parent, false);
        }

        Pot pot = getItem(position);
        if (pot != null) {
            ImageView ivIcon = convertView.findViewById(R.id.iv_pot_icon);
            TextView tvName = convertView.findViewById(R.id.tv_pot_name);
            TextView tvBalance = convertView.findViewById(R.id.tv_pot_balance);
            View viewColor = convertView.findViewById(R.id.view_pot_color);

            tvName.setText(pot.getName());

            if (showBalance) {
                tvBalance.setVisibility(View.VISIBLE);
                tvBalance.setText(CurrencyFormatter.format(pot.getBalance()));
            } else {
                tvBalance.setVisibility(View.GONE);
            }

            // Set Color
            GradientDrawable drawable = (GradientDrawable) viewColor.getBackground();
            if (drawable != null) {
                try {
                    drawable.setColor(Color.parseColor(pot.getColor()));
                } catch (Exception e) {
                    drawable.setColor(Color.parseColor("#4CAF50")); // default
                }
            }

            // Set Icon
            String iconKey = pot.getIcon();
            if (iconKey != null && !iconKey.isEmpty() && !iconKey.equals("ic_default")) {
                ivIcon.setImageResource(IconMapper.getIconResource(getContext(), iconKey));
            } else {
                ivIcon.setImageResource(R.drawable.ic_savings); // default pot icon
            }
        }
        return convertView;
    }
}
