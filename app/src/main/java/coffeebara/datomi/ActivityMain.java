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

import android.graphics.Color;
import android.graphics.Typeface;

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.LinearLayout;

import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;

import android.util.TypedValue;
import android.net.TrafficStats; 

import java.util.Calendar;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;


public class ActivityMain extends Activity {
	
	MobileDataManager mobileDataManager;
	TextView textViewDeadline,
		textViewDays,
		textViewDinamicSuggestion,
		textViewTotalData,
		textViewDaysLabel,
		tvDataSent,
		tvDataReceived;
		
	TextView[] tv_collection;
	
	Button btnCheck;
	Handler handler = new Handler(Looper.getMainLooper());
	
	long prevTotalDataSent = 0;
	long prevTotalDataReceived = -1;
	
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);		
		setContentView(R.layout.activity_main);
		mobileDataManager = new MobileDataManager(this);

		handlePermissions();
		tv_collection = new TextView [] {
			textViewDays = findViewById(R.id.tv_days), 
			textViewDaysLabel = findViewById(R.id.tv_days_label),
			textViewDeadline = findViewById(R.id.tv_deadline),
			textViewTotalData = findViewById(R.id.tv_total_data),
			textViewDinamicSuggestion = findViewById( R.id.tv_dinamic_suggestion ),
			tvDataSent = findViewById( R.id.tv_data_sent ),
			tvDataReceived = findViewById( R.id.tv_data_received )
		};
		
		/*
		//new TextView( this );
		textViewDinamicSuggestion.setLayoutParams( new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT )
		);
		textViewDinamicSuggestion.setTypeface( Typeface.MONOSPACE ); // fontFamily
		textViewDinamicSuggestion.setTextColor( Color.parseColor("#ffffff") );
		textViewDinamicSuggestion.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 15);
		((LinearLayout) textViewTotalData.getParent()) .addView( textViewDinamicSuggestion );
		*/

		btnCheck = findViewById(R.id.btn_check);
		btnCheck.setOnClickListener( checkData() );

		/*
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
		*/
		
		refresh();
	}
	void refresh() {
		
		Runnable displaying = new Runnable() {
			@Override
			public void run () {
				display();
				refresh();
			}
		};		
		handler.postDelayed( displaying, 1000 );
	}

	public void message( String text ) {
		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onResume() {
		super.onResume();
		mobileDataManager.update();
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
				final String loadingMessage = " ... ";
				mobileDataManager.update();
				CharSequence btnCheckText = btnCheck.getText();
				btnCheck.setEnabled(false);
				btnCheck.setText( loadingMessage );
				mobileDataManager.checkMobileData( new MobileDataManager.OnReceiveMobileData() {
					@Override
					public void onReceive(MobileData mobileData, String source) {
						Toast.makeText(ActivityMain.this, source, Toast.LENGTH_LONG).show();
						display();
						btnCheck.setEnabled(true);
						btnCheck.setText( btnCheckText );
					}
					@Override
					public void onReceiveFailed( int failCode ) {
						Toast.makeText(ActivityMain.this, "USSD receive failed: code : " + failCode , Toast.LENGTH_LONG).show();
						btnCheck.setEnabled(true);
						btnCheck.setText( btnCheckText );
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

	void display () {
		final int BAR_SIZE = 30 ;
		if (mobileDataManager.getLogOfToday().size() > 0) {
			
			StringBuilder log = new StringBuilder();
			SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd MMMM");
			//SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("EEEE dd MMMM YYYY");
			DataFormat dataFormatter = mobileDataManager.currentDataFormat();
			/*
			ArrayList<String> logGlobal = new ArrayList<String> (mobileDataManager.getLogGlobal());
			Collections.reverse(logGlobal);
			
			for (String format : logGlobal) {
				log.append(MobileData.parseStringFormat(format, dataFormatter).asString());
			}
			*/

			long initialSuggestion = mobileDataManager.getTodaySuggestionTillDeadline();
			long totalUsed = mobileDataManager.getTodayDataBytesUsed();
			long remainingBytes =  mobileDataManager.getTodayCurrentMobileData().getDataBytes() - mobileDataManager.getDataBytesOffset();
			long initialBytes = mobileDataManager.getTodayLargerMobileData().getDataBytes();

			int barProgress = (int) ( (double) totalUsed / initialSuggestion * BAR_SIZE );
			int remainingDays = mobileDataManager.getDaysTillDeadline();


			textViewDays.setText(String.format("%d", remainingDays));
			textViewDaysLabel.setText( remainingDays > 1 ? "days" : "day" );

			int progressDays = BAR_SIZE - remainingDays;

			textViewDeadline.setText(String.format("  %" + BAR_SIZE + "s\n  %" + ( progressDays ) + "d\n%s", 
				simpleDateFormatter.format( mobileDataManager.getDeadline().getTime() ),
				remainingDays,
				retroBar( BAR_SIZE, BAR_SIZE - remainingDays, '.', '#')
			));
			
			
			int progressTillCero = BAR_SIZE - (int)( (float) remainingBytes / initialBytes * BAR_SIZE );
			String initialBytesAndTotalUsed = String.format( "%s (%s)" , dataFormatter.format( initialBytes ), dataFormatter.format( totalUsed ) );

			textViewTotalData.setText( String.format("  %" + ( progressTillCero + 1 ) + "s\n%s\n  %s%"+ ( BAR_SIZE - initialBytesAndTotalUsed.length() ) +"s" ,
				dataFormatter.format( remainingBytes ),
				retroBar( BAR_SIZE,  progressTillCero, '.', '#'),
				initialBytesAndTotalUsed,
				dataFormatter.format( 0 )
				)
			);

			long dinamicBytes = initialBytes;
			long dinamicUsed = totalUsed ;
			long dinamicSuggestion = initialSuggestion;
			
			while ( dinamicUsed > dinamicSuggestion ) {
				dinamicBytes -= dinamicSuggestion;
				dinamicSuggestion = ( dinamicBytes ) / remainingDays ;
				dinamicUsed = dinamicUsed - dinamicSuggestion ;
			}
			
			int dinamicProgress = ( int ) (( double ) dinamicUsed / dinamicSuggestion  * BAR_SIZE );
			textViewDinamicSuggestion.setText( String.format("  %"+ ( dinamicProgress <= BAR_SIZE ? dinamicProgress + 1 : BAR_SIZE ) +"s \n%s\n  %" + ( BAR_SIZE ) + "s",
				dataFormatter.format( dinamicUsed ),
				retroBar( BAR_SIZE , dinamicProgress, '#', '.' ),
				dataFormatter.format( dinamicSuggestion )
			));
			
			tvDataSent.setText( String.format("%10s/s", dataFormatter.format( getCurrentDataSent()) ) );
			tvDataReceived.setText( String.format("%10s/s", dataFormatter.format( getCurrentDataReceived()) ) );

		} else {
			for ( TextView tv : tv_collection ) {
				tv.setText("");
			}
			textViewTotalData.setText("Press the button [ " + btnCheck.getText() + " ] to record data");
		}
	}
	
	long getCurrentDataSent() {
		long tx = TrafficStats.getMobileTxBytes();
		long currentDataSent = 0;
		if ( prevTotalDataSent != 0 ) {
			currentDataSent = tx - prevTotalDataSent;
		}
		prevTotalDataSent = tx;
		return currentDataSent;
	}
	
	long getCurrentDataReceived() {
		long rx = TrafficStats.getMobileRxBytes();
		long currentDataReceived = 0;
		if ( prevTotalDataReceived != 0 ) {
			currentDataReceived = rx - prevTotalDataReceived;
		}
		prevTotalDataReceived= rx;
		return currentDataReceived;
	}
	
	String retroBar ( int length, int progress, char progressChar, char freeSpaceChar ) {
		StringBuilder bar = new StringBuilder();
		String errorMessage = " ( fail ) ";
		int freeSpace = length - progress ;
		bar.append("[ ");

		if ( progress >= length ) {
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
