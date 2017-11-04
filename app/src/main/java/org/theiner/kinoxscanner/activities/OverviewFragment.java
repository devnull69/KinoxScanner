package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.CheckErgebnisAdapter;
import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.services.AlarmStarterService;
import org.theiner.kinoxscanner.util.AlarmHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

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
    private TextView txtAlarmSet = null;
    private TextView txtLastChecked = null;
    private SharedPreferences settings = null;
    private SwipeRefreshLayout swipeContainer = null;

    private BroadcastReceiver mMessageReceiver;

    private Activity me = null;

    private int currentListIndex = -1;

    private void zeigeWerte() {
        final Activity me = this.getActivity();

        ergebnisListe = myApp.getErgebnisliste();
        if(ergebnisListe == null) {
            CheckKinoxTask.CheckCompleteListener ccl = new CheckKinoxTask.CheckCompleteListener() {
                @Override
                public void onCheckComplete(List<CheckErgebnis> result) {

                    ergebnisListe = result;

                    // Merken, damit bei Neuerstellen des Fragments nicht neu gescannt wird
                    myApp.setErgebnisliste(ergebnisListe);

                    txtStatus.setTypeface(Typeface.DEFAULT);
                    pbProgress.setVisibility(View.GONE);
                    if(ergebnisListe == null) {
                        txtStatus.setText(R.string.ConnectError);
                    } else if (ergebnisListe.size() == 0) {
                        txtStatus.setText(R.string.NoResultsFound);
                    } else {
                        txtStatus.setText(R.string.DownloadsReady);

                        adapter = new CheckErgebnisAdapter(me, ergebnisListe);
                        lvDownload.setAdapter(adapter);

                        SharedPreferences settings = me.getSharedPreferences(PREFS_NAME, me.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("alteAnzahl", result.size());
                        editor.commit();

                        // Show Badge
                        ShortcutBadger.applyCount(me, result.size());
                    }

                    swipeContainer.setRefreshing(false);

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
            if (ergebnisListe.size() == 0) {
                txtStatus.setText(R.string.NoResultsFound);
            } else {
                txtStatus.setText(R.string.DownloadsReady);

                adapter = new CheckErgebnisAdapter(me, ergebnisListe);
                lvDownload.setAdapter(adapter);

                // Show Badge
                ShortcutBadger.applyCount(this.getContext(), ergebnisListe.size());
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

        // Receive message to delete Series (from UpdateKinoxElementActivity via OverviewFragment)
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(adapter != null)
                    adapter.notifyDataSetChanged();
                if (ergebnisListe != null && ergebnisListe.size() == 0)
                    txtStatus.setText(R.string.NoResultsFound);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("updateergebnisliste"));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("kinoxscanner", "OverviewFragment onCreate");

        // Aufrufen, falls die Daten korrupt sind!
        //cleanupFilmeUndSerien();

        me = this.getActivity();
        myApp = (KinoxScannerApplication) me.getApplicationContext();

        settings = me.getSharedPreferences(PREFS_NAME, me.MODE_PRIVATE);

        if(myApp.getSerien() == null)
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
        final View layout = inflater.inflate(R.layout.fragment_overview, null);

        txtStatus = (TextView) layout.findViewById(R.id.txtStatus);
        lvDownload = (ListView) layout.findViewById(R.id.lvDownloads);
        pbProgress = (ProgressBar) layout.findViewById(R.id.pbProgress);
        txtAlarmSet = (TextView) layout.findViewById(R.id.txtAlarmSet);
        txtLastChecked= (TextView) layout.findViewById(R.id.txtLastChecked);

        swipeContainer = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);// Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onScanAgain(layout);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

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

            pbProgress.setVisibility(View.GONE);
            txtStatus.setTypeface(Typeface.DEFAULT_BOLD);
            txtStatus.setText(R.string.NoNetworkConnection);
        }

        PendingIntent pi = AlarmHelper.getPendingIntentFromAlarm(me, 141414);
        if(pi==null) {
            txtAlarmSet.setText(R.string.AlarmWillBeSet);
        }

        me.startService(new Intent(me, AlarmStarterService.class));

        long lastChecked = settings.getLong("lastChecked", -1);
        String lastCheckedStr = me.getString(R.string.never);
        if(lastChecked != -1) {
            Date lastCheckedDate = new Date(lastChecked);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            lastCheckedStr = sdf.format(lastCheckedDate);
        }
        txtLastChecked.setText(me.getString(R.string.LastChecked) + lastCheckedStr + " (" + settings.getString("kinoxurl", "http://www.kinox.to/") + ")");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_DELETE_LINE) {
            if(resultCode == RESULT_IS_OK) {
                Boolean elementRemoved = data.getBooleanExtra("elementRemoved", false);
                if(elementRemoved != null && elementRemoved) {
                    // Die Fragmente dazu auffordern, ihre Listen zu aktualisieren
                    Intent intent = new Intent("updatelist");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }

                Boolean deleteLine = data.getBooleanExtra("deleteLine", false);
                if(deleteLine != null && deleteLine) {
                    // change the List for the adapter and notify the adapter about it
                    ergebnisListe.remove(currentListIndex);
                    adapter.notifyDataSetChanged();

                    // Evtl. Überschrift ändern
                    if(ergebnisListe.size() == 0) {
                        txtStatus.setTypeface(Typeface.DEFAULT);
                        txtStatus.setText(R.string.NoResultsFound);
                    }

                    // Badge aktualisieren
                    ShortcutBadger.applyCount(this.getContext(), ergebnisListe.size());

                    // Alte Anzahl aktualisieren
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("alteAnzahl", ergebnisListe.size());
                    editor.commit();
                }
            }
        }
    }

    public void onScanAgain(View view) {
        // Ursprungszustand herstellen und dann Werte zeigen, falls Internetverbindung besteht

        // Progressbar einschalten und auf 0 setzen
        pbProgress.setVisibility(View.VISIBLE);
        pbProgress.setProgress(0);

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

            //((ViewManager) pbProgress.getParent()).removeView(pbProgress);
            pbProgress.setVisibility(View.GONE);
            txtStatus.setTypeface(Typeface.DEFAULT_BOLD);
            txtStatus.setText(R.string.NoNetworkConnection);
        }

    }

    private void cleanupFilmeUndSerien() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        List<Serie> serien = getSerien();
        ObjectMapper mapper = new ObjectMapper();
        String jsonSerien = "[]";
        try {
            jsonSerien = mapper.writeValueAsString(serien);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        editor.putString("serien", jsonSerien);

        List<Film> filme = getFilme();
        mapper = new ObjectMapper();
        String jsonFilme = "[]";
        try {
            jsonFilme = mapper.writeValueAsString(filme);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        editor.putString("filme", jsonFilme);

        editor.commit();


    }

    private List<Serie> getSerien() {
        List<Serie> result = new ArrayList<>();

        Serie twd = new Serie();
        twd.setName("The Walking Dead");
        twd.setAddr("The_Walking_Dead-1");
        twd.setSeriesID(10437);
        twd.setSeason(7);
        twd.setEpisode(1);
        result.add(twd);

        Serie shameless = new Serie();
        shameless.setName("Shameless");
        shameless.setAddr("Shameless-2");
        shameless.setSeriesID(39278);
        shameless.setSeason(6);
        shameless.setEpisode(1);
        result.add(shameless);

        Serie sn_en = new Serie();
        sn_en.setName("Supernatural (en)");
        sn_en.setAddr("Supernatural_german_subbed");
        sn_en.setSeriesID(27249);
        sn_en.setSeason(11);
        sn_en.setEpisode(19);
        result.add(sn_en);

        Serie sn_de = new Serie();
        sn_de.setName("Supernatural (de)");
        sn_de.setAddr("Supernatural");
        sn_de.setSeriesID(2375);
        sn_de.setSeason(11);
        sn_de.setEpisode(1);
        result.add(sn_de);

        Serie vikings = new Serie();
        vikings.setName("Vikings");
        vikings.setAddr("Vikings-1");
        vikings.setSeriesID(46277);
        vikings.setSeason(4);
        vikings.setEpisode(1);
        result.add(vikings);

        Serie devious = new Serie();
        devious.setName("Devious Maids");
        devious.setAddr("Devious_Maids-1");
        devious.setSeriesID(47777);
        devious.setSeason(4);
        devious.setEpisode(1);
        result.add(devious);

        Serie feartwd = new Serie();
        feartwd.setName("Fear The Walking Dead");
        feartwd.setAddr("Fear_the_Walking_Dead-1");
        feartwd.setSeriesID(55976);
        feartwd.setSeason(2);
        feartwd.setEpisode(2);
        result.add(feartwd);

        Serie tmithc = new Serie();
        tmithc.setName("The Man in the High Castle");
        tmithc.setAddr("The_Man_in_the_High_Castle");
        tmithc.setSeriesID(64040);
        tmithc.setSeason(2);
        tmithc.setEpisode(1);
        result.add(tmithc);

        Serie bcs = new Serie();
        bcs.setName("Better Call Saul");
        bcs.setAddr("Better_Call_Saul");
        bcs.setSeriesID(54593);
        bcs.setSeason(2);
        bcs.setEpisode(9);
        result.add(bcs);

        Serie ol = new Serie();
        ol.setName("Outlander");
        ol.setAddr("Outlander-3");
        ol.setSeriesID(54425);
        ol.setSeason(2);
        ol.setEpisode(1);
        result.add(ol);

        Serie ahs = new Serie();
        ahs.setName("American Horror Story");
        ahs.setAddr("American_Horror_Story-Die_dunkle_Seite_in_dir-1");
        ahs.setSeriesID(37361);
        ahs.setSeason(6);
        ahs.setEpisode(1);
        result.add(ahs);

        Serie tbbt = new Serie();
        tbbt.setName("The Big Bang Theory");
        tbbt.setAddr("The_Big_Bang_Theory_german_subbed");
        tbbt.setSeriesID(27242);
        tbbt.setSeason(9);
        tbbt.setEpisode(20);
        result.add(tbbt);

        return result;
    }

    private List<Film> getFilme() {
        List<Film> result = new ArrayList<>();

        Film deadpool = new Film();
        deadpool.setName("Deadpool");
        deadpool.setAddr("Deadpool");
        deadpool.setLastDate("06.04.2016");
        result.add(deadpool);

        Film zoomania = new Film();
        zoomania.setName("Zoomania");
        zoomania.setAddr("Zoomania");
        zoomania.setLastDate("06.04.2016");
        result.add(zoomania);

        Film allegiant = new Film();
        allegiant.setName("Die Bestimmung-Allegiant Part 1");
        allegiant.setAddr("Die_Bestimmung-Allegiant_Part_1");
        allegiant.setLastDate("06.04.2016");
        result.add(allegiant);

        Film cloverfield = new Film();
        cloverfield.setName("10 Cloverfield Lane");
        cloverfield.setAddr("10_Cloverfield_Lane");
        cloverfield.setLastDate("10.04.2016");
        result.add(cloverfield);

        Film welle = new Film();
        welle.setName("Die 5. Welle");
        welle.setAddr("Die_5-Welle-1");
        welle.setLastDate("10.04.2016");
        result.add(welle);

        return result;
    }
}
