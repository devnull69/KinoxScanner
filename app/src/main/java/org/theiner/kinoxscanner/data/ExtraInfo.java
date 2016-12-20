package org.theiner.kinoxscanner.data;

/**
 * Created by TTheiner on 12.04.2016.
 */
public class ExtraInfo {
    private int seriesID;
    private String imageSubDir;
    private String imdbRating;

    public ExtraInfo(int seriesID, String imageSubDir, String imdbRating) {
        this.seriesID = seriesID;
        this.imageSubDir = imageSubDir;
        this.imdbRating = imdbRating;
    }

    public int getSeriesID() {
        return seriesID;
    }

    public String getImageSubDir() {
        return imageSubDir;
    }

    public String getImdbRating() {
        return imdbRating;
    }
}
