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
   DataFormat dataFormatter;

   public MobileData(String source, Calendar calendarDate, DataFormat dataFormatter) {
      arrSource = source.split(" ");
      credit = retreive("Saldo");
      dataBytes = (long) (retreive("GB") * Math.pow(1000, 3) + retreive("MB") * Math.pow(1000, 2));
      voiceBonus = retreiveStr("Voz");
      smsBonus = (int) retreive("SMS");
      calendarDate = Calendar.getInstance();
      this.calendarDate = calendarDate;
      this.dataFormatter = dataFormatter;
   }

   public MobileData(String source, Long dateLong, DataFormat dataFormatter) {
      arrSource = source.split(" ");
      credit = retreive("Saldo");
      dataBytes = (long) (retreive("GB") * Math.pow(1000, 3) + retreive("MB") * Math.pow(1000, 2));
      voiceBonus = retreiveStr("Voz");
      smsBonus = (int) retreive("SMS");
      calendarDate = Calendar.getInstance();
      this.calendarDate.setTimeInMillis(dateLong);
      this.dataFormatter = dataFormatter;
   }

   private MobileData (String format, DataFormat dataFormatter) {
      String[] arr = format.split(" ");
      calendarDate = Calendar.getInstance();
      calendarDate.setTimeInMillis(Long.parseLong(arr[0]));
      credit = Double.parseDouble(arr[1]);
      dataBytes = Long.parseLong(arr[2]);
      voiceBonus = arr[3];
      smsBonus = Integer.parseInt(arr[4]);
      this.dataFormatter = dataFormatter;
   }

   public static MobileData parseStringFormat(String format, DataFormat dataFormatter) {
      return new MobileData(format, dataFormatter);
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
      SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss aa");
      return 
      String.format(
         "\nData   : %13s\nCredit : $ %,.2f\nMessage: %10d SMS\nVoice  : %10s\nDate   : %s\n",
         dataFormatter.format(getDataBytes()),
         getCredit(),
         getSmsBonus(),
         getVoiceBonus(),
         dateFormatter.format(getCalendarDate().getTime())
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
