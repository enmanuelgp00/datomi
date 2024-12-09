package burakkuneko.datomi;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import burakkuneko.datomi.mobiledata.*;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ActivityOptions extends Activity {
    private EditText editTextDeadline; 
    private Button buttonApply;
    private MobileDataManager mobileDataManager;
    private SimpleDateFormat dateFormater;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private DataFormat dataFormat;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_options);

        dateFormater = new SimpleDateFormat("dd MM yyyy");
        mobileDataManager = new MobileDataManager(this);
        dataFormat = mobileDataManager.currentDataFormat();
        buttonApply = findViewById(R.id.buttonApply);
        editTextDeadline = findViewById(R.id.editTextDeadline);

        loadSetting();
        radioGroup = findViewById(R.id.radio_group_format_data);

        radioGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId){
                
                if (checkedId == R.id.radio_button_binary) {
                    dataFormat.setFormatType(DataFormat.BINARY);
                } else {
                    dataFormat.setFormatType(DataFormat.DECIMAL);
                }
            }
        });
        buttonApply.setOnClickListener(applyChanges);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_clear_data:
                mobileDataManager.clearAllData();
                Toast.makeText(this, "All stored data erased", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
    private View.OnClickListener applyChanges = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateDeadline();
            updateDataFormat();
        };
    };

    void updateDeadline() {
        try {
                Calendar calendarDate = Calendar.getInstance();
                SimpleDateFormat verbose = new SimpleDateFormat("EEEE dd MMMM yyyy");
                calendarDate.setTime(dateFormater.parse(editTextDeadline.getText().toString()));
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
        editTextDeadline.setText(dateFormater.format(initialCalendarDate.getTime()));

        if (dataFormat.getFormatType() == DataFormat.BINARY) {
            radioButton = findViewById(R.id.radio_button_binary);
            radioButton.setChecked(true);
        } else {
            radioButton = findViewById(R.id.radio_button_decimal);
            radioButton.setChecked(true);
        }
    }


}