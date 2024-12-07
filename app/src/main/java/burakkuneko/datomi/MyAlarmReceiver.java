package burakkuneko.datomi;

import burakkuneko.datomi.mobileData.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.Activity;

import android.widget.Toast;

public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MobileDataManager manager = new MobileDataManager(context);
        manager.checkMobileData( new MobileDataManager.OnReceiveMobileData() {
            @Override
            public void onReceive(MobileData mobileData, String source) {

            }
        });
        Toast.makeText(context, "Hello from MyAlarmReceiver", Toast.LENGTH_LONG).show();
        if (context instanceof ActivityMain) {
            ((ActivityMain)context).display();
        }
    }
}