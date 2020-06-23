package org.lasend.api.network;

import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.InvalidResponseException;
import org.lasend.api.exception.UnknownInviteAcceptException;
import org.lasend.api.model.ContactRequestReceived;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.IOException;

public interface ContactRequestAcceptSender extends Sender {
    String sendContactRequestAccept(ContactRequestReceived invite) throws IOException, InvalidResponseException, UnknownInviteAcceptException, UntrustedIdentityException, InvalidKeyException, DeviceAlreadyInContactsException;
}
