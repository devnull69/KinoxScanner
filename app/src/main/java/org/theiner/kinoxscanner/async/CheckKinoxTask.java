package org.theiner.kinoxscanner.async;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.util.List;

/**
 * Created by TTheiner on 29.02.2016.
 */
public class CheckKinoxTask extends AsyncTask<KinoxScannerApplication, Void, List<CheckErgebnis>> {

    public static interface CheckCompleteListener {
        void onCheckComplete(List<CheckErgebnis> result);
    }

    private CheckCompleteListener ccl = null;

    public CheckKinoxTask(CheckCompleteListener ccl) {
        this.ccl = ccl;
    }

    @Override
    protected List<CheckErgebnis> doInBackground(KinoxScannerApplication... myApps) {
        List<CheckErgebnis> ergebnisse = KinoxHelper.check(myApps[0]);
        return ergebnisse;
    }

    @Override
    protected void onPostExecute(List<CheckErgebnis> result) {
        ccl.onCheckComplete(result);
    }
}
