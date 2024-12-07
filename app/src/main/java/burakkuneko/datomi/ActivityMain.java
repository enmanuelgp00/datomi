package burakkuneko.datomi;

import android.app.Activity;
import android.os.Looper;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.net.TrafficStats;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class ActivityMain extends Activity {
    TextView textView;
    Button button;
    Handler handler = new Handler();
    Handler dealer  = new Handler(Looper.getMainLooper());
    SharedPreferences book;
    SharedPreferences.Editor pen;
    TelephonyManager telephonyManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.text_view);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        book = getSharedPreferences("book", Context.MODE_PRIVATE);
        pen = book.edit();
        render();
        handler.post(monitorTraffic());
        button.setOnClickListener(showTraffic());
    }

    View.OnClickListener showTraffic() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {                
                checkMobileData();
            }
        };
    }
    Runnable monitorTraffic() {
        return new Runnable() {
            @Override
            public void run (){
                render();
                handler.postDelayed(this, 1000);
            }
        };
    }
    void render() {        
        textView.setText(String.format("%,.2f mg\n%,.2f mg\n%,.2f mg\n%,d b ", 
                getMobileData(),
                bytesToMG(getGuessedMobileDataBytes()),
                bytesToMG(getRecordedTraffic()),
                getTrafficCurrentBytes()
            )); 
    }
    double bytesToMG(double bytes) {
        return bytes / Math.pow(1024, 2);
    }
    double bytesToMG(long bytes) {
        return bytes / Math.pow(1024, 2);
    }
    double MGToBytes(double mg) {
        return mg * Math.pow(1024, 2);
    }
    double getGuessedMobileDataBytes () {
        setRecordedTraffic(getRecordedTraffic() + getTrafficCurrentBytes());
        return MGToBytes(getMobileData()) - getRecordedTraffic();
    }
    void setRecordedTraffic(long traffic) {
        pen.putString("recorded_traffic", String.valueOf(traffic));
        pen.apply();
    }
    long getRecordedTraffic() {
        return Long.parseLong(book.getString("recorded_traffic", "0"));
    }
    long getTrafficTotal() {
        return TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes();
    }

    int getTrafficCurrentBytes() {
        long oldrecord = Long.parseLong(book.getString("oldrecord", "0"));        
        long newrecord = getTrafficTotal();
        int diff = (int) (newrecord -  oldrecord);
        pen.putString("oldrecord", String.valueOf(newrecord));
        pen.apply();
        if (oldrecord == 0) {
            return getTrafficCurrentBytes();
        } else {
            return diff;
        }
    }
    void checkMobileData() {
        telephonyManager.sendUssdRequest( "*222*328#", new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                double megabytes = UssdInterpeter.getMegabytes(response.toString());
                if (getMobileData() != megabytes) {                    
                    pen.putString("mobiledata", String.valueOf(megabytes));
                    pen.apply();
                    setRecordedTraffic(0);
                }
                render();
                Toast.makeText(ActivityMain.this, response, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int code) {
                Toast.makeText(ActivityMain.this, "failed", Toast.LENGTH_LONG).show();
            }
        }, dealer);
    }
    double getMobileData() {
        return Double.parseDouble(book.getString("mobiledata", "0"));
    }
}