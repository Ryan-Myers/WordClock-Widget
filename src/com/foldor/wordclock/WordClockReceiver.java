package com.foldor.wordclock;

import java.util.Date;

import android.app.PendingIntent;
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
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
 
public class WordClockReceiver extends AppWidgetProvider {
    //Log Tag.
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
        context.getApplicationContext().registerReceiver(mTimeChangedReceiver, sIntentFilter);
        super.onEnabled(context);
    }
    
    /**
     * Updates the widget with the current time.
     *  {@inheritDoc}
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetIds);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
    /**
     * Updates the widget text with the current time when the intent is update/enabled.
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        
        if (action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
            action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
            
        	Context appContext = context.getApplicationContext();
            AppWidgetManager appWM = AppWidgetManager.getInstance(appContext);
            
            //Update the Widget text.
            updateAppWidget(appContext, appWM, appWM.getAppWidgetIds(intent.getComponent()));
        }

        super.onReceive(context, intent);
    }
    
    /**
     * This will update the passed widgets with the current time in English. Runs updateAppWidget for each appWidgetIds value.
     * @param context
     * @param appWM
     * @param appWidgetIds
     */
    public static void updateAppWidget(Context context, AppWidgetManager appWM, int[] appWidgetIds) {    	
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWM, appWidgetId);
        }
    }
    
    /**
     * This will update the passed widget view with the current time in English.
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
        if (views != null) appWidgetManager.updateAppWidget(appWidgetId, views);
        else Log.d(TAG, "updateAppWidget is NULL! ID: " + appWidgetId);
    }
    
    /**
     * This will creates the view with the current time in English, and set up the onClick activity for launching the Preferences Activity.
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @return An updated view of the widget.
     */
    protected static RemoteViews buildAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {    
    	//TODO: Try to remember what this code even does... (In my defense it's been months)
        AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        int layoutId = (providerInfo == null) ? R.layout.main : providerInfo.initialLayout;
        
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        
        //Set the text to the time.
        views.setTextViewText(R.id.wordclock, getTime());
        
        //Grab the FontColor Shared Preference and use it to update the font color of the textview.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String FontColor = prefs.getString("FontColor", "#FFFFFF");
        
        views.setTextColor(R.id.wordclock, Color.parseColor(FontColor));
        
        //Create the onClick event.
        Intent intent = new Intent(context, Preferences.class);
        PendingIntent pendingIntent;
        
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.wordclock, pendingIntent);
        
        return views;
    }
    
    /**
     * Automatically registered when the Widget is created, and unregistered
     * when the Widget is destroyed.
     */
    private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
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
     * Gets the time in English
     * Format: It's Twelve Thirty-Six in the Afternoon
     * Format: It's One O'Six in the Morning
     */
    public static String getTime() {
        Date now = new Date();
        String time;
        
        //HOURS
        int apphours = now.getHours()%12; //Get this in a twelve hour format.
        if (apphours == 0) apphours = 12; //If it's zero then it's midnight or noon.
        
        //MINUTES
        int appminutes = now.getMinutes();
        
        //TIME 
        //Format: It's Twelve Thirty-Six in the Afternoon
        //Format: It's One O'Six in the Morning
        time = "It's " + NumberToWords.get(apphours) + " ";
        if (appminutes < 10 && appminutes != 0) time += "O'"; //Add O' to the minutes if it's less than 10, but not zero.
        time += NumberToWords.get(appminutes);
        time += " in the ";
        time += (now.getHours() >= 12) ? "Afternoon" : "Morning";
        
        return time;
    }
}