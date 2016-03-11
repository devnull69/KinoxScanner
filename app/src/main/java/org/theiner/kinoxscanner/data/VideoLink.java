package org.theiner.kinoxscanner.data;

import java.io.Serializable;

/**
 * Created by TTheiner on 10.03.2016.
 */
public class VideoLink implements Serializable{
    private String hosterName;
    private int hosterNummer;
    private int mirrorCount;

    private static final long serialVersionUID = 0L;

    public String getHosterName() {
        return hosterName;
    }

    public void setHosterName(String hosterName) {
        this.hosterName = hosterName;
    }

    public int getHosterNummer() {
        return hosterNummer;
    }

    public void setHosterNummer(int hosterNummer) {
        this.hosterNummer = hosterNummer;
    }

    public int getMirrorCount() {
        return mirrorCount;
    }

    public void setMirrorCount(int mirrorCount) {
        this.mirrorCount = mirrorCount;
    }
}
