package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.AlternateColorArrayAdapter;
import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.services.AlarmStarterService;
import org.theiner.kinoxscanner.util.AlarmHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OverviewFragment extends Fragment {

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
    private TextView txtAlarmSet = null;
    private TextView txtLastChecked = null;
    private SharedPreferences settings = null;

    private Activity me = null;

    private int currentListIndex = -1;

    private void zeigeWerte() {
        final Activity me = this.getActivity();

        ergebnisListe = myApp.getErgebnisliste();
        if(ergebnisListe == null) {
            CheckKinoxTask.CheckCompleteListener ccl = new CheckKinoxTask.CheckCompleteListener() {
                @Override
                public void onCheckComplete(List<CheckErgebnis> result) {

                    btnScanAgain.setEnabled(true);

                    ergebnisListe = result;

                    // Merken, damit bei Neuerstellen des Fragments nicht neu gescannt wird
                    myApp.setErgebnisliste(ergebnisListe);

                    txtStatus.setTypeface(Typeface.DEFAULT);
                    pbProgress.setVisibility(View.GONE);
                    if (ergebnisListe.size() == 0) {
                        txtStatus.setText("Keine Ergebnisse gefunden.");
                    } else {
                        txtStatus.setText("Folgende Downloads stehen bereit:");

                        adapter = new AlternateColorArrayAdapter<CheckErgebnis>(me, ergebnisListe);
                        lvDownload.setAdapter(adapter);

                        SharedPreferences settings = me.getSharedPreferences(PREFS_NAME, me.MODE_PRIVATE);
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
        } else {
            // Ergebnisliste schon vorhanden
            // ListView füllen, Progressbar disablen, Button enablen
            pbProgress.setVisibility(View.GONE);
            txtStatus.setTypeface(Typeface.DEFAULT);
            btnScanAgain.setEnabled(true);
            if (ergebnisListe.size() == 0) {
                txtStatus.setText("Keine Ergebnisse gefunden.");
            } else {
                txtStatus.setText("Folgende Downloads stehen bereit:");

                adapter = new AlternateColorArrayAdapter<CheckErgebnis>(me, ergebnisListe);
                lvDownload.setAdapter(adapter);
            }
        }
        lvDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                currentListIndex = position;
                CheckErgebnis selected = (CheckErgebnis) listview.getItemAtPosition(position);
                int currentIndex = -1;
                if (selected.foundElement instanceof Serie)
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

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("kinoxscanner", "OverviewFragment onCreate");

        me = this.getActivity();
        myApp = (KinoxScannerApplication) me.getApplicationContext();

        settings = me.getSharedPreferences(PREFS_NAME, me.MODE_PRIVATE);

        myApp.getObjectsFromSharedPreferences(settings);

        // Density metrics merken für Large Icon der Notification
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        float multiplier = metrics.density/3f;   // Bitmap liegt mit 480dpi vor (density Faktor 3), die Bildschirmauflösung kann aber geringer sein

        SharedPreferences.Editor myEditor = settings.edit();

        myEditor.putFloat("multiplier", multiplier);

        myEditor.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_overview, null);

        txtStatus = (TextView) layout.findViewById(R.id.txtStatus);
        lvDownload = (ListView) layout.findViewById(R.id.lvDownloads);
        pbProgress = (ProgressBar) layout.findViewById(R.id.pbProgress);
        btnScanAgain = (Button) layout.findViewById(R.id.btnScanAgain);
        txtAlarmSet = (TextView) layout.findViewById(R.id.txtAlarmSet);
        txtLastChecked= (TextView) layout.findViewById(R.id.txtLastChecked);

        btnScanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScanAgain(v);
            }
        });

        Log.d("kinoxscanner", "OverviewFragment onCreateView");
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("kinoxscanner", "OverviewFragment onActivityCreated");

        // Bei bestehender Netzwerkverbindung:
        ConnectivityManager connMgr = (ConnectivityManager)
                me.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            zeigeWerte();
        else {
            //((ViewManager) pbProgress.getParent()).removeView(pbProgress);

            btnScanAgain.setEnabled(true);

            pbProgress.setVisibility(View.GONE);
            txtStatus.setTypeface(Typeface.DEFAULT_BOLD);
            txtStatus.setText("Es besteht derzeit keine Netzwerkverbindung!");
        }



        me.startService(new Intent(me, AlarmStarterService.class));

        PendingIntent pi = AlarmHelper.getPendingIntentFromAlarm(me, 141414);
        if(pi!=null) {
            txtAlarmSet.setText("Alarm ist gesetzt!");
        } else {
            txtAlarmSet.setText("Alarm ist NICHT gesetzt!");
        }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void onScanAgain(View view) {
        // Ursprungszustand herstellen und dann Werte zeigen, falls Internetverbindung besteht

        // Progressbar einschalten und auf 0 setzen
        pbProgress.setVisibility(View.VISIBLE);
        pbProgress.setProgress(0);

        //Button "Erneut scannen" disabled
        btnScanAgain.setEnabled(false);

        //Statustext löschen
        txtStatus.setText("");

        // Gemerkte Ergebnisliste zurücksetzen
        myApp.setErgebnisliste(null);

        // ListView leeren falls nötig
        if(adapter != null) {
            ergebnisListe.clear();
            adapter.notifyDataSetChanged();
        }

        // Bei bestehender Netzwerkverbindung:
        ConnectivityManager connMgr = (ConnectivityManager)
                me.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            zeigeWerte();
        else {

            btnScanAgain.setEnabled(true);

            //((ViewManager) pbProgress.getParent()).removeView(pbProgress);
            pbProgress.setVisibility(View.GONE);
            txtStatus.setTypeface(Typeface.DEFAULT_BOLD);
            txtStatus.setText("Es besteht derzeit keine Netzwerkverbindung!");
        }

    }

}
