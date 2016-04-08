package org.theiner.kinoxscanner.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.theiner.kinoxscanner.R;

public class ManageFilmeFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("kinoxscanner", "ManageFilmeFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_manage_filme, null);
        Log.d("kinoxscanner", "ManageFilmeFragment onCreateView");
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("kinoxscanner", "ManageFilmeFragment onActivityCreated");
    }


}
