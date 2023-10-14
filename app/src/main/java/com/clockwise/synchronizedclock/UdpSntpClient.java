package com.clockwise.synchronizedclock;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpSntpClient {

    //Define a constant for NTP server host
    private static final String NtpHost = "0.pool.ntp.org";

    //Define a constant for NTP server port
    private static final int NtpPort = 123;

    // Define fetchNtp, a method for fetching NTP-Time
    public long fetchNtp(){
        // Using try-with-resources for DatagramSocket, which auto-closes due to AutoCloseable.
        try(DatagramSocket socket = new DatagramSocket()){



           //fill with UDP logic here


            //Catch Socket Exeptions
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        //Return 0 just as a placeholder, it should return the time
        return 0;
    }
}
