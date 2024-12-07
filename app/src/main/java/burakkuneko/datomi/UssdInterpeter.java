package burakkuneko.datomi;

public abstract class UssdInterpeter {
    public static double getMegabytes(String response ) {        
        String[] res = response.split(" ");
        double mg = 0;
        for (int i = 0; i < res.length; i ++) {
            if (res[i].equals("MG")) {
                mg += Double.parseDouble(res[i - 1]);
            }
            if (res[i].equals("GB")) {
                mg += Double.parseDouble(res[i - 1]) * 1024;
            }
        }
        return mg;
    }
}