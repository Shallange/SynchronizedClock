package com.clockwise.synchronizedclock;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
*
* Temporary comment, saved the system time here whilst trying implementing NTP-time below
*  String formattedDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(date);
   TextClock textClock1 = findViewById(R.id.textClock1);
   textClock1.setText(formattedDate);
* */

public class MainActivity extends AppCompatActivity {
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    Button button1 = findViewById(R.id.button1);//"Click here to update time"-button
    button1.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fetchAndDisplayNtp();
        }
    });
    }

    private void fetchAndDisplayNtp() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create an instance of UdpSntpClient
                    UdpSntpService client = new UdpSntpService();
                    try {
                        Log.d("MAIN_ACTIVITY", "Fetching NTP time...");

                        // Fetch the NTP time
                        final long ntpTime = client.fetchNtp();
                        Log.d("MAIN_ACTIVITY", "Fetched NTP time: " + ntpTime);

                        // Convert the fetched time to a Date object
                        final Date date = new Date(ntpTime);
                        // Format the date to a string
                        final String formattedDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(date);
                        Log.d("MAIN_ACTIVITY", "Formatted date: " + formattedDate);


                        // Update UI on the main thread using the Handler
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Find the TextClock from the layout using its ID
                                TextClock textClock1 = findViewById(R.id.textClock1);
                                // Set the formatted date as the text of the TextClock
                                textClock1.setText(formattedDate);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Start the thread
   }
}