package com.example.schedulemanager.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.models.SubTask;

import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {

    private final List<SubTask> subTasks;
    private final OnSubTaskDeletedListener deleteListener;

    public interface OnSubTaskDeletedListener {
        void onSubTaskDeleted(int position);
    }

    public SubTaskAdapter(List<SubTask> subTasks, OnSubTaskDeletedListener deleteListener) {
        this.subTasks = subTasks;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public SubTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask_edit, parent, false);
        return new SubTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTaskViewHolder holder, int position) {
        SubTask subTask = subTasks.get(position);
        holder.bind(subTask);
    }

    @Override
    public int getItemCount() {
        return subTasks.size();
    }

    class SubTaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        EditText etTitle;
        ImageButton btnDelete;
        private TextWatcher textWatcher;

        public SubTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            etTitle = itemView.findViewById(R.id.etSubTaskTitle);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < subTasks.size()) {
                        subTasks.get(pos).setTitle(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < subTasks.size()) {
                    subTasks.get(pos).setCompleted(isChecked);
                }
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onSubTaskDeleted(pos);
                }
            });
        }

        void bind(SubTask subTask) {
            etTitle.removeTextChangedListener(textWatcher);
            etTitle.setText(subTask.getTitle());
            etTitle.addTextChangedListener(textWatcher);
            
            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(subTask.isCompleted());
            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < subTasks.size()) {
                    subTasks.get(pos).setCompleted(isChecked);
                }
            });
        }
    }
}
