package org.theiner.kinoxscanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;

public class EditSerieActivity extends AppCompatActivity {

    private KinoxScannerApplication myApp;
    private EditText editName = null;
    private EditText editAddr = null;
    private EditText editSeriesID = null;
    private EditText editSeason = null;
    private EditText editEpisode = null;
    private Button btnRemoveSerie = null;

    private int currentIndex = -1;
    private Serie aktuelleSerie = null;

    private boolean isAddrLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_serie);

        myApp = (KinoxScannerApplication) getApplicationContext();

        Intent intent = getIntent();
        currentIndex = intent.getIntExtra(ManageSerienActivity.EXTRA_MESSAGE, -1);

        editName = (EditText) findViewById(R.id.editName);
        editAddr = (EditText) findViewById(R.id.editAddr);
        editSeriesID = (EditText)findViewById(R.id.editSeriesID);
        editSeason = (EditText) findViewById(R.id.editSeason);
        editEpisode = (EditText) findViewById(R.id.editEpisode);
        btnRemoveSerie = (Button) findViewById(R.id.btnRemoveSerie);

        if(currentIndex != -1) {
            aktuelleSerie = myApp.getSerien().get(currentIndex);
            editName.setText(aktuelleSerie.getName());
            editAddr.setText(aktuelleSerie.getAddr());
            editSeriesID.setText(String.valueOf(aktuelleSerie.getSeriesID()));
            editSeason.setText(String.valueOf(aktuelleSerie.getSeason()));
            editEpisode.setText(String.valueOf(aktuelleSerie.getEpisode()));

            btnRemoveSerie.setVisibility(View.VISIBLE);
        } else {
            // Überschrift ändern auf "Neue Serie erfassen"
            setTitle("Neue Serie erfassen");
            btnRemoveSerie.setVisibility(View.INVISIBLE);
        }

        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!isAddrLocked)
                    editAddr.setText(replaceAllSonderzeichen(editName.getText().toString()));
            }
        });
    }

    private String replaceAllSonderzeichen(String inputString) {
        return inputString.replaceAll(" ", "_").replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ü", "ue").replaceAll("ß", "ss").replaceAll("Ä", "Ae").replaceAll("Ö", "Oe").replaceAll("Ü", "Ue");
    }

    public void onExit(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updateList", false);
        setResult(ManageSerienActivity.RESULT_UPDATE_LIST, resultIntent);
        finish();
    }

    public void onSave(View view) {
        if(currentIndex == -1) {
            // Neue Serie anlegen
            Serie neueSerie = new Serie();
            neueSerie.setName(editName.getText().toString());
            neueSerie.setAddr(editAddr.getText().toString());
            neueSerie.setSeriesID(Integer.parseInt(editSeriesID.getText().toString()));
            neueSerie.setSeason(Integer.parseInt(editSeason.getText().toString()));
            neueSerie.setEpisode(Integer.parseInt(editEpisode.getText().toString()));
            myApp.addSerie(neueSerie);
        } else {
            // aktuelle Serie updaten
            aktuelleSerie.setName(editName.getText().toString());
            aktuelleSerie.setAddr(editAddr.getText().toString());
            aktuelleSerie.setSeriesID(Integer.parseInt(editSeriesID.getText().toString()));
            aktuelleSerie.setSeason(Integer.parseInt(editSeason.getText().toString()));
            aktuelleSerie.setEpisode(Integer.parseInt(editEpisode.getText().toString()));
        }

        // In Preferences ablegen
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


        // Zurück und Manage-Liste aktualisieren!
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updateList", true);
        setResult(ManageSerienActivity.RESULT_UPDATE_LIST, resultIntent);
        finish();
    }

    public void onRemoveSerie(View view) {
        if(currentIndex != -1) {
            // Aus Liste löschen
            myApp.removeSerieAt(currentIndex);

            // In Preferences ablegen
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
        }

        // Zurück und Manage-Liste aktualisieren!
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updateList", true);
        setResult(ManageSerienActivity.RESULT_UPDATE_LIST, resultIntent);
        finish();
    }

    public void onSearch(View view) {
        SearchRequest suche = new SearchRequest();
        suche.setSuchString(editName.getText().toString());
        suche.setIsSerie(true);

        Intent searchIntent = new Intent(this, SearchResultActivity.class);
        searchIntent.putExtra(SearchResultActivity.EXTRA_MESSAGE, suche);
        startActivityForResult(searchIntent, OverviewActivity.REQUEST_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == OverviewActivity.REQUEST_SEARCH) {
            if(resultCode == OverviewActivity.RESULT_UPDATE_ELEMENTS) {
                SearchResult suchErgebnis = (SearchResult) data.getSerializableExtra("suchErgebnis");
                if(suchErgebnis == null) {
                    Toast.makeText(this, "Keine Serie gefunden.", Toast.LENGTH_SHORT).show();
                    isAddrLocked = false;
                } else {
                    editAddr.setText(suchErgebnis.getAddr());
                    editSeriesID.setText(String.valueOf(suchErgebnis.getSeriesID()));
                    isAddrLocked = true;
                }
            }
        }
    }
}
