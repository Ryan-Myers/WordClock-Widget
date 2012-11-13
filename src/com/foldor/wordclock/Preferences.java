package com.foldor.wordclock;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.RemoteViews;

public class Preferences extends PreferenceActivity {
    //private static final String TAG="WordClock";
    private static String CONFIGURE_ACTION="android.appwidget.action.APPWIDGET_CONFIGURE";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK) {
            if (CONFIGURE_ACTION.equals(getIntent().getAction())) {
                Intent intent=getIntent();
                Bundle extras=intent.getExtras();
                
                if (extras!=null) {
                    int id=extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    
                    AppWidgetManager mgr=AppWidgetManager.getInstance(this);
                    
                    RemoteViews views=new RemoteViews(getPackageName(), R.layout.main);
                    
                    mgr.updateAppWidget(id, views);

                    Intent result = new Intent();
                    
                    result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
                    setResult(RESULT_OK, result);
                    
                                                            
                    sendBroadcast(new Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_ENABLED, Uri.EMPTY, this, WordClockReceiver.class));
                }
            }
        }
        return(super.onKeyDown(keyCode, event));
    }
}