package org.theiner.kinoxscanner.util;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.joda.time.DateTime;
import org.theiner.kinoxscanner.services.CheckKinoxService;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class AlarmHelper {

    public static PendingIntent getPendingIntentFromAlarm(Context context, int alarmId) {
        return (PendingIntent.getService(context, alarmId,
                new Intent(context, CheckKinoxService.class),
                PendingIntent.FLAG_NO_CREATE));
    }

    public static void setAlarm(Context context, int alarmId, int minutes) {
        Intent serviceIntent = new Intent(context, CheckKinoxService.class);
        PendingIntent pi = PendingIntent.getService(context, alarmId, serviceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        DateTime inMinutes = (new DateTime()).plusMinutes(minutes);


        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT >= 23)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, inMinutes.getMillis(), pi);
        else {
            if(Build.VERSION.SDK_INT >= 19) {
                am.setExact(AlarmManager.RTC_WAKEUP, inMinutes.getMillis(), pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, inMinutes.getMillis(), pi);
            }
        }

    }
}
