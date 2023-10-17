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
        byte[] ntpRequest = new byte[48];
        ntpRequest[0] = 0x1B;
            //00011011 = 0x1B
            //bits 0-1 Leap indicator(LI) '00' = No Leap second
            //bits 2-4 version number '011' = version 3 NTP protocol
            //bits 5-7 Mode '011' = client mode
        byte[] buffer = new byte[48];

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
