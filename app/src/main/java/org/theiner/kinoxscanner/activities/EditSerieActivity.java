package org.theiner.kinoxscanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.async.GetImageTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.util.ImageHelper;

public class EditSerieActivity extends AppCompatActivity {

    private KinoxScannerApplication myApp;
    private EditText editName = null;
    private EditText editAddr = null;
    private EditText editSeriesID = null;
    private EditText editImageSubDir = null;
    private EditText editSeason = null;
    private EditText editEpisode = null;
    private ImageView ivCoverArt = null;

    private int currentIndex = -1;
    private Serie aktuelleSerie = null;

    private boolean isAddrLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_serie);

        myApp = (KinoxScannerApplication) getApplicationContext();

        Intent intent = getIntent();
        currentIndex = intent.getIntExtra(ManageSerienFragment.EXTRA_MESSAGE, -1);

        editName = (EditText) findViewById(R.id.editName);
        editAddr = (EditText) findViewById(R.id.editAddr);
        editSeriesID = (EditText)findViewById(R.id.editSeriesID);
        editImageSubDir = (EditText) findViewById(R.id.editImageSubDir);
        editSeason = (EditText) findViewById(R.id.editSeason);
        editEpisode = (EditText) findViewById(R.id.editEpisode);
        ivCoverArt = (ImageView) findViewById(R.id.ivCoverArt);

        if(currentIndex != -1) {
            aktuelleSerie = myApp.getSerien().get(currentIndex);
            editName.setText(aktuelleSerie.getName());
            editAddr.setText(aktuelleSerie.getAddr());
            editSeriesID.setText(String.valueOf(aktuelleSerie.getSeriesID()));
            editImageSubDir.setText(aktuelleSerie.getImageSubDir());
            editSeason.setText(String.valueOf(aktuelleSerie.getSeason()));
            editEpisode.setText(String.valueOf(aktuelleSerie.getEpisode()));

            Bitmap coverArt = aktuelleSerie.imgFromCache();
            if(coverArt != null) {
                ivCoverArt.setImageBitmap(coverArt);
            } else {
                if(!aktuelleSerie.getAddr().equals("") && !aktuelleSerie.getImageSubDir().equals(""))
                    ImageHelper.startGetImageTask(ivCoverArt, aktuelleSerie.getImageSubDir(), aktuelleSerie.getAddr());
            }

        } else {
            // Überschrift ändern auf "Neue Serie erfassen"
            setTitle(getString(R.string.CreateNewSeries));
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
        setResult(ManageSerienFragment.RESULT_UPDATE_LIST, resultIntent);
        finish();
    }

    public void onSave(View view) {

        // Vollständigkeitsprüfung
        String strName = editName.getText().toString();
        String strAddr = editAddr.getText().toString();
        String strSeriesID = editSeriesID.getText().toString();
        String strImageSubDir = editImageSubDir.getText().toString();
        String strSeason = editSeason.getText().toString();
        String strEpisode = editEpisode.getText().toString();

        if(!"".equals(strName) && !"".equals(strAddr) && !"".equals(strSeriesID) && !"".equals(strImageSubDir) && !"".equals(strSeason) && !"".equals(strEpisode)) {

            if (currentIndex == -1) {
                // Neue Serie anlegen
                Serie neueSerie = new Serie();
                neueSerie.setName(strName);
                neueSerie.setAddr(strAddr);
                neueSerie.setSeriesID(Integer.parseInt(strSeriesID));
                neueSerie.setImageSubDir(strImageSubDir);
                neueSerie.setSeason(Integer.parseInt(strSeason));
                neueSerie.setEpisode(Integer.parseInt(strEpisode));
                myApp.addSerie(neueSerie);
            } else {
                // aktuelle Serie updaten
                aktuelleSerie.setName(strName);
                aktuelleSerie.setAddr(strAddr);
                aktuelleSerie.setSeriesID(Integer.parseInt(strSeriesID));
                aktuelleSerie.setImageSubDir(strImageSubDir);
                aktuelleSerie.setSeason(Integer.parseInt(strSeason));
                aktuelleSerie.setEpisode(Integer.parseInt(strEpisode));
            }

            // In Preferences ablegen
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


            // Zurück und Manage-Liste aktualisieren!
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updateList", true);
            setResult(ManageSerienFragment.RESULT_UPDATE_LIST, resultIntent);
            finish();
        } else {
            Toast.makeText(this, R.string.series_incomplete, Toast.LENGTH_LONG).show();
        }
    }

    public void onSearch(View view) {
        SearchRequest suche = new SearchRequest();
        suche.setSuchString(editName.getText().toString());
        suche.setIsSerie(true);

        Intent searchIntent = new Intent(this, SearchResultActivity.class);
        searchIntent.putExtra(SearchResultActivity.EXTRA_MESSAGE, suche);
        startActivityForResult(searchIntent, OverviewFragment.REQUEST_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == OverviewFragment.REQUEST_SEARCH) {
            if(resultCode == OverviewFragment.RESULT_UPDATE_ELEMENTS) {
                final SearchResult suchErgebnis = (SearchResult) data.getSerializableExtra("suchErgebnis");
                if(suchErgebnis == null) {
                    Toast.makeText(this, R.string.NoSeriesFound, Toast.LENGTH_SHORT).show();
                    isAddrLocked = false;
                } else {
                    editAddr.setText(suchErgebnis.getAddr());
                    editSeriesID.setText(String.valueOf(suchErgebnis.getSeriesID()));
                    editImageSubDir.setText(suchErgebnis.getImageSubDir());

                    ImageHelper.startGetImageTask(ivCoverArt, suchErgebnis.getImageSubDir(), suchErgebnis.getAddr());

                    isAddrLocked = true;
                }
            }
        }
    }

    public void onRemoveText(View view) {
        editName.setText("");
    }
}
