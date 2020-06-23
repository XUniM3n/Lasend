package org.lasend.api.constant;

import lombok.Getter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class NetworkConstants {
    public final static int PORT_LISTEN = 49520;
    public final static String ADVERTISEMENT_MULTICAST_MESSAGE = "MLT";
    public final static String ADVERTISEMENT_UNICAST_MESSAGE = "UNI";
    @Getter
    private static InetAddress MULTICAST_ADDRESS;

    static {
        try {
            MULTICAST_ADDRESS = InetAddress.getByName("239.87.19.52");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
