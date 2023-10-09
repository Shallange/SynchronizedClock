package com.clockwise.synchronizedclock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    Button button1 = findViewById(R.id.button1);//"Click here to update time"-button
    button1.setOnClickListener(new View.OnClickListener() {//adding an eventlistener(OnClick) to button1
        @Override
        public void onClick(View v) {
            String timeStamp = new SimpleDateFormat("yyyy-M-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
            //finding the textClock id and setting the text to the current date/time
            TextClock textClock1 = findViewById(R.id.textClock1);
            textClock1.setText(timeStamp);
        }
    });

    }
}