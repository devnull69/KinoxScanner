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
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.util.KinoxHelper;

public class OptionsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "KinoxScannerFile";

    public SharedPreferences settings;
    private Switch swWifiOnly;

    private Button btnBatteryOpt;
    private RadioGroup radioGrp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swWifiOnly = (Switch) findViewById(R.id.swWifiOnly);
        btnBatteryOpt = (Button) findViewById(R.id.btnBatteryOpt);
        radioGrp = (RadioGroup) findViewById(R.id.radiogrp);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final Activity me = this;

        boolean isWifiOnly = settings.getBoolean("wifionly", true);
        String kinoxUrl = settings.getString("kinoxurl", "http://www.kinox.to/");

        radioGrp.check(getIdFromString(kinoxUrl));

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

    private int getIdFromString(String url) {
        int result;
        String part = url.substring(15, 19);
        switch(part) {
            case "s.to":
                result = R.id.radio_kinosto;
                break;
            case "x.tv":
                result = R.id.radio_tv;
                break;
            case "x.ag":
                result = R.id.radio_ag;
                break;
            case "x.me":
                result = R.id.radio_me;
                break;
            case "x.am":
                result = R.id.radio_am;
                break;
            case "x.nu":
                result = R.id.radio_nu;
                break;
            case "x.pe":
                result = R.id.radio_pe;
                break;
            case "x.sg":
                result = R.id.radio_sg;
                break;
            default:
                result = R.id.radio_to;
        }
        return result;
    }

    private String getStringFromId(int id) {
        String result = "";
        switch(id) {
            case R.id.radio_to:
                result = "x.to";
                break;
            case R.id.radio_kinosto:
                result = "s.to";
                break;
            case R.id.radio_tv:
                result = "x.tv";
                break;
            case R.id.radio_ag:
                result = "x.ag";
                break;
            case R.id.radio_me:
                result = "x.me";
                break;
            case R.id.radio_am:
                result = "x.am";
                break;
            case R.id.radio_nu:
                result = "x.nu";
                break;
            case R.id.radio_pe:
                result = "x.pe";
                break;
            case R.id.radio_sg:
                result = "x.sg";
                break;
            default:
                result = "x.to";
        }
        return "http://www.kino" + result + "/";
    }

    public void onRadioButtonClicked(View view) {
        int selectedId = radioGrp.getCheckedRadioButtonId();

        String kinoxurl = getStringFromId(selectedId);
        KinoxHelper.kinoxURL = kinoxurl;

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("kinoxurl", kinoxurl);
        editor.commit();
    }
}
