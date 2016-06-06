package org.theiner.kinoxscanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.theiner.kinoxscanner.R;

public class OptionsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "KinoxScannerFile";

    private Switch swWifiOnly;

    private Button btnBatteryOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swWifiOnly = (Switch) findViewById(R.id.swWifiOnly);
        btnBatteryOpt = (Button) findViewById(R.id.btnBatteryOpt);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final Activity me = this;

        boolean isWifiOnly = settings.getBoolean("wifionly", true);

        swWifiOnly.setChecked(isWifiOnly);

        swWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("wifionly", isChecked);
                editor.commit();

                if(!isChecked) {
                    Toast.makeText(me, R.string.QuotaWarning, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Button nur für Android API >= 23 anzeigen
        if(Build.VERSION.SDK_INT < 23) {
            btnBatteryOpt.setVisibility(View.GONE);
        } else {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName))
                btnBatteryOpt.setVisibility(View.GONE);
        }
    }

    public void onBack(View view) {
        finish();
    }

    public void onBatteryOpt(View view) {
        // Dialog zur Auswahl der Ausnahme für Akku-Optimierung anzeigen
        if(Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);

            btnBatteryOpt.setVisibility(View.GONE);
        }
    }
}
