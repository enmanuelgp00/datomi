package burakkuneko.datomi;

public class MobileData {

    double megas;
    int days;

    public MobileData() {
    }
    public MobileData(String data) {
		
        String[] arr = data.split(" ");
        days = (int) f(arr, "dias.");
        megas = f(arr, "MB") + (f(arr, "GB") * 1024);
    }


    int getDays() {
        return days;
    }

    double getMegas() {
        return megas;
    }
    private double f(String[] arr, String sign) {
        double total = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(sign)) {

                total += Double.parseDouble(arr[i - 1]);
            }
        }
        return total;
    }
}
