package burakkuneko.datomi.mobileData;

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

public class MobileDataManager {   
   Handler mainLooper = new Handler(Looper.getMainLooper());
   Context context;
   SharedPreferences book;
   SharedPreferences.Editor pen;
   TelephonyManager telephonyManager;
   TreeSet <String> responseHistory;
   Calendar deadline;
   MobileData previousMobileData;
   TreeSet<Sring> logKeys, logOfToday;
   String keyLogOfToday;

   public MobileDataManager(Context context) {
      this.context = context;
      telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      book = context.getSharedPreferences("book", Context.MODE_PRIVATE);
      pen = book.edit(); 
      
      logKeys = book.getStringSet("logKeys", new TreeSet<String>());

      logOfToday = book.getStringSet(getKeyOfToday(), new TreeSet<String>());      

      //responseHistory = new TreeSet<String> (book.getStringSet("responseHistory", new TreeSet<String>()));
      deadline = Calendar.getInstance();
      if (responseHistory.size() > 0) {
         previousMobileData = MobileData.parseStringFormat(responseHistory.last(), getDataFormat());
      } else {
         previousMobileData = new MobileData("", 0L, DataFormat.DECIMAL);
      }
   }
   
   public interface OnReceiveMobileData {
      public void onReceive(MobileData mobileData, String source);
   }
   
   public void checkMobileData(OnReceiveMobileData onReceiveMobileData) {      
      telephonyManager.sendUssdRequest("*222#", new TelephonyManager.UssdResponseCallback() {
         @Override
         public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence res) {
            MobileData currentMobileData = new MobileData(res.toString(), System.currentTimeMillis(), getDataFormat()); 
              
            store(currentMobileData, getKeyOfToday());
            if (currentMobileData.getSmsBonus() > previousMobileData.getSmsBonus()) {
               Calendar newDeadline = Calendar.getInstance();
               newDeadline.setTime(currentMobileData.getCalendarDate().getTime());
               newDeadline.add(Calendar.DAY_OF_MONTH, 30);
               setDeadline(newDeadline);
               Toast.makeText(context, "New deadline created ", Toast.LENGTH_LONG).show();
            }
            previousMobileData = currentMobileData;
            onReceiveMobileData.onReceive(currentMobileData, res.toString());
         }
      }, mainLooper);
   } // end of checkMobileData
   
   private void store(MobileData mobileData, String key) {             
      responseHistory.add(mobileData.getStringFormat());
      pen.putStringSet(key , responseHistory);
      pen.apply();
   }

   public TreeSet<String> getLogOfToday() {
      return logOfToday;
   }

   // public Set <String> getResponseHistory() {
   //    return responseHistory;
   // }

   public Calendar getDeadline() {
      deadline.setTimeInMillis(book.getLong("deadline", 0));
      return deadline;
   }

   public void setDeadline(Calendar date) {
      this.deadline = date;
      pen.putLong("deadline", date.getTimeInMillis());
      pen.apply();
   }

   public int getDaysToDeadline() {
      long pointedDayMillis = getDeadline().getTimeInMillis();
      long currentDayMillis = previousMobileData.getCalendarDate().getTimeInMillis();
      long diffDaysMillis = pointedDayMillis - currentDayMillis;      
      return (int) (diffDaysMillis / (1000 * 60 * 60 * 24));
   }
   public int getDataFormat() {
      return book.getInt("data_format", 1000);
   }
   public void setDataFormat(int dataFormat) {
      pen.putInt("data_format", dataFormat);
      pen.apply();
   }

   public mobileData getTodayFirstMobileData() { 
      return MobileData.parseStringFormat(logOfToday.first());
   }
   public String getKeyOfToday() {
      Calendar currentDate = Calendar.getInstance();
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MM dd");
      return simpleDateFormat.format(currentDate.getTime());
   }

   public TreeSet<String> getLogKeys() {
      return logKeys;
   }
}
