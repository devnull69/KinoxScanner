package org.theiner.kinoxscanner.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

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
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShowHosterVideosActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "KinoxScannerFile";

    private KinoxElementHoster currentHoster;

    private BaseAdapter adapter = null;
    private ListView lvVideoUrls = null;

    private boolean isWifiOnly;

    private CollectVideoLinksTask myTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_hoster_videos);

        Intent intent = getIntent();
        currentHoster = (KinoxElementHoster) intent.getSerializableExtra(UpdateKinoxElementActivity.EXTRA_MESSAGE);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isWifiOnly = settings.getBoolean("wifionly", true);

        final ProgressBar pbProgress = (ProgressBar) findViewById(R.id.pbProgressBar);

        TextView txtName = (TextView) findViewById(R.id.txtName);
        txtName.setText(currentHoster.getFoundElement().toString());

        TextView txtDelayInSec = (TextView) findViewById(R.id.txtDelayInSec);

        int delayInSec = currentHoster.getHosterMirror().getStrategie().delayInSec;

        if(delayInSec > 0) {
            txtDelayInSec.setText(delayInSec + " Sekunden Wartezeit pro Server");
        } else {
            txtDelayInSec.setVisibility(View.GONE);
        }

        // Video-Urls in Listview anzeigen
        if(currentHoster.getVideoLinks() == null)
            currentHoster.setVideoLinks(new ArrayList<VideoLink>());
        adapter = new VideoLinkAdapter(this, currentHoster.getVideoLinks(), isWifiOnly);
        lvVideoUrls = (ListView) findViewById(R.id.lvVideoUrls);
        lvVideoUrls.setAdapter(adapter);

        final Activity me = this;

        // Bei Click => Video abspielen
        lvVideoUrls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
                if(!isWifiOnly || KinoxHelper.isConnectedViaWifi(me)) {
                    VideoLink selected = (VideoLink) listview.getItemAtPosition(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(selected.getVideoURL()), "video/mp4");
                    startActivity(intent);
                } else {
                    Toast.makeText(me, "Sie sind nicht mit W-Lan verbunden!", Toast.LENGTH_SHORT).show();
                }
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
