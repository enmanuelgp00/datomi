package burakkuneko.datomi;

import burakkuneko.datomi.mobiledata.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.BroadcastReceiver;

import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;

import android.app.Activity;
import android.app.PendingIntent;

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;


public class ActivityMain extends Activity {
   
   MobileData mobileData;
   MobileDataManager mobileDataManager;
   TextView textViewOutput, textViewInfo;
   Button buttonCheck;
   List <String> responseHistory;
   Handler handler = new Handler(Looper.getMainLooper());
   
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);      
      setContentView(R.layout.activity_main);      
      handlePermissions();

      textViewOutput = findViewById(R.id.text_view);
      textViewInfo = findViewById(R.id.textViewInfo);
      buttonCheck = findViewById(R.id.buttonCheck);
      

      mobileDataManager = new MobileDataManager(this);
      buttonCheck.setOnClickListener(checkData());
   }
   
   @Override
   public void onResume() {
      super.onResume();
      mobileDataManager = new MobileDataManager(this);
      display();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }
   @Override
   public boolean onOptionsItemSelected(MenuItem menuItem) {
      switch(menuItem.getItemId()) {
         case R.id.menu_options:
            startActivity(new Intent(ActivityMain.this, ActivityOptions.class));
            break;
         case R.id.menu_history:
            startActivity(new Intent(getApplicationContext(), ActivityHistory.class));
            break;
         case R.id.menu_clear_today_data:
            mobileDataManager.clearTodayData();
            display();
            Toast.makeText(getApplicationContext(), "Today recorded data cleaned",Toast.LENGTH_SHORT).show();
            break;
      }
      return super.onOptionsItemSelected(menuItem);
   }
   public View.OnClickListener checkData() {
      return new View.OnClickListener() {
         @Override
         public void onClick(View view) {     
            // mobileDataManager = new MobileDataManager(getApplicationContext());       
            buttonCheck.setEnabled(false);
            buttonCheck.setText("Loading");
            mobileDataManager.checkMobileData( new MobileDataManager.OnReceiveMobileData() {
               @Override
               public void onReceive(MobileData mobileData, String source) {
                  Toast.makeText(ActivityMain.this, source, Toast.LENGTH_LONG).show();
                  display();
                  buttonCheck.setEnabled(true);
                  buttonCheck.setText("Check");
               }
            });
         }
      };
   } // end of checkData()

   public void handlePermissions() {
      if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
         requestPermissions(new String[] {Manifest.permission.CALL_PHONE}, 1);
      };
   }
   
   void display () {
      if (mobileDataManager.getLogOfToday().size() > 0) {
         String log = "";
         SimpleDateFormat simpleDateFormater = new SimpleDateFormat("EEEE dd MMMM yyyy");
         DataFormat dataFormater = mobileDataManager.currentDataFormat();
         responseHistory = new ArrayList <String> (mobileDataManager.getLogOfToday());
         Collections.reverse(responseHistory);
         for (String format : responseHistory) {
            mobileData = MobileData.parseStringFormat(format, dataFormater);
            log = log + mobileData.asString();
         }
         long suggestion = mobileDataManager.todaySuggestionTillDeadline();
         long used = mobileDataManager.todayDataBytesUsed();
         int percent = (int) Math.floor((double) used / suggestion * 100);
         textViewInfo.setText(String.format("Deadline : %s\nRemaining: %d days\nToday suggestion :%10s 100\nToday you've used:%10s %3d ",
            simpleDateFormater.format(mobileDataManager.getDeadline().getTime()),
            mobileDataManager.getDaysTillDeadline(),
            dataFormater.format(suggestion),
            dataFormater.format(used),
            percent
            )
         );
         textViewOutput.setText(log);
      } else {
         textViewOutput.setText("No data recorded yet");
         textViewInfo.setText("");
      }

   }
}
