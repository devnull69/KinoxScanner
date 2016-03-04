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
import org.theiner.kinoxscanner.data.Serie;

public class UpdateSerieActivity extends AppCompatActivity {

    private Serie currentSerie;
    private KinoxScannerApplication myApp;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_serie);

        myApp = (KinoxScannerApplication) getApplicationContext();

        Intent intent = getIntent();
        currentIndex = intent.getIntExtra(OverviewActivity.EXTRA_MESSAGE, -1);
        currentSerie = myApp.getSerien().get(currentIndex);

        TextView txtSerienName = (TextView) findViewById(R.id.txtSerienName);
        txtSerienName.setText(currentSerie.toString());
    }

    public void onExit(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteLine", false);
        setResult(OverviewActivity.RESULT_IS_OK, resultIntent);

        finish();
    }

    public void onNextEpisode(View view) {
        // Nächste Episode ablegen
        currentSerie.setEpisode(currentSerie.getEpisode() + 1);
        updateSerienInSharedPreferences();
    }

    public void onNextSeason(View view) {
        // Nächste Staffel mit Episode 1 ablegen
        currentSerie.setSeason(currentSerie.getSeason() + 1);
        currentSerie.setEpisode(1);
        updateSerienInSharedPreferences();
    }

    public void onRemoveSeries(View view) {
        // Serie komplett entfernen
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

}
