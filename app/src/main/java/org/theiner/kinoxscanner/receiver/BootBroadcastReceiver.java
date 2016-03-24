package org.theiner.kinoxscanner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.theiner.kinoxscanner.services.CheckKinoxService;

/**
 * Created by Thomas on 05.02.2016.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, CheckKinoxService.class);
        context.startService(serviceIntent);

        Toast.makeText(context, "Kinoxscanner: BOOT received.", Toast.LENGTH_SHORT).show();
    }
}
