package org.lasend.api.network.impl.socket;

import org.lasend.api.network.Sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpSocketSender implements Sender {
    protected static DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    protected static void send(DatagramPacket packet) throws IOException {
        socket.send(packet);
    }
}
