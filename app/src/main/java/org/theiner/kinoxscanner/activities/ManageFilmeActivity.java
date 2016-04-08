package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.Serie;

public class ManageFilmeActivity extends AppCompatActivity {

    private KinoxScannerApplication myApp;
    private BaseAdapter adapter = null;
    private ListView lvFilme;

    public final static String EXTRA_MESSAGE = "org.theiner.kinoxscanner.MESSAGEFILME";
    public static int REQUEST_EDIT_FILM = 102;
    public static int RESULT_UPDATE_LIST = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_filme);

        myApp = (KinoxScannerApplication) getApplicationContext();

        adapter = new ArrayAdapter<Film>(this, android.R.layout.simple_list_item_1, myApp.getFilme());
        lvFilme = (ListView) findViewById(R.id.lvFilme);
        lvFilme.setAdapter(adapter);

        final Activity me = this;

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
    }

    public void onExit(View view) {
        finish();
    }

    public void onNewFilm(View view) {
        int currentIndex = -1;
        Intent intent = new Intent(this, EditFilmActivity.class);
        intent.putExtra(EXTRA_MESSAGE, currentIndex);
        startActivityForResult(intent, REQUEST_EDIT_FILM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_EDIT_FILM) {
            if(resultCode == RESULT_UPDATE_LIST) {
                Boolean updateList = data.getBooleanExtra("updateList", false);
                if(updateList != null && updateList) {
                    // notify the adapter about the change
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

}
