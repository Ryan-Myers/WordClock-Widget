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
	
	//Create new intents
    private final static IntentFilter sIntentFilter;
    static {
        sIntentFilter = new IntentFilter();
        sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        sIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }
	
	 @Override
	 public void onEnabled(Context context) {
		 Log.d(TAG, "Enabled");
		 Context appContext = context.getApplicationContext();
		 
		 appContext.registerReceiver(mTimeChangedReceiver, sIntentFilter);
		 
		 AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
		 updateAppWidget(appContext, manager, R.layout.main);
		 
		 super.onEnabled(context);
	 }
	    
	 @Override
	 public void onDisabled(Context context) {
		 context.getApplicationContext().unregisterReceiver(mTimeChangedReceiver);
		 super.onDisabled(context);
	 }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    	context.getApplicationContext().unregisterReceiver(mTimeChangedReceiver);
        super.onDeleted(context, appWidgetIds);
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d(TAG, "onUpdate");
    	updateAppWidget(context, appWidgetManager, R.layout.main);
    	super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
	public void onReceive(Context context, Intent intent) {
    	final String action = intent.getAction();
				
		if (action.equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
			final int appWidgetId = intent.getExtras().getInt( AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		}
		else {
			super.onReceive(context, intent);
		}


		Log.d(TAG, "intent=" + intent.toString() + "; action="+action);
		
		if (action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
			Log.d(TAG, "Updating onReceive..." + intent.getComponent().toString());
			
			AppWidgetManager appWM = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWM.getAppWidgetIds(intent.getComponent());
			for (int appWidgetId : appWidgetIds) {
				updateAppWidget(context, appWM, appWidgetId);
			}
		}
	}
    
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = buildAppWidget(context, appWidgetManager, appWidgetId);
        if (views != null) appWidgetManager.updateAppWidget(appWidgetId, views);
    }
	
	protected static RemoteViews buildAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "Creating widget: " + appWidgetId);
        
        AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        int layoutId = (providerInfo == null) ? R.layout.main : providerInfo.initialLayout;
        
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        
        views.setTextViewText(R.id.wordclock, getTime());
        
        //Grab the FontColor Shared Preference and use it to update the font color of the textview.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String FontColor = prefs.getString("FontColor", "#FFFFFF");
        
        views.setTextColor(R.id.wordclock, Color.parseColor(FontColor));
        
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
        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.d(TAG, "TimeChangedReceiver");
        	
        	AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
			int[] appWidgetIds = manager.getAppWidgetIds(intent.getComponent());
			for (int appWidgetId : appWidgetIds) {
				Log.d(TAG, appWidgetId + "");
            	RemoteViews views = buildAppWidget(context, manager, appWidgetId);
            	
                ComponentName widget = new ComponentName(context.getPackageName(), WordClockReceiver.class.getName());
                manager.updateAppWidget(widget, views);
			}
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
    	
    	Log.d(TAG, time);
    	
    	return time;
    }
}