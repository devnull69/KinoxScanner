package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.async.CollectVideoLinksTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.services.AlarmStarterService;
import org.theiner.kinoxscanner.util.AlarmHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "KinoxScannerFile";
    public final static String EXTRA_MESSAGE_CHECKERGEBNIS = "org.theiner.kinoxscanner.MESSAGECHECKERGEBNIS";
    public final static String EXTRA_MESSAGE_CURRENTINDEX = "org.theiner.kinoxscanner.MESSAGECURRENTINDEX";

    public static int REQUEST_DELETE_LINE = 100;
    public static int RESULT_IS_OK = 200;
    public static int REQUEST_SEARCH = 103;
    public static int RESULT_UPDATE_ELEMENTS = 203;

    private KinoxScannerApplication myApp;

    private ListView lvDownload = null;
    private List<CheckErgebnis> ergebnisListe = null;
    private BaseAdapter adapter = null;
    private TextView txtStatus = null;
    private ProgressBar pbProgress = null;
    private Button btnScanAgain = null;

    private int currentListIndex = -1;

    private void zeigeWerte() {
        final Activity me = this;

        CheckKinoxTask.CheckCompleteListener ccl = new CheckKinoxTask.CheckCompleteListener() {
            @Override
            public void onCheckComplete(List<CheckErgebnis> result) {

                btnScanAgain.setEnabled(true);

                ergebnisListe = result;
                txtStatus = (TextView) findViewById(R.id.txtStatus);
                if(ergebnisListe.size()==0) {
                    txtStatus.setTypeface(Typeface.DEFAULT);

                    txtStatus.setText("Keine Ergebnisse gefunden.");
                    // Progress-Bar verstecken
                    //((ViewManager) pbProgress.getParent()).removeView(pbProgress);
                    pbProgress.setVisibility(View.GONE);
                } else {
                    // Progress-Bar verstecken
                    //((ViewManager) pbProgress.getParent()).removeView(pbProgress);
                    pbProgress.setVisibility(View.GONE);
                    txtStatus.setTypeface(Typeface.DEFAULT);
                    txtStatus.setText("Folgende Downloads stehen bereit:");

                    adapter = new ArrayAdapter<CheckErgebnis>(me, android.R.layout.simple_list_item_1, ergebnisListe);
                    lvDownload = (ListView) findViewById(R.id.lvDownloads);
                    lvDownload.setAdapter(adapter);

                    lvDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                        currentListIndex = position;
                        CheckErgebnis selected = (CheckErgebnis) listview.getItemAtPosition(position);
                        int currentIndex = -1;
                        if(selected.foundElement instanceof Serie)
                            currentIndex = myApp.getSerien().indexOf(selected.foundElement);
                        else
                            currentIndex = myApp.getFilme().indexOf(selected.foundElement);

                        Intent intent = new Intent(me, UpdateKinoxElementActivity.class);
                        Bundle extras = new Bundle();
                        extras.putSerializable(EXTRA_MESSAGE_CHECKERGEBNIS, selected);
                        extras.putInt(EXTRA_MESSAGE_CURRENTINDEX, currentIndex);
                        intent.putExtras(extras);
                        startActivityForResult(intent, REQUEST_DELETE_LINE);
                        }
                    });

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("alteAnzahl", result.size());
                    editor.commit();
                }
            }

            @Override
            public void onProgress(Integer progress) {
                pbProgress.setProgress(progress);
            }
        };

        CheckKinoxTask myTask = new CheckKinoxTask(ccl);
        myTask.execute(myApp);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        myApp = (KinoxScannerApplication) getApplicationContext();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        myApp.getObjectsFromSharedPreferences(settings);

        // Density metrics merken für Large Icon der Notification
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        float multiplier = metrics.density/3f;   // Bitmap liegt mit 480dpi vor (density Faktor 3), die Bildschirmauflösung kann aber geringer sein

        SharedPreferences.Editor myEditor = settings.edit();

        myEditor.putFloat("multiplier", multiplier);

        myEditor.commit();

        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        btnScanAgain = (Button) findViewById(R.id.btnScanAgain);

        // Bei bestehender Netzwerkverbindung:
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            zeigeWerte();
        else {
            //((ViewManager) pbProgress.getParent()).removeView(pbProgress);

            btnScanAgain.setEnabled(true);

            pbProgress.setVisibility(View.GONE);
            txtStatus = (TextView) findViewById(R.id.txtStatus);
            txtStatus.setTypeface(Typeface.DEFAULT_BOLD);
            txtStatus.setText("Es besteht derzeit keine Netzwerkverbindung!");
        }



        startService(new Intent(this, AlarmStarterService.class));

        TextView txtAlarmSet = (TextView) findViewById(R.id.txtAlarmSet);
        PendingIntent pi = AlarmHelper.getPendingIntentFromAlarm(this, 141414);
        if(pi!=null) {
            txtAlarmSet.setText("Alarm ist gesetzt!");
        } else {
            txtAlarmSet.setText("Alarm ist NICHT gesetzt!");
        }

        TextView txtLastChecked= (TextView) findViewById(R.id.txtLastChecked);
        long lastChecked = settings.getLong("lastChecked", -1);
        String lastCheckedStr = "never";
        if(lastChecked != -1) {
            Date lastCheckedDate = new Date(lastChecked);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            lastCheckedStr = sdf.format(lastCheckedDate);
        }
        txtLastChecked.setText(lastCheckedStr);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //zeigeWerte();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_DELETE_LINE) {
            if(resultCode == RESULT_IS_OK) {
                Boolean deleteLine = data.getBooleanExtra("deleteLine", false);
                if(deleteLine != null && deleteLine) {
                    // change the List for the adapter and notify the adapter about it
                    ergebnisListe.remove(currentListIndex);
                    adapter.notifyDataSetChanged();

                    // Evtl. Überschrift ändern
                    if(ergebnisListe.size() == 0) {
                        txtStatus.setTypeface(Typeface.DEFAULT);
                        txtStatus.setText("Keine Ergebnisse gefunden.");
                    }
                }
            }
        }
    }


    public void onExit(View view) {
        System.exit(0);
    }

    public void onManageSerien(View view) {
        Intent intent = new Intent(this, ManageSerienActivity.class);
        startActivity(intent);
    }

    public void onManageFilme(View view) {
        Intent intent = new Intent(this, ManageFilmeActivity.class);
        startActivity(intent);
    }

    public void onScanAgain(View view) {
        // Ursprungszustand herstellen und dann Werte zeigen, falls Internetverbindung besteht

        // Progressbar einschalten und auf 0 setzen
        pbProgress.setVisibility(View.VISIBLE);
        pbProgress.setProgress(0);

        //Button "Erneut scannen" disabled
        btnScanAgain.setEnabled(false);

        //Statustext löschen
        txtStatus.setText("");

        // ListView leeren falls nötig
        if(adapter!= null) {
            ergebnisListe.clear();
            adapter.notifyDataSetChanged();
        }

        // Bei bestehender Netzwerkverbindung:
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            zeigeWerte();
        else {

            btnScanAgain.setEnabled(true);

            //((ViewManager) pbProgress.getParent()).removeView(pbProgress);
            pbProgress.setVisibility(View.GONE);
            txtStatus = (TextView) findViewById(R.id.txtStatus);
            txtStatus.setTypeface(Typeface.DEFAULT_BOLD);
            txtStatus.setText("Es besteht derzeit keine Netzwerkverbindung!");
        }

    }
}
