package com.clockwise.synchronizedclock;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Service class responsible for fetching NTP time using UDP.
 */
public class UdpSntpService {

    /** Define a constant for NTP server host*/
    private static final String NTP_HOST = "se.pool.ntp.org";

    /** Define a constant for NTP server port */
    private static final int NTP_PORT = 123;

    /** Starting buffer position for original timestamp. 24-27 -- 28-31 */
    int t1StartBuff = 24;

    /** Starting buffer position for receive timestamp. 32-35 -- 36-39 */
    int t2StartBuff = 32;

    /** Starting buffer position for transmit timestamp. 40-43 -- 44-47 */
    int t3StartBuff = 40;

    /** Starting buffer position for reference timestamp. 16-19 -- 20-23 */
    int t4StartBuff = 16;

    /**
     * Parse the response buffer to extract NTP timestamps.
     *
     * @param buffer The response buffer.
     * @param b The starting position for the timestamp in the buffer.
     * @return The extracted timestamp.
     */
    private long responseBuffer(byte[] buffer,int b){
        byte b0 = buffer[b];
        byte b1 = buffer[b+1];
        byte b2 = buffer[b+2];
        byte b3 = buffer[b+3];

        byte b4 = buffer[b+4];
        byte b5 = buffer[b+5];
        byte b6 = buffer[b+6];
        byte b7 = buffer[b+7];
        long seconds = (b0 & 0xFFL) << 24 |
                       (b1 & 0xFFL) << 16 |
                       (b2 & 0xFFL) << 8  |
                       (b3 & 0xFFL);
        // Extract the fractional seconds from the response
        long fraction = (b4 & 0xFFL) << 24 |
                        (b5 & 0xFFL) << 16 |
                        (b6 & 0xFFL) << 8  |
                        (b7 & 0xFFL);
        //ntp-time is in seconds(January 1,1900) and this converts it to unix which is in milliseconds(January 1, 1970)
        long timestamp =(seconds - 2208988800L) * 1000L + (fraction * 1000L / 0x100000000L);
        return timestamp;
    }

    /**
     * Fetches the NTP time from the specified server.
     * @return Unix timestamp representing the NTP time.
     * @throws IOException if there's an error during network communication.
     */
    public long fetchNtp() throws IOException {
        // Initialize a byte array for the NTP request.
        byte[] ntpRequest = new byte[48];
        ntpRequest[0] = 0x23;
        //Explanation of the byte config
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

            long t1 = responseBuffer(buffer,t1StartBuff);
            long t2 = responseBuffer(buffer,t2StartBuff);
            long t3 = responseBuffer(buffer,t3StartBuff);
            long t4 = responseBuffer(buffer,t4StartBuff);

            long offset = ((t2 - t1) + (t3 - t4)) / 2;
          
            Log.d("NTP_CLIENT", "NTP Response bytes: " + Arrays.toString(buffer));
            return offset;
            //Catch Socket Exceptions
        } catch (SocketException e) {
            //rethrow the exception as a RuntimeException
            throw new RuntimeException(e);
        }
    }
}
