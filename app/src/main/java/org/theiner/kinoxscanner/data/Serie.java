package org.theiner.kinoxscanner.data;

import android.graphics.Bitmap;

import org.theiner.kinoxscanner.util.ImageHelper;

import java.io.Serializable;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class Serie implements KinoxElement, Serializable, Comparable<Serie>{
    private String name;
    private String addr;
    private int seriesID;
    private int season;
    private int episode;
    private String imageSubDir;

    private static final long serialVersionUID = 0L;

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

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    @Override
    public String toQueryString() {
        return "?Addr=" + addr + "&SeriesID=" + seriesID + "&Season=" + season + "&Episode=" + episode;
    }

    @Override
    public String toHosterRequestQueryString(int hoster, int mirror) {
        return addr + "&Hoster=" + hoster + "&Mirror=" + mirror + "&Season=" + season + "&Episode=" + episode;
    }

    @Override
    public String toString() {
        return name + " " + season + "x" + ((episode<10)?"0":"") + episode;
    }

    @Override
    public Bitmap imgFromCache() {
        return ImageHelper.getImage(addr);

    }

    @Override
    public int compareTo(Serie another) {
        return this.toString().compareTo(another.toString());
    }
}
