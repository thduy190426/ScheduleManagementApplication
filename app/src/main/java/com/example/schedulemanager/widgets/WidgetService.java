package com.example.schedulemanager.widgets;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;

import com.example.schedulemanager.R;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScheduleRemoteViewsFactory(this.getApplicationContext());
    }

    class ScheduleRemoteViewsFactory implements RemoteViewsFactory {
        private Context context;
        private List<Schedule> scheduleList = new ArrayList<>();
        private DatabaseHelper dbHelper;

        public ScheduleRemoteViewsFactory(Context context) {
            this.context = context;
            this.dbHelper = DatabaseHelper.getInstance(context);
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            com.example.schedulemanager.utils.PreferenceManager prefManager = new com.example.schedulemanager.utils.PreferenceManager(context);
            java.util.TimeZone tz = java.util.TimeZone.getTimeZone(prefManager.getTimezone());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(tz);
            String today = sdf.format(new Date());
            scheduleList = dbHelper.getSchedulesByDate(today);
        }

        @Override
        public void onDestroy() {
            scheduleList.clear();
        }

        @Override
        public int getCount() {
            return scheduleList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= scheduleList.size()) return null;

            Schedule schedule = scheduleList.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);

            views.setTextViewText(R.id.item_title, schedule.getTitle());
            views.setTextViewText(R.id.item_time, schedule.getStartTime() + " - " + schedule.getEndTime());
            
            if (schedule.getCategory() != null) {
                String categoryLabel = context.getString(schedule.getCategory().getLabelResId());
                if (schedule.getCategory() == com.example.schedulemanager.models.Category.OTHER && schedule.getCustomCategory() != null) {
                    categoryLabel = schedule.getCustomCategory();
                }
                views.setTextViewText(R.id.item_category, categoryLabel);
                int catColor = ContextCompat.getColor(context, schedule.getCategory().getColorResId());
                views.setInt(R.id.item_category, "setBackgroundColor", catColor);
                views.setViewVisibility(R.id.item_category, View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.item_category, View.GONE);
            }

            int color = ContextCompat.getColor(context, schedule.getPriority().getColorResId());
            views.setInt(R.id.item_priority_indicator, "setBackgroundColor", color);
            
            int statusIcon = schedule.isCompleted() ? 
                    android.R.drawable.checkbox_on_background : 
                    android.R.drawable.checkbox_off_background;
            views.setImageViewResource(R.id.item_status_icon, statusIcon);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(com.example.schedulemanager.utils.IntentKeys.EXTRA_SCHEDULE, schedule);
            views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
