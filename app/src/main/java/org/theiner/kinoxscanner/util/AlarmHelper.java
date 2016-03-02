package org.theiner.kinoxscanner.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;
import org.theiner.kinoxscanner.services.CheckKinox;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class AlarmHelper {

    public static PendingIntent getPendingIntentFromAlarm(Context context, int alarmId) {
        return (PendingIntent.getService(context, alarmId,
                new Intent(context, CheckKinox.class),
                PendingIntent.FLAG_NO_CREATE));
    }

    public static void setAlarm(Context context, int alarmId, int hours) {
        Intent serviceIntent = new Intent(context, CheckKinox.class);
        PendingIntent pi = PendingIntent.getService(context, alarmId, serviceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        DateTime inHours = (new DateTime()).plusHours(hours);
        //DateTime inHours = (new DateTime()).plusMinutes(hours);


        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, inHours.getMillis(), pi);

    }
}
