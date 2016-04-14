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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.FilmSerieAdapter;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.Film;

import java.util.Collections;
import java.util.List;

public class ManageFilmeFragment extends Fragment {

    private KinoxScannerApplication myApp;
    private BaseAdapter adapter = null;
    private ListView lvFilme;
    private Activity me;
    private List<Film> myFilme;

    private BroadcastReceiver mMessageReceiver = null;

    public final static String EXTRA_MESSAGE = "org.theiner.kinoxscanner.MESSAGEFILME";
    public static int REQUEST_EDIT_FILM = 102;
    public static int RESULT_UPDATE_LIST = 202;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("kinoxscanner", "ManageFilmeFragment onCreate");

        me = this.getActivity();
        myApp = (KinoxScannerApplication) me.getApplicationContext();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_manage_filme, null);
        Log.d("kinoxscanner", "ManageFilmeFragment onCreateView");

        myFilme = myApp.getFilme();
        Collections.sort(myFilme);

        adapter = new FilmSerieAdapter<Film>(me, myFilme);
        lvFilme = (ListView) layout.findViewById(R.id.lvFilme);
        lvFilme.setAdapter(adapter);

        lvFilme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                Film selected = (Film) listview.getItemAtPosition(position);
                int currentIndex = myApp.getFilme().indexOf(selected);
                Intent intent = new Intent(me, EditFilmActivity.class);
                intent.putExtra(EXTRA_MESSAGE, currentIndex);
                startActivityForResult(intent, REQUEST_EDIT_FILM);
            }
        });

        // Receive message to delete Film (from UpdateKinoxElementActivity via OverviewFragment)
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
        Log.d("kinoxscanner", "ManageFilmeFragment onActivityCreated");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_EDIT_FILM) {
            if(resultCode == RESULT_UPDATE_LIST) {
                Boolean updateList = data.getBooleanExtra("updateList", false);
                if(updateList != null && updateList) {
                    Collections.sort(myFilme);
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filmsandseries, menu);

        // hide options
        menu.findItem(R.id.action_options).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_add:
                onNewFilm(getView());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onNewFilm(View view) {
        int currentIndex = -1;
        Intent intent = new Intent(me, EditFilmActivity.class);
        intent.putExtra(EXTRA_MESSAGE, currentIndex);
        startActivityForResult(intent, REQUEST_EDIT_FILM);
    }

}
