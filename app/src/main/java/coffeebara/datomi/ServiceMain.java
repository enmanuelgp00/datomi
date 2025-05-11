package coffeebara.datomi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ServiceMain extends Service {
    @Override
    public void onCreate() {
        Log.d(TAG, "Service create");
    }
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }
    public void onDestroy() {
        Log.d(TAG, "Service Destroyed");
    }
    public IBinder onBind(Intent intent) {
        return null;
    }
}