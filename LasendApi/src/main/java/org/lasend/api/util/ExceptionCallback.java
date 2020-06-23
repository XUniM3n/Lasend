package org.lasend.api.util;

import org.lasend.api.model.Device;

public interface ExceptionCallback {
    void onException(Exception e, Thread t);

    void onReceiveFileException(Exception e);

    void onMessageSendException(Exception e);

    void onSendContactRequestException(Exception e, Device receiver);

    void onSendContactAcceptException(Exception e, Device receiver);

    void onSendContactRejectException(Exception e, Device receiver);

    void onReceiveContactRequestException(Exception e, Device sender);

    void onReceiveContactAcceptException(Exception e, Device sender);

    void onReceiveContactRejectException(Exception e, Device sender);
}
