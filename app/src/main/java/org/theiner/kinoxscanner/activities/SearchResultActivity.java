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
import android.widget.TextView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.SearchAdapter;
import org.theiner.kinoxscanner.async.SearchKinoxTask;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;

import java.io.Serializable;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    private ListView lvSuchergebnisse = null;
    private TextView txtBitteWarten = null;
    private BaseAdapter adapter = null;

    public final static String EXTRA_MESSAGE = "org.theiner.kinoxscanner.MESSAGESEARCH";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        Intent intent = getIntent();
        SearchRequest suche = (SearchRequest) intent.getSerializableExtra(EXTRA_MESSAGE);

        lvSuchergebnisse = (ListView) findViewById(R.id.lvSuchergebnisse);
        txtBitteWarten = (TextView) findViewById(R.id.txtBitteWarten);

        lvSuchergebnisse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                SearchResult selected = (SearchResult) listview.getItemAtPosition(position);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("suchErgebnis", selected);
                setResult(OverviewFragment.RESULT_UPDATE_ELEMENTS, resultIntent);
                finish();
            }
        });

        final Activity me = this;

        SearchKinoxTask.CheckCompleteListener ccl = new SearchKinoxTask.CheckCompleteListener(){
            @Override
            public void onCheckComplete(List<SearchResult> result) {
                // Falls Liste leer ist, sofort schließen
                if(result.size()>0) {
                    adapter = new SearchAdapter(me, result);
                    lvSuchergebnisse.setAdapter(adapter);
                    txtBitteWarten.setText("");
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("suchErgebnis", (Serializable) null);
                    setResult(OverviewFragment.RESULT_UPDATE_ELEMENTS, resultIntent);
                    finish();
                }
            }
        };

        SearchKinoxTask mySearchTask = new SearchKinoxTask(ccl);
        mySearchTask.execute(suche);
    }
}
