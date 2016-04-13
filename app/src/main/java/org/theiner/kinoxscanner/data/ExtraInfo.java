package org.theiner.kinoxscanner.data;

/**
 * Created by TTheiner on 12.04.2016.
 */
public class ExtraInfo {
    private int seriesID;
    private String imageSubDir;

    public ExtraInfo(int seriesID, String imageSubDir) {
        this.seriesID = seriesID;
        this.imageSubDir = imageSubDir;
    }

    public int getSeriesID() {
        return seriesID;
    }

    public String getImageSubDir() {
        return imageSubDir;
    }
}
