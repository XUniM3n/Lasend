package org.lasend.api.network;

import org.lasend.api.dto.ContactRequestDto;
import org.lasend.api.exception.DeviceAlreadyInContactsException;
import org.lasend.api.exception.InvalidResponseException;

import java.io.IOException;

public interface ContactRequestSender extends Sender {
    void sendContactRequest(ContactRequestDto invite) throws InvalidResponseException, IOException, DeviceAlreadyInContactsException;
}
