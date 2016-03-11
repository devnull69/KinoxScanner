package org.theiner.kinoxscanner.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by TTheiner on 10.03.2016.
 */
public class KinoxHosterResponse {
    @JsonProperty("Stream")
    private String stream;

    @JsonProperty("Replacement")
    private String replacement;

    @JsonProperty("HosterName")
    private String hosterName;

    @JsonProperty("HosterHome")
    private String hosterHome;

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public String getHosterName() {
        return hosterName;
    }

    public void setHosterName(String hosterName) {
        this.hosterName = hosterName;
    }

    public String getHosterHome() {
        return hosterHome;
    }

    public void setHosterHome(String hosterHome) {
        this.hosterHome = hosterHome;
    }
}
