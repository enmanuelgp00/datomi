package coffeebara.datomi;

import coffeebara.datomi.mobiledata.MobileDataManager;
import android.os.Bundle;

import android.app.Activity;
import android.widget.TextView;

import java.util.TreeSet;

public class ActivityHistory extends Activity {
    MobileDataManager manager;
    TextView tv_history;
    TreeSet<String> logOfKeys;
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_history);
        tv_history = findViewById(R.id.tv_history);
        manager = new MobileDataManager(this);
        logOfKeys = manager.getLogOfKeys();
        String log = "";
        for (String key : logOfKeys) {
            log = log + "\n" + key;
        }
        tv_history.setText(log);
    }
}