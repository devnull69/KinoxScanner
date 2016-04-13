package org.theiner.kinoxscanner.async;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.util.ImageHelper;
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.util.List;

/**
 * Created by TTheiner on 07.03.2016.
 */
public class GetImageTask extends AsyncTask<String, Void, Bitmap> {

    public static interface CheckCompleteListener {
        void onCheckComplete(Bitmap result);
    }

    private CheckCompleteListener ccl = null;

    public GetImageTask(CheckCompleteListener ccl) {
        this.ccl = ccl;
    }

    @Override
    protected Bitmap doInBackground(String... addrs) {
        Bitmap ergebnis = ImageHelper.retrieveImage(addrs[0], addrs[1]);
        return ergebnis;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ccl.onCheckComplete(result);
    }
}
