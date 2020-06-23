package org.lasend.api;

import org.lasend.api.callback.DeviceDiscoveredCallback;
import org.lasend.api.network.Receiver;
import org.lasend.api.network.Sender;
import org.lasend.api.util.ExceptionCallback;

public interface LasendCallbacks {
    ExceptionCallback getExceptionCallback();

    Sender.SenderCallback getSenderCallback();

    Receiver.ReceiverCallback getReceiverCallback();

    DeviceDiscoveredCallback getDeviceDiscoveredCallback();
}
