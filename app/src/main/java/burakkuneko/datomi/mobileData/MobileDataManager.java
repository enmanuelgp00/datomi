package burakkuneko.datomi.mobiledata;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.os.Looper;
import android.os.Handler;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.util.Calendar;

import java.text.SimpleDateFormat;

public class MobileDataManager {

    Handler mainLooper = new Handler(Looper.getMainLooper());
    Context context;
    SharedPreferences book;
    SharedPreferences.Editor pen;
    TelephonyManager telephonyManager;
    TreeSet<String> responseHistory;
    Calendar deadline;
    MobileData previousMobileData, todayFirstMobileData, currentMobileData;
    TreeSet<String> logOfKeys, logOfToday;
    String keyLogOfToday;

    public MobileDataManager(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        book = context.getSharedPreferences("book", Context.MODE_PRIVATE);
        pen = book.edit();

        logOfKeys = getLogOfKeys(); 

        logOfToday = getLogOfToday(); 
        todayFirstMobileData = getTodayFirstMobileData();
		currentMobileData = todayFirstMobileData;
        deadline = getDeadline();
        if (logOfToday.size() > 0) {
            previousMobileData = MobileData.parseStringFormat(logOfToday.last(), currentDataFormat());
        } else if (logOfKeys.size() > 0) {
            String yesterdayLast = new TreeSet<String>(book.getStringSet(logOfKeys.last(), null)).last();
            previousMobileData = MobileData.parseStringFormat(yesterdayLast, currentDataFormat());
        } else {
            previousMobileData = new MobileData("", 0L, currentDataFormat());
        }
    }

    public interface OnReceiveMobileData {
        public void onReceive(MobileData mobileData, String source);
    }

    public void checkMobileData(OnReceiveMobileData onReceiveMobileData) {
        telephonyManager.sendUssdRequest("*222#", new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence res) {
                String keyOfToday = getKeyOfToday();

                currentMobileData = new MobileData(res.toString(), System.currentTimeMillis(), currentDataFormat());
                store(logOfToday, keyOfToday, currentMobileData.getStringFormat());
                if (!logOfKeys.contains(keyOfToday)) {
                    store(logOfKeys, "logOfKeys", getKeyOfToday());
                    todayFirstMobileData = getTodayFirstMobileData();
                }
                verifyDeadline(currentMobileData);
                onReceiveMobileData.onReceive(currentMobileData, res.toString());
            }
        }, mainLooper);
    } // end of checkMobileData

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

    public Calendar getDeadline() {
        Calendar deadline = Calendar.getInstance();
        deadline.setTimeInMillis(book.getLong("deadline", 0));
        return deadline;
    }

    public void setDeadline(Calendar date) {
        this.deadline = date;
        pen.putLong("deadline", date.getTimeInMillis());
        pen.apply();
    }

    private void verifyDeadline(MobileData currentMobileData) {
        if (currentMobileData.getSmsBonus() > previousMobileData.getSmsBonus()) {
            Calendar newDeadline = Calendar.getInstance();
            newDeadline.setTime(currentMobileData.getCalendarDate().getTime());
            newDeadline.add(Calendar.DAY_OF_MONTH, 30);
            setDeadline(newDeadline);
            Toast.makeText(context, "New deadline created ", Toast.LENGTH_LONG).show();
        }
        previousMobileData = currentMobileData;
    }

    public int getDaysTillDeadline() {
        long pointedDayMillis = getDeadline().getTimeInMillis();
        long currentDayMillis = previousMobileData.getCalendarDate().getTimeInMillis();
        long diffDaysMillis = pointedDayMillis - currentDayMillis;
        return (int) (diffDaysMillis / (1000 * 60 * 60 * 24));
    }

    public DataFormat currentDataFormat() {
        return new DataFormat(book.getInt("data_format", DataFormat.DECIMAL));
    }

    public void setDataFormat(DataFormat dataFormat) {
        pen.putInt("data_format", dataFormat.getFormatType());
        pen.apply();
    }

    private MobileData getTodayFirstMobileData() {
        if (logOfToday.size() > 0) {
            return MobileData.parseStringFormat(logOfToday.first(), currentDataFormat());
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

    public long todaySuggestionTillDeadline() {
        return todayFirstMobileData.getDataBytes() / getDaysTillDeadline() + 1;
    }
    public MobileData todayLargerMobileData() {
		if (currentMobileData.getDataBytes() > todayFirstMobileData.getDataBytes()) {
			return currentMobileData;
		} else {
			return todayFirstMobileData;
		}
    }

    public long todayDataBytesUsed() {
        return todayFirstMobileData.getDataBytes() - previousMobileData.getDataBytes();
    }

    public void clearAllData() {
        pen.clear();
        pen.commit();
    }
    public void clearTodayData() {
        String todaysKey = getKeyOfToday();
        logOfToday = new TreeSet<String>();
        pen.putStringSet(todaysKey, logOfToday);
        logOfKeys.remove(todaysKey);
        pen.putStringSet("logOfKeys", logOfKeys);
        pen.commit();
    }
}
