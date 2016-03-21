package org.theiner.kinoxscanner.data;

import org.theiner.kinoxscanner.strategien.HosterStrategie;

import java.io.Serializable;

/**
 * Created by TTheiner on 10.03.2016.
 */
public class HosterMirror implements Serializable{
    private int mirrorCount;
    private HosterStrategie strategie;

    private static final long serialVersionUID = 0L;

    public HosterStrategie getStrategie() {
        return strategie;
    }

    public void setStrategie(HosterStrategie strategie) {
        this.strategie = strategie;
    }

    public int getMirrorCount() {
        return mirrorCount;
    }

    public void setMirrorCount(int mirrorCount) {
        this.mirrorCount = mirrorCount;
    }
}
