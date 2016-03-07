package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.Serie;

public class ManageSerienActivity extends AppCompatActivity {

    private KinoxScannerApplication myApp;
    private BaseAdapter adapter = null;
    private ListView lvSerien;

    public final static String EXTRA_MESSAGE = "org.theiner.kinoxscanner.MESSAGESERIE";
    public static int REQUEST_EDIT_SERIE = 101;
    public static int RESULT_UPDATE_LIST = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_serien);

        myApp = (KinoxScannerApplication) getApplicationContext();

        adapter = new ArrayAdapter<Serie>(this, android.R.layout.simple_list_item_1, myApp.getSerien());
        lvSerien = (ListView) findViewById(R.id.lvSerien);
        lvSerien.setAdapter(adapter);

        final Activity me = this;

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
    }

    public void onExit(View view) {
        finish();
    }

    public void onNewSeries(View view) {
        int currentIndex = -1;
        Intent intent = new Intent(this, EditSerieActivity.class);
        intent.putExtra(EXTRA_MESSAGE, currentIndex);
        startActivityForResult(intent, REQUEST_EDIT_SERIE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_EDIT_SERIE) {
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
