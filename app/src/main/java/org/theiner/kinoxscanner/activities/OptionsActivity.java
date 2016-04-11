package org.theiner.kinoxscanner.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.theiner.kinoxscanner.R;

public class OptionsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "KinoxScannerFile";

    private Switch swWifiOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swWifiOnly = (Switch) findViewById(R.id.swWifiOnly);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean isWifiOnly = settings.getBoolean("wifionly", true);

        swWifiOnly.setChecked(isWifiOnly);

        swWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("wifionly", isChecked);
                editor.commit();
            }
        });
    }

}
