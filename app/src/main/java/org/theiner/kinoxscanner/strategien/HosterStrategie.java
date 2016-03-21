package org.theiner.kinoxscanner.strategien;

import java.io.Serializable;

/**
 * Created by TTheiner on 16.03.2016.
 */
public abstract class HosterStrategie implements Serializable {
    public String hosterName;
    public int hosterNummer;

    private static final long serialVersionUID = 0L;

    public abstract String getVideoURL(String hosterURL);
}
