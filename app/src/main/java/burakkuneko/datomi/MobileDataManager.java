package burakkuneko.datomi;

import java.util.Date;
import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.app.Service;
import android.telephony.TelephonyManager;
import android.os.Looper;
import android.os.Handler;
import android.widget.Toast;

import java.util.Calendar;

public class MobileDataManager {


    TelephonyManager telephonyManager;
    Context context;
    SharedPreferences book;
    SharedPreferences.Editor pen;
    Handler mainLooper = new Handler(Looper.getMainLooper());
    MobileData currentMobileData; 

    public MobileDataManager (Context context) {
        this.context = context;
        if (context instanceof Activity ) {
            book = ((Activity)context).getSharedPreferences("book", Context.MODE_PRIVATE);
        } else {
            book = ((Service )context).getSharedPreferences("book", Context.MODE_PRIVATE);
        }
        pen = book.edit();
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        currentMobileData = getSavedMobileData();
    }
    public interface ResponseCallback {
        public void onReceive(MobileData mobileData, String res);
    }
    
    public void check(MobileDataManager.ResponseCallback callBack) {
        telephonyManager.sendUssdRequest("*222#", new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence res) {
                MobileData newMobileData = new MobileData(res.toString());
                if (newMobileData.after(currentMobileData) && newMobileData.getSmsBonus() > currentMobileData.getSmsBonus()) {                    
                    Toast.makeText(context, "It seems there is a new mobile package bought", Toast.LENGTH_SHORT).show();                                 
                } else {
                    newMobileData.setDeadline(currentMobileData.getDeadline());
                }
                currentMobileData = newMobileData;
                save(currentMobileData);
                callBack.onReceive(currentMobileData, res.toString());
            }
        }, mainLooper);
    }
    private void save(MobileData mobileData) {        
        pen.putInt("SMS", mobileData.getSmsBonus());
        pen.putString("voiceBonus", mobileData.getVoiceBonus());
        pen.putFloat("megabytes", (float) mobileData.getMegabytes());
        pen.putLong("date", mobileData.getDate().getTimeInMillis());
        pen.putLong("deadline", mobileData.getDeadline().getTimeInMillis());
        pen.apply();
    }
    public MobileData getSavedMobileData() {
        MobileData res = new MobileData();
        res.setSmsBonus(book.getInt("SMS", 0));
        res.setVoiceBonus(book.getString("voiceBonus", "00:00:00"));
        res.setMegabytes((double) book.getFloat("megabytes", 0));

        
        Calendar calendar0 = Calendar.getInstance();
        calendar0.setTimeInMillis(book.getLong("date", System.currentTimeMillis()));
        res.setDate(calendar0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(book.getLong("deadline", 0));
        res.setDeadline(calendar);
        return res;
    }
    void changeDeadline(Calendar calendar) {
        pen.putLong("deadline", calendar.getTimeInMillis());
        pen.apply();
    }


}