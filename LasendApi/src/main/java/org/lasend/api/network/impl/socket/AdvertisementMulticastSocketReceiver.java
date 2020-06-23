package org.lasend.api.network.impl.socket;

import lombok.Getter;
import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.network.AdvertisementReceiver;
import org.lasend.api.service.ReceivedDataProcessor;
import org.lasend.api.state.LasendStore;

import java.io.IOException;
import java.net.*;

public class AdvertisementMulticastSocketReceiver implements AdvertisementReceiver, Runnable {

    private int listenPort;
    private MulticastSocket serverSocket;
    @Getter
    private boolean isListening;
    private LasendStore store;
    private LasendCallbacks callbacks;
    private Thread thread;

    public AdvertisementMulticastSocketReceiver(LasendStore store, LasendCallbacks callbacks) {
        this.listenPort = NetworkConstants.PORT_LISTEN;
        this.store = store;
        this.callbacks = callbacks;
    }

    @Override
    public void run() {
        try {
            serverSocket = new MulticastSocket(listenPort);;
            serverSocket.joinGroup(NetworkConstants.getMULTICAST_ADDRESS());
        } catch (IOException e) {
            callbacks.getExceptionCallback().onException(e, Thread.currentThread());
        }
        isListening = true;
        while (isListening) {
            byte[] receiveData = new byte[3];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String receivedString = new String(receivePacket.getData(), 0, receivePacket.getLength());
            ReceivedDataProcessor.processAdvertisementData(receivedString, receivePacket.getAddress(), store, callbacks);
        }
    }

    @Override
    public void start() {
        this.thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() {
        isListening = false;
        serverSocket.close();
    }

}
