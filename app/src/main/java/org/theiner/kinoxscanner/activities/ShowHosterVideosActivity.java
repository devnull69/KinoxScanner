package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.adapter.HosterAdapter;
import org.theiner.kinoxscanner.adapter.VideoLinkAdapter;
import org.theiner.kinoxscanner.async.CollectVideoLinksTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.KinoxElementHoster;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.data.VideoLink;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShowHosterVideosActivity extends AppCompatActivity {

    private KinoxElementHoster currentHoster;

    private BaseAdapter adapter = null;
    private ListView lvVideoUrls = null;

    private CollectVideoLinksTask myTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_hoster_videos);

        Intent intent = getIntent();
        currentHoster = (KinoxElementHoster) intent.getSerializableExtra(UpdateKinoxElementActivity.EXTRA_MESSAGE);

        final ProgressBar pbProgress = (ProgressBar) findViewById(R.id.pbProgressBar);

        TextView txtName = (TextView) findViewById(R.id.txtName);
        txtName.setText(currentHoster.getFoundElement().toString());

        // Video-Urls in Listview anzeigen
        if(currentHoster.getVideoLinks() == null)
            currentHoster.setVideoLinks(new ArrayList<VideoLink>());
        adapter = new VideoLinkAdapter(this, currentHoster.getVideoLinks());
        lvVideoUrls = (ListView) findViewById(R.id.lvVideoUrls);
        lvVideoUrls.setAdapter(adapter);

        // Bei Click => Video abspielen
        lvVideoUrls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                VideoLink selected = (VideoLink) listview.getItemAtPosition(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(selected.getVideoURL()), "video/mp4");
                startActivity(intent);
            }
        });

        // Video-Links sammeln für currentErgebnis
        CollectVideoLinksTask.CheckCompleteListener ccl = new CollectVideoLinksTask.CheckCompleteListener() {
            @Override
            public void onCheckComplete(String result) {
                //((ViewManager) pbProgress.getParent()).removeView(pbProgress);
                pbProgress.setVisibility(View.GONE);
                myTask = null;
            }

            @Override
            public void onProgress(Integer progress) {
                pbProgress.setProgress(progress);
                adapter.notifyDataSetChanged();
            }
        };

        myTask = new CollectVideoLinksTask(ccl);
        myTask.execute(currentHoster);
    }

    public void onExit(View view) {
        if(myTask != null) {
            myTask.cancel(true);
            myTask = null;
        }
        finish();
    }

}
