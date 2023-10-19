package com.clockwise.synchronizedclock;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class UdpSntpService {

    //Define a constant for NTP server host
    private static final String NTP_HOST = "se.pool.ntp.org";

    //Define a constant for NTP server port
    private static final int NTP_PORT = 123;

    // Define fetchNtp, a method for fetching NTP-Time
    public long fetchNtp() throws IOException {
        byte[] ntpRequest = new byte[48];
        ntpRequest[0] = 0x23;
        //Explanation of the byte's bi config
        //00100011 = 0x23
        //bits 0-1 Leap indicator(LI) '00' = No Leap second
        //bits 2-4 version number '100' = version 4 NTP protocol
        //bits 5-7 Mode '011' = client mode

        // Create a byte array to store the server's response
        byte[] buffer = new byte[48];

        // Use a try-with-resources for DatagramSocket, which auto-closes due to AutoCloseable.
        try(DatagramSocket socket = new DatagramSocket()){

            InetAddress address = InetAddress.getByName(NTP_HOST);

            //DatagramPacket(byte[] buf, int length, InetAddress address, int port)
            DatagramPacket packet = new DatagramPacket(ntpRequest, ntpRequest.length,address,NTP_PORT);
            Log.d("NTP_CLIENT", "Sending NTP request to: " + NTP_HOST);

            // send the NTP request to the server
            socket.send(packet);

            //Receive the server's response
            //DatagramPacket(byte[] buf, int length)
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);

            socket.receive(response);
            Log.d("NTP_CLIENT", "Received NTP response");

            /* This was for me to know what each buffer contained and gave me a better understanding why we need to bit shift
            and reorder the timestamp( MSB(47) LSB(40) in the timestamp variable
            System.out.println("Buffer[47]: " + (buffer[47] & 0xFF));
            System.out.println("Buffer[46]: " + (buffer[46] & 0xFF));
            System.out.println("Buffer[45]: " + (buffer[45] & 0xFF));
            System.out.println("Buffer[44]: " + (buffer[44] & 0xFF));
            System.out.println("Buffer[43]: " + (buffer[43] & 0xFF));
            System.out.println("Buffer[42]: " + (buffer[42] & 0xFF));
            System.out.println("Buffer[41]: " + (buffer[41] & 0xFF));
            System.out.println("Buffer[40]: " + (buffer[40] & 0xFF));
            */
            // Extract the seconds from the response
            long seconds = (buffer[40] & 0xFFL) << 24 |
                           (buffer[41] & 0xFFL) << 16 |
                           (buffer[42] & 0xFFL) << 8  |
                           (buffer[43] & 0xFFL);

// Extract the fractional seconds from the response
            long fraction = (buffer[44] & 0xFFL) << 24 |
                            (buffer[45] & 0xFFL) << 16 |
                            (buffer[46] & 0xFFL) << 8  |
                            (buffer[47] & 0xFFL);
            Log.d("NTP_CLIENT", "NTP Response bytes: " + Arrays.toString(buffer));

            //ntp-time is in seconds(January 1,1900) and this converts it to unix which is in milliseconds(January 1, 1970)
            long timestamp =(seconds - 2208988800L) * 1000L + (fraction * 1000L / 0x100000000L);

            //Returns the Unix timestamp
            Log.d("NTP_CLIENT", "Extracted Unix timestamp: " + timestamp);

            return timestamp;
            //Catch Socket Exceptions
        } catch (SocketException e) {
            //rethrow the exception as a RuntimeException
            throw new RuntimeException(e);
        }
    }
}
