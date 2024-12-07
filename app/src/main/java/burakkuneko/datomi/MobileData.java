package burakkuneko.datomi;

import java.util.Date;
import java.util.Calendar;

public class MobileData {
    String[] strMobileData;
    double credit = 0;
    double megabytes = 0;
    int smsBonus = 0;
    String voiceBonus = "";

    Calendar date = Calendar.getInstance();
    Calendar deadline;

    public MobileData () {
        deadline = Calendar.getInstance();
        deadline.add(Calendar.DAY_OF_MONTH, 30);
    }

    public MobileData (String strMobileData) {
        deadline = Calendar.getInstance();
        deadline.add(Calendar.DAY_OF_MONTH, 30);

        this.strMobileData = strMobileData.split(" ");
        credit = retreive("CUP");
        megabytes = (retreive("GB") * 1000 + retreive("MB"));
        smsBonus = (int) retreive("SMS");
        voiceBonus = retreiveStr("Voz");
    }

    private double retreive(String search) {
        double value = 0;
        for (int i = 0; i < strMobileData.length; i ++) {
            if (strMobileData[i].contains(search)) {
                if (strMobileData[i].contains(":")) {
                    value += Double.parseDouble(strMobileData[i + 1]);
                } else {                    
                    value += Double.parseDouble(strMobileData[i - 1]);
                }
            }
        }
        return value;
    }

    private String retreiveStr(String search) {
        String value = "";
        for (int i = 0; i < strMobileData.length; i ++) {
            if (strMobileData[i].contains(search)) {
                if (strMobileData[i].contains(":")) {
                    value =strMobileData[i + 1];
                } else {                    
                    value = strMobileData[i - 1];
                }
            }
        }
        return value;
    }

    String getVoiceBonus() {
        return voiceBonus;
    }

    void setVoiceBonus(String voice) {
        voiceBonus = voice;
    }

    int getSmsBonus() {
        return smsBonus;
    }
    void setSmsBonus(int sms) {
        this.smsBonus = sms;
    }
    void setMegabytes(double mg) {
        this.megabytes = mg;
    }
    double getMegabytes() {
        return megabytes;
    }
    double getCredit() {
        return credit;
    }

    Calendar getDate() {
        return date;
    }
    void setDate(Calendar date) {
        this.date = date;
    }

    boolean after(MobileData mobileData) {        
        int thisMonth = this.getDate().get(Calendar.MONTH);
        int month = mobileData.getDate().get(Calendar.MONTH);
        if (thisMonth > month) {
            return true;
        } else if (thisMonth == month) {
            return this.getDate().get(Calendar.DAY_OF_MONTH) > mobileData.getDate().get(Calendar.DAY_OF_MONTH);
        }
        return false;
    }

    Calendar getDeadline() {
        return deadline;
    }

    void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    int getRemainingDays() {
        return daysBetween(getDeadline(), getDate());
    }

    void setRemainingDays(int days) {        
        Calendar newDeadline = date;
        newDeadline.add(Calendar.DAY_OF_MONTH, days);
        deadline = newDeadline;
    }

    private int daysBetween(Calendar calendar0, Calendar calendar1) {
        long mills0 = calendar0.getTimeInMillis();
        long mills1 = calendar1.getTimeInMillis();
        long diffMills = mills0 - mills1;
        return (int) (diffMills / (24 * 60 * 60 * 1000));
    }



}