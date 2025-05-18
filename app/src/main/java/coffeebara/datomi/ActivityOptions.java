package coffeebara.datomi;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import coffeebara.datomi.mobiledata.*;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ActivityOptions extends Activity {
	private EditText editTextDeadline; 
	private Button buttonApply;
	private MobileDataManager mobileDataManager;
	private SimpleDateFormat dateFormatter;
	private RadioGroup radioGroup;
	private RadioButton radioButton;
	private DataFormat dataFormat;
	private CheckBox chbxDebugMode;
	
	public void onCreate(Bundle savedState) {
		super.onCreate( savedState );
		setContentView( R.layout.activity_options );

		dateFormatter = new SimpleDateFormat("dd MM yyyy");
		mobileDataManager = new MobileDataManager(this);
		dataFormat = mobileDataManager.currentDataFormat();

		editTextDeadline = findViewById( R.id.edtxt_deadline );
		chbxDebugMode = findViewById( R.id.chbx_debug_mode );
		radioGroup = findViewById( R.id.radio_group_data_format );

		if ( mobileDataManager.isDebugModeOn() ) {
			chbxDebugMode.setChecked( true );
		}

		chbxDebugMode.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked ) {
				mobileDataManager.setDebugMode( isChecked );
				if ( isChecked ) {
					quickMessage( "Debug mode enabled" );
				} else {
					quickMessage( "Debug mode disabled" );
				}
			}
		});

		loadSetting();

	
		radioGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId){
				
				if (checkedId == R.id.radio_button_binary) {
					dataFormat.setFormatType(DataFormat.BINARY);
				} else {
					dataFormat.setFormatType(DataFormat.DECIMAL);
				}
				updateDataFormat();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.menu_options, menu );
		return true;
	}

	public boolean onOptionsItemSelected( MenuItem menuItem ) {
		switch(menuItem.getItemId()) {
			case R.id.menu_clear_data:
				mobileDataManager.clearAllData();
				Toast.makeText(this, "All stored data erased", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onOptionsItemSelected(menuItem);
	}

	void updateDeadline() {
		try {
			Calendar calendarDate = Calendar.getInstance();
			SimpleDateFormat verbose = new SimpleDateFormat("EEEE dd MMMM yyyy");
			calendarDate.setTime(dateFormatter.parse(editTextDeadline.getText().toString()));
			calendarDate.add(Calendar.DAY_OF_MONTH, 30);
			mobileDataManager.setDeadline(calendarDate);
			Toast.makeText(ActivityOptions.this, verbose.format(calendarDate.getTime()), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			
		}
	}

	void updateDataFormat() {
		mobileDataManager.setDataFormat(dataFormat);		
	}

	void loadSetting(){		
		Calendar initialCalendarDate = Calendar.getInstance();
		initialCalendarDate = mobileDataManager.getDeadline();
		initialCalendarDate.add(Calendar.DAY_OF_MONTH, -30);
		editTextDeadline.setText(dateFormatter.format(initialCalendarDate.getTime()));

		if (dataFormat.getFormatType() == DataFormat.BINARY) {
			radioButton = findViewById(R.id.radio_button_binary);
			radioButton.setChecked(true);
		} else {
			radioButton = findViewById(R.id.radio_button_decimal);
			radioButton.setChecked(true);
		}
	}
	
	private void quickMessage( String message ) {
		Toast.makeText( this, message, Toast.LENGTH_SHORT ).show();
	}

}