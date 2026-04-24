package com.example.schedulemanager.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
        void onScheduleClick(Schedule schedule);
        void onScheduleCheckChanged(Schedule schedule, boolean isChecked);
    }

    public ScheduleAdapter(OnScheduleClickListener clickListener) {
        super(new DiffCallback());
        this.clickListener = clickListener;
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
        private final View priorityIndicator;
        private final TextView tvTitle;
        private final TextView tvTime;
        private final TextView tvCategory;
        private final CheckBox cbCompleted;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
        }

        public void bind(Schedule schedule, OnScheduleClickListener listener) {
            tvTitle.setText(schedule.getTitle());
            tvTime.setText(String.format("%s | %s - %s", schedule.getDate(), schedule.getStartTime(), schedule.getEndTime()));
            priorityIndicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), schedule.getPriority().getColorResId()));
            
            if (schedule.getCategory() != null) {
                tvCategory.setText(schedule.getCategory().getLabel());
                tvCategory.setVisibility(View.VISIBLE);
                android.graphics.drawable.GradientDrawable drawable = (android.graphics.drawable.GradientDrawable) tvCategory.getBackground();
                drawable.setColor(ContextCompat.getColor(itemView.getContext(), schedule.getCategory().getColorResId()));
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(schedule.isCompleted());

            updateTextStyles(schedule.isCompleted());

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                schedule.setCompleted(isChecked);
                updateTextStyles(isChecked);
                listener.onScheduleCheckChanged(schedule, isChecked);
            });

            itemView.setOnClickListener(v -> listener.onScheduleClick(schedule));
        }

        private void updateTextStyles(boolean isCompleted) {
            if (isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.completed_gray));
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
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
