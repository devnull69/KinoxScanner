package org.theiner.kinoxscanner.context;

import android.app.Application;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.Serie;

import java.io.IOException;
import java.util.List;

/**
 * Created by TTheiner on 04.03.2016.
 */

public class KinoxScannerApplication extends Application {
    private List<Film> filme = null;
    private List<Serie> serien = null;
    private List<CheckErgebnis> ergebnisliste = null;

    public List<CheckErgebnis> getErgebnisliste() {
        return ergebnisliste;
    }

    public void setErgebnisliste(List<CheckErgebnis> ergebnisliste) {
        this.ergebnisliste = ergebnisliste;
    }

    public List<Film> getFilme() {
        return filme;
    }

    public void setFilme(List<Film> filme) {
        this.filme = filme;
    }

    public List<Serie> getSerien() {
        return serien;
    }

    public void setSerien(List<Serie> serien) {
        this.serien = serien;
    }

    public void removeSerieAt(int index) {
        serien.remove(index);
    }

    public void removeSeries(Serie toRemove) {
        serien.remove(serien.indexOf(toRemove));
    }

    public void removeFilmAt(int index) {
        filme.remove(index);
    }

    public void removeFilm(Film toRemove) {
        filme.remove(filme.indexOf(toRemove));
    }

    public void getObjectsFromSharedPreferences(SharedPreferences settings) {
        // Filme und Serien aus den SharedPreferences holen, falls vorhanden
        String filmeStr = settings.getString("filme", "[]");
        String serienStr = settings.getString("serien", "[]");
        ObjectMapper mapper = new ObjectMapper();
        try {
            filme = mapper.readValue(filmeStr, new TypeReference<List<Film>>() {
            });
            serien = mapper.readValue(serienStr, new TypeReference<List<Serie>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addSerie(Serie neueSerie) {
        serien.add(neueSerie);
    }

    public void addFilm(Film neuerFilm) {
        filme.add(neuerFilm);
    }
}
