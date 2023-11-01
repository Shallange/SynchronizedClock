package com.clockwise.synchronizedclock;

import android.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Service class responsible for fetching NTP time using UDP.
 */
public class UdpNtpClient {

    /** Define a constant for NTP server host*/
    private static final String NTP_HOST = "sth1.ntp.se";

    /** Define a constant for NTP server port */
    private static final int NTP_PORT = 123;


    /** Starting buffer position for receive timestamp. 32-35 -- 36-39 */
    int T2_START_BUFF = 32;

    /** Starting buffer position for transmit timestamp. 40-43 -- 44-47 */
    int T3_START_BUFF = 40;

    /** Starting buffer position for reference timestamp. 16-19 -- 20-23 */
    int T4_START_BUFF = 16;

    /**
     * Parses the response buffer to extract NTP timestamps.
     *
     * @param buffer The response buffer containing the timestamp data.
     * @param startPos The starting position for the timestamp in the buffer.
     * @return The extracted timestamp value.
     */
    private long responseBuffer(byte[] buffer,int startPos){
        byte b0 = buffer[startPos];
        byte b1 = buffer[startPos+1];
        byte b2 = buffer[startPos+2];
        byte b3 = buffer[startPos+3];

        byte b4 = buffer[startPos+4];
        byte b5 = buffer[startPos+5];
        byte b6 = buffer[startPos+6];
        byte b7 = buffer[startPos+7];
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
     * Prepares an NTP request packet with the current timestamp.
     *
     * @return A Pair containing the prepared NTP request packet and the current UNIX timestamp.
     */
    private Pair<byte[], Long> prepareNtpRequest() {
        // Initialize a byte array for the NTP request.
        byte[] ntpRequest = new byte[48];
        ntpRequest[0] = 0x23;
        //Explanation of the byte config
        //00100011 = 0x23
        //bits 0-1 Leap indicator(LI) '00' = No Leap second
        //bits 2-4 version number '100' = version 4 NTP protocol
        //bits 5-7 Mode '011' = client mode

        long currentUnixTimeMillis = System.currentTimeMillis();
        long ntpSeconds = (currentUnixTimeMillis / 1000L) + 2208988800L; // Convert UNIX epoch to NTP epoch
        long ntpFraction = ((currentUnixTimeMillis % 1000L) * 0x100000000L) / 1000L;
        // Populate the Originate Timestamp (bytes 24-31) of the request
        ntpRequest[24] = (byte) (ntpSeconds << 24);
        ntpRequest[25] = (byte) (ntpSeconds << 16);
        ntpRequest[26] = (byte) (ntpSeconds << 8);
        ntpRequest[27] = (byte) (ntpSeconds);

        ntpRequest[28] = (byte) (ntpFraction << 24);
        ntpRequest[29] = (byte) (ntpFraction << 16);
        ntpRequest[30] = (byte) (ntpFraction << 8);
        ntpRequest[31] = (byte) (ntpFraction);
        return new Pair<>(ntpRequest, currentUnixTimeMillis);
    }

    /**
     * Fetches the NTP time from the specified NTP server. This method sends an NTP request,
     * receives the response, and calculates the offset.
     *
     * @return The calculated offset value representing the NTP time.
     * @throws IOException if there's an error during network communication.
     */
    public long fetchNtp() throws IOException {
        // Prepare the NTP request packet and get t1.
        Pair<byte[], Long> preparedData = prepareNtpRequest();
        byte[] ntpRequest = preparedData.first;
        long t1 = preparedData.second;
        // Create a byte array to store the server's response
        byte[] buffer = new byte[48];

        // Use a try-with-resources for DatagramSocket, which auto-closes due to AutoCloseable.
        try(DatagramSocket socket = new DatagramSocket()){

            InetAddress address = InetAddress.getByName(NTP_HOST);

            //DatagramPacket(byte[] buf, int length, InetAddress address, int port)
            DatagramPacket packet = new DatagramPacket(ntpRequest, ntpRequest.length,address,NTP_PORT);

            // send the NTP request to the server
            socket.send(packet);

            //Receive the server's response
            //DatagramPacket(byte[] buf, int length)
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);

            socket.receive(response);

            long t2 = responseBuffer(buffer,T2_START_BUFF);
            long t3 = responseBuffer(buffer,T3_START_BUFF);
            long t4 = responseBuffer(buffer,T4_START_BUFF);
            long offset = (t2 - t1) + (t3 - t4)/2;

            return offset;
            //Catch Socket Exceptions
        } catch (SocketException e) {
            //rethrow the exception as a RuntimeException
            throw new RuntimeException(e);
        }
    }
}
