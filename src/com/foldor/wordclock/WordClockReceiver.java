package com.foldor.wordclock;

import java.util.Calendar;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class WordClockReceiver extends AppWidgetProvider {
    private static final String TAG = "WordClock";
    
    /**
     * Add time changed events.
     * ACTION_TIME_TICK, ACTION_TIMEZONE_CHANGED, ACTION_TIME_CHANGED
     */
    private final static IntentFilter sIntentFilter;
    static {
        sIntentFilter = new IntentFilter();
        sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        sIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }
    
    /**
     * Registers the mTimeChangedReceiver function with the sIntentFilter for changing the time.
     * {@inheritDoc}
     */
    @Override
    public void onEnabled(Context context) {
        context.startService(new Intent(context, UpdateService.class));
        super.onEnabled(context);
    }
    
    @Override
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, UpdateService.class));
        super.onDisabled(context);
    }
    
    /**
     * Updates the widget with the current time.
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWM, int[] appWidgetIds) {
        updateAppWidget(context, appWM, appWidgetIds);
        super.onUpdate(context, appWM, appWidgetIds);
    }
    
    /**
     * Updates the widget text with the current time when the intent is updated/enabled.
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
                action.equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {

                AppWidgetManager appWM = AppWidgetManager.getInstance(context);

                //Update the Widget text.
                if (appWM != null) {
                    updateAppWidget(context, appWM, appWM.getAppWidgetIds(intent.getComponent()));
                }
            }
        }

        super.onReceive(context, intent);
    }
    
    /**
     * This will update the passed widgets with the current time in English. Runs updateAppWidget for each appWidgetIds value.
     * @param context
     * @param appWM
     * @param appWidgetIds
     */
    private static void updateAppWidget(Context context, AppWidgetManager appWM, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWM, appWidgetId);
        }
    }
    
    /**
     * This will update the passed widget view with the current time in English.
     * @param context
     * @param appWM
     * @param appWidgetId
     */
    private static void updateAppWidget(Context context, AppWidgetManager appWM, int appWidgetId) {
        RemoteViews views = buildAppWidget(context, appWM, appWidgetId);
        
        if (views != null) {
            appWM.updateAppWidget(appWidgetId, views);
        }
        else {
            Log.d(TAG, "updateAppWidget is NULL! ID: " + appWidgetId);
        }
    }
    
    /**
     * This will creates the view with the current time in English, and set up the onClick activity for launching the Preferences Activity.
     * @param context
     * @param appWM
     * @param appWidgetId
     * @return An updated view of the widget.
     */
    private static RemoteViews buildAppWidget(Context context, AppWidgetManager appWM, int appWidgetId) {
        //Checks to see if the layout has changed from the default.
        AppWidgetProviderInfo providerInfo = appWM.getAppWidgetInfo(appWidgetId);
        int layoutId = (providerInfo == null) ? R.layout.main : providerInfo.initialLayout;
        
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        
        //Set the text to the time.
        views.setTextViewText(R.id.wordclock, getTime());
        
        //Grab the FontColor Shared Preference and use it to update the font color of the textview.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String FontColor = prefs.getString("FontColor", "#FFFFFF");
        
        views.setTextColor(R.id.wordclock, Color.parseColor(FontColor));
        
        //Create the Intent to configure preferences.
        Intent prefsIntent = new Intent(context, Preferences.class);
        prefsIntent.setAction("android.appwidget.action.APPWIDGET_CONFIGURE");
        prefsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        
        PendingIntent prefsPendingIntent = PendingIntent.getActivity(context, 0, prefsIntent, 0);
        
        //Create the onClick event to load the preferences.
        views.setOnClickPendingIntent(R.id.wordclock, prefsPendingIntent);
        
        return views;
    }
    
    /**
     * Automatically registered when the Widget is created, and unregistered
     * when the Widget is destroyed.
     */
    private final static BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        /**
         * Updates the widget text with the time every minute.
         * {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            //ComponentName thisWidget = new ComponentName(context, this.getClass());
            //TODO: Make this more dynamic.
            ComponentName component = new ComponentName(context, "com.foldor.wordclock.WordClockReceiver");
            AppWidgetManager appWM = AppWidgetManager.getInstance(context);
            
            //Update the widget with the new time.
            updateAppWidget(context, appWM, appWM.getAppWidgetIds(component));
        }
    };
    
    /**
     * Create a Service to keep the mTimeChangedReceiver from being silently killed by the Android OS.
     */
    public static final class UpdateService extends Service {
        /**
         * This is necessary to extend the Service Class
         */
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        
        /**
         * Register the mTimeChangedReceiver when the widget is created.
         */
        @Override
        public void onCreate() {
            registerReceiver(mTimeChangedReceiver, sIntentFilter);
            super.onCreate();
        }
        
        /**
         * Un-register the mTimeChangedReceiver when the widget is deleted.
         */
        @Override
        public void onDestroy() {
            unregisterReceiver(mTimeChangedReceiver);
            super.onDestroy();
        }
    }
    
    /**
     * Gets the time in English
     * Format: It's Twelve Thirty-Six in the Afternoon
     * Format: It's One O'Six in the Morning
     */
    private static String getTime() {
        Calendar now = Calendar.getInstance();
        String time;
        
        //HOURS
        int apphours = now.get(Calendar.HOUR_OF_DAY) % 12; //Get this in a twelve hour format.
        if (apphours == 0) apphours = 12; //If it's zero then it's midnight or noon.
            
        //MINUTES
        int appminutes = now.get(Calendar.MINUTE);
        
        //TIME
        time = "It's " + NumberToWords.get(apphours) + " ";
        if (appminutes < 10 && appminutes != 0) time += "O'"; //Add O' to the minutes if it's less than 10, but not zero.
        time += NumberToWords.get(appminutes);
        time += " in the ";
        time += (now.get(Calendar.HOUR_OF_DAY) >= 12) ? "Afternoon" : "Morning";
        
        return time;
    }
}