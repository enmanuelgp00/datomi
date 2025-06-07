package coffeebara.datomi;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import coffeebara.datomi.mobiledata.*;
import android.widget.*;
import android.graphics.Color;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ActivitySettings extends Activity {
	private CalendarUI calendarUI;
	private MobileDataManager mobileDataManager;
	private SimpleDateFormat dateFormatter;
	private DataFormat dataFormat;
	private Switch 
		switchBinaryFormat,
		switchDebugMode;
	private ViewGroup deadlineWrapper;
	private boolean isCalendarDisplayed = false ;
	private TextView packageCombo;

	public void onCreate(Bundle savedState) {
		super.onCreate( savedState );
		setContentView( R.layout.activity_settings );

		dateFormatter = new SimpleDateFormat("dd MM yyyy");

		mobileDataManager = new MobileDataManager(this);
		dataFormat = mobileDataManager.currentDataFormat();

		deadlineWrapper = findViewById( R.id.deadline_wrapper );
		packageCombo = findViewById( R.id.package_combo );
		packageCombo.setText( new SimpleDateFormat( "EE dd MMMM yyyy" ).format( mobileDataManager.getPackageComboDate().getTime() ) );
		switchBinaryFormat = findViewById( R.id.switch_binary_format );				
		switchDebugMode = findViewById( R.id.switch_debug_mode );
		
		calendarUI = new CalendarUI( this , new CalendarUI.OnSubmit() {
			@Override
			public void onDone( View view, Calendar date ) {
				mobileDataManager.setPackageComboDate( date );
				
				packageCombo.setText( new SimpleDateFormat( "EE dd MMMM yyyy" ).format( mobileDataManager.getPackageComboDate().getTime() ) );
				Toast.makeText( ActivitySettings.this, new SimpleDateFormat("EE dd MMMM yyyy").format( date.getTimeInMillis() ), Toast.LENGTH_SHORT ).show();
				((ViewGroup)view.getParent()).removeView( view );
				isCalendarDisplayed = false;
			}
			public void onCancel( View view ) {
				isCalendarDisplayed = false;
			}
		});
		
		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
		relativeParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
		(( RelativeLayout.MarginLayoutParams )relativeParams ).setMargins( 7, 7, 7, 7 );
		calendarUI.setLayoutParams( relativeParams );
		RelativeLayout absoluteParent = findViewById( R.id.main_wrapper );
		calendarUI.setBackgroundColor( Color.parseColor( "#122220" ) );

		deadlineWrapper.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				if ( !isCalendarDisplayed ) {
					absoluteParent.addView( calendarUI );
				}
				isCalendarDisplayed = true;
			}
		} );

		if ( mobileDataManager.isDebugModeOn() ) {
			switchDebugMode.setChecked( true );
		}
		if ( dataFormat.getFormatType() == DataFormat.BINARY ) {
			switchBinaryFormat.setChecked( true );
		}
		
		switchDebugMode.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked ) {
				mobileDataManager.setDebugMode( isChecked );
				if ( isChecked ) {
				} else {
				}
			}
		});

		switchBinaryFormat.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked ) {
				if ( isChecked ) {
					dataFormat.setFormatType( DataFormat.BINARY );
				} else {
					dataFormat.setFormatType( DataFormat.DECIMAL );
				}
				mobileDataManager.setDataFormat(dataFormat);
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
	public ViewGroup getAbsoluteParent( View view ) {
		View parent = ( View ) view.getParent();
		if ( parent != null ) {
			return getAbsoluteParent( parent );
		}
		return ( ViewGroup ) view;
	}
	public LinearLayout getNearLinearParent( View view ) {
		if ( !( view.getParent() instanceof LinearLayout ) ) {
			return getNearLinearParent( ( View ) view.getParent() );
		}		
		return (LinearLayout) view.getParent() ;
	}	
}
