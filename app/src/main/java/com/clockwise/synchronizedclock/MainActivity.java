package com.clockwise.synchronizedclock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextClock;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MainActivity class for the Synchronized Clock application.
 */
public class MainActivity extends AppCompatActivity {
    // Declaring UI components and other instance variables
    private TextClock textClock1;
    private View indicatorView;
    private Button pauseButton;
    private boolean isPaused = false;
    private long offset = 0;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isConnected = true;
    private ScheduledExecutorService scheduler;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initializing UI components
        indicatorView = findViewById(R.id.indicatorView);
        textClock1 = findViewById(R.id.textClock1);
        pauseButton = findViewById(R.id.pauseButton);
        // Initializing network connectivity manager and setting up network callback
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {

            /**
             * Called when the framework connects and has declared a new network ready for use.
             *
             * @param network The Network object corresponding to the network that has become available.
             */
            @Override
            public void onAvailable(Network network) {
                isConnected = true;
                runOnUiThread(() -> {
                    pauseButton.setVisibility(View.VISIBLE);  // Show the pause button
                    indicatorView.setBackgroundColor(Color.GREEN);
                });
                fetchNtpOffset();
            }

            /**
             * Called when a network disconnects or otherwise no longer satisfies this request or callback.
             *
             * @param network The Network object corresponding to the network that has been lost.
             */
            @Override
            public void onLost(Network network) {
                isConnected = false;
                offset = 0;  // Resetting the offset when there's no internet
                runOnUiThread(() -> {
                    pauseButton.setVisibility(View.INVISIBLE);  // Hide the pause button
                    indicatorView.setBackgroundColor(Color.RED);
                });
            }
        };

        NetworkRequest request = new NetworkRequest.Builder().build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
        // Schedule NTP offset fetching every 10 minutes
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::fetchNtpOffset, 0, 10, TimeUnit.MINUTES);

        // Handler and Runnable for periodic UI updates
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis() + offset;
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", new Locale("sv", "SE"));
                textClock1.setText(sdf.format(date));

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);

        // Initialize and set click listener for pause button
        Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(v -> {
            isPaused = !isPaused;// Toggle the paused flag
            if (!isPaused) {
                fetchNtpOffset();
                pauseButton.setText("Pause Fetching");
                Toast.makeText(MainActivity.this, "Fetching Resumed", Toast.LENGTH_SHORT).show();
            } else {
                pauseButton.setText("Resume Fetching");
                Toast.makeText(MainActivity.this, "Fetching Paused", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches the NTP offset from the NTP server.
     */
    private void fetchNtpOffset() {
        // If not connected or paused, return without fetching
        if (!isConnected || isPaused) return;
        new Thread(() -> {// Start a new thread to fetch the NTP offset
            UdpNtpClient client = new UdpNtpClient();
            try {
                offset = client.fetchNtp();// Fetch the offset using the UdpNtpClient
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Called before the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduler.shutdown();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}
