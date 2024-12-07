package burakkuneko.datomi;

import android.app.Activity;
import burakkuneko.datomi.log.Logger;
public class DinamicManager extends Manager {
	Logger log;
	public DinamicManager(Activity Activity, String data)  {
		super(Activity, data);
		log = new Logger(Activity);
	}
	public double getGuessData(){
		return 0.0;
	}
}