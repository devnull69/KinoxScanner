package org.theiner.kinoxscanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewManager;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.KinoxElement;
import org.theiner.kinoxscanner.data.Serie;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateKinoxElementActivity extends AppCompatActivity {

    private KinoxElement currentKinoxElement;
    private KinoxScannerApplication myApp;
    private CheckErgebnis currentErgebnis;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_kinoxelement);

        myApp = (KinoxScannerApplication) getApplicationContext();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        currentErgebnis = (CheckErgebnis) extras.getSerializable(OverviewActivity.EXTRA_MESSAGE_CHECKERGEBNIS);
        currentIndex = extras.getInt(OverviewActivity.EXTRA_MESSAGE_CURRENTINDEX);
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

            setTitle("Film aktualisieren");
        }
    }

    public void onExit(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteLine", false);
        setResult(OverviewActivity.RESULT_IS_OK, resultIntent);

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
        updateSerienInSharedPreferences();
    }

    private void updateSerienInSharedPreferences() {
        // Update serien
        SharedPreferences settings = getSharedPreferences(OverviewActivity.PREFS_NAME, MODE_PRIVATE);
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
        setResult(OverviewActivity.RESULT_IS_OK, resultIntent);

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
        updateFilmeInSharedPreferences();
    }

    private void updateFilmeInSharedPreferences() {
        // Update filme
        SharedPreferences settings = getSharedPreferences(OverviewActivity.PREFS_NAME, MODE_PRIVATE);
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
        setResult(OverviewActivity.RESULT_IS_OK, resultIntent);

        finish();
    }
}
