package org.theiner.kinoxscanner.data;

import java.io.Serializable;

/**
 * Created by TTheiner on 21.03.2016.
 */
public class VideoLink implements Serializable{
    private String hosterName;
    private String videoURL;
    private String filename;
    private String mimeType;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private static final long serialVersionUID = 0L;

    public String getHosterName() {
        return hosterName;
    }

    public void setHosterName(String hosterName) {
        this.hosterName = hosterName;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    @Override
    public String toString() {
        return videoURL;
    }

    public String getExtensionFromMimeType() {
        return "." + mimeType.substring(mimeType.length()-3);
    }
}
