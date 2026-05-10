package com.example.schedulemanager.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.models.Schedule;

public class ScheduleAdapter extends ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder> {

    private final OnScheduleClickListener clickListener;

    public interface OnScheduleClickListener {
        void onScheduleClick(Schedule schedule, View view);
        void onScheduleCheckChanged(Schedule schedule, boolean isChecked);
        default void onScheduleLongClick(Schedule schedule) {}
    }

    public ScheduleAdapter(OnScheduleClickListener clickListener) {
        super(new DiffCallback());
        this.clickListener = clickListener;
    }

    public void moveItem(int fromPosition, int toPosition) {
        java.util.List<Schedule> list = new java.util.ArrayList<>(getCurrentList());
        java.util.Collections.swap(list, fromPosition, toPosition);
        submitList(list);
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener);
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final View cardView;
        private final View priorityIndicator;
        private final TextView tvTitle;
        private final TextView tvTime;
        private final TextView tvCategory;
        private final CheckBox cbCompleted;
        private final ImageView ivHasAttachment;
        private final TextView tvStatus;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardSchedule);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            ivHasAttachment = itemView.findViewById(R.id.ivHasAttachment);
        }

        public void bind(Schedule schedule, OnScheduleClickListener listener) {
            tvTitle.setText(schedule.getTitle());
            tvTime.setText(String.format("%s | %s - %s", schedule.getDate(), schedule.getStartTime(), schedule.getEndTime()));
            priorityIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), schedule.getPriority().getColorResId()));
            
            cardView.setTransitionName("schedule_card_" + schedule.getId());
            
            if (schedule.getCategory() != null) {
                String label = itemView.getContext().getString(schedule.getCategory().getLabelResId());
                int categoryColor = ContextCompat.getColor(itemView.getContext(), schedule.getCategory().getColorResId());
                int iconResId = 0;

                if (schedule.getCategory() == com.example.schedulemanager.models.Category.OTHER &&
                        schedule.getCustomCategory() != null && !schedule.getCustomCategory().isEmpty()) {
                    label = schedule.getCustomCategory();
                    com.example.schedulemanager.database.DatabaseHelper dbHelper = com.example.schedulemanager.database.DatabaseHelper.getInstance(itemView.getContext());
                    com.example.schedulemanager.models.CustomCategory custom = dbHelper.getCustomCategoryByName(label);
                    if (custom != null) {
                        categoryColor = custom.getColor();
                        iconResId = itemView.getContext().getResources().getIdentifier(custom.getIconName(), "drawable", itemView.getContext().getPackageName());
                    }
                }

                tvCategory.setText(label);
                tvCategory.setVisibility(View.VISIBLE);
                android.graphics.drawable.GradientDrawable drawable = (android.graphics.drawable.GradientDrawable) tvCategory.getBackground();
                drawable.setColor(categoryColor);

                if (iconResId != 0) {
                    android.graphics.drawable.Drawable icon = ContextCompat.getDrawable(itemView.getContext(), iconResId);
                    if (icon != null) {
                        icon.setTint(android.graphics.Color.WHITE);
                        tvCategory.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                        tvCategory.setCompoundDrawablePadding(8);
                    }
                } else {
                    tvCategory.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            if (ivHasAttachment != null) {
                ivHasAttachment.setVisibility(schedule.getAttachmentPath() != null && !schedule.getAttachmentPath().isEmpty() ? View.VISIBLE : View.GONE);
            }

            if (!schedule.isCompleted() && com.example.schedulemanager.utils.DateTimeUtils.isDateInPast(schedule.getDate(), schedule.getEndTime(), itemView.getContext())) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText(R.string.overdue);
                tvStatus.setBackgroundResource(R.drawable.bg_overdue_chip);
            } else {
                tvStatus.setVisibility(View.GONE);
            }

            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(schedule.isCompleted());

            updateTextStyles(schedule.isCompleted());

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                schedule.setCompleted(isChecked);
                updateTextStyles(isChecked);
                listener.onScheduleCheckChanged(schedule, isChecked);
            });

            itemView.setOnClickListener(v -> listener.onScheduleClick(schedule, cardView));
            itemView.setOnLongClickListener(v -> {
                listener.onScheduleLongClick(schedule);
                return true;
            });
        }

        private void updateTextStyles(boolean isCompleted) {
            if (isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.completed_gray));
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<Schedule> {
        @Override
        public boolean areItemsTheSame(@NonNull Schedule oldItem, @NonNull Schedule newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Schedule oldItem, @NonNull Schedule newItem) {
            return oldItem.equals(newItem);
        }
    }
}
