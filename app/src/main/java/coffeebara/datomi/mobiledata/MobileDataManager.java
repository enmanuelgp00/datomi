package coffeebara.datomi.mobiledata;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.os.Looper;
import android.os.Handler;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.net.TrafficStats;
import android.util.Log;

import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.util.Calendar;

import java.text.SimpleDateFormat;

public class MobileDataManager {
	
	float debugData = 5.0f;
	float debugUseRate = 0.0001f * 5.0f;

	Handler mainLooper = new Handler(Looper.getMainLooper());
	Context context;
	SharedPreferences book;
	SharedPreferences.Editor pen;
	TelephonyManager telephonyManager;
	TreeSet<String> responseHistory;
	Calendar deadline;
	MobileData todayFirstMobileData, currentMobileData;
	TreeSet<String> logOfKeys, logOfToday;
	String keyLogOfToday;
	
	private class Key {
		final static String DEBUG_MODE = "debugMode";
		final static String DATA_BYTES_USED = "dataBytesUsed";
		final static String TRAFFIC_REFERENCE = "trafficReference";
		final static String TRAFFIC_TOTAL = "trafficTotal";
	}
	public MobileDataManager(Context context) {
		this.context = context;
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		book = context.getSharedPreferences("book", Context.MODE_PRIVATE);
		pen = book.edit();
		update();
		
	}
	public void update() {
		logOfKeys = getLogOfKeys(); 

		logOfToday = getLogOfToday(); 
		todayFirstMobileData = getTodayFirstMobileData();
		currentMobileData = todayFirstMobileData;
		deadline = getDeadline();
		if (logOfToday.size() > 0) {
			currentMobileData = MobileData.parseStringFormat( logOfToday.last(), currentDataFormat() );
		} else if (logOfKeys.size() > 0) {
			String yesterdayLast = new TreeSet<String>( book.getStringSet(logOfKeys.last(), null) ).last();
			currentMobileData = MobileData.parseStringFormat( yesterdayLast, currentDataFormat() );
		} else {
			currentMobileData = new MobileData("", 0L, currentDataFormat());
		}
	}

	public interface OnReceiveMobileData {
		public void onReceive(MobileData mobileData, String source);
		public void onReceiveFailed( int failCode );
	}

	public void checkMobileData( OnReceiveMobileData onReceiveMobileData ) {
		if ( isDebugModeOn() ) {
			String res = debugText();
			String todayKey = getKeyOfToday();

			currentMobileData = new MobileData( res , System.currentTimeMillis(), currentDataFormat());
			store(logOfToday, todayKey, currentMobileData.getStringFormat());
			if ( !logOfKeys.contains( todayKey ) ) {
				store( logOfKeys, "logOfKeys", getKeyOfToday() );
				todayFirstMobileData = getTodayFirstMobileData();
			}
			checkDeadline( currentMobileData );
			onReceiveMobileData.onReceive( currentMobileData, res );
					
			
		} else {
			telephonyManager.sendUssdRequest("*222#", new TelephonyManager.UssdResponseCallback() {
	
				@Override
				public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence res) {
					String todayKey = getKeyOfToday();

					currentMobileData = new MobileData(res.toString(), System.currentTimeMillis(), currentDataFormat());
					store(logOfToday, todayKey, currentMobileData.getStringFormat());
					if ( !logOfKeys.contains( todayKey ) ) {
						store(logOfKeys, "logOfKeys", getKeyOfToday());
						todayFirstMobileData = getTodayFirstMobileData();
					}
					checkDeadline( currentMobileData );
					onReceiveMobileData.onReceive(currentMobileData, res.toString());
				}

				@Override
				public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failCode ) {
					onReceiveMobileData.onReceiveFailed( failCode );
				}
			}, mainLooper);

		}
	}


//	Debugging 

	private String debugText() {		
		return String.format( "Saldo: 870.53 CUP. Datos: %.2f GB Voz: 03:49:00. SMS: 369. Linea activa hasta 04-03-26 vence 31-08-26" , debugData > 0 ? debugData -= debugUseRate : 0 );
	}
	public boolean isDebugModeOn() {
		return book.getBoolean( Key.DEBUG_MODE , false );
	}
	
	public void setDebugMode( boolean state ) {
		pen.putBoolean( Key.DEBUG_MODE, state )
		.commit();
	}

	private void store(TreeSet<String> collection, String id, String info) {
		collection.add(info);
		pen.putStringSet(id, collection);
		pen.apply();
	}

	public TreeSet<String> getLogOfToday() {
		return new TreeSet<String>(book.getStringSet(getKeyOfToday(), new TreeSet<String>()));
	}

	public TreeSet<String> getLogGlobal() {
		TreeSet<String> logGlobal = new TreeSet<String>();
		for (String key : getLogOfKeys()) {
			for (String log : book.getStringSet(key, new TreeSet<String>())) {
				logGlobal.add(log);
			}
		}
		return logGlobal;
	}

//	Deadline packageCombo

	public Calendar getDeadline() {
		Calendar deadline = Calendar.getInstance();
		deadline.setTimeInMillis( book.getLong("deadline", 0 ));
		return deadline;
	}

	public void setDeadline(Calendar date) {
		this.deadline = (Calendar) date.clone();
		pen.putLong("deadline", date.getTimeInMillis());
		pen.commit();
	}

	private void checkDeadline( MobileData mobileData ) {
		if ( mobileData.getSmsBonus() > currentMobileData.getSmsBonus()) {
			Calendar newDeadline = Calendar.getInstance();
			newDeadline.setTime( mobileData.getCalendarDate().getTime());
			newDeadline.add( Calendar.DAY_OF_MONTH, 30 );
			setDeadline( newDeadline );
			Toast.makeText(context, "New deadline established", Toast.LENGTH_LONG).show();
		}
		currentMobileData = mobileData;
	}

	public int getDaysTillDeadline() {
		long pointedDayMillis = getDeadline().getTimeInMillis();
		long currentDayMillis = currentMobileData.getCalendarDate().getTimeInMillis();
		long diffDaysMillis = pointedDayMillis - currentDayMillis;
		return (int) (diffDaysMillis / (1000 * 60 * 60 * 24));
	}
	public void setPackageComboDate( Calendar date ) {
		this.deadline = ( Calendar ) date.clone();
		this.deadline.add( Calendar.DAY_OF_MONTH, 30 );
		pen.putLong("deadline", deadline.getTimeInMillis()).apply();
	}
	
	public Calendar getPackageComboDate() {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis( book.getLong( "deadline", 0l ) );
		date.add( Calendar.DAY_OF_MONTH, -30 );
		return date;
	}
//	Dataformat

	public DataFormat currentDataFormat() {
		return new DataFormat(book.getInt("data_format", DataFormat.DECIMAL));
	}

	public void setDataFormat(DataFormat dataFormat) {
		pen.putInt("data_format", dataFormat.getFormatType());
		pen.apply();
	}

//	Today Data

	private MobileData getTodayFirstMobileData() {
		if (logOfToday.size() > 0) {
			return MobileData.parseStringFormat( logOfToday.first(), currentDataFormat() );
		} else {
			return null;
		}
	}

	public MobileData getTodayCurrentMobileData() {
		if ( getTodayFirstMobileData() != null ) {
			return currentMobileData;			
		} else {
			return null;
		}
	}

	public String getKeyOfToday() {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MM dd");
		return simpleDateFormat.format(currentDate.getTime());
	}

	public TreeSet<String> getLogOfKeys() {
		return new TreeSet<String>(book.getStringSet("logOfKeys", new TreeSet<String>()));
	}

	public long getTodaySuggestionTillDeadline() {
	//	return todayFirstMobileData.getDataBytes() / getDaysTillDeadline() ;
		return getTodayLargerMobileData().getDataBytes() / getDaysTillDeadline() ;

	}

	public MobileData getTodayLargerMobileData() {
		if ( currentMobileData.getDataBytes() > todayFirstMobileData.getDataBytes() ) {
			return currentMobileData;
		} else {
			return todayFirstMobileData;
		}
	}

	public long getTodayDataBytesUsed() {
		
		long savedDataBytesUsed = book.getLong( Key.DATA_BYTES_USED, 0L );
		long todayDataBytesUsed = 0 ;
		MobileData todayLargerMobileData;

		if ( ( todayLargerMobileData = getTodayLargerMobileData() ) == null ) {
			todayDataBytesUsed = 0L;
		} else {
			todayDataBytesUsed = todayLargerMobileData.getDataBytes() - getTodayCurrentMobileData().getDataBytes();
		} 

		long currentMobileTraffic = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();

		boolean isMobileDataOn = currentMobileTraffic != 0;

		if ( savedDataBytesUsed != todayDataBytesUsed ) {
			int tolerance =  (int) Math.pow( 1000, 2 ) * 10 ; // 10mb of tolerance
			pen.putLong( Key.DATA_BYTES_USED, todayDataBytesUsed );

			if ( currentMobileTraffic / tolerance != savedDataBytesUsed / tolerance ) {
				pen.putLong( Key.TRAFFIC_REFERENCE, 0L)
				.putLong( Key.TRAFFIC_TOTAL, 0L);				
			}
			pen.commit();
			return todayDataBytesUsed;
		}
		
		

		if ( isMobileDataOn ) {
			pen.putLong( Key.TRAFFIC_TOTAL, currentMobileTraffic );

			if ( book.getLong( Key.TRAFFIC_REFERENCE, 0L ) == 0 ) {
				pen.putLong( Key.TRAFFIC_REFERENCE, currentMobileTraffic );
			}
			pen.commit();
		}
		return savedDataBytesUsed + getDataBytesOffset();
	}
	
	public long getDataBytesOffset() {
		return book.getLong( Key.TRAFFIC_TOTAL, 0L ) - book.getLong( Key.TRAFFIC_REFERENCE, 0L);
	}

//	Cleaning

	public void clearAllData() {
		pen.clear();
		pen.commit();
	}
	public void clearTodayData() {
		String todaysKey = getKeyOfToday();
		logOfToday = new TreeSet<String>();
		pen.putStringSet(todaysKey, logOfToday);
		logOfKeys.remove(todaysKey);
		pen.putStringSet("logOfKeys", logOfKeys)
		.remove( Key.DATA_BYTES_USED )
		.remove( Key.TRAFFIC_REFERENCE )
		.remove( Key.TRAFFIC_TOTAL );
		pen.commit();
	}
}

