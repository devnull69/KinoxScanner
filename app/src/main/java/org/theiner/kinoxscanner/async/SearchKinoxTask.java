package org.theiner.kinoxscanner.async;

import android.os.AsyncTask;

import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.util.List;

/**
 * Created by TTheiner on 07.03.2016.
 */
public class SearchKinoxTask extends AsyncTask<SearchRequest, Void, List<SearchResult>> {

    public static interface CheckCompleteListener {
        void onCheckComplete(List<SearchResult> result);
    }

    private CheckCompleteListener ccl = null;

    public SearchKinoxTask(CheckCompleteListener ccl) {
        this.ccl = ccl;
    }

    @Override
    protected List<SearchResult> doInBackground(SearchRequest... suchen) {
        List<SearchResult> ergebnisse = KinoxHelper.search(suchen[0]);
        return ergebnisse;
    }

    @Override
    protected void onPostExecute(List<SearchResult> result) {
        ccl.onCheckComplete(result);
    }
}
