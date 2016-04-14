package org.theiner.kinoxscanner.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TTheiner on 14.04.2016.
 */
public class FilmSerieWrapper implements Comparable<FilmSerieWrapper> {
    private KinoxElement kinoxelement;
    private boolean selected;

    public static List<FilmSerieWrapper> wrapAllItems(List<?> items) {
        List<FilmSerieWrapper> result = new ArrayList<>();

        for(int i=0; i<items.size(); i++) {
            result.add(new FilmSerieWrapper((KinoxElement)(items.get(i))));
        }

        return result;
    }

    public FilmSerieWrapper(KinoxElement kinoxelement) {
        this.kinoxelement = kinoxelement;
        this.selected = false;
    }

    public KinoxElement getKinoxelement() {
        return kinoxelement;
    }

    public void setKinoxelement(KinoxElement kinoxelement) {
        this.kinoxelement = kinoxelement;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(FilmSerieWrapper another) {
        if(this.getKinoxelement() instanceof Serie) {
            return ((Serie)this.getKinoxelement()).compareTo((Serie) another.getKinoxelement());
        } else {
            return ((Film)this.getKinoxelement()).compareTo((Film) another.getKinoxelement());
        }
    }
}
