package org.lasend.api.exception;

import org.lasend.api.model.Device;

public class UnknownInviteAcceptException extends Exception {
    public UnknownInviteAcceptException(Device remoteDevice) {
        super("Received invite acceptance from " + remoteDevice.getIdentity() + ". But Lasend doesn't have corresponding invite.");
    }
}
