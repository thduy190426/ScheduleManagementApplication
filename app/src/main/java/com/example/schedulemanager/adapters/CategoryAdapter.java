package com.example.schedulemanager.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.models.CustomCategory;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<CustomCategory> categories;
    private final OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEdit(CustomCategory category, int position);
        void onDelete(CustomCategory category, int position);
    }

    public CategoryAdapter(List<CustomCategory> categories, OnCategoryActionListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_management, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CustomCategory category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());
        
        Context context = holder.itemView.getContext();
        int resId = context.getResources().getIdentifier(category.getIconName(), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.ivCategoryIcon.setImageResource(resId);
        } else {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_category);
        }
        holder.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(category.getColor()));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(category, position));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(category, position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivCategoryIcon;
        ImageButton btnEdit, btnDelete;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
