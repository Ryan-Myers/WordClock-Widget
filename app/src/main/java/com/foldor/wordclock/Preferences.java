package com.foldor.wordclock;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Preferences extends PreferenceActivity {
    private static final String TAG = "WordClock";
    private static final String CONFIGURE_ACTION = "android.appwidget.action.APPWIDGET_CONFIGURE";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //Set up the Listener to change the colour when updated.
        findPreference("FontColor").setOnPreferenceChangeListener(onPreferenceChange);
    }

    /**
     * When the user changes the colour, this will kick off the change immediately.
     */
    private OnPreferenceChangeListener onPreferenceChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Intent activityIntent = getIntent();

            if (CONFIGURE_ACTION.equals(activityIntent.getAction())) {
                Bundle extras = activityIntent.getExtras();

                if (extras != null) {
                    int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);

                    //Put the widget ID into the extras and let the activity caller know the result is ok.
                    Intent result = new Intent();
                    result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                    setResult(RESULT_OK, result);

                    //Update the widget immediately after changing the colour.
                    UpdateWidget();

                    finish();
                }
                else {
                    Log.d(TAG, "Intent Extras is null");

                    return false;
                }
            }
            else {
                Log.d(TAG, "Intent Action is: {" + activityIntent.getAction() + "} and not: " + CONFIGURE_ACTION);

                return false;
            }

            return true;
        }
    };

    /**
     * Send an intent to update the WordClockReceiver Widget.
     */
    private void UpdateWidget() {
        Intent updateWidget = new Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE,
                Uri.EMPTY, this, WordClockReceiver.class);

        sendBroadcast(updateWidget);
    }
}