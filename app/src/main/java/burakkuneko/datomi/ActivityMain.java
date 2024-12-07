package burakkuneko.datomi;

import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import android.net.ConnectivityManager;

import java.util.Calendar;


public class ActivityMain extends Activity {
    TextView textView;
    Button button0, button1;
    LinearLayout mainView;
    MobileData mobileData, oldMobileData;
    MobileDataManager mobileDataManager;
    NetworkStatsManager networkStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);

        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CALL_PHONE}, 1);
        }

        mobileDataManager = new MobileDataManager(this);
        mobileData = mobileDataManager.getSavedMobileData();
        oldMobileData = mobileDataManager.getSavedMobileData();
        mainView = findViewById(R.id.main);

        textView = findViewById(R.id.text_view);        
        load();    
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityMain.this, ActivityOptions.class));
            }
        });
        button0 = findViewById(R.id.button0);
        button0.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkMobileData();
                button0.setEnabled(false);
            }
        });
        Button button2 = new Button(this);
        mainView.addView(button2);
        button2.setText("traffic");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityMain.this, ActivitySecond.class));
            }
        });
    }

    void checkMobileData() {
        mobileDataManager.check( new MobileDataManager.ResponseCallback() {
            @Override
            public void onReceive(MobileData newMobileData, String res) {
                mobileData = newMobileData;
                Toast.makeText(ActivityMain.this, res, Toast.LENGTH_LONG).show();
                load();
                button0.setEnabled(true);
            }
        });
    }

    void load() {
        textView.setText(String.format("Data: %,.2f MB\nGuess: %,.2f\nVoice: %s\nSMS: %d\n\nRemaining: %d %s\nDate: %s\nDeadline: %s", 
        
                mobileData.getMegabytes(),
                getGuessedData(),
                mobileData.getVoiceBonus(),
                mobileData.getSmsBonus(),
                mobileData.getRemainingDays(),
                (mobileData.getRemainingDays() > 1) ? "days" : "day",
                mobileData.getDate().getTime().toString(),
                mobileData.getDeadline().getTime().toString()
            ));
    }

    double getGuessedData() {
        if (hasChangedMobileData()) {
            oldMobileData = mobileData;
        }
        try {   
            NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, oldMobileData.getDate().getTimeInMillis(), System.currentTimeMillis());
            long total = bucket.getRxBytes() + bucket.getTxBytes();
            double dataMg = (double) (total) / Math.pow(1000, 2);
            return mobileData.getMegabytes() - dataMg;
        } catch (Exception e) {

        }
        return -1;
    }

    boolean hasChangedMobileData() {
        return oldMobileData.getMegabytes() != mobileData.getMegabytes();
    }
}