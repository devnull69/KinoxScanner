package org.theiner.kinoxscanner.data;

import java.io.Serializable;

/**
 * Created by TTheiner on 07.03.2016.
 */
public class SearchRequest implements Serializable{
    private String suchString;
    private Boolean isSerie;

    private static final long serialVersionUID = 0L;

    public String getSuchString() {
        return suchString;
    }

    public void setSuchString(String suchString) {
        this.suchString = suchString;
    }

    public Boolean getIsSerie() {
        return isSerie;
    }

    public void setIsSerie(Boolean isSerie) {
        this.isSerie = isSerie;
    }

}
