package org.theiner.kinoxscanner.data;

import java.io.Serializable;

/**
 * Created by TTheiner on 08.03.2016.
 */
public class SearchResult implements Serializable {
    private String name;
    private String addr;
    private int seriesID;
    private String imageSubDir;
    private int languageCode;
    private String imdbRating;

    private static final long serialVersionUID = 0L;


    public String getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(String imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getImageSubDir() {
        return imageSubDir;
    }

    public void setImageSubDir(String imageSubDir) {
        this.imageSubDir = imageSubDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getSeriesID() {
        return seriesID;
    }

    public void setSeriesID(int seriesID) {
        this.seriesID = seriesID;
    }

    public int getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(int languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public String toString() {return name;}
}
