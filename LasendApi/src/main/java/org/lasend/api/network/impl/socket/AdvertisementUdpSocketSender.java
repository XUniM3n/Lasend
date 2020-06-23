package org.lasend.api.network.impl.socket;

import org.lasend.api.constant.NetworkConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class AdvertisementUdpSocketSender extends UdpSocketSender {


    public static void sendMulticast() throws IOException {
        String sendString = NetworkConstants.ADVERTISEMENT_MULTICAST_MESSAGE;
        byte[] sendData = sendString.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, NetworkConstants.getMULTICAST_ADDRESS(), NetworkConstants.PORT_LISTEN);
        send(sendPacket);
    }

    public static void sendUnicast(InetAddress remoteAddress) throws IOException {
        String sendString = NetworkConstants.ADVERTISEMENT_UNICAST_MESSAGE;
        byte[] sendData = sendString.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, remoteAddress, NetworkConstants.PORT_LISTEN);
        send(sendPacket);
    }
}
