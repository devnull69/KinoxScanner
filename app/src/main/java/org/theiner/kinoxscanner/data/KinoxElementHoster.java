package org.theiner.kinoxscanner.data;

import org.theiner.kinoxscanner.strategien.HosterStrategie;

import java.io.Serializable;
import java.util.List;

/**
 * Created by TTheiner on 10.03.2016.
 */
public class KinoxElementHoster implements Serializable{
    private KinoxElement foundElement;
    private HosterMirror hosterMirror;
    private List<VideoLink> videoLinks;

    private static final long serialVersionUID = 0L;

    public List<VideoLink> getVideoLinks() {
        return videoLinks;
    }

    public void setVideoLinks(List<VideoLink> videoLinks) {
        this.videoLinks = videoLinks;
    }

    public KinoxElement getFoundElement() {
        return foundElement;
    }

    public void setFoundElement(KinoxElement foundElement) {
        this.foundElement = foundElement;
    }

    public HosterMirror getHosterMirror() {
        return hosterMirror;
    }

    public void setHosterMirror(HosterMirror hosterMirror) {
        this.hosterMirror = hosterMirror;
    }
}
