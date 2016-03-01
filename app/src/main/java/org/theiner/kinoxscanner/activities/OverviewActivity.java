package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.services.AlarmStarterService;
import org.theiner.kinoxscanner.util.AlarmHelper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OverviewActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "KinoxScannerFile";

    private void zeigeWerte() {
        final Activity me = this;

        CheckKinoxTask.CheckCompleteListener ccl = new CheckKinoxTask.CheckCompleteListener() {
            @Override
            public void onCheckComplete(List<CheckErgebnis> result) {

                TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
                if(result.size()==0) {
                    txtStatus.setText("Keine Ergebnisse gefunden.");
                } else {
                    txtStatus.setText("Folgende Downloads stehen bereit:");
                    ListAdapter adapter = new ArrayAdapter<CheckErgebnis>(me, android.R.layout.simple_list_item_1, result);
                    ListView lvDownload = (ListView) findViewById(R.id.lvDownloads);
                    lvDownload.setAdapter(adapter);
                }
            }
        };

        CheckKinoxTask myTask = new CheckKinoxTask(ccl);
        myTask.execute("test");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        zeigeWerte();


//        startService(new Intent(this, AlarmStarterService.class));
//
//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//
//        // Density metrics merken für Large Icon der Notification
//        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
//        float multiplier = metrics.density/3f;   // Bitmap liegt mit 480dpi vor (density Faktor 3), die Bildschirmauflösung kann aber geringer sein
//
//        SharedPreferences.Editor myEditor = settings.edit();
//        myEditor.putFloat("multiplier", multiplier);
//        myEditor.commit();
//
//        ObjectMapper mapper = new ObjectMapper();
//        List<CheckErgebnis> ergebnisse = null;
//        try {
//            ergebnisse = mapper.readValue(settings.getString("ergebnisse", "[]"), new TypeReference<List<CheckErgebnis>>() {
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        TextView txtAlarm = (TextView) findViewById(R.id.txtAlarm);
//        PendingIntent pi = AlarmHelper.getPendingIntentFromAlarm(this, 141414);
//        if(pi!=null) {
//            txtAlarm.setText("Alarm ist gesetzt!");
//        } else {
//            txtAlarm.setText("Alarm ist nicht gesetzt!");
//        }
//
//        TextView txtLastChecked = (TextView) findViewById(R.id.txtLastChecked);
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
//        long lastChecked = settings.getLong("lastChecked", -1);
//        String lastCheckedStr = "never";
//        if(lastChecked != -1)
//            lastCheckedStr = sdf.format(new Date(lastChecked));
//
//        txtLastChecked.setText(lastCheckedStr);
//
//        TextView txtNetworkAvailable = (TextView) findViewById(R.id.txtNetworkAvailable);
//        txtNetworkAvailable.setText(String.valueOf(settings.getBoolean("networkAvailable", false)));
//
//        TextView txtErgebnisse = (TextView) findViewById(R.id.txtErgebnisse);
//        txtErgebnisse.setText(String.valueOf(settings.getInt("ergebnisZahl", -1)));
//
//        ListAdapter adapter = new ArrayAdapter<CheckErgebnis>(this, android.R.layout.simple_list_item_1, ergebnisse);
//        ListView lvDownload = (ListView) findViewById(R.id.lvDownloads);
//        lvDownload.setAdapter(adapter);
//
    }

    @Override
    protected void onResume() {
        super.onResume();
        zeigeWerte();
    }


    public void onExit(View view) {
        System.exit(0);
    }
}
