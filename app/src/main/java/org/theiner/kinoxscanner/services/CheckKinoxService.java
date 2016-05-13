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

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.activities.OverviewFragment;
import org.theiner.kinoxscanner.activities.ViewPagerActivity;
import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.util.AlarmHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class CheckKinoxService extends Service {
    private static int ALARM_ID = 141414;

    private final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    private void setNewCountInSettings(int neueAnzahl) {
        SharedPreferences settings = getSharedPreferences(OverviewFragment.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("alteAnzahl", neueAnzahl);
        editor.putLong("lastChecked", (new Date()).getTime());
        editor.commit();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final KinoxScannerApplication myApp = new KinoxScannerApplication();

        final SharedPreferences settings = getSharedPreferences(OverviewFragment.PREFS_NAME, MODE_PRIVATE);
        myApp.getObjectsFromSharedPreferences(settings);

        final Context me = this;

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            final int alteAnzahl = settings.getInt("alteAnzahl", 0);
            final float multiplier = settings.getFloat("multiplier", -1);
            CheckKinoxTask.CheckCompleteListener ccl = new CheckKinoxTask.CheckCompleteListener() {
                @Override
                public void onCheckComplete(List<CheckErgebnis> result) {

                    if (result.size() > alteAnzahl) {
                        sendNotification(result.size() + getString(R.string.FilesReady), multiplier);
                    }
                    setNewCountInSettings(result.size());

                    // Update badge
                    ShortcutBadger.applyCount(me, result.size());

                    // end the service
                    stopSelf();
                }

                @Override
                public void onProgress(Integer progress) {

                }
            };

            CheckKinoxTask myTask = new CheckKinoxTask(ccl);
            myTask.execute(myApp);

            // Set an alarm for the next time this service should run:
            setAlarmInMinutes(60);
        } else {
            // Network was not available, try again in 10 minutes
            setAlarmInMinutes(10);
            // end the service
            stopSelf();
        }


    }

    public void setAlarmInMinutes(int minutes) {

        // Jede Stunde
        AlarmHelper.setAlarm(this, ALARM_ID, minutes);
    }

    public void sendNotification(String notifyText, float multiplier) {

        if(isNotificationEnabled(this)) {
            Bitmap myLargeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.large_icon);

            myLargeIcon = Bitmap.createScaledBitmap(myLargeIcon, (int)(myLargeIcon.getWidth()*multiplier), (int)(myLargeIcon.getHeight()*multiplier), false);

            Intent mainIntent = new Intent(this, ViewPagerActivity.class);
            @SuppressWarnings("deprecation")
            Notification noti = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(this, ALARM_ID+1, mainIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle(getString(R.string.DownloadsAvailable))
                    .setSmallIcon(R.mipmap.small_icon)
                    .setLargeIcon(myLargeIcon)
                    .setContentText(notifyText)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setTicker(getString(R.string.DownloadsAvailableLong))
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
