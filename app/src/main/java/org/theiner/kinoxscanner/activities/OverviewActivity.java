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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.services.AlarmStarterService;
import org.theiner.kinoxscanner.util.AlarmHelper;
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OverviewActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "KinoxScannerFile";
    public final static String EXTRA_MESSAGE = "org.theiner.kinoxscanner.MESSAGE";

    public static int REQUEST_DELETE_LINE = 100;
    public static int RESULT_IS_OK = 200;
    public static int REQUEST_SEARCH = 103;
    public static int RESULT_UPDATE_ELEMENTS = 203;

    private KinoxScannerApplication myApp;

    private ListView lvDownload = null;
    private List<CheckErgebnis> ergebnisListe = null;
    private BaseAdapter adapter = null;
    private TextView txtStatus = null;

    private int currentListIndex = -1;

    private void zeigeWerte() {
        final Activity me = this;

        CheckKinoxTask.CheckCompleteListener ccl = new CheckKinoxTask.CheckCompleteListener() {
            @Override
            public void onCheckComplete(List<CheckErgebnis> result) {

                ergebnisListe = result;
                txtStatus = (TextView) findViewById(R.id.txtStatus);
                if(ergebnisListe.size()==0) {
                    txtStatus.setText("Keine Ergebnisse gefunden.");
                } else {
                    txtStatus.setText("Folgende Downloads stehen bereit:");
                    adapter = new ArrayAdapter<CheckErgebnis>(me, android.R.layout.simple_list_item_1, ergebnisListe);
                    lvDownload = (ListView) findViewById(R.id.lvDownloads);
                    lvDownload.setAdapter(adapter);

                    lvDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                            currentListIndex = position;
                            CheckErgebnis selected = (CheckErgebnis) listview.getItemAtPosition(position);
                            if(selected.foundElement instanceof Serie) {
                                Serie currentSerie = (Serie) selected.foundElement;
                                int currentIndex = myApp.getSerien().indexOf(currentSerie);
                                Intent intent = new Intent(me, UpdateSerieActivity.class);
                                intent.putExtra(EXTRA_MESSAGE, currentIndex);
                                startActivityForResult(intent, REQUEST_DELETE_LINE);
                            } else {
                                Film currentFilm = (Film) selected.foundElement;
                                int currentIndex = myApp.getFilme().indexOf(currentFilm);
                                Intent intent = new Intent(me, UpdateFilmActivity.class);
                                intent.putExtra(EXTRA_MESSAGE, currentIndex);
                                startActivityForResult(intent, REQUEST_DELETE_LINE);
                            }
                        }
                    });

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("alteAnzahl", result.size());
                    editor.commit();
                }
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

        // TODO:Entfernen
//        ObjectMapper mapper = new ObjectMapper();
//        List<Film> filme = KinoxHelper.getFilme();
//        String jsonFilme = "[]";
//        try {
//            jsonFilme = mapper.writeValueAsString(filme);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        myEditor.putString("filme", jsonFilme);
        // TODO: END

        myEditor.putFloat("multiplier", multiplier);

        myEditor.commit();

        zeigeWerte();


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
}
