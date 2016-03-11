package org.theiner.kinoxscanner.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TTheiner on 27.02.2016.
 */
public class CheckErgebnis implements Serializable{
    public String name;
    public String datum;
    public List<VideoLink> videoLinks = new ArrayList<>();
    public KinoxElement foundElement;

    private static final long serialVersionUID = 0L;

    @Override
    public String toString() {
        return this.name;
    }

}
