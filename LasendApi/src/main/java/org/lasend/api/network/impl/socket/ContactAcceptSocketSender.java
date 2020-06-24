package org.lasend.api.network.impl.socket;

import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.ContactAcceptDto;
import org.lasend.api.dto.response.ResponseData;
import org.lasend.api.dto.response.SuccessResponse;
import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.InvalidResponseException;
import org.lasend.api.exception.UnknownInviteAcceptException;
import org.lasend.api.model.ContactRequestReceived;
import org.lasend.api.model.Device;
import org.lasend.api.network.ContactRequestAcceptSender;
import org.lasend.api.service.SentDataProcessor;
import org.lasend.api.state.LasendStore;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.IOException;
import java.util.UUID;

public class ContactAcceptSocketSender extends TcpSocketSender implements ContactRequestAcceptSender {
    private final Device remoteDevice;
    private ContactAcceptDto contactAcceptDto;
    private ContactAcceptDto.ContactAcceptDecryptedPayload payload;

    public ContactAcceptSocketSender(Device remoteDevice, LasendStore store, LasendCallbacks callbacks) {
        super(remoteDevice.getAddress(), NetworkConstants.PORT_LISTEN, store, callbacks);
        this.remoteDevice = remoteDevice;
    }

    @Override
    public String sendContactRequestAccept(ContactRequestReceived invite) throws IOException, InvalidResponseException, UnknownInviteAcceptException, UntrustedIdentityException, InvalidKeyException, DeviceAlreadyInContactsException {
        try {
            if (!isConnected) {
                connect();
            }

            if (contactAcceptDto == null) {
                SessionBuilder sessionBuilder = new SessionBuilder(store.getPersistent().getMeStore().getMe().getSignalProtocolStore(), remoteDevice.getSignalProtocolAddress());
                sessionBuilder.process(invite.getPreKeyBundle());
                SessionCipher sessionCipher = new SessionCipher(store.getPersistent().getMeStore().getMe().getSignalProtocolStore(), remoteDevice.getSignalProtocolAddress());
                payload = new ContactAcceptDto.ContactAcceptDecryptedPayload(UUID.randomUUID().toString());
                contactAcceptDto = new ContactAcceptDto(payload, store.getPersistent().getMeStore().getMe().getIdentity(), sessionCipher);
            }

            sendObject(contactAcceptDto);

            ResponseData response = responseObjectMapper.readValue(reader, ResponseData.class);

            if (!(response instanceof SuccessResponse)) {
                throw new InvalidResponseException("Error sending contact acceptance");
            }

            String newChatId = SentDataProcessor.processContactAcceptData(invite, contactAcceptDto, payload, remoteDevice, store, senderCallback, exceptionCallback);

            close();

            return newChatId;
        } catch (InvalidResponseException | IOException | UnknownInviteAcceptException | UntrustedIdentityException | InvalidKeyException | DeviceAlreadyInContactsException e) {
            exceptionCallback.onException(e, Thread.currentThread());
            throw e;
        }
    }
}
