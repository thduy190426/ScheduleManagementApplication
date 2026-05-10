package com.example.schedulemanager.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private final List<String> iconNames;
    private final OnIconSelectedListener listener;
    private int selectedPosition = -1;
    private int currentColor;

    public interface OnIconSelectedListener {
        void onIconSelected(String iconName);
    }

    public IconAdapter(List<String> iconNames, int currentColor, OnIconSelectedListener listener) {
        this.iconNames = iconNames;
        this.currentColor = currentColor;
        this.listener = listener;
    }

    public void setCurrentColor(int color) {
        this.currentColor = color;
        notifyDataSetChanged();
    }

    public void setSelectedIcon(String iconName) {
        for (int i = 0; i < iconNames.size(); i++) {
            if (iconNames.get(i).equals(iconName)) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_picker, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        String iconName = iconNames.get(position);
        Context context = holder.itemView.getContext();
        int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        
        if (resId != 0) {
            holder.ivIcon.setImageResource(resId);
        }
        
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(currentColor));
        
        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_icon);
        } else {
            holder.itemView.setBackgroundResource(0);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onIconSelected(iconName);
        });
    }

    @Override
    public int getItemCount() {
        return iconNames.size();
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;

        IconViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
