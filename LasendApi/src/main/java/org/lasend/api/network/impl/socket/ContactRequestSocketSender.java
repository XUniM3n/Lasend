package org.lasend.api.network.impl.socket;

import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.ContactRequestDto;
import org.lasend.api.dto.DataDto;
import org.lasend.api.dto.response.ResponseData;
import org.lasend.api.dto.response.SuccessResponse;
import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.InvalidResponseException;
import org.lasend.api.model.Device;
import org.lasend.api.network.ContactRequestSender;
import org.lasend.api.service.SentDataProcessor;
import org.lasend.api.state.LasendStore;

import javax.xml.ws.Response;
import java.io.IOException;

public class ContactRequestSocketSender extends TcpSocketSender implements ContactRequestSender {
    private final Device remoteDevice;

    public ContactRequestSocketSender(Device remoteDevice, LasendStore store, LasendCallbacks callbacks) {
        super(remoteDevice.getAddress(), NetworkConstants.PORT_LISTEN, store, callbacks);
        this.remoteDevice = remoteDevice;
    }

    @Override
    public void sendContactRequest(ContactRequestDto invite) throws InvalidResponseException, IOException, DeviceAlreadyInContactsException {
        try {
            if (!isConnected) {
                connect();
            }

            sendObject(invite);

            ResponseData response = responseObjectMapper.readValue(reader, ResponseData.class);

            if(!(response instanceof SuccessResponse)){
                throw new InvalidResponseException("Error sending Contact request");
            }

            SentDataProcessor.processContactRequestData(invite, remoteDevice, store, senderCallback, exceptionCallback);

            stop();
        } catch (InvalidResponseException | IOException | DeviceAlreadyInContactsException e) {
            exceptionCallback.onException(e, Thread.currentThread());
            throw e;
        }
    }
}
