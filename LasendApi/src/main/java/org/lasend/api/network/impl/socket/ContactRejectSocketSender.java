package org.lasend.api.network.impl.socket;

import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.ContactRejectDto;
import org.lasend.api.dto.DataDto;
import org.lasend.api.dto.response.ResponseData;
import org.lasend.api.dto.response.SuccessResponse;
import org.lasend.api.exception.InvalidResponseException;
import org.lasend.api.exception.UnknownInviteAcceptException;
import org.lasend.api.model.ContactRequestReceived;
import org.lasend.api.model.Device;
import org.lasend.api.network.ContactRequestRejectSender;
import org.lasend.api.service.SentDataProcessor;
import org.lasend.api.state.LasendStore;

import java.io.IOException;

public class ContactRejectSocketSender extends TcpSocketSender implements ContactRequestRejectSender {
    private final Device remoteDevice;

    public ContactRejectSocketSender(Device remoteDevice, LasendStore store, LasendCallbacks callbacks) {
        super(remoteDevice.getAddress(), NetworkConstants.PORT_LISTEN, store, callbacks);
        this.remoteDevice = remoteDevice;
    }

    @Override
    public void sendContactRequestReject(ContactRequestReceived invite) throws IOException, InvalidResponseException, UnknownInviteAcceptException {
        try {
            if (!isConnected) {
                connect();
            }

            ContactRejectDto inviteReject = new ContactRejectDto(store.getPersistent().getMeStore().getMe().getIdentity());

            sendObject(inviteReject);

            ResponseData response = responseObjectMapper.readValue(reader, ResponseData.class);

            if(!(response instanceof SuccessResponse)){
                throw new InvalidResponseException("Error sending Contact reject");
            }

            SentDataProcessor.processContactRejectData(invite, inviteReject, remoteDevice, store, senderCallback, exceptionCallback);

            stop();
        } catch (InvalidResponseException | IOException e) {
            exceptionCallback.onException(e, Thread.currentThread());
            throw e;
        }
    }
}
