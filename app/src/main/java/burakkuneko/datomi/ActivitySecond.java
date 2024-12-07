package burakkuneko.datomi;

import android.provider.Settings;
import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class ActivitySecond extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int REQUEST_CODE_READ_PHONE_STATE = 1;
    private TextView dataUsageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataUsageTextView = findViewById(R.id.text_view);

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE);
        } else if (!isUsageAccessGranted()) {
            promptUsageAccessPermission();
        } else {
            displayDataUsage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUsageAccessGranted() && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            displayDataUsage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isUsageAccessGranted()) {
                    displayDataUsage();
                } else {
                    promptUsageAccessPermission();
                }
            } else {
                Toast.makeText(this, "Permission denied to read phone state", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void promptUsageAccessPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private boolean isUsageAccessGranted() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void displayDataUsage() {
        try {
            long end = System.currentTimeMillis();
            long start = end - 1000 * 60;

            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, start, end);
            long dataUsage = bucket.getRxBytes() + bucket.getTxBytes();
            dataUsageTextView.setText(String.format("Mobile Data Usage: %,.2f mg", dataUsage / Math.pow(1000, 2)));
        } catch (RemoteException e) {
            e.printStackTrace();
            dataUsageTextView.setText("Failed to retrieve data usage.");
        }
    }
}
