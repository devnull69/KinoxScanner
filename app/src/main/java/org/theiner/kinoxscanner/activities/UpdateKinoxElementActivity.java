package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.HosterAdapter;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.HosterMirror;
import org.theiner.kinoxscanner.data.KinoxElement;
import org.theiner.kinoxscanner.data.KinoxElementHoster;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.data.VideoLink;
import org.theiner.kinoxscanner.util.ImageHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UpdateKinoxElementActivity extends AppCompatActivity {

    private KinoxElement currentKinoxElement;
    private KinoxScannerApplication myApp;
    private CheckErgebnis currentErgebnis;
    private int currentIndex;

    private boolean elementRemoved = false;

    private BaseAdapter adapter = null;
    private ListView lvHosterMirrors = null;

    public static String EXTRA_MESSAGE = "org.theiner.kinoxscanner.SHOWVIDEOURLS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_kinoxelement);

        myApp = (KinoxScannerApplication) getApplicationContext();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        currentErgebnis = (CheckErgebnis) extras.getSerializable(OverviewFragment.EXTRA_MESSAGE_CHECKERGEBNIS);
        currentIndex = extras.getInt(OverviewFragment.EXTRA_MESSAGE_CURRENTINDEX);
        currentKinoxElement = currentErgebnis.foundElement;

        // currentKinoxElement ist eine Kopie (wegen Serialisierung). Über den Index erhalte ich das korrekte
        // Element aus myApp
        if(currentKinoxElement instanceof Serie)
            currentKinoxElement = myApp.getSerien().get(currentIndex);
        else
            currentKinoxElement = myApp.getFilme().get(currentIndex);

        TextView txtName = (TextView) findViewById(R.id.txtName);
        txtName.setText(currentKinoxElement.toString());

        if(currentKinoxElement instanceof Serie) {
            // Serie, also Filmelemente entfernen
            View film1 = findViewById(R.id.btnFilm1);
            View film2 = findViewById(R.id.btnFilm2);

            ViewManager manager = (ViewManager) film1.getParent();
            manager.removeView(film1);
            manager.removeView(film2);
        } else {
            // Film, also Serienelemente entfernen und Titel ändern
            View serie1 = findViewById(R.id.btnSerie1);
            View serie2 = findViewById(R.id.btnSerie2);
            View serie3 = findViewById(R.id.btnSerie3);

            ViewManager manager = (ViewManager) serie1.getParent();
            manager.removeView(serie1);
            manager.removeView(serie2);
            manager.removeView(serie3);

            setTitle(getString(R.string.UpdateFilm));
        }

        // Hoster-Mirrors in Listview anzeigen
        adapter = new HosterAdapter(this, currentErgebnis.hosterMirrors);
        lvHosterMirrors = (ListView) findViewById(R.id.lvHosterMirrors);
        lvHosterMirrors.setAdapter(adapter);

        final Activity me = this;

        // Bei Click => Zur Videoübersicht des Hosters
        lvHosterMirrors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                HosterMirror selected = (HosterMirror) listview.getItemAtPosition(position);

                KinoxElementHoster currentHoster = new KinoxElementHoster();
                currentHoster.setHosterMirror(selected);
                currentHoster.setFoundElement(currentKinoxElement);
                Intent intent = new Intent(me, ShowHosterVideosActivity.class);
                intent.putExtra(EXTRA_MESSAGE, currentHoster);
                startActivity(intent);
            }
        });
    }

    public void onExit(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteLine", false);
        setResult(OverviewFragment.RESULT_IS_OK, resultIntent);

        finish();
    }

    public void onNextEpisode(View view) {
        // Nächste Episode ablegen
        Serie currentSerie = (Serie) currentKinoxElement;
        currentSerie.setEpisode(currentSerie.getEpisode() + 1);
        updateSerienInSharedPreferences();
    }

    public void onNextSeason(View view) {
        // Nächste Staffel mit Episode 1 ablegen
        Serie currentSerie = (Serie) currentKinoxElement;
        currentSerie.setSeason(currentSerie.getSeason() + 1);
        currentSerie.setEpisode(1);
        updateSerienInSharedPreferences();
    }

    public void onRemoveSeries(View view) {
        // Serie komplett entfernen
        Serie currentSerie = (Serie) currentKinoxElement;
        myApp.removeSerieAt(currentIndex);
        // Bild aus dem Cache löschen
        ImageHelper.removeImage(currentSerie.getAddr());

        // ListView auf ManageSerienFragment aktualisieren
        elementRemoved = true;

        updateSerienInSharedPreferences();
    }

    private void updateSerienInSharedPreferences() {
        // Update serien
        SharedPreferences settings = getSharedPreferences(OverviewFragment.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        ObjectMapper mapper = new ObjectMapper();
        String jsonSerien = "[]";
        try {
            jsonSerien = mapper.writeValueAsString(myApp.getSerien());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        editor.putString("serien", jsonSerien);


        // Update alte Anzahl
        editor.putInt("alteAnzahl", 0);

        editor.commit();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteLine", true);
        resultIntent.putExtra("elementRemoved", elementRemoved);
        setResult(OverviewFragment.RESULT_IS_OK, resultIntent);

        finish();
    }

    public void onUpdateDate(View view) {
        // Datum auf "heute" aktualisieren
        Film currentFilm = (Film) currentKinoxElement;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        currentFilm.setLastDate(sdf.format(new Date()));
        updateFilmeInSharedPreferences();
    }

    public void onRemoveFilm(View view) {
        // Film komplett entfernen
        Film currentFilm = (Film) currentKinoxElement;
        myApp.removeFilmAt(currentIndex);
        // Bild aus dem Cache löschen
        ImageHelper.removeImage(currentFilm.getAddr());

        // ListView auf ManageFilmeFragment aktualisieren
        elementRemoved = true;

        updateFilmeInSharedPreferences();

    }

    private void updateFilmeInSharedPreferences() {
        // Update filme
        SharedPreferences settings = getSharedPreferences(OverviewFragment.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        ObjectMapper mapper = new ObjectMapper();
        String jsonFilme = "[]";
        try {
            jsonFilme = mapper.writeValueAsString(myApp.getFilme());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        editor.putString("filme", jsonFilme);


        // Update alte Anzahl
        editor.putInt("alteAnzahl", 0);

        editor.commit();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteLine", true);
        resultIntent.putExtra("elementRemoved", elementRemoved);
        setResult(OverviewFragment.RESULT_IS_OK, resultIntent);

        finish();
    }
}
