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
	TextView textViewOutput,
		textViewDeadline,
		textViewDays,
		textViewBar,
		textViewData;
	Button buttonCheck;
	Handler handler = new Handler(Looper.getMainLooper());
	
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);		
		setContentView(R.layout.activity_main);	


		handlePermissions();
		textViewOutput = findViewById(R.id.tv_log);
		textViewDays = findViewById(R.id.tv_days);
		textViewData = findViewById(R.id.tv_data);
		textViewDeadline = findViewById(R.id.tv_deadline);
		textViewBar = findViewById(R.id.tv_bar);
		buttonCheck = findViewById(R.id.btn_check);

		mobileDataManager = new MobileDataManager(this);
		buttonCheck.setOnClickListener(checkData());

		textViewOutput.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick(View view) {

				Context context = getApplicationContext();
				NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
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

	}
	
	@Override
	public void onResume() {
		super.onResume();
		//mobileDataManager = new MobileDataManager(this);
		//displayRefresh();
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
				String loadingMessage = "...";
				// mobileDataManager = new MobileDataManager(getApplicationContext());
				CharSequence buttonCheckText = buttonCheck.getText();
				buttonCheck.setEnabled(false);
				buttonCheck.setText( loadingMessage );
				mobileDataManager.checkMobileData( new MobileDataManager.OnReceiveMobileData() {
					@Override
					public void onReceive(MobileData mobileData, String source) {
						Toast.makeText(ActivityMain.this, source, Toast.LENGTH_LONG).show();
						display();
						buttonCheck.setEnabled(true);
						buttonCheck.setText(buttonCheckText);
					}
					@Override
					public void onReceiveFailed( int failCode ) {
						Toast.makeText(ActivityMain.this, "USSD receive failed: code : " + failCode , Toast.LENGTH_LONG).show();
						buttonCheck.setEnabled(true);
						buttonCheck.setText(buttonCheckText);
					}
				});
			}
		};
	}

	public void handlePermissions() {
		if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] {Manifest.permission.CALL_PHONE}, 1);
		};
	}
	void displayRefresh() {
	Thread loop	= new Thread ( new Runnable() {
			@Override
			public void run () {
				display();
				try {
					Thread.sleep( 5000 );
				} catch ( Exception e ) {

				}
				displayRefresh();
			}
		});
		//loop.start();
		//handler.post( loop ); //is need if you update UI elements from onCreate();
		//runOnUiThread( loop );
	}

	void display () {
		final int BAR_SIZE = 30 ;
		if (mobileDataManager.getLogOfToday().size() > 0) {
			StringBuilder log = new StringBuilder();
			SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd MMMM");
			//SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("EEEE dd MMMM YYYY");
			DataFormat dataFormatter = mobileDataManager.currentDataFormat();
			ArrayList<String> logGlobal = new ArrayList<String> (mobileDataManager.getLogGlobal());
			Collections.reverse(logGlobal);
			
			for (String format : logGlobal) {
				log.append(MobileData.parseStringFormat(format, dataFormatter).asString());
			}

			long suggestion = mobileDataManager.getTodaySuggestionTillDeadline();
			long used = mobileDataManager.getTodayDataBytesUsed();
			int barProgress = (int) ( (double) used / suggestion * BAR_SIZE );
			int remainingDays = mobileDataManager.getDaysTillDeadline();

			textViewDays.setText(String.format("%d", remainingDays));
			textViewData.setText( String.format( "%s > %s",
				dataFormatter.format( mobileDataManager.getTodayLargerMobileData().getDataBytes()),
				dataFormatter.format( mobileDataManager.getCurrentMobileData().getDataBytes())
			));
			textViewDeadline.setText(String.format("  %30s\n%s", 
				simpleDateFormatter.format(mobileDataManager.getDeadline().getTime()),
				retroBar( 30, 30 - remainingDays )
			));

			textViewBar.setText(String.format("  %-" + ( BAR_SIZE / 2 ) + "s%" + ( BAR_SIZE / 2 ) + "s \n%s",
				dataFormatter.format( used ),
				dataFormatter.format( suggestion ),
				retroBar( BAR_SIZE , barProgress )
			));

			textViewOutput.setText(log.toString());	
		} else {
			textViewOutput.setText("No data recorded yet");
			textViewDays.setText("?");
			textViewDeadline.setText("");
			textViewBar.setText("");
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
			progressChar = ':';
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
