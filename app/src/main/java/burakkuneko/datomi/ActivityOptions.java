package burakkuneko.datomi;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;



public class ActivityOptions extends Activity {
    Button button0, button1;
    EditText editText0;
    MobileDataManager mobileDataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        mobileDataManager = new MobileDataManager(this);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        editText0 = findViewById(R.id.edit_text0);
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar0 = Calendar.getInstance();
                Date date = strToDate(editText0.getText().toString());
                calendar0.setTime(date);
                calendar0.add(Calendar.DAY_OF_MONTH, 30);
                mobileDataManager.changeDeadline(calendar0);
                Toast.makeText(ActivityOptions.this, "Remaining days changed", Toast.LENGTH_SHORT).show();            
                
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityOptions.this, ActivityMain.class));
            }
        });

    }

    Date strToDate(String strDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            return simpleDateFormat.parse(strDate);
        } catch (ParseException e){

        }
        
            return new Date();
    }
}