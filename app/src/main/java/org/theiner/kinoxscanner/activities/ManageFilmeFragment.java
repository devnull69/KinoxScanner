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
import android.widget.Button;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.FilmSerieAdapter;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.FilmSerieWrapper;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.util.ImageHelper;

import java.util.Collections;
import java.util.List;

public class ManageFilmeFragment extends Fragment {

    private KinoxScannerApplication myApp;
    private BaseAdapter adapter = null;
    private ListView lvFilme;
    private Activity me;
    private List<FilmSerieWrapper> myFilme;

    private Menu myMenu = null;

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

        lvFilme = (ListView) layout.findViewById(R.id.lvFilme);

        updateAdapter();

        lvFilme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                Film selected = (Film) ((FilmSerieWrapper) listview.getItemAtPosition(position)).getKinoxelement();
                int currentIndex = myApp.getFilme().indexOf(selected);
                Intent intent = new Intent(me, EditFilmActivity.class);
                intent.putExtra(EXTRA_MESSAGE, currentIndex);
                startActivityForResult(intent, REQUEST_EDIT_FILM);
            }
        });

        lvFilme.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toggleItem(position);
                return true;
            }
        });

        // Receive message to delete Film (from UpdateKinoxElementActivity via OverviewFragment)
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
        FilmSerieWrapper item = (FilmSerieWrapper) lvFilme.getItemAtPosition(position);
        item.setSelected(!item.isSelected());
        adapter.notifyDataSetChanged();

        showOrHideMenuItems();
    }

    public void showOrHideMenuItems() {
        boolean show = false;

        for(int i=0; i<myFilme.size(); i++) {
            if(myFilme.get(i).isSelected()) {
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
        Log.d("kinoxscanner", "ManageFilmeFragment onActivityCreated");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_EDIT_FILM) {
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
                onNewFilm(getView());
                return true;
            case R.id.action_remove:
                showDeleteDialog();
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

    public void showDeleteDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.HeaderDeleteFilmDialog)
                .setMessage(R.string.QuestionDeleteFilmDialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onRemoveFilm();
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

    public void onRemoveFilm() {
        // Ergebnisliste aus Overview
        List<CheckErgebnis> ergebnisse = myApp.getErgebnisliste();

        for(int i=0; i<myFilme.size(); i++) {
            if(myFilme.get(i).isSelected()) {
                // entfernen
                Film aktuellerFilm = (Film)myFilme.get(i).getKinoxelement();
                myApp.removeFilm(aktuellerFilm);
                // Image aus dem Cache löschen
                ImageHelper.removeImage(aktuellerFilm.getAddr());

                if(ergebnisse != null)
                    for(int e=0; e<ergebnisse.size(); e++) {
                        if(ergebnisse.get(e).foundElement.equals(aktuellerFilm))
                            ergebnisse.remove(e);
                    }
            }
        }

        // In Preferences ablegen
        SharedPreferences settings = getActivity().getSharedPreferences(OverviewFragment.PREFS_NAME, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        ObjectMapper mapper = new ObjectMapper();
        String jsonFilme = "[]";
        try {
            jsonFilme = mapper.writeValueAsString(myApp.getFilme());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        editor.putString("filme", jsonFilme);


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
        myFilme = FilmSerieWrapper.wrapAllItems(myApp.getFilme());
        Collections.sort(myFilme);

        adapter = new FilmSerieAdapter(me, myFilme);
        lvFilme.setAdapter(adapter);

        if(myMenu != null)
            showOrHideMenuItems();
    }
}
