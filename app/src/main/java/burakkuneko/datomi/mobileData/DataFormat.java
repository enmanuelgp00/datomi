package burakkuneko.datomi.mobiledata;

public class DataFormat {
    public final static int BINARY = 1024;
    public final static int DECIMAL = 1000;
    String[] names;
    final String[] BINARY_NAMES  = {"b", "KiB", "MiB", "GiB", "TiB"};
    final String[] DECIMAL_NAMES = {"b", "KB" , "MB" , "GB" , "TB"};
    private int formatType;

    public DataFormat (int formatType) {
        this.formatType = formatType;
        if (formatType == BINARY) {
            names = BINARY_NAMES;
        } else {
            names = DECIMAL_NAMES;
        }
    }

    public String format (long bytes) {
        double data;
        int scale;
        for (int i = 0; i < names.length; i++) {
            scale = i + 1;
            data = bytes / Math.pow(formatType, scale);
            if (data < formatType && data >= 0) {
                return String.format("%,.2f %s",
                    data,
                    names[scale]
                );
            }
        }
        return "unkown";
        //throw new IllegalArgumentException("Error format out of range or negative data");
    }

    public int getFormatType() {
        return formatType;
    }

    public void setFormatType(int type) {
        switch (type) {
            case BINARY:
                formatType = BINARY;
                break;
            case DECIMAL:
                formatType = DECIMAL;
                break;
            default:
                throw new IllegalArgumentException("Wrong format type");
        }
    }
}