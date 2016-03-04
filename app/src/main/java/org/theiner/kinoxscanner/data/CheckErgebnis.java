package org.theiner.kinoxscanner.data;

/**
 * Created by TTheiner on 27.02.2016.
 */
public class CheckErgebnis {
    public String name;
    public String datum;
    public String videoLink;
    public KinoxElement foundElement;

    @Override
    public String toString() {
        return this.name;
    }
}
