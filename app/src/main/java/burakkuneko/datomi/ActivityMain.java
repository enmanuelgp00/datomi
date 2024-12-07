package burakkuneko.datomi;

import burakkuneko.datomi.mobileData.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.Intent;
// import android.content.Context;
import android.content.BroadcastReceiver;

import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;

import android.app.Activity;
// import android.app.AlarmManager;
import android.app.PendingIntent;

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;


public class ActivityMain extends Activity {
   
   MobileData mobileData;
   MobileDataManager mobileDataManager;
   TextView textViewOutput, textViewDeadline;
   Button buttonCheck;
   List <String> responseHistory;
   Handler handler = new Handler(Looper.getMainLooper());
   
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);      
      setContentView(R.layout.activity_main);      
      handlePermissions();

      mobileDataManager = new MobileDataManager(ActivityMain.this);
      textViewOutput = findViewById(R.id.text_view);
      textViewDeadline = findViewById(R.id.textViewDeadline);
      buttonCheck = findViewById(R.id.buttonCheck);

      buttonCheck.setOnClickListener(checkData());
      textViewDeadline.setOnClickListener(changeDeadline());
      
      
      // displayFrame();
   }
   
   @Override
   public void onResume() {
      super.onResume();
      display();
   }
   
   public View.OnClickListener checkData() {
      return new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            mobileDataManager.checkMobileData( new MobileDataManager.OnReceiveMobileData() {
               @Override
               public void onReceive(MobileData mobileData, String source) {
                  Toast.makeText(ActivityMain.this, source, Toast.LENGTH_LONG).show();
                  display();
               }
            });
         }
      };
   } // end of checkData()

   public View.OnClickListener changeDeadline () {
      return new View.OnClickListener () {
         @Override
         public void onClick(View view) {
            startActivity(new Intent(ActivityMain.this, ActivityOptions.class));
         }
      };
   }

   public void handlePermissions() {
      if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
         requestPermissions(new String[] {Manifest.permission.CALL_PHONE}, 1);
      };
   }
   

   public void displayFrame() {
      handler.postDelayed( new Runnable() {
         @Override
         public void run () {
            display();
            displayFrame();
         }
      }, 5000);
   }

   void display () {
      String log = "";
      SimpleDateFormat dateFormater = new SimpleDateFormat("EEEE dd MMMM yyyy");
      responseHistory = new ArrayList <String> (mobileDataManager.getLogOfToday());
      Collections.reverse(responseHistory);
      for (String format : responseHistory) {
         mobileData = MobileData.parseStringFormat(format, mobileDataManager.getDataFormat());
         log = log + mobileData.asString();
         // log = log + "\n" + format;
      }   
      textViewDeadline.setText(String.format("Deadline : %s\nRemaining: %d days",
         dateFormater.format(mobileDataManager.getDeadline().getTime()),
         mobileDataManager.getDaysToDeadline()
         )
      );
      textViewOutput.setText(log);
   }

      

   // private void alarm() {
   //    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
   //    Intent intentToAlarm = new Intent(this, MyAlarmReceiver.class);
   //    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentToAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
   //    Calendar calendar = Calendar.getInstance();
   //    calendar.add(Calendar.MINUTE, 1);
   //    SimpleDateFormat dateFormater = new SimpleDateFormat("hh:mm:ss");

   //    if (alarmManager != null) {
   //       alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
   //       Toast.makeText(this, "Alarm set to: " + dateFormater.format(calendar.getTime()), Toast.LENGTH_LONG ).show();
   //    } else {
   //       Toast.makeText(this, "We could create a Alarm Manager", Toast.LENGTH_LONG ).show();
   //    }
   // }

}
