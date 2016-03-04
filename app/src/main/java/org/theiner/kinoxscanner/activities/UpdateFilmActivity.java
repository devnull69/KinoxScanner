package org.theiner.kinoxscanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.Serie;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateFilmActivity extends AppCompatActivity {

    private Film currentFilm;
    private KinoxScannerApplication myApp;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_film);

        myApp = (KinoxScannerApplication) getApplicationContext();

        Intent intent = getIntent();
        currentIndex = intent.getIntExtra(OverviewActivity.EXTRA_MESSAGE, -1);
        currentFilm = myApp.getFilme().get(currentIndex);

        TextView txtFilmName = (TextView) findViewById(R.id.txtFilmName);
        txtFilmName.setText(currentFilm.getName());
    }

    public void onExit(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteLine", false);
        setResult(OverviewActivity.RESULT_IS_OK, resultIntent);

        finish();
    }

    public void onUpdateDate(View view) {
        // Datum auf "heute" aktualisieren
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        currentFilm.setLastDate(sdf.format(new Date()));
        updateFilmeInSharedPreferences();
    }

    public void onRemoveFilm(View view) {
        // Film komplett entfernen
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
