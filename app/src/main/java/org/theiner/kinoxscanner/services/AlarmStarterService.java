package org.theiner.kinoxscanner.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.theiner.kinoxscanner.util.AlarmHelper;

/**
 * Created by Thomas on 01.02.2016.
 */
public class AlarmStarterService extends Service {

    private static int ALARM_ID = 141414;

    @Override
    public void onCreate() {
        super.onCreate();

        // Set an alarm for the next time the CheckKinox service should run:
        setAlarm();

        // end the service
        stopSelf();
    }

    public void setAlarm() {

        // Falls er nicht schon läuft
        // Jede Stunde
        PendingIntent pi = AlarmHelper.getPendingIntentFromAlarm(this, ALARM_ID);
        if(pi == null)
           AlarmHelper.setAlarm(this, ALARM_ID, 1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
