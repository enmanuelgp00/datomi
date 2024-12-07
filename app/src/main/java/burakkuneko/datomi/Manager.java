package burakkuneko.datomi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.os.Handler;
import android.content.Context;
import android.app.Activity;
import android.app.Service;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import android.widget.Toast;

public class Manager {
    private String DATA;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor; 
    Context context;
    TelephonyManager telephonyManager;
    Handler handler = new Handler(Looper.getMainLooper());
    public Manager(Activity context, String data) {
        this.context = context;
		this.DATA = data;
        sharedPreferences = context.getSharedPreferences(DATA, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }
    public Manager(Service context, String data) {
        this.context = context;  
		this.DATA = data;
        sharedPreferences = context.getSharedPreferences(DATA, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }
    public abstract interface OnMobileDataReceive {
        public void onReceive(String res);
    }
    public void checkMobileData(Manager.OnMobileDataReceive onMobileDataReceive) {

        telephonyManager.sendUssdRequest("*222*328#", new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                onMobileDataReceive.onReceive(response.toString());
            }
            @Override 
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int errorCode) {
                Toast.makeText(context, String.format("Code: %d. Error getting data."), Toast.LENGTH_LONG).show();
            }
        }, handler);
    }
    MobileData getSavedData() {
        return new MobileData(sharedPreferences.getString(DATA, null));
    }
    void saveData(String data) {
        editor.putString(DATA, data);
        editor.apply();
    }

    private boolean handlePermissions(){
        Activity ac = (Activity) context;
        if (ac.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ac.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
            return true;
        } else {
            return false;
        }
    }
}