package org.lasend.api.network;

import org.lasend.api.exception.InvalidResponseException;
import org.lasend.api.exception.UnknownInviteAcceptException;
import org.lasend.api.model.ContactRequestReceived;

import java.io.IOException;

public interface ContactRequestRejectSender extends Sender {
    void sendContactRequestReject(ContactRequestReceived invite) throws IOException, InvalidResponseException, UnknownInviteAcceptException;
}