package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.FilmSerieAdapter;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.Serie;

import java.util.Collections;
import java.util.List;

public class ManageSerienFragment extends Fragment {

    private KinoxScannerApplication myApp;
    private BaseAdapter adapter = null;
    private ListView lvSerien;
    private Button btnNewSeries;
    private Activity me;
    private List<Serie> mySerien;

    private BroadcastReceiver mMessageReceiver = null;

    public final static String EXTRA_MESSAGE = "org.theiner.kinoxscanner.MESSAGESERIE";
    public static int REQUEST_EDIT_SERIE = 101;
    public static int RESULT_UPDATE_LIST = 201;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("kinoxscanner", "ManageSerienFragment onCreate");

        me = this.getActivity();
        myApp = (KinoxScannerApplication) me.getApplicationContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_manage_serien, null);
        Log.d("kinoxscanner", "ManageSerienFragment onCreateView");

        mySerien = myApp.getSerien();
        Collections.sort(mySerien);

        adapter = new FilmSerieAdapter<Serie>(me, mySerien);
        lvSerien = (ListView) layout.findViewById(R.id.lvSerien);
        lvSerien.setAdapter(adapter);

        btnNewSeries = (Button) layout.findViewById(R.id.btnNewSeries);

        btnNewSeries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewSeries(v);
            }
        });

        lvSerien.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                Serie selected = (Serie) listview.getItemAtPosition(position);
                int currentIndex = myApp.getSerien().indexOf(selected);
                Intent intent = new Intent(me, EditSerieActivity.class);
                intent.putExtra(EXTRA_MESSAGE, currentIndex);
                startActivityForResult(intent, REQUEST_EDIT_SERIE);
            }
        });

        // Receive message to delete Series (from UpdateKinoxElementActivity via OverviewFragment)
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("updatelist"));

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("kinoxscanner", "ManageSerienFragment onActivityCreated");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_EDIT_SERIE) {
            if(resultCode == RESULT_UPDATE_LIST) {
                Boolean updateList = data.getBooleanExtra("updateList", false);
                if(updateList != null && updateList) {
                    Collections.sort(mySerien);
                    // notify the adapter about the change
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public void onNewSeries(View view) {
        int currentIndex = -1;
        Intent intent = new Intent(me, EditSerieActivity.class);
        intent.putExtra(EXTRA_MESSAGE, currentIndex);
        startActivityForResult(intent, REQUEST_EDIT_SERIE);
    }


}
