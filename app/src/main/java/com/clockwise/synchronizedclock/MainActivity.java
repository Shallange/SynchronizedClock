package com.clockwise.synchronizedclock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    // Create an Executor to manage background tasks.
    private final Executor executor = Executors.newSingleThreadExecutor();
    // Handler to post tasks to the main thread.
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    // View reference for the network status indicator.
    private View indicatorView;
    // Reference for the TextClock UI element to display time.
    private TextClock textClock1;
    // Flag to check if the app is currently fetching NTP data.
    private boolean isFetchingNTP = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Cache the views for better performance inside onCreate().
        textClock1 = findViewById(R.id.textClock1);
        indicatorView = findViewById(R.id.indicatorView);

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateUIInBackground();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(updateRunnable);

        Button button1 = findViewById(R.id.button1);//"Click here to update time"-button
        button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                fetchAndDisplayNtp();
            }
        });
    }

    private String getSystemTime(){
        Date date=new Date(System.currentTimeMillis());
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(date);
    }
    private void displaySystemTime() {
        textClock1.setText(getSystemTime());
    }


    private boolean isNetworkConnected() {
        // Get the ConnectivityManager instance to manage network connections.
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check if the ConnectivityManager instance is null.
        if (cm == null) {
            Log.d("NetworkCheck", "ConnectivityManager is null");
            return false;
        }
        // Get the currently active network.
        Network network = cm.getActiveNetwork();
        // Check if there's an active network connection.
        if (network == null) {
            Log.d("NetworkCheck", "No active network found");
            return false;
        }
        // Get the capabilities of the active network.
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        // Check if the active network has Wi-Fi transport capabilities.
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
    }


    private void updateUI(boolean isConnected) {
        // Check if the device is connected to the network.
        if (isConnected) {
            if (!isFetchingNTP) {
                try {
                    // Check if the device is connected to the network.
                    fetchAndDisplayNtp();
                    // Set the indicator view's background color to green to indicate a successful fetch.
                    indicatorView.setBackgroundColor(Color.GREEN);
                } catch (Exception e) {
                    // If there's an error in fetching the NTP time, set the indicator view's background color to red.
                    indicatorView.setBackgroundColor(Color.RED);
                }
            }
        } else {
            // If the device is not connected to the network, display the system time.
            displaySystemTime();
            // Set the indicator view's background color to red to indicate no network connection.
            indicatorView.setBackgroundColor(Color.RED);
        }
    }
    private void updateUIInBackground() {
        // Execute the following code in a background thread.
        executor.execute(() -> {
            // Check if the device is connected to the network.
            boolean isConnected = isNetworkConnected();
            // Post the result to the main thread to update the UI.
            mainHandler.post(() -> {
                updateUI(isConnected);
            });
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