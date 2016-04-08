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
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;

public class EditFilmActivity extends AppCompatActivity {

    private KinoxScannerApplication myApp;
    private EditText editName = null;
    private EditText editAddr = null;
    private EditText editLastDate = null;
    private Button btnRemoveFilm = null;

    private int currentIndex = -1;
    private Film aktuellerFilm = null;

    private boolean isAddrLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_film);

        myApp = (KinoxScannerApplication) getApplicationContext();

        Intent intent = getIntent();
        currentIndex = intent.getIntExtra(ManageFilmeFragment.EXTRA_MESSAGE, -1);

        editName = (EditText) findViewById(R.id.editName);
        editAddr = (EditText) findViewById(R.id.editAddr);
        editLastDate = (EditText)findViewById(R.id.editLastDate);
        btnRemoveFilm = (Button) findViewById(R.id.btnRemoveFilm);

        if(currentIndex != -1) {
            aktuellerFilm = myApp.getFilme().get(currentIndex);
            editName.setText(aktuellerFilm.getName());
            editAddr.setText(aktuellerFilm.getAddr());
            editLastDate.setText(aktuellerFilm.getLastDate());

            btnRemoveFilm.setVisibility(View.VISIBLE);
        } else {
            // Überschrift ändern auf "Neuen Film erfassen"
            setTitle("Neuen Film erfassen");
            btnRemoveFilm.setVisibility(View.INVISIBLE);
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
        setResult(ManageFilmeFragment.RESULT_UPDATE_LIST, resultIntent);
        finish();
    }

    public void onSave(View view) {
        if(currentIndex == -1) {
            // Neuen Film anlegen
            Film neuerFilm = new Film();
            neuerFilm.setName(editName.getText().toString());
            neuerFilm.setAddr(editAddr.getText().toString());
            neuerFilm.setLastDate(editLastDate.getText().toString());
            myApp.addFilm(neuerFilm);
        } else {
            // aktuellen Film updaten
            aktuellerFilm.setName(editName.getText().toString());
            aktuellerFilm.setAddr(editAddr.getText().toString());
            aktuellerFilm.setLastDate(editLastDate.getText().toString());
        }

        // In Preferences ablegen
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


        // Zurück und Manage-Liste aktualisieren!
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updateList", true);
        setResult(ManageFilmeFragment.RESULT_UPDATE_LIST, resultIntent);
        finish();
    }

    public void onRemoveFilm(View view) {
        if(currentIndex != -1) {
            // Aus Liste löschen
            myApp.removeFilmAt(currentIndex);

            // In Preferences ablegen
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
        }

        // Zurück und Manage-Liste aktualisieren!
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updateList", true);
        setResult(ManageFilmeFragment.RESULT_UPDATE_LIST, resultIntent);
        finish();
    }

    public void onSearch(View view) {
        SearchRequest suche = new SearchRequest();
        suche.setSuchString(editName.getText().toString());
        suche.setIsSerie(false);

        Intent searchIntent = new Intent(this, SearchResultActivity.class);
        searchIntent.putExtra(SearchResultActivity.EXTRA_MESSAGE, suche);
        startActivityForResult(searchIntent, OverviewFragment.REQUEST_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == OverviewFragment.REQUEST_SEARCH) {
            if(resultCode == OverviewFragment.RESULT_UPDATE_ELEMENTS) {
                SearchResult suchErgebnis = (SearchResult) data.getSerializableExtra("suchErgebnis");
                if(suchErgebnis == null) {
                    Toast.makeText(this, "Kein Film gefunden.", Toast.LENGTH_SHORT).show();
                    isAddrLocked = false;
                } else {
                    editAddr.setText(suchErgebnis.getAddr());
                    isAddrLocked = true;
                }
            }
        }
    }

}
