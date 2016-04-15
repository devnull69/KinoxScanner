package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.FilmSerieAdapter;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.FilmSerieWrapper;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.util.ImageHelper;

import java.util.Collections;
import java.util.List;

public class ManageSerienFragment extends Fragment {

    private KinoxScannerApplication myApp;
    private BaseAdapter adapter = null;
    private ListView lvSerien;
    private Activity me;
    private List<FilmSerieWrapper> mySerien;

    private Menu myMenu = null;

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

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_manage_serien, null);
        Log.d("kinoxscanner", "ManageSerienFragment onCreateView");

        lvSerien = (ListView) layout.findViewById(R.id.lvSerien);

        updateAdapter();

        lvSerien.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                Serie selected = (Serie) ((FilmSerieWrapper) listview.getItemAtPosition(position)).getKinoxelement();
                int currentIndex = myApp.getSerien().indexOf(selected);
                Intent intent = new Intent(me, EditSerieActivity.class);
                intent.putExtra(EXTRA_MESSAGE, currentIndex);
                startActivityForResult(intent, REQUEST_EDIT_SERIE);
            }
        });

        lvSerien.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toggleItem(position);
                return true;
            }
        });

        // Receive message to delete Series (from UpdateKinoxElementActivity via OverviewFragment)
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateAdapter();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("updatelist"));

        return layout;
    }

    private void toggleItem(int position) {
        FilmSerieWrapper item = (FilmSerieWrapper) lvSerien.getItemAtPosition(position);
        item.setSelected(!item.isSelected());
        adapter.notifyDataSetChanged();

        showOrHideMenuItems();
    }

    public void showOrHideMenuItems() {
        boolean show = false;

        for(int i=0; i<mySerien.size(); i++) {
            if(mySerien.get(i).isSelected()) {
                show = true;
                break;
            }
        }

        MenuItem mItem = myMenu.findItem(R.id.action_remove);
        if(mItem != null)
            mItem.setVisible(show);
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
                    updateAdapter();
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

        myMenu = menu;
        // hide options
        myMenu.findItem(R.id.action_options).setVisible(false);

        // show delete icon if selected elements exist
        showOrHideMenuItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_add:
                onNewSeries(getView());
                return true;
            case R.id.action_remove:
                showDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onNewSeries(View view) {
        int currentIndex = -1;
        Intent intent = new Intent(me, EditSerieActivity.class);
        intent.putExtra(EXTRA_MESSAGE, currentIndex);
        startActivityForResult(intent, REQUEST_EDIT_SERIE);
    }

    public void showDeleteDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.HeaderDeleteSeriesDialog)
                .setMessage(R.string.QuestionDeleteSeriesDialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onRemoveSeries();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })

                .show();
    }

    public void onRemoveSeries() {
        // Ergebnisliste aus Overview
        List<CheckErgebnis> ergebnisse = myApp.getErgebnisliste();

        for(int i=0; i<mySerien.size(); i++) {
            if(mySerien.get(i).isSelected()) {
                // entfernen
                Serie aktuelleSerie = (Serie)mySerien.get(i).getKinoxelement();
                myApp.removeSeries(aktuelleSerie);
                // Image aus dem Cache löschen
                ImageHelper.removeImage(aktuelleSerie.getAddr());

                for(int e=0; e<ergebnisse.size(); e++) {
                    if(ergebnisse.get(e).foundElement.equals(aktuelleSerie))
                        ergebnisse.remove(e);
                }
            }
        }

        // In Preferences ablegen
        SharedPreferences settings = getActivity().getSharedPreferences(OverviewFragment.PREFS_NAME, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        ObjectMapper mapper = new ObjectMapper();
        String jsonSerien = "[]";
        try {
            jsonSerien = mapper.writeValueAsString(myApp.getSerien());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        editor.putString("serien", jsonSerien);


        // Update alte Anzahl
        editor.putInt("alteAnzahl", 0);

        editor.commit();

        // adapter anpassen
        updateAdapter();

        // Overview-Fragment dazu auffordern, seine Liste zu aktualisieren
        Intent intent = new Intent("updateergebnisliste");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void updateAdapter() {
        mySerien = FilmSerieWrapper.wrapAllItems(myApp.getSerien());
        Collections.sort(mySerien);

        adapter = new FilmSerieAdapter(me, mySerien);
        lvSerien.setAdapter(adapter);

        if(myMenu != null)
            showOrHideMenuItems();
    }
}
