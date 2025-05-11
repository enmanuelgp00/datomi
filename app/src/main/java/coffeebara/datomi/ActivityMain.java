package coffeebara.datomi;

import coffeebara.datomi.mobiledata.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;


public class ActivityMain extends Activity {
   
   MobileDataManager mobileDataManager;
   TextView textViewOutput, textViewInfo;
   Button buttonCheck;
   Handler handler = new Handler(Looper.getMainLooper());
   
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);      
      setContentView(R.layout.activity_main);      
      handlePermissions();

      textViewOutput = findViewById(R.id.tv_log);
      textViewInfo = findViewById(R.id.tv_info);
      buttonCheck = findViewById(R.id.btn_check);
      

      mobileDataManager = new MobileDataManager(this);
      buttonCheck.setOnClickListener(checkData());

      textViewOutput.setOnClickListener( new View.OnClickListener(){
         @Override
         public void onClick(View view) {

            Context context = getApplicationContext();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name = "MyChannel";
            String description = "Channel for My App";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("my_channel_id", name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(context, "my_channel_id");
            builder.setSmallIcon(R.drawable.small_icon)
               .setContentTitle("My Notification Title")
               .setContentText("This is a notification message");
            notificationManager.notify(1, builder.build());
         }
      });
      
      
   } // end of onCreate();
   
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
	final int BAR_SIZE = 32 ;
	if (mobileDataManager.getLogOfToday().size() > 0) {
		StringBuilder log = new StringBuilder();
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("EEEE dd MMMM");
		DataFormat dataFormatter = mobileDataManager.currentDataFormat();
		ArrayList<String> logGlobal = new ArrayList<String> (mobileDataManager.getLogGlobal());
		Collections.reverse(logGlobal);
			
         for (String format : logGlobal) {
			log.append(MobileData.parseStringFormat(format, dataFormatter).asString());
         }

		long suggestion = mobileDataManager.todaySuggestionTillDeadline();
		long used = mobileDataManager.todayDataBytesUsed();
		int barProgress = (int) ( (double) used / suggestion * BAR_SIZE );
		
		textViewInfo.setText(String.format("[ %d day(s) ] - %s - \n\n  %-" + ( BAR_SIZE / 2 ) + "s%" + ( BAR_SIZE / 2 ) + "s \n%s",
			mobileDataManager.getDaysTillDeadline(),
			simpleDateFormatter.format(mobileDataManager.getDeadline().getTime()),
			dataFormatter.format( used ),
			dataFormatter.format( suggestion ),
            retroBar( BAR_SIZE , barProgress ))
         );
		textViewOutput.setText(log.toString());
      } else {
		textViewOutput.setText("No data recorded yet");
		textViewInfo.setText("");
      }
   }

	String retroBar ( int length, int progress ) {
		StringBuilder bar = new StringBuilder();
		
		String errorMessage = " ( Overpassed ) ";
		char progressChar = '/' ;
		char freeSpaceChar = '.' ;
		int freeSpace = length - progress ;
		bar.append("[ ");

		if ( progress > length ) {		
			int spaceAside = ( length - errorMessage.length() ) / 2 ;
			StringBuilder aside = new StringBuilder();
			for (int i = 0; i < spaceAside; i++ ) {
				aside.append( progressChar );
			}
			bar.append( aside.toString() );
			bar.append( errorMessage );
			bar.append( aside.toString() );
		} else {
			for ( int i = 0; i < progress; i++) {
				bar.append( progressChar );
			}
			for ( int i = 0; i < freeSpace ; i++) {
				bar.append( freeSpaceChar );
			}
		}
		bar.append(" ]");
		return bar.toString();
	}
}
