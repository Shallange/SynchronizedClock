package com.clockwise.synchronizedclock;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.IOException;
/*
 * NtpTimeWorker fetches NTP time using UdpSntpService. It extends Android's Worker
 * for efficient background tasks.
 * On successful fetch -> it returns a success result;
 * otherwise -> it returns a failure.
 */

// Define the NtpTimeWorker class which extends the Worker class from Android's WorkManager library
public class NtpTimeWorker extends Worker{
    // Declare a private variable for the UdpSntpService class
    private UdpSntpService udpSntpService;
    public NtpTimeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        udpSntpService = new UdpSntpService();
    }
    @NonNull
    @Override
    public Result doWork() {
        try {
            // Fetch the NTP time using the UdpSntpService instance
            long ntpTime = udpSntpService.fetchNtp();

            // If fetching the NTP time is successful, return a success result
            return Result.success();
        } catch (IOException e) {
            return Result.failure();
        }
    }
}