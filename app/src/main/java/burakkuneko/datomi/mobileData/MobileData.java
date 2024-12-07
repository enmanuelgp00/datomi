package burakkuneko.datomi.mobiledata;

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class MobileData {
   private String[] arrSource;
   private String voiceBonus;
   private int smsBonus;
   private long dataBytes;
   private double credit;
   private Calendar calendarDate;
   DataFormat dataFormater;

   public MobileData(String source, Calendar calendarDate, DataFormat dataFormater) {
      arrSource = source.split(" ");
      credit = retreive("Saldo");
      dataBytes = (long) (retreive("GB") * Math.pow(1000, 3) + retreive("MB") * Math.pow(1000, 2));
      voiceBonus = retreiveStr("Voz");
      smsBonus = (int) retreive("SMS");
      calendarDate = Calendar.getInstance();
      this.calendarDate = calendarDate;
      this.dataFormater = dataFormater;
   }

   public MobileData(String source, Long dateLong, DataFormat dataFormater) {
      arrSource = source.split(" ");
      credit = retreive("Saldo");
      dataBytes = (long) (retreive("GB") * Math.pow(1000, 3) + retreive("MB") * Math.pow(1000, 2));
      voiceBonus = retreiveStr("Voz");
      smsBonus = (int) retreive("SMS");
      calendarDate = Calendar.getInstance();
      this.calendarDate.setTimeInMillis(dateLong);
      this.dataFormater = dataFormater;
   }

   private MobileData (String format, DataFormat dataFormater) {
      String[] arr = format.split(" ");
      calendarDate = Calendar.getInstance();
      calendarDate.setTimeInMillis(Long.parseLong(arr[0]));
      credit = Double.parseDouble(arr[1]);
      dataBytes = Long.parseLong(arr[2]);
      voiceBonus = arr[3];
      smsBonus = Integer.parseInt(arr[4]);
      this.dataFormater = dataFormater;
   }

   public static MobileData parseStringFormat(String format, DataFormat dataFormater) {
      return new MobileData(format, dataFormater);
   }      

   public String getStringFormat() {
      return String.format("%d %f %d %s %d",
         getCalendarDate().getTimeInMillis(),
         getCredit(),
         getDataBytes(),
         getVoiceBonus(),
         getSmsBonus()
      );
   }

   private double retreive(String token) {
      double value = 0;      
      for (int i = 0; i < arrSource.length; i++) {
         if (arrSource[i].contains(token)) {
            if (arrSource[i].contains(":")) {
              value += Double.parseDouble(arrSource[i + 1]);
            } else {
              value += Double.parseDouble(arrSource[i - 1]);
            }
         }
      }
      return value;
   }

   private String retreiveStr(String token) {
      for (int i = 0; i < arrSource.length; i++) {
         if (arrSource[i].contains(token)) {
            if (arrSource[i].contains(":")) {
              return arrSource[i + 1].replace(".","");  
            } else {
              return arrSource[i - 1];                
            }
         }
      }
      return "";
   }
   public long getDataBytes() {
      return dataBytes;
   }
   public int getSmsBonus() {
      return smsBonus;
   }
   public String getVoiceBonus() {
      return voiceBonus;
   }
   public Calendar getCalendarDate() {
      return calendarDate;
   }
   public double getCredit() {
      return credit;
   }

   public String asString() {
      SimpleDateFormat dateFormater = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss aa");
      return 
      String.format(
         "\nData   : %s\nCredit : $ %,.2f\nMessage: %d SMS\nVoice  : %s\nDate   : %s\n",
         dataFormater.format(getDataBytes()),
         getCredit(),
         getSmsBonus(),
         getVoiceBonus(),
         dateFormater.format(getCalendarDate().getTime())
     );
   }

   public boolean after (MobileData diffMobileData) {
      if (this.getCalendarDate().get(Calendar.YEAR) > diffMobileData.getCalendarDate().get(Calendar.YEAR)) {
         return true;
      } else if (this.getCalendarDate().get(Calendar.MONTH) > diffMobileData.getCalendarDate().get(Calendar.MONTH)) {
         return true;
      } else if (this.getCalendarDate().get(Calendar.MONTH) == diffMobileData.getCalendarDate().get(Calendar.MONTH)) {
         if (this.getCalendarDate().get(Calendar.DAY_OF_MONTH) > diffMobileData.getCalendarDate().get(Calendar.DAY_OF_MONTH)) {
            return true;
         }
      }
      return false;
   }
}
