package com.example.schedulemanager.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;

public abstract class SwipeCallbacks extends ItemTouchHelper.SimpleCallback {

    private final ColorDrawable deleteBackground;
    private final ColorDrawable completeBackground;
    private final Drawable deleteIcon;
    private final Drawable completeIcon;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private final Paint clearPaint;

    public SwipeCallbacks(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

        deleteBackground = new ColorDrawable();
        deleteBackground.setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        
        completeBackground = new ColorDrawable();
        completeBackground.setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));

        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white);
        completeIcon = ContextCompat.getDrawable(context, R.drawable.ic_check);

        intrinsicWidth = deleteIcon != null ? deleteIcon.getIntrinsicWidth() : 0;
        intrinsicHeight = deleteIcon != null ? deleteIcon.getIntrinsicHeight() : 0;

        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();
        boolean isCanceled = dX == 0f && !isCurrentlyActive;

        if (isCanceled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        if (dX < 0) {
            deleteBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            deleteBackground.draw(c);

            int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
            int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
            int deleteIconRight = itemView.getRight() - deleteIconMargin;
            int deleteIconBottom = deleteIconTop + intrinsicHeight;

            if (deleteIcon != null) {
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteIcon.draw(c);
            }
        } else if (dX > 0) {
            completeBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
            completeBackground.draw(c);

            int completeIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int completeIconMargin = (itemHeight - intrinsicHeight) / 2;
            int completeIconLeft = itemView.getLeft() + completeIconMargin;
            int completeIconRight = itemView.getLeft() + completeIconMargin + intrinsicWidth;
            int completeIconBottom = completeIconTop + intrinsicHeight;

            if (completeIcon != null) {
                completeIcon.setBounds(completeIconLeft, completeIconTop, completeIconRight, completeIconBottom);
                completeIcon.draw(c);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }
}
