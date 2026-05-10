package com.example.schedulemanager.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.models.SubTask;

import java.util.List;

public class DetailSubTaskAdapter extends RecyclerView.Adapter<DetailSubTaskAdapter.ViewHolder> {

    private final List<SubTask> subTasks;
    private final OnSubTaskStatusChangedListener listener;

    public interface OnSubTaskStatusChangedListener {
        void onStatusChanged(SubTask subTask, boolean isCompleted);
    }

    public DetailSubTaskAdapter(List<SubTask> subTasks, OnSubTaskStatusChangedListener listener) {
        this.subTasks = subTasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubTask subTask = subTasks.get(position);
        holder.bind(subTask);
    }

    @Override
    public int getItemCount() {
        return subTasks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbSubTask);
            tvTitle = itemView.findViewById(R.id.tvSubTaskTitle);
        }

        void bind(SubTask subTask) {
            tvTitle.setText(subTask.getTitle());
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(subTask.isCompleted());
            updateTitleStrikeThrough(subTask.isCompleted());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                subTask.setCompleted(isChecked);
                updateTitleStrikeThrough(isChecked);
                if (listener != null) {
                    listener.onStatusChanged(subTask, isChecked);
                }
            });
        }

        private void updateTitleStrikeThrough(boolean isCompleted) {
            if (isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.6f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
            }
        }
    }
}
