package org.theiner.kinoxscanner.services;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.activities.OverviewActivity;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.util.AlarmHelper;
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class CheckKinox extends Service {
    private static int ALARM_ID = 141414;

    private final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences settings = getSharedPreferences(OverviewActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putLong("lastChecked", (new Date()).getTime());
        // Netzwerkverbindung vorhanden?
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Bisher gespeichert
            ObjectMapper mapper = new ObjectMapper();
            List<CheckErgebnis> alteErgebnisse = null;
            try {
                alteErgebnisse = mapper.readValue(settings.getString("ergebnisse", "[]"), new TypeReference<List<CheckErgebnis>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            int alteAnzahl = (alteErgebnisse != null ? alteErgebnisse.size() : 0);
            List<CheckErgebnis> ergebnisse = KinoxHelper.check();

            // Speichern in SharedPreferences
            String jsonString = "[]";
            try {
                jsonString = mapper.writeValueAsString(ergebnisse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            editor.putString("ergebnisse", jsonString);

            // Evtl. Notification erzeugen
            int anzahl = ergebnisse.size();
            float multiplier = settings.getFloat("multiplier", -1);

            if (anzahl > alteAnzahl)
                sendNotification(anzahl + " Datei(en) stehen jetzt bereit.", multiplier);
        }

        editor.commit();

        // Set an alarm for the next time this service should run:
        setAlarm();

        // end the service
        stopSelf();
    }

    public void setAlarm() {

        // Jede Stunde
        AlarmHelper.setAlarm(this, ALARM_ID, 1);
    }

    public void sendNotification(String notifyText, float multiplier) {

        if(isNotificationEnabled(this)) {
            Bitmap myLargeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.large_icon);

            myLargeIcon = Bitmap.createScaledBitmap(myLargeIcon, (int)(myLargeIcon.getWidth()*multiplier), (int)(myLargeIcon.getHeight()*multiplier), false);

            Intent mainIntent = new Intent(this, OverviewActivity.class);
            @SuppressWarnings("deprecation")
            Notification noti = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(this, ALARM_ID+1, mainIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle("Downloads verfügbar!")
                    .setSmallIcon(R.mipmap.small_icon)
                    .setLargeIcon(myLargeIcon)
                    .setContentText(notifyText)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setTicker("Es sind Downloads verfügbar!")
                    .setWhen(System.currentTimeMillis())
                    .getNotification();

            NotificationManager notificationManager
                    = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(ALARM_ID+2, noti);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isNotificationEnabled(Context context) {

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();

        int uid = appInfo.uid;

        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */

        try {

            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int)opPostNotificationValue.get(Integer.class);

            return ((int)checkOpNoThrowMethod.invoke(mAppOps,value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
