package org.theiner.kinoxscanner.data;

import android.graphics.Bitmap;

/**
 * Created by TTheiner on 04.03.2016.
 */
public interface KinoxElement {
    public String toQueryString();
    public String toHosterRequestQueryString(int hoster, int mirror);

    public Bitmap imgFromCache();
}
