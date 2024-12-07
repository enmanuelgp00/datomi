package burakkuneko.datomi.log;
import android.app.Activity;
import android.widget.Toast;
public class Logger {
	Activity activity;
	public Logger(Activity activity) {
		this.activity = activity;
	}
	public void print(String str) {
		Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
	}                                                           
	public void print(int num) {
		String str = String.valueOf(num);
		Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
	}            	                                                          
	public void print(double num) {
		String str = String.valueOf(num);
		Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
	}
}
