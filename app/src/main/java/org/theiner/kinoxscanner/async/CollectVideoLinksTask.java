package org.theiner.kinoxscanner.async;

import android.os.AsyncTask;

import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.VideoLink;
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.util.List;

/**
 * Created by TTheiner on 07.03.2016.
 */
public class CollectVideoLinksTask extends AsyncTask<CheckErgebnis, Integer, String> {

    public static interface CheckCompleteListener {
        void onCheckComplete(String result);
        void onProgress(Integer progress);
    }

    private CheckCompleteListener ccl = null;

    public CollectVideoLinksTask(CheckCompleteListener ccl) {
        this.ccl = ccl;
    }

    public void doProgress(int value) {
        publishProgress(value);
    }

    @Override
    protected String doInBackground(CheckErgebnis... checkErgebnisse) {
        CheckErgebnis currentErgebnis = checkErgebnisse[0];
        List<VideoLink> videoLinks = KinoxHelper.collectVideoLinks(this, currentErgebnis);
        currentErgebnis.videoLinks = videoLinks;
        return "ok";
    }

    @Override
    protected void onPostExecute(String result) {
        ccl.onCheckComplete(result);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        ccl.onProgress(progress[0]);
    }
}
